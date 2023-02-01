package org.beckn.app.registry.db.model;

import com.venky.swf.db.annotations.column.ui.PROTECTION;

public interface Company extends com.venky.swf.plugins.collab.db.model.participants.admin.Company {
    @PROTECTION
    public String getCompanyGstIn();
    public void setCompanyGstIn(String companyGstIn);

    @PROTECTION
    public String getCompanyRegistrationNumber();
    public void setCompanyRegistrationNumber(String companyRegistrationNumber);
}
