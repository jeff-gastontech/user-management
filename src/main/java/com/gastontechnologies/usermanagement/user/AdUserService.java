package com.gastontechnologies.usermanagement.user;

import com.microsoft.graph.models.DirectoryObject;
import com.microsoft.graph.models.Group;
import com.microsoft.graph.requests.GraphServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdUserService {

    @Autowired
    private GraphServiceClient graphClient;


    public List<User> getUsersByGroup(Group group) {

        List<DirectoryObject> members =  graphClient.groups(group.id).members().buildRequest().get().getCurrentPage();

        List<User> users = new ArrayList<>();

        for (DirectoryObject dO : members) {
            com.microsoft.graph.models.User user = (com.microsoft.graph.models.User) dO;
            users.add(User.builder().mysqlUsername(user.givenName + "." + user.surname + "." + group.displayName).build());
        }

        return users;
    }

}
