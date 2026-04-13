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

## Pitfall: flashback + combat targeting (passPriority drains mana)

**Problem:** When testing a flashback spell that targets attacking/blocking creatures, calling
`passPriority(player1)` before `castFlashback(player2, ...)` can trigger the auto-pass system.
Auto-pass checks whether player2 has playable cards **in hand** — it does not recognize flashback-castable
cards in the graveyard. If player2's hand has nothing playable, auto-pass fires for player2 too,
both players have passed, the step advances, and the **mana pool is emptied**. The subsequent
`castFlashback` then fails with "Not enough mana to pay flashback cost".

**Solution:** Do NOT call `passPriority()` before `castFlashback()`. The `castFlashback` helper
internally calls `ensurePriority(player)`, which sets up the priority state correctly without
triggering auto-pass.

```java
// WRONG — passPriority triggers auto-pass, which may drain mana
harness.forceStep(TurnStep.DECLARE_ATTACKERS);
harness.clearPriorityPassed();
harness.setGraveyard(player2, List.of(new MyFlashbackCard()));
harness.addMana(player2, ManaColor.WHITE, 1);
harness.addMana(player2, ManaColor.COLORLESS, 3);
harness.passPriority(player1);           // ← auto-pass may advance step & empty pool
harness.castFlashback(player2, 0, targetId); // ← "Not enough mana" error

// CORRECT — castFlashback handles priority internally via ensurePriority()
harness.forceStep(TurnStep.DECLARE_ATTACKERS);
harness.clearPriorityPassed();
harness.setGraveyard(player2, List.of(new MyFlashbackCard()));
harness.addMana(player2, ManaColor.WHITE, 1);
harness.addMana(player2, ManaColor.COLORLESS, 3);
harness.castFlashback(player2, 0, targetId); // ← works, ensurePriority sets up state
```

**Note:** This only affects flashback (graveyard) casts. Hand casts with `castInstant` still need
`passPriority(player1)` because the auto-pass system recognizes cards in hand as playable.

Reference: `BurningOilTest.java` — flashback instant targeting attacking/blocking creatures.

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

## Recipe: dies → targeted trigger (creature targets creature)

Pattern for cards like Bogardan Firefiend, Festering Goblin, Necropede, Sparring Construct — creature dies and controller chooses a target creature for the death trigger.

### Helper: set up combat death

```java
private void setupCombatWhereCardDies(String cardName) {
    Permanent perm = gd.playerBattlefields.get(player1.getId()).stream()
            .filter(p -> p.getCard().getName().equals(cardName))
            .findFirst().orElseThrow();
    perm.setSummoningSick(false);
    perm.setAttacking(true);

    GrizzlyBears bigBear = new GrizzlyBears();
    bigBear.setPower(3);
    bigBear.setToughness(3);
    Permanent blockerPerm = new Permanent(bigBear);
    blockerPerm.setSummoningSick(false);
    blockerPerm.setBlocking(true);
    blockerPerm.addBlockingTarget(0);
    gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

    harness.forceActivePlayer(player1);
    harness.forceStep(TurnStep.DECLARE_BLOCKERS);
    harness.clearPriorityPassed();
}
```

### Test: death trigger prompts for target

```java
@Test
@DisplayName("When CardName dies, controller is prompted to choose a target creature")
void deathTriggerPromptsForTarget() {
    harness.addToBattlefield(player1, new CardName());
    harness.addToBattlefield(player2, new GrizzlyBears());
    setupCombatWhereCardDies("CardName");

    harness.passBothPriorities(); // combat damage — CardName dies

    assertThat(gd.playerGraveyards.get(player1.getId()))
            .anyMatch(c -> c.getName().equals("CardName"));
    assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
    assertThat(gd.interaction.permanentChoice().playerId()).isEqualTo(player1.getId());
}
```

### Test: death trigger resolves on chosen target

