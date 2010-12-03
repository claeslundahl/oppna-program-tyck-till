package se.vgregion.userfeedback.controllers;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
@SessionAttributes("formTemplate")
public class TyckTillAdminController {

    @Autowired
    private FormTemplateRepository formTemplateRepository;

    @Autowired
    private StaticCategoryRepository staticCategoryRepository;


    @RequestMapping(method = RequestMethod.GET, value = "/KontaktaOss/TemplateList")
    public String listView(ModelMap model) {

        Collection<FormTemplate> templates = formTemplateRepository.findAll();
        model.addAttribute("templateList", templates);

        return "KontaktaOssTemplateList";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/KontaktaOss/TemplateEditor")
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

        List<CustomSubCategory> subCategories = customCategory.getCustomSubCategories();
        if (subCategories == null) {
            subCategories = new ArrayList<CustomSubCategory>();
            customCategory.setCustomSubCategories(subCategories);
        }
        model.addAttribute("customSubCategories", subCategories);

        // expose StaticCategories
        StaticCategory contentCategory = staticCategoryRepository.find(StaticCategoryRepository.STATIC_CONTENT_CATEGORY);
        StaticCategory functionCategory = staticCategoryRepository.find(StaticCategoryRepository.STATIC_FUNCTION_CATEGORY);
        StaticCategory otherCategory = staticCategoryRepository.find(StaticCategoryRepository.STATIC_OTHER_CATEGORY);
        model.addAttribute("contentCategory", contentCategory);
        model.addAttribute("functionCategory", functionCategory);
        model.addAttribute("otherCategory", otherCategory);


        return "KontaktaOssTemplateEdit";
    }


    @Transactional
    @RequestMapping(method = RequestMethod.POST)
    public String addTemplate(@Valid @ModelAttribute("formTemplate") FormTemplate formTemplate,
                              BindingResult result,
                              SessionStatus status,
                              ModelMap model) {
        if (result.hasErrors()) {
            return "KontaktaOssTemplateEdit";
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
}