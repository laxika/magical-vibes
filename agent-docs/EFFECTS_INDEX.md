# ACTIVATED_ABILITY_GUIDE

Quick reference for building `ActivatedAbility` instances. Covers all constructor overloads, all parameters, and when to use each variant.

## Fields reference

| Field | Type | Description |
|-------|------|-------------|
| `requiresTap` | `boolean` | `true` if the ability has {T} in its cost (tap as cost) |
| `manaCost` | `String` | Mana cost string like `"{2}{B}"`, or `null` for no mana cost |
| `effects` | `List<CardEffect>` | Effects to resolve (costs first, then actual effects) |
| `needsTarget` | `boolean` | `true` if the ability targets a permanent or player |
| `needsSpellTarget` | `boolean` | `true` if the ability targets a spell on the stack |
| `description` | `String` | Rules text shown to the player (e.g. `"{T}: Draw a card."`) |
| `targetFilter` | `TargetFilter` | Restricts valid targets (permanent filter or stack filter) |
| `loyaltyCost` | `Integer` | Planeswalker loyalty cost (e.g. `+1`, `-2`, `-8`). `null` for non-planeswalker abilities |
| `maxActivationsPerTurn` | `Integer` | Maximum activations per turn. `null` for unlimited |
| `timingRestriction` | `ActivationTimingRestriction` | When the ability can be activated. `null` for default (instant speed) |

### ActivationTimingRestriction values

| Value | Use when |
|-------|----------|
| `SORCERY_SPEED` | Equip abilities, sorcery-speed activated abilities |
| `ONLY_DURING_YOUR_UPKEEP` | Abilities that can only be used during your upkeep |
| `ONLY_WHILE_CREATURE` | Abilities on creature lands that only work while animated |

---

## Constructor quick-pick guide

### 1. Basic ability (most common)

```java
new ActivatedAbility(requiresTap, manaCost, effects, needsTarget, description)
```

**Use when:** Simple tap ability, mana ability, pump, or any ability with no target restrictions.

```java
// Tap to deal damage to any target
new ActivatedAbility(true, null, List.of(new DealDamageToAnyTargetEffect(3)), true,
    "{T}: Kamahl, Pit Fighter deals 3 damage to any target.")

// Pay mana to pump self
new ActivatedAbility(false, "{R}", List.of(new BoostSelfEffect(1, 0)), false,
    "{R}: Furnace Whelp gets +1/+0 until end of turn.")

// Tap + mana to mill
new ActivatedAbility(true, "{2}", List.of(new MillTargetPlayerEffect(2)), true,
    "{2}, {T}: Target player mills two cards.")
```

Cards: `KamahlPitFighter`, `FurnaceWhelp`, `Millstone`, `ProdigalPyromancer`, `ArcanisTheOmnipotent`

---

### 2. Ability with target filter

```java
new ActivatedAbility(requiresTap, manaCost, effects, needsTarget, description, targetFilter)
```

**Use when:** Ability targets a permanent but only specific ones (e.g. "target creature with power 2 or less", "target blue or red creature").

```java
// Target creature with power 2 or less
new ActivatedAbility(true, null, List.of(new MakeTargetUnblockableEffect()), true,
    "{T}: Target creature with power 2 or less can't be blocked this turn.",
    new PermanentPredicateTargetFilter(
        new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentPowerAtMostPredicate(2)
        )),
        "Target creature's power must be 2 or less"
    ))

// Target blue or red creature
new ActivatedAbility(false, "{2}", List.of(new BoostTargetCreatureEffect(1, 0)), true,
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
new ActivatedAbility(requiresTap, manaCost, effects, needsTarget, description, maxActivationsPerTurn)
```

**Use when:** Ability text says "Activate only once each turn" or similar.

```java
// Activate only once per turn
new ActivatedAbility(false, "{2}", List.of(new BoostSelfEffect(2, 2)), false,
    "{2}: This creature gets +2/+2 until end of turn. Activate only once each turn.", 1)
```

**Note:** This overload has the same parameter types as the targetFilter variant (`String` for description, then `Integer` vs `TargetFilter`), so the compiler resolves them by type. Use this when you need a per-turn limit but no target filter.

---

### 4. Ability with spell target filter

```java
new ActivatedAbility(requiresTap, manaCost, effects, needsTarget, needsSpellTarget, description, targetFilter)
```

**Use when:** Ability targets a spell on the stack (e.g. an activated counter ability). Set `needsSpellTarget=true` and provide a `StackEntryPredicateTargetFilter`.

**Note:** For spell cards (not abilities) that target spells, use `setNeedsSpellTarget(true)` and `setTargetFilter(...)` on the Card directly instead.

---

### 5. Ability with timing restriction

```java
new ActivatedAbility(requiresTap, manaCost, effects, needsTarget, description, timingRestriction)
```

**Use when:** The ability can only be activated at specific times.

```java
// Regenerate, only while this land is animated as a creature
new ActivatedAbility(false, "{B}", List.of(new RegenerateEffect()), false,
    "{B}: Regenerate this creature.",
    ActivationTimingRestriction.ONLY_WHILE_CREATURE)

// Sorcery-speed sacrifice ability
new ActivatedAbility(false, null,
    List.of(new SacrificeSelfCost(), new ChooseCardFromTargetHandToDiscardEffect(1, List.of())),
    true,
    "Sacrifice: Target player reveals their hand...",
    ActivationTimingRestriction.SORCERY_SPEED)
```

Cards: `SpawningPool` (ONLY_WHILE_CREATURE), `ThrullSurgeon` (SORCERY_SPEED), `ColossusOfSardia` (ONLY_DURING_YOUR_UPKEEP), `SkyshroudRanger` (SORCERY_SPEED)

---

### 6. Loyalty ability (planeswalkers)

