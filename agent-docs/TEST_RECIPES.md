# TEST_RECIPES

Purpose: minimal reusable test patterns for new cards using `BaseCardTest`.

## Base skeleton

All card tests extend `BaseCardTest`, which provides these fields and `@BeforeEach setUp()` automatically:

```java
// Inherited from BaseCardTest — do NOT redeclare these
protected GameTestHarness harness;
protected Player player1;
protected Player player2;
protected GameService gs;
protected GameQueryService gqs;
protected GameData gd;
```

Minimal test class:

```java
class ExampleCardTest extends BaseCardTest {

    @Test
    @DisplayName("ExampleCard has correct effects")
    void hasCorrectProperties() {
        ExampleCard card = new ExampleCard();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(SomeEffect.class);
    }
}
```

**Do NOT test Scryfall metadata** (name, type, mana cost, color, power, toughness, subtypes, keywords) — it is auto-loaded from Scryfall. Only test engine logic: effects, abilities, targeting, game interactions.

## Adding mana in tests

Generic mana (e.g. the `{2}` in `{2}{W}`) can be paid with any color. For single-color cards, add total CMC of that color in one call. For multi-color, add each color separately and generic as COLORLESS. See `ORACLE_TEXT_EFFECT_MAP.md → Mana cost → test addMana reference` for the full table.

Quick examples:
```java
// {2}{W} → 3 white covers 2 generic + 1 white
harness.addMana(player1, ManaColor.WHITE, 3);

// {1}{B}{G} → each color separately, generic as COLORLESS
harness.addMana(player1, ManaColor.BLACK, 1);
harness.addMana(player1, ManaColor.GREEN, 1);
harness.addMana(player1, ManaColor.COLORLESS, 1);

// {X}{R} with X=3 → 4 red covers X(3) + 1 red
harness.addMana(player1, ManaColor.RED, 4);

// {3} colorless artifact → any ManaColor works, COLORLESS is clearest
harness.addMana(player1, ManaColor.COLORLESS, 3);
```

## Recipe: cast targeted instant/sorcery

```java
harness.setHand(player1, List.of(new Shock()));
harness.addMana(player1, ManaColor.RED, 1);
UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
harness.castInstant(player1, 0, targetId);
harness.passBothPriorities();
```

## Recipe: cast non-target instant/sorcery

```java
harness.setHand(player1, List.of(new TempestOfLight()));
harness.addMana(player1, ManaColor.WHITE, 3);
harness.castInstant(player1, 0);
harness.passBothPriorities();
```

## Recipe: activated ability with target

```java
harness.addToBattlefield(player1, new ProdigalPyromancer());
UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
harness.activateAbility(player1, 0, null, targetId);
harness.passBothPriorities();
```

## Recipe: graveyard activated ability

```java
MagmaPhoenix phoenix = new MagmaPhoenix();
harness.setGraveyard(player1, List.of(phoenix));
harness.addMana(player1, ManaColor.RED, 2);
harness.addMana(player1, ManaColor.COLORLESS, 3);
harness.activateGraveyardAbility(player1, 0); // activates first ability on graveyard card at index 0
harness.passBothPriorities(); // resolve
assertThat(gd.playerHands.get(player1.getId()))
        .anyMatch(c -> c.getName().equals("Magma Phoenix"));
```

## Recipe: force turn-step context

```java
harness.forceActivePlayer(player1);
harness.forceStep(TurnStep.DECLARE_ATTACKERS);
```

Use this for cards that care about combat windows, attack restrictions, or blocking.

## Recipe: verify stack/fizzle behavior

```java
harness.activateAbility(player1, 0, null, targetId);
harness.getGameData().playerBattlefields.get(player2.getId()).clear(); // remove target
harness.passBothPriorities();
assertThat(harness.getGameData().gameLog).anyMatch(log -> log.contains("fizzles"));
```

## Recipe: assert zone movement

