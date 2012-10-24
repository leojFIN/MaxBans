package org.maxgamer.maxbans.listeners;

import java.net.InetAddress;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.banmanager.*;
import org.maxgamer.maxbans.util.Util;

public class JoinListener implements Listener{
    private MaxBans plugin;
    public JoinListener(MaxBans plugin){
        this.plugin = plugin;
    }
    
    @EventHandler (priority = EventPriority.LOWEST)
    public void onJoinHandler(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        
        if(plugin.getBanManager().lockdown){
	        if(!player.hasPermission("maxbans.lockdown.bypass")){
	    		event.setKickMessage("Server is in lockdown mode. Try again shortly. Reason: \n" + plugin.getBanManager().lockdownReason);
	    		event.setResult(Result.KICK_OTHER);
	    		return;
	    	}
	        else{ //Delay this, because it's fucken more important than essentials
	        	final String name = player.getName();
	        	Bukkit.getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable(){
					public void run() {
						Player p = Bukkit.getPlayerExact(name);
						if(p != null){
							p.sendMessage(ChatColor.RED + "Bypassing lockdown!");
						}
					}
	        		
	        	}, 40);
	        }
        }
        
        InetAddress address = event.getAddress();
        
        //Ban
        Ban ban = plugin.getBanManager().getBan(player.getName());
        
        //IP Ban
        IPBan ipban= plugin.getBanManager().getIPBan(address);
        
        //If they havent been banned or IP banned, they can join.
        if(ipban == null && ban == null){
        	plugin.getBanManager().logIP(player.getName(), address.getHostAddress());
        	return;
        }
        
        String reason;
        String banner;
        long expires = 0;
        
        if (ipban != null){ 
            if (ipban instanceof TempIPBan) {
            	TempIPBan tempipban = (TempIPBan) ipban;
            	expires = tempipban.getExpires(); //wish there was a better way to do this
            }
            reason = ipban.getReason();
            banner = ipban.getBanner();
            
        } else{ //We dont need to check ban isn't null here. We already did.
            if (ban instanceof TempBan) {
            	TempBan tempban = (TempBan) ban;
            	expires = tempban.getExpires();
            }
            reason = ban.getReason();
            banner = ban.getBanner();
        }
        
        StringBuilder km = new StringBuilder(25); //kickmessage
        km.append("You\'re banned!\n Reason: ");
        km.append(reason);
        km.append("\n By ");
        km.append(banner + ". ");  //this is probably going to make the length too long
        					// It's more info, it shouldnt be an issue. We can use \n now too!
        					// ThankYou patch notes!
        if (expires > 0) {
        	km.append("Expires in " + Util.getTimeUntil(expires));
        }
        event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
        event.setKickMessage(km.toString());
    }
}
