# EFFECTS_INDEX

Purpose: cut token usage when implementing cards by quickly mapping "card text intent" to existing reusable effects and their constructor signatures.

## How to use this index

1. Parse card text into primitive actions (damage, draw, bounce, etc.).
2. Find each primitive below in the categorized sections and reuse existing effects.
3. Only add new effect records when no existing effect can express the behavior.
4. If you add a new effect record, add an `@HandlesEffect`-annotated resolver method in the matching `*ResolutionService` (see provider map at bottom). No manual registration needed — the annotation auto-registers the handler at startup.
5. If your new effect targets something, override the appropriate `canTarget*()` method(s) on `CardEffect` to return `true` (see targeting section below).
6. If your new effect requires target validation, add a `@ValidatesTarget`-annotated method in the appropriate validator class under `service/validate/` (see target validator map at bottom).

## Marker interfaces

| Interface | Extends | Purpose |
|-----------|---------|---------|
| `CostEffect` | `CardEffect` | Marks effects that represent additional costs (sacrifice, discard, exile, counter removal, tap creature). Cost effects are filtered out during effect snapshotting and excluded from mana ability detection. Implement this instead of `CardEffect` for new cost effects. |
| `ManaProducingEffect` | `CardEffect` | Marks effects that produce mana. Used to identify mana abilities (CR 605.1a) without listing individual effect types. Implement this instead of `CardEffect` for new mana-producing effects. |

### `isSelfTargeting()` default method on `CardEffect`

Effects that implicitly target their source permanent (boost-self, animate-self, regenerate-self, etc.) override `isSelfTargeting()` to return `true`. This is used by `ActivatedAbilityExecutionService` to auto-assign the source as the target when no explicit target is provided.

Effects returning `true`: `BoostSelfEffect`, `UntapSelfEffect`, `AnimateSelfEffect`, `AnimateSelfByChargeCountersEffect`, `AnimateSelfWithStatsEffect`, `AnimateLandEffect`, `PutChargeCounterOnSelfEffect`.

Conditional: `RegenerateEffect` → `!targetsPermanent()`, `GrantKeywordEffect` → `scope == SELF`.

### `isPowerToughnessDefining()` default method on `CardEffect`

Effects that are characteristic-defining abilities (CDAs) for power/toughness (`*/*` effects) override `isPowerToughnessDefining()` to return `true`. Per CR 707.9d, when a copy effect provides specific P/T values (e.g. "except it's 7/7"), CDAs that define P/T are not copied. Used by `CopyPermanentOnEnterEffect` with `powerOverride`/`toughnessOverride`.

Effects returning `true`: `PowerToughnessEqualToControlledLandCountEffect`, `PowerToughnessEqualToControlledCreatureCountEffect`, `PowerToughnessEqualToControlledPermanentCountEffect`, `PowerToughnessEqualToControlledSubtypeCountEffect`, `PowerToughnessEqualToCreatureCardsInAllGraveyardsEffect`, `PowerToughnessEqualToCardsInHandEffect`.

---

## Effect targeting declarations

Effects declare what they can target via default methods on `CardEffect`. `Card.isNeedsTarget()` and `Card.isNeedsSpellTarget()` are derived automatically — never call `setNeedsTarget`/`setNeedsSpellTarget`.

When creating a new effect, override the relevant method(s) to return `true`:

| Method | Returns `true` on these effects |
|--------|---------------------------------|
| `canTargetPlayer()` | DealDamageToAnyTargetEffect, DealDamageEqualToSourcePowerToAnyTargetEffect, DealDamageToAnyTargetAndGainLifeEffect, DealDamageToTargetPlayerEffect, DealDamageToTargetPlayerByHandSizeEffect, DealOrderedDamageToAnyTargetsEffect, DealXDamageToAnyTargetEffect, DealXDamageToAnyTargetAndGainXLifeEffect, TargetPlayerLosesLifeEffect, TargetPlayerLosesLifeAndControllerGainsLifeEffect, TargetPlayerGainsLifeEffect, DoubleTargetPlayerLifeEffect, TargetPlayerDiscardsEffect, TargetPlayerDiscardsReturnSelfIfCardTypeEffect, ChooseCardFromTargetHandToDiscardEffect, ChooseCardsFromTargetHandToTopOfLibraryEffect, LookAtHandEffect, HeadGamesEffect, RedirectDrawsEffect, MillTargetPlayerEffect, MillTargetPlayerByChargeCountersEffect, MillHalfLibraryEffect, ExtraTurnEffect, SacrificeCreatureEffect, SacrificeAttackingCreaturesEffect, ShuffleGraveyardIntoLibraryEffect, RevealTopCardDealManaValueDamageEffect, RevealTopCardOfLibraryEffect, ReturnArtifactsTargetPlayerOwnsToHandEffect, TargetPlayerGainsControlOfSourceCreatureEffect, PutMinusOneMinusOneCounterOnEachCreatureTargetPlayerControlsEffect, GiveTargetPlayerPoisonCountersEffect, ExileTargetPlayerGraveyardEffect, DrawXCardsForTargetPlayerEffect |
| `canTargetPermanent()` | DealDamageToAnyTargetEffect, DealDamageEqualToSourcePowerToAnyTargetEffect, DealDamageToAnyTargetAndGainLifeEffect, DealDamageToTargetCreatureEffect, DealDamageToTargetCreatureEqualToControlledSubtypeCountEffect, DealOrderedDamageToAnyTargetsEffect, DealXDamageToAnyTargetEffect, DealXDamageToAnyTargetAndGainXLifeEffect, DealXDamageToTargetCreatureEffect, DealXDamageDividedAmongTargetAttackingCreaturesEffect, FirstTargetDealsPowerDamageToSecondTargetEffect, DestroyTargetPermanentEffect, DestroyTargetLandAndDamageControllerEffect, DestroyTargetPermanentAndGiveControllerPoisonCountersEffect, DestroyCreatureBlockingThisEffect, ExileTargetPermanentEffect, ReturnTargetPermanentToHandEffect, PutTargetOnBottomOfLibraryEffect, PutTargetOnTopOfLibraryEffect, GainControlOfTargetCreatureUntilEndOfTurnEffect, GainControlOfTargetPermanentUntilEndOfTurnEffect, GainControlOfTargetEquipmentUntilEndOfTurnEffect, GainControlOfEnchantedTargetEffect, GainControlOfTargetAuraEffect, BoostTargetCreatureEffect, BoostFirstTargetCreatureEffect, GainLifeEqualToTargetToughnessEffect, PreventDamageToTargetEffect, TapTargetPermanentEffect, TapOrUntapTargetPermanentEffect, UntapTargetPermanentEffect, MakeTargetUnblockableEffect, TargetCreatureCantBlockThisTurnEffect, ChangeColorTextEffect, EquipEffect, CantBlockSourceEffect, SacrificeCreatureCost, DestroyTargetAndControllerLosesLifePerCreatureDeathsEffect, GrantKeywordEffect (when scope == Scope.TARGET), GrantChosenKeywordToTargetEffect, PutMinusOneMinusOneCounterOnTargetCreatureEffect, UnattachEquipmentFromTargetPermanentsEffect, ExileTargetPermanentAndReturnAtEndStepEffect, AddCardTypeToTargetPermanentEffect, GrantColorUntilEndOfTurnEffect, MustBlockSourceEffect, GrantProtectionFromCardTypeUntilEndOfTurnEffect, SwitchPowerToughnessEffect |
| `canTargetSpell()` | CounterSpellEffect, CounterSpellIfControllerPoisonedEffect, CounterUnlessPaysEffect, CopySpellEffect, ChangeTargetOfTargetSpellWithSingleTargetEffect |
| `canTargetGraveyard()` | ReturnCardFromGraveyardEffect (when targetGraveyard=true), PutCardFromOpponentGraveyardOntoBattlefieldEffect, PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect |

Effects that target both players and permanents (any-target): DealDamageToAnyTargetEffect, DealDamageToAnyTargetAndGainLifeEffect, DealOrderedDamageToAnyTargetsEffect, DealXDamageToAnyTargetEffect, DealXDamageToAnyTargetAndGainXLifeEffect.

### Per-blocker trigger mode

Effects in the `ON_BECOMES_BLOCKED` slot can be registered with `TriggerMode.PER_BLOCKER` via `addEffect(slot, effect, TriggerMode.PER_BLOCKER)`, causing CombatService to create one stack entry per blocking creature (e.g. "whenever this creature becomes blocked **by a creature**"). Cards using this: `SylvanBasilisk`, `EngulfingSlagwurm` (ON_BECOMES_BLOCKED only), `InfiltrationLens`.

### Becomes-target-of-spell trigger

Effects in the `ON_BECOMES_TARGET_OF_SPELL` slot fire when the permanent (or the creature an equipment is attached to) becomes the target of a spell. Any targeting effect can be placed in this slot — e.g. `DealDamageToAnyTargetEffect` for "deal 2 damage to any target" when the equipped creature becomes targeted. Cards using this: `LivewireLash`.

## Wrapper / modifier effects

| Effect | Constructor | Description |
|--------|-------------|-------------|
| `MayEffect` | `(CardEffect wrapped, String prompt)` | Wraps any effect with "you may" choice. For "becomes blocked by a creature" triggers that fire once per blocker, register with `TriggerMode.PER_BLOCKER` via `addEffect()` (e.g. Infiltration Lens) |
| `MayPayManaEffect` | `(String manaCost, CardEffect wrapped, String prompt)` | Wraps any effect with "you may pay {X}. If you do, [effect]" choice. The mana cost is charged before resolving. Used for Spellbomb cycle and similar cards |
| `MetalcraftConditionalEffect` | `(CardEffect wrapped)` | Wraps any effect with metalcraft condition (3+ artifacts). For ETB triggers: checked at trigger time and resolution time, delegates targeting to wrapped effect. For static effects: wraps GrantKeywordEffect or StaticBoostEffect, applied only while metalcraft is met (selfOnly handler) |
| `PermanentEnteredThisTurnConditionalEffect` | `(CardEffect wrapped, CardPredicate predicate, int minCount)` | Wraps any effect with "if at least N permanents matching predicate entered the battlefield under that player's control this turn" condition. Checked at trigger time via `permanentsEnteredBattlefieldThisTurn`. Used by Tunnel Ignus with `CardTypePredicate(LAND), 2` |
| `DefendingPlayerPoisonedConditionalEffect` | `(CardEffect wrapped)` | Wraps any effect with "if defending player is poisoned" condition. Used for ON_ATTACK triggers that only fire when opponent has 1+ poison counters. Used by Septic Rats |
| `NoOtherSubtypeConditionalEffect` | `(CardSubtype subtype, CardEffect wrapped)` | Intervening-if wrapper: "if you control no [subtype] other than this creature". Checked at trigger time and resolution time per CR 603.4. Used by Thopter Assembly with THOPTER subtype |
| `MetalcraftReplacementEffect` | `(CardEffect baseEffect, CardEffect metalcraftEffect)` | Picks between base and upgraded effect at resolution based on metalcraft. Resolves `metalcraftEffect` if 3+ artifacts, otherwise `baseEffect`. Targeting delegates to both inner effects (union). No new handler needed — unwrapped in `EffectResolutionService` |
| `ChooseOneEffect` | `(List<ChooseOneOption> options)` | Modal spell: defines the available modes for "Choose one" spells. Each `ChooseOneOption(String label, CardEffect effect)` or `ChooseOneOption(String label, CardEffect effect, TargetFilter targetFilter)` pairs a display label with its effect and optional per-mode target filter. **Mode is chosen at cast time** (per CR 700.2a): `SpellCastingService` unwraps the `ChooseOneEffect` using the `xValue` parameter as the mode index (0-based) and places only the chosen inner effect on the stack. For ETB creatures, the per-mode `targetFilter` (if present) is set on the triggered ability's StackEntry for resolution-time validation. Used by Slagstorm, Deceiver Exarch |

---