```java
assertThat(gd.playerBattlefields.get(player2.getId()))
        .noneMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
assertThat(gd.playerGraveyards.get(player2.getId()))
        .anyMatch(c -> c.getName().equals("Llanowar Elves"));
```

## Recipe: assert life total changes

```java
harness.setLife(player1, 20);
harness.setLife(player2, 20);
// resolve effect
assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
```

## Recipe: interaction-required effects

- Use `handleMayAbilityChosen`, `handleCardChosen`, `handlePermanentChosen`, `handleListChoice`, or graveyard/library helpers depending on awaiting input.
- After each choice, call `harness.passBothPriorities()` only if the stack is still pending.

## High-value regression checks per new card

1. Resolves with legal target(s).
2. Fizzles correctly when all targets become illegal (if targeted).
3. Costs are enforced (mana, tap, sacrifice, summoning sickness).
4. Zone transitions are correct (battlefield, graveyard, hand, library, exile).
5. No extra side effects occur when resolution is interrupted by input prompts.

## GameTestHarness method reference

### Setup & state

| Method | Signature | Use when |
|--------|-----------|----------|
| `skipMulligan` | `()` | Called automatically by `BaseCardTest.setUp()` |
| `setHand` | `(Player, List<Card>)` | Set a player's hand to specific cards |
| `addMana` | `(Player, ManaColor, int)` | Add mana to a player's pool before casting |
| `addToBattlefield` | `(Player, Card)` | Put a permanent directly onto the battlefield |
| `setGraveyard` | `(Player, List<Card>)` | Set a player's graveyard contents |
| `setLife` | `(Player, int)` | Set a player's life total |
| `clearMessages` | `()` | Clear WebSocket messages from fake connections |

### Casting spells

| Method | Signature | Use when |
|--------|-----------|----------|
| `castCreature` | `(Player, int cardIndex)` | Cast a creature from hand |
| `castEnchantment` | `(Player, int)` | Cast non-aura enchantment |
| `castEnchantment` | `(Player, int, UUID targetId)` | Cast aura targeting a permanent |
| `castArtifact` | `(Player, int)` | Cast an artifact |
| `castPlaneswalker` | `(Player, int)` | Cast a planeswalker |
| `castSorcery` | `(Player, int, int xValue)` | Cast X-cost sorcery |
| `castSorcery` | `(Player, int, UUID targetId)` | Cast targeted sorcery |
| `castSorcery` | `(Player, int, int xValue, UUID targetId)` | Cast X-cost targeted sorcery |
| `castSorcery` | `(Player, int, List<UUID> targetIds)` | Cast multi-target sorcery |
| `castInstant` | `(Player, int)` | Cast non-targeted instant |
| `castInstant` | `(Player, int, UUID targetId)` | Cast targeted instant |
| `castInstant` | `(Player, int, List<UUID> targetIds)` | Cast multi-target instant |
| `castInstantWithConvoke` | `(Player, int, List<UUID>, List<UUID>)` | Cast instant with convoke creatures |
| `castAndResolveInstant` | `(Player, int)` / `(..., UUID)` / `(..., List<UUID>)` | Cast + auto `passBothPriorities()` |
| `castAndResolveSorcery` | `(Player, int, int)` / `(..., UUID)` / `(..., int, UUID)` / `(..., List<UUID>)` | Cast + auto `passBothPriorities()` |
| `playGraveyardLand` | `(Player, int)` | Play a land from graveyard (Crucible of Worlds) |

### Abilities

| Method | Signature | Use when |
|--------|-----------|----------|
| `activateAbility` | `(Player, int permIdx, Integer xValue, UUID targetId)` | Activate first ability on a permanent |
| `activateAbility` | `(Player, int permIdx, Integer xValue, UUID targetId, Zone)` | Activate ability targeting a specific zone |
| `activateAbility` | `(Player, int permIdx, int abilityIdx, Integer xValue, UUID targetId)` | Activate Nth ability on a permanent |
| `activateGraveyardAbility` | `(Player, int graveyardCardIndex)` | Activate first graveyard ability on a card in graveyard |
| `activateGraveyardAbility` | `(Player, int graveyardCardIndex, int abilityIndex)` | Activate Nth graveyard ability on a card in graveyard |
| `sacrificePermanent` | `(Player, int permIdx, UUID targetId)` | Sacrifice a permanent (e.g. for Siege-Gang) |

