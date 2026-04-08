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
| `loyaltyCost` | `Integer` | Planeswalker loyalty cost (e.g. `+1`, `-2`, `-8`). `null` for non-planeswalker abilities. `0` when `variableLoyaltyCost` is `true` |
| `variableLoyaltyCost` | `boolean` | `true` for -X loyalty abilities where X is chosen by the player. The chosen X is passed as xValue |
| `maxActivationsPerTurn` | `Integer` | Maximum activations per turn. `null` for unlimited |
| `timingRestriction` | `ActivationTimingRestriction` | When the ability can be activated. `null` for default (instant speed) |
| `requiredControlledSubtype` | `CardSubtype` | Subtype you must control N+ of to activate (e.g. `CardSubtype.VAMPIRE`). `null` for no restriction |
| `requiredControlledSubtypeCount` | `int` | Minimum count of `requiredControlledSubtype` permanents you must control. `0` when unused |

**Targeting is computed from effects** — `ActivatedAbility.isNeedsTarget()` and `isNeedsSpellTarget()` are derived getters, never stored as fields. For Cards, use `EffectResolution.needsTarget(card)` / `EffectResolution.needsSpellTarget(card)` instead. Override `canTargetPlayer()`, `canTargetPermanent()`, `canTargetSpell()`, or `canTargetGraveyard()` on your effect record to return `true`.

### ActivationTimingRestriction values

| Value | Use when |
|-------|----------|
| `SORCERY_SPEED` | Equip abilities, sorcery-speed activated abilities |
| `ONLY_DURING_YOUR_TURN` | Activate only during your turn (any phase/step, instant speed) |
| `ONLY_DURING_YOUR_UPKEEP` | Abilities that can only be used during your upkeep |
| `ONLY_WHILE_ATTACKING` | Activate only if this creature is attacking (checks `permanent.isAttacking()`) |
| `ONLY_WHILE_CREATURE` | Abilities on creature lands that only work while animated |
| `METALCRAFT` | Activate only if you control three or more artifacts |
| `MORBID` | Activate only if a creature died this turn (checks `gameQueryService.isMorbidMet()`) |
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
    List.of(new SacrificeSelfCost(), new ChooseCardFromTargetHandToDiscardEffect(1, List.of())),
    "Sacrifice: Target player reveals their hand...",
    ActivationTimingRestriction.SORCERY_SPEED)
