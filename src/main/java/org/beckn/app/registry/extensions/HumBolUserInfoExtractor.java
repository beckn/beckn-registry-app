package org.beckn.app.registry.extensions;

import com.venky.cache.Cache;
import com.venky.core.date.DateUtils;
import com.venky.core.io.ByteArrayInputStream;
import com.venky.core.util.ObjectUtil;
import com.venky.extension.Registry;
import com.venky.swf.controller.OidController.OIDProvider;

import com.venky.swf.db.Database;
import com.venky.swf.db.model.application.ApplicationPublicKey;
import com.venky.swf.db.model.io.ModelIOFactory;
import com.venky.swf.extensions.SocialLoginInfoExtractor;
import com.venky.swf.integration.FormatHelper;
import com.venky.swf.integration.FormatHelper.KeyCase;
import com.venky.swf.path.Path;
import com.venky.swf.path._IPath;
import com.venky.swf.plugins.collab.db.model.config.State;
import com.venky.swf.plugins.collab.db.model.participants.Application;
import com.venky.swf.plugins.collab.db.model.participants.admin.Company;
import com.venky.swf.plugins.collab.db.model.user.User;
import com.venky.swf.plugins.collab.db.model.user.UserEmail;
import com.venky.swf.plugins.collab.db.model.user.UserPhone;
import com.venky.swf.plugins.security.db.model.Role;
import com.venky.swf.plugins.security.db.model.UserRole;
import in.succinct.beckn.Request;
import in.succinct.beckn.Subscriber;
import in.succinct.beckn.registry.db.model.City;
import in.succinct.beckn.registry.db.model.Country;
import in.succinct.beckn.registry.db.model.onboarding.NetworkDomain;
import in.succinct.beckn.registry.db.model.onboarding.NetworkParticipant;
import in.succinct.beckn.registry.db.model.onboarding.NetworkRole;
import in.succinct.beckn.registry.db.model.onboarding.ParticipantKey;
import in.succinct.beckn.registry.db.model.onboarding.SubmittedDocument;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class HumBolUserInfoExtractor extends SocialLoginInfoExtractor {
    static {
        Registry.instance().registerExtension(SocialLoginInfoExtractor.class.getName(),new HumBolUserInfoExtractor());
    }

    @Override
    public JSONObject extractUserInfo(OIDProvider provider, OAuthResourceResponse resourceResponse) {
        JSONObject userInfo = (JSONObject) JSONValue.parse(new InputStreamReader(resourceResponse.getBodyAsInputStream()));
        if (userInfo.containsKey("User")){
            userInfo = (JSONObject) userInfo.remove("User");
        }

        JSONObject jsCity = (JSONObject) userInfo.get("City");
        JSONObject jsState = jsCity != null ? (JSONObject) jsCity.get("State") : null ;
        JSONObject jsCountry = jsState != null ? (JSONObject) jsState.get("Country") : null ;
        JSONArray userDocuments = new JSONArray();
        if (userInfo.containsKey("UserDocuments")) {
            userDocuments = (JSONArray) userInfo.remove("UserDocuments");
        }

        if (jsCountry != null){
            Country country  =  ModelIOFactory.getReader(Country.class,JSONObject.class).read(jsCountry);
            country.save();
        }
        if (jsState != null){
            State state =  ModelIOFactory.getReader(State.class,JSONObject.class).read(jsState);
            state.save();
        }
        if (jsCity != null){
            City city =  ModelIOFactory.getReader(City.class,JSONObject.class).read(jsCity);
            city.save();
        }

        JSONObject userPhonesInfo = new JSONObject();
        userPhonesInfo.put("UserPhones",new JSONArray());
        if (userInfo.containsKey("UserPhones")){
            userPhonesInfo.put("UserPhones",userInfo.remove("UserPhones"));
        }

        JSONObject userEmails = new JSONObject();
        userEmails.put("UserEmails",new JSONArray());
        if (userInfo.containsKey("UserEmails")) {
            userEmails.put("UserEmails", userInfo.remove("UserEmails"));
        }
        JSONArray userEmailsArray = (JSONArray) userEmails.get("UserEmails");

        JSONArray userApplicationsArray  = (JSONArray) userInfo.remove("Applications");
        if (userApplicationsArray != null){
            for (Object a : userApplicationsArray){
                JSONObject applicationJS = (JSONObject) a;
                importApplication(applicationJS);
            }
        }
        User user = ModelIOFactory.getReader(User.class,JSONObject.class).read(userInfo);
        user.save();


        Path path = Database.getInstance().getContext(_IPath.class.getName());
        path.createUserSession(user,true);
        Database.getInstance().open(user);

        UserRole userRole = Database.getTable(UserRole.class).newRecord();
        userRole.setUserId(user.getId());
        userRole.setRoleId(Role.getRole("USER").getId());
        userRole = Database.getTable(UserRole.class).getRefreshed(userRole);
        userRole.save();

        for (Iterator<?> i =  userEmailsArray.iterator(); i.hasNext() ; ) {
            Object o = i.next();
            JSONObject userEmail = (JSONObject) o;

            JSONObject companyJS = (JSONObject) (userEmail.remove("Company"));
            if (!ObjectUtil.equals("Y",userEmail.get("Validated"))){
                i.remove(); //Don't pull unvalidated emails
                path.addErrorMessage(String.format("Unvalidated email %s was skipped",userEmail.get("Email")));
                continue;
            }

            if (companyJS == null || !ObjectUtil.equals("Y",userEmail.get("CompanyAdmin"))){
                continue;
            }
            userRole = Database.getTable(UserRole.class).newRecord();
            userRole.setUserId(user.getId());
            userRole.setRoleId(Role.getRole("ADMIN").getId());
            userRole = Database.getTable(UserRole.class).getRefreshed(userRole);
            userRole.save();

            JSONArray applicationsArray = (JSONArray) companyJS.remove("Applications");
            Company company = ModelIOFactory.getReader(Company.class,JSONObject.class).read(companyJS);
            company.save();

            NetworkParticipant networkParticipant = NetworkParticipant.find(company.getDomainName());
            networkParticipant.save();


            JSONObject attachmentsInfo = new JSONObject();
            JSONArray attachments =  new JSONArray();
            attachmentsInfo.put("Attachments",attachments);//Network participant id to be set.


            userDocuments.forEach(ud->{
                JSONObject attachment = new JSONObject();
                JSONObject userDocument = (JSONObject)ud;
                attachment.put("File",userDocument.get("File"));
                attachment.put("FileContentName",userDocument.get("FileContentName"));
                attachment.put("FileContentType",userDocument.get("FileContentType"));
                attachment.put("FileContentSize",userDocument.get("FileContentSize"));
                attachment.put("DocumentType",userDocument.get("DocumentType"));
                attachment.put("NetworkParticipantId",networkParticipant.getId());
                attachments.add(attachment);
            });
            try {
                ModelIOFactory.getReader(SubmittedDocument.class, JSONObject.class).read(new ByteArrayInputStream(attachmentsInfo.toString().getBytes(StandardCharsets.UTF_8))).forEach(a->a.save());
            }catch (Exception ex){
                throw new RuntimeException(ex);
            }



            userEmail.put("CompanyId",company.getId());
            if (applicationsArray == null){
                continue;
            }
            for (Object a : applicationsArray){
                JSONObject applicationJS = (JSONObject) a;
                importApplication(applicationJS,company,networkParticipant);
            }
        }


        try {
            ((JSONArray)userEmails.get("UserEmails")).forEach(ue->((JSONObject)ue).put("UserId",user.getId()));
            ((JSONArray)userPhonesInfo.get("UserPhones")).forEach(ue->((JSONObject)ue).put("UserId",user.getId()));

            ModelIOFactory.getReader(UserEmail.class, JSONObject.class).read(new ByteArrayInputStream(userEmails.toString().getBytes(StandardCharsets.UTF_8))).forEach(o -> o.save());
            ModelIOFactory.getReader(UserPhone.class, JSONObject.class).read(new ByteArrayInputStream(userPhonesInfo.toString().getBytes(StandardCharsets.UTF_8))).forEach(o -> o.save());
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }

        JSONObject out = new JSONObject();
        ModelIOFactory.getWriter(User.class,JSONObject.class).write(user,out, user.getReflector().getVisibleFields(new ArrayList<>()));
        FormatHelper.instance(out).change_key_case(KeyCase.SNAKE);
        return out;
    }
    private void importApplication(JSONObject applicationJS){
        NetworkParticipant networkParticipant = NetworkParticipant.find((String)applicationJS.get("AppId"));
        networkParticipant.save();
        importApplication(applicationJS,null, networkParticipant);
    }
    private void importApplication(JSONObject applicationJS, Company company, NetworkParticipant networkParticipant) {
        JSONArray applicationPublicKeysArray = (JSONArray) applicationJS.remove("ApplicationPublicKeys");
        JSONArray endPoints = (JSONArray) applicationJS.remove("EndPoints");
        if (endPoints == null || endPoints.isEmpty()){
            return;
        }
        if (applicationPublicKeysArray == null || applicationPublicKeysArray.isEmpty()){
            return;
        }

        if (company != null) {
            applicationJS.put("CompanyId", company.getId());
        }
        Application application = ModelIOFactory.getReader(Application.class,JSONObject.class).read(applicationJS,false);
        application.save();

        for (Object ep : endPoints){
            JSONObject endPoint = (JSONObject) ep;
            JSONObject openApi = endPoint != null ? (JSONObject) endPoint.remove("OpenApi") : null;
            String type = null;
            if (openApi!= null && (Arrays.asList(NetworkRole.SUBSCRIBER_ENUM.split(",")).contains(openApi.get("Name")) )){
                type = ((String)openApi.get("Name"));
            }else {
                type = (NetworkRole.SUBSCRIBER_TYPE_UNKNOWN);
            }

            final String appRole = type;
            NetworkRole role = NetworkRole.find(new Subscriber(){{
                setSubscriberId(application.getAppId());
                setType(appRole);
            }});
            role.setNetworkParticipantId(networkParticipant.getId());
            //role.setType(type);
            //role.setSubscriberId(application.getAppId());
            if (role.getRawRecord().isNewRecord()) {
                if (endPoint != null) {
                    role.setUrl((String)endPoint.get("BaseUrl"));
                }
                String nicCode = (String)applicationJS.get("IndustryClassificationCode");
                if (!ObjectUtil.isVoid(nicCode)){
                    NetworkDomain networkDomain =  NetworkDomain.find(nicCode);
                    if (!networkDomain.getRawRecord().isNewRecord()){
                        role.setNetworkDomainId(networkDomain.getId());
                        role.setStatus(NetworkRole.SUBSCRIBER_STATUS_INITIATED);
                    }
                }
            }
            role.save();
        }



        Cache<String,ParticipantKey> cache = new Cache<String, ParticipantKey>() {
            @Override
            protected ParticipantKey getValue(String keyId) {
                ParticipantKey key =Database.getTable(ParticipantKey.class).newRecord();
                key.setKeyId(keyId);
                key.setValidFrom(new Timestamp(0L));
                key.setValidUntil(new Timestamp(DateUtils.HIGH_DATE.getTime()));
                return key;
            }
        };

        for (Object apk : applicationPublicKeysArray) {
            JSONObject applicationPublicKeyJs = (JSONObject) apk;
            applicationPublicKeyJs.put("ApplicationId",application.getId());
            ApplicationPublicKey applicationPublicKey = ModelIOFactory.getReader(ApplicationPublicKey.class,JSONObject.class).read(applicationPublicKeyJs);
            applicationPublicKey.save();

            if (ObjectUtil.equals(applicationPublicKey.getPurpose(),ApplicationPublicKey.PURPOSE_SIGNING) && ObjectUtil.equals(applicationPublicKey.getAlgorithm(), Request.SIGNATURE_ALGO)){
                ParticipantKey pk = cache.get(applicationPublicKey.getKeyId());
                pk.setSigningPublicKey(applicationPublicKey.getPublicKey());
                if (applicationPublicKey.getValidUntil() != null && applicationPublicKey.getValidUntil().before(pk.getValidUntil())) {
                    pk.setValidUntil(applicationPublicKey.getValidUntil());
                }
                if (applicationPublicKey.getValidFrom() != null && applicationPublicKey.getValidFrom().after(pk.getValidFrom())) {
                    pk.setValidFrom(applicationPublicKey.getValidFrom());
                }
            }else if (ObjectUtil.equals(applicationPublicKey.getPurpose(),ApplicationPublicKey.PURPOSE_ENCRYPTION) && ObjectUtil.equals(applicationPublicKey.getAlgorithm(),Request.ENCRYPTION_ALGO)){
                cache.get(applicationPublicKey.getKeyId()).setEncrPublicKey(applicationPublicKey.getPublicKey());
            }
        }
        for (ParticipantKey pk : cache.values()){
            if (networkParticipant != null){
                pk.setNetworkParticipantId(networkParticipant.getId());
                pk.setVerified(true); // Always true
            }
            pk = Database.getTable(ParticipantKey.class).getRefreshed(pk);
            if (!pk.getRawRecord().isNewRecord() || (!ObjectUtil.isVoid(pk.getSigningPublicKey()) && !ObjectUtil.isVoid(pk.getEncrPublicKey())) ) {
                pk.save();
            }
        }
    }
}
