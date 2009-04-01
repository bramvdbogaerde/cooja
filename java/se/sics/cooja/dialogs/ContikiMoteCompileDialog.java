/*
 * Copyright (c) 2009, Swedish Institute of Computer Science.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the Institute nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE INSTITUTE AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE INSTITUTE OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * $Id: ContikiMoteCompileDialog.java,v 1.3 2009/04/01 14:00:30 fros4943 Exp $
 */

package se.sics.cooja.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import se.sics.cooja.CoreComm;
import se.sics.cooja.GUI;
import se.sics.cooja.MoteInterface;
import se.sics.cooja.MoteType;
import se.sics.cooja.ProjectConfig;
import se.sics.cooja.Simulation;
import se.sics.cooja.contikimote.ContikiMoteType;
import se.sics.cooja.contikimote.ContikiMoteType.CommunicationStack;

/**
 * Contiki Mote Type compile dialog.
 *
 * @author Fredrik Osterlind
 */
public class ContikiMoteCompileDialog extends AbstractCompileDialog {
  private static Logger logger = Logger.getLogger(ContikiMoteCompileDialog.class);

  private JComboBox commStackComboBox = new JComboBox(CommunicationStack.values());

  public static boolean showDialog(
      Container parent,
      Simulation simulation,
      MoteType moteType) {

    final ContikiMoteCompileDialog dialog = new ContikiMoteCompileDialog(parent, simulation, moteType);

    /* Show dialog and wait for user */
    dialog.setVisible(true); /* BLOCKS */
    if (!dialog.createdOK()) {
      return false;
    }

    /* Assume that if a firmware exists, compilation was ok */
    return true;
  }

  private ContikiMoteCompileDialog(Container parent, Simulation simulation, MoteType moteType) {
    super(parent, simulation, moteType);

    if (contikiSource != null) {
      /* Make sure compilation variables are updated */
      getDefaultCompileCommands(contikiSource);
    }

    /* Add Contiki mote type specifics */
    addMoteInterfaceClasses();
    addAdvancedTab(tabbedPane);
  }

  public boolean canLoadFirmware(File file) {
    /* Disallow loading firmwares without compilation */
    /*
    if (file.getName().endsWith(ContikiMoteType.librarySuffix)) {
      return true;
    }
    */

    return false;
  }

  public String getDefaultCompileCommands(File source) {
    if (moteType == null) {
      /* Not ready to compile yet */
      return "";
    }

    if (source == null || !source.exists()) {
      /* Not ready to compile yet */
      return "";
    }

    if (moteType.getIdentifier() == null) {
      /* Generate mote type identifier */
      moteType.setIdentifier(
          ContikiMoteType.generateUniqueMoteTypeID(simulation.getMoteTypes(), null));
    }

    /* Create variables used for compiling Contiki */
    ((ContikiMoteType)moteType).setContikiSourceFile(source);
    ((ContikiMoteType)moteType).libSource = new File(
        source.getParentFile(),
        "obj_cooja/" + moteType.getIdentifier() + ".c"
    );
    ((ContikiMoteType)moteType).libFile = new File(
        source.getParentFile(),
        "obj_cooja/" + moteType.getIdentifier() + ContikiMoteType.librarySuffix
    );
    ((ContikiMoteType)moteType).archiveFile = new File(
        source.getParentFile(),
        "obj_cooja/" + moteType.getIdentifier() + ContikiMoteType.dependSuffix
    );
    ((ContikiMoteType)moteType).mapFile = new File(
        source.getParentFile(),
        "obj_cooja/" + moteType.getIdentifier() + ContikiMoteType.mapSuffix);
    ((ContikiMoteType)moteType).javaClassName = CoreComm.getAvailableClassName();

    if (((ContikiMoteType)moteType).javaClassName == null) {
      logger.fatal("Could not allocate a core communicator!");
      return "";
    }

    /* Prepare compiler environment */
    String[][] env = null;
    try {
      env = CompileContiki.createCompilationEnvironment(
          moteType.getIdentifier(),
          source,
          ((ContikiMoteType)moteType).mapFile,
          ((ContikiMoteType)moteType).libFile,
          ((ContikiMoteType)moteType).archiveFile,
          ((ContikiMoteType) moteType).getCommunicationStack()
      );
      String[] envOneDimension = new String[env.length];
      for (int i=0; i < env.length; i++) {
        envOneDimension[i] = env[i][0] + "=" + env[i][1];
      }
      createEnvironmentTab(tabbedPane, env);

      /* Prepare compiler with environment variables */
      this.compilationEnvironment = envOneDimension;
    } catch (Exception e) {
      logger.warn("Error when creating environment: " + e.getMessage());
      e.printStackTrace();
      env = null;
    }

    String defines = "";
    if (((ContikiMoteType) moteType).getCommunicationStack() == CommunicationStack.UIP) {
      defines = " DEFINES=WITH_UIP=1";
    }
    if (((ContikiMoteType) moteType).getCommunicationStack() == CommunicationStack.UIPV6) {
      defines = " DEFINES=UIP_CONF_IPV6=1";
    }
    return
    /*"make clean TARGET=cooja\n" + */
    "make " + getExpectedFirmwareFile(source).getName() + " TARGET=cooja" + defines;
  }

  public File getExpectedFirmwareFile(File source) {
    return ContikiMoteType.getExpectedFirmwareFile(source);
  }

