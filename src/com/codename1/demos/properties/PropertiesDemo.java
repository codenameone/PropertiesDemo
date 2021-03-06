package com.codename1.demos.properties;


import com.codename1.components.FloatingActionButton;
import com.codename1.components.MultiButton;
import com.codename1.components.ToastBar;
import com.codename1.db.Database;
import com.codename1.ui.Display;
import com.codename1.ui.Form;
import com.codename1.ui.Dialog;
import com.codename1.ui.Label;
import com.codename1.ui.plaf.UIManager;
import com.codename1.ui.util.Resources;
import com.codename1.io.Log;
import com.codename1.properties.InstantUI;
import com.codename1.properties.PropertyBusinessObject;
import com.codename1.properties.SQLMap;
import com.codename1.properties.UiBinding;
import com.codename1.ui.Button;
import com.codename1.ui.ButtonGroup;
import com.codename1.ui.Container;
import com.codename1.ui.FontImage;
import com.codename1.ui.RadioButton;
import com.codename1.ui.SwipeableContainer;
import com.codename1.ui.TextField;
import com.codename1.ui.Toolbar;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.GridLayout;
import com.codename1.ui.spinner.Picker;
import com.codename1.ui.table.TableLayout;
import java.io.IOException;
import java.util.List;

/**
 * This file was generated by <a href="https://www.codenameone.com/">Codename One</a> for the purpose 
 * of building native mobile applications using Java.
 */
public class PropertiesDemo {

    private Form current;
    private Resources theme;
    private Database db;
    private SQLMap sm;

    public void init(Object context) {
        theme = UIManager.initFirstTheme("/theme");

        // Enable Toolbar on all Forms by default
        Toolbar.setGlobalToolbar(true);

        // Pro only feature, uncomment if you have a pro subscription
        Log.bindCrashProtection(true);

        try {
            Contact c = new Contact();
            db = Display.getInstance().openOrCreate("propertiesdemo.db");
            sm = SQLMap.create(db);
            sm.setPrimaryKeyAutoIncrement(c, c.id);
            sm.createTable(c);
        } catch(IOException err) {
            Log.e(err);
        }
    }
    
    public void start() {
        if(current != null){
            current.show();
            return;
        }
        
        showContactsList(true);
    }
    
    private void showContactsList(boolean forward) {
        try {
            Contact c = new Contact();
            List<PropertyBusinessObject> contacts = sm.select(c, c.name, true, 1000, 0);

            Form contactsForm = new Form("Contacts", BoxLayout.y());
            for(PropertyBusinessObject currentContact : contacts) {
                String name = ((Contact)currentContact).name.get();
                if(name == null || name.length() == 0) {
                    name = "[null]";
                }
                MultiButton b = new MultiButton(name);
                b.setTextLine2(c.phone.get());
                b.addActionListener(e -> editContact((Contact)currentContact));
                Button delete = new Button("");
                FontImage.setMaterialIcon(delete, FontImage.MATERIAL_DELETE, 4);
                SwipeableContainer sc = new SwipeableContainer(delete, b);

                delete.addActionListener(e -> {
                    try {
                        sm.delete(currentContact);
                        sc.setX(contactsForm.getWidth());
                        contactsForm.getContentPane().animateUnlayoutAndWait(200, 255);
                        sc.remove();
                        contactsForm.getContentPane().animateLayout(180);
                    } catch(IOException err) {
                        Log.e(err);
                        ToastBar.showErrorMessage("Error while deleting: "+ err);
                    }
                });
                
                contactsForm.add(sc);
            }
            
            FloatingActionButton add = FloatingActionButton.createFAB(FontImage.MATERIAL_ADD);
            add.bindFabToContainer(contactsForm.getContentPane());
            add.addActionListener(e -> {
                editContact(new Contact());
            });
            
            if(forward) {
                contactsForm.show();
            } else {
                contactsForm.showBack();
            }
        } catch(Exception err) {
            Log.e(err);
        }
    }

    public void editContact(Contact c) {
        Form contactEditor = new Form(c.name.get(), BoxLayout.y());
        contactEditor.add(createMagicalContactEditorContainer(c));
        contactEditor.getToolbar().setBackCommand("", e -> {
            showContactsList(false);
        });
        contactEditor.getToolbar().addMaterialCommandToRightBar("", FontImage.MATERIAL_CHECK, e -> {
            try {
                if(c.id.get() == null) {
                    sm.insert(c);
                } else {
                    sm.update(c);
                }
            } catch(IOException err) {
                Log.e(err);
            }
            showContactsList(false);
        });
        contactEditor.show();
    }

    public Container createMagicalContactEditorContainer(Contact c) {
        InstantUI iui = new InstantUI();
        iui.excludeProperty(c.id);
        iui.setMultiChoiceLabels(c.gender, "Male", "Female", "Undefined");
        iui.setMultiChoiceValues(c.gender, "M", "F", "U");
        return iui.createEditUI(c, true);
    }
    
    public Container createManualContactEditorContainer(Contact c) {
        Container resp;
        if(Display.getInstance().isTablet()) {
            TableLayout tl = new TableLayout(6, 2);
            tl.setGrowHorizontally(true);
            resp = new Container(tl);
        } else {
            resp = new Container(BoxLayout.y());
        }
        UiBinding uib = new UiBinding();
        
        TextField nameTf = new TextField();
        uib.bind(c.name, nameTf);
        resp.add(c.name.getLabel()).
                add(nameTf);
        
        TextField emailTf = new TextField();
        emailTf.setConstraint(TextField.EMAILADDR);
        uib.bind(c.email, emailTf);
        resp.add(c.email.getLabel()).
                add(emailTf);

        TextField phoneTf = new TextField();
        phoneTf.setConstraint(TextField.PHONENUMBER);
        uib.bind(c.phone, phoneTf);
        resp.add(c.phone.getLabel()).
                add(phoneTf);
        
        Picker dateOfBirth = new Picker();
        dateOfBirth.setType(Display.PICKER_TYPE_DATE);
        uib.bind(c.dateOfBirth, dateOfBirth);
        resp.add(c.dateOfBirth.getLabel()).
                add(dateOfBirth);
        
        ButtonGroup genderGroup = new ButtonGroup();
        RadioButton male = RadioButton.createToggle("Male", genderGroup);
        RadioButton female = RadioButton.createToggle("Female", genderGroup);
        RadioButton undefined = RadioButton.createToggle("Undefined", genderGroup);
        uib.bindGroup(c.gender, new String[] {"M", "F", "U"}, male, female, undefined);
        resp.add(c.gender.getLabel()).
                add(GridLayout.encloseIn(3, male, female, undefined));

        TextField rankTf = new TextField();
        rankTf.setConstraint(TextField.NUMERIC);
        uib.bind(c.rank, rankTf);
        resp.add(c.rank.getLabel()).
                add(rankTf);
        
        return resp;
    }
    
    

    public void stop() {
        current = Display.getInstance().getCurrent();
        if(current instanceof Dialog) {
            ((Dialog)current).dispose();
            current = Display.getInstance().getCurrent();
        }
    }
    
    public void destroy() {
    }

}