## Damage

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `DealDamageToAnyTargetEffect` | `(int damage, boolean cantRegenerate)` | deal N damage to any target |
| `DealDamageEqualToSourcePowerToAnyTargetEffect` | `()` | deal damage equal to source permanent's power to any target (uses effective power at resolution) |
| `DealDamageToTargetCreatureEffect` | `(int damage)` | deal N damage to target creature |
| `DealDamageToBlockedAttackersOnDeathEffect` | `(int damage)` | ON_DEATH marker: when this creature dies during combat, deal N damage to each creature it blocked this combat (e.g. Cathedral Membrane). Target permanent IDs baked in at trigger time from blockingTargetPermanentIds |
| `DealDamageToTargetPlayerEffect` | `(int damage)` | deal N damage to target player |
| `DealDamageToTargetPlayerByHandSizeEffect` | `()` | deal damage equal to hand size to target player |
| `MassDamageEffect` | `(int damage)` or `(int damage, boolean damagesPlayers)` or `(int damage, boolean usesXValue, boolean damagesPlayers, PermanentPredicate filter)` | deal N damage to all creatures (optionally filtered by predicate), optionally to all players too. Use `usesXValue=true` to use X value instead of fixed damage |
| `DealDamageToEachPlayerEffect` | `(int damage)` | deal N damage to each player (not creatures). Used by modal spells like Slagstorm's second mode |
| `DealDamageToAnyTargetAndGainLifeEffect` | `(int damage, int lifeGain)` | deal N damage and gain M life |
| `DealDamageToControllerEffect` | `(int damage)` | deal N damage to the card's controller (pain lands, self-damage) |
| `DealDamageToTargetControllerIfTargetHasKeywordEffect` | `(int damage, Keyword keyword)` | deal N damage to targeted creature's controller if that creature has the specified keyword |
| `DealDamageToDiscardingPlayerEffect` | `(int damage)` | deal N damage to any player who discards (trigger) |
| `DealDamageToTriggeringPermanentControllerEffect` | `(int damage)` | deal N damage to the controller of the permanent that caused the trigger (target pre-set at trigger-collection time) |
| `DealDamageToTargetCreatureEqualToControlledSubtypeCountEffect` | `(CardSubtype subtype)` | deal damage to target creature equal to number of controlled permanents of subtype |
| `DealDamageIfFewCardsInHandEffect` | `(int maxCards, int damage)` | deal N damage to target player if they have maxCards or fewer in hand |
| `DealDamageOnLandTapEffect` | `(int damage)` | deal N damage to a player whenever they tap a land (Manabarbs-style) |
| `DealDamageToEachOpponentEqualToCardsDrawnThisTurnEffect` | `()` | deal damage to each opponent equal to the number of cards that player has drawn this turn |
| `DealOrderedDamageToAnyTargetsEffect` | `(List<Integer> damageAmounts)` | deal different amounts to multiple targets (e.g. 3 then 1) |
| `DealXDamageToAnyTargetEffect` | `()` or `(boolean exileInsteadOfDie)` | deal X damage to any target (X spell). When `exileInsteadOfDie=true`, if target creature would die this turn, exile it instead (sets `exileInsteadOfDieThisTurn` flag, cleared at end of turn) |
| `DealXDamageToAnyTargetAndGainXLifeEffect` | `()` | deal X damage and gain X life (X spell) |
| `DealXDamageToTargetCreatureEffect` | `()` | deal X damage to target creature (X spell) |
| `DealXDamageDividedAmongTargetAttackingCreaturesEffect` | `()` | deal X damage divided among attacking creatures |
| `FirstTargetDealsPowerDamageToSecondTargetEffect` | `()` | first target creature deals damage equal to its power to second target creature (bite mechanic) |
| `DoubleDamageEffect` | `()` | double all damage dealt (static) |
| `SacrificeArtifactThenDealDividedDamageEffect` | `(int totalDamage)` | Sacrifice an artifact, then deal N total damage divided among any number of targets (creatures/players). Wrap in `MayEffect` for optional sacrifice. Damage assignments provided before cast via `pendingETBDamageAssignments`. Does NOT use standard targeting |
| `SacrificeOtherCreatureOrDamageEffect` | `(int damage)` | sacrifice another creature or take N damage (upkeep trigger) |
| `SpellCastTriggerEffect` | `(CardPredicate spellFilter, List<CardEffect> resolvedEffects)` or `(CardPredicate spellFilter, List<CardEffect> resolvedEffects, String manaCost)` | generic trigger descriptor: when a spell matching the predicate is cast, put the resolved effects on the stack. Works in both `ON_ANY_PLAYER_CASTS_SPELL` and `ON_CONTROLLER_CASTS_SPELL` slots. Wrap in `MayEffect` for optional triggers. Set `manaCost` (e.g. `"{1}"`) for "may pay" triggers. Use `spellFilter = null` for "whenever you cast a spell" (any spell). Replaces the old per-card descriptors (GainLifeOnSpellCastEffect, DealDamageToAnyTargetOnArtifactCastEffect, etc.) |

