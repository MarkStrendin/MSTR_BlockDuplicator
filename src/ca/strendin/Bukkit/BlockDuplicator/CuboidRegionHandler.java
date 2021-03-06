package ca.strendin.Bukkit.BlockDuplicator;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CuboidRegionHandler {   
    
    // Temporary workspace for inputting coordinates into a new region
    private static Hashtable<Player,Block> playerWorkspace = new Hashtable<Player,Block>();
    private static Hashtable<Player,CuboidPreRegion> preRegions = new Hashtable<Player,CuboidPreRegion>();    
    private static ArrayList<CuboidRegion> regions = new ArrayList<CuboidRegion>(); 
     
    
    public static CuboidRegion getRegionByName(String name) {        
        for (CuboidRegion thisRegion : regions) {
            if (thisRegion.getName().toLowerCase().equals(name.toLowerCase())) {
                return thisRegion;
            }
        }
        return null;
    }
    
    // Deserializes regions
    public static void initRegions(BlockDuplicator plugin) {        
        BDLogging.logThis("Initializing regions");
        // Clear current region lists
        playerWorkspace.clear();
        preRegions.clear();
        regions.clear();
        

        // Check to see if the regions directory exists
        File regionDir = new File("bdregions");
        
        if (!regionDir.exists()) {
            BDLogging.logThis("Region directory does not exist - creating");
            regionDir.mkdir();
            // Since we know there will be no regions to load, don't bother trying
            return;
        }
        
        // For each file contained in it, attempt to load
        FilenameFilter filter = new FilenameFilter() {
          public boolean accept(File dir, String name) {
              return name.endsWith(".bdregion");          
          }
        };
        
        String[] regionFileNames = regionDir.list(filter);
        
        for (String thisRegionFile : regionFileNames) {
            try {
                FileInputStream fileIn = new FileInputStream(regionDir + "/" + thisRegionFile);
                ObjectInputStream in = new ObjectInputStream(fileIn);
                regions.add((CuboidRegion) in.readObject());
                BDLogging.logThis("Loaded region: " + thisRegionFile);
                in.close();
                fileIn.close();
            } catch (Exception e) {
                BDLogging.logThis("Failed to deserialize " + thisRegionFile);
            }
        }
    }
    
    
    // Serializes all regions
    public static void saveAllRegions(){        
        //SAVEDIR - get from the main class somehow
        for (CuboidRegion thisRegion : regions) {
            try {
                FileOutputStream fileout = new FileOutputStream("bdregions/" + thisRegion.getName() + ".bdregion");
                ObjectOutputStream out = new ObjectOutputStream(fileout);
                out.writeObject(thisRegion);
                out.close();
                fileout.close();                
            } catch (IOException i) {
                BDLogging.logThis("Failed to save region data for " + thisRegion.getName());
            }
        }
    }
    
    //TODO: Actually sanitize the string
    public static String sanitizeInput(String input) {
        
        String working = null;
        
        // Only allow a region name to be 20 characters long (just because)
        // only allow a region name to be lower case
        if (input.length() > 20) {
            working = input.substring(0, 20).toLowerCase();            
        } else {
            working = input.toLowerCase();
        }
        
        // only output alphabet characters and numbers - remove anything else
        
        String REGEX = "[^a-z0-9]";
        
        Pattern p = Pattern.compile(REGEX);
        Matcher m = p.matcher(working); // get a matcher object
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
          m.appendReplacement(sb, "");
        }
        m.appendTail(sb);
        
        working = sb.toString();
        return working;
    }
    
    private static String displayFlag(boolean thisBool, String flagName) {
    	ChatColor trueColor = ChatColor.GREEN;
    	ChatColor falseColor = ChatColor.RED;
    	
    	if (thisBool) {
    		return trueColor + flagName;
    	} else {
    		return falseColor + flagName;    		
    	}
    	
    }
    
    public static void sendRegionInfo(Player player, CuboidRegion specifiedRegion) {  
    	ChatColor mainColour = ChatColor.AQUA;
    	ChatColor valueColour = ChatColor.WHITE;
    	
        BDLogging.sendPlayer(player,mainColour + "Info for region \"" + valueColour +specifiedRegion.getName()+ mainColour +"\":");
        BDLogging.sendPlayerInfo(player,mainColour +" Coordinates: " + valueColour + specifiedRegion.getCoordinateString());              
        BDLogging.sendPlayerInfo(player,mainColour +" Owner: " + valueColour + specifiedRegion.getOwner());
        BDLogging.sendPlayerInfo(player,mainColour +" Flags: " + displayFlag(specifiedRegion.canDuplicate(),"duplicate") + ", " 
        + displayFlag(specifiedRegion.canDataCycle(),"datacycle") + ", "
        + displayFlag(specifiedRegion.canStorePaint(),"setink") + ", "
        + displayFlag(specifiedRegion.canApplyPaint(),"paint") + ", "
        + displayFlag(specifiedRegion.canBreakBlocks(),"break") + ", "
        + displayFlag(specifiedRegion.canExplode(),"explode") + ", "
        + displayFlag(specifiedRegion.canPlayersEnter(),"players") + ", "
        + displayFlag(specifiedRegion.canEnemyMobsSpawnHere(),"enemies") + ", "
        + displayFlag(specifiedRegion.canAnnounceOnEnter(),"announce")
        );
        /*
        BDLogging.sendPlayerInfo(player,mainColour +" Allow duplicator: " + valueColour + specifiedRegion.canDuplicate());
        BDLogging.sendPlayerInfo(player,mainColour +" Allow data scrolling: " + valueColour + specifiedRegion.canDataCycle());
        BDLogging.sendPlayerInfo(player,mainColour +" Allow setting ink: " + valueColour + specifiedRegion.canStorePaint());
        BDLogging.sendPlayerInfo(player,mainColour +" Allow applying ink: " + valueColour + specifiedRegion.canApplyPaint());
        BDLogging.sendPlayerInfo(player,mainColour +" Allow block breaking: " + valueColour + specifiedRegion.canBreakBlocks());
        BDLogging.sendPlayerInfo(player,mainColour +" Allow explosion damage: " + valueColour + specifiedRegion.canExplode());
        BDLogging.sendPlayerInfo(player,mainColour +" Allow enemies to spawn in region: " + valueColour + specifiedRegion.canEnemyMobsSpawnHere());
        BDLogging.sendPlayerInfo(player,mainColour +" Allow other players to enter region: " + valueColour + specifiedRegion.canPlayersEnter());
        BDLogging.sendPlayerInfo(player,mainColour +" Announce name on enter: " + valueColour + specifiedRegion.canAnnounceOnEnter());
        */
    }
    
    public static void removeRegion (Player thePlayer, String regionName) {
        
        boolean removedRegion = false;        
        CuboidRegion foundRegion = null;
        
        for (CuboidRegion thisRegion : regions) {
            if (thisRegion.getName().endsWith(regionName)) {
                foundRegion = thisRegion;
                removedRegion = true;
            }
        }
 
        if (foundRegion != null) {
            if (removedRegion = true) {
                regions.remove(foundRegion);
                File thisRegionFile = new File("bdregions/" + foundRegion.getName() + ".bdregion");
                if (thisRegionFile.exists()) {
                    thisRegionFile.delete();
                }
            }
        }
        
        if (removedRegion) {            
            BDLogging.sendPlayer(thePlayer, "Region removed");
        } else {
            BDLogging.sendPlayerError(thePlayer, "Region not found");
        }
    }    

	public static void setRegionOwner (Player thePlayer, String regionName, String newOwner) {
	    
	    boolean regionOwnerSet = false;        
	    CuboidRegion foundRegion = null;
	    
	    for (CuboidRegion thisRegion : regions) {
	        if (thisRegion.getName().endsWith(regionName)) {
	            foundRegion = thisRegion;
	            regionOwnerSet = true;
	        }
	    }
	
	    if (foundRegion != null) {
	        if (regionOwnerSet = true) {
	        	foundRegion.setOwner(newOwner);	            
	        }
	    }
	    
	    if (regionOwnerSet) {            
	        BDLogging.sendPlayer(thePlayer, "Region owner set to " + newOwner);
	    } else {
	        BDLogging.sendPlayerError(thePlayer, "Region not found");
	    }
	}    
    
    public static void createRegion(Player thePlayer, String regionName) {
        // Coordinates should already be stored in the preRegions hashtable        
        
        // Check for a pre-region
        if (preRegions.containsKey(thePlayer)) {           
            CuboidRegion newRegion = new CuboidRegion(sanitizeInput(regionName.toLowerCase()), thePlayer,preRegions.get(thePlayer));           
            regions.add(newRegion);
            saveAllRegions();
            BDLogging.sendPlayer(thePlayer, "New region created: " + ChatColor.AQUA + sanitizeInput(regionName.toLowerCase()));
        } else {
            BDLogging.sendPlayerError(thePlayer, "Not ready to create a region yet!");
        }        
    }
    
    public static void inputCoordinate(Player thePlayer, Block theBlock) {               
        
        // If there is already a saved pre-region, delete it
        if (preRegions.containsKey(thePlayer)) {
            preRegions.remove(thePlayer);
        }
        
        
        // If the player has a block stored already, create a region
        if (playerWorkspace.containsKey(thePlayer)) {
           CuboidPreRegion preRegion = new CuboidPreRegion(theBlock,playerWorkspace.get(thePlayer));
           
           playerWorkspace.remove(thePlayer);
           
           if (preRegions.containsKey(thePlayer)) {
               preRegions.remove(thePlayer);
           }            
           
           preRegions.put(thePlayer, preRegion);
           
           BDLogging.sendPlayer(thePlayer,"Ready to create a region!");  
           BDLogging.sendPlayerInfo(thePlayer,"Use \"/bdregion create <name>\" to create a region");  
        } else {
        // If the player does not have a block stored already, just store it
            playerWorkspace.put(thePlayer, theBlock);
            BDLogging.sendPlayer(thePlayer,"Block location stored");            
        }
    }
        
    public static void getRegionInfoHere(Player thePlayer, Block thisBlock) {
        // Go through list of regions and check
        
        Location blockLocation = thisBlock.getLocation();
        
        boolean foundMatch = false;
        
        for (CuboidRegion thisRegion : regions) {
            if (thisRegion.isInThisRegion(blockLocation)) {
                sendRegionInfo(thePlayer,thisRegion);                               
                foundMatch = true;
            }
        }
        
        if (foundMatch == false) {
            BDLogging.sendPlayerInfo(thePlayer,"No regions here!");
        }
    }
    
    public static CuboidRegion getRegionHere(Block thisBlock) {
        // Go through list of regions and check
        
        Location blockLocation = thisBlock.getLocation();
                
        for (CuboidRegion thisRegion : regions) {
            if (thisRegion.isInThisRegion(blockLocation)) {                                               
                return thisRegion;
            }
        }
        return null;
    }
    
    public static CuboidRegion getRegionHere(Location blockLocation) {
        // Go through list of regions and check
                
        for (CuboidRegion thisRegion : regions) {
            if (thisRegion.isInThisRegion(blockLocation)) {                                               
                return thisRegion;
            }
        }
        return null;
    }
    
     
    public static void listRegions(Player thePlayer) {
        BDLogging.sendPlayer(thePlayer, "All regions:");
        for (CuboidRegion thisRegion : regions) {
            BDLogging.sendPlayer(thePlayer, " " + thisRegion.toString());
        }
    }
    
    public static boolean canDuplicateHere(Player player, Block block) {
        boolean returnMe = true;       
        // Check for ignore permission
        if (BDPermissions.ignoresRegions(player)) {
            return true;
        }
                
        // Check for regions        
        for (CuboidRegion thisRegion : regions) {
            if (thisRegion.isInThisRegion(block.getLocation())) {
            	if (thisRegion.getOwner().toLowerCase().contentEquals((player.getName().toLowerCase()))) {
                	return true;
                } else if (thisRegion.canDuplicate() == false) {
                    returnMe = false;
                }
            }
        }
        
        return returnMe;        
    }
    
    public static boolean canDataCycleHere(Player player, Block block) {
        boolean returnMe = true;        
        // Check for ignore permission
        
        if (BDPermissions.ignoresRegions(player)) {            
            return true;            
        }
                
        // Check for regions        
        for (CuboidRegion thisRegion : regions) {
            if (thisRegion.isInThisRegion(block.getLocation())) {
            	if (thisRegion.getOwner().toLowerCase().contentEquals((player.getName().toLowerCase()))) {
                	return true;
                } else if (thisRegion.canDataCycle() == false) {
                    returnMe = false;
                }
            }
        }
    
        return returnMe;        
    }
    
    public static boolean canSetInkHere(Player player, Block block) {
        boolean returnMe = true;        
        // Check for ignore permission
        if (BDPermissions.ignoresRegions(player)) {
            return true;
        }
        
        // Check for regions        
        for (CuboidRegion thisRegion : regions) {
            if (thisRegion.isInThisRegion(block.getLocation())) {
            	if (thisRegion.getOwner().toLowerCase().contentEquals((player.getName().toLowerCase()))) {
                	return true;
                } else if (thisRegion.canStorePaint() == false) {
                    returnMe = false;
                }
            }
        }
        
        return returnMe;         
    }
    
    public static boolean canBreakBlocksHere(Player player, Block block) {
        boolean returnMe = true;        
        // Check for ignore permission
        if (BDPermissions.ignoresRegions(player)) {
            return true;
        }
        
        // Check for regions        
        for (CuboidRegion thisRegion : regions) {
            if (thisRegion.isInThisRegion(block.getLocation())) {
            	
            	if (thisRegion.getOwner().toLowerCase().contentEquals((player.getName().toLowerCase()))) {
                	return true;
                } else if (thisRegion.canBreakBlocks() == false) {
                    returnMe = false;
                }
            }
        }
        
        return returnMe;         
    }
    
    public static boolean canPaintHere(Player player, Block block) {
        boolean returnMe = true;       
        // Check for ignore permission
        if (BDPermissions.ignoresRegions(player)) {
            return true;
        }
        
        // Check for regions        
        for (CuboidRegion thisRegion : regions) {
            if (thisRegion.isInThisRegion(block.getLocation())) {
            	if (thisRegion.getOwner().toLowerCase().contentEquals((player.getName().toLowerCase()))) {
                	return true;
                } else if (thisRegion.canApplyPaint() == false) {
                    returnMe = false;
                }
            }
        }
                
        return returnMe;        
    }    
}
