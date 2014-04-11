package org.vaadin.example;

import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.inject.Inject;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.vaadin.example.login.UserLoggedInEvent;
import org.vaadin.maddon.label.Header;

import com.vaadin.annotations.Theme;
import com.vaadin.cdi.CDIUI;
import com.vaadin.cdi.CDIViewProvider;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ChameleonTheme;

@CDIUI
@Theme("dawn")
public class VaadinUI extends UI {
    private static final long serialVersionUID = 3618386613849364696L;

    private Navigator navigator;

    @Inject
    private CDIViewProvider viewProvider;

    private HorizontalLayout topBar;

    private Button logout;

    private Header header;

    @Inject
    private ApplicationViewArea viewArea;

    private final Button.ClickListener logoutClickListener = new Button.ClickListener() {
        private static final long serialVersionUID = -1545988729141348821L;

        @Override
        public void buttonClick(ClickEvent event) {
            SecurityUtils.getSubject().logout();
            VaadinSession.getCurrent().close();
            Page.getCurrent().setLocation("");
        }
    };

    @Override
    protected void init(VaadinRequest request) {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setMargin(true);

        topBar = new HorizontalLayout();
        topBar.setWidth(100, Unit.PERCENTAGE);
        topBar.setSpacing(true);

        header = new Header("Welcome, please login");
        header.setWidth(100, Unit.PERCENTAGE);

        logout = new Button("Logout", logoutClickListener);
        logout.setStyleName(ChameleonTheme.BUTTON_LINK);

        topBar.addComponents(header, logout);
        topBar.setExpandRatio(header, 1);

        layout.addComponents(topBar, viewArea.getViewContainer());
        layout.setExpandRatio(viewArea.getViewContainer(), 1);

        setContent(layout);

        navigator = new Navigator(this, viewArea);
        navigator.addProvider(viewProvider);

        if (!isLoggedIn()) {
            navigator.navigateTo("");
            logout.setVisible(false);
        } else {
            if (navigator.getState().isEmpty()) {
                navigator.navigateTo("customers");
                logout.setVisible(true);
            }
        }
    }

    private boolean isLoggedIn() {
        Subject subject = SecurityUtils.getSubject();
        if (subject == null) {
            System.err.println("Could not find subject");
            return false;
        }

        return subject.isAuthenticated();
    }

    public void userLoggedIn(
            @Observes(notifyObserver = Reception.IF_EXISTS) UserLoggedInEvent event) {
        Notification.show("Welcome back " + event.getUsername());
        navigator.navigateTo("customers");
        logout.setVisible(true);
    }

    public void onViewNavigated(
            @Observes(notifyObserver = Reception.IF_EXISTS) ViewNavigationEvent event) {
        header.setText(event.getViewName());
    }
}