## Destruction / sacrifice

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `DestroyTargetPermanentEffect` | `(boolean cannotBeRegenerated)` or `(boolean cannotBeRegenerated, CreateCreatureTokenEffect tokenForController)` | destroy target permanent. When `tokenForController` is non-null, creates that token for the target's controller regardless of whether destruction succeeds (e.g. Beast Within, Pongify) |
| `DestroyAllPermanentsEffect` | `(Set<CardType> targetTypes)` or `(Set<CardType> targetTypes, boolean cannotBeRegenerated)` or `(Set<CardType> targetTypes, boolean onlyOpponents, boolean cannotBeRegenerated)` or `(Set<CardType> targetTypes, boolean onlyOpponents, boolean cannotBeRegenerated, PermanentPredicate filter)` | destroy all permanents of given types. Optional predicate filter for additional conditions |
| `DestroyAllCreaturesAndCreateTokenFromDestroyedCountEffect` | `(String tokenName, List<CardSubtype> tokenSubtypes, Set<CardType> tokenAdditionalTypes)` | destroy all creatures, then create a colorless X/X creature token where X = number actually destroyed (skips indestructible/regenerated) |
| `DestroyTargetLandAndDamageControllerEffect` | `(int damage)` | destroy target land and deal N to its controller |
| `DestroyTargetPermanentAndGiveControllerPoisonCountersEffect` | `(int poisonCounters)` or `()` (default 1) | destroy target permanent and give its controller N poison counters |
| `DestroyBlockedCreatureAndSelfEffect` | `()` | destroy creature this blocks and itself (Deathtrap-style) |
| `DestroyCreatureBlockingThisEffect` | `()` | destroy creature that blocks this (combat trigger) |
| `DestroyTargetCreatureAndGainLifeEqualToToughnessEffect` | `()` | destroy target creature and gain life equal to its toughness (combat trigger, life gain occurs even if destroy fails). Works with both ON_BLOCK and ON_BECOMES_BLOCKED slots |
| `DestroyTargetPermanentAndBoostSelfByManaValueEffect` | `()` | destroy target permanent and boost source creature +X/+0 until end of turn, where X is the permanent's mana value. Boost applies even if destruction fails (indestructible). Target type restriction handled by ability's target filter. Used by Hoard-Smelter Dragon |
| `DestroyTargetPermanentAndGainLifeEqualToManaValueEffect` | `()` | destroy target permanent and gain life equal to its mana value. Life gain occurs even if destruction fails (indestructible). Target type restriction handled by spell's target filter. Used by Divine Offering |
| `DestroyTargetAndControllerLosesLifePerCreatureDeathsEffect` | `()` | destroy target creature; its controller loses life equal to the number of creatures put into all graveyards from the battlefield this turn (counts ALL players' creature deaths). Used with `SacrificeCreatureCost` for Flesh Allergy |
| `DestroyEquipmentAttachedToTargetCreatureEffect` | `()` | destroy all Equipment attached to the target creature. Uses same target as co-located damage effect. Place BEFORE any damage effect on the same spell (engine destroys creatures immediately on lethal damage, clearing attachedTo). Resolved by `DestructionResolutionService` |
| `SacrificeCreatureEffect` | `()` | controller sacrifices a creature |
| `SacrificeAttackingCreaturesEffect` | `(int baseCount, int metalcraftCount)` | target player sacrifices attacking creatures; metalcraft upgrades count |
| `EachOpponentSacrificesCreatureEffect` | `()` | each opponent sacrifices a creature |
| `SacrificeSelfEffect` | `()` | sacrifice this permanent |
| `SacrificeUnlessDiscardCardTypeEffect` | `(CardType requiredType)` | sacrifice unless you discard a card of type (null = any) |
| `SacrificeUnlessReturnOwnPermanentTypeToHandEffect` | `(CardType permanentType)` | sacrifice this permanent unless you return a permanent of the specified type you control to its owner's hand (ETB bounce-or-sacrifice, e.g. Glint Hawk) |
| `DestroyNonlandPermanentsWithManaValueEqualToChargeCountersEffect` | `()` | destroy each nonland permanent with mana value equal to the number of charge counters on source (reads snapshotted count from xValue). Used by Ratchet Bomb |
| `DestroyNonlandPermanentsWithManaValueXDealtCombatDamageEffect` | `()` | destroy each nonland permanent with mana value equal to X whose controller was dealt combat damage by the source permanent this turn. Reads X from xValue, checks combatDamageToPlayersThisTurn. Used by Steel Hellkite |
| `SacrificeSelfToDestroyCreatureDamagedPlayerControlsEffect` | `()` | "sacrifice this, then destroy target creature that player controls." Wrap in `MayEffect` for "you may" behavior. Used on ON_COMBAT_DAMAGE_TO_PLAYER triggers where "that player" is the damaged player. StackEntry context: targetPermanentId = damaged player, sourcePermanentId = source creature. Presents multi-permanent choice (max 1). Resolved by `DestructionResolutionService` |
| `SacrificeAtEndOfCombatEffect` | `()` | sacrifice at end of combat |
| `SacrificeTargetThenRevealUntilTypeToBattlefieldEffect` | `(Set<CardType> cardTypes)` | sacrifice the targeted permanent, then its controller reveals cards from the top of their library until a card matching one of the specified types is found; that card is put onto the battlefield under that player's control, and all other revealed cards are shuffled into their library. Polymorph-style effect (used by Shape Anew for artifact→artifact) |

### Sacrifice costs (for activated abilities)

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `SacrificeSelfCost` | `()` | sacrifice this permanent as cost |
| `SacrificeCreatureCost` | `()` or `(boolean trackSacrificedManaValue)` | sacrifice a creature as cost. When `trackSacrificedManaValue=true`, the sacrificed creature's mana value is stored in the StackEntry's xValue for use by effects like `SearchLibraryForCreatureWithExactMVToBattlefieldEffect` (e.g. Birthing Pod) |
| `SacrificeSubtypeCreatureCost` | `(CardSubtype subtype)` | sacrifice a creature of specific subtype as cost |
| `SacrificeArtifactCost` | `()` | sacrifice an artifact as cost (works for both activated abilities and spell costs) |
| `SacrificePermanentCost` | `(PermanentPredicate filter, String description)` | sacrifice a permanent matching filter as cost. Uses `GameQueryService.matchesPermanentPredicate()` for validation. E.g. `new SacrificePermanentCost(new PermanentAnyOfPredicate(List.of(new PermanentIsArtifactPredicate(), new PermanentIsCreaturePredicate())), "Sacrifice an artifact or creature")` |
| `SacrificeMultiplePermanentsCost` | `(int count, PermanentPredicate filter)` | sacrifice N permanents matching filter as cost (e.g. "Sacrifice three artifacts: ..." with `PermanentIsArtifactPredicate`). Multi-step UI: if exactly N match, auto-sacrifices all; otherwise prompts one-at-a-time. Validated and paid in `AbilityActivationService` |
| `SacrificeAllCreaturesYouControlCost` | `()` | sacrifice all creatures you control as cost |
| `DiscardCardTypeCost` | `(CardType requiredType)` | discard a card of specific type as cost |
| `RemoveCounterFromSourceCost` | `(int count, CounterType counterType)` | remove N counters of the specified type from this permanent as cost. `CounterType.MINUS_ONE_MINUS_ONE` for "-1/-1 counters", `PLUS_ONE_PLUS_ONE` for "+1/+1 counters", `ANY` to prefer -1/-1 then +1/+1. Compact: `()` defaults to `(1, ANY)`, `(int count)` defaults to `(count, ANY)` |
| `RemoveChargeCountersFromSourceCost` | `(int count)` | remove N charge counters from source as cost (e.g. "Remove three charge counters: ..."). Validated and paid in `AbilityActivationService` |
| `TapCreatureCost` | `(PermanentPredicate predicate)` | tap an untapped creature matching predicate you control as cost. Auto-selects if only one valid target; presents permanent choice if multiple. The creature can be summoning sick (no tap symbol in cost). E.g. `new TapCreatureCost(new PermanentColorInPredicate(Set.of(CardColor.BLUE)))` for "tap an untapped blue creature" |
| `TapMultiplePermanentsCost` | `(int count, PermanentPredicate filter)` | tap N untapped permanents matching filter as cost. Auto-selects if exactly N valid; presents permanent choice one at a time if more. E.g. `new TapMultiplePermanentsCost(5, new PermanentHasSubtypePredicate(CardSubtype.MYR))` for "tap five untapped Myr you control" |
| `ExileCardFromGraveyardCost` | `(CardType requiredType)` | exile a card of specific type from your graveyard as cost (null = any). Two-phase async flow: prompts graveyard choice, then resumes activation. Validated and paid in `AbilityActivationService` |

## Counter spells

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `CounterSpellEffect` | `()` | counter target spell |
| `CounterSpellIfControllerPoisonedEffect` | `()` | counter target spell if its controller is poisoned (has at least one poison counter) |
| `CounterUnlessPaysEffect` | `(int amount)` | counter unless controller pays N generic mana |
| `RegisterDelayedCounterTriggerEffect` | `(int genericManaAmount)` | registers a delayed trigger (opening hand reveal) that counters each opponent's first spell unless they pay N generic mana. Handled by MayAbilityHandlerService, not GameService |
| `RegisterDelayedManaTriggerEffect` | `(ManaColor color, int amount)` | registers a delayed trigger (opening hand reveal) that adds N mana of the given color at the beginning of the revealing player's first precombat main phase. Handled by MayAbilityHandlerService, not GameService |
| `CreatureSpellsCantBeCounteredEffect` | `()` | creature spells can't be countered (static) |

## Bounce / return to hand

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `ReturnTargetPermanentToHandEffect` | `()` | return target permanent(s) to owner's hand; supports single-target via `targetPermanentId` and multi-target via `targetPermanentIds` |
| `ReturnCreaturesToOwnersHandEffect` | `(Set<TargetFilter> filters)` | return all creatures matching filters to owners' hands |
| `ReturnSelfToHandEffect` | `()` | return this permanent to owner's hand |
| `ReturnSelfToHandOnCoinFlipLossEffect` | `()` | return self to hand if coin flip is lost |
| `ReturnPermanentsOnCombatDamageToPlayerEffect` | `()` or `(PermanentPredicate filter)` | return permanents when combat damage dealt to player (Ninja-style). Optional filter restricts which permanents can be chosen (e.g. `PermanentIsCreaturePredicate` for creatures only) |
| `ReturnArtifactsTargetPlayerOwnsToHandEffect` | `()` | return all artifacts target player owns to hand |
| `BounceCreatureOnUpkeepEffect` | `(Scope scope, Set<TargetFilter> filters, String prompt)` | at upkeep, return a creature matching filters. Scope: `SOURCE_CONTROLLER`, `TRIGGER_TARGET_PLAYER` |
| `ReturnSelfToHandAndCreateTokensEffect` | `(CreateCreatureTokenEffect tokenEffect)` | return source to hand then create tokens (compound upkeep effect, e.g. Thopter Assembly) |
| `ReturnDamageSourcePermanentToHandEffect` | `()` | whenever a permanent deals damage to controller, return it to owner's hand (Dissipation Field-style). Use with `ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU` slot |
| `DamageSourceControllerGainsControlOfThisPermanentEffect` | `(boolean combatOnly, boolean creatureOnly)` | whenever a permanent deals damage to controller, the damage source's controller gains control of this permanent (Contested War Zone-style). Use with `ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU` slot. `combatOnly=true` restricts to combat damage only; `creatureOnly=true` restricts to creature damage sources only |
| `PutTargetOnBottomOfLibraryEffect` | `()` | put target permanent on bottom of owner's library |
| `PutTargetOnTopOfLibraryEffect` | `()` | put target permanent on top of owner's library |

## Graveyard return

### Unified effect: `ReturnCardFromGraveyardEffect`

All graveyard-to-hand and graveyard-to-battlefield return effects are handled by a single unified record.

**Canonical constructor:**
```
ReturnCardFromGraveyardEffect(
    GraveyardChoiceDestination destination,  // HAND or BATTLEFIELD
    CardPredicate filter,                    // which cards qualify (null = any)
    GraveyardSearchScope source,             // CONTROLLERS_GRAVEYARD, ALL_GRAVEYARDS, OPPONENT_GRAVEYARD
    boolean targetGraveyard,                 // true = player chooses whose graveyard to search at cast time
    boolean returnAll,                       // true = return all matching cards, false = choose one
    boolean thisTurnOnly,                    // true = only cards put there from battlefield this turn
    PermanentPredicate attachmentTarget,     // non-null = aura attaches to matching permanent on ETB
    boolean gainLifeEqualToManaValue,        // true = controller gains life equal to returned card's mana value
    boolean attachToSource                   // true = auto-attach returned equipment to the source permanent (e.g. Auriok Survivors)
)
```

**Convenience constructors:**

| Constructor | Equivalent canonical | When to use |
|-------------|---------------------|-------------|
| `(destination, filter)` | `(destination, filter, CONTROLLERS_GRAVEYARD, false, false, false, null, false, false)` | choose one from controller's graveyard (most common) |
| `(destination, filter, source)` | `(destination, filter, source, false, false, false, null, false, false)` | choose one from a specific scope (e.g. ALL_GRAVEYARDS) |
| `(destination, filter, targetGraveyard)` | `(destination, filter, CONTROLLERS_GRAVEYARD, targetGraveyard, false, false, null, false, false)` | targets graveyard at cast time (for spells like Recollect, Recover) |
| `(dest, filter, src, tgt, all, turn, attach, life)` | `(dest, filter, src, tgt, all, turn, attach, life, false)` | backward-compatible 8-param constructor |

**CardPredicate filter system** (in `model/filter/`):

| Predicate | Constructor | Matches |
|-----------|-------------|---------|
| `CardTypePredicate` | `(CardType cardType)` | cards of a given type (CREATURE, ARTIFACT, etc.) |
| `CardSubtypePredicate` | `(CardSubtype subtype)` | cards of a given subtype (ZOMBIE, GOBLIN, etc.) |
| `CardKeywordPredicate` | `(Keyword keyword)` | cards with a given keyword (INFECT, FLYING, etc.) |
| `CardIsSelfPredicate` | `()` | only the source card itself (Squee-style self-return) |
| `CardIsAuraPredicate` | `()` | aura cards |
| `CardNotPredicate` | `(CardPredicate predicate)` | NOT — inverts a predicate |
| `CardAllOfPredicate` | `(List<CardPredicate> predicates)` | AND — all predicates must match |
| `CardAnyOfPredicate` | `(List<CardPredicate> predicates)` | OR — any predicate matches |

Pass `null` as filter to allow any card.

**GraveyardSearchScope enum** (in `model/`):

| Value | Meaning |
|-------|---------|
| `CONTROLLERS_GRAVEYARD` | search only the controller's graveyard (default) |
| `ALL_GRAVEYARDS` | search any player's graveyard |
| `OPPONENT_GRAVEYARD` | search only opponent's graveyard |

**GraveyardChoiceDestination enum** (in `model/`):

| Value | Meaning |
|-------|---------|
| `HAND` | return chosen card(s) to hand |
| `BATTLEFIELD` | put chosen card(s) onto battlefield |

**Migration from old effects:**

| Old effect | New equivalent |
|------------|----------------|
| `ReturnCardFromGraveyardToHandEffect()` | `ReturnCardFromGraveyardEffect(HAND, null, true)` |
| `ReturnCardFromGraveyardToHandEffect(CardType.CREATURE)` | `ReturnCardFromGraveyardEffect(HAND, new CardTypePredicate(CREATURE))` |
| `ReturnCardOfSubtypeFromGraveyardToHandEffect(subtype)` | `ReturnCardFromGraveyardEffect(HAND, new CardSubtypePredicate(subtype))` |
| `ReturnCardWithKeywordFromGraveyardToHandEffect(type, kw)` | `ReturnCardFromGraveyardEffect(HAND, new CardAllOfPredicate(List.of(new CardTypePredicate(type), new CardKeywordPredicate(kw))))` |
| `ReturnSelfFromGraveyardToHandEffect()` | `ReturnCardFromGraveyardEffect(HAND, new CardIsSelfPredicate(), CONTROLLERS_GRAVEYARD, false, true, false, null)` |
| `ReturnCreatureFromGraveyardToBattlefieldEffect()` | `ReturnCardFromGraveyardEffect(BATTLEFIELD, new CardTypePredicate(CREATURE))` |
| `ReturnArtifactOrCreatureFromAnyGraveyardToBattlefieldEffect()` | `ReturnCardFromGraveyardEffect(BATTLEFIELD, new CardAnyOfPredicate(List.of(new CardTypePredicate(ARTIFACT), new CardTypePredicate(CREATURE))), ALL_GRAVEYARDS)` |
| `ReturnAuraFromGraveyardToBattlefieldEffect()` | `ReturnCardFromGraveyardEffect(BATTLEFIELD, new CardIsAuraPredicate(), CONTROLLERS_GRAVEYARD, false, false, false, attachmentTarget)` |
| `ReturnCreatureCardsPutIntoYourGraveyardFromBattlefieldThisTurnToHandEffect()` | `ReturnCardFromGraveyardEffect(HAND, new CardTypePredicate(CREATURE), CONTROLLERS_GRAVEYARD, false, true, true, null)` |

**Common usage examples:**

| Card | Usage |
|------|-------|
| Recollect | `ReturnCardFromGraveyardEffect(HAND, null, true)` — any card, targets graveyard |
| Gravedigger | `MayEffect(ReturnCardFromGraveyardEffect(HAND, new CardTypePredicate(CREATURE)))` — creature to hand |
| Corpse Cur | `ReturnCardFromGraveyardEffect(HAND, new CardAllOfPredicate(List.of(new CardTypePredicate(CREATURE), new CardKeywordPredicate(INFECT))))` — creature with infect |
| Lord of the Undead | `ReturnCardFromGraveyardEffect(HAND, new CardSubtypePredicate(ZOMBIE))` — Zombie subtype |
| Doomed Necromancer | `ReturnCardFromGraveyardEffect(BATTLEFIELD, new CardTypePredicate(CREATURE))` — creature to battlefield |
| Beacon of Unrest | `ReturnCardFromGraveyardEffect(BATTLEFIELD, new CardAnyOfPredicate(...), ALL_GRAVEYARDS)` — artifact or creature from any graveyard |
| Nomad Mythmaker | canonical constructor with `attachmentTarget = new PermanentIsCreaturePredicate()` — aura to battlefield attached to creature |
| Auriok Survivors | `MayEffect(ReturnCardFromGraveyardEffect(BATTLEFIELD, new CardSubtypePredicate(EQUIPMENT), CONTROLLERS_GRAVEYARD, false, false, false, null, false, true))` — equipment to battlefield attached to source |
| Squee, Goblin Nabob | `ReturnCardFromGraveyardEffect(HAND, new CardIsSelfPredicate(), CONTROLLERS_GRAVEYARD, false, true, false, null)` — self-return |
| No Rest for the Wicked | `ReturnCardFromGraveyardEffect(HAND, new CardTypePredicate(CREATURE), CONTROLLERS_GRAVEYARD, false, true, true, null, false)` — all creatures that died this turn |
| Razor Hippogriff | `ReturnCardFromGraveyardEffect(HAND, new CardTypePredicate(ARTIFACT), CONTROLLERS_GRAVEYARD, false, false, false, null, true)` — artifact to hand + gain life equal to mana value |

### Other graveyard effects

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `PutTargetCardsFromGraveyardOnTopOfLibraryEffect` | `(CardPredicate filter)` | put any number of target cards matching filter from controller's graveyard on top of library. Multi-target selection at cast time via SpellCastingService "any number" graveyard targeting. Used by Frantic Salvage |
| `ReturnTargetCardsFromGraveyardToHandEffect` | `(CardPredicate filter, int maxTargets)` | return up to maxTargets target cards matching filter from controller's graveyard to hand. Multi-target selection at cast time via SpellCastingService "up to N" graveyard targeting. Used by Morbid Plunder |
| `ReturnDyingCreatureToBattlefieldAndAttachSourceEffect` | `(UUID dyingCardId)` or `()` | return a dying nontoken creature to the battlefield and attach the source equipment to it. No-arg constructor (dyingCardId is null) used in card definition; dyingCardId populated at trigger time in GameHelper. Wrap in MayPayManaEffect for "you may pay {X}" triggers. Used by Nim Deathmantle |
| `PutCardFromOpponentGraveyardOntoBattlefieldEffect` | `(boolean tapped)` | put target artifact/creature with MV=X from opponent's graveyard onto battlefield under your control (tapped if `tapped=true`), then mill that player X cards |
| `PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect` | `()` | put target creature card from opponent's graveyard onto battlefield under your control with haste; exile at beginning of next end step; if it would leave the battlefield, exile it instead |
| `PutImprintedCreatureOntoBattlefieldEffect` | `()` | when this creature dies, reveal imprinted card; if creature, put onto battlefield (Clone Shell dies trigger) |

## Draw / discard / hand manipulation

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `DrawCardEffect` | `(int amount)` | draw N cards |
| `DrawCardForTargetPlayerEffect` | `(int amount, boolean requireSourceUntapped, boolean targetsPlayer)` | target player draws N cards; optionally requires source untapped; when `targetsPlayer=true`, auto-derives player targeting for activated abilities. Compact: `(int amount)` defaults to `(amount, false, false)` |
| `DrawXCardsForTargetPlayerEffect` | `()` | target player draws X cards (reads X from stack entry xValue; targets player) |
| `DrawCardsEqualToChargeCountersOnSourceEffect` | `()` | draw cards equal to charge counters on source (reads snapshotted count from xValue) |
| `DrawAndLoseLifePerSubtypeEffect` | `(CardSubtype subtype)` | draw cards and lose life for each permanent of subtype you control |
| `DiscardCardEffect` | `(int amount)` | discard N cards |
| `EachPlayerDiscardsEffect` | `(int amount)` | each player discards N cards in APNAP order (active player first). Uses queued sequential discard interaction. Controller's discard has `discardCausedByOpponent=false`; others have `true`. |
| `TargetPlayerDiscardsEffect` | `(int amount)` | target player discards N cards |
| `TargetPlayerDiscardsReturnSelfIfCardTypeEffect` | `(int amount, CardType returnIfType)` | target player discards N cards; if a discarded card matches the type, return the source spell from graveyard to owner's hand (e.g. Psychic Miasma) |
| `TargetPlayerRandomDiscardEffect` | `(int amount, boolean causedByOpponent)` | target player discards N cards at random. Convenience ctors: `()` → amount=1, causedByOpponent=true (e.g. Hypnotic Specter); `(int amount)` → causedByOpponent=false (e.g. Goblin Lore self-discard). When `causedByOpponent=true`, uses `entry.getTargetPermanentId()` for who discards and sets `discardCausedByOpponent = true`; when `false`, uses `entry.getControllerId()`. |
| `ChooseCardFromTargetHandToDiscardEffect` | `(int count, List<CardType> excludedTypes)` | choose N cards from target's hand to discard (excludedTypes can't be chosen) |
| `ChooseCardsFromTargetHandToTopOfLibraryEffect` | `(int count)` | choose N cards from target hand to put on top of library |
| `LookAtHandEffect` | `()` | look at target player's hand |
| `RevealOpponentHandsEffect` | `()` | reveal all opponents' hands |
| `HeadGamesEffect` | `()` | exchange target player's hand with cards from your library (Head Games) |
| `RedirectDrawsEffect` | `()` | redirect opponent's draws to controller (static, e.g. Plagiarize-style) |
| `ShuffleHandIntoLibraryAndDrawEffect` | `()` | each player shuffles cards from their hand into their library, then draws that many cards (wheel effect) |

