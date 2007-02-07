  /*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package info.jtrac.wicket;

import info.jtrac.domain.Attachment;
import info.jtrac.domain.Field;
import info.jtrac.domain.Item;
import info.jtrac.domain.ItemUser;
import info.jtrac.domain.Space;
import info.jtrac.domain.State;
import info.jtrac.domain.User;
import info.jtrac.domain.UserSpaceRole;
import info.jtrac.util.AttachmentUtils;
import info.jtrac.util.SecurityUtils;
import info.jtrac.util.UserUtils;
import java.io.File;
import java.util.List;
import wicket.Component;
import wicket.markup.html.form.CheckBox;
import wicket.markup.html.form.DropDownChoice;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.IChoiceRenderer;
import wicket.markup.html.form.ListMultipleChoice;
import wicket.markup.html.form.TextArea;
import wicket.markup.html.form.TextField;
import wicket.markup.html.form.upload.FileUpload;
import wicket.markup.html.form.upload.FileUploadField;
import wicket.markup.html.panel.FeedbackPanel;
import wicket.model.AbstractReadOnlyModel;
import wicket.model.BoundCompoundPropertyModel;

/**
 * Create / Edit item form page
 */
public class ItemFormPage extends BasePage {        
    
    private JtracFeedbackMessageFilter filter;    
    
    public ItemFormPage(Space space) {
        super("Edit Item");
        add(new HeaderPanel(space));
        Item item = new Item();
        item.setSpace(space);        
        FeedbackPanel feedback = new FeedbackPanel("feedback");
        filter = new JtracFeedbackMessageFilter();
        feedback.setFilter(filter);
        border.add(feedback);        
        border.add(new ItemForm("form", item));
    }
    
    private class ItemForm extends Form {
        
        private FileUploadField fileUploadField;
        
        public ItemForm(String id, Item item) {
            super(id);
            setMultiPart(true);
            final BoundCompoundPropertyModel model = new BoundCompoundPropertyModel(item);
            setModel(model);
            // summary =========================================================
            final Component summaryField = new TextField("summary").setRequired(true).add(new ErrorHighlighter()).setOutputMarkupId(true);
            add(summaryField);
            ItemFormPage.this.getBodyContainer().addOnLoadModifier(new AbstractReadOnlyModel() {
                public Object getObject(Component component) {
                    return "document.getElementById('" + summaryField.getMarkupId() + "').focus()";
                }
            }, summaryField);
            // detail ==========================================================
            add(new TextArea("detail").setRequired(true).add(new ErrorHighlighter()));
            // custom fields ===================================================
            List<Field> fields = item.getSpace().getMetadata().getFieldList();            
            add(new CustomFieldsFormPanel("fields", model, fields));
            // assigned to =====================================================
            Space space = item.getSpace();
            List<UserSpaceRole> userSpaceRoles = getJtrac().findUserRolesForSpace(space.getId());
            List<User> assignable = UserUtils.filterUsersAbleToTransitionFrom(userSpaceRoles, space, State.OPEN);
            DropDownChoice choice = new DropDownChoice("assignedTo", assignable, new IChoiceRenderer() {
                public Object getDisplayValue(Object o) {
                    return ((User) o).getName();
                }
                public String getIdValue(Object o, int i) {
                    return ((User) o).getId() + "";
                }                
            });
            choice.setNullValid(true);
            choice.setRequired(true);
            choice.add(new ErrorHighlighter());            
            add(choice);
            // notify list =====================================================
            List<ItemUser> choices = UserUtils.convertToItemUserList(userSpaceRoles);
            ListMultipleChoice itemUsers = new JtracCheckBoxMultipleChoice("itemUsers", choices, new IChoiceRenderer() {
                public Object getDisplayValue(Object o) {
                    return ((ItemUser) o).getUser().getName();
                }
                public String getIdValue(Object o, int i) {
                    return ((ItemUser) o).getUser().getId() + "";
                }               
            });
            add(itemUsers);
            // attachment ======================================================
            fileUploadField = new FileUploadField("file");
            // TODO file size limit
            add(fileUploadField);    
            // send notifications===============================================
            add(new CheckBox("sendNotifications"));
        }
        
        @Override
        protected void validate() {
            filter.reset();
            super.validate();
        }
        
        @Override
        protected void onSubmit() {
            final FileUpload fileUpload = fileUploadField.getFileUpload();
            Attachment attachment = null;
            if (fileUpload != null) {
                String fileName = AttachmentUtils.cleanFileName(fileUpload.getClientFileName());
                attachment = new Attachment();
                attachment.setFileName(fileName);
            }
            Item item = (Item) getModelObject();
            User user = SecurityUtils.getPrincipal();
            item.setLoggedBy(user);
            item.setStatus(State.OPEN);
            getJtrac().storeItem(item, attachment);
            if (attachment != null) {
                File file = AttachmentUtils.getNewFile(attachment);
                try {
                    fileUpload.writeTo(file);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }            
            setResponsePage(new ItemViewPage(item, null));
        }
        
    }
    
}