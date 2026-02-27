# ACTIVATED_ABILITY_GUIDE

Quick reference for building `ActivatedAbility` instances. Covers all constructor overloads, all parameters, and when to use each variant.

## Fields reference

| Field | Type | Description |
|-------|------|-------------|
| `requiresTap` | `boolean` | `true` if the ability has {T} in its cost (tap as cost) |
| `manaCost` | `String` | Mana cost string like `"{2}{B}"`, or `null` for no mana cost |
| `effects` | `List<CardEffect>` | Effects to resolve (costs first, then actual effects) |
| `description` | `String` | Rules text shown to the player (e.g. `"{T}: Draw a card."`) |
| `targetFilter` | `TargetFilter` | Restricts valid targets (permanent filter or stack filter) |
| `loyaltyCost` | `Integer` | Planeswalker loyalty cost (e.g. `+1`, `-2`, `-8`). `null` for non-planeswalker abilities |
| `maxActivationsPerTurn` | `Integer` | Maximum activations per turn. `null` for unlimited |
| `timingRestriction` | `ActivationTimingRestriction` | When the ability can be activated. `null` for default (instant speed) |

**Targeting is computed from effects** — `isNeedsTarget()` and `isNeedsSpellTarget()` are derived getters, never stored as fields. Override `canTargetPlayer()`, `canTargetPermanent()`, `canTargetSpell()`, or `canTargetGraveyard()` on your effect record to return `true`.

### ActivationTimingRestriction values