```java
@Test
@DisplayName("Death trigger applies effect to chosen creature")
void deathTriggerResolvesOnTarget() {
    harness.addToBattlefield(player1, new CardName());
    harness.addToBattlefield(player2, new GrizzlyBears());
    UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

    setupCombatWhereCardDies("CardName");
    harness.passBothPriorities(); // CardName dies

    assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);

    // Choose target — handlePermanentChosen puts the triggered ability on the stack
    harness.handlePermanentChosen(player1, bearsId);

    // Verify stack entry
    assertThat(gd.stack).hasSize(1);
    assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("CardName");
    assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(bearsId);

    // Resolve — assert the effect (damage, -1/-1, counters, etc.)
    harness.passBothPriorities();

    // TODO: assert the effect on bearsId (e.g. zone change, modifier, life change)
}
```

### Test: validIds contains only legal targets

```java
@Test
@DisplayName("Death trigger only offers valid creature targets")
void deathTriggerValidIds() {
    harness.addToBattlefield(player1, new CardName());
    GrizzlyBears bear = new GrizzlyBears();
    harness.addToBattlefield(player2, bear);
    UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

    setupCombatWhereCardDies("CardName");
    harness.passBothPriorities();

    assertThat(gd.interaction.permanentChoice().validIds()).contains(bearsId);
}
```

### Test: no valid targets after Wrath (creature-only triggers)

```java
@Test
@DisplayName("Death trigger skips if no creatures on battlefield (Wrath)")
void deathTriggerSkipsWithNoCreatures() {
    harness.addToBattlefield(player1, new CardName());
    harness.addToBattlefield(player2, new GrizzlyBears());

    harness.setHand(player1, List.of(new WrathOfGod()));
    harness.addMana(player1, ManaColor.WHITE, 4);

    harness.getGameService().playCard(gd, player1, 0, 0, null, null);
    harness.passBothPriorities(); // Wrath resolves — all creatures die

    // No creature-only trigger prompt (no valid targets)
    assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.PERMANENT_CHOICE);
    assertThat(gd.gameLog).anyMatch(log -> log.contains("no valid targets"));
}
```

### Test: fizzle when target removed before resolution

```java
@Test
@DisplayName("Ability fizzles when target creature is removed before resolution")
void abilityFizzlesWhenTargetRemoved() {
    harness.addToBattlefield(player1, new CardName());
    harness.addToBattlefield(player2, new GrizzlyBears());
    UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

    setupCombatWhereCardDies("CardName");
    harness.passBothPriorities(); // CardName dies

    harness.handlePermanentChosen(player1, bearsId);

    // Remove target before resolution
    gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getId().equals(bearsId));

    harness.passBothPriorities(); // resolve — should fizzle

    assertThat(gd.stack).isEmpty();
    assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
}
```

## Recipe: dies → targeted trigger (any target — creature or player)

Variant for cards like Perilous Myr, Pitchburn Devils — `DealDamageToAnyTargetEffect` allows choosing a creature OR a player.

The combat-death helper and creature-target tests are identical to the creature-only recipe above. The difference is the player-targeting tests:

### Test: death trigger targets a player

```java
@Test
@DisplayName("Death trigger deals damage to chosen player")
void deathTriggerTargetsPlayer() {
    harness.addToBattlefield(player1, new CardName());
    harness.setLife(player2, 20);

    setupCombatWhereCardDies("CardName");
    harness.passBothPriorities(); // CardName dies

    assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);

    // Target a player (pass player UUID instead of permanent UUID)
    harness.handlePermanentChosen(player1, player2.getId());
    harness.passBothPriorities();

    assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18); // adjust for damage amount
}
```

### Test: any-target trigger still fires after Wrath (can target players)

```java
@Test
@DisplayName("Any-target death trigger still fires after Wrath (targets player)")
void deathTriggerAfterWrathTargetsPlayer() {
    harness.addToBattlefield(player1, new CardName());
    harness.setLife(player2, 20);

    harness.setHand(player1, List.of(new WrathOfGod()));
    harness.addMana(player1, ManaColor.WHITE, 4);

    harness.getGameService().playCard(gd, player1, 0, 0, null, null);
    harness.passBothPriorities();

    // Any-target trigger should still fire — players are valid targets
    assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);

    harness.handlePermanentChosen(player1, player2.getId());
    harness.passBothPriorities();

    assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
}
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

## Recipe: aura with static effect

```java
// Setup: attach aura directly (skip casting)
Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