  private void addMoteInterfaceClasses() {
    ProjectConfig projectConfig = moteType.getConfig();
    String[] intfNames = projectConfig.getStringArrayValue(ContikiMoteType.class, "MOTE_INTERFACES");

    boolean selected = true;
    if (moteIntfBox.getComponentCount() > 0) {
      selected = false;
    }

    /* Load mote interface classes */
    for (String intfName : intfNames) {
      Class<? extends MoteInterface> intfClass =
        gui.tryLoadClass(this, MoteInterface.class, intfName);

      if (intfClass == null) {
        logger.warn("Failed to load mote interface class: " + intfName);
        continue;
      }

      addMoteInterface(intfClass, selected);
    }
  }

  private void addAdvancedTab(JTabbedPane parent) {

    /* TODO System symbols */
    /*JCheckBox symbolsCheckBox = new JCheckBox("With system symbols", false);
    symbolsCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
    symbolsCheckBox.setEnabled(false);
    symbolsCheckBox.setToolTipText("Not implemented");*/

    /* Communication stack */
    JLabel label = new JLabel("Communication stack");
    label.setPreferredSize(LABEL_DIMENSION);
    commStackComboBox.setSelectedItem(((ContikiMoteType)moteType).getCommunicationStack());
    commStackComboBox.setEnabled(true);
    commStackComboBox.setPreferredSize(LABEL_DIMENSION);
    commStackComboBox.setMaximumSize(LABEL_DIMENSION);
    commStackComboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ((ContikiMoteType)moteType).setCommunicationStack(
            (CommunicationStack)commStackComboBox.getSelectedItem());
        setDialogState(DialogState.SELECTED_SOURCE);
      }
    });
    Box commStackBox = Box.createHorizontalBox();
    commStackBox.setAlignmentX(Component.LEFT_ALIGNMENT);
    commStackBox.add(label);
    commStackBox.add(Box.createHorizontalGlue());
    commStackBox.add(commStackComboBox);

    /* Advanced tab */
    Box box = Box.createVerticalBox();
    /*box.add(symbolsCheckBox);*/
    box.add(commStackBox);
    box.add(Box.createVerticalGlue());
    parent.addTab("Advanced", null, new JScrollPane(box), "Advanced Contiki Mote Type settings");
  }

  private void createEnvironmentTab(JTabbedPane parent, Object[][] env) {
    /* Remove any existing environment tabs */
    for (int i=0; i < tabbedPane.getTabCount(); i++) {
      if (tabbedPane.getTitleAt(i).equals("Environment")) {
        tabbedPane.removeTabAt(i--);
      }
    }

    /* Create new tab, fill with current environment data */
    String[] columnNames = { "Variable", "Value" };
    JTable table = new JTable(env, columnNames) {
      public boolean isCellEditable(int row, int column) {
        return false;
      }
    };

    JPanel panel = new JPanel(new BorderLayout());
    JButton button = new JButton("Change environment variables: Open external tools dialog");
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        /* Show external tools dialog */
        ExternalToolsDialog.showDialog(GUI.getTopParentContainer());

        /* Update and select environment tab */
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            getDefaultCompileCommands(((ContikiMoteType)moteType).getContikiSourceFile());
            for (int i=0; i < tabbedPane.getTabCount(); i++) {
              if (tabbedPane.getTitleAt(i).equals("Environment")) {
                tabbedPane.setSelectedIndex(i);
                break;
              }
            }
            setDialogState(DialogState.AWAITING_COMPILATION);
          }
        });

      }
    });
    panel.add(BorderLayout.NORTH, button);
    panel.add(BorderLayout.CENTER, new JScrollPane(table));

    parent.addTab("Environment", null, panel, "Environment variables");
  }

  public void writeSettingsToMoteType() {
    /* XXX Do not load the generated firmware.
     * Instead, load the copy in obj_cooja/ */
    File contikiFirmware = new File(
        moteType.getContikiSourceFile().getParentFile(),
        "obj_cooja/" + moteType.getIdentifier() + ContikiMoteType.librarySuffix
    );
    moteType.setContikiFirmwareFile(contikiFirmware);

    /* TODO System symbols */
    ((ContikiMoteType)moteType).setHasSystemSymbols(false);
  }

  public void compileContiki()
  throws Exception {
    if (((ContikiMoteType)moteType).libSource == null ||
        ((ContikiMoteType)moteType).libFile == null ||
        ((ContikiMoteType)moteType).archiveFile == null ||
        ((ContikiMoteType)moteType).mapFile == null ||
        ((ContikiMoteType)moteType).javaClassName == null) {
      throw new Exception("Library variables not defined");
    }

    /* Delete output files before compiling */
    ((ContikiMoteType)moteType).libSource.delete();
    ((ContikiMoteType)moteType).libFile.delete();
    ((ContikiMoteType)moteType).archiveFile.delete();
    ((ContikiMoteType)moteType).mapFile.delete();

    /* Extract Contiki dependencies from currently selected mote interfaces */
    String[] coreInterfaces =
      ContikiMoteType.getRequiredCoreInterfaces(getSelectedMoteInterfaceClasses());
    ((ContikiMoteType)moteType).setCoreInterfaces(coreInterfaces);

    /* Generate Contiki main source */
    try {
      CompileContiki.generateSourceFile(
          ((ContikiMoteType)moteType).libSource,
          ((ContikiMoteType)moteType).javaClassName,
          ((ContikiMoteType)moteType).getSensors(),
          ((ContikiMoteType)moteType).getCoreInterfaces()
      );
    } catch (Exception e) {
      throw (Exception) new Exception("Error when generating Contiki main source").initCause(e);
    }

    /* Start compiling */
    super.compileContiki();
  }
}
