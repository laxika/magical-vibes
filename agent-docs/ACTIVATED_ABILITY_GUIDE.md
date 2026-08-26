# ACTIVATED_ABILITY_GUIDE

Quick reference for building `ActivatedAbility` instances. Covers all constructor overloads, all parameters, and when to use each variant.

`ON_ALLY_SOURCE_DEALS_NONCOMBAT_DAMAGE_TO_OPPONENT` scans the damaging source controller's battlefield and fires only from the noncombat player-damage path. Chandra's Pyreling uses it with `SequenceEffect.of(BoostSelfEffect(1, 0), GrantKeywordEffect(DOUBLE_STRIKE, SELF))`.

## Fields reference

| Field | Type | Description |
|-------|------|-------------|
| `requiresTap` | `boolean` | `true` if the ability has {T} in its cost (tap as cost) |
| `requiresUntap` | `boolean` | `true` if the ability has {Q} in its cost (untap as cost); set via `.withRequiresUntap()`, never combined with `requiresTap` |
| `manaCost` | `String` | Mana cost string like `"{2}{B}"`, or `null` for no mana cost |
| `manaCostOfEnchantedPermanent` | `boolean` | Uses the mana cost of the permanent enchanted by this Aura; set via `.withManaCostOfEnchantedPermanent()` |
| `effects` | `List<CardEffect>` | Effects to resolve (costs first, then actual effects) |
| `description` | `String` | Rules text shown to the player (e.g. `"{T}: Draw a card."`) |
| `targetFilter` | `TargetFilter` | Restricts valid targets (permanent filter or stack filter) |
| `loyaltyCost` | `Integer` | Planeswalker loyalty cost (e.g. `+1`, `-2`, `-8`). `null` for non-planeswalker abilities. `0` when `variableLoyaltyCost` is `true` |
| `variableLoyaltyCost` | `boolean` | `true` for -X loyalty abilities where X is chosen by the player. The chosen X is passed as xValue |
| `maxActivationsPerTurn` | `Integer` | Maximum activations per turn (printed fixed number). `null` for unlimited |
| `maxActivationsPerTurnAmount` | `DynamicAmount` | Board-derived per-turn cap for "Activate no more times each turn than [count]" (Withering Wisps). Re-evaluated at every activation. Set via `.withMaxActivationsPerTurn(amount, description)`; `null` for no dynamic cap |
| `timingRestriction` | `ActivationTimingRestriction` | When the ability can be activated. `null` for default (instant speed) |
| `requiredControlledSubtype` | `CardSubtype` | Subtype you must control N+ of to activate (e.g. `CardSubtype.VAMPIRE`). `null` for no restriction |
| `requiredControlledSubtypeCount` | `int` | Minimum count of `requiredControlledSubtype` permanents you must control. `0` when unused |

**Targeting is computed from effects** — `ActivatedAbility.isNeedsTarget()` and `isNeedsSpellTarget()` are derived getters, never stored as fields. For Cards, use `EffectResolution.needsTarget(card)` / `EffectResolution.needsSpellTarget(card)` instead. Override `targetSpec()` on your effect record to return a non-NONE `TargetSpec` (category + `harmful` flag + optional predicate) — see `EFFECTS_INDEX.md`.

### ActivationTimingRestriction values