Permanent auraPerm = new Permanent(new YourAura());
auraPerm.setAttachedTo(bearsPerm.getId());
gd.playerBattlefields.get(player1.getId()).add(auraPerm);

// Assert static effect (e.g. P/T boost via GameQueryService)
assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(EXPECTED);

// Remove aura and verify effect is gone
gd.playerBattlefields.get(player1.getId()).remove(auraPerm);
assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(BASE);
```

## Recipe: aura's own activated ability (not granted)

The ability is on the aura permanent itself. `permanentIndex` points to the **aura**, not the enchanted creature.

```java
// Setup
Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears()); // index 0
Permanent auraPerm = new Permanent(new YourAura());
auraPerm.setAttachedTo(bearsPerm.getId());
gd.playerBattlefields.get(player1.getId()).add(auraPerm);           // index 1

harness.addMana(player1, ManaColor.COLORLESS, 1); // ability cost

// Activate on the AURA (index 1), no target needed
harness.activateAbility(player1, 1, null, null);
harness.passBothPriorities();

// Assert effect on enchanted creature
assertThat(bearsPerm.isTapped()).isTrue();
```

## Recipe: aura grants activated ability to creature

The creature gets the ability. `permanentIndex` points to the **creature**, not the aura.

```java
// Setup
Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears()); // index 0
Permanent auraPerm = new Permanent(new YourAura());
auraPerm.setAttachedTo(bearsPerm.getId());
gd.playerBattlefields.get(player1.getId()).add(auraPerm);           // index 1

// Activate on the CREATURE (index 0), with target
harness.activateAbility(player1, 0, null, targetId);
harness.passBothPriorities();
```

## Recipe: cast aura targeting a creature

```java
Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());
harness.setHand(player1, List.of(new YourAura()));
harness.addMana(player1, ManaColor.WHITE, TOTAL_CMC);

gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);
harness.passBothPriorities();

// Verify attached
assertThat(gd.playerBattlefields.get(player1.getId()))
        .anyMatch(p -> p.getCard().getName().equals("Your Aura")
                && p.isAttached()
                && p.getAttachedTo().equals(bearsPerm.getId()));
```

## Recipe: aura with upkeep MayEffect trigger + library manipulation

For auras like Call to the Kindred that have an upkeep "you may" trigger leading to a library search/choice interaction. This combines several patterns: aura setup, upkeep advancement, MayEffect handling, and library search interaction.

### Helper: set up aura attached to creature

```java
private void setupAuraOnCreature() {
    Permanent creature = new Permanent(new GrizzlyBears());
    creature.setSummoningSick(false);
    gd.playerBattlefields.get(player1.getId()).add(creature);

    Permanent auraPerm = new Permanent(new YourAura());
    auraPerm.setAttachedTo(creature.getId());
    gd.playerBattlefields.get(player1.getId()).add(auraPerm);
}

private void setupLibraryTopCards(List<Card> cards) {
    List<Card> deck = gd.playerDecks.get(player1.getId());
    deck.clear();
    deck.addAll(cards);
}

