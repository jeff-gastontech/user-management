package com.gastontechnologies.usermanagement.group;

import com.gastontechnologies.usermanagement.UserManagementApplication;
import com.microsoft.graph.models.Group;
import com.microsoft.graph.requests.GraphServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroupService {

    private static final Logger log = LoggerFactory.getLogger(GroupService.class);

    @Autowired
    private GraphServiceClient graphClient;

    public List<Group> getGroupsByName(String name) {
        log.info("Group name: {}", name);
        return graphClient.groups().buildRequest().filter("displayName eq " + "'" + name + "'").get().getCurrentPage();
    }

}
