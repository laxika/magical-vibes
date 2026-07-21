# ACTIVATED_ABILITY_GUIDE

Quick reference for building `ActivatedAbility` instances. Covers all constructor overloads, all parameters, and when to use each variant.

## Fields reference

| Field | Type | Description |
|-------|------|-------------|
| `requiresTap` | `boolean` | `true` if the ability has {T} in its cost (tap as cost) |
| `requiresUntap` | `boolean` | `true` if the ability has {Q} in its cost (untap as cost); set via `.withRequiresUntap()`, never combined with `requiresTap` |
| `manaCost` | `String` | Mana cost string like `"{2}{B}"`, or `null` for no mana cost |
| `effects` | `List<CardEffect>` | Effects to resolve (costs first, then actual effects) |
| `description` | `String` | Rules text shown to the player (e.g. `"{T}: Draw a card."`) |
| `targetFilter` | `TargetFilter` | Restricts valid targets (permanent filter or stack filter) |
| `loyaltyCost` | `Integer` | Planeswalker loyalty cost (e.g. `+1`, `-2`, `-8`). `null` for non-planeswalker abilities. `0` when `variableLoyaltyCost` is `true` |
| `variableLoyaltyCost` | `boolean` | `true` for -X loyalty abilities where X is chosen by the player. The chosen X is passed as xValue |
| `maxActivationsPerTurn` | `Integer` | Maximum activations per turn. `null` for unlimited |
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
| `ONLY_WHILE_ATTACKING` | Activate only if this creature is attacking (checks `permanent.isAttacking()`) |
| `ONLY_BEFORE_ATTACKERS_DECLARED` | Activate only during your turn, before attackers are declared (active player + step before `DECLARE_ATTACKERS`). Stern Marshal |
| `ONLY_DURING_COMBAT` | Activate only during the combat phase (checks `gameData.currentStep.isCombatPhase()`). Jade Statue |
| `ONLY_DURING_DECLARE_ATTACKERS_IF_ATTACKED` | Activate only during the declare attackers step and only if you've been attacked this step (a creature is attacking you or a planeswalker you control). Kongming's Contraptions. Checks `gameData.currentStep == DECLARE_ATTACKERS` + `gameQueryService.isPlayerBeingAttacked(gd, playerId)` |
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

Cards: `OonasProwler`

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

```java
addGraveyardActivatedAbility(new ActivatedAbility(
    false, "{3}{W}",
    List.of(new ReturnSourceFromGraveyardAttachedToTargetEffect()),
    "{3}{W}: Return this card … attached to target creature. Activate only as a sorcery.",
    new PermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "Target must be a creature"),
    null, null, ActivationTimingRestriction.SORCERY_SPEED));
```

Cards: `GryffsBoon`

**Embalm / Eternalize** ("{cost}, Exile this card from your graveyard: Create a token that's a copy of it, except ... Activate only as a sorcery.") is a graveyard activated ability whose cost exiles the source card. Use `ExileSelfFromGraveyardCost()` (paid at activation, before the ability hits the stack, so it can't be activated twice off the same card) plus `CreateTokenCopyOfSourceEffect(false, 1, colorOverride, addedSubtype, removeManaCost)` for the transformed copy, and `ActivationTimingRestriction.SORCERY_SPEED` (now enforced for graveyard abilities by `validateGraveyardTimingRestrictions`).

```java
// Embalm {5}{W}: white Zombie copy with no mana cost, keeps the source's P/T.
addGraveyardActivatedAbility(new ActivatedAbility(
    false, "{5}{W}",
    List.of(
        new ExileSelfFromGraveyardCost(),
        new CreateTokenCopyOfSourceEffect(false, 1, CardColor.WHITE, CardSubtype.ZOMBIE, true)),
    "Embalm {5}{W} (...)",
    ActivationTimingRestriction.SORCERY_SPEED));

// Eternalize {3}{W}{W}: same shape but a BLACK Zombie with a fixed 4/4 base P/T (last two args).
new CreateTokenCopyOfSourceEffect(false, 1, CardColor.BLACK, CardSubtype.ZOMBIE, true, 4, 4);
```

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

```java
// Reinforce 2—{2}{W} ({2}{W}, Discard this card: Put two +1/+1 counters on target creature.)
addHandActivatedAbility(new ActivatedAbility(false, "{2}{W}",
    List.of(new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 2)),
    "Reinforce 2—{2}{W} ({2}{W}, Discard this card: Put two +1/+1 counters on target creature.)",
    new PermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "Target must be a creature")));

// Reinforce X—{X}{W}{W} (Swell of Courage) — X counters via new XValue()
addHandActivatedAbility(new ActivatedAbility(false, "{X}{W}{W}",
    List.of(new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, new XValue())),
    "Reinforce X—{X}{W}{W} (...)",
    new PermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "Target must be a creature")));
```

Cards: `BurrentonBombardier`

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

An ability that produces mana and has no target/loyalty cost is a **mana ability** (resolves immediately, no stack). Any non-mana effects in its list are treated as reflexive "when you do" riders resolved inline by `ActivatedAbilityExecutionService.doResolveManaAbility`. Only a fixed set of rider effects are supported there: `GainLifeEffect`, `DealDamageToPlayersEffect` with recipient `CONTROLLER`, `DealDamageToPlayersEffect` with recipient `EACH_OPPONENT` (Rubble Rouser: `{T}, Exile a card from your graveyard: Add {R}. When you do, deal 1 damage to each opponent.`), and `PutCountersOnSelfEffect` (Pyramid of the Pantheon: `{2}, {T}: Add one mana of any color. Put a brick counter on this artifact.` — the counter is placed on the source, respecting `cantHaveCounters`). To support a new rider, add a branch in `doResolveManaAbility` — a rider effect placed on a mana ability but not handled there is silently dropped.

## Costs in the effects list

Sacrifice and discard costs go in the `effects` list BEFORE the actual effect. The engine processes them in order.

All cost effects implement the `CostEffect` marker interface (which extends `CardEffect`). When creating a new cost effect, implement `CostEffect` instead of `CardEffect` — this ensures it is automatically filtered out during effect snapshotting and excluded from mana ability detection.

**Cost-vs-effect sacrifice invariant (rules-critical).** A sacrifice written *before the colon* of an **activated ability** is a **cost** — it MUST use a `Sacrifice…Cost` record so it is paid at activation, before priority passes. Never model an activated ability's sacrifice cost as a `Sacrifice…Effect` on the stack: that would hand opponents a response window they should not get and snapshot the sacrificed permanent's characteristics at the wrong time. Conversely, a sacrifice that happens during **resolution** — a spell's effect, a triggered ability, a delayed sacrifice, or anything written *after* the colon ("…: create a token, **then** sacrifice a creature") — is genuinely part of the effect and MUST use a `Sacrifice…Effect`; there is no cost there to pay. For characteristic-dependent sacrifice costs (X = the sacrificed creature's power/toughness/mana value/color symbols), `SacrificeCreatureCost` snapshots those at payment time via its `trackSacrificed*` flags — no effect-time workaround is needed.