private void advanceToUpkeep(Player activePlayer) {
    harness.forceActivePlayer(activePlayer);
    harness.forceStep(TurnStep.UNTAP);
    harness.clearPriorityPassed();
    harness.passBothPriorities(); // advances to UPKEEP
}
```

### Test: upkeep prompts may ability

```java
@Test
void upkeepPromptsMayAbility() {
    setupAuraOnCreature();
    setupLibraryTopCards(List.of(new GrizzlyBears(), new LlanowarElves(), new Shock(), new Plains(), new Plains()));

    advanceToUpkeep(player1);
    harness.passBothPriorities(); // resolve MayEffect from stack

    assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
    assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
}
```

### Test: declining does nothing

```java
@Test
void decliningDoesNothing() {
    setupAuraOnCreature();
    Card topCard = new GrizzlyBears();
    setupLibraryTopCards(List.of(topCard, new LlanowarElves(), new Shock(), new Plains(), new Plains()));

    advanceToUpkeep(player1);
    harness.passBothPriorities();
    harness.handleMayAbilityChosen(player1, false);

    assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
}
```

### Test: accepting offers matching cards for library search

```java
@Test
void acceptingOffersMatchingCards() {
    setupAuraOnCreature();
    setupLibraryTopCards(List.of(new GrizzlyBears(), new LlanowarElves(), new Shock(), new Plains(), new Plains()));

    advanceToUpkeep(player1);
    harness.passBothPriorities();
    harness.handleMayAbilityChosen(player1, true);

    assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
    assertThat(gd.interaction.librarySearch().canFailToFind()).isTrue();
    // Assert only expected cards are offered
    assertThat(gd.interaction.librarySearch().cards()).hasSize(EXPECTED_COUNT);
}
```

### Test: choosing a card puts it onto the battlefield, then reorders rest

```java
@Test
void choosingPutsOnBattlefieldThenReorders() {
    setupAuraOnCreature();
    setupLibraryTopCards(List.of(new GrizzlyBears(), new LlanowarElves(), new Shock(), new Plains(), new Plains()));

    advanceToUpkeep(player1);
    harness.passBothPriorities();
    harness.handleMayAbilityChosen(player1, true);

    // Choose first matching card
    harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

    // Verify card on battlefield
    assertThat(gd.playerBattlefields.get(player1.getId()))
            .anyMatch(p -> p.getCard().getName().equals("Expected Card Name"));

    // Remaining cards in reorder phase
    assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REORDER);
    assertThat(gd.interaction.libraryView().reorderCards()).hasSize(REMAINING_COUNT);
}
```

### Test: declining to choose (index -1) reorders all

```java
@Test
void decliningToChooseReordersAll() {
    setupAuraOnCreature();
    setupLibraryTopCards(List.of(new GrizzlyBears(), new LlanowarElves(), new Shock(), new Plains(), new Plains()));

    advanceToUpkeep(player1);
    harness.passBothPriorities();
    harness.handleMayAbilityChosen(player1, true);

    int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();
    harness.getGameService().handleLibraryCardChosen(gd, player1, -1);

    assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore);
    assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REORDER);
}
```

Reference: `CallToTheKindredTest.java` — full working example.

## Reference tests

- `magical-vibes-backend/src/test/java/com/github/laxika/magicalvibes/cards/o/OrcishArtilleryTest.java`
- `magical-vibes-backend/src/test/java/com/github/laxika/magicalvibes/cards/s/ShockTest.java`
- `magical-vibes-backend/src/test/java/com/github/laxika/magicalvibes/cards/a/ArcaneTeachingsTest.java` — aura granting activated ability to creature
- `magical-vibes-backend/src/test/java/com/github/laxika/magicalvibes/cards/b/BurdenOfGuiltTest.java` — aura with own activated ability
- `magical-vibes-backend/src/test/java/com/github/laxika/magicalvibes/cards/c/CondemnTest.java`
- `magical-vibes-backend/src/test/java/com/github/laxika/magicalvibes/cards/t/TwincastTest.java`
- `magical-vibes-backend/src/test/java/com/github/laxika/magicalvibes/cards/s/SiegeGangCommanderTest.java`
- `magical-vibes-backend/src/test/java/com/github/laxika/magicalvibes/cards/b/BogardanFirefiendTest.java` — dies → targeted creature trigger
- `magical-vibes-backend/src/test/java/com/github/laxika/magicalvibes/cards/p/PerilousMyrTest.java` — dies → any-target trigger (creature or player)
- `magical-vibes-backend/src/test/java/com/github/laxika/magicalvibes/cards/f/FesteringGoblinTest.java` — dies → targeted creature trigger (-1/-1)
- `magical-vibes-backend/src/test/java/com/github/laxika/magicalvibes/cards/b/BurningOilTest.java` — flashback instant targeting attacking/blocking creatures
- `magical-vibes-backend/src/test/java/com/github/laxika/magicalvibes/cards/c/CallToTheKindredTest.java` — aura with upkeep MayEffect + library creature-sharing-type search

