# AudioPerspective

AudioPerspective is a Fabric addon for [Simple Voice Chat by henkelmax](https://github.com/henkelmax/simple-voice-chat) that allows the audio listener position to be separated from the physical Minecraft player position.

The primary use case is detached-camera and freecam setups. While the player remains physically in one location, supported audio can be received based on the position of the active camera instead.

Simple Voice Chat already partially supports detached cameras through client-side camera-relative spatialization. However, its server-side recipient selection still uses the physical player position. AudioPlayer inherits this limitation through its use of Simple Voice Chat, while AudioPlayer Roleplay additionally performs its own server-side region checks.

## Status

AudioPerspective is currently experimental as a proof-of-concept implementation.

The core functionality is working, but the mod currently depends on internal implementation details of Simple Voice Chat and AudioPlayer Roleplay. Compatibility may therefore break when those mods change their internals.

## Requirements

AudioPerspective must be installed on both the client and the server.

Current development target:

* Minecraft 26.2
* Fabric Loader 0.19.3 or newer
* Fabric API
* Simple Voice Chat 2.6.22+26.2

Optional integration:

* AudioPlayer
* AudioPlayer Roleplay 0.4.0+26.2

## How it works

When the local Minecraft camera becomes detached from the player, the client periodically sends the camera position and dimension to the server.

The server keeps this virtual listener position as temporary per-player state. When Simple Voice Chat evaluates whether that player is within range of a locational audio source, AudioPerspective substitutes the virtual listener position instead of the physical player position.

When the camera is reattached, the player disconnects, the dimension changes, or the virtual listener state expires, the physical player position is used again.

The physical Minecraft player is never altered or tampered with by the mod.

## Simple Voice Chat

Simple Voice Chat already supports camera-relative spatialization on the client.

However, server-side recipient selection normally still uses the physical player position. This means that audio outside the server's configured broadcast range is never sent to the client, even when a detached camera is close enough to hear it.

AudioPerspective changes the receiver position used for this server-side range calculation.

## AudioPlayer

Ordinary locational AudioPlayer sounds use Simple Voice Chat's locational audio channels and therefore benefit from the same virtual listener position automatically.

## AudioPlayer Roleplay

AudioPlayer Roleplay performs additional region calculations using the player's position directly.

AudioPerspective includes optional compatibility for its Regions module, including:

* `CLIP`
* `FALLOFF`
* multi-region nearest-boundary selection

AudioPlayer Roleplay is not a required dependency.

## Freecam compatibility

The mod does not implement its own freecam.

Instead, it observes Minecraft's active camera entity. A freecam implementation that detaches the camera from the local player without moving the real server-side player should generally be compatible with this approach.

This implementation was tested and validated with both [Freecam by hashalite and MattSturgeon](https://github.com/MinecraftFreecam/Freecam) and [Tweakeroo by maruohon](https://github.com/maruohon/tweakeroo/) freecam implementations. Compatibility with every freecam implementation is not guaranteed.

## Security considerations

A virtual listener position can allow a player to receive spatial audio from a location where their physical player is not present.

The current implementation therefore has implications similar to remote listening or observation features and should only be used on servers where this behavior is intended.

More granular controls, such as separating player voice from other mod/plugin-generated locational audio, are planned for future development. Implementing these controls cleanly may require additional upstream APIs or adaptations in Simple Voice Chat and/or AudioPlayer Roleplay.

## Technical implementation

The current implementation uses Mixins because Simple Voice Chat does not (yet) expose a public server-side API for overriding a receiver's effective listener position.

For Simple Voice Chat, the mod intercepts the receiver-position reads used by its server-side range calculation and substitutes the active virtual listener position when appropriate.

The AudioPlayer Roleplay integration similarly replaces only the position reads involved in region membership and falloff calculations.

These integrations are intentionally narrow: unrelated Minecraft player positioning and audio source positioning remain unchanged.

A potential future upstream listener-position API in Simple Voice Chat would allow these internal Mixins to be reduced or removed, as well as enabling other future mods to do more creative things with a player's effective listening position.

## Building

Java 25 is required.

Clone the repository and run:

```bash
./gradlew build
```

The built JAR will be created under:

```text
build/libs/
```

## License

Copyright (c) 2026 distant-nova

All Rights Reserved.