| Value | Use when |
|-------|----------|
| `SORCERY_SPEED` | Equip abilities, sorcery-speed activated abilities |
| `ONLY_DURING_YOUR_TURN` | Activate only during your turn (any phase/step, instant speed) |
| `ONLY_DURING_YOUR_UPKEEP` | Abilities that can only be used during your upkeep |
| `ONLY_DURING_ANY_UPKEEP` | Abilities usable during any player's upkeep step (only checks `currentStep == UPKEEP`, not the active player). Pair with `.withActivatableByAnyPlayer()` for "any player may activate this ability but only during any upkeep step" (Infinite Hourglass) |
| `ONLY_DURING_YOUR_DRAW_STEP` | Abilities usable only during the activating player's draw step (`currentStep == DRAW` and activator == active player). Pair with `.withActivatableByAnyPlayer()` for "any player may activate this ability but only during their draw step" (Well of Knowledge) |
| `ONLY_DURING_OPPONENTS_UPKEEP` | Abilities usable only during an upkeep step of a turn whose active player is not the activating player (`currentStep == UPKEEP` **and** activator != active player). Trade Caravan |
| `ONLY_DURING_OPPONENTS_TURN` | Activate only during a turn whose active player is not the activating player (`activator != gameData.activePlayerId`). Ghost Town's "Activate only if it's not your turn." |
| `ONLY_DURING_OPPONENTS_TURN_BEFORE_COMBAT` | Abilities usable only during a turn whose active player is not the activating player **and** only in a step before the combat phase (`TurnStep.isBeforeCombat()` — untap/upkeep/draw/precombat main). Maddening Imp |
| `ONLY_WHILE_ATTACKING` | Activate only if this creature is attacking (checks `permanent.isAttacking()`) |
| `ONLY_WHILE_ATTACKING_OR_BLOCKING` | Activate only if this creature is attacking or blocking (checks `permanent.isAttacking() \|\| permanent.isBlocking()`). Sawback Manticore |
| `ONLY_BEFORE_ATTACKERS_DECLARED` | Activate only during your turn, before attackers are declared (active player + step before `DECLARE_ATTACKERS`). Stern Marshal |
| `BEFORE_ATTACKERS_DECLARED` | Activate only before attackers are declared (any player's turn). Also requires `combatPhasesThisTurn <= 1` so a turn with multiple combats only allows activation before the first declare-attackers step. Norritt |
| `BEFORE_BLOCKERS_DECLARED` | Activate only before blockers are declared (any player's turn). Steps before `DECLARE_BLOCKERS`, plus `combatPhasesThisTurn <= 1` so a turn with multiple combats only allows activation before the first declare-blockers step. Acidic Dagger |
| `ONLY_BEFORE_END_OF_COMBAT` | Activate only during a step that precedes the end of combat step, on any player's turn (`gameData.currentStep.isBeforeEndOfCombat()`). Dwarven Sea Clan |
| `ONLY_DURING_COMBAT` | Activate only during the combat phase (checks `gameData.currentStep.isCombatPhase()`). Jade Statue |
| `ONLY_DURING_END_OF_COMBAT` | Activate only during the end of combat step (`currentStep == END_OF_COMBAT`). Desert |
| `ONLY_DURING_DECLARE_ATTACKERS_IF_ATTACKED` | Activate only during the declare attackers step and only if you've been attacked this step (a creature is attacking you or a planeswalker you control). Kongming's Contraptions. Checks `gameData.currentStep == DECLARE_ATTACKERS` + `gameQueryService.isPlayerBeingAttacked(gd, playerId)` |
| `ONLY_DURING_DECLARE_BLOCKERS` | Activate only during the declare blockers step (`currentStep == DECLARE_BLOCKERS`). General Jarkeld |
| `ONLY_DURING_DECLARE_BLOCKERS_IF_BLOCKED` | Activate only during the declare blockers step and only if at least one creature is blocking this creature (`gameQueryService.isBlockedByAnyCreature`). Grizzled Wolverine |
| `ONLY_WHILE_CREATURE` | Abilities on creature lands that only work while animated |
| `CAST_NONCREATURE_SPELL_THIS_TURN` | Activate only if you've cast a noncreature spell this turn (checks `gameQueryService.playerCastNoncreatureSpellThisTurn()`). Seeker of Insight |
| `METALCRAFT` | Activate only if you control three or more artifacts |
| `COVEN` | Activate only if you control three or more creatures with different powers (checks distinct effective powers via `gameQueryService.isCovenMet()`). Ambitious Farmhand |
| `MORBID` | Activate only if a creature died this turn (checks `gameQueryService.isMorbidMet()`) |
| `OPPONENT_CONTROLS_FLYING_CREATURE` | Activate only if an opponent controls a creature with flying (checks `gameQueryService.anyOpponentControlsFlyingCreature()`). Groundling Pouncer |
| `OPPONENT_CONTROLS_MORE_LANDS` | Activate only if an opponent controls strictly more lands than you (checks `gameQueryService.anyOpponentControlsMoreLands()`). Weathered Wayfarer |
| `POWER_4_OR_GREATER` | Activate only if this creature's power is 4 or greater (checks effective power incl. static bonuses) |
| `RAID` | Activate only if you attacked this turn (checks `playersDeclaredAttackersThisTurn`). Works with both battlefield and graveyard activated abilities |

---

## Constructor quick-pick guide

### 1. Basic ability (most common)

```java
new ActivatedAbility(requiresTap, manaCost, effects, description)
```

**Use when:** Simple tap ability, mana ability, pump, or any ability with no target restrictions.

```java
// Tap to deal damage to any target
new ActivatedAbility(true, null, List.of(new DealDamageToAnyTargetEffect(3)),
    "{T}: Kamahl, Pit Fighter deals 3 damage to any target.")

// Pay mana to pump self
new ActivatedAbility(false, "{R}", List.of(new BoostSelfEffect(1, 0)),
    "{R}: Furnace Whelp gets +1/+0 until end of turn.")

// Tap + mana to mill
new ActivatedAbility(true, "{2}", List.of(new MillEffect(2, MillRecipient.TARGET_PLAYER)),
    "{2}, {T}: Target player mills two cards.")
```

Cards: `KamahlPitFighter`, `FurnaceWhelp`, `Millstone`, `ProdigalPyromancer`, `ArcanisTheOmnipotent`

---

### 2. Ability with target filter

```java
new ActivatedAbility(requiresTap, manaCost, effects, description, targetFilter)
```

**Use when:** Ability targets a permanent but only specific ones (e.g. "target creature with power 2 or less", "target blue or red creature").

```java
// Target creature with power 2 or less
new ActivatedAbility(true, null, List.of(new MakeCreatureUnblockableEffect()),
    "{T}: Target creature with power 2 or less can't be blocked this turn.",
    new PermanentPredicateTargetFilter(
        new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentPowerAtMostPredicate(2)
        )),
        "Target creature's power must be 2 or less"
    ))

// Target blue or red creature
new ActivatedAbility(false, "{2}", List.of(new BoostTargetCreatureEffect(1, 0)),
    "{2}: Target blue or red creature gets +1/+0 until end of turn.",
    new PermanentPredicateTargetFilter(
        new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentColorInPredicate(Set.of(CardColor.BLUE, CardColor.RED))
        )),
        "Target must be a blue or red creature"
    ))
```

Cards: `CraftyPathmage`, `HateWeaver`, `FemerefArchers`, `IcyManipulator`

---

### 3. Ability with max activations per turn

```java
new ActivatedAbility(requiresTap, manaCost, effects, description, maxActivationsPerTurn)
```

**Use when:** Ability text says "Activate only once each turn" or similar.

```java
// Activate only once per turn
new ActivatedAbility(false, "{2}", List.of(new BoostSelfEffect(2, 2)),
    "{2}: This creature gets +2/+2 until end of turn. Activate only once each turn.", 1)
```

**Note:** This overload has the same parameter types as the targetFilter variant (`String` for description, then `Integer` vs `TargetFilter`), so the compiler resolves them by type. Use this when you need a per-turn limit but no target filter.

---

### 3b. Ability with max activations per *game* (`.withMaxActivationsPerGame`)

```java
new ActivatedAbility(...).withMaxActivationsPerGame(1)
```

**Use when:** Ability text says "Activate only once" (no "each turn") — the cap spans the whole game, not the turn.

```java
// {1}{R}: This creature gets +2/+0 and gains flying. … Activate only once and only if you control a snow Mountain.
new ActivatedAbility(false, "{1}{R}",
        List.of(new BoostSelfEffect(2, 0), new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF),
                new SacrificeSelfAtEndStepEffect()),
        "…")
    .withMaxActivationsPerGame(1)
    .withRequiredControlledPermanents(new PermanentAllOfPredicate(List.of(
            new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN),
            new PermanentHasSupertypePredicate(CardSupertype.SNOW))), 1, "snow Mountains")
```

Counted in `GameData.activatedAbilityUsesThisGame` (permanent id → ability index → count), which is never cleared at turn cleanup — only by Karn's game restart. Because the count is keyed by permanent id, a permanent that leaves and re-enters the battlefield is a new object and may activate again (CR 400.7). Goblin Ski Patrol.

---

### 3c. Exhaust ability (`.withExhaust()`)

Exhaust is a whole-game, once-per-permanent activated ability. Mark the ability separately from
the activation cap so cards with "whenever you activate an exhaust ability" can distinguish it
from ordinary abilities.

```java
new ActivatedAbility(false, "{4}", List.of(effect),
        "Exhaust — {4}: [effect] (Activate each exhaust ability only once.)")
    .withMaxActivationsPerGame(1)
    .withExhaust()
```

Use `EffectSlot.ON_CONTROLLER_ACTIVATES_EXHAUST_ABILITY` for a trigger that watches the
controller's exhaust activations. The trigger is collected before the activated ability resolves.
For a trigger that functions while its source card is in the graveyard and returns that card from
there (Afterburner Expert), use `EffectSlot.GRAVEYARD_ON_CONTROLLER_ACTIVATES_EXHAUST_ABILITY`;
the graveyard collector scans every card in the activating player's graveyard.

---

### 3c. Modal ability ("{cost}: Choose one …")

Put a raw `ChooseOneEffect` in the ability's effect list. Unlike a modal *spell* (mode unwrapped at cast time in `SpellCastingService`), an activated ability keeps the `ChooseOneEffect` on the stack and `ChooseOneEffectHandler` prompts for the mode as the ability resolves. A mode may hold several effects via `ChooseOneOption(label, List.of(...))`.

```java
// {G}: Choose one. Activate only once each turn.
addActivatedAbility(new ActivatedAbility(false, "{G}", List.of(new ChooseOneEffect(List.of(
        new ChooseOneEffect.ChooseOneOption(RHINO_MODE, List.of(
                new SourceBecomesSubtypeUntilEndOfTurnEffect(CardSubtype.RHINO),
                new SetBasePowerToughnessEffect(4, 4, GrantScope.SELF),
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.SELF))),
        …))),
        "{G}: Choose one. Activate only once each turn.", 1));
```

**Limitation:** the per-mode `targetFilter` is only wired into the cast path, so modes must be non-targeting. Skinshifter. When the modes need *different* targets, split the ability into one `ActivatedAbility` per mode sharing the same cost — a mode is chosen while the ability is activated (CR 601.2b via CR 602.2b), at the same time as its targets, so this is behaviourally identical and each mode can declare its own target filter (Disciple of the Ring). When every mode applies to the *same* target creature, use `ChooseOneForTargetCreatureEffect(options)` instead — it declares a benign `CREATURE` target spec, so the target is chosen at activation and the mode still at resolution (Nature's Blessing). Tests answer the prompt with `harness.handleListChoice(player, modeLabel)`.

---

### 4. Ability with timing restriction

```java
new ActivatedAbility(requiresTap, manaCost, effects, description, timingRestriction)
```

**Use when:** The ability can only be activated at specific times.

```java
// Regenerate, only while this land is animated as a creature
new ActivatedAbility(false, "{B}", List.of(new RegenerateEffect()),
    "{B}: Regenerate this creature.",
    ActivationTimingRestriction.ONLY_WHILE_CREATURE)

// Sorcery-speed sacrifice ability
new ActivatedAbility(false, null,
    List.of(new SacrificeSelfCost(), new ChooseCardsFromTargetHandEffect(1, List.of(), HandChoiceDestination.DISCARD)),
    "Sacrifice: Target player reveals their hand...",
    ActivationTimingRestriction.SORCERY_SPEED)
```

Cards: `SpawningPool` (ONLY_WHILE_CREATURE), `ThrullSurgeon` (SORCERY_SPEED), `ColossusOfSardia` (ONLY_DURING_YOUR_UPKEEP), `SkyshroudRanger` (SORCERY_SPEED), `VonaButcherOfMagan` (ONLY_DURING_YOUR_TURN)

**Note:** For abilities that target spells on the stack (e.g. activated counter ability), spell targeting is auto-derived from effects (e.g. `CounterUnlessPaysEffect.targetSpec()` returns `benign(SPELL_ON_STACK)`). Use a `StackEntryPredicateTargetFilter` if target legality is restricted.

---

### 5. Loyalty ability (planeswalkers)

```java
new ActivatedAbility(loyaltyCost, effects, description)
```

**Use when:** Planeswalker loyalty ability with no target restrictions.

```java
// +1: Create a token
new ActivatedAbility(+1, List.of(new CreateTokenEffect("Kithkin", 1, 1, CardColor.WHITE, Set.of(CardColor.GREEN, CardColor.WHITE), List.of(CardSubtype.KITHKIN))),
    "+1: Create a 1/1 green and white Kithkin creature token.")

// -8: Ultimate
new ActivatedAbility(-8, List.of(new AjaniUltimateEffect()),
    "\u22128: Look at the top X cards...")
```

Cards: `AjaniOutlandChaperone`

---

### 6. Loyalty ability with target filter

```java
new ActivatedAbility(loyaltyCost, effects, description, targetFilter)
```

**Use when:** Planeswalker loyalty ability that targets a specific permanent.

```java
// -2: Deal 4 damage to target tapped creature
new ActivatedAbility(-2, List.of(new DealDamageToTargetCreatureEffect(4)),
    "\u22122: Ajani deals 4 damage to target tapped creature.",
    new PermanentPredicateTargetFilter(
        new PermanentIsTappedPredicate(),
        "Target must be a tapped creature"
    ))
```

Cards: `AjaniOutlandChaperone`

---

### 7. Variable loyalty ability (-X)

```java
ActivatedAbility.variableLoyaltyAbility(effects, description, targetFilter)
```

**Use when:** Planeswalker -X loyalty ability where the player chooses X (e.g. "−X: Deal X damage to target creature"). The chosen X is stored as `xValue` on the stack entry and also used as the loyalty cost (removing X counters).

```java
// −X: Chandra Nalaar deals X damage to target creature.
ActivatedAbility.variableLoyaltyAbility(
    List.of(new DealDamageToTargetCreatureEffect(new XValue())),
    "\u2212X: Chandra Nalaar deals X damage to target creature.",
    null)
```

Cards: `ChandraNalaar`

---

### 8. Ability with subtype count restriction

```java
new ActivatedAbility(requiresTap, manaCost, effects, description, requiredControlledSubtype, requiredControlledSubtypeCount)
```

**Use when:** Ability text says "Activate only if you control N or more [subtype]" (e.g. "Activate only if you control five or more Vampires").

```java
// {B}: Transform Bloodline Keeper. Activate only if you control five or more Vampires.
new ActivatedAbility(false, "{B}",
    List.of(new TransformSelfEffect()),
    "{B}: Transform Bloodline Keeper. Activate only if you control five or more Vampires.",
    CardSubtype.VAMPIRE, 5)
```

Cards: `BloodlineKeeper`

---

### 8a. Ability with predicate-count restriction (`.withRequiredControlledPermanents`)

```java
new ActivatedAbility(requiresTap, manaCost, effects, description)
    .withRequiredControlledPermanents(PermanentPredicate predicate, int count, String description)
```

**Use when:** Ability text says "Activate only if you control N or more [permanents matching a predicate]" where the filter is a **color/type/etc.** rather than a creature subtype (use section 8 for subtypes). `description` is the plural noun phrase spliced into the error message ("Activate only if you control N or more <description>"). Validated in `AbilityActivationService` via `gameQueryService.countControlledPermanentsMatching`.

```java
// {B}, {T}: Each opponent loses 1 life. Activate only if you control two or more black permanents.
new ActivatedAbility(true, "{B}",
    List.of(new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT)),
    "{B}, {T}: Each opponent loses 1 life. Activate only if you control two or more black permanents.")
    .withRequiredControlledPermanents(new PermanentColorInPredicate(Set.of(CardColor.BLACK)), 2, "black permanents")
```

Cards: `LeechriddenSwamp`

---

### 8a-counters. Ability gated on counters on the source (`.withRequiredSourceCounters`)

```java
new ActivatedAbility(requiresTap, manaCost, effects, description)
    .withRequiredSourceCounters(CounterType counterType, int count)
```

**Use when:** Ability text says "Activate only if there are N or more [type] counters on this permanent." Checked against the **source permanent itself** (`permanent.getCounterCount(type) >= count`) in `AbilityActivationService.validateTimingRestrictions` — source-exact, unlike the board-wide `.withRequiredControlledPermanents`. Does NOT remove the counters (that would be `RemoveCounterFromSourceCost`).

```java
// {1}, {T}: <detain effect>. Activate only if there are three or more brick counters on this artifact.
new ActivatedAbility(true, "{1}",
    List.of(new LockTargetPermanentEffect(true, true, true, EffectDuration.UNTIL_YOUR_NEXT_TURN)),
    "{1}, {T}: Until your next turn, ...")
    .withRequiredSourceCounters(CounterType.BRICK, 3)
```

Cards: `EdificeOfAuthority`

---

### 8a-graveyard. Ability gated on cards in the controller's graveyard (`.withRequiredGraveyardCards`)

```java
new ActivatedAbility(requiresTap, manaCost, effects, description)
    .withRequiredGraveyardCards(CardPredicate predicate, int count, String description)
```

**Use when:** Ability text says "Activate only if there are N or more [matching] cards in your graveyard" (e.g. Gate to the Afterlife's "six or more creature cards in your graveyard"). Counts only **non-token** cards in the controller's own graveyard matching the `CardPredicate`, in `AbilityActivationService.validateTimingRestrictions`. `description` is the noun phrase spliced into the error ("Activate only if there are N or more <description>").

```java
// {2}, {T}, Sacrifice this artifact: <tutor>. Activate only if there are six or more creature cards in your graveyard.
new ActivatedAbility(true, "{2}",
    List.of(new SacrificeSelfCost(), new SearchZonesForCardNamedToBattlefieldEffect("God-Pharaoh's Gift")),
    "{2}, {T}, Sacrifice Gate to the Afterlife: Search your graveyard, hand, and/or library for a card named God-Pharaoh's Gift and put it onto the battlefield. ...")
    .withRequiredGraveyardCards(new CardTypePredicate(CardType.CREATURE), 6, "creature cards in your graveyard")
```

Cards: `GateToTheAfterlife`

---

### 8a-condition. Ability gated on an arbitrary Condition (`.withActivationCondition`)

```java
new ActivatedAbility(requiresTap, manaCost, effects, description)
    .withActivationCondition(Condition condition, String description)
```

**Use when:** Ability text says "Activate only if …" and the gate is a **compound** condition the typed helpers above cannot express alone (notably OR of board + graveyard Desert). Reuses the sealed `Condition` hierarchy / `ConditionEvaluationService` (same `AnyOf` + `ControlsPermanent` + `GraveyardCardThreshold` pattern as Sidewinder Naga / Desert's Hold). `description` is the full error message thrown when the condition is not met. Prefer `.withRequiredControlledPermanents` / `.withRequiredGraveyardCards` / timing enums when they cover the oracle text by themselves.

```java
// {T}: deals 1 damage to target player or planeswalker.
// Activate only if you control a Desert or there is a Desert card in your graveyard.
new ActivatedAbility(true, null,
    List.of(new DealDamageToTargetPlayerOrPlaneswalkerEffect(1)),
    "{T}: … Activate only if you control a Desert or there is a Desert card in your graveyard.")
    .withActivationCondition(
        new AnyOf(List.of(
            new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.DESERT)),
            new GraveyardCardThreshold(1, new CardSubtypePredicate(CardSubtype.DESERT)))),
        "Activate only if you control a Desert or there is a Desert card in your graveyard")
```

Cards: `WallOfForgottenPharaohs`

For a turn-scoped OR gate that checks either an oil counter removal from a permanent the
activating player controlled or an oil-bearing permanent entering a graveyard, use
`OilCounterEventThisTurn`. The engine records both event forms centrally and clears them at turn
progression; `ChurningReservoir` is the reference implementation.

For an Aura ability gated on what it is attached to ("Activate only if enchanted creature is white"), use
`EnchantedPermanentMatches(PermanentPredicate filter, String description)` — false unless the source is an
attached Aura whose host matches the predicate. Nature's Chosen:
`new EnchantedPermanentMatches(new PermanentColorInPredicate(Set.of(CardColor.WHITE)), "enchanted creature is white")`.

Also used for graveyard activated abilities whose gate needs the source card itself (e.g. Ashen Ghoul's
`CardsAboveSelfInGraveyard(3, CardTypePredicate(CREATURE))` with `ONLY_DURING_YOUR_UPKEEP`). Evaluated in
`validateGraveyardTimingRestrictions` via `ConditionContext.forCard`.

Cards: `AshenGhoul`

---

### 8a-dynamic-limit. Per-turn activation cap computed from the board (`.withMaxActivationsPerTurn`)

```java
new ActivatedAbility(requiresTap, manaCost, effects, description)
    .withMaxActivationsPerTurn(DynamicAmount amount, String description)
```

**Use when:** Ability text says "Activate no more times each turn than [some count]" — the cap is not a
printed number, so the `Integer maxActivationsPerTurn` constructor parameter can't express it. The amount is
re-evaluated from the current game state at **every** activation (`AmountEvaluationService` with the source
permanent + its controller in context), so losing the counted permanents mid-turn lowers the cap immediately.
`description` is spliced into the activation error message ("… no more times each turn than <description> (N)").
A cap of 0 blocks activation entirely. Composes with the fixed `maxActivationsPerTurn` (both are enforced).

```java
// {B}: This enchantment deals 1 damage to each creature and each player.
// Activate no more times each turn than the number of snow Swamps you control.
new ActivatedAbility(false, "{B}", List.of(new MassDamageEffect(1, true)), "{B}: …")
    .withMaxActivationsPerTurn(
        new PermanentCount(new PermanentAllOfPredicate(List.of(
                new PermanentHasSubtypePredicate(CardSubtype.SWAMP),
                new PermanentHasSupertypePredicate(CardSupertype.SNOW))), CountScope.CONTROLLER),
        "the number of snow Swamps you control")
```

Cards: `WitheringWisps`

---

### 8b. Ability any player may activate

```java
new ActivatedAbility(false, null, effects, description).withActivatableByAnyPlayer()
```

**Use when:** Ability text says "Any player may activate this ability" (e.g. Oona's Prowler).
The `.withActivatableByAnyPlayer()` fluent flag lets a player who does **not** control the
source activate it; that activating player pays the costs (mana/discard/etc.) from their own
resources, while the effect still resolves against the source permanent (e.g. `BoostSelfEffect`
applies to the source regardless of who activated). Resolution finds the source across all
battlefields when the activator isn't the controller.

```java
// Discard a card: Oona's Prowler gets -2/-0 until end of turn. Any player may activate this ability.
new ActivatedAbility(false, null,
    List.of(new DiscardCardTypeCost(null, null), new BoostSelfEffect(-2, 0)),
    "Discard a card: Oona's Prowler gets -2/-0 until end of turn. Any player may activate this ability.")
    .withActivatableByAnyPlayer()
```

Cards: `OonasProwler`, `Mercenaries`, `ArmageddonClock`, `InfiniteHourglass`, `AetherStorm`, `FeralHydra`

Chain `.withActivatableOnlyByEnchantedPermanentController()` on top of the any-player flag for an
Aura ability only the enchanted permanent's controller may activate ("That creature's controller may
sacrifice a permanent…", Volrath's Curse). The any-player flag makes the Aura reachable from a
battlefield the activator doesn't control; the second flag rejects every player except the enchanted
permanent's controller (`AbilityActivationService.validateActivationLegality`).

```java
// Sacrifice a permanent: the enchanted creature's controller ignores this Aura until end of turn.
new ActivatedAbility(false, null,
    List.of(new SacrificePermanentCost(new PermanentTruePredicate(), "a permanent"),
            new IgnoreSourceAuraEffectsUntilEndOfTurnEffect()), description)
    .withActivatableByAnyPlayer()
    .withActivatableOnlyByEnchantedPermanentController()
```

Cards: `VolrathsCurse`

Chain `.withActivatableOnlyByOpponents()` instead for "Only your opponents may activate this
ability" (Soul Ransom) — same two-flag shape, but the second flag rejects only the source
permanent's own controller. Pair it with an effect that acts on the source's controller rather than
the entry's controller (`SacrificeSelfAndControllerDrawsEffect`), since the activator is the opponent.

```java
// Discard two cards: this Aura's controller sacrifices it, then draws two cards.
new ActivatedAbility(false, null,
    List.of(new DiscardCardTypeCost(null, null, 2),
            new SacrificeSelfAndControllerDrawsEffect(2)), description)
    .withActivatableByAnyPlayer()
    .withActivatableOnlyByOpponents()
```

Cards: `SoulRansom`

---

### 8b. Untap-symbol cost `{Q}` (`.withRequiresUntap()`)

```java
new ActivatedAbility(false, "{1}{W}{W}", effects, description).withRequiresUntap()
```

**Use when:** the ability's cost includes the untap symbol `{Q}` (e.g. Order of Whiteclay).
Pass `requiresTap = false` (never combine `{T}` and `{Q}`) and chain `.withRequiresUntap()`.
The source must be **tapped** to activate; paying the cost **untaps** it. Creatures obey the
same summoning-sickness restriction as `{T}` (CR 302.6). No enchanted-permanent-tap triggers
fire (untapping, not tapping).

Cards: `OrderOfWhiteclay`

---

### 9. Equipment ability (equip with sorcery speed + controlled creature filter)

```java
new EquipActivatedAbility(manaCost)
```

**Use when:** Any equipment card with a mana-cost-only equip ability. `EquipActivatedAbility` extends `ActivatedAbility` and wires up `EquipEffect`, sorcery-speed timing, and the "target creature you control" filter automatically — only the mana cost varies.

Cards: `LoxodonWarhammer` ({3}), `LeoninScimitar` ({1}), `BarkOfDoran` ({1}), `WhispersilkCloak` ({2})

For an ability granted by an Equipment that uses "Unattach [this Equipment]" as a cost, use
`new UnattachSourceEquipmentCost()` before the ability's resolving effects. The activation flow
detaches the granting Equipment and leaves it on the battlefield.

For "this Equipment can be attached only to …" use the three-argument overload
`new EquipActivatedAbility(manaCost, restrictionPredicate, failureMessage)`, which ANDs the
predicate onto the "creature you control" filter, **and** call `setAttachRestriction(predicate)` on
the card so the same requirement is enforced continuously — an Equipment whose host stops matching
becomes unattached as a state-based action (CR 704.5n, `AuraAttachmentService`). Konda's Banner
(legendary creature only).

---

### 10. Full constructor (all parameters)

```java
new ActivatedAbility(requiresTap, manaCost, effects,
    description, targetFilter, loyaltyCost, maxActivationsPerTurn, timingRestriction)
```

**Use when:** None of the simpler overloads fit. Pass `null` for unused optional parameters.

---

### 11. Multi-target ability constructor

```java
new ActivatedAbility(requiresTap, manaCost, effects, description,
    multiTargetFilters, minTargets, maxTargets)
```

**Use when:** The ability targets multiple permanents or players. Each position in `multiTargetFilters` (a `List<TargetFilter>`) constrains the corresponding target selection. Use `PlayerPredicateTargetFilter` for player-targeting positions or permanent filters for permanent-targeting positions. `minTargets` and `maxTargets` define the required count. The frontend enters multi-target selection mode when `maxTargets > 1`. Targets are passed via `StackEntry.getTargetIds()`.

Cards: `BrassSquire` (2 targets: Equipment + creature), `SoulConduit` (2 targets: player + player)

**Filterless "any target" group** ("−6: deals 6 damage to each of up to six targets" — Chandra, the Firebrand): pass `multiTargetFilters = List.of()` with `minTargets 0` / `maxTargets 6` on the full ctor. With no global and no per-position filter, `ValidTargetService`/`TargetLegalityService` derive the slot type from the effects' `targetSpec()`: when every permanent-targeting effect also targets players the slot is "any target", so players are offered alongside creatures and planeswalkers and every other permanent type is rejected. A filterless group is routed onto the multi-target path by `maxTargets > 1` even though `isMultiTarget()` (per-position filters) is false — X-scaled groups stay on the single-target path.

**X-scaled target count** ("{X}, {T}, Sacrifice this artifact: X target creatures with power 2 or less can't be blocked this turn" — Runed Arch): use the full ctor with a single `targetFilter`, empty `multiTargetFilters`, `minTargets = 0` and a sanity `maxTargets` cap, then chain `.withXScaledTargets()`. This is the ability-side counterpart of `Card.targetX`: the paid X bounds the target count via `ActivatedAbility.getEffectiveMinTargets/MaxTargets(x)`, honoured by `TargetLegalityService.validateMultiTargetAbility(..., xValue)` and `ValidTargetService.computeValidTargetsForAbility(..., xValue)`. Per-position filtering falls back to the ability's single `targetFilter`, and the chosen group rides on `StackEntry.getTargetIds()` (so any handler that fans over `getTargetIds()` — e.g. `MakeCreatureUnblockableEffect` — works unchanged).

```java
addActivatedAbility(new ActivatedAbility(true, "{X}",
        List.of(new SacrificeSelfCost(), new MakeCreatureUnblockableEffect()),
        "{X}, {T}, Sacrifice this artifact: X target creatures with power 2 or less can't be blocked this turn.",
        creatureWithPowerAtMost2Filter, null, null, null, List.of(), 0, 100)
        .withXScaledTargets());
```

**Minimum X:** chain `.withMinimumXValue(n)` when an activated ability says that X cannot be
zero or has another printed lower bound. The activation legality check and dry-run availability
query both use this bound; the chosen value still flows to the stack entry as normal.

Test harness: `activateAbilityWithMultiTargets(player, permanentIndex, abilityIndex, xValue, targetIds)`.

---

### 12. Graveyard activated ability

```java
addGraveyardActivatedAbility(new ActivatedAbility(requiresTap, manaCost, effects, description))
```

**Use when:** The card has an activated ability that can be used while it is in the graveyard (e.g. "{3}{R}{R}: Return ~ from your graveyard to your hand."). These are distinct from `GRAVEYARD_UPKEEP_TRIGGERED` triggered abilities — graveyard activated abilities can be activated at instant speed whenever the player has priority, not just during upkeep.

- Uses `Card.addGraveyardActivatedAbility()` instead of `addActivatedAbility()`
- The ability is exposed via `Card.getGraveyardActivatedAbilities()`
- Activated from graveyard via `AbilityActivationService.activateGraveyardAbility()`
- Blocked by Pithing Needle (checks `ActivatedAbilitiesOfChosenNameCantBeActivatedEffect`)
- Creates a `StackEntry` with `StackEntryType.ACTIVATED_ABILITY` using the Card reference (no source permanent)
- Frontend sends `ACTIVATE_GRAVEYARD_ABILITY` message with `graveyardCardIndex` and `abilityIndex`
- `CardView` includes `graveyardActivatedAbilities` list for frontend rendering

```java
// {3}{R}{R}: Return Magma Phoenix from your graveyard to your hand.
addGraveyardActivatedAbility(new ActivatedAbility(
    false, "{3}{R}{R}",
    List.of(ReturnCardFromGraveyardEffect.builder()
        .destination(GraveyardChoiceDestination.HAND)
        .filter(new CardIsSelfPredicate())
        .returnAll(true)
        .build()),
    "{3}{R}{R}: Return Magma Phoenix from your graveyard to your hand."));
```

Cards: `MagmaPhoenix`

**Targeted graveyard abilities** (e.g. Gryff's Boon "{cost}: Return this card … attached to target creature") use an effect with a non-`NONE` `targetSpec()` plus an optional `TargetFilter` on the `ActivatedAbility`. Activation accepts `targetId` via `activateGraveyardAbility(..., xValue, targetId)` / wire `ActivateGraveyardAbilityRequest.targetId`. The stack entry carries the target and `ability.getTargetFilter()` so illegal targets fizzle on resolution.

**Graveyard abilities targeting graveyard cards** (Soul of Innistrad "{3}{B}{B}, Exile this card from your graveyard: Return up to three target creature cards from your graveyard to your hand") pass a list of card ids: `activateGraveyardAbility(..., xValue, targetId, graveyardTargetIds)` / `ActivateGraveyardAbilityRequest.graveyardCardIds` / harness `activateGraveyardAbilityWithGraveyardTargets(player, graveyardCardIndex, abilityIndex, ids)`. The ids are validated by `TargetLegalityService.validateMultiTargetGraveyardAbility` **before** any cost is paid (CR 601.2c), so a self-exiling ability may legally target its own source card — that target is simply gone by resolution. They ride on the stack entry as `targetCardIds` with `Zone.GRAVEYARD`, the same shape the battlefield path uses (`activateAbility(..., Zone.GRAVEYARD, targetIds)`).

**Multi-target graveyard abilities** (Soul of Shandalar "{3}{R}{R}, Exile this card from your graveyard: It deals 3 damage to target player or planeswalker and 3 damage to up to one target creature that player … controls") declare `multiTargetFilters` + `minTargets`/`maxTargets` on the graveyard ability exactly as a battlefield ability would, `MultiTargetConstraint` included. `AbilityActivationService` detects them (`isMultiTarget() || maxTargets > 1`) and reuses the same id-list parameter for the announced targets: they go through `TargetLegalityService.validateMultiTargetAbility` before any cost is paid and land in the stack entry's flat `targetIds` (no `Zone.GRAVEYARD`), so ordinary multi-target handlers resolve them unchanged. Harness: `activateGraveyardAbilityWithTargets(player, graveyardCardIndex, abilityIndex, targetIds)`. Wire: the ids reuse `ActivateGraveyardAbilityRequest.graveyardCardIds`; the frontend enumerates each position via `ValidTargetsRequest.graveyardCardIndex` + `abilityIndex`.

```java
addGraveyardActivatedAbility(new ActivatedAbility(
    false, "{3}{W}",
    List.of(new ReturnSourceFromGraveyardAttachedToTargetEffect()),
    "{3}{W}: Return this card … attached to target creature. Activate only as a sorcery.",
    TargetFilters.creature(),
    null, null, ActivationTimingRestriction.SORCERY_SPEED));
```

Cards: `GryffsBoon`

**Embalm / Eternalize** ("{cost}, Exile this card from your graveyard: Create a token that's a copy of it, except ... Activate only as a sorcery.") is a graveyard activated ability whose cost exiles the source card. Use `ExileSelfFromGraveyardCost()` (paid at activation, before the ability hits the stack, so it can't be activated twice off the same card) plus `CreateTokenCopyOfSourceEffect(false, 1, colorOverride, addedSubtype, removeManaCost)` for the transformed copy, and `ActivationTimingRestriction.SORCERY_SPEED` (now enforced for graveyard abilities by `validateGraveyardTimingRestrictions`).

Plain embalm and eternalize are `addEmbalm` / `addEternalize` — pass the cost and the card's own
creature types, which is the only part of the reminder text that varies between cards (the
subtypes are loaded from Scryfall after the constructor runs, so they cannot be derived here):

```java
addEmbalm("{5}{W}", "Angel");          // Angel of Sanctions   → white Zombie Angel token
addEternalize("{3}{W}{W}", "Cat");     // Adorned Pouncer      → 4/4 black Zombie Cat token
```

Underneath, both build the shape below — reach for it directly only when the ability is not plain
embalm/eternalize (an extra activation cost, a different token transformation):

```java
addGraveyardActivatedAbility(new ActivatedAbility(
    false, "{5}{W}",
    List.of(
        new ExileSelfFromGraveyardCost(),
        new CreateTokenCopyOfSourceEffect(false, 1, CardColor.WHITE, CardSubtype.ZOMBIE, true)),
    "Embalm {5}{W} (...)",
    ActivationTimingRestriction.SORCERY_SPEED));

// Eternalize: same shape but a BLACK Zombie with a fixed 4/4 base P/T (last two args).
new CreateTokenCopyOfSourceEffect(false, 1, CardColor.BLACK, CardSubtype.ZOMBIE, true, 4, 4);
```

**Scavenge** is `addScavenge("{2}{B}")` (CR 702.97a) — `ExileSelfFromGraveyardCost()` plus
`PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, new SourceCardPower())`, a
`TargetFilters.creature()` filter and `SORCERY_SPEED`. The count is `SourceCardPower`, not
`SourcePower`: the scavenged card was exiled as a cost and never was a permanent, so the number
comes from the card. Cards: `SewerShambler`. `Card.scavengeAbility(cost)` builds the same
`ActivatedAbility` standalone — used by `GrantScavengeEqualToManaCostToCreatureCardsEffect` (Varolz,
the Scar-Striped) to grant scavenge for each graveyard creature card's own mana cost.

**Unearth** is `addUnearth("{B}")` — returns the card itself from the graveyard with haste, exiled
at the beginning of the next end step, sorcery speed only. It makes no token, so
`isEmbalmOrEternalize()` is false for it. Sedris, the Traitor King grants unearth to *other*
creatures and is unrelated to this helper.

**Extra activation costs on Embalm/Eternalize** (e.g. Sunscourge Champion's "Eternalize—{2}{W}{W}, Discard a card"): just add the cost effect to the list alongside `ExileSelfFromGraveyardCost`. A `DiscardCardTypeCost(null, null)` in a graveyard ability is now honored — activation suspends on a `DiscardCostChoice` (unpayable/empty hand → activation is illegal), the source is exiled first, and the ability completes after the discard is chosen (`PendingGraveyardAbilityActivation` mirrors the battlefield `PendingAbilityActivation`). The token's own ETB triggers fire when it enters.

Cards: `AngelOfSanctions` (Embalm), `AdornedPouncer` (Eternalize), `SunscourgeChampion` (Eternalize + discard cost; ETB `GainLifeEffect(new SourcePower())`)

**X-cost graveyard abilities** are supported: use an `{X}...` mana cost and read the paid X at
resolution via `entry.getXValue()` in your effect handler. The paid X flows through
`activateGraveyardAbility(gameData, player, graveyardCardIndex, abilityIndex, xValue)` onto the stack
entry (harness: `activateGraveyardAbility(player, gyIndex, abilityIndex, xValue)`; frontend routes any
graveyard ability whose cost contains `{X}` through the X-value prompt). Cards: `Evershrike`
(`{X}{W/B}{W/B}: Return this card from your graveyard to the battlefield. You may put an Aura card with
mana value X or less from your hand onto the battlefield attached to it. If you don't, exile this
creature.` — self-return `ReturnCardFromGraveyardEffect` + `PutAuraFromHandOntoSelfWithinXManaValueOrExileEffect`).

---

### 13. Hand activated ability (Reinforce)

```java
addHandActivatedAbility(new ActivatedAbility(false, manaCost, effects, description, targetFilter))
```

**Use when:** The card has an activated ability usable only while it is in the owner's hand. Currently this is the **Reinforce** keyword ("Reinforce N—{cost} ({cost}, Discard this card: Put N +1/+1 counters on target creature.)"). Discarding the source card is an intrinsic part of the cost — the engine handles it; do **not** add a discard cost effect.

- Uses `Card.addHandActivatedAbility()` / exposed via `Card.getHandActivatedAbilities()`
- Activated via `AbilityActivationService.activateHandAbility()` (validates targets, pays mana, discards the source card to the graveyard — firing discard triggers — then pushes the ability onto the stack targeting the chosen permanent)
- Targeting is a normal `TargetFilter` on the `ActivatedAbility` (e.g. `PermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), ...)`)
- Frontend sends `ACTIVATE_HAND_ABILITY` with `handCardIndex`, `abilityIndex`, `targetId`, `xValue`; `CardView.handActivatedAbilities` exposes it
- Harness: `harness.activateHandAbility(player, handCardIndex, targetId)` (or `(..., targetId, xValue)` for an X cost)
- **Reinforce X** ("Reinforce X—{X}{W}{W}"): use an `{X}...` mana cost and a `new XValue()` amount on the counter effect. The paid X flows through `activateHandAbility(..., xValue)` onto the stack entry's `xValue`, which `XValue` reads at resolution.
- **`.withExilesSourceFromHand()`** — "Exile this card from your hand: Add {G}" (Elvish Spirit Guide). The intrinsic cost exiles the source instead of discarding it, so no discard triggers fire. The ability is a mana ability (`AwardManaEffect`) and therefore resolves immediately into the controller's pool without using the stack (CR 605.1a); `AbilityActivationService.resolveHandManaAbility` handles it. Card: `e/ElvishSpiritGuide.java`.

```java
// Reinforce 2—{2}{W} ({2}{W}, Discard this card: Put two +1/+1 counters on target creature.)
addHandActivatedAbility(new ActivatedAbility(false, "{2}{W}",
    List.of(new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 2)),
    "Reinforce 2—{2}{W} ({2}{W}, Discard this card: Put two +1/+1 counters on target creature.)",
    TargetFilters.creature()));

// Reinforce X—{X}{W}{W} (Swell of Courage) — X counters via new XValue()
addHandActivatedAbility(new ActivatedAbility(false, "{X}{W}{W}",
    List.of(new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, new XValue())),
    "Reinforce X—{X}{W}{W} (...)",
    TargetFilters.creature()));
```

Cards: `BurrentonBombardier`

#### Cycling

Plain cycling is `addCycling("{2}")` — do not build it by hand. It registers the hand ability
and derives the reminder text from the cost, which matters because
`ActivatedAbility.isCyclingAbility()` decides what counts as cycling by reading the description
(the name segment before the first `{` must end in "cycling"). A retyped or reworded string
silently stops being cycling as far as the engine is concerned.

```java
addCycling("{1}{U}"); // Cycling {1}{U} ({1}{U}, Discard this card: Draw a card.)
```

Cycling that does something extra when activated is a *different* ability and keeps its explicit
construction — list the extra effect ahead of the draw, and keep the "Cycling {cost} (…)"
description so it is still recognised:

```java
// Renewed Faith: "Cycling {1}{W}. When you cycle this card, you may gain 2 life."
addHandActivatedAbility(new ActivatedAbility(false, "{1}{W}",
    List.of(new MayEffect(new GainLifeEffect(2), "Gain 2 life?"), new DrawCardEffect(1)),
    "Cycling {1}{W} ({1}{W}, Discard this card: Draw a card.)"));
```

Typecycling and landcycling (`Islandcycling`, `Basic landcycling`) search rather than draw, so
they are built explicitly too; `isCyclingAbility()` still recognises them by the name segment.

Cards: `DesertCerodon` (plain), the Sojourners and Resounding cycles (with an extra effect)

#### Ninjutsu

Ninjutsu is `addNinjutsu("{2}{U}{U}")` — do not build it by hand. It registers a hand ability
flagged `ActivatedAbility.isNinjutsuAbility()` carrying a single `NinjutsuEffect`, and that flag is
what makes `AbilityActivationService.activateNinjutsuAbility` take over: it pays the mana, returns
the unblocked attacking creature named by `targetId` (a **cost**, not a target — the generic
targeting validation is skipped) to its owner's hand, and leaves the source card in hand, revealed,
until the ability resolves (CR 702.49a/b).

`NinjutsuEffectHandler` then moves the card from hand onto the battlefield tapped and attacking the
same defender the returned creature was attacking — that defender is captured at activation time and
baked into the `NinjutsuEffect(attackTargetId)` snapshot, because the returned attacker is gone by
resolution (CR 702.49c). The ninja was never declared as an attacker, so no attack triggers or attack
legality checks run.

Only a creature matching `PermanentIsUnblockedAttackingPredicate` **on the activating player's own
battlefield** is a legal cost; that predicate already refuses steps before declare blockers.

```java
addNinjutsu("{2}{U}{U}");
```

Cards: `HigureTheStillWind`

#### Hand ability targeting graveyard cards (Faerie Macabre)

A "Discard this card: ..." hand ability whose effect targets cards in graveyards (not a battlefield
permanent) uses the same `addHandActivatedAbility` registration with **no `TargetFilter`** — the
graveyard targets are supplied at activation time as a list of card IDs.

```java
// Discard this card: Exile up to two target cards from graveyards. (Faerie Macabre)
addHandActivatedAbility(new ActivatedAbility(false, null,
    List.of(new ExileCardsFromGraveyardEffect(2, 0)),
    "Discard this card: Exile up to two target cards from graveyards."));
```

- Activated via `AbilityActivationService.activateHandAbilityWithGraveyardTargets()` (validates the
  graveyard targets via `TargetLegalityService.validateMultiTargetGraveyardAbility`, discards the
  source card, then pushes the ability with `Zone.GRAVEYARD` + `targetCardIds`). The
  `ExileCardsFromGraveyardEffect` handler exiles the chosen cards; because targets are locked before
  the discard, the source card itself is never a legal target.
- Frontend sends `ACTIVATE_HAND_ABILITY` with `handCardIndex`, `abilityIndex`, `graveyardCardIds`.
- Harness: `harness.activateHandAbilityWithGraveyardTargets(player, handCardIndex, graveyardCardIds)`

Cards: `FaerieMacabre`

---

## Mana ability riders ("Add {X}. When you do, ...")

An ability that produces mana and has no target/loyalty cost is a **mana ability** (resolves immediately, no stack). Any non-mana effects in its list are treated as reflexive "when you do" riders resolved inline by `ActivatedAbilityExecutionService.doResolveManaAbility`. Supported rider effects include `GainLifeEffect`, `DealDamageToPlayersEffect` with recipient `CONTROLLER`, `DealDamageToPlayersEffect` with recipient `EACH_OPPONENT` (Rubble Rouser: `{T}, Exile a card from your graveyard: Add {R}. When you do, deal 1 damage to each opponent.`), `PutCountersOnSelfEffect` (Pyramid of the Pantheon: `{2}, {T}: Add one mana of any color. Put a brick counter on this artifact.` — the counter is placed on the source, respecting `cantHaveCounters`), and `CopyNextInstantOrSorceryCastThisTurnEffect` (Ether: `{T}, Exile this artifact: Add {U}. When you next cast an instant or sorcery spell this turn, copy that spell.`). A `ConditionalEffect` wrapping one of those riders is also supported — the condition is evaluated at mana-ability resolution and the wrapped rider runs only when met (Avid Reclaimer: `{T}: Add {G} or {U}. If you control a Nissa planeswalker, you gain 2 life.`). Effects that directly draw from a library, such as `DrawCardEffect`, make the ability use the stack instead. To support a new rider, add a branch in `doResolveManaAbility` — a rider effect placed on a mana ability but not handled there is silently dropped.

## Costs in the effects list

Sacrifice and discard costs go in the `effects` list BEFORE the actual effect. The engine processes them in order.

All cost effects implement the `CostEffect` marker interface (which extends `CardEffect`). When creating a new cost effect, implement `CostEffect` instead of `CardEffect` — this ensures it is automatically filtered out during effect snapshotting and excluded from mana ability detection.

**Cost-vs-effect sacrifice invariant (rules-critical).** A sacrifice written *before the colon* of an **activated ability** is a **cost** — it MUST use a `Sacrifice…Cost` record so it is paid at activation, before priority passes. Never model an activated ability's sacrifice cost as a `Sacrifice…Effect` on the stack: that would hand opponents a response window they should not get and snapshot the sacrificed permanent's characteristics at the wrong time. Conversely, a sacrifice that happens during **resolution** — a spell's effect, a triggered ability, a delayed sacrifice, or anything written *after* the colon ("…: create a token, **then** sacrifice a creature") — is genuinely part of the effect and MUST use a `Sacrifice…Effect`; there is no cost there to pay. For characteristic-dependent sacrifice costs (X = the sacrificed creature's power/toughness/mana value/color symbols), `SacrificeCreatureCost` snapshots those at payment time via its `trackSacrificed*` flags — no effect-time workaround is needed.

| Cost effect | Constructor | Use when |
|------------|-------------|----------|
| `SacrificeSelfCost` | `()` or `(true)` | "Sacrifice this: ...". `(true)` snapshots this permanent's effective power into xValue at payment (Mausoleum Wanderer) |
| `SacrificeCreatureCost` | `()` | "Sacrifice a creature: ..." |
| `SacrificeCreatureCost.withPermanentSnapshot()` | `()` | preserve the sacrificed creature's last-known battlefield characteristics for a later effect in the same ability (Pyre of Heroes) |
| `SacrificeCreatureCost` | `(false, false, false, true)` | "Sacrifice another creature: ..." (excludeSelf prevents sacrificing the source) |
| `SacrificePermanentCost` | `(new PermanentIsArtifactPredicate(), "an artifact", false)` | "Sacrifice an artifact: ..." (`excludeSource=false` so an artifact source can sacrifice itself) |
| `SacrificePermanentCost` | `(PermanentPredicate filter, String description)` or `(PermanentPredicate filter, String description, boolean excludeSource)` | "Sacrifice an artifact or creature: ..." or "Sacrifice a Goblin: ..." — generic predicate-based sacrifice. Use `PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate(), new PermanentHasSubtypePredicate(CardSubtype.GOBLIN)))` and `excludeSource=false` for subtype creature costs that can sacrifice the source. The 4-arg form `(filter, description, excludeSource, trackSacrificedPower)` snapshots the sacrificed permanent's effective power into the ability's xValue at payment — Freyalise Supplicant (`PermanentAllOfPredicate(creature + PermanentColorInPredicate(RED, WHITE))` + `DealDamageToAnyTargetEffect(new Divided(new XValue(), 2))`). The 5-arg form adds `trackSacrificedManaValue` — Soldevi Adnate (`AwardManaEffect(ManaColor.BLACK, new XValue())`, a mana ability, `excludeSource=false` so it can eat itself). The 6-arg form adds `trackSacrificedToughness` — Korozda Guildmage (`creature + PermanentNotPredicate(PermanentIsTokenPredicate)` + `CreateTokenEffect(new XValue(), …)`) |
| `ExilePermanentCost` | `(PermanentPredicate filter, String description[, boolean excludeSource[, boolean trackExiledManaValue]])` | "Exile a creature you control: ..." or another predicate-based permanent exile cost. `trackExiledManaValue=true` snapshots the exiled permanent's mana value into the ability's xValue for a companion effect such as Food Chain's restricted mana |
| `ExileArtifactsWithTotalManaValueCost` | `()` | "Exile one or more other artifacts you control with total mana value X: ..."; prompts for a non-empty subset, sums the selected artifacts' mana values into the ability's xValue, and exiles them as the activation cost |
| `CraftMaterialCost` | `()`, `(CardSubtype)`, or `(count, requiredType, nonlandOnly, requireActivatedAbility)` | Craft's alternative material. The default exiles exactly one other artifact permanent you control or artifact card in your graveyard; pass `null` for `requiredType` to allow any other permanent/card. The subtype form requires exactly one matching permanent/card. Use `oneOrMore()` or `oneOrMore(subtype)` for open-ended craft costs, and `nonlandsWithActivatedAbilities(N)` for N+ nonlands with activated abilities. Pair with `ExileSelfCost` and a return-from-exile transformed effect. |
| `SacrificeMultiplePermanentsCost` | `(int count, PermanentPredicate filter)` | "Sacrifice three artifacts: ..." (use with matching predicate) |
| `SacrificeAllMatchingPermanentsCost` | `(PermanentPredicate filter)` | "Sacrifice all matching permanents you control: ..." — pays automatically, including the source when it matches; zero matching permanents is legal |
| `SacrificePermanentsSequenceCost` | `(List<PermanentPredicate> filters, List<String> descriptions)` | "Sacrifice a green creature, a white creature, and a blue creature: ..." (Angel's Herald) — one distinct permanent per per-slot filter, in order. Use this single cost, NOT several `SacrificePermanentCost` entries: the activation resume path carries only one cost effect through interactive picks, so multiple distinct sacrifice costs on one ability would silently skip the 2nd/3rd. Only offers a slot permanents whose selection still leaves a full matching for the remaining slots (no dead-end mid-payment). Slot filters see the ability's source card, so a slot may be `PermanentIsSourceCardPredicate()` for "…, and this creature" — Urborg Panther ("Sacrifice a creature named Feral Shadow, a creature named Breathstealer, and this creature") uses two `PermanentNamedPredicate`s plus that source slot, which pins the third sacrifice to the activating Panther rather than any copy |
| `ReturnMultiplePermanentsToHandCost` | `(int count, PermanentPredicate filter)` | "Return two lands you control to their owner's hand: ..." (bounces N matching permanents as cost). Works with both battlefield and graveyard activated abilities |
| `ReturnSelfToHandCost` | `()` | "Return this [permanent] to its owner's hand: ..." — bounces the source permanent as the activation cost (Cycle of Life, Ovinomancer). Paid in `ActivatedAbilityExecutionService` before the ability goes on the stack, so the ability still resolves from an empty battlefield slot. Pair with a `null` mana cost; combine with `requiresTap = true` when the cost is `{T}, Return this …` |
| `TapMultiplePermanentsCost` | `(int count, PermanentPredicate filter)` or `(int, PermanentPredicate, boolean excludeSource)` | "Tap N untapped [matching] you control: ..." — taps N untapped matching permanents as cost (Captivating Vampire, Gilt-Leaf Archdruid, Crackleburr's damage half). Combine `PermanentIsCreaturePredicate` + `PermanentColorInPredicate` via `PermanentAllOfPredicate` for "tap two red creatures". The 3-arg `(DynamicAmount, PermanentPredicate, boolean)` form takes `new XValue()` for "Tap X untapped [matching] you control" — X announced at activation (Aryel, Knight of Windgrace); `TapCostSupport` evaluates it once where the cost handler is built. |
| `UntapMultiplePermanentsCost` | `(int count, PermanentPredicate filter)` or `(int, PermanentPredicate, boolean excludeSource[, boolean opponentControlled])` | Untap-symbol `{Q}` mirror of `TapMultiplePermanentsCost`: "Untap N tapped [matching] you control: ..." — valid choices are *tapped* matching permanents, which get untapped as cost. Pair with `.withRequiresUntap()` on the ability. Crackleburr ("Untap two tapped blue creatures you control"). With `opponentControlled = true` the choices come from opponents' battlefields instead and the ability keeps `{T}`, not `{Q}`; a `count = 1` instance also records the untapped permanent on the source for `AwardManaOfTypeUntappedLandCouldProduceEffect` (Benthic Explorers). |
| `SacrificeAllCreaturesYouControlCost` | `()` | "Sacrifice all creatures: ..." (SPELL: Soulblast) |
| `SacrificeAllPermanentsYouControlCost` | `()` | "Sacrifice all permanents you control: ..." — SPELL-slot additional cast cost (Kaervek's Spite). Legal with zero permanents |
| `SacrificeXPermanentsCost` | `(PermanentPredicate filter[, boolean requireAtLeastOne])` | "Sacrifice X [matching]: ..." — sacrifices X permanents matching the filter, where X is the ability's xValue chosen at activation (the sacrifice-analog of `TapMultiplePermanentsCost` with an `XValue` count). Set `requireAtLeastOne=true` for "sacrifice one or more" wording. Springjack Pasture uses the default; Radiant Lotus requires at least one artifact |
| `DiscardCardTypeCost` | `(CardPredicate, String label)` or `(CardPredicate, String label, boolean manaValueEqualsX)` or `(CardPredicate, String label, int count)` or `(predicate, label, manaValueEqualsX, count, sameName, trackManaValue)` | "Discard a [label] card: ..." (null predicate = any card). E.g. `(new CardTypePredicate(CardType.LAND), "land")`, `(new CardIsHistoricPredicate(), "historic")`, `(null, null)` for any. `manaValueEqualsX=true` → "Discard a card with mana value X" (restricts valid discards to MV == chosen X; pair with an `{X}` cost). Knollspine Invocation. `count` (default 1) for "Discard N cards" (Haunted Dead = 2); activation prompts sequentially until all are paid. `(predicate, label, int count, boolean sameName)` with `sameName=true` for "Discard two nonland cards with the same name" (Sphinx of the Chimes): the first prompt only offers cards whose name appears at least `count` times in hand, and every later prompt is pinned to that name. `trackManaValue=true` snapshots the discarded card's mana value into the entry's `xValue` (Mercurial Chemister + `DealDamageToTargetCreatureEffect(new XValue())`) . `imprintOnSource=true` (7th component) imprints the discarded card on the source card so the ability's own effects can inspect it at resolution via `ImprintedCardMatches` — Necromancer's Stockpile's "If the discarded card was a Zombie card" |
| `ExileCardFromHandCost` | `()`, `(CardPredicate, String label)`, `(CardPredicate, String label, int count)`, or `(CardPredicate, String label, int count, boolean imprintOnSource)` | "Exile a card from your hand: ..." — null predicate = any card. Shares the `HandCardCost` activation path with `DiscardCardTypeCost` (up-front legality check, interactive hand pick), but exiles the chosen card, firing no discard triggers. `imprintOnSource=true` remembers the exiled card on the source for resolution-time comparisons (Holistic Wisdom). Cadaverous Bloom |
| `ExileTopCardOfLibraryCost` | `(int count)` or `(int count, boolean imprintOnSource)` | "Exile the top card of your library: ..." — exiles the top N library cards as activation cost. Blocks activation if the library is too small. Royal Herbalist (`count=1`). `imprintOnSource=true` imprints the last card exiled this way on the source permanent so the ability's effect can inspect it at resolution via the `ImprintedCardMatches` condition (Storm Elemental) |
| `DiscardHandCost` | `()` | "Discard your hand: ..." — discards the controller's entire hand as a cost (no choice, no legality restriction; empty hand is fine). Fires per-card discard triggers. Works as an activation cost (Slate of Ancestry) **and** as a SPELL-slot additional cast cost (Kaervek's Spite; spell already left the hand before payment) |
| `DiscardRandomCardCost` | `()` | "Discard a card at random: ..." — discards one uniformly-random card from the controller's hand as a cost (no player choice). Requires a non-empty hand to activate, or another card in hand when used in a SPELL slot. Fires the discarded card's discard triggers. Coral Helm; Acceptable Losses |
| `DiscardRandomCardCost` | `()` or `(int count)` | "Discard a card at random: ..." / "Discard N cards at random: ..." — discards the specified number of cards uniformly at random from the controller's hand as a cost (no player choice). Requires at least `count` cards to activate. Fires each discarded card's discard triggers. Coral Helm; Meteor Storm (`count=2`) |
| `ExileTopCardOfGraveyardCost` | `()` or `(CardType requiredType)` | "Exile the top card of your graveyard: ..." — exiles the last (most recently added) card of the controller's graveyard. No player choice; activation is illegal when nothing matches. Alms (`()`). `requiredType` narrows it to "the top [type] card of your graveyard": the matching card closest to the top, with nonmatching cards above it skipped rather than blocking. Also supported as the payable side of `ForcedCostOrElseEffect` — Barrow Ghoul `ForcedCostOrElseEffect(ExileTopCardOfGraveyardCost(CREATURE), [SacrificeSelfEffect], true)`; an empty-of-creatures graveyard makes it unpayable so the sacrifice happens with no prompt |
| `ExileCardFromGraveyardCost` | `(CardType)`, `(CardSubtype)`, `(CardSubtype, boolean anyGraveyard)`, or `(CardType, boolean payManaCost, boolean imprint, boolean trackPower)` | "Exile a [type] card from your graveyard: ..." (null = any type). Use the `(CardSubtype)` ctor for "Exile an Elf card" (Scarred Vinebreeder); pass `anyGraveyard=true` for wording that says "from a graveyard" (Thelon of Havenwood). Use `(CardType, CardType alternateType)` for "an instant or sorcery card" (Disciple of the Ring). For spells: use in SPELL slot with `trackExiledPower=true` to set X to exiled card's power |
| `ExileInstantOrSorcerySpellCost` | `()` | "Exile an instant or sorcery spell you control: ..." — prompts for one qualifying spell controlled by the activating player on the stack, then exiles it as the activation cost. Copies are removed from the stack without being put into exile. Nivmagus Elemental |
| `ExileNCardsFromGraveyardCost` | `(int count, CardType requiredType)` | "Exile N [type] cards from your graveyard: ..." (null type = any). On a **battlefield** ability the front N cards are exiled deterministically (Immortal Coil, `count=2, null`). On a **graveyard-activated** ability (Salvage Titan, `count=3, ARTIFACT`) the path exiles N cards matching the type via `hasType` (artifact creatures count) **excluding the source card**, so a self-return ability doesn't exile the card it means to bring back — needs N *other* matching cards to activate |
| `TapEnchantedPermanentCost` | `()` | "Tap enchanted [land]: ..." on an Aura's own activated ability (Earthlore). Taps the permanent the source Aura is attached to — not the Aura — and fires that permanent's tap triggers (Psychic Venom). Since an already-tapped permanent can't pay it, it also covers the printed "Activate only if enchanted land is untapped" clause; no separate activation condition is needed. Pair with `requiresTap = false`. |
| `TapTwoCreaturesSharingTypeCost` | `()` | "Tap two untapped creatures you control that share a creature type: ..." (Weight of Conscience). The two tapped creatures must share a creature type with each other (Changeling-aware, mutual constraint) — not expressible with `TapMultiplePermanentsCost`'s per-permanent filter. |
| `RevealTwoCardsSharingColorCost` | `()` | "Reveal two cards from your hand that share a color: ..." (Illuminated Folio). Revealed cards stay in hand; the cost only gates the ability, so payment auto-reveals any qualifying pair (a valid pair must exist to activate; colorless cards never qualify). |
| `RevealXCardsFromHandCost` | `(CardPredicate filter)` | "Reveal X [matching] cards from your hand: ..." — X is announced for the ability, the controller chooses exactly X matching cards, and the cards stay in hand while being revealed to all players. `withXValueFromCardsInHand(CardColor)` exposes the matching-color cap to the client. |
| `RemoveCounterFromSourceCost` | `()` | "Remove a counter from this: ..." |
| `RemoveXCountersFromSourceCost` | `(CounterType)` | "Remove X [type] counters from this: ..." (Night Dealings, Cruel Sadist). X is chosen at activation and capped by the counters on the source (not an `{X}` mana cost); it becomes the activation's `xValue`, readable by `XValue` / `ManaValueBound`. When the client does not announce X the engine prompts for it with an `XValueChoice` |
| `RemoveOneOrMoreCountersFromSourceCost` | `(CounterType)` | "Remove one or more counters from this: ..."; the chosen positive count is supplied as `xValue` |
| `RemoveOneOrMoreCountersFromControlledCreaturesCost` | `(CounterType)` | "Remove one or more counters from among creatures you control: ..."; board-wide sibling of the above, counters split freely across your creatures, count supplied as `xValue`. Pair with `.withXValueFromControlledCreatureCounters()` so the client caps X by your whole board (Ooze Flux) |
| `PutCounterOnSourceCost` | `()` = -1/-1 ×1, or `(powerMod, toughnessMod, count)` | "Put a -1/-1 counter on this creature: ..." — puts counters on the source as a cost (paid immediately on activation). `powerMod > 0` → +1/+1; `powerMod == 0 && toughnessMod < 0` → -0/-1 (Wall of Roots, unaffected by -1/-1 replacement effects); otherwise -1/-1. Respects `cantHaveCounters`/`cantHaveMinusOneMinusOneCounters`. Barrenton Medic |
| `PutTypedCounterOnSourceCost` | `(CounterType)` = 1 counter, or `(CounterType, count)` | "Put a verse counter on this creature: ..." — typed sibling of `PutCounterOnSourceCost` for counters with no P/T meaning (verse, charge, …), paid immediately on activation. Respects `cantHaveCounters`. Pair with `CountersOnSource(type)` to size the effect (Yisan, the Wanderer Bard) |
| `IncreaseActivationCostPerCounterEffect` | `(CounterType, int increasePerCounter)` | raises the generic activation cost by N per counter of that type on the source, counted at activation time. Pair with a printed `{0}` cost for "{X}: … X is the number of [type] counters on this permanent" (Chromatic Armor). Mirror of `ReduceActivationCostPerCounterEffect` (Diary of Dreams) |
| `ReduceActivationCostEffect` | `(DynamicAmount amount)` | reduces the generic activation cost by the evaluated amount at activation time. Use a counting amount for "This ability costs {1} less to activate for each …" (Nemesis of Mortals) |
| `ReduceBoastActivationCostEffect` | `(DynamicAmount amount)` | STATIC, controller-scoped reduction for marked boast abilities; the amount is evaluated against the permanent carrying the effect (Dragonkin Berserker) |
| `PutCounterOnControlledCreatureCost` | `(CounterType counterType, int count)` | "Put a -1/-1 counter on a creature you control: ..." — puts counter(s) on any creature you control (not just the source), chosen via the `PermanentChoiceCostHandler` pattern (auto-selects when only one creature exists, prompts when multiple). Also valid as a SPELL-slot cost (Scarscale Ritual). Hatchet Bully |
| `ReturnCardFromGraveyardToHandCost` | `(CardPredicate predicate)` | Payable side of `ForcedCostOrElseEffect` only. "sacrifice this unless you return a [predicate] card from your graveyard to your hand" — Harvest Wurm (`CardPredicateUtils.basicLand()`). No matching graveyard card ⇒ unpayable, penalty resolves with no prompt; accepting opens a mandatory `GraveyardChoice` to `HAND` |
| `RemoveCounterFromControlledPermanentCost` | `()` or `(CounterType...)` or `(int count, PermanentPredicate filter, boolean excludeSource)` | Removes one or more counters from permanents you control; the no-argument form allows any kind, varargs restrict the listed kinds, and the counted form can filter eligible permanents and exclude the source. As an activated-ability cost, one candidate pays automatically and several prompt a permanent choice. Chisei, Heart of Oceans; Power Conduit; Ion Storm; Tekuthal, Inquiry Dominus |
| `PayManaCost` | `(String manaCost)` | Payable side of `ForcedCostOrElseEffect` only (not an `ActivatedAbility` cost). "you may pay {cost}; if you don't, [penalty]" — e.g. Force of Nature `ForcedCostOrElseEffect(PayManaCost("{G}{G}{G}{G}"), penalties, true)` |
| `FlipCoinsCost` | `(int count)` | Payable side of `ForcedCostOrElseEffect` only: flips `count` coins and emits the payer's win/loss coin-flip triggers. Used by Karplusan Minotaur's `CumulativeUpkeepEffect.flipCoin()` |

```java
// {1}{R}, Sacrifice a Goblin: Deal 2 damage to any target
new ActivatedAbility(false, "{1}{R}",
    List.of(new SacrificePermanentCost(
        new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate(), new PermanentHasSubtypePredicate(CardSubtype.GOBLIN))),
        "Sacrifice a Goblin", false), new DealDamageToAnyTargetEffect(2)),
    "{1}{R}, Sacrifice a Goblin: Siege-Gang Commander deals 2 damage to any target.")

// Sacrifice self: Gain 3 life
new ActivatedAbility(false, null,
    List.of(new SacrificeSelfCost(), new GainLifeEffect(3)),
    "Sacrifice Bottle Gnomes: You gain 3 life.")

// {T}, Sacrifice an artifact: Deal 2 damage to any target
new ActivatedAbility(true, null,
    List.of(new SacrificePermanentCost(new PermanentIsArtifactPredicate(), "an artifact", false),
            new DealDamageToAnyTargetEffect(2)),
    "{T}, Sacrifice an artifact: Barrage Ogre deals 2 damage to any target.")

// {T}, Sacrifice three artifacts: Search library for artifact to battlefield
new ActivatedAbility(true, null,
    List.of(new SacrificeMultiplePermanentsCost(3, new PermanentIsArtifactPredicate()),
            new SearchLibraryEffect(new CardTypePredicate(CardType.ARTIFACT), LibrarySearchDestination.BATTLEFIELD)),
    "{T}, Sacrifice three artifacts: Search your library for an artifact card, put it onto the battlefield, then shuffle.")
```

Cards: `SiegeGangCommander`, `BottleGnomes`, `DoomedNecromancer`, `ThrullSurgeon`, `BloodfireColossus`, `BarrageOgre`, `KuldothaForgemaster`

---

## TargetFilter types and Predicates

**Full reference:** See **PREDICATES_REFERENCE.md** for complete tables of all TargetFilter types, PermanentPredicate, StackEntryPredicate, and PlayerPredicate compositions.

**Quick summary of TargetFilter types:**

For the common restrictions ("target creature", "target land you control", …) use the
`TargetFilters` factories instead of constructing these directly — see
PREDICATES_REFERENCE.md § TargetFilters.

| Filter class | Use when |
|-------------|----------|
| `PermanentPredicateTargetFilter` | Target any permanent matching predicate |
| `ControlledPermanentPredicateTargetFilter` | Target only permanents YOU control |
| `OwnedPermanentPredicateTargetFilter` | Target only permanents YOU OWN |
| `StackEntryPredicateTargetFilter` | Target a spell on the stack |
| `PlayerPredicateTargetFilter` | Target a player |

**Quick summary of composition predicates:**

| Predicate | Use |
|-----------|-----|
| `PermanentAllOfPredicate(List)` | AND: all must match |
| `PermanentAnyOfPredicate(List)` | OR: at least one matches |
| `PermanentNotPredicate(predicate)` | NOT: inverts |

---

## Card-level targeting (for spells, not abilities)

For spells (instants/sorceries) that need targets, targeting is auto-derived from effects. Override `targetSpec()` on your effect record to return a non-NONE `TargetSpec` (category + `harmful` flag + optional predicate — see `EFFECTS_INDEX.md`). Then in the card constructor:

```java
setTargetFilter(new SomeTargetFilter()); // restricts valid targets (optional)
addEffect(EffectSlot.SPELL, effect);     // effect resolved when spell resolves
// EffectResolution.needsTarget(card) and EffectResolution.needsSpellTarget(card) are computed automatically from effects
```

---

## EffectSlot quick reference

| Slot | Fires when |
|------|------------|
| `SPELL` | Instant/sorcery resolves |
| `ON_ENTER_BATTLEFIELD` | Permanent enters the battlefield (ETB) |
| `ON_TAP` | Permanent is tapped for mana (lands) |
| `MULLIGAN_ACTION` | Special action offered while deciding whether to mulligan |
| `STATIC` | Continuous effect, always active while on battlefield |
| `UPKEEP_TRIGGERED` | Controller's upkeep. Supports any-target routing (creature/planeswalker/battle/player) when an effect is true "any target" (`targetSpec()` admits both `Kind.PLAYER` and `Kind.PERMANENT`, e.g. Form of the Dragon via `UpkeepAnyTargetTrigger`), single-player targeting (e.g. Bloodgift Demon via `UpkeepPlayerTargetTrigger`), and multi-player targeting (e.g. Axis of Mortality via `UpkeepMultiPlayerTargetTrigger` when any effect has `targetSpec().playerTargetCount() >= 2`) |
| `EACH_UPKEEP_TRIGGERED` | Each player's upkeep |
| `OPPONENT_UPKEEP_TRIGGERED` | Each opponent's upkeep |
| `ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED` | Upkeep of the enchanted permanent's controller (fires regardless of which player controls the aura). `affectedPlayerId` is baked in at trigger time for effects like `EnchantedCreatureControllerLosesLifeEffect` |
| `ENCHANTED_PERMANENT_CONTROLLER_DRAW_TRIGGERED` | Draw step of the enchanted permanent's controller (fires regardless of which player controls the aura). That player is baked as `targetId` — pair with `DrawCardForTargetPlayerEffect` (Righteous Authority) |
| `ENCHANTED_PLAYER_UPKEEP_TRIGGERED` | Upkeep of the enchanted player (for player auras/Curses). The enchanted player's ID is passed as `targetId` on the stack entry. Curse subtype is auto-detected via `isEnchantPlayer()` |
| `ENCHANTED_PLAYER_END_STEP_TRIGGERED` | "At the beginning of each end step, enchanted player …" (player aura/Curse). Fires at EVERY end step (any player's turn), unlike the upkeep variant. The enchanted player's ID (`attachedTo`) is baked as `targetId` on the stack entry. Used by Fraying Sanity (`MillEffect(new CardsPutIntoGraveyardByTargetPlayerThisTurn(), TARGET_PLAYER)`) |
| `ON_ENCHANTED_PLAYER_CREATURE_ENTERS_BATTLEFIELD` | "Whenever a creature enchanted player controls enters, …" (player-aura/Curse slot). `TriggerCollectionService.checkEnchantedPlayerCreatureEntersTriggers` scans every battlefield for Curses attached to the entering creature's controller and queues one non-targeting triggered ability each, controlled by the Aura's controller with the enchanted player baked as `targetId` — so `LoseLifeEffect(TARGET_PLAYER)` hits them and an accompanying `GainLifeEffect` feeds "you". Used by Trespasser's Curse (`LoseLifeEffect(1, TARGET_PLAYER)` + `GainLifeEffect(1)`) |
| `GRAVEYARD_UPKEEP_TRIGGERED` | Upkeep trigger from graveyard |
| `GRAVEYARD_END_STEP_TRIGGERED` | End-step trigger from graveyard ("At the beginning of the end step, if this card is in your graveyard …"). Fires at EVERY end step, scanned across all players' graveyards in APNAP order by `StepTriggerService.handleEndStepTriggers`; a wrapping `ConditionalEffect` is an intervening-if checked at trigger time. Used by Krovikan Horror |
| `GRAVEYARD_CONTROLLER_END_STEP_TRIGGERED` | End-step trigger from a graveyard card during its owner's end step ("At the beginning of your end step …"). Scans only the active player's graveyard when that player is the card's owner; a wrapping `ConditionalEffect` is checked at trigger time. Used by Silversmote Ghoul |
| `GRAVEYARD_ON_CONTROLLER_CASTS_SPELL` | Spell-cast trigger from graveyard — fires when the controller casts a spell matching the `SpellCastTriggerEffect.spellFilter()` while this card is in their graveyard (e.g. Lingering Phantom). Supports `manaCost` for "you may pay" patterns |
| `GRAVEYARD_ON_CONTROLLER_SURVEILS` | Surveil trigger from graveyard — fires whenever the controller surveils while this card is in their graveyard. Effects are queued from the card's graveyard entry; Blood Operative uses `ConditionalEffect(SourceCardInGraveyard, MayPayManaEffect("{0}", 3, ReturnSourceCardFromGraveyardToOwnerHandEffect(), ...))` |
| `GRAVEYARD_ON_CONTROLLER_PERMANENT_SACRIFICED` | Permanent-sacrifice trigger from graveyard — fires whenever the controller sacrifices a permanent while this card is in their graveyard. Wrap in `TriggeringCardConditionalEffect` to filter the sacrificed permanent (e.g. `CardSubtypePredicate(CLUE)`). A source card sacrificed in the same event does not trigger. Scanned in `TriggerCollectionService.checkGraveyardAllyPermanentSacrificedTriggers`. Used by Curious Cadaver |
| `ON_CONTROLLER_CARDS_LEAVE_GRAVEYARD` | Fires once when one or more cards leave the controller's graveyard, batching simultaneous departures |
| `ON_CONTROLLER_ARTIFACT_OR_CREATURE_CARDS_LEAVE_GRAVEYARD` | Fires once when one or more non-token artifact and/or creature cards leave the controller's graveyard, batching simultaneous departures |
| `GRAVEYARD_ON_CREATURE_CARD_LEAVES_OPPONENT_GRAVEYARD` | Fires once per creature card that leaves an **opponent's** graveyard, while this card is in its owner's graveyard. Fired per leaving card from `GraveyardService.notifyCardLeftGraveyard` (single removals via `PermanentRemovalService.removeCardFromGraveyardById`) and from the bulk `clearGraveyard` path; never batched. Non-targeting. Used by Erebos's Titan |
| `DRAW_TRIGGERED` | At the beginning of controller's draw step (draw step only, not spell draws) |
| `EACH_DRAW_TRIGGERED` | At the beginning of any player's draw step (draw step only). For "each **opponent's** draw step", have the effect implement the `OpponentDrawStepOnlyEffect` marker — `StepTriggerService` then skips the trigger on the controller's own draw step (Malignant Growth) |
| `ON_CONTROLLER_DRAWS` | Whenever controller draws a card (all draws: draw step, spells, abilities). Use `NthCardDrawTriggerEffect` for an exact per-turn draw number, such as "your second card each turn". |
| `ON_OPPONENT_DRAWS` | Whenever an opponent draws a card (all draws: draw step, spells, abilities). `NthCardDrawTriggerEffect` narrows this to an opponent's exact per-turn draw number. |
| `ON_OPPONENT_DISCARDS` | An opponent discards a card |
| `ON_CONTROLLER_DISCARDS` | The controller discards a card ("whenever you discard a card"; cycling counts). Scanned on the discarding player's own battlefield in `TriggerCollectionService.checkDiscardTriggers`. Used by Necropotence (`ExileDiscardedCardFromGraveyardEffect`, resolved inline), Curator of Mysteries (`ScryEffect`, enqueued as a `TRIGGERED_ABILITY`), Drake Haven (`MayPayManaEffect`, enqueued as a `TRIGGERED_ABILITY` — its may-pay prompt comes up at resolution) and Hekma Sentinels (`BoostSelfEffect`, enqueued as a `TRIGGERED_ABILITY` carrying the source permanent id so "this creature gets +1/+1"). Targeted variants queue a `PermanentChoiceContext.DiscardControllerTriggerTarget` instead: Zenith Seeker (`GrantKeywordEffect` with `GrantScope.TARGET`) and Ominous Sphinx (`BoostTargetCreatureEffect` with an opponent-creature `filter`) — the effect's own predicate narrows the target |
| `ON_CONTROLLER_DISCARD_EVENT` | The controller discards one or more cards as one event; the event context carries the number discarded. Used by Cryptcaller Chariot for one trigger that creates one token per discarded card and Marauding Mako for one trigger that puts that many counters on itself |
| `ON_SELF_DISCARDED` | This card is discarded for any reason ("When you discard this card"). Non-targeting effects (e.g. `MayPayManaEffect`) enqueue a `TRIGGERED_ABILITY`; any-target effects use `DiscardTriggerAnyTarget`. Used by Edgar's Awakening |
| `ON_SELF_DISCARDED_BY_OPPONENT` | This card is discarded by an opponent. Same split as `ON_SELF_DISCARDED`: any-target effects use the `DiscardTriggerAnyTarget` prompt (Guerrilla Tactics), non-targeting effects are enqueued straight onto the stack (Mangara's Blessing). `EnterBattlefieldOnDiscardEffect` is a replacement effect and is filtered out |
| `END_STEP_TRIGGERED` | End step (any player's turn — "at the beginning of the end step"). The stack entry's `targetId` is `null` unless the effect implements the `EndStepPlayerTargetedEffect` marker, which makes `StepTriggerService` bake the end-step (active) player into `targetId` — use it for "at the beginning of each player's end step, … that player …" effects (Monsoon's `TapPlayersPermanentsAndDamageEqualToCountEffect`) |
| `CONTROLLER_END_STEP_TRIGGERED` | Controller's end step only ("at the beginning of your end step") |
| `ON_ATTACK` | This creature attacks. Wrap in `OncePerTurnTriggerEffect` for "attacks for the first time each turn" (Aurelia, the Warleader) — unwrapped at most once per turn per permanent |
| `ON_ALLY_CREATURES_ATTACK` | One or more creatures the controller controls attack (fires once per combat, not per creature). Scans all controller's permanents after attackers are declared. Targeted effects use the attack-trigger target-selection pipeline; non-targeting effects are pushed directly to the stack |
| `GRAVEYARD_ON_ALLY_CREATURES_ATTACK` | Like ON_ALLY_CREATURES_ATTACK but fires from the controller's graveyard. The attacker count is passed via xValue. Supports `ConditionalEffect(new MinimumAttackers(minimumAttackers), wrapped)` wrapper for "N or more creatures" conditions. Used by Warcry Phoenix |
| `GRAVEYARD_ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER` | Like ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER but fires from the controller's graveyard. Holds an `AllyCombatDamageTriggerEffect`; the stack entry's source is the graveyard card itself (no source permanent). Wrap the inner effect in `MayEffect(ReturnSourceCardFromGraveyardToOwnerHandEffect(), ...)` for "if this card is in your graveyard, you may return this card to your hand". Scanned in `CombatDamageService.checkAllyCreatureCombatDamageToPlayerTriggers`. Used by Auntie's Snitch (Goblin-or-Rogue dealer predicate). Set `oneOrMoreDealers=true` for "whenever one or more creatures you control deal combat damage to a player" so the trigger fires once per damage event rather than once per dealer — Pyrewild Shaman (`MayPayManaEffect("{3}", ReturnSourceCardFromGraveyardToOwnerHandEffect(), …)`) |
| `GRAVEYARD_ON_ALLY_CREATURE_ENTERS_BATTLEFIELD` | Like ON_ALLY_CREATURE_ENTERS_BATTLEFIELD but fires from the controller's graveyard. Wrap in `TriggeringCardConditionalEffect(CardSubtypePredicate(...), inner)` to gate on the entering creature's subtype. A `MayEffect` inner is queued as a may-ability; anything else (e.g. `MayPayManaEffect`) goes on the stack. Excluded from Naban ETB doubling (a graveyard card is not a permanent). Scanned in `TriggerCollectionService.checkAllyCreatureEntersTriggers`. Used by Unconventional Tactics ("whenever a Zombie you control enters, you may pay {W}: return this from your graveyard to your hand") |
| `GRAVEYARD_ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD` | Like ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD but fires from the controller's graveyard. Wrap in `TriggeringCardConditionalEffect(CardTypePredicate(ARTIFACT), inner)` to gate on the entering artifact. A `MayEffect` inner is queued as a may-ability; anything else goes on the stack. Scanned in `TriggerCollectionService.checkAllyArtifactEntersTriggers`. Used by Ovalchase Daredevil ("whenever an artifact you control enters, you may return this card from your graveyard to your hand") |
| `ON_ALLY_CREATURE_ATTACKS` | Fires once per attacking creature the controller controls (unlike ON_ALLY_CREATURES_ATTACK which fires once per combat). Scans all controller's permanents for each attacker. Supports `TriggeringCardConditionalEffect` (filter by the attacking creature's card) and `TriggeringPermanentConditionalEffect` (filter by the attacking permanent, e.g. "with a +1/+1 counter on it"). Mandatory effects go on the stack sourced by the ability's owner (attacked target captured for `DealDamageToAttackedTargetEffect`). A `MayEffect` is queued as a CR 603.5 resolution-time may whose source **permanent** is the *attacking* creature ("that creature") while the source **card** is the ability's owner — so the owner's card-level `target(...)` filter governs legal targets (give it a `PermanentPredicateTargetFilter(new PermanentIsPlaneswalkerPredicate())` for player-or-planeswalker damage). Used by Sanctum Seeker (Vampire drain), Hellrider (attacked-target damage), Rage Forger (counter-bearing attacker may ping a player/planeswalker) |
| `ON_ALLY_CREATURE_ATTACKS_UNBLOCKED` | Fires once per **unblocked** attacking creature the controller controls, during the declare-blockers step (both when the defender declares blocks and when no blockers exist). Supports `TriggeringCardConditionalEffect` to filter by the unblocked creature. The unblocked creature is set as the trigger's non-targeting `sourcePermanentId`, so self-scoped effects like `BoostSelfEffect` apply to "it" (the unblocked creature), not the source. Checked in `CombatBlockService`. Used by Stinkdrinker Bandit (Rogues get +2/+1) |
| `ON_CREATURE_ATTACKS_YOU` | Whenever a creature attacks you or a planeswalker you control. Fires once per attacking creature, on the defending player's permanents (the player being attacked, directly or via their planeswalker). The attacking creature's permanent ID is set as the non-targeting `targetId` on the stack entry. Checked in `CombatAttackService.declareAttackers`. Used by Lost in the Woods |
| `ON_CREATURES_ATTACK_YOU` | Whenever one or more creatures attack you. Fires **once per combat** (not per creature), on the attacked player's permanents, and only for creatures attacking that player directly — attacking a planeswalker they control does not count (unlike `ON_CREATURE_ATTACKS_YOU`). No `targetId` is set; scale the effect with `PermanentCount(PermanentIsAttackingSourceControllerPredicate(), CountScope.ANY_PLAYER)`. Checked in `CombatAttackService.declareAttackers`. Used by Orim's Prayer |
| `ON_OPPONENT_ATTACKS_PLANESWALKER_YOU_CONTROL` | Whenever an opponent attacks a planeswalker you control with one or more creatures. Fires once per combat on the defending player's permanents; the attacking player's ID is set as the non-targeting `targetId`. Checked in `CombatAttackService.declareAttackers`. |
| `ON_ANY_PLAYER_ATTACKS` | Whenever ANY player attacks with one or more creatures. Fires once per combat (not per creature), on every permanent with this slot across all battlefields. The attacking player is set as the non-targeting `targetId`, so player-scoped effects (e.g. `DestroyAllPermanentsEffect` with `EachPermanentScope.TARGET_PLAYER`) act on "that player". Checked in `CombatAttackService.declareAttackers`. Used by Total War |
| `ON_ANY_CREATURE_ATTACKS` | Whenever ANY creature attacks (any controller, any defender). Fires once per attacking creature, on every permanent with this slot across all battlefields. The attacking creature is set as the non-targeting `targetId`, so a plain `DealDamageToTargetCreatureEffect` hits "it". Checked in `CombatAttackService.declareAttackers`. Supports `TriggeringPermanentConditionalEffect` to restrict which attackers trigger it (Windreader Sphinx — flying attackers only). Used by Caltrops |
| `ON_OPPONENT_CREATURE_BECOMES_TARGET_OF_YOUR_SPELL_OR_ABILITY` | Whenever a creature an OPPONENT controls becomes the target of a spell or ability YOU control. Fires on all permanents with this slot on the spell/ability controller's battlefield. Targeted creature set as non-targeting `targetId`, listening permanent as `sourcePermanentId`. Used by Willbreaker (`GainControlOfTargetEffect(WHILE_SOURCE_ON_BATTLEFIELD)`) |
| `ON_ANY_CREATURE_BECOMES_TARGET_OF_SPELL_OR_ABILITY` | Whenever ANY creature (any controller) becomes the target of ANY spell or ability. Fires on ALL permanents with this slot across every battlefield. The targeted creature is set as the non-targeting `targetId`. Checked in `TriggerCollectionService.checkBecomesTargetOfSpellTriggers`/`checkBecomesTargetOfAbilityTriggers`. Used by Cowardice (`ReturnToHandEffect.target()`) |
| `ON_ALLY_CREATURE_EXPLORES` | Whenever a creature you control explores. Fires after the explore process completes (land into hand, or +1/+1 counter and may-graveyard choice). Supports `TriggeringCardConditionalEffect` to inspect the revealed card, plus targeted effects (e.g. BoostTargetCreatureEffect) via `ExploreTriggerTarget` queue — targets restricted to opponent's creatures. Used by Lurking Chupacabra and Nicanzil, Current Conductor |
| `ON_EXPLOIT` | When this permanent exploits a creature (CR 702.110). Fired after a successful `ExploitEffect` sacrifice while the source was on the battlefield at resolution start (self-sac still counts). Stack-targeting effects (e.g. `CounterSpellEffect`) use `ExploitTriggerTarget` — pair with `StackEntryHasTargetPredicate` to include activated/triggered abilities. Used by Overcharged Amalgam |
| `ON_BLOCK` | This creature blocks |
| `ON_BECOMES_BLOCKED` | This creature becomes blocked. Register effects with `TriggerMode.PER_BLOCKER` to fire once per blocker |
| `ON_ATTACKS_UNBLOCKED` | This creature attacks and isn't blocked. Fires once per unblocked attacker during the declare-blockers step (after blocks are declared, or immediately if the defender can't block) — before combat damage, and independent of whether damage is dealt. Player-affecting effects read the defending player from the non-targeting `targetId`. Checked in `CombatBlockService`. Used by Abyssal Nightstalker |
| `ON_ENCHANTED_CREATURE_ATTACKS_UNBLOCKED` | Aura slot: the creature this aura is attached to attacks and isn't blocked. Fires alongside `ON_ATTACKS_UNBLOCKED` in `CombatBlockService.collectUnblockedAttackTriggers` (scanning auras attached to each unblocked attacker); the enchanted attacker is baked as the non-targeting `sourcePermanentId` and the defending player as `targetId`. Used by Cloak of Confusion (`AssignNoCombatDamageAndDefendingPlayerDiscardsEffect`) |
| `ON_ALLY_CREATURE_BECOMES_BLOCKED` | Whenever a creature you control becomes blocked. Fires once per blocked attacker, on every permanent with this slot on the blocked creature's controller's battlefield. Non-targeting effects use the blocked creature as `sourcePermanentId`, so self-scoped effects like `BoostSelfEffect` apply to "it". Effects with permanent/player targets use the shared `AttackTriggerTarget` pipeline and the watching permanent as `sourcePermanentId`. Wrap in `TriggeringCardConditionalEffect` to filter by the blocked creature. Checked in `CombatBlockService`. Used by Unstoppable Ash and Close Quarters |
| `ON_ANY_CREATURE_BLOCKS` | Global watcher: whenever ANY creature blocks, regardless of controller. Fires once per blocking creature (not per attacker blocked) on every permanent with this slot across all battlefields; the blocker is baked in as the non-targeting `targetId`, so `LoseLifeEffect(N, TARGET_PERMANENT_CONTROLLER)` is "that creature's controller". Used by Carnage Gladiator |
| `ON_ANY_CREATURE_BECOMES_BLOCKED` | Global watcher: whenever ANY creature becomes blocked, regardless of controller. Fires once per attacker/blocker pair on every permanent with this slot across all battlefields. Effects must implement `BlockPairConditionalEffect`; `CombatBlockService` evaluates `firesForPair(attackerPower, blockerPower)` at trigger time and bakes the `actsOn()` participant in as the non-targeting `targetId` (attacker as `sourcePermanentId`). Used by No Quarter |
| `ON_ANY_CREATURES_BLOCK` | Global watcher: whenever one or more creatures block. Fires **once per block declaration** (not per pair, unlike `ON_ANY_CREATURE_BECOMES_BLOCKED`) on every permanent with this slot across all battlefields, and only when at least one creature blocked. No `targetId` is set — effects read the board's blocking state themselves (`PermanentIsBlockingPredicate` / `PermanentIsBlockedPredicate`). Checked in `CombatBlockService`. Used by Tide of War |
| `ON_ANY_PERMANENT_RETURNED_TO_HAND` | Whenever a permanent is returned to a player's hand (bounced from the battlefield). Fires on every permanent with this slot across all battlefields, once per returned permanent. The owner the permanent returned to is the non-targeting `targetId`, so a player-directed effect (e.g. `DiscardEffect(1, TARGET_PLAYER)`) acts on "that player". Fired from `PermanentRemovalService.removePermanentToHand` via `TriggerCollectionService.checkPermanentReturnedToHandTriggers`. Used by Warped Devotion |
| `ON_CONTROLLER_CREATURE_RETURNED_TO_HAND` | Whenever a creature is returned from the battlefield to this permanent's controller's hand. Fires once per matching creature, including the watching permanent itself, before removal. Fired from `PermanentRemovalService.removePermanentToHand` via `TriggerCollectionService.checkControllerCreatureReturnedToHandTriggers`. Used by Stormfront Riders |
| `ON_COMBAT_DAMAGE_TO_PLAYER` | This creature deals combat damage to a player. Fires once per combat damage step, so double strike can trigger in both first-strike and regular damage steps |
| `ON_SELF_DEALS_COMBAT_DAMAGE_TO_PLAYER_OR_BATTLE` | This permanent itself deals combat damage to a player or battle. Uses the targeted combat-damage trigger flow, so an optional permanent target can be chosen when the trigger is put on the stack |
| `ON_COMBAT_DAMAGE_TO_CREATURE` | This creature deals combat damage to a creature. Fires once per damaged creature; the damaged creature is baked as non-targeting `targetId` so effects like `DestroyTargetPermanentEffect` / `PutCounterOnTargetPermanentEffect` act on "that creature" |
| `ON_DAMAGE_TO_PLAYER` | Any damage to a player (not just combat) |
| `ON_DEATH` | This permanent dies |
| `ON_SACRIFICE` | This permanent is sacrificed |
| `ON_ALLY_CREATURE_ENTERS_BATTLEFIELD` | A creature (including tokens) enters battlefield under your control |
| `ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD` | "Whenever this creature or another creature you control enters" — same scan as `ON_ALLY_CREATURE_ENTERS_BATTLEFIELD` minus the self-exclusion, so the source's own entry also fires it. The entering permanent rides along as the trigger's `triggeringPermanentId` (through `EntersTriggerTarget` for targeted effects), so "that creature" is resolvable. Gruul Ragebeast (`EnteringCreatureFightsTargetCreatureEffect`) |
| `ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD` | A nontoken creature enters battlefield under your control (not this permanent, not tokens). Used with MayPayManaEffect for Minion Reflector's copy trigger, or mandatory `CreateTokenCopyOfTargetPermanentEffect` (optionally gated by `TriggeringCardConditionalEffect`) for Necroduality. Entering permanent ID is baked as stack `targetId` / PendingMayAbility.targetCardId so the copy effect knows which creature to copy |
| `ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD` | An artifact enters battlefield under your control (not this permanent). Supports `TriggeringCardConditionalEffect` (e.g. Blood token gate) and intervening-if `ControlsPermanentCount` (checked at trigger time; ConditionalEffect left wrapped for resolution re-check). |
| `ON_ALLY_NONTOKEN_ARTIFACT_ENTERS_BATTLEFIELD` | A nontoken artifact enters battlefield under your control (not this permanent). Used with MayPayManaEffect for Mirrorworks' copy trigger. Entering permanent ID is passed via PendingMayAbility.targetCardId |
| `ON_ANY_CREATURE_DIES` | Any creature (including tokens) on any battlefield dies. Fires for all permanents on all battlefields. Supports targeted effects via DeathTriggerTarget (e.g. Falkenrath Noble). Unwraps `TriggeringPermanentConditionalEffect` against the dying permanent — the predicate sees the dying creature's on-battlefield state incl. counters at death (e.g. Blowfly Infestation's "if it had a -1/-1 counter on it"). Wrap in `OncePerTurnTriggerEffect` for "triggers only once each turn" (Morbid Opportunist). `DealDamageToPlayersEffect(TRIGGERING_PERMANENT_CONTROLLER)` bakes the dying creature's controller as `targetId` (Dingus Staff) |
| `ON_ANY_NONTOKEN_CREATURE_DIES` | Any nontoken creature on any battlefield dies (not just controller's). Used with MayEffect for Mimic Vat's imprint trigger; wrap in `OncePerTurnTriggerEffect` for "triggers only once each turn" (Ghoulish Procession) |
| `ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD` | Any artifact (any player's) is put into a graveyard from the battlefield. Fires for destroy, sacrifice, etc. |
| `ON_ANOTHER_NONTOKEN_ARTIFACT_PUT_INTO_GRAVEYARD_OR_EXILE_FROM_BATTLEFIELD` | Another nontoken artifact you control is put into a graveyard or exile from the battlefield. Controller-scoped watcher; does not fire for tokens, bounce, tuck, or the source artifact itself. |
| `ON_ANY_ENCHANTMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD` | Any enchantment (any player's) is put into a graveyard from the battlefield. Fires for destroy, sacrifice, etc. Used by Femeref Enchantress with `DrawCardEffect`. |
| `ON_ALLY_ENCHANTMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD` | An enchantment controlled by this permanent's controller is put into a graveyard from the battlefield. Fires for destroy, sacrifice, etc.; uses the dying enchantment's controller rather than its owner. Used by Starfield Mystic with `PutCountersOnSourceEffect(1, 1, 1)`. |
| `ON_ARTIFACT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD` | An artifact is put into an opponent's graveyard from the battlefield. Only fires when the graveyard owner is an opponent of this permanent's controller. Supports MayEffect wrapping. |
| `ON_ANY_LAND_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD` | Any land (any player's) is put into a graveyard from the battlefield. Fires for destroy, sacrifice, etc. Used by Dingus Egg with `DealDamageToPlayersEffect(2, TRIGGERING_PERMANENT_CONTROLLER)` — target pre-set to the land's controller at trigger time. Any other effect uses the generic collector (no target pre-set), e.g. Akki Raider with `BoostSelfEffect(1, 0)`. |
| `ON_OPPONENT_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD` | A permanent of **any** type an opponent of this permanent's controller controls is put into a graveyard from the battlefield. Fired once per removal in `PermanentRemovalService.processGraveyardAndTriggers` via `TriggerCollectionService.checkOpponentPermanentPutIntoGraveyardTriggers`; the collector bakes the dying card id + that opponent's id into the effect. Used by Prince of Thralls with `StealDyingOpponentPermanentUnlessPaysLifeEffect(3)`. |
| `ON_OTHER_PLAYER_OWNED_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD` | A permanent of **any** type **owned** by a player other than this permanent's controller is put into a graveyard from the battlefield. Ownership-based, not control-based (a stolen permanent still counts for its owner). Fired in `PermanentRemovalService.processGraveyardAndTriggers` via `TriggerCollectionService.checkOtherPlayerOwnedPermanentPutIntoGraveyardTriggers`. Used by Kothophed, Soul Hoarder with `SequenceEffect.of(DrawCardEffect(1), LoseLifeEffect(1))`. |
| `ON_ALLY_CREATURE_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE` | A creature card is put into your graveyard from anywhere (battlefield, hand, library, stack, exile). Uses printed card types (tokens never fire; a creature card that was a noncreature permanent still does). Fires on permanents the graveyard owner controls. Checked in `GraveyardService.addCardToGraveyard`. Used by Soulcipher Board (`SequenceEffect` of `RemoveCounterFromSourceEffect(OMEN, 1)` + `ConditionalEffect(NotCondition(SourceCounterThreshold(1, OMEN)), TransformSelfEffect)`). |
| `ON_BLACK_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE` | A black card is put into an opponent's graveyard from anywhere (battlefield, hand, library, stack, exile). Only fires on permanents controlled by an opponent of the graveyard owner. Checked in `GraveyardService.addCardToGraveyard`. Supports MayEffect wrapping. Used by Compost. |
| `ON_CREATURE_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE` | A creature card is put into an opponent's graveyard from anywhere (battlefield, hand, library, stack, exile). Only fires on permanents controlled by an opponent of the graveyard owner; printed types, so tokens never trigger. Checked in `GraveyardService.addCardToGraveyard` via `TriggerCollectionService.checkCreatureCardPutIntoGraveyardFromAnywhereTriggers`. Used by Profane Memento. |
| `ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD` | Any other creature enters battlefield |
| `ON_PERMANENT_ENTERS_FROM_GRAVEYARD` | Any permanent (not just creatures) enters from ANY graveyard, checked via `enteredFromGraveyardOwnerId`. Queues a non-targeting stack entry for the source's controller (`TriggerCollectionService.checkPermanentEntersFromGraveyardTriggers`). Used by River Kelpie. Contrast `ON_CREATURE_ENTERS_FROM_GRAVEYARD` (Flayer of the Hatebound): creatures-only, controller's graveyard only, any-target pipeline |
| `ON_SELF_ENTERS_FROM_GRAVEYARD` | "When this creature enters from a graveyard" — fires **only for the entering permanent itself**, never for other permanents (`TriggerCollectionService.checkSelfEntersFromGraveyardTriggers`). A targeting effect picks its target as the ability goes on the stack (CR 603.3b) through the shared `ETBTokenTargetTrigger` pipeline, using the card's `target(...)` filter; a non-targeting effect is queued straight onto the stack. Deliberately separate from `ON_ENTER_BATTLEFIELD` so a normal cast never asks for a target. Used by Treacherous Pit-Dweller |
| `ON_ANY_PERMANENT_ENTERS_BATTLEFIELD` | "Whenever a player puts a permanent onto the battlefield" — fires for EVERY permanent entering under ANY player's control (any type, including the source itself), once per entering permanent, for every permanent on any battlefield carrying this slot. Each effect in the slot is queued as its own triggered ability. The entering permanent's controller is the non-targeting `targetId`, so a player-directed effect (`SacrificePermanentsEffect(..., SacrificeRecipient.TARGET_PLAYER)`) acts on "that player". Restrict which permanents trigger it with a `TriggeringCardConditionalEffect` wrapper. `TriggerCollectionService.checkAnyPermanentEntersTriggers`. Used by Nature's Wrath |
| `ON_ALLY_CREATURE_DIES` | A creature you control dies. Supports `TriggeringCardConditionalEffect` wrapping to filter by dying creature predicates (e.g. Slimefoot only triggers for Saprolings, Requiem Angel for non-Spirits) |
| `ON_ALLY_CREATURE_OR_PLANESWALKER_DIES` | A creature **or planeswalker** you control dies. Same collection shape as `ON_ALLY_CREATURE_DIES` (MayEffect dispatched to a may-ability, other effects batched onto one stack entry) but fired for planeswalker deaths too, and only once for a permanent that is both. Ajani's Last Stand |
| `ON_ALLY_NONTOKEN_CREATURE_DIES` | A nontoken creature you control dies. Only fires for nontoken creatures (tokens are excluded). Used by Gutter Grime |
| `ON_ALLY_NONTOKEN_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD` | A nontoken permanent of any type is put into your graveyard from the battlefield. Fires only for the graveyard owner, including permanents you do not control, and excludes tokens. Used by Jinxed Ring with `DealDamageToPlayersEffect(1, CONTROLLER)`. |
| `ON_ANY_NONTOKEN_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD` | A nontoken permanent of any type is put into any player's graveyard from the battlefield. Fires for every graveyard owner and bakes that owner as the trigger's target. Used by Liability with `LoseLifeEffect(1, TARGET_PLAYER)`. |
| `ON_ALLY_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD` | A permanent of any type, including a token, is put into your graveyard from the battlefield. Fires only for the graveyard owner. Used by Scrapheap with `TriggeringCardConditionalEffect(CardAnyOfPredicate(ARTIFACT, ENCHANTMENT), GainLifeEffect(1))`. |
| `ON_DAMAGED_CREATURE_DIES` | A creature damaged by this permanent dies |
| `ON_ANY_PLAYER_CASTS_SPELL` | Any player casts a spell |
| `ON_CONTROLLER_CASTS_SPELL` | Controller casts a spell ("whenever you cast...") |
| `ON_CONTROLLER_COPIES_SPELL` | Controller copies an instant or sorcery spell |
| `ON_ANY_PLAYER_TAPS_LAND` | Any player taps a land |
| `ON_CONTROLLER_TAPS_CREATURE_FOR_MANA` | The controller taps a creature they control for mana |
| `ON_SELF_TAPPED_FOR_MANA` | You tap **this** permanent for mana. Dispatched from the mana-ability tap path only (`TriggerCollectionService.checkSelfTappedForManaTriggers`), so it never fires when the permanent is tapped to attack or by an opponent. Deferred into `pendingManaAbilityTriggers` like every other mana-ability trigger (CR 603.3). Used by Zhur-Taa Druid (`DealDamageToPlayersEffect(1, EACH_OPPONENT)`) |
| `ON_OPPONENT_PERMANENT_BECOMES_TAPPED` | A permanent an opponent of the controller controls becomes tapped (any tap — for mana or forced). Opponent-scoped counterpart of `ON_ALLY_PERMANENT_BECOMES_TAPPED`; wrap in `TriggeringPermanentConditionalEffect` to filter the tapped permanent. Both tap slots set `StackEntry.triggeringPermanentId` to the tapped permanent (a non-target reference, never validated or fizzled) so `PutCounterOnReferencedPermanentEffect(PermanentReference.TRIGGERING, …)` can act on "it" — Freyalise's Winds uses both slots for "whenever *a* permanent becomes tapped". When the resolved effect is `DealDamageToPlayersEffect(…, TRIGGERING_PERMANENT_CONTROLLER)`, the collector also bakes the tapped permanent's controller as `targetId` (Royal Decree); otherwise `targetId` stays null so may-target tap triggers (Surgespanner) still choose at resolution. Used by Thoughtleech |
| `ON_ENCHANTED_PERMANENT_TAPPED` | The permanent this aura is attached to becomes tapped. Does NOT fire for "enters tapped" (CR 603.6d). `affectedPlayerId` is baked in at trigger time with the enchanted permanent's controller. Effects: `GivePoisonCountersEffect` (Relic Putrescence), `DestroyReferencedPermanentEffect(ATTACHED)` (Spreading Algae — "destroy it"), `DealDamageToPlayersEffect(N, TRIGGERING_PERMANENT_CONTROLLER)` (Psychic Venom — "deals N damage to that land's controller"; collector bakes the target to the tapped permanent's controller), `MillEffect(N, TARGET_PLAYER)` (Chronic Flooding — "its controller mills N cards"; collector bakes the tapped permanent's controller as `targetId`), `RemoveCounterFromSourceThenDestroyEnchantedAtZeroEffect` (Orcish Mine — also on `UPKEEP_TRIGGERED`), `DrawCardEffect` (Betrayal — "you draw a card"; controller is the Aura's controller). When the land taps for mana the trigger is deferred into `pendingManaAbilityTriggers` (CR 603.3) until a player next receives priority |
| `ON_ENCHANTED_CREATURE_DEALT_DAMAGE` | The creature this aura is attached to is dealt damage (combat or non-combat). Damage amount passed via `TriggerContext.DamageToCreature` and snapshotted to xValue for "that much damage" effects. Effects: `EnchantedCreatureDealsDamageEqualToDealtDamageToControllerEffect` (Spiteful Shadows / Binding Agony), `DestroyReferencedPermanentEffect(ATTACHED)` (Mortal Wound), `EnchantedCreatureControllerLosesLifeEffect(0)` (Ragged Veins — life loss equal to the damage) |
| `ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU` | Any permanent deals damage to this permanent's controller |
| `ON_CREATURE_DEALS_COMBAT_DAMAGE_TO_YOU` | "Whenever a creature deals combat damage to you, ..." — a watcher on the *damaged* side, creature- and combat-only. Fired from `TriggerCollectionService.checkDamageDealtToControllerTriggers` (combat branch) via `queueCreatureCombatDamageToYouTriggers`: the whole slot becomes ONE triggered ability per watching permanent, pushed on the stack with `targetId` = the damaging creature and `setNonTargeting(true)`, so `DestroyTargetPermanentEffect()` reads as "destroy that creature" without targeting (CR 115.10a). Unlike `ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU` (resolves immediately, any permanent source, combat or not) this uses the stack. Teysa, Envoy of Ghosts (`DestroyTargetPermanentEffect()` + `CreateTokenEffect(...)`) |
| `ON_CONTROLLER_DEALT_DAMAGE` | "Whenever you're dealt damage, ..." — this permanent's controller is dealt damage (combat or non-combat, from any source incl. spells/abilities). Unlike `ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU` (reacts to the source, permanent sources only), this carries only the amount, snapshotted onto the trigger's `eventValue`. Read it with an `EventValue` amount: `PutCountersOnSelfEffect(CounterType.VITALITY, new EventValue())` ("put that many counters" — Living Artifact). Fires once per source (CR: simultaneous sources trigger separately) from `CombatDamageService` (per source) and `DamageSupport` (non-combat) |
| `ON_CREATURE_DEALS_DAMAGE_TO_YOU_OR_YOUR_PERMANENT` | "Whenever a creature of the chosen color deals damage to you or a white creature you control, ..." — a watcher on the *damaged* side. Scans the damaged player's battlefield; the watcher's `Permanent.getChosenColor()` gates which damaging creatures qualify and the effect's `damagedPermanentFilter` gates the "or a … you control" half (damage to the player themself always qualifies). Fires once per damaging creature per damage event from all four choke points (combat to player + to creature, non-combat to player + to creature) via `TriggerCollectionService.checkCreatureDamageToYouOrYourPermanentTriggers`. Used by Mangara's Equity (`ReflectDamageToChosenColorCreatureEffect`) |
| `ON_ALLY_SOURCE_DEALS_DAMAGE_TO_OPPONENT` | "Whenever a source you control deals damage to another player, ..." — the outbound mirror of `ON_CONTROLLER_DEALT_DAMAGE`: it scans the **damaging** source's controller's battlefield and only fires when the damaged player is someone else. Fires once per source, amount snapshotted onto `eventValue` and read by an `EventValue` amount. Driven from the same two player-damage choke points via `TriggerCollectionService.checkAllySourceDealtDamageToOpponentTriggers`. `PutCountersOnSelfEffect(CounterType, true)` excludes damage dealt by the watching permanent itself for "another source" wording (Talon of Pain). `TriggeringPermanentConditionalEffect` may filter the damaging permanent and use `PutCounterOnReferencedPermanentEffect(PermanentReference.TRIGGERING, ...)` to act on it. Used by Night Dealings (`PutCountersOnSelfEffect(CounterType.THEFT, new EventValue())`), which does not exclude its source |
| `ON_CONTROLLER_DEALT_DAMAGE_BY_OPPONENT` | "Whenever a source an opponent controls deals damage to you, ..." — like `ON_CONTROLLER_DEALT_DAMAGE` but gated so only damage from a source an **opponent** controls fires it (the controller's own self-damage is ignored). Same `eventValue` snapshot / `EventValue` read. Opponent gate applied in `TriggerCollectionService.checkControllerDealtDamageTriggers` from the source controller the two choke points pass (combat = active player; non-combat = the spell/ability's controller). Register the trigger on `MayEffect.class` for "you may" wording. Used by Retaliator Griffin (`MayEffect(PutCountersOnSelfEffect(PLUS_ONE_PLUS_ONE, new EventValue()))`) |
| `ON_ANY_SOURCE_DEALS_DAMAGE` | Global watcher: any source (creature or spell) deals damage to anything. Fires on every permanent with this slot across all battlefields; carries `TriggerContext.SourceDealsDamage` (source card, source controller, summed total). Damage one source deals simultaneously is summed into one trigger. Effect: `ReflectSourceDamageToItsControllerEffect(color)` (Justice). Driven from `CombatDamageService` (per source) and `EffectResolutionService` (non-combat flush) |
| `ON_SELF_DEALS_DAMAGE` | "Whenever this creature deals damage, ..." — fires only for the damage **this** permanent deals (combat or non-combat, to a creature/player/planeswalker). Shares the summed choke point behind `ON_ANY_SOURCE_DEALS_DAMAGE` but keyed off the source card, so it still triggers when the source dies dealing that damage; the summed total is snapshotted onto the trigger's `eventValue`. Read it with an `EventValue` amount: `GainLifeEffect(new EventValue())` ("you gain that much life" — El-Hajjâj) |
| `ON_ALLY_INSTANT_OR_SORCERY_DEALS_DAMAGE` | "Whenever an instant or sorcery spell you control deals damage, ..." — fires once per damage event no matter how many objects the spell damaged simultaneously, only for spells their controller controls. Shares the batched non-combat choke point behind `ON_ANY_SOURCE_DEALS_DAMAGE`; the summed total is snapshotted onto the trigger's `eventValue`. Effects that care specifically about damage to an opponent can use the recipient-aware damage map (Satyr Firedancer). Blaze Commando, Satyr Firedancer |
| `ON_SELF_DEALS_DAMAGE` | "Whenever this creature deals damage, ..." — fires only for the damage **this** permanent deals (combat or non-combat, to a creature/player/planeswalker). Shares the summed choke point behind `ON_ANY_SOURCE_DEALS_DAMAGE` but keyed off the source card, so it still triggers when the source dies dealing that damage; the summed total is snapshotted onto the trigger's `eventValue`. Read it with an `EventValue` amount: `GainLifeEffect(new EventValue())` ("you gain that much life" — El-Hajjâj)  Granted instances of this slot (`Permanent.addTemporaryTriggeredEffect`/`addPersistentTriggeredEffect`) are collected too — the Genju cycle grants the animated land "whenever this creature deals damage, its controller gains that much life" until end of turn.|
| `ON_ALLY_PERMANENT_SACRIFICED` | A permanent you control is sacrificed (not this one — "another") |
| `ON_ANY_CREATURE_SACRIFICED` | Global watcher: any player sacrifices a creature ("Whenever a player sacrifices a creature"). Fires on every permanent with this slot across all battlefields, once per sacrificed creature (last-known info); the trigger belongs to the scanning permanent's controller. A wrapped `MayEffect(PutCountersOnSourceEffect(1,1,1))` resolves onto the source (like Scavenger Drake's `ON_ANY_CREATURE_DIES`). Fired from both sacrifice choke points (`DestructionSupport.sacrificeAndLog` for edict/chosen sacrifices, `checkAllyPermanentSacrificedTriggers` for sacrifice-self / sacrifice-as-cost). Used by Thraximundar |
| `ON_BECOMES_TARGET_OF_SPELL` | This permanent becomes target of a spell |
| `ON_BECOMES_TARGET_OF_AURA_SPELL` | This permanent becomes the target of an Aura spell (any controller's). Used by Fugitive Druid |
| `ON_BECOMES_TARGET_OF_OPPONENT_SPELL` | This permanent becomes target of an opponent's spell |
| `ON_BECOMES_TARGET_OF_OPPONENT_SPELL_ONLY` | This permanent becomes target of an opponent's spell, but not an ability |
| `ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY` | This permanent becomes target of any spell or ability |
| `ON_ALLY_CREATURE_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY` | Global monitor: a creature you control becomes target of opponent's spell/ability. Used by Shapers' Sanctuary |
| `ON_ALLY_PERMANENT_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY` | Global monitor: a permanent you control becomes target of opponent's spell/ability. Player targets do not count. Used by Battle Mammoth |
| `ON_ALLY_CREATURE_OR_CREATURE_SPELL_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY` | Global monitor: a creature or creature spell you control becomes target of opponent's spell/ability. Used by Surrak, Elusive Hunter |
| `ON_EQUIPPED_CREATURE_DIES` | Equipped creature dies |
| `ON_EQUIPPED_CREATURE_TRANSFORMS` | Equipped creature transforms (either direction) |
| `ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD` | Enchanted permanent dies (graveyard only) |
| `ON_ENCHANTED_PERMANENT_LEAVES_BATTLEFIELD` | Enchanted permanent leaves battlefield (any destination) |
| `ON_OPPONENT_LAND_ENTERS_BATTLEFIELD` | Opponent's land enters. Wrap with `ConditionalEffect(new PermanentEnteredThisTurn(predicate, minCount), wrapped)` for "second+ land" |
| `ON_ALLY_LAND_ENTERS_BATTLEFIELD` | Your land enters (landfall) |
| `ON_CONTROLLER_PLAYS_LAND` | You **play** a land ("whenever you play a land"; land-play special action only — not every land ETB). Fired from the land-play sites (hand, graveyard, exile, free-play) via `TriggerCollectionService.checkControllerPlaysLandTriggers`, so a land put onto the battlefield by an effect does NOT trigger it — use `ON_ALLY_LAND_ENTERS_BATTLEFIELD` for landfall. Pair with `ON_CONTROLLER_CASTS_SPELL` to cover "whenever you play a card" (Search the City, Juju Bubble) |
| `ON_OPPONENT_PLAYS_LAND` | An **opponent** plays a land ("whenever an opponent plays a land"). Opponent-side mirror of `ON_CONTROLLER_PLAYS_LAND`, fired from the same land-play sites, so an opponent's land put onto the battlefield by an effect does NOT trigger it — use `ON_OPPONENT_LAND_ENTERS_BATTLEFIELD` for that. Dirtcowl Wurm |
| `GRAVEYARD_ON_COMBAT_DAMAGE_TO_YOU_OR_YOUR_PLANESWALKER` | Combat damage is dealt to the controller or to a planeswalker they control, while this card is in their graveyard. Fires once per combat damage step per damaged player in `CombatDamageService.checkGraveyardCombatDamageToYouOrPlaneswalkerTriggers`. Unlike every other graveyard slot this one **targets**: the trigger is routed through the `AttackTriggerTarget` pending-choice pipeline, so the card's `target(...)` filter narrows the legal targets and the ability is skipped when none exist (CR 603.3c). Used by Vengeful Pharaoh (`ConditionalEffect(SourceCardInGraveyard, DestroyTargetPermanentEffect)` + `PutSourceCardFromGraveyardIntoLibraryNFromTopEffect(0)`, `TargetFilters.attackingCreature()`) |
| `GRAVEYARD_ON_ALLY_LAND_ENTERS_BATTLEFIELD` | Like ON_ALLY_LAND_ENTERS_BATTLEFIELD but fires from the controller's graveyard. Wrap in `TriggeringCardConditionalEffect(new CardSubtypePredicate(...), wrapped)` to filter by the entering land, and wrap the inner effect in `MayEffect(ReturnCardFromGraveyardEffect.builder().destination(HAND).filter(new CardIsSelfPredicate()).build(), ...)` for "you may return this card from your graveyard to your hand". Scanned over the land controller's graveyard in `TriggerCollectionService.checkAllyLandEntersTriggers`. Used by Reach of Branches ("whenever a Forest you control enters") |
| `ON_OPPONENT_CREATURE_DIES` | An opponent's creature dies |
| `ON_DEALT_DAMAGE` | This creature is dealt damage (combat or non-combat). The damage amount is snapshotted onto the queued entry's `eventValue`, so "it deals that much damage" effects can read it with an `EventValue` amount (Stuffy Doll) |
| `ON_OPENING_HAND_REVEAL` | First upkeep, cards in hand (Chancellor cycle). Wrap with `MayEffect` |
| `ON_OPPONENT_LOSES_LIFE` | Opponent loses life (damage or direct) |
| `ON_ALLY_EQUIPMENT_ENTERS_BATTLEFIELD` | An Equipment enters under your control |
| `ON_OPPONENT_CREATURE_ENTERS_BATTLEFIELD` | An opponent's creature enters |
| `ON_OPPONENT_SHUFFLES_LIBRARY` | Opponent shuffles library |
| `ON_OPPONENT_SEARCHES_LIBRARY` | Opponent searches their own library; the searching player is baked in as `targetId` (Ob Nixilis, Unshackled) |
| `ON_CONTROLLER_GAINS_LIFE` | Controller gains life |
| `ON_OPPONENT_DEALT_NONCOMBAT_DAMAGE` | Opponent dealt noncombat damage |
| `GRAVEYARD_ON_OPPONENT_DAMAGED_BY_RED_SPELL_OR_PLANESWALKER` | Opponent dealt damage by your red instant/sorcery spell or red planeswalker, fired from your graveyard |
| `ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER` | A creature you control deals combat damage to a player |
| `ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER_OR_BATTLE` | One or more matching creatures you control deal combat damage to a player or battle; `oneOrMoreDealers=true` batches separately for each damaged player or battle and passes the matching dealer ids through `CombatDamageDealerAwareEffect` wrappers |
| `ON_SELF_MILLED` | This card is milled into graveyard |
| `STATE_TRIGGERED` | State-triggered ability (rule 603.8). Fires when predicate is true, won't retrigger while on stack |
| `SAGA_CHAPTER_I` | Saga chapter I (first lore counter placed, on ETB and precombat main) |
| `SAGA_CHAPTER_II` | Saga chapter II (second lore counter) |
| `SAGA_CHAPTER_III` | Saga chapter III (third lore counter, saga sacrificed after) |
| `BEGINNING_OF_COMBAT_TRIGGERED` | Beginning of combat on controller's turn |
| `EACH_BEGINNING_OF_COMBAT_TRIGGERED` | Beginning of each combat (any player's turn) |
| `OPPONENT_BEGINNING_OF_COMBAT_TRIGGERED` | Beginning of combat on each opponent's turn only (never the controller's) — Sentinel of the Eternal Watch |
| `END_OF_COMBAT_TRIGGERED` | "At end of combat" — as the end of combat step begins (CR 511.2), every combat, any player's turn |
| `PRECOMBAT_MAIN_TRIGGERED` | Beginning of precombat main phase on controller's turn |
| `EACH_PRECOMBAT_MAIN_TRIGGERED` | Beginning of each player's first main phase (any player's turn). Stack entry is controlled by the source's controller with the active player carried as `targetId` |
| `POSTCOMBAT_MAIN_TRIGGERED` | Beginning of each postcombat main phase on controller's turn |
| `ON_OPPONENT_CREATURE_DEALT_DAMAGE` | An opponent's creature is dealt damage. Layer-aware: a planeswalker or battle that is not also a creature does not fire it |
| `ON_OPPONENT_CREATURE_DEALT_EXCESS_NONCOMBAT_DAMAGE` | One or more creatures an opponent controls are dealt excess noncombat damage. Fires once per noncombat damage event and captures the damaged creature as a non-targeting reference |
| `ON_ANY_CREATURE_DEALT_DAMAGE` | Any creature (yours or an opponent's) is dealt damage — same layer-aware creature gate as above. Queued stack entry targets the damaged creature (targetId set, non-targeting). Register a target-taking effect like `DestroyTargetPermanentEffect(true)` — Death Pits of Rath |
| `ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE` | A creature you control (matching the effect's source filter) deals damage — combat or non-combat — to a creature; same layer-aware creature gate on the *damaged* permanent as the two slots above. Fires on the watcher, not the damaged creature; the damage-source creature reflects that much damage to the damaged creature's controller. Register `ReflectAllyDamageToDamagedCreatureControllerEffect(sourceFilter)` — Greatbow Doyen |
| `ON_ALLY_CREATURE_DEALS_COMBAT_DAMAGE` | A creature you control deals combat damage to anything — creature, player, or planeswalker. Fires on the watcher (which needn't be a creature), once per damage-dealing creature per combat damage step, with all that creature's simultaneous damage summed into one trigger (snapshotted as `eventValue`). Non-targeting, with the watcher bound as the trigger's source permanent so `PutCountersOnSelfEffect` lands on the watcher — Five-Alarm Fire |
| `ON_ALLY_CREATURE_DEALS_DAMAGE_TO_PLANESWALKER` | A creature you control deals damage to a planeswalker. Fires once per source and planeswalker damage event; the target planeswalker ID is baked into the non-targeting trigger entry, and the damaging creature is available as the non-targeting triggering permanent for referenced-permanent effects — Hooded Blightfang |
| `ON_CONTROLLER_LOSES_LIFE` | Controller loses life |
| `ON_SELF_LEAVES_BATTLEFIELD` | This permanent leaves the battlefield (any means) |
| `ON_ANOTHER_PERMANENT_LEAVES_BATTLEFIELD` | Another permanent (any type, any player's) leaves the battlefield by any means. Global watcher fired from every leave path in `PermanentRemovalService`; effects that need the departed permanent's identity implement `LeavingPermanentIdAwareEffect` |
| `ON_ANOTHER_CREATURE_LEAVES_BATTLEFIELD` | Another creature (any player's) leaves the battlefield by any means (destroy, exile, bounce, sacrifice, tuck) — broader than "dies". Global watcher fired from every leave path in `PermanentRemovalService` via `TriggerCollectionService.checkAnotherCreatureLeavesBattlefieldTriggers`; fires on every permanent with the slot except the leaving creature itself. Non-targeting: a "you may have target player mill two cards" is a `MayEffect(MillEffect(2, TARGET_PLAYER), …)` whose "may" and player target are resolved on the stack (Extractor Demon) |
| `ON_ANOTHER_ARTIFACT_LEAVES_BATTLEFIELD` | Another artifact **you control** leaves the battlefield by any means (destroy, exile, bounce, sacrifice, tuck). Controller-scoped watcher fired from every leave path in `PermanentRemovalService` via `TriggerCollectionService.checkAnotherArtifactLeavesBattlefieldTriggers`; fires only on the leaving artifact's controller's battlefield, except the leaving artifact itself. Pair with `ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD` (same effect on both slots) for "whenever another artifact you control enters or leaves the battlefield". Non-targeting: player target + "you may pay {1}" resolve on the stack via `MayPayManaEffect(SequenceEffect.of(LoseLifeEffect(TARGET_PLAYER), GainLifeEffect))` (Sludge Strider) |
| `ON_ALLY_CREATURE_LEAVES_BATTLEFIELD` | Another creature **you control** leaves the battlefield by any means. Controller-scoped sibling of `ON_ANOTHER_CREATURE_LEAVES_BATTLEFIELD`. Fired via `TriggerCollectionService.checkAllyCreatureLeavesBattlefieldTriggers`. Used by Luminous Phantom (`GainLifeEffect(1)`) |
| `ON_SELF_PUT_INTO_GRAVEYARD_FROM_ANYWHERE` | This card is put into a graveyard from anywhere (battlefield/hand/library/stack). Fired for every zone→graveyard transition in `GraveyardService.addCardToGraveyard` (card enters graveyard first, then trigger). Used by Purity with `ShuffleSelfFromGraveyardIntoLibraryEffect` |
| `ON_SELF_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD` | This card is put into a graveyard specifically **from the battlefield** ("dies" for a permanent). Fired in `GraveyardService.addCardToGraveyard` only when the source zone is `BATTLEFIELD` (card enters graveyard first, then trigger). Used by Spreading Algae with `ReturnCardFromGraveyardEffect.builder().destination(HAND).filter(new CardIsSelfPredicate()).returnAll(true).build()` ("return it to its owner's hand") |
| `ON_ALLY_AURA_OR_EQUIPMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD` | Your Aura or Equipment dies |
| `ON_TRANSFORM_TO_BACK_FACE` | This permanent transforms to back face |
| `ON_TRANSFORM_TO_FRONT_FACE` | This permanent transforms back to front face |
| `ON_PLAYER_LOSES_GAME` | A player loses the game (fired in `GameOutcomeService`; 2-player engine ends before it resolves) |
| `GRAVEYARD_ON_ALLY_CREATURE_DIES` | Like ON_ALLY_CREATURE_DIES but fires from the controller's graveyard. Wrap in `TriggeringCardConditionalEffect` to filter the dying creature. A `MayEffect` inner is queued as a may-ability; anything else (e.g. `MayPayManaEffect`) goes on the stack. A source card that dies in the same event does not trigger. Scanned in `TriggerCollectionService.checkGraveyardAllyCreatureDeathTriggers`. Used by Furious Forebear |