## Library manipulation

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `SearchLibraryForCardToHandEffect` | `()` | search library for any card to hand |
| `SearchLibraryForBasicLandToHandEffect` | `()` | search library for basic land to hand |
| `SearchLibraryForCardTypesToHandEffect` | `(Set<CardType> cardTypes)` or `(Set<CardType> cardTypes, int maxManaValue)` or `(Set<CardType> cardTypes, int minManaValue, int maxManaValue)` | search library for card of specific types to hand (optionally filtered by min/max mana value) |
| `SearchLibraryForCardTypesToBattlefieldEffect` | `(Set<CardType> cardTypes, boolean requiresBasicSupertype, boolean entersTapped)` | search library for card to battlefield |
| `SearchLibraryForCardTypeToExileAndImprintEffect` | `(Set<CardType> cardTypes)` | search library for card of specific types, exile it, and imprint on source permanent |
| `SearchLibraryForCreatureWithMVXOrLessToHandEffect` | `()` | search library for creature with MV X or less to hand |
| `SearchLibraryForCreatureWithColorAndMVXOrLessToBattlefieldEffect` | `(CardColor requiredColor)` | search library for creature of specified color with MV X or less to battlefield |
| `SearchLibraryForCreatureWithSubtypeToBattlefieldEffect` | `(CardSubtype requiredSubtype)` | search library for creature card with specified subtype and put it onto the battlefield |
| `SearchLibraryForCreatureWithExactMVToBattlefieldEffect` | `(int mvOffset)` | search library for creature with MV exactly equal to xValue + mvOffset, put onto battlefield. Used with `SacrificeCreatureCost(true)` which stores sacrificed creature's MV in xValue (e.g. Birthing Pod with mvOffset=1) |
| `CastTopOfLibraryWithoutPayingManaCostEffect` | `(Set<CardType> castableTypes)` | look at top card of controller's library; if it matches one of the castable types, may cast it without paying its mana cost |
| `CastTargetInstantOrSorceryFromGraveyardEffect` | `(GraveyardSearchScope scope, boolean withoutPayingManaCost)` | ETB: target instant or sorcery from a graveyard matching scope, you may cast it (without paying mana cost if flag is true). Has `canTargetGraveyard()=true`. Graveyard targeting handled by GameHelper ETB flow |
| `DistantMemoriesEffect` | `()` | search library for any card, exile it, shuffle; opponent may let you have it, otherwise draw 3 |
| `SearchLibraryForCreatureToTopOfLibraryEffect` | `()` | search library for a creature card, reveal it, then shuffle and put that card on top of library |
| `PayManaAndSearchLibraryForCardNamedToBattlefieldEffect` | `(String manaCost, String cardName)` | pay mana, search for named card to battlefield |
| `LookAtTopCardsMayRevealCreaturePutIntoHandRestOnBottomEffect` | `(int count, Set<CardType> cardTypes)` or `(int count, Set<CardType> cardTypes, boolean anyNumber)` | look at top N, may reveal matching type to hand, rest on bottom; anyNumber=true allows multi-select |
| `LookAtTopCardsPutMatchingPermanentNameOnBattlefieldEffect` | `(int count)` | look at top N cards, may put one onto battlefield if it shares a name with any permanent, rest on bottom in any order |
| `ImprintFromTopCardsEffect` | `(int count)` | look at top N cards, exile one face down (imprint on source), rest on bottom in any order |
| `LookAtTopCardsHandTopBottomEffect` | `(int count)` | look at top N cards, choose hand/top/bottom for each |
| `ReorderTopCardsOfLibraryEffect` | `(int count)` | reorder top N cards of library |
| `RevealTopCardDealManaValueDamageEffect` | `(boolean damageTargetPlayer, boolean damageTargetCreatures, boolean returnToHandIfLand)` | reveal top card of target's library, deal mana value damage to player/creatures, optionally return to hand if land |
| `RevealTopCardOfLibraryEffect` | `()` | reveal top card of library (static/continuous) |
| `ExileSpellEffect` | `()` | exile this spell instead of putting it into the graveyard after resolution (marker, like ShuffleIntoLibraryEffect) |
| `ShuffleIntoLibraryEffect` | `()` | shuffle this permanent into owner's library |
| `ShuffleGraveyardIntoLibraryEffect` | `()` | shuffle graveyard into library |

## Mill

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `MillTargetPlayerEffect` | `(int count)` | target player mills N cards |
| `MillHalfLibraryEffect` | `()` | target player mills half their library |
| `MillByHandSizeEffect` | `()` | target player mills cards equal to hand size |
| `MillTargetPlayerByChargeCountersEffect` | `()` | target player mills X cards where X is charge counters on source (reads snapshotted count from xValue) |
| `EachOpponentMillsEffect` | `(int count)` | each opponent mills N cards |

## Exile

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `ExileTargetPermanentEffect` | `()` | exile target permanent (also handles multi-target via targetPermanentIds) |
| `ExileCardsFromGraveyardEffect` | `(int maxTargets, int lifeGain)` | exile up to N cards from graveyard, gain lifeGain per card |
| `ExileCreaturesFromGraveyardAndCreateTokensEffect` | `()` | exile creature cards from graveyard, create tokens for each |
| `ExileTopCardsRepeatOnDuplicateEffect` | `(int count)` | exile top N cards, repeat if duplicate names found |
| `ExileSelfAndReturnAtEndStepEffect` | `()` | exile this permanent, return it at beginning of next end step (Argent Sphinx-style) |
| `ExileTargetPermanentAndImprintEffect` | `()` | exile target permanent permanently and imprint the exiled card onto the source permanent; the card does NOT return when the source leaves (Exclusion Ritual-style) |
| `ExileTargetPermanentAndReturnAtEndStepEffect` | `()` | exile target permanent, return it at beginning of next end step under owner's control (Glimmerpoint Stag-style) |
| `ExileTargetPermanentUntilSourceLeavesEffect` | `()` | exile target permanent until source leaves the battlefield, then return it under owner's control (O-ring style). Tracked via `GameData.exileReturnOnPermanentLeave` map. Often wrapped in `MayEffect` for "you may" triggers |
| `ImprintDyingCreatureEffect` | `(UUID dyingCardId)` or `()` | exile a dying nontoken creature and imprint it on the source permanent; previously imprinted card is returned to its owner's graveyard. No-arg constructor (dyingCardId is null) used in card definition; dyingCardId populated at trigger time |
| `ExileFromHandToImprintEffect` | `(CardPredicate filter, String description)` | exiles a card matching the predicate from the controller's hand and imprints it on the source permanent. Description is used in the player prompt. Prototype Portal: `CardTypePredicate(ARTIFACT)`, Semblance Anvil: `CardNotPredicate(CardTypePredicate(LAND))` |
| `ChooseCardNameAndExileFromZonesEffect` | `(List<CardType> excludedTypes)` | Two-step interaction: (1) choose a card name (excluding given types), (2) present all matching cards from target player's hand, graveyard, and library for "any number" selection — player chooses 0 to N to exile. Library is always shuffled. Targets player. Uses `MULTI_ZONE_EXILE_CHOICE` awaiting input. Used by Memoricide, Cranial Extraction |
| `ExileTargetPlayerGraveyardEffect` | `()` | exile all cards from target player's graveyard. Targets player. Used by Nihil Spellbomb |
| `ExileTargetCardFromGraveyardAndImprintOnSourceEffect` | `(CardType requiredType)` | exile target card of required type from any graveyard and track in `permanentExiledCards` for the source permanent (imprint). Targets graveyard (`canTargetGraveyard()=true`). Fizzles if target removed before resolution. Validated by `GraveyardTargetValidators`, resolved by `GraveyardReturnResolutionService`. Used by Myr Welder |
| `ExileTargetCardFromGraveyardEffect` | `(CardType requiredType)` | exile target card of required type from any graveyard (no imprint tracking). Targets graveyard (`canTargetGraveyard()=true`). Fizzles if target removed before resolution. Validated by `GraveyardTargetValidators`, resolved by `GraveyardReturnResolutionService`. Used by Conversion Chamber |
| `EachPlayerExilesTopCardsToSourceEffect` | `(int count)` | each player exiles top N cards of their library, tracked as "exiled with" the source permanent via `GameData.permanentExiledCards`. Used by Knowledge Pool ETB |
| `KnowledgePoolCastTriggerEffect` | `()` | Marker effect for `ON_ANY_PLAYER_CASTS_SPELL`. When a spell is cast from hand, creates a `KnowledgePoolExileAndCastEffect` triggered ability. Only fires for spells cast from hand (prevents infinite loops) |
| `KnowledgePoolExileAndCastEffect` | `(UUID originalSpellCardId, UUID knowledgePoolPermanentId, UUID castingPlayerId)` | Resolution effect for Knowledge Pool's cast trigger. Exiles the original spell from the stack to the KP pool, then presents the casting player with a choice of nonland "other" cards from the pool to cast without paying mana cost. `castingPlayerId` tracks "that player" (the caster) since the trigger is controlled by the KP controller per CR 603.3a. Uses `KNOWLEDGE_POOL_CAST_CHOICE` awaiting input |

