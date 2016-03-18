package com.atlassian.plugins.tutorial.refapp;

import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import java.net.URI;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import javax.inject.Named;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;

@Named("MyPluginServlet")
public class MyPluginServlet extends HttpServlet
{

    private static final Logger log = LoggerFactory.getLogger(MyPluginServlet.class);

    @ComponentImport
    private final UserManager userManager;
    @ComponentImport
    private final LoginUriProvider loginUriProvider;
    @ComponentImport
    private final TemplateRenderer  templateRenderer;

    @Inject
    public MyPluginServlet(UserManager userManager, LoginUriProvider loginUriProvider, TemplateRenderer templateRenderer)
    {
        this.userManager = userManager;
        this.loginUriProvider = loginUriProvider;
        this.templateRenderer = templateRenderer;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        // since API 2.10, getRemoteUserName & isSystemAdmin are deprecated
        UserKey userKey = userManager.getRemoteUserKey(req);
        if( (userKey == null) || (!userManager.isAdmin(userKey)))
        {
            redirectToLogin(req, resp);
            return;
        }

        // resp.setContentType("text/html");
        // resp.getWriter().write("<html><body>Hello! from MyPluginServlet.</body></html>");
        templateRenderer.render("admin.vm", resp.getWriter());
    }

    private void redirectToLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        resp.sendRedirect(loginUriProvider.getLoginUri(getUri(req)).toASCIIString());
    }

    private URI getUri(HttpServletRequest request)
    {
        StringBuffer builder = request.getRequestURL();
        if (request.getQueryString() != null)
        {
            builder.append("?");
            builder.append(request.getQueryString());
        }
        return URI.create(builder.toString());
    }
}