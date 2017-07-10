# Configuration (ccemux.json)
Configuration is done within the `ccemux.json` file.
This file resides in the `~/.local/share/ccemux` directory on Linux, `%APPDATA%\ccemux` on Windows, and `~/Library/Application Support/ccemux` on MacOSX.

It contains the following:  
- `ccmodule`, default `ComputerCraft`: ComputerCraft module name.
- `ccRevision`, default `1.80pr0-build0`: Module revision.
- `ccExt`, default `jar`: Module extention.
- `ccPatternRemote`, default `http://cc.crzd.me/maven/dan200/computercraft/ComputerCraft/[revision]/[module]-[revision].[ext]`: Remote path from which to download the module if it doesn't exist locally.
- `ccPatternLocal`, default `[module][revision].[ext]`: Local module path.
- `termWidth`, default `51`: Computer screen width in cells.
- `termHeight`, default `19`: Computer screen height in cells.
- `termScale`, default `3`: Scale factor from computercraft pixel to display pixel.
- `apiEnabled`, default `true`: Whether or not the CCEmuX API functionality is exposed to the user.
- `renderer`, default `AWT`: Which renderer to use. The only option right now is `AWT`.
- `maxComputerCapacity`, default `1000000`: Computer storage limit in bytes.
- `pluginBlacklist`, default `[]`: List of blacklisted plugins.