| Value | Use when |
|-------|----------|
| `SORCERY_SPEED` | Equip abilities, sorcery-speed activated abilities |
| `ONLY_DURING_YOUR_UPKEEP` | Abilities that can only be used during your upkeep |
| `ONLY_WHILE_CREATURE` | Abilities on creature lands that only work while animated |
| `METALCRAFT` | Activate only if you control three or more artifacts |
| `POWER_4_OR_GREATER` | Activate only if this creature's power is 4 or greater (checks effective power incl. static bonuses) |

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
new ActivatedAbility(true, "{2}", List.of(new MillTargetPlayerEffect(2)),
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
new ActivatedAbility(true, null, List.of(new MakeTargetUnblockableEffect()),
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
    List.of(new SacrificeSelfCost(), new ChooseCardFromTargetHandToDiscardEffect(1, List.of())),
    "Sacrifice: Target player reveals their hand...",
    ActivationTimingRestriction.SORCERY_SPEED)
```

Cards: `SpawningPool` (ONLY_WHILE_CREATURE), `ThrullSurgeon` (SORCERY_SPEED), `ColossusOfSardia` (ONLY_DURING_YOUR_UPKEEP), `SkyshroudRanger` (SORCERY_SPEED)

**Note:** For abilities that target spells on the stack (e.g. activated counter ability), spell targeting is auto-derived from effects (e.g. `CounterUnlessPaysEffect.canTargetSpell()` returns `true`). Use a `StackEntryPredicateTargetFilter` if target legality is restricted.

---

### 5. Loyalty ability (planeswalkers)

```java
new ActivatedAbility(loyaltyCost, effects, description)
```

**Use when:** Planeswalker loyalty ability with no target restrictions.

```java
// +1: Create a token
new ActivatedAbility(+1, List.of(new CreateCreatureTokenEffect("Kithkin", 1, 1, CardColor.WHITE, Set.of(CardColor.GREEN, CardColor.WHITE), List.of(CardSubtype.KITHKIN))),
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

### 7. Equipment ability (equip with sorcery speed + controlled creature filter)

```java
new ActivatedAbility(false, manaCost, List.of(new EquipEffect()),
    "Equip " + manaCost,
    new ControlledPermanentPredicateTargetFilter(
        new PermanentIsCreaturePredicate(),
        "Target must be a creature you control"
    ),
    null,
    null,
    ActivationTimingRestriction.SORCERY_SPEED)
```

**Use when:** Any equipment card with equip cost. This is the standard equip pattern — always the same structure, only the mana cost varies. Uses the canonical 8-param constructor.

Cards: `LoxodonWarhammer` ({3}), `LeoninScimitar` ({1}), `BarkOfDoran` ({1}), `WhispersilkCloak` ({2})

---

### 8. Full constructor (all parameters)

```java
new ActivatedAbility(requiresTap, manaCost, effects,
    description, targetFilter, loyaltyCost, maxActivationsPerTurn, timingRestriction)
```

**Use when:** None of the simpler overloads fit. Pass `null` for unused optional parameters.

---

## Costs in the effects list

Sacrifice and discard costs go in the `effects` list BEFORE the actual effect. The engine processes them in order.

All cost effects implement the `CostEffect` marker interface (which extends `CardEffect`). When creating a new cost effect, implement `CostEffect` instead of `CardEffect` — this ensures it is automatically filtered out during effect snapshotting and excluded from mana ability detection.

| Cost effect | Constructor | Use when |
|------------|-------------|----------|
| `SacrificeSelfCost` | `()` | "Sacrifice this: ..." |
| `SacrificeCreatureCost` | `()` | "Sacrifice a creature: ..." |
| `SacrificeSubtypeCreatureCost` | `(CardSubtype)` | "Sacrifice a Goblin: ..." |
| `SacrificeArtifactCost` | `()` | "Sacrifice an artifact: ..." |
| `SacrificeMultiplePermanentsCost` | `(int count, PermanentPredicate filter)` | "Sacrifice three artifacts: ..." (use with matching predicate) |
| `SacrificeAllCreaturesYouControlCost` | `()` | "Sacrifice all creatures: ..." |
| `DiscardCardTypeCost` | `(CardType)` | "Discard a [type] card: ..." |
| `ExileCardFromGraveyardCost` | `(CardType)` | "Exile a [type] card from your graveyard: ..." (null = any type) |
| `RemoveCounterFromSourceCost` | `()` | "Remove a counter from this: ..." |

```java
// {1}{R}, Sacrifice a Goblin: Deal 2 damage to any target
new ActivatedAbility(false, "{1}{R}",
    List.of(new SacrificeSubtypeCreatureCost(CardSubtype.GOBLIN), new DealDamageToAnyTargetEffect(2)),
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
            new SearchLibraryForCardTypesToBattlefieldEffect(Set.of(CardType.ARTIFACT), false, false)),
    "{T}, Sacrifice three artifacts: Search your library for an artifact card, put it onto the battlefield, then shuffle.")
```

Cards: `SiegeGangCommander`, `BottleGnomes`, `DoomedNecromancer`, `ThrullSurgeon`, `BloodfireColossus`, `BarrageOgre`, `KuldothaForgemaster`

---

## TargetFilter types

| Filter class | Constructor | Use when |
|-------------|-------------|----------|
| `PermanentPredicateTargetFilter` | `(PermanentPredicate, String errorMsg)` | Target any permanent matching predicate |
| `ControlledPermanentPredicateTargetFilter` | `(PermanentPredicate, String errorMsg)` | Target only permanents YOU control matching predicate |
| `StackEntryPredicateTargetFilter` | `(StackEntryPredicate, String errorMsg)` | Target a spell on the stack |
| `PlayerPredicateTargetFilter` | `(PlayerPredicate, String errorMsg)` | Target a player matching predicate |

### Common PermanentPredicate compositions

| Predicate | Constructor | Matches |
|-----------|-------------|---------|
| `PermanentIsCreaturePredicate` | `()` | creatures |
| `PermanentIsArtifactPredicate` | `()` | artifacts |
| `PermanentIsLandPredicate` | `()` | lands |
| `PermanentIsEnchantmentPredicate` | `()` | enchantments |
| `PermanentIsTappedPredicate` | `()` | tapped permanents |
| `PermanentIsAttackingPredicate` | `()` | attacking creatures |
| `PermanentIsBlockingPredicate` | `()` | blocking creatures |
| `PermanentIsSourceCardPredicate` | `()` | the source card itself |
| `PermanentIsTokenPredicate` | `()` | token permanents |
| `PermanentColorInPredicate` | `(Set<CardColor>)` | permanents of specified colors |
| `PermanentHasSubtypePredicate` | `(CardSubtype)` | permanents with specific subtype |
| `PermanentHasAnySubtypePredicate` | `(Set<CardSubtype>)` | permanents with any of the subtypes |
| `PermanentHasKeywordPredicate` | `(Keyword)` | permanents with specific keyword |
| `PermanentPowerAtMostPredicate` | `(int maxPower)` | creatures with power <= N |
| `PermanentControlledBySourceControllerPredicate` | `()` | permanents controlled by the source's controller |
| `PermanentTruePredicate` | `()` | always matches (no restriction) |
| `PermanentAllOfPredicate` | `(List<PermanentPredicate>)` | AND: all predicates must match |
| `PermanentAnyOfPredicate` | `(List<PermanentPredicate>)` | OR: at least one predicate matches |
| `PermanentNotPredicate` | `(PermanentPredicate)` | NOT: inverts a predicate |

### Common StackEntryPredicate compositions

| Predicate | Constructor | Matches |
|-----------|-------------|---------|
| `StackEntryTypeInPredicate` | `(Set<StackEntryType>)` | spells of specific types |
| `StackEntryColorInPredicate` | `(Set<CardColor>)` | spells of specific colors |
| `StackEntryIsSingleTargetPredicate` | `()` | spells with exactly one target |
| `StackEntryAllOfPredicate` | `(List<StackEntryPredicate>)` | AND composition |
| `StackEntryAnyOfPredicate` | `(List<StackEntryPredicate>)` | OR composition |
| `StackEntryNotPredicate` | `(StackEntryPredicate)` | NOT inversion |

### PlayerPredicate compositions

| Predicate | Constructor | Matches |
|-----------|-------------|---------|
| `PlayerRelationPredicate` | `(PlayerRelation)` | player by relation. `PlayerRelation`: `OPPONENT`, `SELF` |

---

## Card-level targeting (for spells, not abilities)

For spells (instants/sorceries) that need targets, targeting is auto-derived from effects. Override `canTargetPlayer()`, `canTargetPermanent()`, `canTargetSpell()`, or `canTargetGraveyard()` on your effect record to return `true`. Then in the card constructor:

```java
setTargetFilter(new SomeTargetFilter()); // restricts valid targets (optional)
addEffect(EffectSlot.SPELL, effect);     // effect resolved when spell resolves
// isNeedsTarget() and isNeedsSpellTarget() are computed automatically from effects
```

---

## EffectSlot quick reference

| Slot | Fires when |
|------|------------|
| `SPELL` | Instant/sorcery resolves |
| `ON_ENTER_BATTLEFIELD` | Permanent enters the battlefield (ETB) |
| `ON_TAP` | Permanent is tapped for mana (lands) |
| `STATIC` | Continuous effect, always active while on battlefield |
| `UPKEEP_TRIGGERED` | Controller's upkeep |
| `EACH_UPKEEP_TRIGGERED` | Each player's upkeep |
| `OPPONENT_UPKEEP_TRIGGERED` | Each opponent's upkeep |
| `GRAVEYARD_UPKEEP_TRIGGERED` | Upkeep trigger from graveyard |
| `DRAW_TRIGGERED` | Controller draws a card |
| `EACH_DRAW_TRIGGERED` | Any player draws a card |
| `ON_OPPONENT_DRAWS` | An opponent draws a card |
| `ON_OPPONENT_DISCARDS` | An opponent discards a card |
| `ON_SELF_DISCARDED_BY_OPPONENT` | This card is discarded by an opponent |
| `END_STEP_TRIGGERED` | End step |
| `ON_ATTACK` | This creature attacks |
| `ON_BLOCK` | This creature blocks |
| `ON_BECOMES_BLOCKED` | This creature becomes blocked. Register effects with `TriggerMode.PER_BLOCKER` to fire once per blocker |
| `ON_COMBAT_DAMAGE_TO_PLAYER` | This creature deals combat damage to a player |
| `ON_COMBAT_DAMAGE_TO_CREATURE` | This creature deals combat damage to a creature |
| `ON_DAMAGE_TO_PLAYER` | Any damage to a player (not just combat) |
| `ON_DEATH` | This permanent dies |
| `ON_SACRIFICE` | This permanent is sacrificed |
| `ON_ALLY_CREATURE_ENTERS_BATTLEFIELD` | A creature enters battlefield under your control |
| `ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD` | An artifact enters battlefield under your control (not this permanent) |
| `ON_ANY_NONTOKEN_CREATURE_DIES` | Any nontoken creature on any battlefield dies (not just controller's). Used with MayEffect for Mimic Vat's imprint trigger |
| `ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD` | Any artifact (any player's) is put into a graveyard from the battlefield. Fires for destroy, sacrifice, etc. |
| `ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD` | Any other creature enters battlefield |
| `ON_ALLY_CREATURE_DIES` | A creature you control dies |
| `ON_DAMAGED_CREATURE_DIES` | A creature damaged by this permanent dies |
| `ON_ANY_PLAYER_CASTS_SPELL` | Any player casts a spell |
| `ON_CONTROLLER_CASTS_SPELL` | Controller casts a spell ("whenever you cast...") |
| `ON_ANY_PLAYER_TAPS_LAND` | Any player taps a land |
| `ON_ENCHANTED_PERMANENT_TAPPED` | The permanent this aura is attached to becomes tapped. Does NOT fire for "enters tapped" (CR 603.6d). `affectedPlayerId` is baked in at trigger time with the enchanted permanent's controller |
| `ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU` | Any permanent deals damage to this permanent's controller |
| `ON_ALLY_PERMANENT_SACRIFICED` | A permanent you control is sacrificed (not this one — "another") |
| `ON_BECOMES_TARGET_OF_SPELL` | Fires when the permanent (or a permanent this is attached to) becomes the target of a spell. Used by equipment like Livewire Lash |