```java
new ActivatedAbility(loyaltyCost, effects, needsTarget, description)
```

**Use when:** Planeswalker loyalty ability with no target restrictions.

```java
// +1: Create a token
// CreateCreatureTokenWithColorsEffect(int amount, String tokenName, int power, int toughness, Set<CardColor> colors, CardColor primaryColor, List<CardSubtype> subtypes)
// Convenience: CreateCreatureTokenWithColorsEffect(String tokenName, int power, int toughness, Set<CardColor> colors, CardColor primaryColor, List<CardSubtype> subtypes) — defaults amount to 1
new ActivatedAbility(+1, List.of(new CreateCreatureTokenWithColorsEffect(
        "Kithkin", 1, 1,
        Set.of(CardColor.GREEN, CardColor.WHITE),
        CardColor.GREEN,
        List.of(CardSubtype.KITHKIN))), false,
    "+1: Create a 1/1 green and white Kithkin creature token.")

// -8: Ultimate
new ActivatedAbility(-8, List.of(new AjaniUltimateEffect()), false,
    "\u22128: Look at the top X cards...")
```

Cards: `AjaniOutlandChaperone`

---

### 7. Loyalty ability with target filter

```java
new ActivatedAbility(loyaltyCost, effects, needsTarget, description, targetFilter)
```

**Use when:** Planeswalker loyalty ability that targets a specific permanent.

```java
// -2: Deal 4 damage to target tapped creature
new ActivatedAbility(-2, List.of(new DealDamageToTargetCreatureEffect(4)), true,
    "\u22122: Ajani deals 4 damage to target tapped creature.",
    new PermanentPredicateTargetFilter(
        new PermanentIsTappedPredicate(),
        "Target must be a tapped creature"
    ))
```

Cards: `AjaniOutlandChaperone`

---

### 8. Equipment ability (equip with sorcery speed + controlled creature filter)

```java
new ActivatedAbility(false, manaCost, List.of(new EquipEffect()), true, false,
    "Equip " + manaCost,
    new ControlledPermanentPredicateTargetFilter(
        new PermanentIsCreaturePredicate(),
        "Target must be a creature you control"
    ),
    null,
    ActivationTimingRestriction.SORCERY_SPEED)
```

**Use when:** Any equipment card with equip cost. This is the standard equip pattern — always the same structure, only the mana cost varies.

Cards: `LoxodonWarhammer` ({3}), `LeoninScimitar` ({1}), `BarkOfDoran` ({1}), `WhispersilkCloak` ({2})

---

### 9. Full constructor (all parameters)

```java
new ActivatedAbility(requiresTap, manaCost, effects, needsTarget, needsSpellTarget,
    description, targetFilter, loyaltyCost, maxActivationsPerTurn, timingRestriction)
```

**Use when:** None of the simpler overloads fit. Pass `null` for unused optional parameters.

---

## Costs in the effects list

Sacrifice and discard costs go in the `effects` list BEFORE the actual effect. The engine processes them in order.

| Cost effect | Constructor | Use when |
|------------|-------------|----------|
| `SacrificeSelfCost` | `()` | "Sacrifice this: ..." |
| `SacrificeCreatureCost` | `()` | "Sacrifice a creature: ..." |
| `SacrificeSubtypeCreatureCost` | `(CardSubtype)` | "Sacrifice a Goblin: ..." |
| `SacrificeAllCreaturesYouControlCost` | `()` | "Sacrifice all creatures: ..." |
| `DiscardCardTypeCost` | `(CardType)` | "Discard a [type] card: ..." |

```java
// {1}{R}, Sacrifice a Goblin: Deal 2 damage to any target
new ActivatedAbility(false, "{1}{R}",
    List.of(new SacrificeSubtypeCreatureCost(CardSubtype.GOBLIN), new DealDamageToAnyTargetEffect(2)),
    true,
    "{1}{R}, Sacrifice a Goblin: Siege-Gang Commander deals 2 damage to any target.")

// Sacrifice self: Gain 3 life
new ActivatedAbility(false, null,
    List.of(new SacrificeSelfCost(), new GainLifeEffect(3)),
    false,
    "Sacrifice Bottle Gnomes: You gain 3 life.")
```

Cards: `SiegeGangCommander`, `BottleGnomes`, `DoomedNecromancer`, `ThrullSurgeon`, `BloodfireColossus`

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

For spells (instants/sorceries) that need targets, use these `Card` setters in the constructor instead of ActivatedAbility:

```java
setNeedsTarget(true);                    // targets a permanent or player
setNeedsSpellTarget(true);               // targets a spell on the stack
setTargetFilter(new SomeTargetFilter()); // restricts valid targets
addEffect(EffectSlot.SPELL, effect);     // effect resolved when spell resolves
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
| `ON_BECOMES_BLOCKED` | This creature becomes blocked |
| `ON_COMBAT_DAMAGE_TO_PLAYER` | This creature deals combat damage to a player |
| `ON_COMBAT_DAMAGE_TO_CREATURE` | This creature deals combat damage to a creature |
| `ON_DAMAGE_TO_PLAYER` | Any damage to a player (not just combat) |
| `ON_DEATH` | This permanent dies |
| `ON_SACRIFICE` | This permanent is sacrificed |
| `ON_ALLY_CREATURE_ENTERS_BATTLEFIELD` | A creature enters battlefield under your control |
| `ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD` | Any other creature enters battlefield |
| `ON_ALLY_CREATURE_DIES` | A creature you control dies |
| `ON_DAMAGED_CREATURE_DIES` | A creature damaged by this permanent dies |
| `ON_ANY_PLAYER_CASTS_SPELL` | Any player casts a spell |
| `ON_ANY_PLAYER_TAPS_LAND` | Any player taps a land |