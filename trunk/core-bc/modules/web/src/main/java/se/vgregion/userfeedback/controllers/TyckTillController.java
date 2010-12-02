package se.vgregion.userfeedback.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.NoResultException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import se.vgregion.userfeedback.FeedbackReport;
import se.vgregion.userfeedback.FeedbackReportService;
import se.vgregion.userfeedback.ReportBuilder;
import se.vgregion.userfeedback.domain.Attachment;
import se.vgregion.userfeedback.domain.AttachmentRepository;
import se.vgregion.userfeedback.domain.CustomCategory;
import se.vgregion.userfeedback.domain.CustomSubCategory;
import se.vgregion.userfeedback.domain.FormTemplate;
import se.vgregion.userfeedback.domain.FormTemplateRepository;
import se.vgregion.userfeedback.domain.StaticCategory;
import se.vgregion.userfeedback.domain.StaticCategoryRepository;
import se.vgregion.userfeedback.domain.UserContact;
import se.vgregion.userfeedback.domain.UserFeedback;
import se.vgregion.userfeedback.domain.UserFeedbackRepository;

/**
 * @author <a href="mailto:david.rosell@redpill-linpro.com">David Rosell</a>
 */

@Controller
@RequestMapping(value = { "/KontaktaOss" })
@SessionAttributes("userFeedback")
public class TyckTillController {
    private static final Logger logger = LoggerFactory.getLogger(TyckTillController.class);

    @Autowired
    private FormTemplateRepository formTemplateRepository;

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private UserFeedbackRepository userFeedbackRepository;

    @Autowired
    private StaticCategoryRepository staticCategoryRepository;

