package org.beckn.app.registry.configuration;

import com.venky.core.util.ObjectUtil;
import com.venky.swf.configuration.Installer;
import com.venky.swf.db.Database;
import com.venky.swf.plugins.collab.db.model.config.Role;
import org.beckn.app.registry.db.model.DefaultUserRoles;

public class AppInstaller implements Installer{

  public void install() {
      installRoles();
  }

  public void installRoles(){
    if (Database.getTable(Role.class).isEmpty()) {
      for (String allowedRole : DefaultUserRoles.ALLOWED_ROLES) {
        Role role = Database.getTable(Role.class).newRecord();
        role.setName(allowedRole);
        if (!ObjectUtil.equals(allowedRole,DefaultUserRoles.ALLOWED_ROLES[0])){
          role.setStaff(true);
        }
        role.save();
      }
    }
    Role admin = com.venky.swf.plugins.security.db.model.Role.getRole(Role.class,"ADMIN");
    if (!admin.isStaff()){
      admin.setStaff(true);
      admin.save();
    }
  }
}

