# TEST_CREATURES_REFERENCE

Quick lookup of creatures commonly used in card tests. Sorted by frequency of use.

## Creatures by power/toughness

| P/T | Class | Import path | Color | Notes |
|-----|-------|------------|-------|-------|
| 0/2 | `Ornithopter` | `cards/o/Ornithopter` | Colorless | Flying, 0-cost artifact creature |
| 1/1 | `LlanowarElves` | `cards/l/LlanowarElves` | Green | Mana dork ({T}: Add {G}) |
| 1/1 | `FugitiveWizard` | `cards/f/FugitiveWizard` | Blue | Vanilla |
| 1/1 | `SuntailHawk` | `cards/s/SuntailHawk` | White | Flying |
| 2/1 | `EliteVanguard` | `cards/e/EliteVanguard` | White | Vanilla |
| 2/2 | `GrizzlyBears` | `cards/g/GrizzlyBears` | Green | Most-used test creature (~1200 imports) |
| 2/4 | `GiantSpider` | `cards/g/GiantSpider` | Green | Reach |
| 3/3 | `HillGiant` | `cards/h/HillGiant` | Red | Vanilla |
| 3/3 | `AngelOfMercy` | `cards/a/AngelOfMercy` | White | Flying, ETB gain 3 life |
| 4/4 | `AirElemental` | `cards/a/AirElemental` | Blue | Flying |
| 4/4 | `SerraAngel` | `cards/s/SerraAngel` | White | Flying, Vigilance |
| 8/8 | `AvatarOfMight` | `cards/a/AvatarOfMight` | Green | Trample, cost reduction |

## Common non-creature test cards

| Card | Import path | Type | Notes |
|------|------------|------|-------|
| `Pacifism` | `cards/p/Pacifism` | Enchantment — Aura | Prevents attacking/blocking |
| `Shock` | `cards/s/Shock` | Instant | 2 damage to any target |
| `Demystify` | `cards/d/Demystify` | Instant | Destroy target enchantment |
| `HolyDay` | `cards/h/HolyDay` | Instant | Prevent all combat damage |

## Picking test creatures by power

| Need power | Best pick | Why |
|-----------|-----------|-----|
| 0 | `Ornithopter` | Only 0-power creature commonly used |
| 1 | `LlanowarElves` | Most common 1/1 |
| 2 | `GrizzlyBears` | Default test creature |
| 3 | `HillGiant` | Clean vanilla 3/3 |
| 4 | `AirElemental` | Common 4/4 flyer |
| 8 | `AvatarOfMight` | Large creature for "big power" tests |

## Helper methods in BaseCardTest

```java
// Add a ready (not summoning-sick) creature to a player's battlefield
protected Permanent addCreatureReady(Player player, Card card)

// Find a permanent by name on a player's battlefield
protected Permanent findPermanent(Player player, String name)
```

## Common test patterns

```java
// Add a ready creature directly to battlefield (bypasses casting)
Permanent creature = addCreatureReady(player1, new GrizzlyBears());

// Manual creation with summoning sickness control
Permanent perm = new Permanent(new GrizzlyBears());
perm.setSummoningSick(false);
gd.playerBattlefields.get(player1.getId()).add(perm);

// Cast from hand (goes on stack, needs priority pass to resolve)
harness.setHand(player1, List.of(new GrizzlyBears()));
harness.addMana(player1, ManaColor.GREEN, 2);
harness.castCreature(player1, 0);
harness.passBothPriorities(); // resolves
```