### Player input handlers

| Method | Signature | Use when |
|--------|-----------|----------|
| `handlePermanentChosen` | `(Player, UUID)` | Game awaits permanent selection (sacrifice, etc.) |
| `handleMultiplePermanentsChosen` | `(Player, List<UUID>)` | Game awaits multiple permanent selections |
| `handleMultipleGraveyardCardsChosen` | `(Player, List<UUID>)` | Game awaits graveyard card selections |
| `handleCardChosen` | `(Player, int cardIndex)` | Game awaits hand card selection |
| `handleGraveyardCardChosen` | `(Player, int cardIndex)` | Game awaits graveyard card by index |
| `handleListChoice` | `(Player, String colorName)` | Game awaits color choice (e.g. "White") |
| `handleMayAbilityChosen` | `(Player, boolean)` | Game awaits may-ability yes/no |
| `handleCombatDamageAssigned` | `(Player, int attackerIdx, Map<UUID, Integer>)` | Assign combat damage to blockers |

### Game control

| Method | Signature | Use when |
|--------|-----------|----------|
| `forceActivePlayer` | `(Player)` | Set active player for deterministic tests |
| `forceStep` | `(TurnStep)` | Jump to a specific turn step |
| `clearPriorityPassed` | `()` | Reset priority state |
| `passPriority` | `(Player)` | Single player passes priority |
| `passBothPriorities` | `()` | Both players pass (resolves top of stack) |
| `getPermanentId` | `(Player, String cardName) → UUID` | Look up permanent UUID by card name |

### Assertions

| Method | Signature | Checks |
|--------|-----------|--------|
| `assertLife` | `(Player, int)` | Life total equals expected |
| `assertOnBattlefield` | `(Player, String cardName)` | Card is on battlefield |
| `assertNotOnBattlefield` | `(Player, String cardName)` | Card is NOT on battlefield |
| `assertInGraveyard` | `(Player, String cardName)` | Card is in graveyard |
| `assertNotInGraveyard` | `(Player, String cardName)` | Card is NOT in graveyard |
| `assertInHand` | `(Player, String cardName)` | Card is in hand |
| `assertNotInHand` | `(Player, String cardName)` | Card is NOT in hand |

### Getters

| Getter | Returns | Use when |
|--------|---------|----------|
| `getGameData()` | `GameData` | Direct access to game state for custom assertions |
| `getPlayer1()` / `getPlayer2()` | `Player` | Get player references |
| `getConn1()` / `getConn2()` | `FakeConnection` | Inspect sent WebSocket messages |
| `getGameService()` | `GameService` | Call game engine methods directly |
| `getGameRegistry()` | `GameRegistry` | Access game registry |
| `getGameQueryService()` | `GameQueryService` | Query game state (static bonuses, etc.) |
| `getMessageHandler()` | `MessageHandler` | For AI/message handler tests |
| `getSessionManager()` | `WebSocketSessionManager` | Access session management |

## Reference tests

- `magical-vibes-backend/src/test/java/com/github/laxika/magicalvibes/cards/o/OrcishArtilleryTest.java`
- `magical-vibes-backend/src/test/java/com/github/laxika/magicalvibes/cards/s/ShockTest.java`
- `magical-vibes-backend/src/test/java/com/github/laxika/magicalvibes/cards/c/CondemnTest.java`
- `magical-vibes-backend/src/test/java/com/github/laxika/magicalvibes/cards/t/TwincastTest.java`
- `magical-vibes-backend/src/test/java/com/github/laxika/magicalvibes/cards/s/SiegeGangCommanderTest.java`