| Cost effect | Constructor | Use when |
|------------|-------------|----------|
| `SacrificeSelfCost` | `()` or `(true)` | "Sacrifice this: ...". `(true)` snapshots this permanent's effective power into xValue at payment (Mausoleum Wanderer) |
| `SacrificeCreatureCost` | `()` | "Sacrifice a creature: ..." |
| `SacrificeCreatureCost` | `(false, false, false, true)` | "Sacrifice another creature: ..." (excludeSelf prevents sacrificing the source) |
| `SacrificeArtifactCost` | `()` | "Sacrifice an artifact: ..." |
| `SacrificePermanentCost` | `(PermanentPredicate filter, String description)` or `(PermanentPredicate filter, String description, boolean excludeSource)` | "Sacrifice an artifact or creature: ..." or "Sacrifice a Goblin: ..." — generic predicate-based sacrifice. Use `PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate(), new PermanentHasSubtypePredicate(CardSubtype.GOBLIN)))` and `excludeSource=false` for subtype creature costs that can sacrifice the source |
| `SacrificeMultiplePermanentsCost` | `(int count, PermanentPredicate filter)` | "Sacrifice three artifacts: ..." (use with matching predicate) |
| `SacrificePermanentsSequenceCost` | `(List<PermanentPredicate> filters, List<String> descriptions)` | "Sacrifice a green creature, a white creature, and a blue creature: ..." (Angel's Herald) — one distinct permanent per per-slot filter, in order. Use this single cost, NOT several `SacrificePermanentCost` entries: the activation resume path carries only one cost effect through interactive picks, so multiple distinct sacrifice costs on one ability would silently skip the 2nd/3rd. Only offers a slot permanents whose selection still leaves a full matching for the remaining slots (no dead-end mid-payment) |
| `ReturnMultiplePermanentsToHandCost` | `(int count, PermanentPredicate filter)` | "Return two lands you control to their owner's hand: ..." (bounces N matching permanents as cost). Works with both battlefield and graveyard activated abilities |
| `TapMultiplePermanentsCost` | `(int count, PermanentPredicate filter)` or `(int, PermanentPredicate, boolean excludeSource)` | "Tap N untapped [matching] you control: ..." — taps N untapped matching permanents as cost (Captivating Vampire, Gilt-Leaf Archdruid, Crackleburr's damage half). Combine `PermanentIsCreaturePredicate` + `PermanentColorInPredicate` via `PermanentAllOfPredicate` for "tap two red creatures". |
| `UntapMultiplePermanentsCost` | `(int count, PermanentPredicate filter)` or `(int, PermanentPredicate, boolean excludeSource)` | Untap-symbol `{Q}` mirror of `TapMultiplePermanentsCost`: "Untap N tapped [matching] you control: ..." — valid choices are *tapped* matching permanents, which get untapped as cost. Pair with `.withRequiresUntap()` on the ability. Crackleburr ("Untap two tapped blue creatures you control"). |
| `SacrificeAllCreaturesYouControlCost` | `()` | "Sacrifice all creatures: ..." |
| `SacrificeXPermanentsCost` | `(PermanentPredicate filter)` | "Sacrifice X [matching]: ..." — sacrifices X permanents matching the filter, where X is the ability's xValue chosen at activation (the sacrifice-analog of `TapXPermanentsCost`). Springjack Pasture (`PermanentAllOfPredicate(creature + Goat subtype)`); the number sacrificed becomes X for the ability's `AwardXAnyColorManaEffect` + gain-X-life rider |
| `DiscardCardTypeCost` | `(CardPredicate, String label)` or `(CardPredicate, String label, boolean manaValueEqualsX)` | "Discard a [label] card: ..." (null predicate = any card). E.g. `(new CardTypePredicate(CardType.LAND), "land")`, `(new CardIsHistoricPredicate(), "historic")`, `(null, null)` for any. `manaValueEqualsX=true` → "Discard a card with mana value X" (restricts valid discards to MV == chosen X; pair with an `{X}` cost). Knollspine Invocation |
| `DiscardHandCost` | `()` | "Discard your hand: ..." — discards the controller's entire hand as a cost (no choice, no legality restriction; empty hand is fine). Fires per-card discard triggers. Slate of Ancestry |
| `DiscardRandomCardCost` | `()` | "Discard a card at random: ..." — discards one uniformly-random card from the controller's hand as a cost (no player choice). Requires a non-empty hand to activate. Fires the discarded card's discard triggers. Coral Helm |
| `ExileCardFromGraveyardCost` | `(CardType)`, `(CardSubtype)`, or `(CardType, boolean payManaCost, boolean imprint, boolean trackPower)` | "Exile a [type] card from your graveyard: ..." (null = any type). Use the `(CardSubtype)` ctor for "Exile an Elf card" (Scarred Vinebreeder). For spells: use in SPELL slot with `trackExiledPower=true` to set X to exiled card's power |
| `ExileNCardsFromGraveyardCost` | `(int count, CardType requiredType)` | "Exile N [type] cards from your graveyard: ..." (null type = any). On a **battlefield** ability the front N cards are exiled deterministically (Immortal Coil, `count=2, null`). On a **graveyard-activated** ability (Salvage Titan, `count=3, ARTIFACT`) the path exiles N cards matching the type via `hasType` (artifact creatures count) **excluding the source card**, so a self-return ability doesn't exile the card it means to bring back — needs N *other* matching cards to activate |
| `TapTwoCreaturesSharingTypeCost` | `()` | "Tap two untapped creatures you control that share a creature type: ..." (Weight of Conscience). The two tapped creatures must share a creature type with each other (Changeling-aware, mutual constraint) — not expressible with `TapMultiplePermanentsCost`'s per-permanent filter. |
| `RevealTwoCardsSharingColorCost` | `()` | "Reveal two cards from your hand that share a color: ..." (Illuminated Folio). Revealed cards stay in hand; the cost only gates the ability, so payment auto-reveals any qualifying pair (a valid pair must exist to activate; colorless cards never qualify). |
| `RemoveCounterFromSourceCost` | `()` | "Remove a counter from this: ..." |
| `PutCounterOnSourceCost` | `()` = -1/-1 ×1, or `(powerMod, toughnessMod, count)` | "Put a -1/-1 counter on this creature: ..." — puts counters on the source as a cost (paid immediately on activation). Respects `cantHaveCounters`/`cantHaveMinusOneMinusOneCounters`. Barrenton Medic |
| `PutCounterOnControlledCreatureCost` | `(CounterType counterType, int count)` | "Put a -1/-1 counter on a creature you control: ..." — puts counter(s) on any creature you control (not just the source), chosen via the `PermanentChoiceCostHandler` pattern (auto-selects when only one creature exists, prompts when multiple). Also valid as a SPELL-slot cost (Scarscale Ritual). Hatchet Bully |
| `PayManaCost` | `(String manaCost)` | Payable side of `ForcedCostOrElseEffect` only (not an `ActivatedAbility` cost). "you may pay {cost}; if you don't, [penalty]" — e.g. Force of Nature `ForcedCostOrElseEffect(PayManaCost("{G}{G}{G}{G}"), penalties, true)` |

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
    List.of(new SacrificeArtifactCost(), new DealDamageToAnyTargetEffect(2)),
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
| `STATIC` | Continuous effect, always active while on battlefield |
| `UPKEEP_TRIGGERED` | Controller's upkeep. Supports any-target routing (creature/planeswalker/player) when an effect is true "any target" (`targetSpec().category()` includes players AND permanents, e.g. Form of the Dragon via `UpkeepAnyTargetTrigger`), single-player targeting (e.g. Bloodgift Demon via `UpkeepPlayerTargetTrigger`), and multi-player targeting (e.g. Axis of Mortality via `UpkeepMultiPlayerTargetTrigger` when any effect has `targetSpec().playerTargetCount() >= 2`) |
| `EACH_UPKEEP_TRIGGERED` | Each player's upkeep |
| `OPPONENT_UPKEEP_TRIGGERED` | Each opponent's upkeep |
| `ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED` | Upkeep of the enchanted permanent's controller (fires regardless of which player controls the aura). `affectedPlayerId` is baked in at trigger time for effects like `EnchantedCreatureControllerLosesLifeEffect` |
| `ENCHANTED_PLAYER_UPKEEP_TRIGGERED` | Upkeep of the enchanted player (for player auras/Curses). The enchanted player's ID is passed as `targetId` on the stack entry. Curse subtype is auto-detected via `isEnchantPlayer()` |
| `ENCHANTED_PLAYER_END_STEP_TRIGGERED` | "At the beginning of each end step, enchanted player …" (player aura/Curse). Fires at EVERY end step (any player's turn), unlike the upkeep variant. The enchanted player's ID (`attachedTo`) is baked as `targetId` on the stack entry. Used by Fraying Sanity (`MillEffect(new CardsPutIntoGraveyardByTargetPlayerThisTurn(), TARGET_PLAYER)`) |
| `ON_ENCHANTED_PLAYER_CREATURE_ENTERS_BATTLEFIELD` | "Whenever a creature enchanted player controls enters, …" (player-aura/Curse slot). `TriggerCollectionService.checkEnchantedPlayerCreatureEntersTriggers` scans every battlefield for Curses attached to the entering creature's controller and queues one non-targeting triggered ability each, controlled by the Aura's controller with the enchanted player baked as `targetId` — so `LoseLifeEffect(TARGET_PLAYER)` hits them and an accompanying `GainLifeEffect` feeds "you". Used by Trespasser's Curse (`LoseLifeEffect(1, TARGET_PLAYER)` + `GainLifeEffect(1)`) |
| `GRAVEYARD_UPKEEP_TRIGGERED` | Upkeep trigger from graveyard |
| `GRAVEYARD_ON_CONTROLLER_CASTS_SPELL` | Spell-cast trigger from graveyard — fires when the controller casts a spell matching the `SpellCastTriggerEffect.spellFilter()` while this card is in their graveyard (e.g. Lingering Phantom). Supports `manaCost` for "you may pay" patterns |
| `DRAW_TRIGGERED` | At the beginning of controller's draw step (draw step only, not spell draws) |
| `EACH_DRAW_TRIGGERED` | At the beginning of any player's draw step (draw step only) |
| `ON_CONTROLLER_DRAWS` | Whenever controller draws a card (all draws: draw step, spells, abilities) |
| `ON_OPPONENT_DRAWS` | Whenever an opponent draws a card (all draws: draw step, spells, abilities) |
| `ON_OPPONENT_DISCARDS` | An opponent discards a card |
| `ON_CONTROLLER_DISCARDS` | The controller discards a card ("whenever you discard a card"; cycling counts, CR 702.29e). Scanned on the discarding player's own battlefield in `TriggerCollectionService.checkDiscardTriggers`. Used by Necropotence (`ExileDiscardedCardFromGraveyardEffect`, resolved inline), Curator of Mysteries (`ScryEffect`, enqueued as a `TRIGGERED_ABILITY`), Drake Haven (`MayPayManaEffect`, enqueued as a `TRIGGERED_ABILITY` — its may-pay prompt comes up at resolution) and Hekma Sentinels (`BoostSelfEffect`, enqueued as a `TRIGGERED_ABILITY` carrying the source permanent id so "this creature gets +1/+1"). Targeted variants queue a `PermanentChoiceContext.DiscardControllerTriggerTarget` instead: Zenith Seeker (`GrantKeywordEffect` with `GrantScope.TARGET`) and Ominous Sphinx (`BoostTargetCreatureEffect` with an opponent-creature `filter`) — the effect's own predicate narrows the target |
| `ON_SELF_DISCARDED_BY_OPPONENT` | This card is discarded by an opponent |
| `END_STEP_TRIGGERED` | End step (any player's turn — "at the beginning of the end step") |
| `CONTROLLER_END_STEP_TRIGGERED` | Controller's end step only ("at the beginning of your end step") |
| `ON_ATTACK` | This creature attacks |
| `ON_ALLY_CREATURES_ATTACK` | One or more creatures the controller controls attack (fires once per combat, not per creature). Scans all controller's permanents after attackers declared |
| `GRAVEYARD_ON_ALLY_CREATURES_ATTACK` | Like ON_ALLY_CREATURES_ATTACK but fires from the controller's graveyard. The attacker count is passed via xValue. Supports `ConditionalEffect(new MinimumAttackers(minimumAttackers), wrapped)` wrapper for "N or more creatures" conditions. Used by Warcry Phoenix |
| `GRAVEYARD_ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER` | Like ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER but fires from the controller's graveyard. Holds an `AllyCombatDamageTriggerEffect`; the stack entry's source is the graveyard card itself (no source permanent). Wrap the inner effect in `MayEffect(ReturnSourceCardFromGraveyardToOwnerHandEffect(), ...)` for "if this card is in your graveyard, you may return this card to your hand". Scanned in `CombatDamageService.checkAllyCreatureCombatDamageToPlayerTriggers`. Used by Auntie's Snitch (Goblin-or-Rogue dealer predicate) |
| `GRAVEYARD_ON_ALLY_CREATURE_ENTERS_BATTLEFIELD` | Like ON_ALLY_CREATURE_ENTERS_BATTLEFIELD but fires from the controller's graveyard. Wrap in `TriggeringCardConditionalEffect(CardSubtypePredicate(...), inner)` to gate on the entering creature's subtype. A `MayEffect` inner is queued as a may-ability; anything else (e.g. `MayPayManaEffect`) goes on the stack. Excluded from Naban ETB doubling (a graveyard card is not a permanent). Scanned in `TriggerCollectionService.checkAllyCreatureEntersTriggers`. Used by Unconventional Tactics ("whenever a Zombie you control enters, you may pay {W}: return this from your graveyard to your hand") |
| `ON_ALLY_CREATURE_ATTACKS` | Fires once per attacking creature the controller controls (unlike ON_ALLY_CREATURES_ATTACK which fires once per combat). Scans all controller's permanents for each attacker. Supports `TriggeringCardConditionalEffect` (filter by the attacking creature's card) and `TriggeringPermanentConditionalEffect` (filter by the attacking permanent, e.g. "with a +1/+1 counter on it"). Mandatory effects go on the stack sourced by the ability's owner (attacked target captured for `DealDamageToAttackedTargetEffect`). A `MayEffect` is queued as a CR 603.5 resolution-time may whose source **permanent** is the *attacking* creature ("that creature") while the source **card** is the ability's owner — so the owner's card-level `target(...)` filter governs legal targets (give it a `PermanentPredicateTargetFilter(new PermanentIsPlaneswalkerPredicate())` for player-or-planeswalker damage). Used by Sanctum Seeker (Vampire drain), Hellrider (attacked-target damage), Rage Forger (counter-bearing attacker may ping a player/planeswalker) |
| `ON_ALLY_CREATURE_ATTACKS_UNBLOCKED` | Fires once per **unblocked** attacking creature the controller controls, during the declare-blockers step (both when the defender declares blocks and when no blockers exist). Supports `TriggeringCardConditionalEffect` to filter by the unblocked creature. The unblocked creature is set as the trigger's non-targeting `sourcePermanentId`, so self-scoped effects like `BoostSelfEffect` apply to "it" (the unblocked creature), not the source. Checked in `CombatBlockService`. Used by Stinkdrinker Bandit (Rogues get +2/+1) |
| `ON_CREATURE_ATTACKS_YOU` | Whenever a creature attacks you or a planeswalker you control. Fires once per attacking creature, on the defending player's permanents (the player being attacked, directly or via their planeswalker). The attacking creature's permanent ID is set as the non-targeting `targetId` on the stack entry. Checked in `CombatAttackService.declareAttackers`. Used by Lost in the Woods |
| `ON_ANY_CREATURE_ATTACKS` | Whenever ANY creature attacks (any controller, any defender). Fires once per attacking creature, on every permanent with this slot across all battlefields. The attacking creature is set as the non-targeting `targetId`, so a plain `DealDamageToTargetCreatureEffect` hits "it". Checked in `CombatAttackService.declareAttackers`. Used by Caltrops |
| `ON_ANY_CREATURE_BECOMES_TARGET_OF_SPELL_OR_ABILITY` | Whenever ANY creature (any controller) becomes the target of ANY spell or ability. Fires on ALL permanents with this slot across every battlefield. The targeted creature is set as the non-targeting `targetId`. Checked in `TriggerCollectionService.checkBecomesTargetOfSpellTriggers`/`checkBecomesTargetOfAbilityTriggers`. Used by Cowardice (`ReturnToHandEffect.target()`) |
| `ON_ALLY_CREATURE_EXPLORES` | Whenever a creature you control explores. Fires after the explore process completes (land into hand, or +1/+1 counter and may-graveyard choice). Supports targeted effects (e.g. BoostTargetCreatureEffect) via `ExploreTriggerTarget` queue — targets restricted to opponent's creatures. Used by Lurking Chupacabra |
| `ON_EXPLOIT` | When this permanent exploits a creature (CR 702.110). Fired after a successful `ExploitEffect` sacrifice while the source was on the battlefield at resolution start (self-sac still counts). Stack-targeting effects (e.g. `CounterSpellEffect`) use `ExploitTriggerTarget` — pair with `StackEntryHasTargetPredicate` to include activated/triggered abilities. Used by Overcharged Amalgam |
| `ON_BLOCK` | This creature blocks |
| `ON_BECOMES_BLOCKED` | This creature becomes blocked. Register effects with `TriggerMode.PER_BLOCKER` to fire once per blocker |
| `ON_ATTACKS_UNBLOCKED` | This creature attacks and isn't blocked. Fires once per unblocked attacker during the declare-blockers step (after blocks are declared, or immediately if the defender can't block) — before combat damage, and independent of whether damage is dealt. Player-affecting effects read the defending player from the non-targeting `targetId`. Checked in `CombatBlockService`. Used by Abyssal Nightstalker |
| `ON_ENCHANTED_CREATURE_ATTACKS_UNBLOCKED` | Aura slot: the creature this aura is attached to attacks and isn't blocked. Fires alongside `ON_ATTACKS_UNBLOCKED` in `CombatBlockService.collectUnblockedAttackTriggers` (scanning auras attached to each unblocked attacker); the enchanted attacker is baked as the non-targeting `sourcePermanentId` and the defending player as `targetId`. Used by Cloak of Confusion (`AssignNoCombatDamageAndDefendingPlayerDiscardsEffect`) |
| `ON_ALLY_CREATURE_BECOMES_BLOCKED` | Whenever a creature you control becomes blocked. Fires once per blocked attacker, on every permanent with this slot on the blocked creature's controller's battlefield. The blocked creature is set as the non-targeting `sourcePermanentId`, so self-scoped effects like `BoostSelfEffect` apply to "it". Wrap in `TriggeringCardConditionalEffect` to filter by the blocked creature. Checked in `CombatBlockService`. Used by Unstoppable Ash |
| `ON_ANY_PERMANENT_RETURNED_TO_HAND` | Whenever a permanent is returned to a player's hand (bounced from the battlefield). Fires on every permanent with this slot across all battlefields, once per returned permanent. The owner the permanent returned to is the non-targeting `targetId`, so a player-directed effect (e.g. `DiscardEffect(1, TARGET_PLAYER)`) acts on "that player". Fired from `PermanentRemovalService.removePermanentToHand` via `TriggerCollectionService.checkPermanentReturnedToHandTriggers`. Used by Warped Devotion |
| `ON_COMBAT_DAMAGE_TO_PLAYER` | This creature deals combat damage to a player. Fires once per combat damage step, so double strike can trigger in both first-strike and regular damage steps |
| `ON_COMBAT_DAMAGE_TO_CREATURE` | This creature deals combat damage to a creature. Fires once per damaged creature; the damaged creature is baked as non-targeting `targetId` so effects like `DestroyTargetPermanentEffect` / `PutCounterOnTargetPermanentEffect` act on "that creature" |
| `ON_DAMAGE_TO_PLAYER` | Any damage to a player (not just combat) |
| `ON_DEATH` | This permanent dies |
| `ON_SACRIFICE` | This permanent is sacrificed |
| `ON_ALLY_CREATURE_ENTERS_BATTLEFIELD` | A creature (including tokens) enters battlefield under your control |
| `ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD` | A nontoken creature enters battlefield under your control (not this permanent, not tokens). Used with MayPayManaEffect for Minion Reflector's copy trigger, or mandatory `CreateTokenCopyOfTargetPermanentEffect` (optionally gated by `TriggeringCardConditionalEffect`) for Necroduality. Entering permanent ID is baked as stack `targetId` / PendingMayAbility.targetCardId so the copy effect knows which creature to copy |
| `ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD` | An artifact enters battlefield under your control (not this permanent) |
| `ON_ALLY_NONTOKEN_ARTIFACT_ENTERS_BATTLEFIELD` | A nontoken artifact enters battlefield under your control (not this permanent). Used with MayPayManaEffect for Mirrorworks' copy trigger. Entering permanent ID is passed via PendingMayAbility.targetCardId |
| `ON_ANY_CREATURE_DIES` | Any creature (including tokens) on any battlefield dies. Fires for all permanents on all battlefields. Supports targeted effects via DeathTriggerTarget (e.g. Falkenrath Noble). Unwraps `TriggeringPermanentConditionalEffect` against the dying permanent — the predicate sees the dying creature's on-battlefield state incl. counters at death (e.g. Blowfly Infestation's "if it had a -1/-1 counter on it") |
| `ON_ANY_NONTOKEN_CREATURE_DIES` | Any nontoken creature on any battlefield dies (not just controller's). Used with MayEffect for Mimic Vat's imprint trigger |
| `ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD` | Any artifact (any player's) is put into a graveyard from the battlefield. Fires for destroy, sacrifice, etc. |
| `ON_ARTIFACT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD` | An artifact is put into an opponent's graveyard from the battlefield. Only fires when the graveyard owner is an opponent of this permanent's controller. Supports MayEffect wrapping. |
| `ON_ANY_LAND_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD` | Any land (any player's) is put into a graveyard from the battlefield. Fires for destroy, sacrifice, etc. Used by Dingus Egg with `DealDamageToPlayersEffect(2, TRIGGERING_PERMANENT_CONTROLLER)` — target pre-set to the land's controller at trigger time. |
| `ON_OPPONENT_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD` | A permanent of **any** type an opponent of this permanent's controller controls is put into a graveyard from the battlefield. Fired once per removal in `PermanentRemovalService.processGraveyardAndTriggers` via `TriggerCollectionService.checkOpponentPermanentPutIntoGraveyardTriggers`; the collector bakes the dying card id + that opponent's id into the effect. Used by Prince of Thralls with `StealDyingOpponentPermanentUnlessPaysLifeEffect(3)`. |
| `ON_ALLY_CREATURE_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE` | A creature card is put into your graveyard from anywhere (battlefield, hand, library, stack, exile). Uses printed card types (tokens never fire; a creature card that was a noncreature permanent still does). Fires on permanents the graveyard owner controls. Checked in `GraveyardService.addCardToGraveyard`. Used by Soulcipher Board (`SequenceEffect` of `RemoveCounterFromSourceEffect(OMEN, 1)` + `ConditionalEffect(NotCondition(SourceCounterThreshold(1, OMEN)), TransformSelfEffect)`). |
| `ON_BLACK_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE` | A black card is put into an opponent's graveyard from anywhere (battlefield, hand, library, stack, exile). Only fires on permanents controlled by an opponent of the graveyard owner. Checked in `GraveyardService.addCardToGraveyard`. Supports MayEffect wrapping. Used by Compost. |
| `ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD` | Any other creature enters battlefield |
| `ON_PERMANENT_ENTERS_FROM_GRAVEYARD` | Any permanent (not just creatures) enters from ANY graveyard, checked via `enteredFromGraveyardOwnerId`. Queues a non-targeting stack entry for the source's controller (`TriggerCollectionService.checkPermanentEntersFromGraveyardTriggers`). Used by River Kelpie. Contrast `ON_CREATURE_ENTERS_FROM_GRAVEYARD` (Flayer of the Hatebound): creatures-only, controller's graveyard only, any-target pipeline |
| `ON_ALLY_CREATURE_DIES` | A creature you control dies. Supports `TriggeringCardConditionalEffect` wrapping to filter by dying creature predicates (e.g. Slimefoot only triggers for Saprolings, Requiem Angel for non-Spirits) |
| `ON_ALLY_NONTOKEN_CREATURE_DIES` | A nontoken creature you control dies. Only fires for nontoken creatures (tokens are excluded). Used by Gutter Grime |
| `ON_DAMAGED_CREATURE_DIES` | A creature damaged by this permanent dies |
| `ON_ANY_PLAYER_CASTS_SPELL` | Any player casts a spell |
| `ON_CONTROLLER_CASTS_SPELL` | Controller casts a spell ("whenever you cast...") |
| `ON_ANY_PLAYER_TAPS_LAND` | Any player taps a land |
| `ON_OPPONENT_PERMANENT_BECOMES_TAPPED` | A permanent an opponent of the controller controls becomes tapped (any tap — for mana or forced). Opponent-scoped counterpart of `ON_ALLY_PERMANENT_BECOMES_TAPPED`; wrap in `TriggeringPermanentConditionalEffect` to filter the tapped permanent. Used by Thoughtleech |
| `ON_ENCHANTED_PERMANENT_TAPPED` | The permanent this aura is attached to becomes tapped. Does NOT fire for "enters tapped" (CR 603.6d). `affectedPlayerId` is baked in at trigger time with the enchanted permanent's controller. Effects: `GivePoisonCountersEffect` (Relic Putrescence), `DestroyEnchantedPermanentEffect` (Spreading Algae — "destroy it"), `DealDamageToPlayersEffect(N, TRIGGERING_PERMANENT_CONTROLLER)` (Psychic Venom — "deals N damage to that land's controller"; collector bakes the target to the tapped permanent's controller). When the land taps for mana the trigger is deferred into `pendingManaAbilityTriggers` (CR 603.3) until a player next receives priority |
| `ON_ENCHANTED_CREATURE_DEALT_DAMAGE` | The creature this aura is attached to is dealt damage (combat or non-combat). Damage amount passed via `TriggerContext.DamageToCreature` and snapshotted to xValue for "that much damage" effects |
| `ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU` | Any permanent deals damage to this permanent's controller |
| `ON_CONTROLLER_DEALT_DAMAGE` | "Whenever you're dealt damage, ..." — this permanent's controller is dealt damage (combat or non-combat, from any source incl. spells/abilities). Unlike `ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU` (reacts to the source, permanent sources only), this carries only the amount, snapshotted onto the trigger's `eventValue`. Read it with an `EventValue` amount: `PutCountersOnSelfEffect(CounterType.VITALITY, new EventValue())` ("put that many counters" — Living Artifact). Fires once per source (CR: simultaneous sources trigger separately) from `CombatDamageService` (per source) and `DamageSupport` (non-combat) |
| `ON_CONTROLLER_DEALT_DAMAGE_BY_OPPONENT` | "Whenever a source an opponent controls deals damage to you, ..." — like `ON_CONTROLLER_DEALT_DAMAGE` but gated so only damage from a source an **opponent** controls fires it (the controller's own self-damage is ignored). Same `eventValue` snapshot / `EventValue` read. Opponent gate applied in `TriggerCollectionService.checkControllerDealtDamageTriggers` from the source controller the two choke points pass (combat = active player; non-combat = the spell/ability's controller). Register the trigger on `MayEffect.class` for "you may" wording. Used by Retaliator Griffin (`MayEffect(PutCountersOnSelfEffect(PLUS_ONE_PLUS_ONE, new EventValue()))`) |
| `ON_ANY_SOURCE_DEALS_DAMAGE` | Global watcher: any source (creature or spell) deals damage to anything. Fires on every permanent with this slot across all battlefields; carries `TriggerContext.SourceDealsDamage` (source card, source controller, summed total). Damage one source deals simultaneously is summed into one trigger. Effect: `ReflectSourceDamageToItsControllerEffect(color)` (Justice). Driven from `CombatDamageService` (per source) and `EffectResolutionService` (non-combat flush) |
| `ON_SELF_DEALS_DAMAGE` | "Whenever this creature deals damage, ..." — fires only for the damage **this** permanent deals (combat or non-combat, to a creature/player/planeswalker). Shares the summed choke point behind `ON_ANY_SOURCE_DEALS_DAMAGE` but keyed off the source card, so it still triggers when the source dies dealing that damage; the summed total is snapshotted onto the trigger's `eventValue`. Read it with an `EventValue` amount: `GainLifeEffect(new EventValue())` ("you gain that much life" — El-Hajjâj) |
| `ON_ALLY_PERMANENT_SACRIFICED` | A permanent you control is sacrificed (not this one — "another") |
| `ON_ANY_CREATURE_SACRIFICED` | Global watcher: any player sacrifices a creature ("Whenever a player sacrifices a creature"). Fires on every permanent with this slot across all battlefields, once per sacrificed creature (last-known info); the trigger belongs to the scanning permanent's controller. A wrapped `MayEffect(PutCountersOnSourceEffect(1,1,1))` resolves onto the source (like Scavenger Drake's `ON_ANY_CREATURE_DIES`). Fired from both sacrifice choke points (`DestructionSupport.sacrificeAndLog` for edict/chosen sacrifices, `checkAllyPermanentSacrificedTriggers` for sacrifice-self / sacrifice-as-cost). Used by Thraximundar |
| `ON_BECOMES_TARGET_OF_SPELL` | This permanent becomes target of a spell |
| `ON_BECOMES_TARGET_OF_OPPONENT_SPELL` | This permanent becomes target of an opponent's spell |
| `ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY` | This permanent becomes target of any spell or ability |
| `ON_ALLY_CREATURE_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY` | Global monitor: a creature you control becomes target of opponent's spell/ability. Used by Shapers' Sanctuary |
| `ON_EQUIPPED_CREATURE_DIES` | Equipped creature dies |
| `ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD` | Enchanted permanent dies (graveyard only) |
| `ON_ENCHANTED_PERMANENT_LEAVES_BATTLEFIELD` | Enchanted permanent leaves battlefield (any destination) |
| `ON_OPPONENT_LAND_ENTERS_BATTLEFIELD` | Opponent's land enters. Wrap with `ConditionalEffect(new PermanentEnteredThisTurn(predicate, minCount), wrapped)` for "second+ land" |
| `ON_ALLY_LAND_ENTERS_BATTLEFIELD` | Your land enters (landfall) |
| `GRAVEYARD_ON_ALLY_LAND_ENTERS_BATTLEFIELD` | Like ON_ALLY_LAND_ENTERS_BATTLEFIELD but fires from the controller's graveyard. Wrap in `TriggeringCardConditionalEffect(new CardSubtypePredicate(...), wrapped)` to filter by the entering land, and wrap the inner effect in `MayEffect(ReturnCardFromGraveyardEffect.builder().destination(HAND).filter(new CardIsSelfPredicate()).build(), ...)` for "you may return this card from your graveyard to your hand". Scanned over the land controller's graveyard in `TriggerCollectionService.checkAllyLandEntersTriggers`. Used by Reach of Branches ("whenever a Forest you control enters") |
| `ON_OPPONENT_CREATURE_DIES` | An opponent's creature dies |
| `ON_DEALT_DAMAGE` | This creature is dealt damage (combat or non-combat) |
| `ON_OPENING_HAND_REVEAL` | First upkeep, cards in hand (Chancellor cycle). Wrap with `MayEffect` |
| `ON_OPPONENT_LOSES_LIFE` | Opponent loses life (damage or direct) |
| `ON_ALLY_EQUIPMENT_ENTERS_BATTLEFIELD` | An Equipment enters under your control |
| `ON_OPPONENT_CREATURE_ENTERS_BATTLEFIELD` | An opponent's creature enters |
| `ON_OPPONENT_SHUFFLES_LIBRARY` | Opponent shuffles library |
| `ON_CONTROLLER_GAINS_LIFE` | Controller gains life |
| `ON_OPPONENT_DEALT_NONCOMBAT_DAMAGE` | Opponent dealt noncombat damage |
| `ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER` | A creature you control deals combat damage to a player |
| `ON_SELF_MILLED` | This card is milled into graveyard |
| `STATE_TRIGGERED` | State-triggered ability (rule 603.8). Fires when predicate is true, won't retrigger while on stack |
| `SAGA_CHAPTER_I` | Saga chapter I (first lore counter placed, on ETB and precombat main) |
| `SAGA_CHAPTER_II` | Saga chapter II (second lore counter) |
| `SAGA_CHAPTER_III` | Saga chapter III (third lore counter, saga sacrificed after) |
| `BEGINNING_OF_COMBAT_TRIGGERED` | Beginning of combat on controller's turn |
| `EACH_BEGINNING_OF_COMBAT_TRIGGERED` | Beginning of each combat (any player's turn) |
| `PRECOMBAT_MAIN_TRIGGERED` | Beginning of precombat main phase on controller's turn |
| `POSTCOMBAT_MAIN_TRIGGERED` | Beginning of each postcombat main phase on controller's turn |
| `ON_OPPONENT_CREATURE_DEALT_DAMAGE` | An opponent's creature is dealt damage |
| `ON_ANY_CREATURE_DEALT_DAMAGE` | Any creature (yours or an opponent's) is dealt damage. Queued stack entry targets the damaged creature (targetId set, non-targeting). Register a target-taking effect like `DestroyTargetPermanentEffect(true)` — Death Pits of Rath |
| `ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE` | A creature you control (matching the effect's source filter) deals damage — combat or non-combat — to a creature. Fires on the watcher, not the damaged creature; the damage-source creature reflects that much damage to the damaged creature's controller. Register `ReflectAllyDamageToDamagedCreatureControllerEffect(sourceFilter)` — Greatbow Doyen |
| `ON_CONTROLLER_LOSES_LIFE` | Controller loses life |
| `ON_SELF_LEAVES_BATTLEFIELD` | This permanent leaves the battlefield (any means) |
| `ON_ANOTHER_CREATURE_LEAVES_BATTLEFIELD` | Another creature (any player's) leaves the battlefield by any means (destroy, exile, bounce, sacrifice, tuck) — broader than "dies". Global watcher fired from every leave path in `PermanentRemovalService` via `TriggerCollectionService.checkAnotherCreatureLeavesBattlefieldTriggers`; fires on every permanent with the slot except the leaving creature itself. Non-targeting: a "you may have target player mill two cards" is a `MayEffect(MillEffect(2, TARGET_PLAYER), …)` whose "may" and player target are resolved on the stack (Extractor Demon) |
| `ON_ANOTHER_ARTIFACT_LEAVES_BATTLEFIELD` | Another artifact **you control** leaves the battlefield by any means (destroy, exile, bounce, sacrifice, tuck). Controller-scoped watcher fired from every leave path in `PermanentRemovalService` via `TriggerCollectionService.checkAnotherArtifactLeavesBattlefieldTriggers`; fires only on the leaving artifact's controller's battlefield, except the leaving artifact itself. Pair with `ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD` (same effect on both slots) for "whenever another artifact you control enters or leaves the battlefield". Non-targeting: player target + "you may pay {1}" resolve on the stack via `MayPayManaEffect(SequenceEffect.of(LoseLifeEffect(TARGET_PLAYER), GainLifeEffect))` (Sludge Strider) |
| `ON_ALLY_CREATURE_LEAVES_BATTLEFIELD` | Another creature **you control** leaves the battlefield by any means. Controller-scoped sibling of `ON_ANOTHER_CREATURE_LEAVES_BATTLEFIELD`. Fired via `TriggerCollectionService.checkAllyCreatureLeavesBattlefieldTriggers`. Used by Luminous Phantom (`GainLifeEffect(1)`) |
| `ON_SELF_PUT_INTO_GRAVEYARD_FROM_ANYWHERE` | This card is put into a graveyard from anywhere (battlefield/hand/library/stack). Fired for every zone→graveyard transition in `GraveyardService.addCardToGraveyard` (card enters graveyard first, then trigger). Used by Purity with `ShuffleSelfFromGraveyardIntoLibraryEffect` |
| `ON_SELF_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD` | This card is put into a graveyard specifically **from the battlefield** ("dies" for a permanent). Fired in `GraveyardService.addCardToGraveyard` only when the source zone is `BATTLEFIELD` (card enters graveyard first, then trigger). Used by Spreading Algae with `ReturnCardFromGraveyardEffect.builder().destination(HAND).filter(new CardIsSelfPredicate()).returnAll(true).build()` ("return it to its owner's hand") |
| `ON_ALLY_AURA_OR_EQUIPMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD` | Your Aura or Equipment dies |
| `ON_TRANSFORM_TO_BACK_FACE` | This permanent transforms to back face |
| `ON_TRANSFORM_TO_FRONT_FACE` | This permanent transforms back to front face |
| `ON_PLAYER_LOSES_GAME` | A player loses the game (fired in `GameOutcomeService`; 2-player engine ends before it resolves) |
