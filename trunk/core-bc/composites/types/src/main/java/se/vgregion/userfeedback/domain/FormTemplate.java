package se.vgregion.userfeedback.domain;

import org.hibernate.validator.constraints.NotEmpty;
import se.vgregion.dao.domain.patterns.entity.AbstractEntity;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

/**
 * This action do that and that, if it has something special it is.
 *
 * @author <a href="mailto:david.rosell@redpill-linpro.com">David Rosell</a>
 */
@Entity
@Table
public class FormTemplate extends AbstractEntity<Long> implements Serializable {
    private static final long serialVersionUID = 7819565362034276611L;

    @Id
    @GeneratedValue
    private Long id;

    @Version
    private Integer version;

    @NotEmpty(message = "{template.name}")
    @Column(unique = true, nullable = false)
    private String name;

    @Size(min = 5, max = 50, message = "{template.title}")
    private String title;

    @Column(length = 2048)
    @Size(max = 2048, message = "{template.description}")
    private String description;

    private String lastChangedBy;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastChanged = new Date();


    // optional parts
    private Boolean showCustom;

    private Boolean showContent = Boolean.TRUE;

    private Boolean showFunction = Boolean.TRUE;

    private Boolean showOther = Boolean.TRUE;

    private Boolean showContact = Boolean.TRUE;

    private Boolean showContactByEmail = Boolean.TRUE;

    private Boolean showContactByPhone = Boolean.TRUE;

    private Boolean showAttachment = Boolean.TRUE;

    @OneToOne(cascade = CascadeType.ALL)
    private CustomCategory customCategory = new CustomCategory();

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name="usd", column=@Column(name = "default_usd")),
            @AttributeOverride(name="pivotal", column=@Column(name = "default_pivotal")),
            @AttributeOverride(name="mbox", column=@Column(name = "default_mbox"))

    })
    private Backend defaultBackend;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name="usd", column=@Column(name = "content_usd")),
            @AttributeOverride(name="pivotal", column=@Column(name = "content_pivotal")),
            @AttributeOverride(name="mbox", column=@Column(name = "content_mbox"))

    })
    private Backend contentBackend;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name="usd", column=@Column(name = "function_usd")),
            @AttributeOverride(name="pivotal", column=@Column(name = "function_pivotal")),
            @AttributeOverride(name="mbox", column=@Column(name = "function_mbox"))

    })
    private Backend functionBackend;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name="usd", column=@Column(name = "other_usd")),
            @AttributeOverride(name="pivotal", column=@Column(name = "other_pivotal")),
            @AttributeOverride(name="mbox", column=@Column(name = "other_mbox"))

    })
    private Backend otherBackend;

    @Override
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getShowCustom() {
        return showCustom;
    }

    public void setShowCustom(Boolean showCustom) {
        this.showCustom = showCustom;
    }

    public Boolean getShowContent() {
        return showContent;
    }

    public void setShowContent(Boolean showContent) {
        this.showContent = showContent;
    }

    public Boolean getShowFunction() {
        return showFunction;
    }

    public void setShowFunction(Boolean showFunction) {
        this.showFunction = showFunction;
    }

    public Boolean getShowOther() {
        return showOther;
    }

    public void setShowOther(Boolean showOther) {
        this.showOther = showOther;
    }

    public Boolean getShowContact() {
        return showContact;
    }

    public void setShowContact(Boolean showContact) {
        this.showContact = showContact;
    }

    public Boolean getShowContactByEmail() {
        return showContactByEmail;
    }

    public void setShowContactByEmail(Boolean showContactByEmail) {
        this.showContactByEmail = showContactByEmail;
    }

    public Boolean getShowContactByPhone() {
        return showContactByPhone;
    }

    public void setShowContactByPhone(Boolean showContactByPhone) {
        this.showContactByPhone = showContactByPhone;
    }

    public Boolean getShowAttachment() {
        return showAttachment;
    }

    public void setShowAttachment(Boolean showAttachment) {
        this.showAttachment = showAttachment;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getLastChangedBy() {
        return lastChangedBy;
    }

    public void setLastChangedBy(String lastChangedBy) {
        this.lastChangedBy = lastChangedBy;
    }

    public Date getLastChanged() {
        return lastChanged;
    }

    public void setLastChanged(Date lastChanged) {
        this.lastChanged = lastChanged;
    }

    public CustomCategory getCustomCategory() {
        return customCategory;
    }

    public void setCustomCategory(CustomCategory customCategory) {
        this.customCategory = customCategory;
    }

    public Backend getDefaultBackend() {
        return defaultBackend;
    }

    public void setDefaultBackend(Backend defaultBackend) {
        this.defaultBackend = defaultBackend;
    }

    public Backend getContentBackend() {
        return contentBackend;
    }

    public void setContentBackend(Backend contentBackend) {
        this.contentBackend = contentBackend;
    }

    public Backend getFunctionBackend() {
        return functionBackend;
    }

    public void setFunctionBackend(Backend functionBackend) {
        this.functionBackend = functionBackend;
    }

    public Backend getOtherBackend() {
        return otherBackend;
    }

    public void setOtherBackend(Backend otherBackend) {
        this.otherBackend = otherBackend;
    }
}