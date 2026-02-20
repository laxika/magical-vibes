# TEST_RECIPES

Purpose: minimal reusable test patterns for new cards using `GameTestHarness`.

## Base skeleton

```java
private GameTestHarness harness;
private Player player1;
private Player player2;

@BeforeEach
void setUp() {
    harness = new GameTestHarness();
    player1 = harness.getPlayer1();
    player2 = harness.getPlayer2();
    harness.skipMulligan();
    harness.clearMessages();
}
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

- Use `handleMayAbilityChosen`, `handleCardChosen`, `handlePermanentChosen`, `handleColorChosen`, or graveyard/library helpers depending on awaiting input.
- After each choice, call `harness.passBothPriorities()` only if the stack is still pending.

## High-value regression checks per new card

1. Resolves with legal target(s).
2. Fizzles correctly when all targets become illegal (if targeted).
3. Costs are enforced (mana, tap, sacrifice, summoning sickness).
4. Zone transitions are correct (battlefield, graveyard, hand, library, exile).
5. No extra side effects occur when resolution is interrupted by input prompts.

## Reference tests

- `magical-vibes-backend/src/test/java/com/github/laxika/magicalvibes/cards/o/OrcishArtilleryTest.java`
- `magical-vibes-backend/src/test/java/com/github/laxika/magicalvibes/cards/s/ShockTest.java`
- `magical-vibes-backend/src/test/java/com/github/laxika/magicalvibes/cards/c/CondemnTest.java`
- `magical-vibes-backend/src/test/java/com/github/laxika/magicalvibes/cards/t/TwincastTest.java`
- `magical-vibes-backend/src/test/java/com/github/laxika/magicalvibes/cards/s/SiegeGangCommanderTest.java`

