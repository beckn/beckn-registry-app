/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.beckn.app.registry.menu;

import com.venky.swf.db.model.User;
import com.venky.swf.menu.DefaultMenuBuilder;
import com.venky.swf.path._IPath;
import com.venky.swf.routing.Config;
import com.venky.swf.views.controls.page.Menu;
import com.venky.swf.views.controls.page.Menu.SubMenu;

/**
 *
 * @author venky
 */
public class AppMenuBuilder extends DefaultMenuBuilder{
    protected SubMenu userMenu(_IPath path, Menu appmenu, User user){
        SubMenu userMenu = appmenu.getSubmenu(user.getLongName());
        _IPath userPath = path.getModelAccessPath(User.class);

        if (userPath.canAccessControllerAction("edit", String.valueOf(user.getId()))){
            if (Config.instance().getOpenIdProviders().contains("HUMBOL")) {
                userMenu.addMenuItem("Settings", "https://id.humbhionline.in/users/current");
            }else {
                userMenu.addMenuItem("Settings", "/users/edit/" + user.getId());
            }
        }else if (userPath.canAccessControllerAction("show", String.valueOf(user.getId()))){
            if (Config.instance().getOpenIdProviders().contains("HUMBOL")) {
                userMenu.addMenuItem("Settings", "https://id.humbhionline.in/users/current" );
            }else {
                userMenu.addMenuItem("Settings", "/users/show/" + user.getId());
            }
        }

        return userMenu;
    }
}
