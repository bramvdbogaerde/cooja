<?xml version="1.0" encoding="UTF-8"?>
<simconf>
  <simulation>
    <title>My simulation</title>
    <delaytime>0</delaytime>
    <ticktime>1</ticktime>
    <randomseed>123456</randomseed>
    <motedelay>1000</motedelay>
    <radiomedium>
      se.sics.cooja.radiomediums.UDGM
      <transmitting_range>50.0</transmitting_range>
      <interference_range>100.0</interference_range>
      <success_ratio_tx>1.0</success_ratio_tx>
      <success_ratio_rx>1.0</success_ratio_rx>
    </radiomedium>
    <motetype>
      se.sics.cooja.mspmote.SkyMoteType
      <identifier>sky1</identifier>
      <description>Sky Mote Type #1</description>
      <source>../../../examples/sky-shell/sky-shell.c</source>
      <command>make sky-shell.sky TARGET=sky</command>
    </motetype>
    <mote>
      se.sics.cooja.mspmote.SkyMote
      <motetype_identifier>sky1</motetype_identifier>
      <interface_config>
        se.sics.cooja.interfaces.Position
        <x>62.86427076032819</x>
        <y>38.98246035522194</y>
        <z>0.0</z>
      </interface_config>
      <interface_config>
        se.sics.cooja.mspmote.interfaces.MspMoteID
        <id>1</id>
      </interface_config>
    </mote>
  </simulation>
  <plugin>
    se.sics.cooja.plugins.SimControl
    <width>302</width>
    <z>1</z>
    <height>187</height>
    <location_x>0</location_x>
    <location_y>0</location_y>
    <minimized>false</minimized>
  </plugin>
  <plugin>
    se.sics.cooja.plugins.VisState
    <width>300</width>
    <z>3</z>
    <height>300</height>
    <location_x>390</location_x>
    <location_y>0</location_y>
    <minimized>false</minimized>
  </plugin>
  <plugin>
    se.sics.cooja.plugins.LogListener
    <plugin_config>
      <filter />
      <history>256</history>
    </plugin_config>
    <width>690</width>
    <z>2</z>
    <height>190</height>
    <location_x>0</location_x>
    <location_y>457</location_y>
    <minimized>false</minimized>
  </plugin>
</simconf>