## Tokens

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `CreateCreatureTokenEffect` | `(String tokenName, int power, int toughness, CardColor color, List<CardSubtype> subtypes, Set<Keyword> keywords, Set<CardType> additionalTypes)` or `(int amount, ...)` or multi-color: `(int amount, String tokenName, int power, int toughness, CardColor color, Set<CardColor> colors, List<CardSubtype> subtypes)` or `(String tokenName, int power, int toughness, CardColor color, Set<CardColor> colors, List<CardSubtype> subtypes)` or tapped-and-attacking: `(int amount, String tokenName, int power, int toughness, CardColor color, List<CardSubtype> subtypes, boolean tappedAndAttacking)` | create N creature tokens. `color` is primary display color. `colors` (Set&lt;CardColor&gt;, nullable) is full color identity for multi-color tokens. Multi-color constructors default keywords/additionalTypes to empty sets. When `tappedAndAttacking=true`, tokens enter the battlefield tapped and attacking (CR 508.8) — used for "whenever ~ attacks, create tokens tapped and attacking" abilities (e.g. Hero of Bladehold) |
| `CreateXCreatureTokenEffect` | `(String tokenName, int power, int toughness, CardColor color, List<CardSubtype> subtypes, Set<Keyword> keywords, Set<CardType> additionalTypes)` or `(String tokenName, int power, int toughness, CardColor color, List<CardSubtype> subtypes)` | create X creature tokens where X comes from the spell's X value on the stack entry. Used for X-cost token spells like White Sun's Zenith |
| `CreateTokensPerOwnCreatureDeathsThisTurnEffect` | `(String tokenName, int power, int toughness, CardColor color, List<CardSubtype> subtypes, Set<Keyword> keywords, Set<CardType> additionalTypes)` or `(String tokenName, int power, int toughness, CardColor color, List<CardSubtype> subtypes)` | create one creature token for each creature put into the controller's graveyard from the battlefield this turn. Uses `creatureDeathCountThisTurn` per-player count. Used by Fresh Meat |
| `CreateTokenCopyOfImprintedCardEffect` | `(boolean grantHaste, boolean exileAtEndStep)` | create a token that is a copy of the card imprinted on the source permanent. When `grantHaste=true`, the token gains haste. When `exileAtEndStep=true`, the token is exiled at the beginning of the next end step. No-arg constructor `()` defaults to `(true, true)` for Mimic Vat. Use `(false, false)` for permanent tokens like Prototype Portal |
| `CreateTokenCopyOfSourceEffect` | `()` | create a token that is a copy of the source permanent (the permanent with this ability). Copies all copiable characteristics per CR 707.2 including effects and activated abilities |
| `CreateTokenCopyOfTargetPermanentEffect` | `()` | create a token that is a copy of the permanent referenced by `targetPermanentId` on the stack entry. Used for triggered abilities where the permanent to copy is determined at trigger time (e.g. Mirrorworks). Copies all copiable characteristics per CR 707.2 |
| `CreateTokensEqualToControlledCreatureCountEffect` | `(String tokenName, int power, int toughness, CardColor color, List<CardSubtype> subtypes, Set<Keyword> keywords, Set<CardType> additionalTypes)` | create creature tokens equal to the number of creatures the controller controls. Count is determined at resolution time. Used for Chancellor of the Forge ETB |
| `CreateTokenPerEquipmentOnSourceEffect` | `(String tokenName, int power, int toughness, CardColor color, List<CardSubtype> subtypes, Set<Keyword> keywords, Set<CardType> additionalTypes)` | create creature tokens equal to the number of Equipment attached to the source permanent. Requires `sourcePermanentId` on StackEntry (automatically provided by UPKEEP_TRIGGERED). Used by Kemba, Kha Regent |
| `LivingWeaponEffect` | `()` | living weapon ETB: create 0/0 black Phyrexian Germ token and attach this equipment to it (resolved by PermanentControlResolutionService) |

## Life

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `GainLifeEffect` | `(int amount)` | gain N life |
| `PayXManaGainXLifeEffect` | `()` | on resolution, pays all available mana from the controller's pool as X and gains X life. Used for "you may pay {X}. If you do, you gain X life" triggered abilities where payment happens during resolution (e.g. Vigil for the Lost) |
| `GainLifeForEachSubtypeOnBattlefieldEffect` | `(CardSubtype subtype)` | gain 1 life per permanent with given subtype on the battlefield (all players) |
| `GainLifePerControlledCreatureEffect` | `()` | gain 1 life per creature you control |
| `GainLifePerCreatureOnBattlefieldEffect` | `()` | gain 1 life per creature on the battlefield (all players) |
| `GainLifePerCardsInHandEffect` | `()` | gain 1 life per card in controller's hand (upkeep trigger) |
| `GainLifePerGraveyardCardEffect` | `()` | gain life equal to cards in graveyard |
| `GainLifeEqualToTargetToughnessEffect` | `()` | gain life equal to target creature's toughness |
| `GainLifeEqualToToughnessEffect` | `()` | gain life equal to own toughness (self, e.g. dies trigger) |
| `GainLifeEqualToDamageDealtEffect` | `()` | gain life equal to damage dealt (lifelink-style, static) |
| `GainLifeEqualToChargeCountersOnSourceEffect` | `()` | gain life equal to number of charge counters on source (activated ability sacrifice effect) |
| `TargetPlayerGainsLifeEffect` | `(int amount)` | target player gains N life |
| `DoubleTargetPlayerLifeEffect` | `()` | double target player's life total |
| `LoseLifeEffect` | `(int amount)` | lose N life |
| `EachOpponentLosesLifeEffect` | `(int amount)` | each opponent loses N life |
| `EachOpponentLosesLifeAndControllerGainsLifeLostEffect` | `(int amount)` | each opponent loses N life, controller gains total life lost |
| `EachOpponentLosesXLifeAndControllerGainsLifeLostEffect` | `()` | each opponent loses X life, controller gains total life lost |
| `TargetPlayerLosesLifeEffect` | `(int amount)` | target player loses N life |
| `TargetPlayerLosesLifeAndControllerGainsLifeEffect` | `(int lifeLoss, int lifeGain)` | drain: target loses N, you gain M |
| `DrainLifePerControlledPermanentEffect` | `(PermanentPredicate filter, int multiplier)` | target player loses X life, controller gains X life, where X = multiplier × matching permanents controlled (e.g. Tezzeret -4: twice artifacts) |
| `EnchantedCreatureControllerLosesLifeEffect` | `(int amount, UUID affectedPlayerId)` | enchanted creature's controller loses N life (trigger) |
| `EachPlayerLosesLifeEffect` | `(int amount)` | each player (including controller) loses N life |
| `EachPlayerLosesLifePerCreatureControlledEffect` | `(int lifePerCreature)` | each player loses N life per creature they control |
| `LoseLifeUnlessDiscardEffect` | `(int lifeLoss)` | target player loses N life unless they discard a card. Punisher choice made by the affected player. Place in `ON_OPPONENT_CASTS_SPELL` slot. Resolved via `PlayerInteractionResolutionService` → may ability prompt → discard choice or life loss |

## Poison counters

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `GiveControllerPoisonCountersEffect` | `(int amount)` | give the controller of this effect N poison counters. Used for self-poisoning triggers like Phyrexian Vatmother's upkeep trigger |
| `GiveEachPlayerPoisonCountersEffect` | `(int amount)` | give each player N poison counters (including controller). Used for ETB effects like Ichor Rats |
| `GiveEnchantedPermanentControllerPoisonCountersEffect` | `(int amount)` or `(int amount, UUID affectedPlayerId)` | give N poison counter(s) to the controller of the enchanted permanent. Used on `ON_ENCHANTED_PERMANENT_TAPPED` slot. `affectedPlayerId` is null in card definition; baked in at trigger time by `TriggerCollectionService.checkEnchantedPermanentTapTriggers` |
| `GiveTargetPlayerPoisonCountersEffect` | `(int amount)` or `(int amount, CardPredicate spellFilter)` | give target player N poison counters. With `spellFilter`, doubles as trigger descriptor for `ON_CONTROLLER_CASTS_SPELL`: fires when controller casts a spell matching the predicate. Resolves into a copy with `spellFilter == null` |
| `GiveControllerPoisonCountersOnTargetDeathThisTurnEffect` | `(int amount)` | delayed trigger: registers the target creature so that when it dies this turn, its controller gets N poison counters. Reads target from stack entry's `targetPermanentId`. Tracking in `GameData.creatureGivingControllerPoisonOnDeathThisTurn`, cleared at end of turn |

## Win / lose game

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `TargetPlayerLosesGameEffect` | `(UUID playerId)` | target player loses the game |
| `LoseGameIfNotCastFromHandEffect` | `()` | lose the game if not cast from hand (ETB check) |
| `WinGameIfCreaturesInGraveyardEffect` | `(int threshold)` | win if N+ creature cards in graveyard |
| `CantHaveCountersEffect` | `()` | this permanent can't have counters put on it (static) |
| `CantLoseGameEffect` | `()` | you can't lose and opponents can't win (static) |
| `LifeTotalCantChangeEffect` | `()` | controller's life total can't change (static) |

