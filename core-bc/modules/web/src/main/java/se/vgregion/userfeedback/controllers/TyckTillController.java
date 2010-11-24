package se.vgregion.userfeedback.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import se.vgregion.userfeedback.domain.FormTemplate;
import se.vgregion.userfeedback.domain.FormTemplateRepository;
import se.vgregion.userfeedback.domain.UserFeedback;

import javax.persistence.NoResultException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * @author <a href="mailto:david.rosell@redpill-linpro.com">David Rosell</a>
 */

@Controller
@RequestMapping(value = {"/", "/KontaktaOss"})
@SessionAttributes("userFeedback")
public class TyckTillController {
    private String mainHeading = "Kontakta oss";
    private String leadText = "Här kan du lämna synpunkter och ställa frågor om webbplatsen. Har du frågor om vården kan du ställa dem här också.";

    @Autowired
    private FormTemplateRepository formTemplateRepository;

    @RequestMapping(method = RequestMethod.GET)
    public String setupForm(@RequestParam(value="formName", required = false) String formName,
                          @RequestParam(value="breadcrumb", required = false) String breadcrumb,
                          ModelMap model) {


        FormTemplate template = lookupFormTemplate(formName);
        model.addAttribute("template", template);

        UserFeedback userFeedback;
        if (!model.containsKey("userFeedback")) {
            userFeedback = new UserFeedback();
            userFeedback.setCaseSubject(null);
            model.addAttribute("userFeedback", userFeedback);
        } else {
            userFeedback = (UserFeedback)model.get("userFeedback");
        }
        userFeedback.setBreadcrumb(breadcrumb);

        model.addAttribute("subject_content", UserFeedback.CaseSubject.webpageContent);
        model.addAttribute("subject_function", UserFeedback.CaseSubject.webpageFunction);
        model.addAttribute("subject_healthcare", UserFeedback.CaseSubject.healthcare);
        model.addAttribute("subject_other", UserFeedback.CaseSubject.other);

        model.addAttribute("healthcareCategories", UserFeedback.HealthcareCategory.getLabelMap());

        model.addAttribute("contactOptions", UserFeedback.UserContactOption.getLabelMap());

        return "KontaktaOss";

    }

    private FormTemplate lookupFormTemplate(String formName) {
        FormTemplate template;
        try {
            template = formTemplateRepository.find(formName);
        } catch (NoResultException nrex) {
            template = formTemplateRepository.find("default");
        }

        return template;
    }

    @RequestMapping(method = RequestMethod.POST)
    public String sendUserFeedback(@ModelAttribute("userFeedback") UserFeedback userFeedback,
                                   MultipartHttpServletRequest multipartRequest,
                                   ModelMap model) {
        System.out.println("Sending...");

        for (Iterator<String> filenameIterator = multipartRequest.getFileNames();filenameIterator.hasNext();) {
            String fileName = filenameIterator.next();

            processAttachment(multipartRequest.getFile(fileName), userFeedback);
        }

        UserFeedback.CaseSubject caseSubject = userFeedback.getCaseSubject();
        if (caseSubject != null) {
            System.out.println("CaseSubject: " +caseSubject.getName());
        }

        System.out.println("Message: "+ userFeedback.getMessage());

        return "Tacksida";
    }

    private void processAttachment(MultipartFile file, UserFeedback userFeedback) {
        if (file.isEmpty()) return;

        Map<String, byte[]> attachmentMap = userFeedback.getAttachments().getFileMap();
        try {
            attachmentMap.put(file.getOriginalFilename(), file.getBytes());
            System.out.println("File: "+file.getOriginalFilename());
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
