package fr.xebia.confluence2wordpress.core.converter;

import java.util.List;
import java.util.Map;

import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.util.velocity.VelocityUtils;

import fr.xebia.confluence2wordpress.core.converter.visitors.Heading;


public class VelocityHelper {

    public String generateHeader() {
        // Create the Velocity Context
        Map<String,Object> context = MacroUtils.defaultVelocityContext();
        // Render the Template
        String result = VelocityUtils.getRenderedTemplate("/vm/rdp-header.vm", context);
        return result;
    }

    public String generateTOC(List<Heading> headings) {
        // Create the Velocity Context
        Map<String,Object> context = MacroUtils.defaultVelocityContext();
        context.put("headings", headings);
        // Render the Template
        String result = VelocityUtils.getRenderedTemplate("/vm/toc.vm", context);
        return result;
    }

}