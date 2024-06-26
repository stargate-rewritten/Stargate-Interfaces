package net.knarcraft.stargateinterfaces.command;

import net.knarcraft.stargateinterfaces.manager.IconManager;
import net.knarcraft.stargateinterfaces.property.Icon;
import net.knarcraft.stargateinterfaces.util.NameHelper;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.api.StargateAPI;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.RegistryAPI;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.api.permission.PermissionManager;
import org.sgrewritten.stargate.network.StorageType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static net.knarcraft.stargateinterfaces.util.TabCompleterHelper.filterMatching;

/**
 * A tab completer for the /sgc dial command
 */
public class DialTabCompleter implements TabCompleter {

    private final String spaceReplacement = IconManager.getIconString(Icon.SPACE_REPLACEMENT);
    private final StargateAPI stargateAPI;

    /**
     * Instantiates a new dial tab completer
     *
     * @param stargateAPI <p>A reference to the Stargate API</p>
     */
    public DialTabCompleter(StargateAPI stargateAPI) {
        this.stargateAPI = stargateAPI;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s,
                                      @NotNull String[] args) {
        //Don't display any info to non-authorized users
        if (!commandSender.hasPermission(StargateCommandType.DIAL.getPermissionNode())) {
            return new ArrayList<>();
        }

        if (!(commandSender instanceof Player player)) {
            return new ArrayList<>();
        }

        List<String> availableNetworks = new ArrayList<>();
        Map<String, List<String>> availablePortals = new HashMap<>();
        RegistryAPI registryAPI = stargateAPI.getRegistry();
        PermissionManager permissionManager = stargateAPI.getPermissionManager(player);

        //Populate the collections with available networks and portals
        populateNetworksAndPortals(permissionManager, availableNetworks, availablePortals);

        if (args.length > 2) {
            return new ArrayList<>();
        } else if (args.length > 1) {
            Network network = NameHelper.getNetworkFromName(registryAPI, args[0]);
            if (network != null) {
                String networkName = NameHelper.getVisualNetworkName(network);
                if (availablePortals.containsKey(networkName)) {
                    return filterMatching(availablePortals.get(networkName), args[1].replace(spaceReplacement, " "));
                }
            }
        } else {
            return filterMatching(availableNetworks, args[0].replace(spaceReplacement, " "));
        }

        return new ArrayList<>();
    }

    /**
     * Populates the given collections with available networks and portals
     *
     * @param permissionManager <p>The permission manager to use to check for availability</p>
     * @param availableNetworks <p>The list to store available networks to</p>
     * @param availablePortals  <p>The map to store available portals to</p>
     */
    private void populateNetworksAndPortals(PermissionManager permissionManager, List<String> availableNetworks,
                                            Map<String, List<String>> availablePortals) {
        Iterator<Network> networkIterator = stargateAPI.getRegistry().getNetworkRegistry(StorageType.LOCAL).iterator();
        //Get all available networks and portals
        while (networkIterator.hasNext()) {
            Network network = networkIterator.next();
            String networkName = NameHelper.getVisualNetworkName(network);
            Collection<Portal> portals = network.getAllPortals();
            for (Portal portal : portals) {
                if (permissionManager.hasAccessPermission((RealPortal) portal)) {
                    //Add an empty list if the network has not been encountered before
                    if (!availablePortals.containsKey(networkName)) {
                        availablePortals.put(networkName, new LinkedList<>());
                    }
                    availablePortals.get(networkName).add(portal.getName().replace(" ", spaceReplacement));
                }
            }
        }
        //Add only the network names with portals available to the player
        availableNetworks.addAll(availablePortals.keySet());
    }

}