    @Autowired
    private FeedbackReportService reportService;

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(CustomSubCategory.class, new CustomSubCategoryPropertyEditor());
    }

    /**
     * Used to render the userFeedback form. Basic initialization before render-view.
     * 
     * @param formName
     *            - FormTemplate name. The request parameter "formName" decide how the form is rendered. It is an
     *            inparameter to give the client this choise.
     * @param breadcrumb
     *            - client specific inparameter declaring from where the contact were initiated.
     * @param model
     *            - the normal Spring ModelMap.
     * @return - view to be rendered.
     */
    @RequestMapping(method = RequestMethod.GET)
    public String setupForm(@RequestParam(value = "formName", required = false) String formName, @RequestParam(
            value = "breadcrumb", required = false) String breadcrumb, ModelMap model) {

        FormTemplate template = lookupFormTemplate(formName);
        model.addAttribute("template", template);

        model.addAttribute("contentCategory", staticCategoryRepository
                .find(StaticCategoryRepository.STATIC_CONTENT_CATEGORY));
        model.addAttribute("functionCategory", staticCategoryRepository
                .find(StaticCategoryRepository.STATIC_FUNCTION_CATEGORY));
        model.addAttribute("otherCategory", staticCategoryRepository
                .find(StaticCategoryRepository.STATIC_OTHER_CATEGORY));

        UserFeedback userFeedback;
        if (!model.containsKey("userFeedback")) {
            userFeedback = new UserFeedback();
            userFeedback.setCaseCategory(null);
            model.addAttribute("userFeedback", userFeedback);
        } else {
            userFeedback = (UserFeedback) model.get("userFeedback");
        }
        userFeedback.setBreadcrumb(breadcrumb);

        model.addAttribute("contactOptions", UserContact.UserContactOption.getLabelMap());

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

    /**
     * Action method to send Userfeedback.
     * 
     * @param userFeedback
     *            - form backing bean.
     * @param formTemplateId
     *            - FormTemplate used to render form, used for extracting CustomCategory configuration.
     * @param multipartRequest
     *            - the http-request used for attachment upload and PlatformData extraction.
     * @param status
     *            - controls Session state.
     * @param model
     *            - the normal Spring ModelMap.
     * @return - view to render.
     */
    @Transactional
    @RequestMapping(method = RequestMethod.POST)
    public String sendUserFeedback(@ModelAttribute("userFeedback") UserFeedback userFeedback,
            @RequestParam("formTemplateId") Long formTemplateId, MultipartHttpServletRequest multipartRequest,
            SessionStatus status, ModelMap model) {
        logger.info("Sending...");

        ReportBuilder builder = new ReportBuilder();
        FeedbackReport report = builder.buildFeedbackReport(userFeedback, multipartRequest);
        reportService.reportFeedback(report);

        // logger.debug("User agent data captured: " + report);

        processUserfeedback(userFeedback, formTemplateId, multipartRequest);

        log(userFeedback, model);

        // Log UserFeedback in db
        if (userFeedback.getId() == null) {
            userFeedbackRepository.persist(userFeedback);
        } else {
            userFeedbackRepository.merge(userFeedback);
        }

        status.setComplete();

        return "Tacksida";
    }

    private void processUserfeedback(UserFeedback userFeedback, Long formTemplateId,
            MultipartHttpServletRequest multipartRequest) {
        // 1: Lookup Attachments
        for (Iterator<String> filenameIterator = multipartRequest.getFileNames(); filenameIterator.hasNext();) {
            String fileName = filenameIterator.next();

            processAttachment(multipartRequest.getFile(fileName), userFeedback);
        }

        FormTemplate template = formTemplateRepository.find(formTemplateId);
        // 2: Lookup CaseCategory
        String caseCategory = lookupCaseCategory(userFeedback, template);
        userFeedback.setCaseCategory(caseCategory);

        // 3: Lookup CaseSubCategory
        List<String> caseSubCategories = lookupCaseSubCategory(userFeedback, template);
        userFeedback.setCaseSubCategories(caseSubCategories);

        // 4: Lookup CaseContact
        String caseContact = lookupCaseContact(userFeedback, template);
        userFeedback.setCaseContact(caseContact);
    }

    private String lookupCaseCategory(UserFeedback userFeedback, FormTemplate template) {
        if (userFeedback.getCaseCategoryId() == null) {
            return "";
        }

        if (userFeedback.getCaseCategoryId() > 0) {
            return template.getCustomCategory().getName();
        }

        return staticCategoryRepository.find(userFeedback.getCaseCategoryId()).getName();
    }

    private List<String> lookupCaseSubCategory(UserFeedback userFeedback, FormTemplate template) {
        List<String> subCategories = new ArrayList<String>();
        if (userFeedback.getCaseSubCategoryIds() == null) {
            return subCategories;
        }

        if (userFeedback.getCaseCategoryId() > 0) {
            CustomCategory customCategory = template.getCustomCategory();
            for (Long subCategoryId : userFeedback.getCaseSubCategoryIds()) {
                for (CustomSubCategory subCategory : customCategory.getCustomSubCategories()) {
                    if (subCategoryId.equals(subCategory.getId())) {
                        subCategories.add(subCategory.getName());
                    }
                }
            }

            return subCategories;
        }

        StaticCategory category = staticCategoryRepository.find(userFeedback.getCaseCategoryId());
        for (Long subCategoryId : userFeedback.getCaseSubCategoryIds()) {
            for (Map.Entry<Long, String> subCategoryEntry : category.getSubCategories().entrySet()) {
                if (subCategoryId.equals(subCategoryEntry.getKey())) {
                    subCategories.add(subCategoryEntry.getValue());
                }
            }
        }

        return subCategories;
    }

    private String lookupCaseContact(UserFeedback userFeedback, FormTemplate template) {
        CustomCategory customCategory = template.getCustomCategory();

        logger.debug(customCategory.getName());

        // 0: Check if there exist a customCategory
        if (customCategory == null) {
            return "";
        }
        // 1: check if customCategory
        if (customCategory.getName().equals(userFeedback.getCaseCategory())) {
            List<String> caseSubCategoryList = userFeedback.getCaseSubCategories();
            // 2: check if single selection - else defaultContact
            if (caseSubCategoryList == null || caseSubCategoryList.size() != 1) {
                return customCategory.getDefaultContact();
            }

            String caseSubCategory = caseSubCategoryList.get(0);
            // 3: check if customSubCategory has contact
            for (CustomSubCategory subCategory : customCategory.getCustomSubCategories()) {
                if (subCategory.getName().equals(caseSubCategory)) {
                    if (!StringUtils.isBlank(subCategory.getContact())) {
                        return subCategory.getContact();
                    }
                }
            }
        }

        return customCategory.getDefaultContact();
    }

    private void processAttachment(MultipartFile file, UserFeedback userFeedback) {
        if (file.isEmpty()) {
            return;
        }

        Collection<Attachment> attachments = userFeedback.getAttachments();
        try {
            Attachment attachment = new Attachment();
            attachment.setFilename(file.getOriginalFilename());
            attachment.setFile(file.getBytes());

            logger.debug(file.getOriginalFilename());

            attachments.add(attachment);
        } catch (IOException e) {
            e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
        }
    }

    private void log(UserFeedback userFeedback, ModelMap model) {
        if (!logger.isDebugEnabled()) {
            return;
        }

        logger.debug("attachmentSize: " + userFeedback.getAttachments().size());

        for (Map.Entry<String, Object> entry : model.entrySet()) {
            logger.debug("Entry: " + entry.getKey());
        }
    }
}