## Creature pump / boost

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `SwitchPowerToughnessEffect` | `()` | switch target creature's power and toughness until end of turn |
| `BoostTargetCreatureEffect` | `(int powerBoost, int toughnessBoost)` | target creature gets +X/+Y until end of turn |
| `BoostSelfEffect` | `(int powerBoost, int toughnessBoost)` | this creature gets +X/+Y until end of turn |
| `BoostAllOwnCreaturesEffect` | `(int powerBoost, int toughnessBoost)` or `(int powerBoost, int toughnessBoost, PermanentPredicate filter)` | all your creatures get +X/+Y until end of turn (one-shot). Optional predicate filter |
| `BoostAllCreaturesEffect` | `(int powerBoost, int toughnessBoost)` or `(int powerBoost, int toughnessBoost, PermanentPredicate filter)` | ALL creatures (all players) get +X/+Y until end of turn (one-shot). Optional predicate filter. Unlike `BoostAllOwnCreaturesEffect`, iterates over every player's battlefield |
| `StaticBoostEffect` | `(int powerBoost, int toughnessBoost, Set<Keyword> grantedKeywords, GrantScope scope, PermanentPredicate filter)` | unified static boost: +X/+Y and keywords with predicate-based filtering. Scope: `OWN_CREATURES`, `OPPONENT_CREATURES`, `ALL_CREATURES`. Filter: optional `PermanentPredicate` (color, subtype, not, etc). Convenience constructors: `(p, t, scope)`, `(p, t, scope, filter)`, `(p, t, keywords, scope)` |
| `BoostCreaturesOfChosenColorEffect` | `(int powerBoost, int toughnessBoost)` | static: creatures you control of the source permanent's chosen color get +X/+Y. The chosen color is stored on the permanent at runtime via `Permanent.getChosenColor()`. Used by Caged Sun |
| `BoostTargetCreatureXEffect` | `(int powerMultiplier, int toughnessMultiplier)` | target creature gets +(multiplier*X)/+(multiplier*X) until end of turn, where X is mana paid |
| `BoostAllCreaturesXEffect` | `(int powerMultiplier, int toughnessMultiplier)` or `(int powerMultiplier, int toughnessMultiplier, PermanentPredicate filter)` | all creatures get +X/+X where X is mana paid. Optional `PermanentPredicate filter` to restrict which creatures are affected |
| `BoostAttachedCreatureEffect` | `(int powerBoost, int toughnessBoost)` | enchanted/equipped creature gets +X/+Y (static, works for both auras and equipment) |
| `BoostAttachedCreaturePerCardsInAllGraveyardsEffect` | `(CardPredicate filter)` | equipped creature gets +X/+X where X = cards in all graveyards matching filter |
| `BoostAttachedCreaturePerMatchingLandNameEffect` | `(int powerPerMatch, int toughnessPerMatch)` | equipped creature gets +X/+Y per land on the battlefield with the same name as the imprinted card |
| `BoostEnchantedCreaturePerControlledSubtypeEffect` | `(CardSubtype subtype, int powerPerSubtype, int toughnessPerSubtype)` | enchanted creature gets +X/+Y per controlled subtype |
| `BoostByOtherCreaturesWithSameNameEffect` | `(int powerPerCreature, int toughnessPerCreature)` | +X/+Y per other creature with same name (static) |
| `BoostBySharedCreatureTypeEffect` | `()` | +1/+1 for each other creature sharing a creature type (static) |
| `BoostFirstTargetCreatureEffect` | `(int powerBoost, int toughnessBoost)` | first target creature in multi-target spell gets +X/+Y until end of turn |
| `BoostSelfPerEnchantmentOnBattlefieldEffect` | `(int powerPerEnchantment, int toughnessPerEnchantment)` | +X/+Y per enchantment on battlefield (static) |
| `BoostSelfPerBlockingCreatureEffect` | `(int powerPerBlockingCreature, int toughnessPerBlockingCreature)` | +X/+Y for each creature blocking this (combat trigger) |
| `BoostSelfPerControlledPermanentEffect` | `(int powerPerPermanent, int toughnessPerPermanent, PermanentPredicate filter)` | +X/+Y for each permanent you control matching the filter (activated ability) |
| `BoostSelfWhenBlockingKeywordEffect` | `(Keyword requiredKeyword, int powerBoost, int toughnessBoost)` | +X/+Y when blocking a creature with the required keyword (e.g. flying). Place in `ON_BLOCK` slot. CombatService converts to BoostSelfEffect at trigger time |
| `TapSubtypeBoostSelfAndDamageDefenderEffect` | `(CardSubtype subtype)` | when this creature attacks, you may tap any number of untapped creatures of subtype you control → gets +X/+0 until end of turn and deals X damage to defending player. Place in `ON_ATTACK` slot. Prompts multi-permanent choice for eligible untapped creatures |