```

Cards: `SpawningPool` (ONLY_WHILE_CREATURE), `ThrullSurgeon` (SORCERY_SPEED), `ColossusOfSardia` (ONLY_DURING_YOUR_UPKEEP), `SkyshroudRanger` (SORCERY_SPEED), `VonaButcherOfMagan` (ONLY_DURING_YOUR_TURN)

**Note:** For abilities that target spells on the stack (e.g. activated counter ability), spell targeting is auto-derived from effects (e.g. `CounterUnlessPaysEffect.canTargetSpell()` returns `true`). Use a `StackEntryPredicateTargetFilter` if target legality is restricted.

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
    List.of(new DealXDamageToTargetCreatureEffect()),
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

---

## Costs in the effects list

Sacrifice and discard costs go in the `effects` list BEFORE the actual effect. The engine processes them in order.

All cost effects implement the `CostEffect` marker interface (which extends `CardEffect`). When creating a new cost effect, implement `CostEffect` instead of `CardEffect` — this ensures it is automatically filtered out during effect snapshotting and excluded from mana ability detection.

| Cost effect | Constructor | Use when |
|------------|-------------|----------|
| `SacrificeSelfCost` | `()` | "Sacrifice this: ..." |
| `SacrificeCreatureCost` | `()` | "Sacrifice a creature: ..." |
| `SacrificeCreatureCost` | `(false, false, false, true)` | "Sacrifice another creature: ..." (excludeSelf prevents sacrificing the source) |
| `SacrificeSubtypeCreatureCost` | `(CardSubtype)` | "Sacrifice a Goblin: ..." |
| `SacrificeArtifactCost` | `()` | "Sacrifice an artifact: ..." |
| `SacrificePermanentCost` | `(PermanentPredicate filter, String description)` | "Sacrifice an artifact or creature: ..." — generic predicate-based sacrifice. E.g. `new SacrificePermanentCost(new PermanentAnyOfPredicate(List.of(new PermanentIsArtifactPredicate(), new PermanentIsCreaturePredicate())), "Sacrifice an artifact or creature")` |
| `SacrificeMultiplePermanentsCost` | `(int count, PermanentPredicate filter)` | "Sacrifice three artifacts: ..." (use with matching predicate) |
| `ReturnMultiplePermanentsToHandCost` | `(int count, PermanentPredicate filter)` | "Return two lands you control to their owner's hand: ..." (bounces N matching permanents as cost). Works with both battlefield and graveyard activated abilities |
| `SacrificeAllCreaturesYouControlCost` | `()` | "Sacrifice all creatures: ..." |
| `DiscardCardTypeCost` | `(CardPredicate, String label)` | "Discard a [label] card: ..." (null predicate = any card). E.g. `(new CardTypePredicate(CardType.LAND), "land")`, `(new CardIsHistoricPredicate(), "historic")`, `(null, null)` for any |
| `ExileCardFromGraveyardCost` | `(CardType)` or `(CardType, boolean payManaCost, boolean imprint, boolean trackPower)` | "Exile a [type] card from your graveyard: ..." (null = any type). For spells: use in SPELL slot with `trackExiledPower=true` to set X to exiled card's power |
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

For spells (instants/sorceries) that need targets, targeting is auto-derived from effects. Override `canTargetPlayer()`, `canTargetPermanent()`, `canTargetSpell()`, or `canTargetGraveyard()` on your effect record to return `true`. Then in the card constructor:

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
| `UPKEEP_TRIGGERED` | Controller's upkeep. Supports single-player targeting (e.g. Bloodgift Demon via `pendingUpkeepPlayerTargets`) and multi-player targeting (e.g. Axis of Mortality via `pendingUpkeepMultiPlayerTargets` when any effect has `requiredPlayerTargetCount() >= 2`) |
| `EACH_UPKEEP_TRIGGERED` | Each player's upkeep |
| `OPPONENT_UPKEEP_TRIGGERED` | Each opponent's upkeep |
| `ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED` | Upkeep of the enchanted permanent's controller (fires regardless of which player controls the aura). `affectedPlayerId` is baked in at trigger time for effects like `EnchantedCreatureControllerLosesLifeEffect` |
| `ENCHANTED_PLAYER_UPKEEP_TRIGGERED` | Upkeep of the enchanted player (for player auras/Curses). The enchanted player's ID is passed as `targetId` on the stack entry. Curse subtype is auto-detected via `isEnchantPlayer()` |
| `GRAVEYARD_UPKEEP_TRIGGERED` | Upkeep trigger from graveyard |
| `GRAVEYARD_ON_CONTROLLER_CASTS_SPELL` | Spell-cast trigger from graveyard — fires when the controller casts a spell matching the `SpellCastTriggerEffect.spellFilter()` while this card is in their graveyard (e.g. Lingering Phantom). Supports `manaCost` for "you may pay" patterns |
| `DRAW_TRIGGERED` | At the beginning of controller's draw step (draw step only, not spell draws) |
| `EACH_DRAW_TRIGGERED` | At the beginning of any player's draw step (draw step only) |
| `ON_CONTROLLER_DRAWS` | Whenever controller draws a card (all draws: draw step, spells, abilities) |
| `ON_OPPONENT_DRAWS` | Whenever an opponent draws a card (all draws: draw step, spells, abilities) |
| `ON_OPPONENT_DISCARDS` | An opponent discards a card |
| `ON_SELF_DISCARDED_BY_OPPONENT` | This card is discarded by an opponent |
| `END_STEP_TRIGGERED` | End step (any player's turn — "at the beginning of the end step") |
| `CONTROLLER_END_STEP_TRIGGERED` | Controller's end step only ("at the beginning of your end step") |
| `ON_ATTACK` | This creature attacks |
| `ON_ALLY_CREATURES_ATTACK` | One or more creatures the controller controls attack (fires once per combat, not per creature). Scans all controller's permanents after attackers declared |
| `GRAVEYARD_ON_ALLY_CREATURES_ATTACK` | Like ON_ALLY_CREATURES_ATTACK but fires from the controller's graveyard. The attacker count is passed via xValue. Supports `MinimumAttackersConditionalEffect` wrapper for "N or more creatures" conditions. Used by Warcry Phoenix |
| `ON_ALLY_CREATURE_ATTACKS` | Fires once per attacking creature the controller controls (unlike ON_ALLY_CREATURES_ATTACK which fires once per combat). Scans all controller's permanents for each attacker. Supports `SubtypeConditionalEffect` to filter by the attacking creature's subtype. Used by Sanctum Seeker |
| `ON_ALLY_CREATURE_EXPLORES` | Whenever a creature you control explores. Fires after the explore process completes (land into hand, or +1/+1 counter and may-graveyard choice). Supports targeted effects (e.g. BoostTargetCreatureEffect) via `pendingExploreTriggerTargets` queue — targets restricted to opponent's creatures. Used by Lurking Chupacabra |
| `ON_BLOCK` | This creature blocks |
| `ON_BECOMES_BLOCKED` | This creature becomes blocked. Register effects with `TriggerMode.PER_BLOCKER` to fire once per blocker |
| `ON_COMBAT_DAMAGE_TO_PLAYER` | This creature deals combat damage to a player |
| `ON_COMBAT_DAMAGE_TO_CREATURE` | This creature deals combat damage to a creature |
| `ON_DAMAGE_TO_PLAYER` | Any damage to a player (not just combat) |
| `ON_DEATH` | This permanent dies |
| `ON_SACRIFICE` | This permanent is sacrificed |
| `ON_ALLY_CREATURE_ENTERS_BATTLEFIELD` | A creature enters battlefield under your control |
| `ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD` | An artifact enters battlefield under your control (not this permanent) |
| `ON_ALLY_NONTOKEN_ARTIFACT_ENTERS_BATTLEFIELD` | A nontoken artifact enters battlefield under your control (not this permanent). Used with MayPayManaEffect for Mirrorworks' copy trigger. Entering permanent ID is passed via PendingMayAbility.targetCardId |
| `ON_ANY_CREATURE_DIES` | Any creature (including tokens) on any battlefield dies. Fires for all permanents on all battlefields. Supports targeted effects via pendingDeathTriggerTargets (e.g. Falkenrath Noble) |
| `ON_ANY_NONTOKEN_CREATURE_DIES` | Any nontoken creature on any battlefield dies (not just controller's). Used with MayEffect for Mimic Vat's imprint trigger |
| `ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD` | Any artifact (any player's) is put into a graveyard from the battlefield. Fires for destroy, sacrifice, etc. |
| `ON_ARTIFACT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD` | An artifact is put into an opponent's graveyard from the battlefield. Only fires when the graveyard owner is an opponent of this permanent's controller. Supports MayEffect wrapping. |
| `ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD` | Any other creature enters battlefield |
| `ON_ALLY_CREATURE_DIES` | A creature you control dies. Supports SubtypeConditionalEffect wrapping to filter by dying creature's subtype (e.g. Slimefoot only triggers for Saprolings) |
| `ON_ALLY_NONTOKEN_CREATURE_DIES` | A nontoken creature you control dies. Only fires for nontoken creatures (tokens are excluded). Used by Gutter Grime |
| `ON_DAMAGED_CREATURE_DIES` | A creature damaged by this permanent dies |
| `ON_ANY_PLAYER_CASTS_SPELL` | Any player casts a spell |
| `ON_CONTROLLER_CASTS_SPELL` | Controller casts a spell ("whenever you cast...") |
| `ON_ANY_PLAYER_TAPS_LAND` | Any player taps a land |
| `ON_ENCHANTED_PERMANENT_TAPPED` | The permanent this aura is attached to becomes tapped. Does NOT fire for "enters tapped" (CR 603.6d). `affectedPlayerId` is baked in at trigger time with the enchanted permanent's controller |
| `ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU` | Any permanent deals damage to this permanent's controller |
| `ON_ALLY_PERMANENT_SACRIFICED` | A permanent you control is sacrificed (not this one — "another") |
| `ON_BECOMES_TARGET_OF_SPELL` | This permanent becomes target of a spell |
| `ON_BECOMES_TARGET_OF_OPPONENT_SPELL` | This permanent becomes target of an opponent's spell |
| `ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY` | This permanent becomes target of any spell or ability |
| `ON_ALLY_CREATURE_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY` | Global monitor: a creature you control becomes target of opponent's spell/ability. Used by Shapers' Sanctuary |
| `ON_EQUIPPED_CREATURE_DIES` | Equipped creature dies |
| `ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD` | Enchanted permanent dies (graveyard only) |
| `ON_ENCHANTED_PERMANENT_LEAVES_BATTLEFIELD` | Enchanted permanent leaves battlefield (any destination) |
| `ON_OPPONENT_LAND_ENTERS_BATTLEFIELD` | Opponent's land enters. Wrap with `PermanentEnteredThisTurnConditionalEffect` for "second+ land" |
| `ON_ALLY_LAND_ENTERS_BATTLEFIELD` | Your land enters (landfall) |
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
| `ON_OPPONENT_CREATURE_DEALT_DAMAGE` | An opponent's creature is dealt damage |
| `ON_CONTROLLER_LOSES_LIFE` | Controller loses life |
| `ON_SELF_LEAVES_BATTLEFIELD` | This permanent leaves the battlefield (any means) |
| `ON_ALLY_AURA_OR_EQUIPMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD` | Your Aura or Equipment dies |
| `ON_TRANSFORM_TO_BACK_FACE` | This permanent transforms to back face |
