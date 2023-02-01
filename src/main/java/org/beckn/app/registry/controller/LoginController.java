package org.beckn.app.registry.controller;

import com.venky.core.util.ObjectUtil;
import com.venky.swf.controller.Controller;
import com.venky.swf.controller.annotations.RequireLogin;
import com.venky.swf.path.Path;
import com.venky.swf.util.TemplateProcessor;
import com.venky.swf.views.ForwardedView;
import com.venky.swf.views.View;

public class LoginController extends Controller {
    public LoginController(Path path) {
        super(path);
    }
    @RequireLogin(false)
    public View index(){
        if (ObjectUtil.equals(getPath().getRequest().getMethod(),"GET") && getSessionUser() == null &&
                TemplateProcessor.getInstance().exists("/html/index.html")){
            return new ForwardedView(getPath(),"/",
                    "html/index.html" );
        }else {
            return super.login();
        }
    }
}