## P/T setting / counters

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `PowerToughnessEqualToControlledLandCountEffect` | `()` | P/T = number of lands you control (static) |
| `PowerToughnessEqualToControlledCreatureCountEffect` | `()` | P/T = number of creatures you control (static) |
| `PowerToughnessEqualToControlledPermanentCountEffect` | `(PermanentPredicate filter)` | P/T = number of permanents you control matching predicate (static). E.g. `new PermanentIsArtifactPredicate()` for artifacts |
| `PowerToughnessEqualToControlledSubtypeCountEffect` | `(CardSubtype subtype)` | P/T = number of permanents of subtype you control (static) |
| `PowerToughnessEqualToCreatureCardsInAllGraveyardsEffect` | `()` | P/T = number of creature cards in all graveyards (static) |
| `PowerToughnessEqualToCardsInHandEffect` | `()` | P/T = number of cards in controller's hand (static) |
| `PutCountersOnSourceEffect` | `(int powerModifier, int toughnessModifier, int amount)` | put N counters on this creature (e.g. `(1,1,1)` for +1/+1, `(-1,-1,2)` for two -1/-1) |
| `PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect` | `(Set<CardColor> triggerColors, int amount, boolean onlyOwnSpells)` | put +1/+1 counters when spell of matching color is cast. Use `ON_CONTROLLER_CASTS_SPELL` with `onlyOwnSpells=true` for "whenever you cast" cards; use `ON_ANY_PLAYER_CASTS_SPELL` with `onlyOwnSpells=false` for "whenever a player casts" cards |
| `PutMinusOneMinusOneCounterOnEachAttackingCreatureEffect` | `()` | put a -1/-1 counter on each attacking creature (all players' attacking creatures) |
| `PutMinusOneMinusOneCounterOnEachOtherCreatureEffect` | `()` | put a -1/-1 counter on each other creature (all players' creatures except the source permanent) |
| `EnterWithXChargeCountersEffect` | `()` | enters battlefield with X charge counters (replacement effect, reads X from spell cast) |
| `EnterWithFixedChargeCountersEffect` | `(int count)` | enters battlefield with N charge counters (replacement effect, fixed count) |
| `PutMinusOneMinusOneCounterOnEachCreatureTargetPlayerControlsEffect` | `()` | put a -1/-1 counter on each creature target player controls (targets player) |
| `PutChargeCounterOnSelfEffect` | `()` | put a charge counter on this permanent (self-target, used as activated ability effect) |
| `PutChargeCounterOnTargetPermanentEffect` | `()` | put a charge counter on target permanent (targets permanent, use with PermanentPredicateTargetFilter to restrict to artifacts etc.) |
| `PutMinusOneMinusOneCounterOnTargetCreatureEffect` | `(int count)` / `()` / `(int count, boolean regenerateIfSurvives)` | put count -1/-1 counters on target creature (targets permanent). No-arg defaults to 1. With `regenerateIfSurvives=true`, regenerates the creature after placing counters if its toughness is 1 or greater (Gore Vassal) |
| `PutXMinusOneMinusOneCountersOnEachCreatureEffect` | `()` | put X -1/-1 counters on each creature (all players' creatures), where X comes from the spell's X value |
| `ProliferateEffect` | `()` | proliferate: choose any number of permanents with counters, add one of each counter type already there |
| `PutAwakeningCountersOnTargetLandsEffect` | `()` | combat damage trigger: choose any number of lands you control, put an awakening counter on each. Lands with awakening counters are 8/8 green Elemental creatures (permanent). Place in `ON_COMBAT_DAMAGE_TO_PLAYER` slot. Handled inline in CombatService via multi-permanent choice |

## Keywords / abilities

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `GrantKeywordEffect` | `(Keyword keyword, GrantScope scope)` or `(Keyword keyword, GrantScope scope, PermanentPredicate filter)` | grant keyword. Scope: `SELF`, `TARGET`, `ENCHANTED_CREATURE`, `EQUIPPED_CREATURE`, `OWN_TAPPED_CREATURES`, `OWN_CREATURES`, `ALL_CREATURES`. Optional predicate filter for conditional grants |
| `GrantFlashToCardTypeEffect` | `(CardType cardType)` | controller may cast spells of the given card type as though they had flash. Checked in `GameBroadcastService.hasFlashGrantForCard()`. Used by Shimmer Myr (ARTIFACT) |
| `EquippedConditionalEffect` | `(CardEffect wrapped)` | Wraps any effect with "as long as equipped" condition. Analogous to `MetalcraftConditionalEffect`. For static effects: wraps `GrantKeywordEffect`, `StaticBoostEffect`, or `ProtectionFromColorsEffect`, applied only while at least one Equipment is attached (selfOnly handler). Used by Sunspear Shikari |
| `GrantChosenKeywordToTargetEffect` | `(List<Keyword> options)` | on resolution, prompts controller to choose one keyword from `options`, then grants it to target permanent until end of turn. Uses COLOR_CHOICE wire protocol with `KeywordGrantChoice` context. Used by Golem Artisan |
| `GrantActivatedAbilityEffect` | `(ActivatedAbility ability, GrantScope scope, PermanentPredicate filter)` or `(ActivatedAbility ability, GrantScope scope)` | grant activated ability to permanents matching scope + filter. Supported scopes: `OWN_PERMANENTS`, `ENCHANTED_CREATURE`, `EQUIPPED_CREATURE`, `OWN_TAPPED_CREATURES`, `OWN_CREATURES`, `ALL_CREATURES`, and other creature scopes. Replaces the old `GrantActivatedAbilityToEnchantedCreatureEffect` — use `GrantScope.ENCHANTED_CREATURE` instead |
| `GainActivatedAbilitiesOfCreatureCardsInAllGraveyardsEffect` | `()` | this creature has all activated abilities of all creature cards in all graveyards (static, selfOnly). Used by Necrotic Ooze |
| `GainActivatedAbilitiesOfExiledCardsEffect` | `()` | this permanent has all activated abilities of all cards exiled with it via `permanentExiledCards` (static, selfOnly). Used by Myr Welder |
| `GrantAdditionalBlockEffect` | `(int additionalBlocks)` | can block N additional creatures |
| `GrantAdditionalBlockPerEquipmentEffect` | `()` | can block an additional creature for each Equipment attached (static, self-only). Used by Kemba's Legion |
| `RegenerateEffect` | `()` or `(boolean targetsPermanent)` | regenerate self (default) or target creature when `targetsPermanent=true` |
| `GrantCardTypeEffect` | `(CardType cardType, GrantScope scope)` | grant a card type to permanents matching scope (e.g. "is an artifact in addition to its other types"). Flows through static bonus system and updates `isArtifact(GameData, Permanent)` / metalcraft / targeting. Scope: `EQUIPPED_CREATURE`, `ENCHANTED_CREATURE`, etc. Used by Silverskin Armor |
| `GrantColorEffect` | `(CardColor color, GrantScope scope)` or `(CardColor color, GrantScope scope, boolean overriding)` | grant a color to permanents matching scope. When `overriding=true`, replaces all existing colors (e.g. "is a black Zombie"). Scope: `EQUIPPED_CREATURE`, `ENCHANTED_CREATURE`, etc. Used by Nim Deathmantle |
| `GrantSubtypeEffect` | `(CardSubtype subtype, GrantScope scope)` or `(CardSubtype subtype, GrantScope scope, boolean overriding)` | grant a creature subtype to permanents matching scope. When `overriding=true`, replaces all existing creature subtypes (non-creature subtypes like Equipment/Aura are preserved). Scope: `EQUIPPED_CREATURE`, `ENCHANTED_CREATURE`, etc. Used by Nim Deathmantle |
| `EnchantedPermanentBecomesTypeEffect` | `(CardSubtype subtype)` | static effect for auras that set the enchanted permanent's subtype ("enchanted [permanent] is a [type]"). When the subtype is a basic land type (SWAMP, ISLAND, FOREST, MOUNTAIN, PLAINS), per MTG rule 305.7 replaces all existing basic land subtypes and overrides the land's mana production. Handled by static bonus system (`landSubtypeOverriding` flag) and `AbilityActivationService.tapPermanent`. Used by Evil Presence |
| `GrantColorUntilEndOfTurnEffect` | `(CardColor color)` | target permanent becomes the specified color until end of turn. Per CR 105.3, replaces all previous colors (sets `colorOverridden` flag, clears existing `grantedColors`). Cleared on `resetModifiers()`. Checked by `PermanentColorInPredicate` for static effects, targeting, and costs |
| `GrantProtectionFromCardTypeUntilEndOfTurnEffect` | `(CardType cardType)` | target creature gains protection from the specified card type until end of turn. Adds to `Permanent.protectionFromCardTypes`. Cleared on `resetModifiers()`. Protection prevents blocking by, damage from, and targeting by sources of that card type. Checked via `GameQueryService.hasProtectionFromSourceCardTypes()` in combat, damage, and targeting services |
| `GrantProtectionChoiceUntilEndOfTurnEffect` | `(boolean includeArtifacts)` or `()` | on resolution, prompts the controller to choose a color (and optionally "artifacts" when `includeArtifacts=true`), then grants the target permanent protection from that choice until end of turn. Color protection stored in `Permanent.protectionFromColorsUntilEndOfTurn`, artifact protection in `Permanent.protectionFromCardTypes`. Both cleared on `resetModifiers()`. Triggers `ColorChoiceContext.ProtectionColorChoice` interaction. Used by Apostle's Blessing (`includeArtifacts=true`). The no-arg constructor defaults to `includeArtifacts=false` for cards like Gods Willing |
| `GrantDamageToOpponentCreatureBounceUntilEndOfTurnEffect` | `()` | until end of turn, creatures you control gain "Whenever this creature deals damage to an opponent, you may return target creature that player controls to its owner's hand." Sets `hasDamageToOpponentCreatureBounce` flag on controlled creatures; CombatService creates `ReturnPermanentsOnCombatDamageToPlayerEffect(PermanentIsCreaturePredicate)` trigger with xValue=1. Cleared on `resetModifiers()`. Used by Arm with Aether |

## Combat restrictions / evasion

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `CantBeBlockedEffect` | `()` | can't be blocked (static) |
| `CantBlockEffect` | `()` | this creature can't block (static) |
| `CantBlockSourceEffect` | `(UUID sourcePermanentId)` | target creature can't block source permanent |
| `CanBeBlockedOnlyByFilterEffect` | `(PermanentPredicate blockerPredicate, String allowedBlockersDescription)` | can only be blocked by matching creatures (static) |
| `CanBeBlockedByAtMostNCreaturesEffect` | `(int maxBlockers)` | can be blocked by at most N creatures (static) |
| `CanBlockOnlyIfAttackerMatchesPredicateEffect` | `(PermanentPredicate attackerPredicate, String allowedAttackersDescription)` | this creature can only block attackers matching predicate (static) |
| `CantAttackOrBlockUnlessEquippedEffect` | `()` | this creature can't attack or block unless it's equipped (static) |
| `CantAttackUnlessDefenderControlsMatchingPermanentEffect` | `(PermanentPredicate defenderPermanentPredicate, String requirementDescription)` | can't attack unless defender controls matching permanent (static) |
| `CantBeBlockedIfDefenderControlsMatchingPermanentEffect` | `(PermanentPredicate defenderPermanentPredicate)` | can't be blocked as long as defender controls matching permanent (static) |
| `CanAttackAsThoughNoDefenderEffect` | `()` | this creature can attack as though it didn't have defender (static, typically wrapped in MetalcraftConditionalEffect) |
| `MustAttackEffect` | `()` | this creature must attack each turn if able (static) |
| `MustBeBlockedByAllCreaturesEffect` | `()` | all creatures able to block this must do so (static, Lure-style) |
| `MustBlockSourceEffect` | `(UUID sourcePermanentId)` | target creature must block source permanent this turn if able |
| `AssignCombatDamageAsThoughUnblockedEffect` | `()` | assign combat damage as though unblocked (static) |
| `AssignCombatDamageWithToughnessEffect` | `()` | assign combat damage using toughness instead of power (static) |
| `MakeTargetUnblockableEffect` | `()` | target creature is unblockable this turn |
| `MakeAllCreaturesUnblockableEffect` | `()` | all creatures on all battlefields can't be blocked this turn |
| `TargetCreatureCantBlockThisTurnEffect` | `()` | target creature can't block this turn |
| `TargetPlayerCreaturesCantBlockThisTurnEffect` | `()` | all creatures controlled by target player (or planeswalker's controller) can't block this turn — uses shared target, no own targeting |
| `CantBlockThisTurnEffect` | `(PermanentPredicate filter)` | all creatures on all battlefields matching the predicate can't block this turn — non-targeted mass effect, pass `null` for all creatures |
| `EnchantedCreatureCantAttackOrBlockEffect` | `()` | enchanted creature can't attack or block (static, Pacifism-style) |
| `EnchantedCreatureCantAttackEffect` | `()` | enchanted creature can't attack but can still block (static, Forced Worship-style) |
| `EnchantedCreatureCantActivateAbilitiesEffect` | `()` | enchanted creature's activated abilities can't be activated (static, Arrest-style) |

## Tap / untap

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `TapTargetPermanentEffect` | `()` | tap target permanent |
| `TapOrUntapTargetPermanentEffect` | `()` | tap or untap target permanent |
| `UntapTargetPermanentEffect` | `()` | untap target permanent |
| `UntapSelfEffect` | `()` | untap this permanent |
| `UntapAttackedCreaturesEffect` | `()` | untap creatures that attacked this turn (end of combat) |
| `TapCreaturesEffect` | `(Set<TargetFilter> filters)` | tap all creatures matching filters |
| `DoesntUntapDuringUntapStepEffect` | `()` | this permanent doesn't untap during untap step (static) |
| `MayNotUntapDuringUntapStepEffect` | `()` | controller may choose not to untap this permanent during untap step (static); prompts player via may-ability system |
| `PreventTargetUntapWhileSourceTappedEffect` | `()` | target permanent doesn't untap during its controller's untap step for as long as the source permanent remains tapped; piggybacks on companion targeting effect (e.g. `TapTargetPermanentEffect`) |
| `AttachedCreatureDoesntUntapEffect` | `()` | attached creature (aura or equipment) doesn't untap during untap step (static) |
| `UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect` | `(TurnStep step)` | untap all your permanents during each other player's step |
| `UntapAllControlledPermanentsEffect` | `(PermanentPredicate filter)` | untap all permanents you control matching filter (e.g. `PermanentIsLandPredicate` for "untap all lands you control") |
| `UntapEachOtherCreatureYouControlEffect` | `(PermanentPredicate filter)` | untap each other creature you control matching filter; `()` no-arg overload untaps all (ON_ATTACK trigger or activated ability) |
| `UnattachEquipmentFromTargetPermanentsEffect` | `()` | unattach all equipment from target permanents (multi-target) |

## Control / steal

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `TargetPlayerGainsControlOfSourceCreatureEffect` | `()` | target opponent gains control of this creature (ETB) |
| `GainControlOfTargetPermanentUntilEndOfTurnEffect` | `()` | gain control of target permanent until end of turn — card's target filter handles type restriction (Threaten, Metallic Mastery) |
| `AttachTargetToSourcePermanentEffect` | `()` | attach the targeted permanent to the source permanent (e.g. steal Equipment and equip it — combine with GainControlOfTargetPermanentUntilEndOfTurnEffect) |
| `GainControlOfEnchantedTargetEffect` | `()` | gain control of enchanted permanent (static, Control Magic-style) |
| `GainControlOfTargetAuraEffect` | `()` | gain control of target aura |
| `ControlEnchantedCreatureEffect` | `()` | control enchanted creature (static) |
| `EnchantedCreatureDealsDamageToItsOwnerEffect` | `(int damage)` | enchanted creature deals N damage to its owner (the original owner, not current controller). Use on `UPKEEP_TRIGGERED` slot for auras that steal + ping owner |

## Prevention / protection / redirection

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `PreventDamageToTargetEffect` | `(int amount)` | prevent next N damage to target |
| `PreventNextDamageEffect` | `(int amount)` | prevent next N damage to target creature or player |
| `PreventAllCombatDamageEffect` | `()` | prevent all combat damage this turn |
| `PreventAllDamageEffect` | `()` | prevent all damage (e.g. Fog-style) |
| `PreventAllDamageToAndByEnchantedCreatureEffect` | `()` | prevent all damage to and dealt by enchanted creature |
| `PreventDamageFromColorsEffect` | `(Set<CardColor> colors)` | prevent all damage from sources of specified colors (static) |
| `PreventNextColorDamageToControllerEffect` | `(CardColor chosenColor)` | prevent next damage of chosen color to controller |
| `PreventAllDamageByTargetCreatureEffect` | `()` | prevent all damage target creature(s) would deal this turn (multi-target via targetPermanentIds) |
| `PreventAllDamageFromChosenSourceEffect` | `()` | prevent all damage a chosen source would deal to controller this turn (prompts permanent choice on resolution) |
| `PreventDamageAndAddMinusCountersEffect` | `()` | prevent all damage to this creature and put a -1/-1 counter for each 1 damage prevented (static, e.g. Phyrexian Hydra) |
| `ProtectionFromColorsEffect` | `(Set<CardColor> colors)` | protection from specified colors (static) |
| `ProtectionFromChosenColorEffect` | `()` | protection from chosen color (static, requires ChooseColorOnEnterEffect) |
| `CantBeTargetedBySpellColorsEffect` | `(Set<CardColor> colors)` | can't be targeted by spells of specified colors (static) |
| `CantBeTargetOfSpellsOrAbilitiesEffect` | `()` | can't be targeted by opponents' spells or abilities (hexproof behavior, use with GrantEffectEffect) |
| `GrantEffectEffect` | `(CardEffect effect, GrantScope scope)` | grant a CardEffect to permanents matching scope (e.g. OWN_CREATURES) |
| `RedirectPlayerDamageToEnchantedCreatureEffect` | `()` | redirect damage dealt to player to enchanted creature |
| `RedirectUnblockedCombatDamageToSelfEffect` | `()` | redirect unblocked combat damage to this creature |
| `GrantControllerShroudEffect` | `()` | controller has shroud (can't be targeted) (static) |

## Mana

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `AwardManaEffect` | `(ManaColor color, int amount)` or `(ManaColor color)` (defaults amount to 1) | add N mana of specified color. Also stack-resolvable via `@HandlesEffect` in LifeResolutionService |
| `AwardAnyColorManaEffect` | `()` | add one mana of any color |
| `AddManaOnEnchantedLandTapEffect` | `(ManaColor color, int amount)` | when enchanted land is tapped, add N mana of color |
| `AddExtraManaOfChosenColorOnLandTapEffect` | `()` | ON_ANY_PLAYER_TAPS_LAND trigger: when a land you control taps for mana of the source permanent's chosen color, add one additional mana of that color. Checks ON_TAP effects of the tapped land. Used by Caged Sun |
| `DoubleManaPoolEffect` | `()` | double your mana pool |
| `AddManaPerControlledSubtypeEffect` | `(ManaColor color, CardSubtype subtype)` | add one mana of color for each permanent with subtype you control |
| `AwardArtifactOnlyColorlessManaEffect` | `(int amount)` | add N colorless mana that can only be spent to cast artifact spells or activate abilities of artifacts. Stored in `ManaPool.artifactOnlyColorless`; `ManaCost.canPay/pay` accept `artifactContext=true` to include this mana |
| `AwardMyrOnlyColorlessManaEffect` | `(int amount)` | add N colorless mana that can only be spent to cast Myr spells or activate abilities of Myr. Stored in `ManaPool.myrOnlyColorless`; `ManaCost.canPay/pay` accept `myrContext=true` to include this mana |
| `PreventManaDrainEffect` | `()` | players don't lose unspent mana as steps/phases end (static) |

## Copy / clone

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `CopyPermanentOnEnterEffect` | `(PermanentPredicate filter, String typeLabel)` or `(PermanentPredicate filter, String typeLabel, Integer powerOverride, Integer toughnessOverride)` | enter as copy of permanent matching filter (Clone-style). Optional P/T overrides for "copy except it's X/Y" (e.g. Quicksilver Gargantuan) |
| `CopySpellEffect` | `()` | copy target spell |
| `CopySpellForEachOtherSubtypePermanentEffect` | `(CardSubtype subtype)` | trigger descriptor: whenever a player casts an instant or sorcery spell that targets only a single permanent with the given subtype, copy the spell for each other permanent with that subtype the spell could target. Each copy targets a different one of those permanents. Place in `ON_ANY_PLAYER_CASTS_SPELL` slot. Resolution snapshot populated at trigger time by `checkSpellCastTriggers`. Used by Precursor Golem |
| `BecomeCopyOfTargetCreatureEffect` | `()` | source permanent becomes a copy of target creature, retaining the triggered ability that granted this effect. Used by Cryptoplasm. Place in UPKEEP_TRIGGERED wrapped in MayEffect. canTargetPermanent=true |
| `ChangeTargetOfTargetSpellWithSingleTargetEffect` | `()` | change target of target spell with single target |

## Turn / phase

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `ExtraTurnEffect` | `(int count)` | take N extra turns |
| `AdditionalCombatMainPhaseEffect` | `(int count)` | get N additional combat + main phases |
| `EndTurnEffect` | `()` | end the turn |
| `ControlTargetPlayerNextTurnEffect` | `()` | control target player during their next turn (Mindslaver) |
| `PermanentsEnterTappedThisTurnEffect` | `()` | all permanents enter tapped this turn (Due Respect) |

## Animate / transform

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `AnimateLandEffect` | `(int power, int toughness, List<CardSubtype> grantedSubtypes, Set<Keyword> grantedKeywords, CardColor animatedColor)` or `(int power, int toughness, List<CardSubtype> grantedSubtypes, Set<Keyword> grantedKeywords, CardColor animatedColor, Set<CardType> grantedCardTypes)` | land becomes creature until end of turn (manlands); use 6-arg form to also grant card types (e.g. Artifact for Inkmoth Nexus) |
| `AnimateSelfEffect` | `(List<CardSubtype> grantedSubtypes)` | this permanent becomes a creature (e.g. Mutavault-style) |
| `AnimateSelfWithStatsEffect` | `(int power, int toughness, List<CardSubtype> grantedSubtypes, Set<Keyword> grantedKeywords)` | this permanent becomes a creature with fixed P/T and keywords until end of turn (e.g. Glint Hawk Idol) |
| `AnimateSelfByChargeCountersEffect` | `(List<CardSubtype> grantedSubtypes)` | becomes creature with P/T equal to charge counters until end of turn |
| `AnimateTargetPermanentEffect` | `(int power, int toughness)` | target permanent becomes a creature with base P/T permanently (not until end of turn); retains other types (e.g. Tezzeret, Agent of Bolas -1) |
| `AnimateNoncreatureArtifactsEffect` | `()` | animate all noncreature artifacts into creatures (March of the Machines-style) |
| `AddCardTypeToTargetPermanentEffect` | `(CardType cardType)` | target permanent becomes the given card type in addition to its other types until end of turn (e.g. Liquimetal Coating makes target an artifact) |
| `GrantEquipByManaValueEffect` | `(PermanentPredicate filter)` | grants matching permanents an equip ability with cost {X} and boosts equipped creatures +X/+0, where X = permanent's mana value (static) |

## Enchantment-specific

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `PutAuraFromHandOntoSelfEffect` | `()` | put an aura from hand onto this creature (ETB) |
| `GainControlOfTargetAuraEffect` | `()` | gain control of target aura |
| `ChangeColorTextEffect` | `()` | change color words in enchanted permanent's text (Sleight of Mind-style) |

## Equipment

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `EquipEffect` | `()` | equip to target creature. Prefer using `EquipActivatedAbility(manaCost)` which wraps this effect with sorcery-speed timing and "creature you control" filter |
| `BoostAttachedCreatureEffect` | `(int powerBoost, int toughnessBoost)` | equipped creature gets +X/+Y (static) |
| `BoostAttachedCreaturePerCardsInAllGraveyardsEffect` | `(CardPredicate filter)` | equipped creature gets +X/+X where X = cards in all graveyards matching filter (static) |
| `BoostAttachedCreaturePerMatchingLandNameEffect` | `(int powerPerMatch, int toughnessPerMatch)` | equipped creature gets +X/+Y per land matching imprinted card name (static) |
| `SacrificeOnUnattachEffect` | `()` | whenever this equipment becomes unattached, sacrifice the previously-equipped creature (static marker) |
| `AttachSourceEquipmentToTargetCreatureEffect` | `()` | attach source equipment to target creature on ETB. Reads sourcePermanentId as equipment, targetPermanentId as creature. Used by equipment with "When this Equipment enters, attach it to target creature you control." |
| `AttachTargetEquipmentToTargetCreatureEffect` | `()` | attach target Equipment to target creature (multi-target; reads targetPermanentIds[0] as equipment, [1] as creature) |

## Static restrictions / taxes

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `CantCastSpellsWithSameNameAsExiledCardEffect` | `()` | no player can cast spells with the same name as the card exiled by the source permanent (static, Exclusion Ritual) |
| `CantCastSpellTypeEffect` | `(Set<CardType> restrictedTypes)` | controller can't cast spells of specified types (static) |
| `CantSearchLibrariesEffect` | `()` | players can't search libraries; any player may pay {2} to ignore until end of turn (static, Leonin Arbiter) |
| `LimitSpellsPerTurnEffect` | `(int maxSpells)` | each player can cast at most N spells per turn (static) |
| `IncreaseOpponentCastCostEffect` | `(Set<CardType> affectedTypes, int amount)` | opponent's spells of types cost N more (static) |
| `RequirePaymentToAttackEffect` | `(int amountPerAttacker)` | must pay N mana per attacking creature (static) |
| `ReduceOwnCastCostIfOpponentControlsMoreCreaturesEffect` | `(int minimumCreatureDifference, int amount)` | reduce cast cost by N if opponent has M+ more creatures |
| `ReduceOwnCastCostForSharedCardTypeWithImprintEffect` | `(int amount)` | reduce cast cost of controller's spells by N if they share a card type with the imprinted card (static, Semblance Anvil) |
| `ReduceOwnCastCostIfMetalcraftEffect` | `(int amount)` | reduce this spell's cast cost by N if controller has metalcraft (3+ artifacts) (static, Stoic Rebuttal) |
| `NoMaximumHandSizeEffect` | `()` | you have no maximum hand size (static, requires permanent on battlefield) |
| `GrantPermanentNoMaxHandSizeEffect` | `()` | you have no maximum hand size for the rest of the game (one-shot spell effect, persists via GameData.playersWithNoMaximumHandSize) |
| `EnterPermanentsOfTypesTappedEffect` | `(Set<CardType> cardTypes)` | permanents of specified types enter tapped (static) |
| `EntersTappedUnlessFewLandsEffect` | `(int maxOtherLands)` | enters tapped unless you control N or fewer other lands (fast lands, static) |

## Choose / name

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `ChooseCardNameOnEnterEffect` | `()` or `(List<CardType> excludedTypes)` | choose a card name as ETB (implements `ChooseCardNameEffect` marker). No-arg allows any name (Pithing Needle); with excludedTypes filters the name list (e.g. `List.of(LAND)` for Phyrexian Revoker) |
| `ActivatedAbilitiesOfChosenNameCantBeActivatedEffect` | `()` or `(boolean blocksManaAbilities)` | activated abilities of chosen name can't be activated (static). No-arg (false) excludes mana abilities (Pithing Needle); `true` also blocks mana abilities (Phyrexian Revoker) |
| `ChooseColorOnEnterEffect` | `()` | choose a color as ETB (implements `ChooseColorEffect` marker) |

## Draw replacement / library interaction

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `AbundanceDrawReplacementEffect` | `()` | replace draws with Abundance's reveal-until mechanic (static) |
| `ReplaceSingleDrawEffect` | `(UUID playerId, DrawReplacementKind kind)` | replace a single draw with a replacement effect |
| `PlayLandsFromGraveyardEffect` | `()` | you may play lands from your graveyard (static) |

## Put onto battlefield

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `PutCardToBattlefieldEffect` | `(CardType cardType)` | you may put a card of type from hand onto battlefield (wrap in MayEffect) |
| `OpponentMayPlayCreatureEffect` | `()` | opponent may put a creature card from hand onto battlefield |
| `OpponentMayReturnExiledCardOrDrawEffect` | `(int drawCount)` | opponent may let controller have an exiled card; if declined, controller draws N cards. Used as may-ability marker after library-search-to-exile |

## Card-specific / one-off

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `AjaniUltimateEffect` | `()` | Ajani's ultimate: put 100 counters (planeswalker-specific) |
| `KothEmblemEffect` | `()` | Koth's emblem: Mountains you control have '{T}: This land deals 1 damage to any target.' |
| `VenserEmblemEffect` | `()` | Venser's emblem: "Whenever you cast a spell, exile target permanent." Creates emblem with ExileTargetOnControllerSpellCastEffect |
| `ExileTargetOnControllerSpellCastEffect` | `()` | Marker effect stored in Emblem.staticEffects. Triggers when controller casts a spell, prompting target permanent choice then exiling it |
| `GenesisWaveEffect` | `()` | reveal top X cards, put any number of permanent cards with MV ≤ X onto battlefield, rest to graveyard. X read from `StackEntry.getXValue()` |

---

## Provider map (where to add `@HandlesEffect` resolver methods)

| Category | Resolution service |
|----------|--------------------|
| Damage | `combat.DamageResolutionService` |
| Destruction/sacrifice | `DestructionResolutionService` |
| Bounce | `battlefield.BounceResolutionService` |
| Counter | `CounterResolutionService` |
| Library/search/mill | `LibraryResolutionService` |
| Graveyard return/exile | `GraveyardReturnResolutionService` |
| Player interaction (draw/discard/choices) | `effect/PlayerInteractionResolutionService` |
| Life | `effect/LifeResolutionService` |
| Creature mods (tap/pump/keyword) | `effect/CreatureModResolutionService` |
| Permanent control/tokens/regeneration | `effect/PermanentControlResolutionService` |
| Static continuous effects | `effect/StaticEffectResolutionService` |
| Prevention | `PreventionResolutionService` |
| Turn effects | `TurnResolutionService` |
| Copy/retarget | `CopyResolutionService`, `TargetRedirectionResolutionService` |
| Exile permanent | `ExileResolutionService` |
| Card-specific one-offs | `effect/CardSpecificResolutionService` |
| Land-tap triggers | `GameHelper` (`checkLandTapTriggers`) |
| Win conditions | `effect/WinConditionResolutionService` |

All resolution services are in `magical-vibes-backend/src/main/java/com/github/laxika/magicalvibes/service/`.

## Target validator map (where to add `@ValidatesTarget` methods)

Target validation is auto-registered via `@ValidatesTarget` annotations, mirroring `@HandlesEffect`. Validator classes live in `service/validate/`.

| Category | Validator class | Dependencies |
|----------|----------------|--------------|
| Damage (any target, creature, player) | `DamageTargetValidators` | `TargetValidationService`, `GameQueryService` |
| Creature mods (tap/untap/boost/block) | `CreatureModTargetValidators` | `TargetValidationService` |
| Destruction (sacrifice, destroy) | `DestructionTargetValidators` | `TargetValidationService`, `GameQueryService` |
| Graveyard (return, opponent graveyard) | `GraveyardTargetValidators` | `TargetValidationService`, `GameQueryService` |
| Bounce (return to hand) | `BounceTargetValidators` | `TargetValidationService` |
| Library (mill, reveal) | `LibraryTargetValidators` | `TargetValidationService` |
| Permanent control (gain control, bottom of library) | `PermanentControlTargetValidators` | `TargetValidationService`, `GameQueryService` |
| Life (drain, gain) | `LifeTargetValidators` | `TargetValidationService` |

**Two method signatures supported:**
- **Pattern A:** `void method(TargetValidationContext ctx)` — effect not needed
- **Pattern B:** `void method(TargetValidationContext ctx, ConcreteEffectType effect)` — effect auto-cast

Helper methods on `TargetValidationService`: `requireTarget()`, `requireBattlefieldTarget()`, `requireCreature()`, `checkProtection()`, `requireTargetPlayer()`, `findSourcePermanentIndex()`, `findSourcePermanentController()`.

## Canonical card examples

- Burn spell: `cards/s/Shock.java`
- Multi-effect targeted spell: `cards/c/Condemn.java`
- Effect composition in activated ability: `cards/o/OrcishArtillery.java`
- Spell-copy targeting stack: `cards/t/Twincast.java`
- Aura static lock: `cards/p/Pacifism.java`
- Static "can't block" creature: `cards/s/SpinelessThug.java`
- ETB token + activated cost/effect composition: `cards/s/SiegeGangCommander.java`
- ETB control handoff + upkeep drawback: `cards/s/SleeperAgent.java`
- Opponent draw trigger damage: `cards/u/UnderworldDreams.java`
- Conditional self cast-cost reduction: `cards/a/AvatarOfMight.java`
- Evasion blocked-only-by-wall-or-flying: `cards/e/ElvenRiders.java`
- ETB card name choice + static ability lock: `cards/p/PithingNeedle.java`
- Pain land (tap for colorless or colored + damage): `cards/s/SulfurousSprings.java`
- Creature land (manland): `cards/t/TreetopVillage.java`
- Lord with subtype boost + keywords: `cards/g/GoblinKing.java`
- Equipment with boost: `cards/l/LoxodonWarhammer.java`
