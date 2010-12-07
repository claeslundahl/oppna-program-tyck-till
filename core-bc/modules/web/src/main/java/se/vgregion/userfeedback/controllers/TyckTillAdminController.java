package se.vgregion.userfeedback.controllers;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import se.vgregion.userfeedback.domain.*;

import javax.validation.Valid;
import java.util.*;

/**
 * @author <a href="mailto:david.rosell@redpill-linpro.com">David Rosell</a>
 */

@Controller
@SessionAttributes({ "formTemplate", "customCategory" })
public class TyckTillAdminController {

    @Autowired
    private FormTemplateRepository formTemplateRepository;

    @Autowired
    private StaticCategoryRepository staticCategoryRepository;

    @Value("${deploy.path}")
    private String deployPath;

    @RequestMapping(method = RequestMethod.GET, value = "/TemplateList")
    public String listView(ModelMap model) {

        Collection<FormTemplate> templates = formTemplateRepository.findAll();
        model.addAttribute("templateList", templates);

        model.addAttribute("deployPath", deployPath);
        return "TemplateList";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/TemplateEdit")
    public String viewTemplates(@RequestParam(value = "templateId", required = false) Long templateId,
            ModelMap model) {
        FormTemplate template;
        if (templateId == null) {
            template = (FormTemplate) model.get("formTemplate");

            if (template == null) {
                template = new FormTemplate();
            }
        } else {
            template = formTemplateRepository.find(templateId);
        }
        model.addAttribute("formTemplate", template);

        // expose CustomSubCategories
        CustomCategory customCategory = template.getCustomCategory();
        if (customCategory == null) {
            customCategory = new CustomCategory();
            template.setCustomCategory(customCategory);
        }
        model.addAttribute("customCategory", customCategory);

        List<CustomSubCategory> subCategories = customCategory.getCustomSubCategories();
        if (subCategories == null) {
            subCategories = new ArrayList<CustomSubCategory>();
            customCategory.setCustomSubCategories(subCategories);
        }
        model.addAttribute("customSubCategories", subCategories);

        // expose StaticCategories
        StaticCategory contentCategory = staticCategoryRepository
                .find(StaticCategoryRepository.STATIC_CONTENT_CATEGORY);
        StaticCategory functionCategory = staticCategoryRepository
                .find(StaticCategoryRepository.STATIC_FUNCTION_CATEGORY);
        StaticCategory otherCategory = staticCategoryRepository
                .find(StaticCategoryRepository.STATIC_OTHER_CATEGORY);
        model.addAttribute("contentCategory", contentCategory);
        model.addAttribute("functionCategory", functionCategory);
        model.addAttribute("otherCategory", otherCategory);

        model.addAttribute("deployPath", deployPath);
        return "TemplateEdit";
    }

    @Transactional
    @RequestMapping(method = RequestMethod.POST)
    public String addTemplate(@Valid @ModelAttribute("formTemplate") FormTemplate formTemplate,
            BindingResult result, SessionStatus status, ModelMap model) {
        model.addAttribute("deployPath", deployPath);
        if (result.hasErrors()) {
            return "TemplateEdit";
        }

        CustomCategory customCategory = formTemplate.getCustomCategory();
        if (customCategory != null) {
            for (Iterator<CustomSubCategory> it = customCategory.getCustomSubCategories().iterator(); it.hasNext();) {
                CustomSubCategory subCategory = it.next();
                if (StringUtils.isBlank(subCategory.getName())) {
                    it.remove();
                }
            }
        }

        formTemplate.setLastChanged(new Date());
        if (formTemplate.getId() == null) {
            formTemplateRepository.persist(formTemplate);
        } else {
            formTemplateRepository.merge(formTemplate);
        }

        status.setComplete();

        return "redirect:TemplateList";
    }

    /**
     * Edit CustomCategory without persis. FormTemplate has to be handled to allow preserving data not stored yet.
     * 
     * @param formTemplate
     *            - main backing bean.
     * @return - view with edit form.
     */
    @RequestMapping(method = RequestMethod.GET, value = "/CustomCategoryEdit")
    public String editCustomCategory(@ModelAttribute("formTemplate") FormTemplate formTemplate,
                                     ModelMap model) {
        model.addAttribute("deployPath", deployPath);

        List<CustomSubCategory> subCategories = formTemplate.getCustomCategory().getCustomSubCategories();
        int emptyCnt = 0;
        for (CustomSubCategory subCategory : subCategories) {
            if (StringUtils.isBlank(subCategory.getName())) emptyCnt++;
        }

        for (int i = emptyCnt; i < 3; i++) {
            CustomSubCategory customSubCategory = new CustomSubCategory();
            customSubCategory.setBackend(new Backend());

            subCategories.add(customSubCategory);
        }

        return "CustomCategoryEdit";
    }

    /**
     * Redirect back to TemplateEdit to continue working with the formTemplate.
     * 
     * @param formTemplate
     *            - the main backing bean.
     * @return - view for FormTemplate edit.
     */
    @RequestMapping(method = RequestMethod.POST, value = "/CustomCategoryUpdate")
    public String updateCustomCategory(@ModelAttribute("formTemplate") FormTemplate formTemplate,
                                       ModelMap model) {
        model.addAttribute("deployPath", deployPath);
        for (Iterator<CustomSubCategory> it = formTemplate.getCustomCategory().getCustomSubCategories().iterator(); it
                .hasNext();) {
            CustomSubCategory subCategory = it.next();
            if (StringUtils.isBlank(subCategory.getName())) {
                it.remove();
            }
        }

        return "redirect:TemplateEdit";
    }

}