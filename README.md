# nCollections API

This module contains **only** the public API surface of nCollections: one
interface (`nCollectionsService`), a static facade (`nCollectionsAPI`), and
two Bukkit events. No implementation logic lives here — it's safe to depend
on, and there's nothing sensitive to protect in it.

You need the [nCollections](https://builtbybit.com/resources/ncollections-fully-configurable.117823/) plugin actually
installed and enabled on the server for any of this to do anything; this
module only gives you something to *compile* against.

## Installation

Add the JitPack repository, then depend on this module with
`provided`/`compileOnly` scope — **never shade it into your own jar**. The
real classes are supplied at runtime by the nCollections plugin jar
installed on the server; if you bundle a second copy into your own plugin,
you'll get a duplicate class definition and the lookup below will silently
fail.

**Maven**

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.NebuxStudio.nCollections-api</groupId>
    <artifactId>ncollections-api</artifactId>
    <version>v1.1.0</version>
    <scope>provided</scope>
</dependency>
```

**Gradle**

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    compileOnly 'com.github.NebuxStudio.nCollections-api:ncollections-api:v1.1.0'
}
```

**plugin.yml**

```yaml
softdepend: [nCollections]
# or "depend: [nCollections]" if your plugin can't function without it
```

## Getting started

Every method is a static call on `nCollectionsAPI` — no need to hold a
reference to the nCollections plugin instance, no setup on your end beyond
the dependency above:

```java
import com.nebuxstudio.ncollections.api.nCollectionsAPI;

nCollectionsAPI.addProgress(player, "mining", "iron", 1);
```

Under the hood, `NCollectionsAPI` looks up the live implementation through
Bukkit's `ServicesManager`. If nCollections isn't installed or hasn't
finished enabling yet, every call fails safely — `false`, `0`, or an empty
collection, never an exception. If you'd rather check availability
explicitly first:

```java
if (nCollectionsAPI.get() == null) {
    getLogger().warning("nCollections not found, disabling integration.");
    return;
}
```

## Thread safety

- **Read methods** (`getProgress`, `isLevelCompleted`, `getCurrentLevel`,
  `getMaxLevel`, `isMaxed`, `hasPendingClaim`, `entryExists`, `isDataLoaded`,
  `getCategories`, `getEntries`, `getAllProgress`) are safe to call from
  **any thread**.
- **Write methods** (`addProgress`, `setProgress`, `resetEntry`, `resetAll`,
  `claimLevel`, `claimAll`, `openMenu`) always hop onto the correct thread
  for the target player internally — the main thread on Bukkit/Spigot/
  Paper/Purpur, that player's own region thread on Folia — before touching
  anything. Safe to call from an async task (e.g. a database callback).
  Because of that hop, the effect may not be visible the instant the call
  returns; the return value only reports whether the *request* was valid
  (player online, category/entry exist), not that it already ran.

## Method reference

Every example assumes `Player player` is already in scope, and category/
entry ids match whatever is defined in the server's `collections.yml`
(these examples use `"mining"` / `"iron"`).

### Writing progress

**`addProgress(Player player, String categoryId, String entryId, long amount)`**
Adds to a player's progress. Works for any entry regardless of its
configured trigger, but this is the main method a `CUSTOM`-trigger entry is
meant to be driven by — nCollections has no listener for those at all.

```java
// A quest plugin rewarding collection progress for finishing a task
nCollectionsAPI.addProgress(player, "special", "quest-points", 1);
```

**`setProgress(Player player, String categoryId, String entryId, long amount)`**
Sets progress to an exact value instead of adding to it — for importing
data from another system. Raising the value still runs the normal
level-completion checks (rewards granted/queued, messages sent, events
fired); lowering it just adjusts the counter and never un-completes a level
already earned.

```java
// Migrating a player's stats from a previous stats-tracking plugin
nCollectionsAPI.setProgress(player, "mining", "iron", legacyIronCount);
```

**`resetEntry(Player player, String categoryId, String entryId)`**
Wipes progress, completed levels, and pending claims for one entry. Cannot
be undone.

```java
nCollectionsAPI.resetEntry(player, "mining", "iron");
```

**`resetAll(Player player)`**
Wipes every collection a player has ever touched. Cannot be undone.

```java
nCollectionsAPI.resetAll(player);
```

### Claiming rewards

**`claimLevel(Player player, String categoryId, String entryId, int level)`**
Claims a single level's pending reward, if one is waiting.

```java
nCollectionsAPI.claimLevel(player, "mining", "iron", 2);
```

**`claimAll(Player player)`**
Claims every pending reward the player has, across every category.

```java
nCollectionsAPI.claimAll(player);
```

### Reading progress

**`getProgress(Player player, String categoryId, String entryId) -> long`**
Current cumulative amount collected (0 if never collected).

```java
long ironCollected = nCollectionsAPI.getProgress(player, "mining", "iron");
```

**`isLevelCompleted(Player player, String categoryId, String entryId, int level) -> boolean`**

```java
if (nCollectionsAPI.isLevelCompleted(player, "mining", "iron", 2)) {
    player.sendMessage("You already maxed out Iron level 2!");
}
```

**`getCurrentLevel(Player player, String categoryId, String entryId) -> int`**
Highest level the player has completed (0 if none yet).

**`getMaxLevel(String categoryId, String entryId) -> int`**
Highest level number defined for that entry in `collections.yml`. No
`Player` parameter — this is server config, not player state.

```java
int current = nCollectionsAPI.getCurrentLevel(player, "mining", "iron");
int max = nCollectionsAPI.getMaxLevel("mining", "iron");
player.sendMessage("Iron: level " + current + "/" + max);
```

**`isMaxed(Player player, String categoryId, String entryId) -> boolean`**
Whether the player has completed every level of that entry.

**`hasPendingClaim(Player player, String categoryId, String entryId, int level) -> boolean`**
Whether a specific level's reward is sitting unclaimed.

```java
if (nCollectionsAPI.hasPendingClaim(player, "mining", "iron", 2)) {
    player.sendMessage("You have an unclaimed reward! Run /collections.");
}
```

**`entryExists(String categoryId, String entryId) -> boolean`**
Whether that category/entry combination exists in the loaded
`collections.yml` at all. Useful to validate config on your own plugin's
startup.

**`isDataLoaded(Player player) -> boolean`**
Whether the player's collection data has finished loading — briefly `false`
right after they join, since it loads asynchronously.

```java
@EventHandler
public void onJoin(PlayerJoinEvent event) {
    // Player data loads async on join — don't read it the same tick.
    Bukkit.getScheduler().runTaskLater(this, () -> {
        long progress = nCollectionsAPI.getProgress(event.getPlayer(), "mining", "iron");
        // ...
    }, 20L);
}
```

**`getCategories() -> Set<String>`**
Every category id currently defined.

**`getEntries(String categoryId) -> Set<String>`**
Every entry id defined within a category.

```java
for (String categoryId : nCollectionsAPI.getCategories()) {
    for (String entryId : nCollectionsAPI.getEntries(categoryId)) {
        System.out.println(categoryId + "." + entryId);
    }
}
```

**`getAllProgress(Player player) -> Map<String, Long>`**
A snapshot of every entry the player has any progress in, keyed
`"category.entry"`. Handy for building your own stats/leaderboard plugin
without querying entry-by-entry.

```java
Map<String, Long> all = nCollectionsAPI.getAllProgress(player);
long totalItemsCollected = all.values().stream().mapToLong(Long::longValue).sum();
```

### GUI

**`openMenu(Player player)`**
Opens the categories menu — the same screen `/collections` opens.

**`openMenu(Player player, String categoryId)`**
Opens a specific category's entries menu directly — same as
`/collections <category>`.

```java
// From an NPC interaction, another plugin's own menu, a custom command, etc.
nCollectionsAPI.openMenu(player, "mining");
```

### Leaderboards

**`getEntryLeaderboard(String categoryId, String entryId, int limit) -> CompletableFuture<List<LeaderboardEntry>>`**
Top players for a single collection entry, ranked highest first. Always
global — with MySQL storage shared across a network, this ranks everyone
on the network, not just players on this server.

**`getCategoryLeaderboard(String categoryId, int limit) -> CompletableFuture<List<LeaderboardEntry>>`**
Same idea, but ranked by the SUM of everything a player has collected
across every entry in that category.

Both complete off the calling thread, and `LeaderboardEntry` is a simple
record: `uuid()`, `name()` (resolved fresh via Bukkit's own offline-player
cache, never stored by nCollections), and `amount()`.

```java
nCollectionsAPI.getEntryLeaderboard("mining", "iron", 10).thenAccept(top10 -> {
    int rank = 1;
    for (LeaderboardEntry entry : top10) {
        System.out.println("#" + rank + " " + entry.name() + " - " + entry.amount());
        rank++;
    }
});
```

## Events

Prefer these over polling when you want to *react* to progress rather than
cause it.

### `CollectionProgressEvent`

Fired right **before** progress is applied, for any trigger (block break,
farming, fishing, entity kill, or the API itself). Cancellable, and the
amount can be changed before it lands.

```java
public class BoosterListener implements Listener {

    @EventHandler
    public void onProgress(CollectionProgressEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("myplugin.vip")) {
            event.setAmount(event.getAmount() * 2); // double collection gains for VIPs
        }
    }
}
```

```java
public class RestrictedWorldListener implements Listener {

    @EventHandler
    public void onProgress(CollectionProgressEvent event) {
        if (event.getPlayer().getWorld().getName().equals("minigame_arena")) {
            event.setCancelled(true); // no collection progress inside the minigame
        }
    }
}
```

Available getters: `getPlayer()`, `getCategoryId()`, `getEntryId()`,
`getAmount()` / `setAmount(long)`, `isCancelled()` / `setCancelled(boolean)`.

### `CollectionLevelCompleteEvent`

Fired **after** a level is completed — the level is already marked
completed and its reward already granted or queued for claim. Not
cancellable, purely informational.

```java
public class AnnounceListener implements Listener {

    @EventHandler
    public void onLevelComplete(CollectionLevelCompleteEvent event) {
        Bukkit.broadcastMessage(event.getPlayer().getName() + " reached level "
                + event.getLevel() + " in " + event.getEntryId() + "!");

        if (event.isRequiresClaim()) {
            event.getPlayer().sendMessage("Don't forget to claim your reward!");
        }
    }
}
```

Available getters: `getPlayer()`, `getCategoryId()`, `getEntryId()`,
`getLevel()`, `isRequiresClaim()`.

## Full example

A tiny plugin that gives progress toward a custom `CUSTOM`-trigger
collection whenever a player votes (via VotifierEvent, for example), and
congratulates them in a custom way when they max it out:

```java
public final class VoteCollectionBridge extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        if (nCollectionsAPI.get() == null) {
            getLogger().warning("nCollections not found, disabling.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onVote(VotifierEvent event) {
        Player player = Bukkit.getPlayerExact(event.getVote().getUsername());
        if (player != null) {
            NCollectionsAPI.addProgress(player, "special", "votes", 1);
        }
    }

    @EventHandler
    public void onLevelComplete(CollectionLevelCompleteEvent event) {
        if (!event.getCategoryId().equals("special") || !event.getEntryId().equals("votes")) {
            return; // not our collection, ignore
        }
        if (nCollectionsAPI.isMaxed(event.getPlayer(), "special", "votes")) {
            Bukkit.broadcastMessage(event.getPlayer().getName() + " maxed out the Votes collection!");
        }
    }
}
```
