package com.atlassian.plugins.tutorial.refapp;

import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import com.google.common.collect.Maps;


@Named("MyPluginServlet")
public class MyPluginServlet extends HttpServlet
{

//    private static final Logger log = LoggerFactory.getLogger(MyPluginServlet.class);
    private static final String PLUGIN_STORAGE_KEY="com.atlassian.plugins.tutorial.refapp.adminUI";

    @ComponentImport
    private final UserManager userManager;
    @ComponentImport
    private final LoginUriProvider loginUriProvider;
    @ComponentImport
    private final TemplateRenderer  templateRenderer;
    @ComponentImport
    private final PluginSettingsFactory pluginSettingsFactory;

    @Inject
    public MyPluginServlet(UserManager userManager, LoginUriProvider loginUriProvider, TemplateRenderer templateRenderer, PluginSettingsFactory pluginSettingsFactory)
    {
        this.userManager = userManager;
        this.loginUriProvider = loginUriProvider;
        this.templateRenderer = templateRenderer;

        this.pluginSettingsFactory = pluginSettingsFactory;
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

        Map<String, Object> context = Maps.newHashMap();

        PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();

        if( pluginSettings.get(PLUGIN_STORAGE_KEY + ".name")==null)
        {
            String noName = "Enter your name here ";
            pluginSettings.put(PLUGIN_STORAGE_KEY + ".name", noName);
        }

        if( pluginSettings.get(PLUGIN_STORAGE_KEY + ".age")==null)
        {
            String noAge = "Enter your age here ";
            pluginSettings.put(PLUGIN_STORAGE_KEY + ".age", noAge);
        }

        context.put("name", pluginSettings.get(PLUGIN_STORAGE_KEY + ".name"));
        context.put("age",  pluginSettings.get(PLUGIN_STORAGE_KEY + ".age"));
        // resp.setContentType("text/html");
        // resp.getWriter().write("<html><body>Hello! from MyPluginServlet.</body></html>");
        resp.setContentType("text/html;charset=utf-8");
        templateRenderer.render("admin.vm", resp.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();

        pluginSettings.put(PLUGIN_STORAGE_KEY + ".name",req.getParameter("name"));
        pluginSettings.put(PLUGIN_STORAGE_KEY + ".age", req.getParameter("age"));

        resp.sendRedirect("hello");
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