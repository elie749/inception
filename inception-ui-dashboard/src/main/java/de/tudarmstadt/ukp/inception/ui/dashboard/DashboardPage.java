/*
 * Copyright 2017
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tudarmstadt.ukp.inception.ui.dashboard;

import static de.tudarmstadt.ukp.clarin.webanno.api.SecurityUtil.annotationEnabeled;
import static de.tudarmstadt.ukp.clarin.webanno.api.SecurityUtil.curationEnabeled;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import de.tudarmstadt.ukp.clarin.webanno.api.ProjectService;
import de.tudarmstadt.ukp.clarin.webanno.api.WebAnnoConst;
import de.tudarmstadt.ukp.clarin.webanno.security.UserDao;
import de.tudarmstadt.ukp.clarin.webanno.security.model.User;
import de.tudarmstadt.ukp.clarin.webanno.ui.core.login.LoginPage;
import de.tudarmstadt.ukp.clarin.webanno.ui.core.menu.MenuItem;
import de.tudarmstadt.ukp.clarin.webanno.ui.core.menu.MenuItemRegistry;
import de.tudarmstadt.ukp.clarin.webanno.ui.core.page.ApplicationPageBase;
import de.tudarmstadt.ukp.inception.ui.dashboard.dashlet.CurrentProjectDashlet;

/**
 * Main menu page.
 */
public class DashboardPage extends ApplicationPageBase
{
    private static final long serialVersionUID = -2487663821276301436L;

    private @SpringBean ProjectService projectService;
    private @SpringBean UserDao userRepository;
    private @SpringBean MenuItemRegistry menuItemService;

    private DashboardMenu menu;

    public DashboardPage()
    {
        setStatelessHint(true);
        setVersioned(false);
        
        // In case we restore a saved session, make sure the user actually still exists in the DB.
        // redirect to login page (if no usr is found, admin/admin will be created)
        User user = userRepository.getCurrentUser();
        if (user == null) {
            setResponsePage(LoginPage.class);
        }
        
        // if not either a curator or annotator, display warning message
        if (
                !annotationEnabeled(projectService, user, WebAnnoConst.PROJECT_TYPE_ANNOTATION) && 
                !annotationEnabeled(projectService, user, WebAnnoConst.PROJECT_TYPE_AUTOMATION) && 
                !annotationEnabeled(projectService, user, WebAnnoConst.PROJECT_TYPE_CORRECTION) && 
                !curationEnabeled(projectService, user)) 
        {
            info("You are not member of any projects to annotate or curate");
        }
        
        menu = new DashboardMenu("menu", LoadableDetachableModel.of(this::getMenuItems));
        add(menu);
        
        add(new CurrentProjectDashlet("currentProjectDashlet"));
    }
    
    private List<MenuItem> getMenuItems()
    {
        return menuItemService.getMenuItems().stream()
                .filter(item -> item.getPath().matches("/[^/]+"))
                .collect(Collectors.toList());
    }
}
