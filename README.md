QuickBelt
===========

QuickBelt is a plugin for the Minecraft Bukkit API that provides
rapid auto-reloading for broken tools and used-up stacks of blocks
in your toolbar.

The related discussion thread for this plugin is located at
<http://forums.bukkit.org/threads/7202/>

Building from source
-----------

Please note that QuickBelt contains submodules, so to checkout:

    git clone git://github.com/cyklo/Bukkit-QuickBelt.git
    cd Bukkit-QuickBelt
    git submodule update --init

This projects includes an Ant make. To build, you need a copy of `bukkit.jar`
in the top-level folder. Then run `ant; ant jar` like so:

    cd Bukkit-QuickBelt
    wget -O bukkit.jar http://ci.bukkit.org/job/dev-Bukkit/lastSuccessfulBuild/artifact/target/bukkit-0.0.1-SNAPSHOT.jar
    ant
    ant jar