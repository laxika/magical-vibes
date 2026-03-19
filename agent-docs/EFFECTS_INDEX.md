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

Effects returning `true`: `BoostSelfEffect`, `UntapSelfEffect`, `AnimateSelfEffect`, `AnimateSelfByChargeCountersEffect`, `AnimateSelfWithStatsEffect`, `AnimateLandEffect`, `PutChargeCounterOnSelfEffect`, `PutSlimeCounterAndCreateOozeTokenEffect`.

Conditional: `RegenerateEffect` → `!targetsPermanent()`, `GrantKeywordEffect` → `scope == SELF`.

### `isPowerToughnessDefining()` default method on `CardEffect`

Effects that are characteristic-defining abilities (CDAs) for power/toughness (`*/*` effects) override `isPowerToughnessDefining()` to return `true`. Per CR 707.9d, when a copy effect provides specific P/T values (e.g. "except it's 7/7"), CDAs that define P/T are not copied. Used by `CopyPermanentOnEnterEffect` with `powerOverride`/`toughnessOverride`.

Effects returning `true`: `PowerToughnessEqualToControlledLandCountEffect`, `PowerToughnessEqualToControlledCreatureCountEffect`, `PowerToughnessEqualToControlledPermanentCountEffect`, `PowerToughnessEqualToControlledSubtypeCountEffect`, `PowerToughnessEqualToCreatureCardsInAllGraveyardsEffect`, `PowerToughnessEqualToCardsInAllGraveyardsEffect`, `PowerToughnessEqualToCardsInControllerGraveyardEffect`, `PowerToughnessEqualToCardsInHandEffect`, `PowerToughnessEqualToControllerLifeTotalEffect`, `BoostSelfBySlimeCountersOnLinkedPermanentEffect`.

---

## Effect targeting declarations

Effects declare what they can target via default methods on `CardEffect`. `Card.isNeedsTarget()` and `Card.isNeedsSpellTarget()` are derived automatically — never call `setNeedsTarget`/`setNeedsSpellTarget`.

When creating a new effect, override the relevant method(s) to return `true`:

| Method | Returns `true` on these effects |
|--------|---------------------------------|
| `canTargetPlayer()` | DealDamageToAnyTargetEffect, DealDamageToAnyTargetEqualToChargeCountersOnSourceEffect, DealDamageToTargetAndTheirCreaturesEffect, DealDamageToTargetPlayerEqualToCardTypeCountInGraveyardEffect, DealDamageEqualToSourcePowerToAnyTargetEffect, DealDamageToAnyTargetAndGainLifeEffect, DealDamageToTargetPlayerEffect, DealDamageToTargetPlayerByHandSizeEffect, DealOrderedDamageToAnyTargetsEffect, DealXDamageToAnyTargetEffect, DealXDamageToAnyTargetAndGainXLifeEffect, DealXDamageDividedEvenlyAmongTargetsEffect, TargetPlayerLosesLifeEffect, TargetPlayerLosesLifeEqualToPowerEffect, TargetPlayerLosesLifeAndControllerGainsLifeEffect, TargetPlayerGainsLifeEffect, EachTargetPlayerGainsLifeEffect, DoubleTargetPlayerLifeEffect, TargetPlayerDiscardsEffect, TargetPlayerDiscardsReturnSelfIfCardTypeEffect, ChooseCardFromTargetHandToDiscardEffect, ChooseCardFromTargetHandToExileEffect, ChooseCardsFromTargetHandToTopOfLibraryEffect, LookAtHandEffect, HeadGamesEffect, RedirectDrawsEffect, MillTargetPlayerEffect, MillTargetPlayerByChargeCountersEffect, TargetPlayerDiscardsByChargeCountersEffect, MillHalfLibraryEffect, ExtraTurnEffect, SacrificeCreatureEffect, SacrificeAttackingCreaturesEffect, ShuffleGraveyardIntoLibraryEffect, RevealTopCardDealManaValueDamageEffect, RevealTopCardOfLibraryEffect, ReturnArtifactsTargetPlayerOwnsToHandEffect, TargetPlayerGainsControlOfSourceCreatureEffect, PutMinusOneMinusOneCounterOnEachCreatureTargetPlayerControlsEffect, GiveTargetPlayerPoisonCountersEffect, ExileTargetPlayerGraveyardEffect, DrawXCardsForTargetPlayerEffect, MillBottomOfTargetLibraryConditionalTokenEffect, MillTargetPlayerAndBoostSelfByManaValueEffect, SeparatePermanentsIntoPilesAndSacrificeEffect |
| `canTargetPermanent()` | DealDamageToAnyTargetEffect, DealDamageToAnyTargetEqualToChargeCountersOnSourceEffect, DealDamageToTargetAndTheirCreaturesEffect, DealDamageEqualToSourcePowerToAnyTargetEffect, DealDamageToAnyTargetAndGainLifeEffect, DealDamageToTargetCreatureEffect, DealDamageToTargetCreatureControllerEffect, DealDamageToTargetCreatureEqualToControlledSubtypeCountEffect, DealOrderedDamageToAnyTargetsEffect, DealXDamageToAnyTargetEffect, DealXDamageToAnyTargetAndGainXLifeEffect, DealXDamageToTargetCreatureEffect, DealXDamageDividedAmongTargetAttackingCreaturesEffect, FirstTargetDealsPowerDamageToSecondTargetEffect, FirstTargetFightsSecondTargetEffect, DestroyTargetPermanentEffect, DestroyTargetPermanentAtEndStepEffect, DestroyTargetLandAndDamageControllerEffect, DestroyTargetPermanentAndGiveControllerPoisonCountersEffect, DestroyCreatureBlockingThisEffect, ExileTargetPermanentEffect, ReturnTargetPermanentToHandEffect, PutTargetOnBottomOfLibraryEffect, PutTargetOnTopOfLibraryEffect, GainControlOfTargetCreatureUntilEndOfTurnEffect, GainControlOfTargetPermanentUntilEndOfTurnEffect, GainControlOfTargetEquipmentUntilEndOfTurnEffect, GainControlOfTargetPermanentEffect, GainControlOfEnchantedTargetEffect, GainControlOfTargetAuraEffect, BoostTargetCreatureEffect, BoostTargetCreaturePerControlledPermanentEffect, BoostFirstTargetCreatureEffect, GainLifeEqualToTargetToughnessEffect, PreventDamageToTargetEffect, TapTargetPermanentEffect, TapOrUntapTargetPermanentEffect, UntapTargetPermanentEffect, UntapAllTargetPermanentsEffect, MakeCreatureUnblockableEffect, TargetCreatureCantBlockThisTurnEffect, ChangeColorTextEffect, EquipEffect, CantBlockSourceEffect, SacrificeCreatureCost, DestroyTargetAndControllerLosesLifePerCreatureDeathsEffect, GrantKeywordEffect (when scope == Scope.TARGET), GrantChosenKeywordToTargetEffect, PutMinusOneMinusOneCounterOnTargetCreatureEffect, PutPlusOnePlusOneCounterOnTargetCreatureEffect, PutPlusOnePlusOneCounterOnFirstTargetIfSupertypeEffect, UnattachEquipmentFromTargetPermanentsEffect, ExileTargetPermanentAndReturnAtEndStepEffect, AddCardTypeToTargetPermanentEffect, GrantColorUntilEndOfTurnEffect, MustBlockSourceEffect, GrantProtectionFromCardTypeUntilEndOfTurnEffect, SwitchPowerToughnessEffect, SetBasePowerToughnessUntilEndOfTurnEffect, RemoveChargeCountersFromTargetPermanentEffect, RemoveCountersFromTargetAndBoostSelfEffect, GrantSubtypeToTargetCreatureEffect, GainControlOfTargetPermanentWhileSourceEffect |
| `canTargetSpell()` | CounterSpellEffect, CounterSpellIfControllerPoisonedEffect, CounterUnlessPaysEffect, CopySpellEffect, ChangeTargetOfTargetSpellWithSingleTargetEffect, ChangeTargetOfTargetSpellToSourceEffect, ChooseNewTargetsForTargetSpellEffect |
| `canTargetGraveyard()` | ReturnCardFromGraveyardEffect (when targetGraveyard=true), PutCardFromOpponentGraveyardOntoBattlefieldEffect, PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect |
| `canTargetExile()` | ReturnTargetCardFromExileToHandEffect |

Effects that target both players and permanents (any-target): DealDamageToAnyTargetEffect, DealDamageToAnyTargetEqualToChargeCountersOnSourceEffect, DealDamageToAnyTargetAndGainLifeEffect, DealOrderedDamageToAnyTargetsEffect, DealXDamageToAnyTargetEffect, DealXDamageToAnyTargetAndGainXLifeEffect.

### Per-blocker trigger mode

Effects in the `ON_BECOMES_BLOCKED` slot can be registered with `TriggerMode.PER_BLOCKER` via `addEffect(slot, effect, TriggerMode.PER_BLOCKER)`, causing CombatService to create one stack entry per blocking creature (e.g. "whenever this creature becomes blocked **by a creature**"). Cards using this: `SylvanBasilisk`, `EngulfingSlagwurm` (ON_BECOMES_BLOCKED only), `InfiltrationLens`.

### Becomes-target-of-spell trigger

Effects in the `ON_BECOMES_TARGET_OF_SPELL` slot fire when the permanent (or the creature an equipment is attached to) becomes the target of a spell. Any targeting effect can be placed in this slot — e.g. `DealDamageToAnyTargetEffect` for "deal 2 damage to any target" when the equipped creature becomes targeted. Cards using this: `LivewireLash`.

### Becomes-target-of-spell-or-ability trigger

Effects in the `ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY` slot fire when the permanent (or a permanent it is attached to) becomes the target of any spell or activated ability. The triggered ability goes directly on the stack with `sourcePermanentId` set to the source permanent. Cards using this: `IceCage`.

## Wrapper / modifier effects

| Effect | Constructor | Description |
|--------|-------------|-------------|
| `MayEffect` | `(CardEffect wrapped, String prompt)` | Wraps any effect with "you may" choice. For "becomes blocked by a creature" triggers that fire once per blocker, register with `TriggerMode.PER_BLOCKER` via `addEffect()` (e.g. Infiltration Lens) |
| `MayPayManaEffect` | `(String manaCost, CardEffect wrapped, String prompt)` | Wraps any effect with "you may pay {X}. If you do, [effect]" choice. The mana cost is charged before resolving. Used for Spellbomb cycle and similar cards |
| `MetalcraftConditionalEffect` | `(CardEffect wrapped)` | Wraps any effect with metalcraft condition (3+ artifacts). For ETB triggers: checked at trigger time and resolution time, delegates targeting to wrapped effect. For static effects: wraps GrantKeywordEffect or StaticBoostEffect, applied only while metalcraft is met (selfOnly handler) |
| `MorbidConditionalEffect` | `(CardEffect wrapped)` | Wraps any effect with morbid condition (a creature died this turn). For ETB triggers: checked at trigger time (intervening-if) and resolution time, delegates targeting to wrapped effect. For END_STEP_TRIGGERED: checked at trigger time (intervening-if) and resolution time; if wrapped effect targets, queued via `pendingEndStepTriggerTargets` for target selection. Used by Festerhide Boar with PutCountersOnSourceEffect, Reaper from the Abyss with DestroyTargetPermanentEffect |
| `PermanentEnteredThisTurnConditionalEffect` | `(CardEffect wrapped, CardPredicate predicate, int minCount)` | Wraps any effect with "if at least N permanents matching predicate entered the battlefield under that player's control this turn" condition. Checked at trigger time via `permanentsEnteredBattlefieldThisTurn`. Used by Tunnel Ignus with `CardTypePredicate(LAND), 2` |
| `EnteringCreatureMinPowerConditionalEffect` | `(int minPower, CardEffect wrapped)` | Wraps any ON_ALLY_CREATURE_ENTERS_BATTLEFIELD effect with "entering creature has power >= minPower" condition. Checked at trigger time. Inner effect can be MayEffect or mandatory. Used by Garruk's Packleader (minPower=3, MayEffect(DrawCardEffect)) |
| `EnteringCreatureMaxPowerConditionalEffect` | `(int maxPower, CardEffect wrapped)` | Wraps any ON_ALLY_CREATURE_ENTERS_BATTLEFIELD effect with "entering creature has power <= maxPower" condition. Checked at trigger time. Inner effect can be MayPayManaEffect, MayEffect, or mandatory. Used by Mentor of the Meek (maxPower=2, MayPayManaEffect("{1}", DrawCardEffect)) |
| `SubtypeConditionalEffect` | `(CardSubtype subtype, CardEffect wrapped)` | Wraps any trigger with "triggering creature has subtype" condition. Works with ON_ALLY_CREATURE_ENTERS_BATTLEFIELD (checked in BattlefieldEntryService) and ON_ANY_CREATURE_DIES (checked in DeathTriggerService). Creates stack entry with sourcePermanentId. Used by Champion of the Parish (HUMAN, PutCountersOnSourceEffect(1,1,1)) and Village Cannibals (HUMAN, PutCountersOnSourceEffect(1,1,1)) |
| `ImprintedCardNameMatchesEnteringPermanentConditionalEffect` | `(CardEffect wrapped)` | Wraps any ON_OPPONENT_LAND_ENTERS_BATTLEFIELD effect with "entering permanent name matches source's imprinted card name" condition. Checked at trigger time. Used by Invader Parasite |
| `DefendingPlayerPoisonedConditionalEffect` | `(CardEffect wrapped)` | Wraps any effect with "if defending player is poisoned" condition. Used for ON_ATTACK triggers that only fire when opponent has 1+ poison counters. Used by Septic Rats |
| `OpponentPoisonedConditionalEffect` | `(CardEffect wrapped)` | Wraps any effect with "as long as an opponent is poisoned" condition. Used for STATIC effects (e.g. conditional keyword grants) that apply when any opponent has 1+ poison counters. Used by Viridian Betrayers |
| `NoOtherSubtypeConditionalEffect` | `(CardSubtype subtype, CardEffect wrapped)` | Intervening-if wrapper: "if you control no [subtype] other than this creature". Checked at trigger time and resolution time per CR 603.4. Used by Thopter Assembly with THOPTER subtype |
| `ControllerLifeThresholdConditionalEffect` | `(int lifeThreshold, CardEffect wrapped)` | Wraps any effect with "as long as you have N or more life" condition. For static effects: wraps StaticBoostEffect or GrantKeywordEffect, applied only while controller's life total >= threshold (selfOnly handler). Used by Serra Ascendant with threshold 30 |
| `ControlsSubtypeConditionalEffect` | `(CardSubtype subtype, CardEffect wrapped)` | Wraps any effect with "as long as you control a [subtype]" condition. For static effects: wraps GrantKeywordEffect, StaticBoostEffect, or ProtectionFromColorsEffect, applied only while controller has at least one permanent with the specified subtype (selfOnly handler). Used by Angelic Overseer with HUMAN |
| `OpponentControlsSubtypeConditionalEffect` | `(CardSubtype subtype, CardEffect wrapped)` | Wraps any effect with "as long as an opponent controls a [subtype]" condition. For static effects: wraps GrantKeywordEffect, StaticBoostEffect, or ProtectionFromColorsEffect, applied only while any opponent has at least one permanent with the specified subtype (selfOnly handler). Used by Night Revelers with HUMAN |
| `SelfHasKeywordConditionalEffect` | `(Keyword keyword, CardEffect wrapped)` | Wraps any effect with "as long as this creature has [keyword]" condition. For static effects: wraps GrantKeywordEffect or StaticBoostEffect, applied only while the source permanent has the specified keyword (selfOnly handler). Accounts for temporary keyword removal from activated abilities. Used by Manor Gargoyle with DEFENDER wrapping INDESTRUCTIBLE |
| `ActivationCountConditionalEffect` | `(int threshold, int abilityIndex, CardEffect wrapped)` | Wraps any effect with "if ability has been activated N or more times this turn" condition. Checked at resolution time via `activatedAbilityUsesThisTurn`. Used by Dragon Whelp with threshold=4, abilityIndex=0, wrapping SacrificeSelfEffect |
| `DidntAttackConditionalEffect` | `(CardEffect wrapped)` | Intervening-if wrapper: "if this creature didn't attack this turn". Checked at trigger time (StepTriggerService) and resolution time (EffectResolutionService). Used by Homicidal Brute's CONTROLLER_END_STEP_TRIGGERED wrapping TapAndTransformSelfEffect |
| `NoSpellsCastLastTurnConditionalEffect` | `(CardEffect wrapped)` | Intervening-if wrapper: "if no spells were cast last turn". Checked at trigger time (StepTriggerService) and resolution time (EffectResolutionService). Used by Innistrad werewolf front faces with EACH_UPKEEP_TRIGGERED wrapping TransformSelfEffect (e.g. Daybreak Ranger) |
| `TwoOrMoreSpellsCastLastTurnConditionalEffect` | `(CardEffect wrapped)` | Intervening-if wrapper: "if a player cast two or more spells last turn". Checked at trigger time (StepTriggerService) and resolution time (EffectResolutionService). Used by Innistrad werewolf back faces with EACH_UPKEEP_TRIGGERED wrapping TransformSelfEffect (e.g. Nightfall Predator) |
| `TopCardOfLibraryColorConditionalEffect` | `(CardColor color, CardEffect wrapped)` | Wraps any effect with "as long as the top card of your library is [color]" condition. For static effects: wraps StaticBoostEffect or GrantKeywordEffect, applied only while top card of controller's library has the specified color (self + others handlers). Used by Vampire Nocturnus with BLACK |
| `MetalcraftReplacementEffect` | `(CardEffect baseEffect, CardEffect metalcraftEffect)` | Picks between base and upgraded effect at resolution based on metalcraft. Resolves `metalcraftEffect` if 3+ artifacts, otherwise `baseEffect`. Targeting delegates to both inner effects (union). No new handler needed — unwrapped in `EffectResolutionService` |
| `MorbidReplacementEffect` | `(CardEffect baseEffect, CardEffect morbidEffect)` | Picks between base and upgraded effect at resolution based on morbid. Resolves `morbidEffect` if a creature died this turn, otherwise `baseEffect`. Targeting delegates to both inner effects (union). No new handler needed — unwrapped in `EffectResolutionService`. Used by Brimstone Volley |
| `TargetSubtypeReplacementEffect` | `(CardSubtype subtype, CardEffect baseEffect, CardEffect upgradedEffect)` | Picks between base and upgraded effect at resolution based on whether the target permanent has the specified subtype. Targeting delegates to both inner effects (union). No new handler needed — unwrapped in `EffectResolutionService`. Used by Elder Cathar (HUMAN) |
| `FlipCoinWinEffect` | `(CardEffect wrapped)` | Wraps any effect with "Flip a coin. If you win the flip, [effect]." Flips a coin at resolution time; if won, dispatches the wrapped effect. If lost, nothing happens. Used by Sorcerer's Strongbox |
| `ChooseOneEffect` | `(List<ChooseOneOption> options)` | Modal spell: defines the available modes for "Choose one" spells. Each `ChooseOneOption(String label, CardEffect effect)` or `ChooseOneOption(String label, CardEffect effect, TargetFilter targetFilter)` pairs a display label with its effect and optional per-mode target filter. **Mode is chosen at cast time** (per CR 700.2a): `SpellCastingService` unwraps the `ChooseOneEffect` using the `xValue` parameter as the mode index (0-based) and places only the chosen inner effect on the stack. For ETB creatures, the per-mode `targetFilter` (if present) is set on the triggered ability's StackEntry for resolution-time validation. Used by Slagstorm, Deceiver Exarch |

---

## Damage

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `DealDamageToAnyTargetEffect` | `(int damage, boolean cantRegenerate)` | deal N damage to any target |
| `DealDamageEqualToSourcePowerToAnyTargetEffect` | `()` | deal damage equal to source permanent's power to any target (uses effective power at resolution) |
| `SourceFightsTargetCreatureEffect` | `()` | source permanent and target creature deal damage to each other equal to their respective powers (fight mechanic). Both use effective power at resolution |
| `PackHuntEffect` | `(CardSubtype creatureSubtype)` | pack hunt: tap all untapped creatures of given subtype controller controls, each deals damage equal to its power to target creature, target creature deals its power divided evenly among those creatures. Uses effective power at resolution |
| `DealDamageToTargetAndTheirCreaturesEffect` | `(int damage)` | deal N damage to target player or planeswalker AND each creature that player or that planeswalker's controller controls (e.g. Chandra Nalaar ultimate) |
| `DealDamageToEachCreatureDamagedPlayerControlsEffect` | `()` | deal damage equal to combat damage dealt (from xValue) to each creature the damaged player controls (from targetId). Used with ON_COMBAT_DAMAGE_TO_PLAYER slot. CombatDamageService passes damageDealt and defenderId automatically. Used by Balefire Dragon |
| `DealDamageToTargetCreatureEffect` | `(int damage)` or `(int damage, boolean unpreventable)` | deal N damage to target creature. When `unpreventable=true`, bypasses all damage prevention (shields, protection, global prevention) |
| `DealDamageToBlockedAttackersOnDeathEffect` | `(int damage)` | ON_DEATH marker: when this creature dies during combat, deal N damage to each creature it blocked this combat (e.g. Cathedral Membrane). Target permanent IDs baked in at trigger time from blockingTargetIds |
| `DealDamageToTargetPlayerEffect` | `(int damage)` | deal N damage to target player |
| `DealDamageToTargetPlayerByHandSizeEffect` | `()` | deal damage equal to hand size to target player |
| `MassDamageEffect` | `(int damage)` or `(int damage, boolean damagesPlayers)` or `(int damage, boolean usesXValue, boolean damagesPlayers, PermanentPredicate filter)` | deal N damage to all creatures (optionally filtered by predicate), optionally to all players too. Use `usesXValue=true` to use X value instead of fixed damage |
| `DealDamageToEachPlayerEffect` | `(int damage)` | deal N damage to each player (not creatures). Used by modal spells like Slagstorm's second mode |
| `DealDamageToAnyTargetEqualToChargeCountersOnSourceEffect` | `()` | deal damage to any target equal to charge counters on source (reads snapshotted count from xValue). Used by Shrine of Burning Rage |
| `DealDamageToAnyTargetAndGainLifeEffect` | `(int damage, int lifeGain)` | deal N damage and gain M life |
| `DealDamageToControllerEffect` | `(int damage)` | deal N damage to the card's controller (pain lands, self-damage) |
| `DealDamageToEnchantedPlayerEffect` | `(int damage)` or `(int damage, UUID affectedPlayerId)` | deal N damage to the enchanted player. 1-arg constructor for card definitions; `affectedPlayerId` baked in at trigger time. Used by Curse of the Pierced Heart |
| `DealDamageToTargetControllerIfTargetHasKeywordEffect` | `(int damage, Keyword keyword)` | deal N damage to targeted creature's controller if that creature has the specified keyword |
| `DealDamageToTargetCreatureControllerEffect` | `(int damage)` | deal N damage to targeted creature's controller unconditionally |
| `DealDamageToDiscardingPlayerEffect` | `(int damage)` | deal N damage to any player who discards (trigger) |
| `DealDamageToTriggeringPermanentControllerEffect` | `(int damage)` | deal N damage to the controller of the permanent that caused the trigger (target pre-set at trigger-collection time) |
| `DealDamageToTargetCreatureEqualToControlledSubtypeCountEffect` | `(CardSubtype subtype, boolean gainLife)` | deal damage to target creature equal to number of controlled permanents of subtype; when gainLife=true, gain life equal to the damage amount. Convenience ctor `(subtype)` defaults gainLife=false |
| `DealDamageToAnyTargetEqualToControlledSubtypeCountAndGainLifeEffect` | `(CardSubtype subtype, boolean gainLife)` | deal damage to any target equal to number of controlled permanents of subtype; when gainLife=true, gain life equal to the damage amount. Used by Corrupt |
| `DealDamageIfFewCardsInHandEffect` | `(int maxCards, int damage)` | deal N damage to target player if they have maxCards or fewer in hand |
| `DealDamageOnLandTapEffect` | `(int damage)` | deal N damage to a player whenever they tap a land (Manabarbs-style) |
| `DealDamageToEachOpponentEqualToCardsDrawnThisTurnEffect` | `()` | deal damage to each opponent equal to the number of cards that player has drawn this turn |
| `DealDamageToTargetPlayerEqualToCardTypeCountInGraveyardEffect` | `(CardType cardType)` | deal damage to target player equal to the number of cards of the specified type in controller's graveyard |
| `DealOrderedDamageToAnyTargetsEffect` | `(List<Integer> damageAmounts)` | deal different amounts to multiple targets (e.g. 3 then 1) |
| `DealXDamageToAnyTargetEffect` | `()` or `(boolean exileInsteadOfDie)` | deal X damage to any target (X spell). When `exileInsteadOfDie=true`, if target creature would die this turn, exile it instead (sets `exileInsteadOfDieThisTurn` flag, cleared at end of turn) |
| `DealXDamageToAnyTargetAndGainXLifeEffect` | `()` | deal X damage and gain X life (X spell) |
| `DealXDamageToTargetCreatureEffect` | `()` | deal X damage to target creature (X spell) |
| `DealXDamageDividedAmongTargetAttackingCreaturesEffect` | `()` | deal X damage divided among attacking creatures |
| `DealXDamageDividedEvenlyAmongTargetsEffect` | `()` | deal X damage divided evenly (rounded down) among any number of targets (creatures/players). Uses `targetIds` for target list. Pair with `setAdditionalCostPerExtraTarget(1)` for Fireball-style cost |
| `FirstTargetDealsPowerDamageToSecondTargetEffect` | `()` | first target creature deals damage equal to its power to second target creature (bite mechanic) |
| `FirstTargetFightsSecondTargetEffect` | `()` | first and second target creatures deal damage to each other equal to their respective powers (fight mechanic). Protection checked per creature color |
| `DoubleDamageEffect` | `()` | double all damage dealt (static) |
| `MultiplyTokenCreationEffect` | `(int multiplier)` | multiply the number of tokens created under the controller's control by the given factor (static). Controller-specific — only applies to the permanent's controller's token creation. Multiple instances stack multiplicatively. Used by Parallel Lives (multiplier=2) |
| `DoubleControllerSpellDamageEffect` | `(CardColor color)` | double damage from instant/sorcery spells of the specified color controlled by this permanent's controller (static). Unlike DoubleDamageEffect, only affects the controller's spells of the matching color. Used by Fire Servant |
| `DoubleEquippedCreatureCombatDamageEffect` | `()` | double combat damage dealt by and received by the equipped creature (static). Equipment-only effect. Used by Inquisitor's Flail |
| `DealDividedDamageToAnyTargetsEffect` | `(int totalDamage, int maxTargets)` | Deal N total damage divided as you choose among up to M targets (creatures/players). Damage assignments provided via `pendingETBDamageAssignments`. Does NOT use standard targeting. Used for ETB/attack triggers (e.g. Inferno Titan) |
| `DealDividedDamageAmongTargetCreaturesEffect` | `(int totalDamage)` | Deal N total damage divided as you choose among targeted creatures. Damage assignments stored on `StackEntry.damageAssignments`. Uses card's TargetFilter for creature/color restrictions. Set `minTargets`/`maxTargets` on card. Used for spell-based divided damage (e.g. Ignite Disorder) |
| `SacrificeArtifactThenDealDividedDamageEffect` | `(int totalDamage)` | Sacrifice an artifact, then deal N total damage divided among any number of targets (creatures/players). Wrap in `MayEffect` for optional sacrifice. Damage assignments provided before cast via `pendingETBDamageAssignments`. Does NOT use standard targeting |
| `SacrificeOtherCreatureOrDamageEffect` | `(int damage)` | sacrifice another creature or take N damage (upkeep trigger) |
| `SacrificeOtherCreatureOpponentsLoseLifeOrTapAndLoseLifeEffect` | `(int lifeLoss)` | sacrifice another creature, then each opponent loses life equal to sacrificed creature's power; if can't sacrifice, tap self and controller loses N life (upkeep trigger) |
| `SpellCastTriggerEffect` | `(CardPredicate spellFilter, List<CardEffect> resolvedEffects)` or `(CardPredicate spellFilter, List<CardEffect> resolvedEffects, String manaCost)` | generic trigger descriptor: when a spell matching the predicate is cast, put the resolved effects on the stack. Works in both `ON_ANY_PLAYER_CASTS_SPELL` and `ON_CONTROLLER_CASTS_SPELL` slots. Wrap in `MayEffect` for optional triggers. Set `manaCost` (e.g. `"{1}"`) for "may pay" triggers. Use `spellFilter = null` for "whenever you cast a spell" (any spell). Replaces the old per-card descriptors (GainLifeOnSpellCastEffect, DealDamageToAnyTargetOnArtifactCastEffect, etc.) |
| `DealDamageEqualToSpellManaValueToAnyTargetEffect` | `(CardPredicate spellFilter)` | spell-cast trigger: when controller casts a spell matching the filter, deal damage equal to that spell's mana value to any target. Uses `ON_CONTROLLER_CASTS_SPELL` slot. Target selection via `pendingSpellTargetTriggers` queue. See Rage Extractor |
| `CastFromGraveyardTriggerEffect` | `(List<CardEffect> resolvedEffects)` | trigger descriptor: whenever controller casts a spell from the graveyard (flashback, etc.), put the resolved effects on the stack. Uses `ON_CONTROLLER_CASTS_SPELL` slot. If resolved effects need any-target selection, uses `pendingSpellTargetTriggers` queue. See Burning Vengeance |
| `KickedSpellCastTriggerEffect` | `(List<CardEffect> resolvedEffects)` | trigger descriptor: whenever controller casts a spell, if that spell was kicked, put the resolved effects on the stack. Uses `ON_CONTROLLER_CASTS_SPELL` slot. Checks the stack for the spell's kicked status. See Bloodstone Goblin |
| `PlaneswalkerDealDamageAndReceivePowerDamageEffect` | `(int damage)` | source planeswalker deals N damage to target creature, then that creature deals damage equal to its power back to the source planeswalker (removing loyalty counters). `canTargetPermanent()=true`. Used by Garruk Relentless |

## Destruction / sacrifice

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `DestroyTargetPermanentEffect` | `(boolean cannotBeRegenerated)` or `(boolean cannotBeRegenerated, CreateCreatureTokenEffect tokenForController)` | destroy target permanent. When `tokenForController` is non-null, creates that token for the target's controller regardless of whether destruction succeeds (e.g. Beast Within, Pongify) |
| `DestroyTargetPermanentAtEndStepEffect` | `()` | schedule target permanent for destruction at the beginning of the next end step. The target is registered in `GameData.pendingDestroyAtEndStep` and destroyed when end step is processed (direct, not via stack). Used by Stone Giant |
| `DestroyAllPermanentsEffect` | `(PermanentPredicate filter)` or `(PermanentPredicate filter, boolean cannotBeRegenerated)` | destroy all permanents matching the predicate filter. Use `PermanentIsCreaturePredicate` for creatures, `PermanentNotPredicate(PermanentIsLandPredicate())` for nonland, compose with `PermanentAllOfPredicate` for multiple conditions (e.g. tapped creatures, opponent's creatures) |
| `DestroyAllPermanentsAndGainLifePerDestroyedEffect` | `(PermanentPredicate filter, int lifePerDestroyed)` | destroy all permanents matching the filter, then controller gains lifePerDestroyed life for each actually destroyed (skips indestructible/regenerated). Used by Paraselene |
| `EachPlayerChoosesCreatureDestroyRestEffect` | `()` | each player chooses a creature they control; destroy the rest. APNAP ordering, respects indestructible and regeneration. Players with 0-1 creatures auto-resolve. Uses the forced sacrifice queue in destroy-rest mode. Used by Divine Reckoning |
| `DestroyAllCreaturesAndCreateTokenFromDestroyedCountEffect` | `(String tokenName, List<CardSubtype> tokenSubtypes, Set<CardType> tokenAdditionalTypes)` | destroy all creatures, then create a colorless X/X creature token where X = number actually destroyed (skips indestructible/regenerated) |
| `DestroyTargetPermanentAndControllerSearchesLibraryToBattlefieldEffect` | `(CardPredicate searchFilter, boolean may)` | destroy target permanent; its controller searches their library for a card matching the filter and puts it onto the battlefield, then shuffles. When `may` is true, the search is optional. Used by Ghost Quarter (basic land filter, may=true) |
| `DestroyTargetLandAndDamageControllerEffect` | `(int damage)` | destroy target land and deal N to its controller |
| `DestroyTargetPermanentAndControllerLosesLifeEffect` | `(int lifeLoss)` | destroy target permanent and its controller loses N life. Life loss occurs regardless of whether destruction succeeds (e.g. indestructible) |
| `DestroyTargetPermanentAndGiveControllerPoisonCountersEffect` | `(int poisonCounters)` or `()` (default 1) | destroy target permanent and give its controller N poison counters |
| `DestroyBlockedCreatureAndSelfEffect` | `()` | destroy creature this blocks and itself (Deathtrap-style) |
| `DestroySourcePermanentEffect` | `()` | destroy the source permanent (identified by sourcePermanentId on the stack entry). Used by Ice Cage's becomes-target trigger |
| `DestroyCreatureBlockingThisEffect` | `()` | destroy creature that blocks this (combat trigger) |
| `DestroyTargetCreatureAndGainLifeEqualToToughnessEffect` | `()` | destroy target creature and gain life equal to its toughness (combat trigger, life gain occurs even if destroy fails). Works with both ON_BLOCK and ON_BECOMES_BLOCKED slots |
| `DestroySubtypeCombatOpponentEffect` | `(CardSubtype requiredSubtype, boolean cannotBeRegenerated)` | equipment combat trigger: when equipped creature blocks or becomes blocked by a creature with the required subtype, destroy that creature. At trigger creation time, the combat opponent's subtype is checked; if it matches, a `DestroyTargetPermanentEffect(cannotBeRegenerated)` is auto-targeted on the opponent and placed on the stack as non-targeting. Use ON_BLOCK (non-per-blocker) + ON_BECOMES_BLOCKED (PER_BLOCKER). See Wooden Stake |
| `DestroyTargetPermanentAndBoostSelfByManaValueEffect` | `()` | destroy target permanent and boost source creature +X/+0 until end of turn, where X is the permanent's mana value. Boost applies even if destruction fails (indestructible). Target type restriction handled by ability's target filter. Used by Hoard-Smelter Dragon |
| `DestroyTargetPermanentAndGainLifeEqualToManaValueEffect` | `()` | destroy target permanent and gain life equal to its mana value. Life gain occurs even if destruction fails (indestructible). Target type restriction handled by spell's target filter. Used by Divine Offering |
| `DestroyTargetAndControllerLosesLifePerCreatureDeathsEffect` | `()` | destroy target creature; its controller loses life equal to the number of creatures put into all graveyards from the battlefield this turn (counts ALL players' creature deaths). Used with `SacrificeCreatureCost` for Flesh Allergy |
| `DestroyOneOfTargetsAtRandomEffect` | `()` | destroy one permanent at random from `targetIds` on the stack entry. Filters out targets that left the battlefield before resolution. Used by Capricious Efreet's upkeep trigger (multi-target random destruction) |
| `DestroyEquipmentAttachedToTargetCreatureEffect` | `()` | destroy all Equipment attached to the target creature. Uses same target as co-located damage effect. Effect order doesn't matter; lethal damage destruction is deferred until all effects on the stack entry resolve. Resolved by `DestructionResolutionService` |
| `SacrificeCreatureEffect` | `()` | target player sacrifices a creature (has `canTargetPlayer()`) |
| `SacrificeCreatureAndControllerGainsLifeEqualToToughnessEffect` | `()` | target player sacrifices a creature; spell's controller gains life equal to the sacrificed creature's toughness (has `canTargetPlayer()`). Uses `SacrificeCreatureControllerGainsLifeEqualToToughness` choice context when multiple creatures. E.g. Tribute to Hunger |
| `ControllerSacrificesCreatureEffect` | `()` | controller sacrifices a creature (non-targeting, uses `entry.getControllerId()`). Use for abilities where the controller sacrifices without targeting a player (e.g. Stitcher's Apprentice) |
| `SacrificeAttackingCreaturesEffect` | `(int baseCount, int metalcraftCount)` | target player sacrifices attacking creatures; metalcraft upgrades count |
| `EachOpponentSacrificesCreatureEffect` | `()` | each opponent sacrifices a creature |
| `EachOpponentSacrificesPermanentsEffect` | `(int count, PermanentPredicate filter)` | each opponent sacrifices N permanents matching filter. Controller is excluded. Same APNAP simultaneous-sacrifice logic as EachPlayerSacrificesPermanentsEffect. Used by Yawning Fissure |
| `EachPlayerSacrificesPermanentsEffect` | `(int count, PermanentPredicate filter)` | each player sacrifices N permanents matching filter. Players with fewer than N matching permanents sacrifice all. Players with more are prompted to choose in APNAP order. Used by Destructive Force |
| `DamageSourceControllerGetsPoisonCounterEffect` | `(UUID damageSourceControllerId)` | damage source's controller gets a poison counter (e.g. Reaper of Sheoldred). Register as marker (null) on ON_DEALT_DAMAGE; actual UUID filled at trigger time |
| `DamageSourceControllerSacrificesPermanentsEffect` | `(int count, UUID sacrificingPlayerId)` | damage source's controller sacrifices that many permanents of their choice (e.g. Phyrexian Obliterator). Register as marker (0, null) on ON_DEALT_DAMAGE; actual values filled at trigger time |
| `SacrificeSelfEffect` | `()` | sacrifice this permanent |
| `SacrificeUnlessDiscardCardTypeEffect` | `(CardType requiredType)` | sacrifice unless you discard a card of type (null = any) |
| `SacrificeUnlessReturnOwnPermanentTypeToHandEffect` | `(CardType permanentType)` | sacrifice this permanent unless you return a permanent of the specified type you control to its owner's hand (ETB bounce-or-sacrifice, e.g. Glint Hawk) |
| `DestroyNonlandPermanentsWithManaValueEqualToChargeCountersEffect` | `()` | destroy each nonland permanent with mana value equal to the number of charge counters on source (reads snapshotted count from xValue). Used by Ratchet Bomb |
| `DestroyNonlandPermanentsWithManaValueXDealtCombatDamageEffect` | `()` | destroy each nonland permanent with mana value equal to X whose controller was dealt combat damage by the source permanent this turn. Reads X from xValue, checks combatDamageToPlayersThisTurn. Used by Steel Hellkite |
| `SacrificeSelfAndDrawCardsEffect` | `(int amount)` | "sacrifice this, then draw N cards." Wrap in `MayEffect` for "you may" behavior. Used on ON_COMBAT_DAMAGE_TO_PLAYER triggers. StackEntry context: sourcePermanentId = source creature. Resolved by `PlayerInteractionResolutionService` |
| `SacrificeSelfAndTargetDiscardsPerPoisonCounterEffect` | `()` | "sacrifice this, then that player discards a card for each poison counter they have." Wrap in `MayEffect` for "you may" behavior. Used on ON_COMBAT_DAMAGE_TO_PLAYER triggers where "that player" is the damaged player. StackEntry context: targetId = damaged player, sourcePermanentId = source creature. Resolved by `PlayerInteractionResolutionService` |
| `SacrificeSelfToDestroyCreatureDamagedPlayerControlsEffect` | `()` | "sacrifice this, then destroy target creature that player controls." Wrap in `MayEffect` for "you may" behavior. Used on ON_COMBAT_DAMAGE_TO_PLAYER triggers where "that player" is the damaged player. StackEntry context: targetId = damaged player, sourcePermanentId = source creature. Presents multi-permanent choice (max 1). Resolved by `DestructionResolutionService` |
| `SacrificeAtEndOfCombatEffect` | `()` | sacrifice at end of combat |
| `SacrificeTargetThenRevealUntilTypeToBattlefieldEffect` | `(Set<CardType> cardTypes)` | sacrifice the targeted permanent, then its controller reveals cards from the top of their library until a card matching one of the specified types is found; that card is put onto the battlefield under that player's control, and all other revealed cards are shuffled into their library. Polymorph-style effect (used by Shape Anew for artifact→artifact) |
| `DestroyTargetThenRevealUntilTypeToBattlefieldEffect` | `(boolean cannotBeRegenerated, Set<CardType> cardTypes)` | destroy the targeted permanent (optionally preventing regeneration), then its controller reveals cards from the top of their library until a card matching one of the specified types is found; that card is put onto the battlefield under that player's control, and all other revealed cards are shuffled into their library. Reveal happens regardless of whether destruction succeeds (indestructible). Used by Polymorph (creature→creature with cannotBeRegenerated=true) |
| `ExileAllCreaturesYouControlThenRevealCreaturesToBattlefieldEffect` | `()` | exile all creatures you control, then reveal cards from the top of your library until you reveal that many creature cards; put all revealed creature cards onto the battlefield, then shuffle the rest into your library. Mass Polymorph-style effect |
| `SeparatePermanentsIntoPilesAndSacrificeEffect` | `()` | separate all permanents target player controls into two piles; target player sacrifices all permanents in the chosen pile (Liliana of the Veil ultimate). Two-step async flow: controller assigns piles via multi-permanent choice, then target player picks pile to sacrifice via may-ability choice |

### Sacrifice costs (for activated abilities)

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `ExileSelfCost` | `()` | exile this permanent as cost (e.g. Brittle Effigy). Handled in `ActivatedAbilityExecutionService` via `removePermanentToExile` |
| `SacrificeSelfCost` | `()` | sacrifice this permanent as cost |
| `SacrificeCreatureCost` | `()` or `(boolean trackSacrificedManaValue)` or `(boolean trackSacrificedManaValue, boolean trackSacrificedPower)` or `(boolean trackSacrificedManaValue, boolean trackSacrificedPower, boolean trackSacrificedToughness)` or `(boolean trackSacrificedManaValue, boolean trackSacrificedPower, boolean trackSacrificedToughness, boolean excludeSelf)` | sacrifice a creature as cost. When `trackSacrificedManaValue=true`, the sacrificed creature's mana value is stored in the StackEntry's xValue (e.g. Birthing Pod). When `trackSacrificedPower=true`, the sacrificed creature's effective power (including static bonuses) is stored in xValue (e.g. Ichor Explosion). When `trackSacrificedToughness=true`, the sacrificed creature's effective toughness is stored in xValue (e.g. Disciple of Griselbrand). When `excludeSelf=true`, the source permanent cannot be chosen as the sacrifice target — use for "Sacrifice another creature" costs (e.g. Grimgrin, Corpse-Born) |
| `SacrificeSubtypeCreatureCost` | `(CardSubtype subtype)` | sacrifice a creature of specific subtype as cost |
| `SacrificeArtifactCost` | `()` | sacrifice an artifact as cost (works for both activated abilities and spell costs) |
| `SacrificePermanentCost` | `(PermanentPredicate filter, String description)` | sacrifice a permanent matching filter as cost. Uses `GameQueryService.matchesPermanentPredicate()` for validation. E.g. `new SacrificePermanentCost(new PermanentAnyOfPredicate(List.of(new PermanentIsArtifactPredicate(), new PermanentIsCreaturePredicate())), "Sacrifice an artifact or creature")` |
| `SacrificeMultiplePermanentsCost` | `(int count, PermanentPredicate filter)` | sacrifice N permanents matching filter as cost (e.g. "Sacrifice three artifacts: ..." with `PermanentIsArtifactPredicate`). Multi-step UI: if exactly N match, auto-sacrifices all; otherwise prompts one-at-a-time. Validated and paid in `AbilityActivationService` |
| `SacrificeAllCreaturesYouControlCost` | `()` | sacrifice all creatures you control as cost |
| `DiscardCardTypeCost` | `(CardType requiredType)` | discard a card of specific type as cost (null = any card) |
| `RemoveCounterFromSourceCost` | `(int count, CounterType counterType)` | remove N counters of the specified type from this permanent as cost. `CounterType.CHARGE` for charge counters, `MINUS_ONE_MINUS_ONE` for "-1/-1 counters", `PLUS_ONE_PLUS_ONE` for "+1/+1 counters", `STUDY` for study counters, `WISH` for wish counters, `ANY` to prefer -1/-1 then +1/+1. Compact: `()` defaults to `(1, ANY)`, `(int count)` defaults to `(count, ANY)` |
| `RemoveChargeCountersFromSourceCost` | `(int count)` | remove N charge counters from source as cost (e.g. "Remove three charge counters: ..."). Validated and paid in `AbilityActivationService` |
| `TapCreatureCost` | `(PermanentPredicate predicate)` | tap an untapped creature matching predicate you control as cost. Auto-selects if only one valid target; presents permanent choice if multiple. The creature can be summoning sick (no tap symbol in cost). E.g. `new TapCreatureCost(new PermanentColorInPredicate(Set.of(CardColor.BLUE)))` for "tap an untapped blue creature" |
| `TapMultiplePermanentsCost` | `(int count, PermanentPredicate filter)` or `(int count, PermanentPredicate filter, boolean excludeSource)` | tap N untapped permanents matching filter as cost. Auto-selects if exactly N valid; presents permanent choice one at a time if more. Use `excludeSource=true` when the ability also has `requiresTap=true` ({T} in cost) to prevent the source from being counted twice. E.g. `new TapMultiplePermanentsCost(5, new PermanentHasSubtypePredicate(CardSubtype.MYR))` for "tap five untapped Myr you control"; `new TapMultiplePermanentsCost(2, new PermanentIsCreaturePredicate(), true)` for "{T}, tap two untapped creatures you control" |
| `TapXPermanentsCost` | `(PermanentPredicate filter)` or `(PermanentPredicate filter, boolean excludeSource)` | tap X untapped permanents matching filter as cost, where X is the xValue chosen by the player at activation time. Like `TapMultiplePermanentsCost` but with dynamic count from xValue. Use `excludeSource=true` when the ability also has `requiresTap=true`. E.g. `new TapXPermanentsCost(new PermanentHasSubtypePredicate(CardSubtype.KNIGHT), true)` for "{T}, tap X untapped Knights you control" (Aryel, Knight of Windgrace) |
| `ExileCardFromGraveyardCost` | `(CardType requiredType)` or `(CardType requiredType, boolean payExiledCardManaCost, boolean imprintOnSource)` or `(CardType requiredType, boolean payExiledCardManaCost, boolean imprintOnSource, boolean trackExiledPower)` | exile a card of specific type from your graveyard as cost (null = any). For activated abilities: two-phase async flow prompts graveyard choice, then resumes activation (handled by `AbilityActivationService`). For spell costs: add to SPELL slot and pass `exileGraveyardCardIndex` (handled by `SpellCastingService`). When `payExiledCardManaCost=true`, the exiled card's mana cost is dynamically charged. When `imprintOnSource=true`, the exiled card is stored as imprinted card. When `trackExiledPower=true`, the exiled card's power becomes the X value (e.g. Corpse Lunge). 1-arg and 3-arg constructors default `trackExiledPower` to false |
| `ExileXCardsFromGraveyardCost` | `()` | exile X cards (any type) from your graveyard as an additional cost to cast a spell. The number of exiled cards becomes the X value. Player chooses which and how many cards to exile (0 or more). Add to SPELL slot and pass `exileGraveyardCardIndices` (List<Integer>) via `PlayCardRequest`. Handled by `SpellCastingService`. E.g. Harvest Pyre |
| `ExileNCardsFromGraveyardCost` | `(int count, CardType requiredType)` | exile exactly N cards of the specified type from your graveyard as an additional cost to cast a spell (null requiredType = any). Unlike `ExileXCardsFromGraveyardCost`, requires an exact count and does not set the X value. Add to SPELL slot and pass `exileGraveyardCardIndices` (List<Integer>) via `PlayCardRequest`. `GameBroadcastService` checks graveyard has enough matching cards for castability. Handled by `SpellCastingService`. E.g. Skaab Ruinator (3, CREATURE) |

## Counter spells

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `CounterSpellEffect` | `()` | counter target spell |
| `CounterSpellAndExileEffect` | `()` | counter target spell and exile it instead of putting it into its owner's graveyard |
| `CounterSpellIfControllerPoisonedEffect` | `()` | counter target spell if its controller is poisoned (has at least one poison counter) |
| `TargetSpellControllerLosesLifeEffect` | `(int amount)` | target spell's controller loses N life. Companion effect for counter spells (e.g. Psychic Barrier). Place before CounterSpellEffect so target is still on stack |
| `TargetSpellControllerDiscardsEffect` | `(int amount)` | target spell's controller discards N cards. Companion effect for counter spells (e.g. Frightful Delusion). Place before counter effect so target is still on stack |
| `CounterUnlessPaysEffect` | `(int amount)` | counter unless controller pays N generic mana |
| `RegisterDelayedCounterTriggerEffect` | `(int genericManaAmount)` | registers a delayed trigger (opening hand reveal) that counters each opponent's first spell unless they pay N generic mana. Handled by MayAbilityHandlerService, not GameService |
| `RegisterDelayedManaTriggerEffect` | `(ManaColor color, int amount)` | registers a delayed trigger (opening hand reveal) that adds N mana of the given color at the beginning of the revealing player's first precombat main phase. Handled by MayAbilityHandlerService, not GameService |
| `LeylineStartOnBattlefieldEffect` | `()` | marker effect for leyline cards: if in opening hand, player may begin the game with the card on the battlefield (CR 103.6a). Used as wrapped effect inside MayEffect, queued in MulliganService.startGame(), handled by MayMiscHandlerService.handleLeylineChoice(). Used by Leyline of Anticipation, Leyline of the Void |
| `ExileOpponentCardsInsteadOfGraveyardEffect` | `()` | static replacement effect: if a card would be put into an opponent's graveyard from anywhere, exile it instead (CR 614.1). Checked in GraveyardService.addCardToGraveyard(). Used by Leyline of the Void |
| `CantBeCounteredEffect` | `()` | this spell can't be countered (static). Replaces `setCantBeCountered(true)` |
| `CreatureSpellsCantBeCounteredEffect` | `()` | creature spells can't be countered (static) |
| `CreatureEnteringDontCauseTriggersEffect` | `()` | creatures entering don't cause abilities to trigger (static, e.g. Torpor Orb) |
| `CreaturesEnterAsCopyOfSourceEffect` | `()` | creatures you control enter as a copy of this creature (static replacement effect, e.g. Essence of the Wild). Handled in BattlefieldEntryService.putPermanentOntoBattlefield() |

## Bounce / return to hand

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `ReturnTargetPermanentToHandEffect` | `()` or `(int lifeLoss)` | return target permanent(s) to owner's hand; supports single-target via `targetId` and multi-target via `targetIds`. When `lifeLoss > 0`, each bounced permanent's controller loses that much life (e.g. Vapor Snag) |
| `ReturnCreaturesToOwnersHandEffect` | `(Set<TargetFilter> filters)` | return all creatures matching filters to owners' hands |
| `ReturnSelfToHandEffect` | `()` | return this permanent to owner's hand |
| `ReturnSelfToHandOnCoinFlipLossEffect` | `()` | return self to hand if coin flip is lost |
| `ReturnPermanentsOnCombatDamageToPlayerEffect` | `()` or `(PermanentPredicate filter)` | return permanents when combat damage dealt to player (Ninja-style). Optional filter restricts which permanents can be chosen (e.g. `PermanentIsCreaturePredicate` for creatures only) |
| `ReturnArtifactsTargetPlayerOwnsToHandEffect` | `()` | return all artifacts target player owns to hand |
| `BounceCreatureOnUpkeepEffect` | `(Scope scope, Set<TargetFilter> filters, String prompt)` | at upkeep, return a creature matching filters. Scope: `SOURCE_CONTROLLER`, `TRIGGER_TARGET_PLAYER` |
| `ReturnSelfToHandAndCreateTokensEffect` | `(CreateCreatureTokenEffect tokenEffect)` | return source to hand then create tokens (compound upkeep effect, e.g. Thopter Assembly) |
| `SacrificeEnchantedCreatureAndCreateTokenEffect` | `(CreateCreatureTokenEffect tokenEffect)` | sacrifice the enchanted creature, then create a token for the aura's controller (compound upkeep effect, e.g. Parasitic Implant) |
| `SacrificeEnchantedCreatureEffect` | `()` | sacrifice the enchanted creature without additional effects. Use with `ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED` for "at the beginning of your upkeep, sacrifice this creature" (e.g. Necrotic Plague) |
| `ReturnSourceAuraToOpponentCreatureOnDeathEffect` | `()` / `(UUID enchantedCreatureControllerId)` | when enchanted creature dies, return this aura from owner's graveyard to battlefield attached to a creature controlled by an opponent of the dying creature's controller. UUID is baked in at trigger time. Use with `ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD` (e.g. Necrotic Plague) |
| `ReturnDamageSourcePermanentToHandEffect` | `()` | whenever a permanent deals damage to controller, return it to owner's hand (Dissipation Field-style). Use with `ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU` slot |
| `DamageSourceControllerGainsControlOfThisPermanentEffect` | `(boolean combatOnly, boolean creatureOnly)` | whenever a permanent deals damage to controller, the damage source's controller gains control of this permanent (Contested War Zone-style). Use with `ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU` slot. `combatOnly=true` restricts to combat damage only; `creatureOnly=true` restricts to creature damage sources only |
| `PutTargetOnBottomOfLibraryEffect` | `()` | put target permanent on bottom of owner's library |
| `PutTargetOnTopOfLibraryEffect` | `()` | put target permanent on top of owner's library |

## Graveyard return

### Unified effect: `ReturnCardFromGraveyardEffect`

All graveyard-to-hand and graveyard-to-battlefield return effects are handled by a single unified record.

**Builder pattern:**
```
ReturnCardFromGraveyardEffect.builder()
    .destination(GraveyardChoiceDestination)   // HAND, BATTLEFIELD, or TOP_OF_OWNERS_LIBRARY (required)
    .filter(CardPredicate)                     // which cards qualify (default: null = any)
    .source(GraveyardSearchScope)              // default: CONTROLLERS_GRAVEYARD — omit unless ALL_GRAVEYARDS or OPPONENT_GRAVEYARD
    .targetGraveyard(boolean)                  // true = player chooses whose graveyard to search at cast time (default: false)
    .returnAll(boolean)                        // true = return all matching cards, false = choose one (default: false)
    .thisTurnOnly(boolean)                     // true = only cards put there from battlefield this turn (default: false)
    .attachmentTarget(PermanentPredicate)      // non-null = aura attaches to matching permanent on ETB (default: null)
    .gainLifeEqualToManaValue(boolean)         // true = controller gains life equal to returned card's mana value (default: false)
    .attachToSource(boolean)                   // true = auto-attach returned equipment to the source permanent (default: false)
    .grantHaste(boolean)                       // true = grant haste to the returned permanent (default: false)
    .exileAtEndStep(boolean)                   // true = exile at next end step (default: false)
    .requiresManaValueEqualsX(boolean)         // true = restrict to cards with MV = X (default: false)
    .grantColor(CardColor)                     // non-null = permanently grant this color "in addition to" (default: null)
    .grantSubtype(CardSubtype)                 // non-null = permanently grant this subtype "in addition to" (default: null)
    .enterTapped(boolean)                      // true = enters the battlefield tapped (default: false)
    .underOwnersControl(boolean)               // true = put each card onto battlefield under its owner's control (default: false)
    .returnAtRandom(boolean)                   // true = return a random matching card instead of player choice (default: false)
    .choosePermanentType(boolean)              // true = prompt controller to choose a permanent type at resolution, then return all matching (default: false); implies returnAll
    .exileSourceFromGraveyard(boolean)         // true = exile the source card from graveyard before selecting random cards (default: false); e.g. Moldgraf Monstrosity
    .build()
```

Only `destination` is required. All booleans default to `false`, all object references default to `null`, and `source` defaults to `CONTROLLERS_GRAVEYARD`. Only set fields that differ from defaults.

**CardPredicate filter system** (in `model/filter/`):

| Predicate | Constructor | Matches |
|-----------|-------------|---------|
| `CardTypePredicate` | `(CardType cardType)` | cards of a given type (CREATURE, ARTIFACT, etc.) |
| `CardSubtypePredicate` | `(CardSubtype subtype)` | cards of a given subtype (ZOMBIE, GOBLIN, etc.) |
| `CardKeywordPredicate` | `(Keyword keyword)` | cards with a given keyword (INFECT, FLYING, etc.) |
| `CardIsSelfPredicate` | `()` | only the source card itself (Squee-style self-return) |
| `CardIsAuraPredicate` | `()` | aura cards |
| `CardIsPermanentPredicate` | `()` | permanent cards (creature, artifact, enchantment, planeswalker, land, kindred — not instant/sorcery) |
| `CardMaxManaValuePredicate` | `(int maxManaValue)` | cards with mana value ≤ maxManaValue |
| `CardMinManaValuePredicate` | `(int minManaValue)` | cards with mana value ≥ minManaValue |
| `CardSupertypePredicate` | `(CardSupertype supertype)` | cards with a given supertype (BASIC, LEGENDARY) |
| `CardColorPredicate` | `(CardColor color)` | cards of a given color |
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
| `TOP_OF_OWNERS_LIBRARY` | put card on top of its owner's library (e.g. Noxious Revival) |

**Migration from old effects:**

| Old effect | New equivalent |
|------------|----------------|
| `ReturnCardFromGraveyardToHandEffect()` | `ReturnCardFromGraveyardEffect.builder().destination(HAND).targetGraveyard(true).build()` |
| `ReturnCardFromGraveyardToHandEffect(CardType.CREATURE)` | `ReturnCardFromGraveyardEffect.builder().destination(HAND).filter(new CardTypePredicate(CREATURE)).build()` |
| `ReturnCardOfSubtypeFromGraveyardToHandEffect(subtype)` | `ReturnCardFromGraveyardEffect.builder().destination(HAND).filter(new CardSubtypePredicate(subtype)).build()` |
| `ReturnCardWithKeywordFromGraveyardToHandEffect(type, kw)` | `ReturnCardFromGraveyardEffect.builder().destination(HAND).filter(new CardAllOfPredicate(List.of(new CardTypePredicate(type), new CardKeywordPredicate(kw)))).build()` |
| `ReturnSelfFromGraveyardToHandEffect()` | `ReturnCardFromGraveyardEffect.builder().destination(HAND).filter(new CardIsSelfPredicate()).returnAll(true).build()` |
| `ReturnCreatureFromGraveyardToBattlefieldEffect()` | `ReturnCardFromGraveyardEffect.builder().destination(BATTLEFIELD).filter(new CardTypePredicate(CREATURE)).build()` |
| `ReturnArtifactOrCreatureFromAnyGraveyardToBattlefieldEffect()` | `ReturnCardFromGraveyardEffect.builder().destination(BATTLEFIELD).filter(new CardAnyOfPredicate(List.of(new CardTypePredicate(ARTIFACT), new CardTypePredicate(CREATURE)))).source(ALL_GRAVEYARDS).build()` |
| `ReturnAuraFromGraveyardToBattlefieldEffect()` | `ReturnCardFromGraveyardEffect.builder().destination(BATTLEFIELD).filter(new CardIsAuraPredicate()).attachmentTarget(attachmentTarget).build()` |
| `ReturnCreatureCardsPutIntoYourGraveyardFromBattlefieldThisTurnToHandEffect()` | `ReturnCardFromGraveyardEffect.builder().destination(HAND).filter(new CardTypePredicate(CREATURE)).returnAll(true).thisTurnOnly(true).build()` |

**Common usage examples:**

| Card | Usage |
|------|-------|
| Recollect | `ReturnCardFromGraveyardEffect.builder().destination(HAND).targetGraveyard(true).build()` — any card, targets graveyard |
| Gravedigger | `MayEffect(ReturnCardFromGraveyardEffect.builder().destination(HAND).filter(new CardTypePredicate(CREATURE)).build())` — creature to hand |
| Corpse Cur | `ReturnCardFromGraveyardEffect.builder().destination(HAND).filter(new CardAllOfPredicate(List.of(new CardTypePredicate(CREATURE), new CardKeywordPredicate(INFECT)))).build()` — creature with infect |
| Lord of the Undead | `ReturnCardFromGraveyardEffect.builder().destination(HAND).filter(new CardSubtypePredicate(ZOMBIE)).build()` — Zombie subtype |
| Doomed Necromancer | `ReturnCardFromGraveyardEffect.builder().destination(BATTLEFIELD).filter(new CardTypePredicate(CREATURE)).build()` — creature to battlefield |
| Beacon of Unrest | `ReturnCardFromGraveyardEffect.builder().destination(BATTLEFIELD).filter(new CardAnyOfPredicate(...)).source(ALL_GRAVEYARDS).build()` — artifact or creature from any graveyard |
| Nomad Mythmaker | `ReturnCardFromGraveyardEffect.builder().destination(BATTLEFIELD).filter(new CardIsAuraPredicate()).attachmentTarget(new PermanentIsCreaturePredicate()).build()` — aura to battlefield attached to creature |
| Auriok Survivors | `MayEffect(ReturnCardFromGraveyardEffect.builder().destination(BATTLEFIELD).filter(new CardSubtypePredicate(EQUIPMENT)).attachToSource(true).build())` — equipment to battlefield attached to source |
| Squee, Goblin Nabob | `ReturnCardFromGraveyardEffect.builder().destination(HAND).filter(new CardIsSelfPredicate()).returnAll(true).build()` — self-return |
| No Rest for the Wicked | `ReturnCardFromGraveyardEffect.builder().destination(HAND).filter(new CardTypePredicate(CREATURE)).returnAll(true).thisTurnOnly(true).build()` — all creatures that died this turn |
| Razor Hippogriff | `ReturnCardFromGraveyardEffect.builder().destination(HAND).filter(new CardTypePredicate(ARTIFACT)).gainLifeEqualToManaValue(true).build()` — artifact to hand + gain life equal to mana value |
| Noxious Revival | `ReturnCardFromGraveyardEffect.builder().destination(TOP_OF_OWNERS_LIBRARY).source(ALL_GRAVEYARDS).targetGraveyard(true).build()` — any card from any graveyard on top of owner's library |
| Postmortem Lunge | `ReturnCardFromGraveyardEffect.builder().destination(BATTLEFIELD).filter(new CardTypePredicate(CREATURE)).targetGraveyard(true).grantHaste(true).exileAtEndStep(true).requiresManaValueEqualsX(true).build()` — X-cost creature with MV=X from your graveyard to battlefield with haste; exile at next end step |
| Rise from the Grave | `ReturnCardFromGraveyardEffect.builder().destination(BATTLEFIELD).filter(new CardTypePredicate(CREATURE)).source(ALL_GRAVEYARDS).grantColor(CardColor.BLACK).grantSubtype(CardSubtype.ZOMBIE).build()` — creature from any graveyard to battlefield as a black Zombie in addition to other colors/types |
| Reassembling Skeleton | `ReturnCardFromGraveyardEffect.builder().destination(BATTLEFIELD).filter(new CardIsSelfPredicate()).returnAll(true).enterTapped(true).build()` — self-return to battlefield tapped (graveyard activated ability) |
| Open the Vaults | `ReturnCardFromGraveyardEffect.builder().destination(BATTLEFIELD).filter(new CardAnyOfPredicate(List.of(new CardTypePredicate(ARTIFACT), new CardTypePredicate(ENCHANTMENT)))).source(ALL_GRAVEYARDS).returnAll(true).underOwnersControl(true).build()` — return all artifacts and enchantments from all graveyards under their owners' control |
| Charmbreaker Devils | `ReturnCardFromGraveyardEffect.builder().destination(HAND).filter(new CardAnyOfPredicate(List.of(new CardTypePredicate(INSTANT), new CardTypePredicate(SORCERY)))).returnAtRandom(true).build()` — return a random instant or sorcery from your graveyard to hand |
| Make a Wish | `ReturnCardFromGraveyardEffect.builder().destination(HAND).returnAtRandom(true).randomCount(2).build()` — return two cards at random from your graveyard to hand |
| Creeping Renaissance | `ReturnCardFromGraveyardEffect.builder().destination(HAND).returnAll(true).choosePermanentType(true).build()` — choose a permanent type, return all cards of that type from your graveyard to hand |
| Moldgraf Monstrosity | `ReturnCardFromGraveyardEffect.builder().destination(BATTLEFIELD).filter(new CardTypePredicate(CREATURE)).returnAtRandom(true).randomCount(2).exileSourceFromGraveyard(true).build()` — on death, exile self from graveyard then return two random creature cards to battlefield |

### Other graveyard effects

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `PutTargetCardsFromGraveyardOnTopOfLibraryEffect` | `(CardPredicate filter)` | put any number of target cards matching filter from controller's graveyard on top of library. Multi-target selection at cast time via SpellCastingService "any number" graveyard targeting. Used by Frantic Salvage |
| `ReturnTargetCardsFromGraveyardToHandEffect` | `(CardPredicate filter, int maxTargets)` | return up to maxTargets target cards matching filter from controller's graveyard to hand. Multi-target selection at cast time via SpellCastingService "up to N" graveyard targeting. Used by Morbid Plunder |
| `ShuffleTargetCardsFromGraveyardIntoLibraryEffect` | `(CardPredicate filter, int maxTargets)` | target player shuffles up to maxTargets target cards matching filter from their graveyard into their library. `canTargetPlayer()=true`. Target player chosen at cast time, then graveyard card selection prompted via "up to N target player's graveyard" targeting. Used by Memory's Journey |
| `ReturnDyingCreatureToBattlefieldAndAttachSourceEffect` | `(UUID dyingCardId)` or `()` | return a dying nontoken creature to the battlefield and attach the source equipment to it. No-arg constructor (dyingCardId is null) used in card definition; dyingCardId populated at trigger time in GameHelper. Wrap in MayPayManaEffect for "you may pay {X}" triggers. Used by Nim Deathmantle |
| `PutCardFromOpponentGraveyardOntoBattlefieldEffect` | `(boolean tapped)` | put target artifact/creature with MV=X from opponent's graveyard onto battlefield under your control (tapped if `tapped=true`), then mill that player X cards |
| `PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect` | `()` | put target creature card from opponent's graveyard onto battlefield under your control with haste; exile at beginning of next end step; if it would leave the battlefield, exile it instead |
| `PutImprintedCreatureOntoBattlefieldEffect` | `()` | when this creature dies, reveal imprinted card; if creature, put onto battlefield (Clone Shell dies trigger) |
| `PutImprintedCardIntoOwnersHandEffect` | `()` | when this permanent dies, put the imprinted (exiled) card into its owner's hand (Hoarding Dragon dies trigger) |

## Draw / discard / hand manipulation

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `DrawCardEffect` | `(int amount)` | draw N cards |
| `EachPlayerDrawsCardEffect` | `(int amount)` | each player draws N cards |
| `DrawCardForTargetPlayerEffect` | `(int amount, boolean requireSourceUntapped, boolean targetsPlayer)` | target player draws N cards; optionally requires source untapped; when `targetsPlayer=true`, auto-derives player targeting for activated abilities. Compact: `(int amount)` defaults to `(amount, false, false)` |
| `DrawXCardsEffect` | `()` | controller draws X cards (reads X from stack entry xValue; non-targeting) |
| `DrawXCardsForTargetPlayerEffect` | `()` | target player draws X cards (reads X from stack entry xValue; targets player) |
| `DrawCardsEqualToChargeCountersOnSourceEffect` | `()` | draw cards equal to charge counters on source (reads snapshotted count from xValue) |
| `DrawAndLoseLifePerSubtypeEffect` | `(CardSubtype subtype)` | draw cards and lose life for each permanent of subtype you control |
| `DrawAndDiscardCardEffect` | `(int drawAmount, int discardAmount)` | draw N then discard N cards (loot). Convenience ctor: `()` defaults to `(1, 1)`. Commonly wrapped in MayEffect for "you may draw a card. If you do, discard a card." |
| `DiscardCardEffect` | `(int amount)` | discard N cards |
| `EachPlayerDiscardsEffect` | `(int amount)` | each player discards N cards in APNAP order (active player first). Uses queued sequential discard interaction. Controller's discard has `discardCausedByOpponent=false`; others have `true`. |
| `EachOpponentDiscardsEffect` | `(int amount)` | each opponent discards N cards in APNAP order (skips controller). Uses same queued sequential discard interaction as EachPlayerDiscardsEffect. All discards have `discardCausedByOpponent=true`. Convenience ctor: `()` defaults to amount=1. |
| `EachPlayerRandomDiscardEffect` | `(int amount)` | each player discards N cards at random in APNAP order. No player interaction required (random selection). Controller's discard has `discardCausedByOpponent=false`; others have `true`. Used by Burning Inquiry. |
| `TargetPlayerDiscardsByChargeCountersEffect` | `()` | target player discards X cards where X = charge counters on source (snapshotted into xValue at activation time) |
| `TargetPlayerDiscardsEffect` | `(int amount)` | target player discards N cards |
| `TargetPlayerDiscardsReturnSelfIfCardTypeEffect` | `(int amount, CardType returnIfType)` | target player discards N cards; if a discarded card matches the type, return the source spell from graveyard to owner's hand (e.g. Psychic Miasma) |
| `TargetPlayerRandomDiscardEffect` | `(int amount, boolean causedByOpponent)` | target player discards N cards at random. Convenience ctors: `()` → amount=1, causedByOpponent=true (e.g. Hypnotic Specter); `(int amount)` → causedByOpponent=false (e.g. Goblin Lore self-discard). When `causedByOpponent=true`, uses `entry.getTargetId()` for who discards and sets `discardCausedByOpponent = true`; when `false`, uses `entry.getControllerId()`. |
| `TargetPlayerRandomDiscardXEffect` | `()` | target player discards X cards at random, where X is the X value paid when casting the spell (read from `entry.getXValue()`). Always treats discard as caused by opponent. Used by Mind Shatter. |
| `ChooseCardFromTargetHandToDiscardEffect` | `(int count, List<CardType> excludedTypes)` | choose N cards from target's hand to discard (excludedTypes can't be chosen) |
| `ChooseCardFromTargetHandToExileEffect` | `(int count, List<CardType> excludedTypes)` | choose N cards from target's hand to exile (excludedTypes can't be chosen). Same as ChooseCardFromTargetHandToDiscardEffect but exiles instead of discarding. Also has `(int count, List<CardType> excludedTypes, List<CardType> includedTypes)` overload. |
| `ChooseCardsFromTargetHandToTopOfLibraryEffect` | `(int count)` | choose N cards from target hand to put on top of library |
| `LookAtHandEffect` | `()` | look at target player's hand |
| `RevealOpponentHandsEffect` | `()` | reveal all opponents' hands |
| `RevealRandomCardFromTargetPlayerHandEffect` | `()` | target player reveals a card at random from their hand (e.g. Merfolk Spy combat damage trigger) |
| `HeadGamesEffect` | `()` | exchange target player's hand with cards from your library (Head Games) |
| `RedirectDrawsEffect` | `()` | redirect opponent's draws to controller (static, e.g. Plagiarize-style) |
| `ShuffleHandIntoLibraryAndDrawEffect` | `()` | each player shuffles cards from their hand into their library, then draws that many cards (wheel effect) |
| `EachPlayerShufflesHandAndGraveyardIntoLibraryEffect` | `()` | each player shuffles their hand and graveyard into their library. Used by Timetwister-family cards (Time Reversal, Timetwister). Combine with EachPlayerDrawsCardEffect for the draw portion |

## Library manipulation

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `SacrificeCreatureSearchLibraryForCreatureToHandEffect` | `()` | sacrifice a creature (as effect, not cost); if you do, search library for a creature card, reveal it, put it into your hand, then shuffle. With 0 creatures nothing happens; with 1 auto-sacrifices; with 2+ prompts choice via `SacrificeCreatureThenSearchLibrary` context. Used by Garruk, the Veil-Cursed |
| `SearchLibraryForCardToHandEffect` | `()` | search library for any card to hand |
| `SearchLibraryForCardsByNameToHandEffect` | `(String cardName, int maxCount)` | search library for up to N cards with specified name, reveal them, put into hand. Multi-pick via remainingCount. Used by Squadron Hawk |
| `SearchLibraryForBasicLandToHandEffect` | `()` | search library for basic land to hand |
| `SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect` | `()` | search library for up to two basic lands: one to battlefield tapped, one to hand. Single search action (one Leonin Arbiter check). Used by Cultivate, Kodama's Reach |
| `SearchLibraryForCardTypesToHandEffect` | `(CardPredicate filter)` | search library for a card matching the CardPredicate and put into hand. Description auto-generated via `CardPredicateUtils.describeFilter()` |
| `SearchLibraryForCardTypesToBattlefieldEffect` | `(CardPredicate filter, boolean entersTapped)` or `(CardPredicate filter, boolean entersTapped, int maxCount)` | search library for card(s) matching CardPredicate to battlefield. When maxCount > 1, uses multi-pick (e.g. Primeval Titan searches for up to 2 lands) |
| `SearchLibraryForCardTypeToExileAndImprintEffect` | `(Set<CardType> cardTypes)` | search library for card of specific types, exile it, and imprint on source permanent |
| `SearchLibraryForCreatureWithMVXOrLessToHandEffect` | `()` | search library for creature with MV X or less to hand |
| `SearchLibraryForCreatureWithColorAndMVXOrLessToBattlefieldEffect` | `(CardColor requiredColor)` | search library for creature of specified color with MV X or less to battlefield |
| `SearchLibraryForCreatureWithSubtypeToBattlefieldEffect` | `(CardSubtype requiredSubtype)` | search library for creature card with specified subtype and put it onto the battlefield |
| `SearchLibraryForSubtypeToBattlefieldAttachedToTargetPlayerEffect` | `(CardSubtype requiredSubtype)` | search library for card with specified subtype and put it onto the battlefield attached to target player. `canTargetPlayer()=true`. Used for Curse search (e.g. Bitterheart Witch) |
| `SearchLibraryForCreatureWithExactMVToBattlefieldEffect` | `(int mvOffset)` | search library for creature with MV exactly equal to xValue + mvOffset, put onto battlefield. Used with `SacrificeCreatureCost(true)` which stores sacrificed creature's MV in xValue (e.g. Birthing Pod with mvOffset=1) |
| `CastTopOfLibraryWithoutPayingManaCostEffect` | `(Set<CardType> castableTypes)` | look at top card of controller's library; if it matches one of the castable types, may cast it without paying its mana cost |
| `RevealTopCardMayPlayFreeOrExileEffect` | `()` | reveal top card of controller's library; may play it (any type, including lands) without paying its mana cost; if not played, exile it. Used by Djinn of Wishes |
| `CastTargetInstantOrSorceryFromGraveyardEffect` | `(GraveyardSearchScope scope, boolean withoutPayingManaCost)` | ETB: target instant or sorcery from a graveyard matching scope, you may cast it (without paying mana cost if flag is true). Has `canTargetGraveyard()=true`. Graveyard targeting handled by GameHelper ETB flow |
| `DistantMemoriesEffect` | `()` | search library for any card, exile it, shuffle; opponent may let you have it, otherwise draw 3 |
| `SearchLibraryForCardToTopOfLibraryEffect` | `()` | search library for any card, then shuffle and put that card on top of library (unrestricted, no reveal) |
| `SearchLibraryForCreatureToTopOfLibraryEffect` | `()` | search library for a creature card, reveal it, then shuffle and put that card on top of library |
| `SearchTargetLibraryForCardsToGraveyardEffect` | `(int maxCount, Set<CardType> cardTypes)` | search target opponent's library for up to N cards of specified types and put them into that player's graveyard, then shuffle. Targets player. Multi-pick via remainingCount |
| `SearchTargetLibraryForCardToExileWithPlayPermissionEffect` | `()` | search target opponent's library for any card, exile it face down, shuffle. Grants caster permission to play the exiled card for as long as it remains exiled. Targets player |
| `PayManaAndSearchLibraryForCardNamedToBattlefieldEffect` | `(String manaCost, String cardName)` | pay mana, search for named card to battlefield |
| `LookAtTopCardMayRevealTypeTransformEffect` | `(Set<CardType> cardTypes)` | look at top card, the controller may reveal it (regardless of type). If revealed card matches one of the card types, transform the source permanent. Card stays on top of library. Used by Delver of Secrets with `Set.of(INSTANT, SORCERY)` and `UPKEEP_TRIGGERED` |
| `LookAtTopCardsMayRevealCreaturePutIntoHandRestOnBottomEffect` | `(int count, Set<CardType> cardTypes)` or `(int count, Set<CardType> cardTypes, boolean anyNumber)` | look at top N, may reveal matching type to hand, rest on bottom; anyNumber=true allows multi-select |
| `LookAtTopCardsPutMatchingPermanentNameOnBattlefieldEffect` | `(int count)` | look at top N cards, may put one onto battlefield if it shares a name with any permanent, rest on bottom in any order |
| `ImprintFromTopCardsEffect` | `(int count)` | look at top N cards, exile one face down (imprint on source), rest on bottom in any order |
| `LookAtTopCardsOfTargetLibraryMayExileOneEffect` | `(int count)` | look at top N cards of target player's library, may exile one, rest on top in any order (used by Psychic Surgery) |
| `LookAtTopCardsChooseOneToHandRestToGraveyardEffect` | `(int count)` | look at top N cards, choose one to hand, rest to graveyard |
| `LookAtTopCardsHandTopBottomEffect` | `(int count)` | look at top N cards, choose hand/top/bottom for each |
| `LookAtTopCardsPerChargeCounterChooseOneToHandRestOnBottomEffect` | `()` | look at top X cards (X = charge counters on source, snapshotted before sacrifice), choose one to hand, rest on bottom in any order |
| `ReorderTopCardsOfLibraryEffect` | `(int count)` | reorder top N cards of library |
| `ScryEffect` | `(int count)` | scry N — look at top N cards, put any number on bottom in any order, rest on top in any order |
| `RevealTopCardDealManaValueDamageEffect` | `(boolean damageTargetPlayer, boolean damageTargetCreatures, boolean returnToHandIfLand)` | reveal top card of target's library, deal mana value damage to player/creatures, optionally return to hand if land |
| `RevealTopCardPutIntoHandAndLoseLifeEffect` | `()` | reveal top card of controller's library, put into hand, lose life equal to mana value (Dark Confidant/Dark Tutelage style) |
| `RevealTopCardsTypeToHandRestToGraveyardEffect` | `(int count, Set<CardType> cardTypes)` | reveal top N cards of controller's library; all cards matching the specified types go to hand, the rest go to graveyard. No player choice — deterministic sorting. Used by Mulch (count=4, LAND) |
| `RevealTopCardOfLibraryEffect` | `()` | reveal top card of target player's library (one-shot activated ability, e.g. Aven Windreader) |
| `PlayWithTopCardRevealedEffect` | `()` | static marker: "Play with the top card of your library revealed." While on battlefield, controller's library top card is visible to all players in UI. Used by Vampire Nocturnus |
| `RevealTopCardCreatureToBattlefieldOrMayBottomEffect` | `()` | reveal top card of controller's library; if creature, put onto battlefield; otherwise, may put on bottom of library. Used by Lurking Predators (ON_OPPONENT_CASTS_SPELL trigger) |
| `EachPlayerNameCardRevealTopEffect` | `()` | each player names a card, then each reveals top card — match goes to hand, mismatch goes to bottom (ON_ATTACK trigger, Conundrum Sphinx) |
| `ExileSpellEffect` | `()` | exile this spell instead of putting it into the graveyard after resolution (marker, like ShuffleIntoLibraryEffect) |
| `ShuffleIntoLibraryEffect` | `()` | shuffle this permanent into owner's library |
| `ShuffleGraveyardIntoLibraryEffect` | `()` | shuffle graveyard into library |
| `ShuffleSelfAndGraveyardIntoLibraryEffect` | `()` | shuffle source permanent and controller's graveyard into library |
| `ShuffleSelfIntoOwnerLibraryRevealUntilNameToBattlefieldEffect` | `(String cardName)` | shuffle source permanent into owner's library, then reveal cards until finding one with the given name — found card goes to battlefield under owner's control, all other revealed cards go to owner's graveyard |
| `ShuffleLibraryEffect` | `()` | shuffle controller's library (no cards moved) — used with MayEffect for "you may shuffle" |

## Mill

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `MillControllerEffect` | `(int count)` | controller mills N cards (self-mill, no target). Used by Armored Skaab |
| `MillControllerAndDealDamageByHighestManaValueEffect` | `(int count)` | mills N cards from controller's library, deals damage to any target equal to greatest mana value among milled cards. Used by Heretic's Punishment |
| `MillControllerCost` | `(int count)` | **Cost**: controller mills N cards as activation cost. Blocks activation if library too small. Used by Deranged Assistant |
| `MillTargetPlayerAndBoostSelfByManaValueEffect` | `()` | target player mills one card, source creature gets +X/+X until end of turn where X is milled card's mana value. Used by Mindshrieker |
| `MillTargetPlayerEffect` | `(int count)` | target player mills N cards |
| `MillHalfLibraryEffect` | `()` | target player mills half their library |
| `MillByHandSizeEffect` | `()` | target player mills cards equal to hand size |
| `MillTargetPlayerByChargeCountersEffect` | `()` | target player mills X cards where X is charge counters on source (reads snapshotted count from xValue) |
| `EachOpponentMillsEffect` | `(int count)` | each opponent mills N cards |
| `MillOpponentOnLifeLossEffect` | `()` | triggered effect: whenever an opponent loses life, that player mills that many cards. Amount determined at trigger time. Used by Mindcrank |
| `MillBottomOfTargetLibraryConditionalTokenEffect` | `(CardType conditionType, String tokenName, int tokenPower, int tokenToughness, CardColor tokenColor, List<CardSubtype> tokenSubtypes)` | target player puts bottom card of library into graveyard; if it matches conditionType, controller creates a creature token. Used by Cellar Door |
| `RevealUntilTypeMillAndBoostAttackerEffect` | `(Set<CardType> cardTypes, int powerBoostPerCard, int toughnessBoostPerCard)` | defending player reveals cards from top of library until a card matching one of the specified types is found; equipped creature gets +powerBoostPerCard/+toughnessBoostPerCard per card revealed; all revealed cards go to graveyard. Used on equipment ON_ATTACK triggers. Defending player derived as opponent. Used by Trepanation Blade |
| `ReplaceCombatDamageWithMillEffect` | `(PermanentPredicate attackerPredicate)` | static replacement: if a creature matching the predicate you control would deal combat damage to a player, instead that player mills that many cards. Checked in CombatDamageService.accumulatePlayerDamage(). Used by Undead Alchemist (Zombie predicate) |
| `ExileMilledCreatureAndCreateTokenEffect` | `(String tokenName, int tokenPower, int tokenToughness, CardColor tokenColor, List<CardSubtype> tokenSubtypes)` | triggered: whenever a creature card is put into an opponent's graveyard from their library, exile that card and create a creature token. Uses ON_OPPONENT_CREATURE_CARD_MILLED slot. Handled by MiscTriggerCollectorService. Used by Undead Alchemist |

## Exile

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `ExileTargetCreatureAndAllWithSameNameEffect` | `()` | exile target creature and all other creatures on the battlefield with the same name. Fizzles if target leaves. Used by Sever the Bloodline |
| `ExileTargetPermanentEffect` | `()` | exile target permanent (also handles multi-target via targetIds) |
| `ExilePermanentDamagedPlayerControlsEffect` | `(PermanentPredicate predicate)` | exile target permanent controlled by the damaged player (combat damage trigger); use inside MayEffect with ON_COMBAT_DAMAGE_TO_PLAYER |
| `ExileCardsFromGraveyardEffect` | `(int maxTargets, int lifeGain)` | exile up to N cards from graveyard, gain lifeGain per card |
| `ExileCreaturesFromGraveyardAndCreateTokensEffect` | `()` | exile creature cards from graveyard, create tokens for each |
| `ExileTopCardsRepeatOnDuplicateEffect` | `(int count)` | exile top N cards, repeat if duplicate names found |
| `ExileSelfAndReturnAtEndStepEffect` | `()` | exile this permanent, return it at beginning of next end step (Argent Sphinx-style) |
| `ExileTargetPermanentAndImprintEffect` | `()` | exile target permanent permanently and imprint the exiled card onto the source permanent; the card does NOT return when the source leaves (Exclusion Ritual-style) |
| `ExileTargetPermanentAndTrackWithSourceEffect` | `()` | exile target permanent and track the exiled card in `permanentExiledCards` with the source permanent's ID. Unlike imprint, used for effects that reference "cards exiled with [source]" (Karn Liberated -3). Resolved by `ExileResolutionService` |
| `TargetPlayerExilesFromHandEffect` | `(int amount)` | target player exiles N cards from their hand (their choice), tracked in `permanentExiledCards` with source permanent. `canTargetPlayer()=true`. Uses `EXILE_FROM_HAND_CHOICE` awaiting input. Resolved by `PlayerInteractionResolutionService`. Used by Karn Liberated +4 |
| `TargetPlayerExilesCardFromGraveyardEffect` | `(int lifeGainIfCreature)` | target player exiles a card from their graveyard (their choice). If the exiled card is a creature and `lifeGainIfCreature > 0`, the ability's controller gains that much life. `canTargetPlayer()=true`. Auto-exiles when graveyard has exactly 1 card. Resolved by `GraveyardReturnResolutionService`. Used by Graveyard Shovel |
| `ExileTargetPermanentAndReturnAtEndStepEffect` | `()` or `(boolean returnTapped)` | exile target permanent, return it at beginning of next end step under owner's control (Glimmerpoint Stag-style). When `returnTapped=true`, the permanent returns tapped (Mystifying Maze-style). No-arg constructor defaults to `false` |
| `ExileTargetPermanentUntilSourceLeavesEffect` | `()` | exile target permanent until source leaves the battlefield, then return it under owner's control (O-ring style). Tracked via `GameData.exileReturnOnPermanentLeave` map. Often wrapped in `MayEffect` for "you may" triggers |
| `ImprintDyingCreatureEffect` | `(UUID dyingCardId)` or `()` | exile a dying nontoken creature and imprint it on the source permanent; previously imprinted card is returned to its owner's graveyard. No-arg constructor (dyingCardId is null) used in card definition; dyingCardId populated at trigger time |
| `ExileFromHandToImprintEffect` | `(CardPredicate filter, String description)` | exiles a card matching the predicate from the controller's hand and imprints it on the source permanent. Description is used in the player prompt. Prototype Portal: `CardTypePredicate(ARTIFACT)`, Semblance Anvil: `CardNotPredicate(CardTypePredicate(LAND))` |
| `ChooseCardNameAndExileFromZonesEffect` | `(List<CardType> excludedTypes)` | Two-step interaction: (1) choose a card name (excluding given types), (2) present all matching cards from target player's hand, graveyard, and library for "any number" selection — player chooses 0 to N to exile. Library is always shuffled. Targets player. Uses `MULTI_ZONE_EXILE_CHOICE` awaiting input. Used by Memoricide, Cranial Extraction |
| `ExileTargetPlayerGraveyardEffect` | `()` | exile all cards from target player's graveyard. Targets player. Used by Nihil Spellbomb |
| `ExileNonBasicLandGraveyardAndSameNameFromLibraryEffect` | `()` | exile all non-basic-land cards from target player's graveyard, then search their library for all cards with the same name as any exiled card and exile them too. Shuffles library. Fully automatic (no player choice). Targets player. Used by Haunting Echoes |
| `ExileTargetCardFromGraveyardAndImprintOnSourceEffect` | `(CardType requiredType)` | exile target card of required type from any graveyard and track in `permanentExiledCards` for the source permanent (imprint). Targets graveyard (`canTargetGraveyard()=true`). Fizzles if target removed before resolution. Validated by `GraveyardTargetValidators`, resolved by `GraveyardReturnResolutionService`. Used by Myr Welder |
| `ExileTargetCardFromGraveyardEffect` | `(CardType requiredType)` | exile target card of required type from any graveyard (no imprint tracking). Targets graveyard (`canTargetGraveyard()=true`). Fizzles if target removed before resolution. Validated by `GraveyardTargetValidators`, resolved by `GraveyardReturnResolutionService`. Used by Conversion Chamber |
| `ExileTargetGraveyardCardAndSameNameFromZonesEffect` | `()` | target a card in any graveyard (not a basic land), then search its owner's graveyard, hand, and library for any number of cards with the same name and exile them. Owner shuffles. Targets graveyard (`canTargetGraveyard()=true`, `canTargetAnyGraveyard()=true`). Uses `MULTI_ZONE_EXILE_CHOICE` awaiting input. Validated by `GraveyardTargetValidators`, resolved by `PlayerInteractionResolutionService`. Used by Surgical Extraction |
| `EachPlayerExilesTopCardsToSourceEffect` | `(int count)` | each player exiles top N cards of their library, tracked as "exiled with" the source permanent via `GameData.permanentExiledCards`. Used by Knowledge Pool ETB |
| `DealDamageToEnchantedPlayerEffect` | `(int damage)` or `(int damage, UUID affectedPlayerId)` | deals N damage to the enchanted player (the player a curse is attached to). 1-arg constructor used in card definitions; `affectedPlayerId` baked in at trigger time by StepTriggerService. Used by Curse of the Pierced Heart (ENCHANTED_PLAYER_UPKEEP_TRIGGERED) |
| `ExileCardsFromOwnGraveyardEffect` | `(int count)` or `(int count, UUID affectedPlayerId)` | forces a player to exile N cards from their own graveyard (their choice). 1-arg constructor used in card definitions; `affectedPlayerId` populated at trigger/resolution time. Used by Curse of Oblivion (ENCHANTED_PLAYER_UPKEEP_TRIGGERED) |
| `ReturnTargetCardFromExileToHandEffect` | `(CardPredicate filter, boolean ownedOnly)` | return target exiled card matching filter to hand. When `ownedOnly=true`, restricts targeting to the controller's own exile zone (MTG "you own"). Targets exile (`canTargetExile()=true`). Fizzles if target removed before resolution. Validated by `ExileTargetValidators`, resolved by `ExileReturnResolutionService`. Used by Runic Repetition |
| `KnowledgePoolCastTriggerEffect` | `()` | Marker effect for `ON_ANY_PLAYER_CASTS_SPELL`. When a spell is cast from hand, creates a `KnowledgePoolExileAndCastEffect` triggered ability. Only fires for spells cast from hand (prevents infinite loops) |
| `KnowledgePoolExileAndCastEffect` | `(UUID originalSpellCardId, UUID knowledgePoolPermanentId, UUID castingPlayerId)` | Resolution effect for Knowledge Pool's cast trigger. Exiles the original spell from the stack to the KP pool, then presents the casting player with a choice of nonland "other" cards from the pool to cast without paying mana cost. `castingPlayerId` tracks "that player" (the caster) since the trigger is controlled by the KP controller per CR 603.3a. Uses `KNOWLEDGE_POOL_CAST_CHOICE` awaiting input |
| `MirrorOfFateEffect` | `()` | choose up to seven face-up exiled cards you own, exile all cards from your library, then put the chosen cards on top of your library. Uses `MIRROR_OF_FATE_CHOICE` awaiting input. Resolved by `ExileResolutionService` |
| `OmenMachineDrawStepEffect` | `()` | Triggered at each player's draw step (via `EACH_DRAW_TRIGGERED`). Exiles top card of active player's library. If land, puts it onto the battlefield. If non-land, casts it without paying mana cost if able (mandatory). Targeted spells prompt for target via `ExileCastSpellTarget`. If no valid targets, card stays in exile. Resolved by `ExileResolutionService` |
| `RevealRandomHandCardAndPlayEffect` | `()` | Reveals a random card from the target player's hand. If land, puts it onto the battlefield. If non-land, casts it without paying mana cost if able (mandatory). Targeted spells prompt for target via `HandCastSpellTarget`. If no valid targets, card stays in hand. Resolved by `PlayerInteractionResolutionService` |

## Tokens

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `CreateCreatureTokenEffect` | `(String tokenName, int power, int toughness, CardColor color, List<CardSubtype> subtypes, Set<Keyword> keywords, Set<CardType> additionalTypes)` or `(int amount, ...)` or multi-color: `(int amount, String tokenName, int power, int toughness, CardColor color, Set<CardColor> colors, List<CardSubtype> subtypes)` or `(String tokenName, int power, int toughness, CardColor color, Set<CardColor> colors, List<CardSubtype> subtypes)` or tapped-and-attacking: `(int amount, String tokenName, int power, int toughness, CardColor color, List<CardSubtype> subtypes, boolean tappedAndAttacking)` or tapped-attacking-with-keywords-exile: `(int amount, String tokenName, int power, int toughness, CardColor color, List<CardSubtype> subtypes, Set<Keyword> keywords, boolean tappedAndAttacking, boolean exileAtEndOfCombat)` or enters-tapped: `(int amount, String tokenName, int power, int toughness, CardColor color, List<CardSubtype> subtypes, Set<Keyword> keywords, Set<CardType> additionalTypes, boolean tapped)` or with-token-effects: `(int amount, String tokenName, int power, int toughness, CardColor color, List<CardSubtype> subtypes, Set<Keyword> keywords, Set<CardType> additionalTypes, Map<EffectSlot, CardEffect> tokenEffects)` | create N creature tokens. `color` is primary display color. `colors` (Set&lt;CardColor&gt;, nullable) is full color identity for multi-color tokens. Multi-color constructors default keywords/additionalTypes to empty sets. When `tappedAndAttacking=true`, tokens enter the battlefield tapped and attacking (CR 508.8) — used for "whenever ~ attacks, create tokens tapped and attacking" abilities (e.g. Hero of Bladehold). When `tapped=true`, tokens enter the battlefield tapped but not attacking — used for cards like Army of the Damned. When `exileAtEndOfCombat=true`, tokens are exiled at end of combat step — used for temporary attack tokens (e.g. Geist of Saint Traft). When `tokenEffects` is non-empty, each entry's effect is registered on the token card at the given slot, giving the token its own triggered abilities (e.g. Mitotic Slime's cascading death triggers) |
| `CreateXCreatureTokenEffect` | `(String tokenName, int power, int toughness, CardColor color, List<CardSubtype> subtypes, Set<Keyword> keywords, Set<CardType> additionalTypes)` or `(String tokenName, int power, int toughness, CardColor color, List<CardSubtype> subtypes)` | create X creature tokens where X comes from the spell's X value on the stack entry. Used for X-cost token spells like White Sun's Zenith |
| `CreateTokensPerOwnCreatureDeathsThisTurnEffect` | `(String tokenName, int power, int toughness, CardColor color, List<CardSubtype> subtypes, Set<Keyword> keywords, Set<CardType> additionalTypes)` or `(String tokenName, int power, int toughness, CardColor color, List<CardSubtype> subtypes)` | create one creature token for each creature put into the controller's graveyard from the battlefield this turn. Uses `creatureDeathCountThisTurn` per-player count. Used by Fresh Meat |
| `CreateTokensPerCreatureCardInGraveyardEffect` | `(String tokenName, int power, int toughness, CardColor color, List<CardSubtype> subtypes, Set<Keyword> keywords, Set<CardType> additionalTypes, boolean tappedAndAttacking)` or `(String tokenName, int power, int toughness, CardColor color, List<CardSubtype> subtypes, boolean tappedAndAttacking)` | create one creature token for each creature card in the controller's graveyard. When `tappedAndAttacking=true`, tokens enter tapped and attacking. Used by Kessig Cagebreakers |
| `CreateTokenCopyOfImprintedCardEffect` | `(boolean grantHaste, boolean exileAtEndStep)` | create a token that is a copy of the card imprinted on the source permanent. When `grantHaste=true`, the token gains haste. When `exileAtEndStep=true`, the token is exiled at the beginning of the next end step. No-arg constructor `()` defaults to `(true, true)` for Mimic Vat. Use `(false, false)` for permanent tokens like Prototype Portal |
| `CreateTokenCopyOfExiledCostCardEffect` | `()` | create a token that is a copy of the card exiled as part of an `ExileCardFromGraveyardCost` with `imprintOnSource=true`. The exiled card is tracked via the source permanent's imprinted card reference, set during cost payment. Used by Back from the Brink |
| `CreateTokenCopyOfSourceEffect` | `()` | create a token that is a copy of the source permanent (the permanent with this ability). Copies all copiable characteristics per CR 707.2 including effects and activated abilities |
| `CreateTokenCopyOfTargetPermanentEffect` | `()` | create a token that is a copy of the permanent referenced by `targetId` on the stack entry. Used for triggered abilities where the permanent to copy is determined at trigger time (e.g. Mirrorworks). Copies all copiable characteristics per CR 707.2 |
| `CreateTokensEqualToChargeCountersOnSourceEffect` | `(String tokenName, int power, int toughness, CardColor color, List<CardSubtype> subtypes, Set<Keyword> keywords, Set<CardType> additionalTypes)` | create creature tokens equal to the number of charge counters on the source permanent. Count is snapshotted as xValue before sacrifice (see ActivatedAbilityExecutionService). Used by Shrine of Loyal Legions |
| `CreateTokensEqualToControlledCreatureCountEffect` | `(String tokenName, int power, int toughness, CardColor color, List<CardSubtype> subtypes, Set<Keyword> keywords, Set<CardType> additionalTypes)` | create creature tokens equal to the number of creatures the controller controls. Count is determined at resolution time. Used for Chancellor of the Forge ETB |
| `CreateTokensPerControlledCreatureSubtypeEffect` | `(CardSubtype subtype, int divisor, String tokenName, int power, int toughness, CardColor color, List<CardSubtype> subtypes, Set<Keyword> keywords, Set<CardType> additionalTypes)` | create creature tokens equal to the number of creatures with the specified subtype the controller controls, divided by divisor (rounded down). Count is determined at resolution time. Used for Endless Ranks of the Dead (divisor=2) |
| `CreateTokensPerControlledLandSubtypeEffect` | `(CardSubtype landSubtype, String tokenName, int power, int toughness, CardColor color, List<CardSubtype> subtypes, Set<Keyword> keywords, Set<CardType> additionalTypes)` | create creature tokens equal to the number of lands with the specified subtype the controller controls. Count is determined at resolution time. Used for Howl of the Night Pack |
| `CreateTokenPerEquipmentOnSourceEffect` | `(String tokenName, int power, int toughness, CardColor color, List<CardSubtype> subtypes, Set<Keyword> keywords, Set<CardType> additionalTypes)` | create creature tokens equal to the number of Equipment attached to the source permanent. Requires `sourcePermanentId` on StackEntry (automatically provided by UPKEEP_TRIGGERED). Used by Kemba, Kha Regent |
| `CreateTokenPerOpponentPoisonCounterEffect` | `(String tokenName, int power, int toughness, CardColor color, List<CardSubtype> subtypes, Set<Keyword> keywords, Set<CardType> additionalTypes)` | create creature tokens equal to the total number of poison counters on opponents. Used by Phyrexian Swarmlord |
| `CreateLifeTotalAvatarTokenEffect` | `(String tokenName, CardColor color, List<CardSubtype> subtypes)` | create a creature token with P/T = controller's life total (CDA). Token gets `PowerToughnessEqualToControllerLifeTotalEffect` as a static effect so P/T updates dynamically. Used by Ajani Goldmane |
| `LivingWeaponEffect` | `()` | living weapon ETB: create 0/0 black Phyrexian Germ token and attach this equipment to it (resolved by PermanentControlResolutionService) |
| `PutSlimeCounterAndCreateOozeTokenEffect` | `()` | composite: puts a slime counter on the source permanent, then creates a 0/0 green Ooze creature token with a CDA linking its P/T to the number of slime counters on the source. The token gets a `BoostSelfBySlimeCountersOnLinkedPermanentEffect` as a static effect. Place in `ON_ALLY_NONTOKEN_CREATURE_DIES` slot. Used by Gutter Grime |

## Life

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `GainLifeEffect` | `(int amount)` | gain N life |
| `PayXManaGainXLifeEffect` | `()` | on resolution, pays all available mana from the controller's pool as X and gains X life. Used for "you may pay {X}. If you do, you gain X life" triggered abilities where payment happens during resolution (e.g. Vigil for the Lost) |
| `GainLifeForEachSubtypeOnBattlefieldEffect` | `(CardSubtype subtype)` | gain 1 life per permanent with given subtype on the battlefield (all players) |
| `GainLifePerControlledCreatureEffect` | `()` | gain 1 life per creature you control |
| `GainLifePerControlledMatchingPermanentEffect` | `(List<PermanentPredicate> predicates)` | gain life equal to sum of permanents you control matching each predicate; controller-only version of GainLifePerMatchingPermanentOnBattlefieldEffect |
| `GainLifePerCreatureOnBattlefieldEffect` | `()` | gain 1 life per creature on the battlefield (all players) |
| `GainLifePerMatchingPermanentOnBattlefieldEffect` | `(List<PermanentPredicate> predicates)` | gain life equal to sum of permanents matching each predicate on the battlefield (all players); a permanent matching multiple predicates is counted once per match (e.g. artifact creature counts twice with creature+artifact predicates) |
| `GainLifePerCardsInHandEffect` | `()` | gain 1 life per card in controller's hand (upkeep trigger) |
| `GainLifePerCreatureCardInGraveyardEffect` | `(int lifePerCreature)` | gain N life per creature card in controller's graveyard |
| `GainLifePerGraveyardCardEffect` | `()` | gain life equal to cards in graveyard |
| `GainLifeEqualToTargetToughnessEffect` | `()` | gain life equal to target creature's toughness |
| `GainLifeEqualToToughnessEffect` | `()` | gain life equal to own toughness (self, e.g. dies trigger) |
| `GainLifeEqualToDamageDealtEffect` | `()` | gain life equal to damage dealt (lifelink-style, static) |
| `GainLifeEqualToChargeCountersOnSourceEffect` | `()` | gain life equal to number of charge counters on source (activated ability sacrifice effect) |
| `GainLifeEqualToXValueEffect` | `()` | gain life equal to xValue on stack entry (use with `SacrificeCreatureCost(trackToughness/power)` or other xValue-setting costs) |
| `TargetPlayerGainsLifeEffect` | `(int amount)` | target player gains N life |
| `EachTargetPlayerGainsLifeEffect` | `(int amount)` | each targeted player gains N life (multi-target, reads from `targetIds`). Pair with `setMinTargets(0)` and `setMaxTargets(99)` for "any number of target players". Used by Hunters' Feast |
| `DoubleTargetPlayerLifeEffect` | `()` | double target player's life total |
| `ExchangeTargetPlayersLifeTotalsEffect` | `()` | two target players exchange life totals (multi-target player ability, reads targets from `targetIds`). CR 118.7: if either player's life can't change, the exchange doesn't occur |
| `ExchangeLifeTotalWithToughnessEffect` | `()` | exchange controller's life total with source creature's toughness (CR 701.10e). Uses `sourcePermanentId` to find the creature. Player's life becomes creature's effective toughness; creature's base toughness is permanently set to former life total (layer 7b). Handles can't-change-life, can't-gain-life checks |
| `LoseLifeEffect` | `(int amount)` | lose N life |
| `EachOpponentLosesLifeEffect` | `(int amount)` | each opponent loses N life |
| `EachOpponentLosesLifeAndControllerGainsLifeLostEffect` | `(int amount)` | each opponent loses N life, controller gains total life lost |
| `EachOpponentLosesXLifeAndControllerGainsLifeLostEffect` | `()` | each opponent loses X life, controller gains total life lost |
| `TargetPlayerLosesLifeEffect` | `(int amount)` | target player loses N life |
| `TargetPlayerLosesLifeEqualToPowerEffect` | `()` | ON_DEATH marker: target player loses life equal to dying creature's last-known power. DeathTriggerService bakes the power into a TargetPlayerLosesLifeEffect at trigger time |
| `TargetPlayerLosesLifeEqualToLifeGainedEffect` | `()` | ON_CONTROLLER_GAINS_LIFE marker: target opponent loses life equal to life gained. MiscTriggerCollectorService bakes the amount into a TargetPlayerLosesLifeEffect at trigger time |
| `TargetPlayerLosesLifeAndControllerGainsLifeEffect` | `(int lifeLoss, int lifeGain)` | drain: target loses N, you gain M |
| `DrainLifePerControlledPermanentEffect` | `(PermanentPredicate filter, int multiplier)` | target player loses X life, controller gains X life, where X = multiplier × matching permanents controlled (e.g. Tezzeret -4: twice artifacts) |
| `EnchantedCreatureControllerLosesLifeEffect` | `(int amount, UUID affectedPlayerId)` | enchanted creature's controller loses N life (trigger) |
| `EachPlayerLosesLifeEffect` | `(int amount)` | each player (including controller) loses N life |
| `EachPlayerLosesLifePerCreatureControlledEffect` | `(int lifePerCreature)` | each player loses N life per creature they control |
| `LoseLifeUnlessDiscardEffect` | `(int lifeLoss)` | target player loses N life unless they discard a card. Punisher choice made by the affected player. Place in `ON_OPPONENT_CASTS_SPELL` slot. Resolved via `PlayerInteractionResolutionService` → may ability prompt → discard choice or life loss |
| `LoseLifeUnlessPaysEffect` | `(int lifeLoss, int payAmount, CardPredicate spellFilter)` | target player loses N life unless they pay {M}. Optional `spellFilter` restricts which spells trigger it (null = any). Place in `ON_OPPONENT_CASTS_SPELL` slot. If player can't pay, auto-applies life loss. Otherwise prompts via may ability. Convenience ctor: `(int lifeLoss, int payAmount)` sets filter to null |

## Poison counters

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `GiveControllerPoisonCountersEffect` | `(int amount)` | give the controller of this effect N poison counters. Used for self-poisoning triggers like Phyrexian Vatmother's upkeep trigger |
| `GiveEachPlayerPoisonCountersEffect` | `(int amount)` | give each player N poison counters (including controller). Used for ETB effects like Ichor Rats |
| `GiveEnchantedPermanentControllerPoisonCountersEffect` | `(int amount)` or `(int amount, UUID affectedPlayerId)` | give N poison counter(s) to the controller of the enchanted permanent. Used on `ON_ENCHANTED_PERMANENT_TAPPED` slot. `affectedPlayerId` is null in card definition; baked in at trigger time by `TriggerCollectionService.checkEnchantedPermanentTapTriggers` |
| `GiveTargetPlayerPoisonCountersEffect` | `(int amount)` or `(int amount, CardPredicate spellFilter)` | give target player N poison counters. With `spellFilter`, doubles as trigger descriptor for `ON_CONTROLLER_CASTS_SPELL`: fires when controller casts a spell matching the predicate. Resolves into a copy with `spellFilter == null` |
| `GiveControllerPoisonCountersOnTargetDeathThisTurnEffect` | `(int amount)` | delayed trigger: registers the target creature so that when it dies this turn, its controller gets N poison counters. Reads target from stack entry's `targetId`. Tracking in `GameData.creatureGivingControllerPoisonOnDeathThisTurn`, cleared at end of turn |

## Win / lose game

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `TargetPlayerLosesGameEffect` | `(UUID playerId)` | target player loses the game |
| `LoseGameIfNotCastFromHandEffect` | `()` | lose the game if not cast from hand (ETB check) |
| `WinGameIfCreaturesInGraveyardEffect` | `(int threshold)` | win if N+ creature cards in graveyard |
| `WinGameOnEmptyLibraryDrawEffect` | `()` | static: if controller would draw from empty library, they win the game instead (Laboratory Maniac). Checked in `DrawService.performDrawCard()` |
| `CantHaveCountersEffect` | `()` | this permanent can't have counters put on it (static) |
| `CantHaveMinusOneMinusOneCountersEffect` | `()` | this creature can't have -1/-1 counters put on it (granted via GrantEffectEffect). Checked by `GameQueryService.cantHaveMinusOneMinusOneCounters()` |
| `PlayerCantGetPoisonCountersEffect` | `()` | controller can't get poison counters (static on source permanent). Checked at all poison counter application points |
| `RemoveKeywordEffect` | `(Keyword keyword, GrantScope scope)` or `(Keyword keyword, GrantScope scope, PermanentPredicate filter)` | static: creatures in scope lose the specified keyword. Added to `removedKeywords` in static bonus computation. Also works as one-shot in activated abilities with SELF/TARGET scope: adds keyword to `Permanent.removedKeywords` (cleared at end of turn) |
| `CantLoseGameEffect` | `()` | you can't lose and opponents can't win (static) |
| `CantLoseGameFromLifeEffect` | `()` | you don't lose the game for having 0 or less life, but can still lose from poison or other effects (static) |
| `DamageDealtAsInfectBelowZeroLifeEffect` | `()` | as long as you have 0 or less life, all damage dealt to you is dealt as though its source had infect (static) |
| `LifeTotalCantChangeEffect` | `()` | controller's life total can't change (static) |
| `PlayersCantGainLifeEffect` | `()` | no player can gain life (static, global — checked on any battlefield) |
| `DamageCantBePreventedEffect` | `()` | damage can't be prevented by any means (static, global — bypasses shields, protection damage, color prevention) |

## Creature pump / boost

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `SetBasePowerToughnessUntilEndOfTurnEffect` | `(int power, int toughness)` | set target creature's base power and toughness until end of turn (modifiers still apply on top) |
| `SwitchPowerToughnessEffect` | `()` | switch target creature's power and toughness until end of turn |
| `BoostTargetCreatureEffect` | `(int powerBoost, int toughnessBoost)` | target creature gets +X/+Y until end of turn |
| `BoostTargetCreaturePerControlledPermanentEffect` | `(int powerPerPermanent, int toughnessPerPermanent, PermanentPredicate filter)` | target creature gets +N/+N per controlled permanent matching filter until end of turn |
| `BoostSelfEffect` | `(int powerBoost, int toughnessBoost)` | this creature gets +X/+Y until end of turn |
| `BoostAllOwnCreaturesEffect` | `(int powerBoost, int toughnessBoost)` or `(int powerBoost, int toughnessBoost, PermanentPredicate filter)` | all your creatures get +X/+Y until end of turn (one-shot). Optional predicate filter |
| `BoostAllOwnCreaturesByGreatestPowerEffect` | `()` | all your creatures get +X/+X until end of turn, where X is the greatest power among creatures you control at resolution time. Used by Overwhelming Stampede |
| `BoostAllOwnCreaturesByCreatureCardsInGraveyardEffect` | `()` | all your creatures get +X/+X until end of turn, where X is the number of creature cards in your graveyard. Used by Garruk, the Veil-Cursed |
| `BoostAllCreaturesEffect` | `(int powerBoost, int toughnessBoost)` or `(int powerBoost, int toughnessBoost, PermanentPredicate filter)` | ALL creatures (all players) get +X/+Y until end of turn (one-shot). Optional predicate filter. Unlike `BoostAllOwnCreaturesEffect`, iterates over every player's battlefield |
| `StaticBoostEffect` | `(int powerBoost, int toughnessBoost, Set<Keyword> grantedKeywords, GrantScope scope, PermanentPredicate filter)` | unified static boost: +X/+Y and keywords with predicate-based filtering. Scope: `OWN_CREATURES`, `OPPONENT_CREATURES`, `ALL_CREATURES`, `ENCHANTED_CREATURE`, `EQUIPPED_CREATURE`, `ENCHANTED_PLAYER_CREATURES`. With `GrantScope.ENCHANTED_CREATURE` it works as an aura boost (enchanted creature gets +X/+Y), with `GrantScope.EQUIPPED_CREATURE` it works as an equipment boost (equipped creature gets +X/+Y), and with `GrantScope.ENCHANTED_PLAYER_CREATURES` it affects creatures controlled by the enchanted player (for Curses). Filter: optional `PermanentPredicate` (color, subtype, not, etc). Convenience constructors: `(p, t, scope)`, `(p, t, scope, filter)`, `(p, t, keywords, scope)` |
| `BoostCreaturesOfChosenColorEffect` | `(int powerBoost, int toughnessBoost)` | static: creatures you control of the source permanent's chosen color get +X/+Y. The chosen color is stored on the permanent at runtime via `Permanent.getChosenColor()`. Used by Caged Sun |
| `BoostTargetCreatureXEffect` | `(int powerMultiplier, int toughnessMultiplier)` | target creature gets +(multiplier*X)/+(multiplier*X) until end of turn, where X is mana paid |
| `BoostAllCreaturesXEffect` | `(int powerMultiplier, int toughnessMultiplier)` or `(int powerMultiplier, int toughnessMultiplier, PermanentPredicate filter)` | all creatures get +X/+X where X is mana paid. Optional `PermanentPredicate filter` to restrict which creatures are affected |
| `BoostCreaturePerCardsInAllGraveyardsEffect` | `(CardPredicate filter, GrantScope scope)` | attached creature gets +X/+X where X = cards in all graveyards matching filter. Scope: `ENCHANTED_CREATURE` or `EQUIPPED_CREATURE` |
| `BoostCreaturePerCardsInControllerGraveyardEffect` | `(CardPredicate filter, int powerPerCard, int toughnessPerCard, GrantScope scope)` | attached creature gets +X/+Y per card in controller's graveyard matching filter. Scope: `ENCHANTED_CREATURE` or `EQUIPPED_CREATURE`. Used by Runechanter's Pike (instants+sorceries, power only) |
| `BoostCreaturePerControlledCardTypeEffect` | `(CardType cardType, int powerPerMatch, int toughnessPerMatch, GrantScope scope)` | attached creature gets +X/+Y per controlled permanent with card type (static). Scope: `ENCHANTED_CREATURE` or `EQUIPPED_CREATURE`. Used by Blackblade Reforged (LAND) |
| `BoostCreaturePerControlledSubtypeEffect` | `(CardSubtype subtype, int powerPerSubtype, int toughnessPerSubtype, GrantScope scope)` | attached creature gets +X/+Y per controlled permanent with subtype (static). Scope: `ENCHANTED_CREATURE` or `EQUIPPED_CREATURE` |
| `BoostCreaturePerMatchingLandNameEffect` | `(int powerPerMatch, int toughnessPerMatch, GrantScope scope)` | attached creature gets +X/+Y per land on the battlefield with the same name as the imprinted card. Scope: `ENCHANTED_CREATURE` or `EQUIPPED_CREATURE` |
| `BoostByOtherCreaturesWithSameNameEffect` | `(int powerPerCreature, int toughnessPerCreature)` | +X/+Y per other creature with same name (static) |
| `BoostBySharedCreatureTypeEffect` | `()` | +1/+1 for each other creature sharing a creature type (static) |
| `BoostFirstTargetCreatureEffect` | `(int powerBoost, int toughnessBoost)` | first target creature in multi-target spell gets +X/+Y until end of turn |
| `BoostSecondTargetCreatureEffect` | `(int powerBoost, int toughnessBoost)` | second target creature in multi-target spell gets +X/+Y until end of turn |
| `BoostSelfPerEnchantmentOnBattlefieldEffect` | `(int powerPerEnchantment, int toughnessPerEnchantment)` | +X/+Y per enchantment on battlefield (static) |
| `BoostSelfPerOpponentPermanentEffect` | `(int powerPerPermanent, int toughnessPerPermanent, PermanentPredicate filter)` | +X/+Y per permanent opponents control matching the filter (static, selfOnly). Use `PermanentIsCreaturePredicate` for creatures, etc. |
| `BoostSelfPerOpponentPoisonCounterEffect` | `(int powerPerCounter, int toughnessPerCounter)` | +X/+Y per poison counter on opponents (static). Counts total poison counters across all opponents, excludes controller's own counters |
| `BoostSelfByImprintedCreaturePTEffect` | `()` | +X/+Y where X is the imprinted creature card's power and Y is its toughness (static, selfOnly). Used by Phyrexian Ingester |
| `BoostSelfPerBlockingCreatureEffect` | `(int powerPerBlockingCreature, int toughnessPerBlockingCreature)` | +X/+Y for each creature blocking this (combat trigger) |
| `BoostSelfPerControlledPermanentEffect` | `(int powerPerPermanent, int toughnessPerPermanent, PermanentPredicate filter)` | +X/+Y for each permanent you control matching the filter (activated ability) |
| `BoostSelfPerControlledSubtypeEffect` | `(CardSubtype subtype, int powerPerPermanent, int toughnessPerPermanent)` | +X/+Y for each permanent with subtype you control (static, selfOnly). Used by Earth Servant |
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
| `PowerToughnessEqualToCardsInAllGraveyardsEffect` | `(CardPredicate filter)` | P/T = number of cards matching filter in all graveyards (static). E.g. `new CardTypePredicate(CardType.ARTIFACT)` for artifact cards |
| `PowerToughnessEqualToCardsInControllerGraveyardEffect` | `(CardPredicate filter)` | P/T = number of cards matching filter in controller's graveyard only (static). E.g. `new CardTypePredicate(CardType.CREATURE)` for creature cards |
| `PowerToughnessEqualToCardsInHandEffect` | `()` | P/T = number of cards in controller's hand (static) |
| `PowerToughnessEqualToControllerLifeTotalEffect` | `()` | P/T = controller's life total (static CDA, e.g. Ajani Goldmane's Avatar token, Serra Avatar) |
| `BoostSelfBySlimeCountersOnLinkedPermanentEffect` | `(UUID linkedPermanentId)` | CDA static self-effect placed on tokens: P/T equals the number of slime counters on the linked permanent. If the linked permanent has left the battlefield, P/T is 0/0 (token dies to SBA). Created automatically by `PutSlimeCounterAndCreateOozeTokenEffect`. Used by Gutter Grime's Ooze tokens |
| `PutCountersOnSourceEffect` | `(int powerModifier, int toughnessModifier, int amount)` | put N counters on this creature (e.g. `(1,1,1)` for +1/+1, `(-1,-1,2)` for two -1/-1) |
| `PutCountersOnDamageDealerEffect` | `(int powerModifier, int toughnessModifier, int amount, PermanentPredicate predicate)` | ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER slot: when a creature you control matching predicate deals combat damage to a player, put N counters on that creature. Null predicate means any creature. Resolved via PutCountersOnSourceEffect. Used by Rakish Heir (Vampire predicate) |
| `PutPlusOnePlusOneCounterOnEachControlledPermanentEffect` | `(PermanentPredicate predicate)` | put a +1/+1 counter on each permanent you control matching the predicate. Use `PermanentAllOfPredicate` to combine filters (e.g. artifact + creature). Supports source-aware predicates like `PermanentNotPredicate(PermanentIsSourceCardPredicate())` for "each other creature" patterns |
| `PutPlusOnePlusOneCounterOnEachOwnCreatureEffect` | `()` | put a +1/+1 counter on each creature you control |
| `PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect` | `(Set<CardColor> triggerColors, int amount, boolean onlyOwnSpells)` | put +1/+1 counters when spell of matching color is cast. Use `ON_CONTROLLER_CASTS_SPELL` with `onlyOwnSpells=true` for "whenever you cast" cards; use `ON_ANY_PLAYER_CASTS_SPELL` with `onlyOwnSpells=false` for "whenever a player casts" cards; use `ON_OPPONENT_CASTS_SPELL` with `onlyOwnSpells=false` for "whenever an opponent casts" cards (wrap in MayEffect for "you may" triggers like Mold Adder) |
| `PutMinusOneMinusOneCounterOnEachAttackingCreatureEffect` | `()` | put a -1/-1 counter on each attacking creature (all players' attacking creatures) |
| `PutMinusOneMinusOneCounterOnEachOtherCreatureEffect` | `()` | put a -1/-1 counter on each other creature (all players' creatures except the source permanent) |
| `PutMinusOneMinusOneCounterOnEnchantedCreatureEffect` | `(int count)` / `()` | put N -1/-1 counters on enchanted creature (default 1). Use with UPKEEP_TRIGGERED for auras like Glistening Oil |
| `PutPlusOnePlusOneCounterOnEnchantedCreatureEffect` | `(int count)` / `()` | put N +1/+1 counters on enchanted creature (default 1). Use with UPKEEP_TRIGGERED for auras like Primal Cocoon |
| `PutPlusOnePlusOneCounterOnTargetCreatureEffect` | `(int count)` | put N +1/+1 counters on target creature |
| `PutPlusOnePlusOneCounterOnFirstTargetIfSupertypeEffect` | `(CardSupertype supertype, int count)` | put N +1/+1 counters on the first target only if it has the specified supertype (e.g. LEGENDARY). For multi-target spells like Ancient Animus |
| `EnterWithXChargeCountersEffect` | `()` | enters battlefield with X charge counters (replacement effect, reads X from spell cast) |
| `EnterWithXPlusOnePlusOneCountersEffect` | `()` | enters battlefield with X +1/+1 counters (replacement effect, reads X from spell cast). Use for creatures like Protean Hydra |
| `EnterWithPlusOnePlusOneCountersIfKickedEffect` | `(int count)` | enters battlefield with N +1/+1 counters if the spell was kicked (replacement effect). Use with `KickerEffect` on STATIC slot. E.g. Academy Drake |
| `KickerEffect` | `(String cost)` | STATIC effect declaring that a spell has kicker with the given mana cost (e.g. "{4}"). SpellCastingService reads this to pay the optional additional cost. Combine with kicker-conditional effects on ON_ENTER_BATTLEFIELD or SPELL slot |
| `KickedConditionalEffect` | `(CardEffect wrapped)` | conditional wrapper that resolves its inner effect only when the spell was kicked (MTG Rule 702.32c). Place on SPELL slot alongside other spell effects. E.g. Blink of an Eye uses `KickedConditionalEffect(new DrawCardEffect())` |
| `EnterWithFixedChargeCountersEffect` | `(int count)` | enters battlefield with N charge counters (replacement effect, fixed count) |
| `EnterWithFixedWishCountersEffect` | `(int count)` | enters battlefield with N wish counters (replacement effect, fixed count). Used by Djinn of Wishes |
| `EnterWithPlusOnePlusOneCountersPerSubtypeEffect` | `(CardSubtype subtype, boolean includeGraveyard)` | ETB replacement effect: enters with a +1/+1 counter for each other permanent with the specified subtype you control, plus (if `includeGraveyard=true`) each card with the subtype in your graveyard. Handled in `BattlefieldEntryService.putPermanentOntoBattlefield()` so it works for all entry paths (cast, reanimate, etc.). Checks Changeling on both battlefield permanents and graveyard cards. Used by Unbreathing Horde (ZOMBIE, true) |
| `GraveyardEnterWithAdditionalCountersEffect` | `(CardSubtype subtype, int count)` | graveyard static ability (replacement effect): while this card is in your graveyard, creatures with the specified subtype you control enter with N additional +1/+1 counters. Uses CR 614.12 lookahead via `GameQueryService.permanentWouldHaveSubtype()` to account for static subtype-granting effects (e.g. Xenograft). Simultaneously entering permanents are excluded per CR 614.12. Checked in BattlefieldEntryService.putPermanentOntoBattlefield(). Used by Dearly Departed |
| `PutMinusOneMinusOneCounterOnEachCreatureTargetPlayerControlsEffect` | `()` | put a -1/-1 counter on each creature target player controls (targets player) |
| `PutChargeCounterOnSelfEffect` | `()` | put a charge counter on this permanent (self-target, used as activated ability effect) |
| `PutCounterOnSelfEffect` | `(CounterType counterType)` | put a counter of the specified type on this permanent (self-target). Supports CHARGE, HATCHLING, STUDY, WISH, PLUS_ONE_PLUS_ONE, MINUS_ONE_MINUS_ONE. Used by Grimoire of the Dead (STUDY) |
| `PutCounterOnSelfThenTransformIfThresholdEffect` | `(CounterType counterType, int threshold)` | put a counter of the specified type on this permanent, then if the count reaches the threshold, remove all counters of that type and transform. Used by Ludevic's Test Subject (HATCHLING, 5) |
| `PutChargeCounterOnTargetPermanentEffect` | `()` | put a charge counter on target permanent (targets permanent, use with PermanentPredicateTargetFilter to restrict to artifacts etc.) |
| `PutPhylacteryCounterOnTargetPermanentEffect` | `()` | put a phylactery counter on an artifact you control as an "as enters" replacement effect. Does NOT target (shroud/hexproof don't prevent it). Counter is placed directly when creature enters, not via stack. Artifact choice is validated at resolution: must be an artifact controlled by the caster. If no valid artifact is chosen, ability does nothing. Used by Phylactery Lich |
| `StateTriggerEffect` | `(StateTriggerPredicate, List<CardEffect>, String)` | state-triggered ability (MTG rule 603.8). When predicate condition is met after SBAs, pushes effects onto the stack as a triggered ability. Won't retrigger while the ability is on the stack. Place in `STATE_TRIGGERED` slot. Used by Phylactery Lich |
| `RemoveChargeCountersFromTargetPermanentEffect` | `(int maxCount)` | remove up to N charge counters from target permanent (targets permanent, use with PermanentPredicateTargetFilter to restrict targets) |
| `RemoveCountersFromTargetAndBoostSelfEffect` | `()` | remove up to X counters of any type from target permanent (X from xValue), then boost source +1/+0 per counter removed until end of turn. Removes in order: +1/+1, charge, phylactery, loyalty, -1/-1, awakening. Used by Hex Parasite |
| `PutMinusOneMinusOneCounterOnTargetCreatureEffect` | `(int count)` / `()` / `(int count, boolean regenerateIfSurvives)` | put count -1/-1 counters on target creature (targets permanent). No-arg defaults to 1. With `regenerateIfSurvives=true`, regenerates the creature after placing counters if its toughness is 1 or greater (Gore Vassal) |
| `PutXMinusOneMinusOneCountersOnEachCreatureEffect` | `()` | put X -1/-1 counters on each creature (all players' creatures), where X comes from the spell's X value |
| `ProliferateEffect` | `()` | proliferate: choose any number of permanents with counters, add one of each counter type already there |
| `PutAwakeningCountersOnTargetLandsEffect` | `()` | combat damage trigger: choose any number of lands you control, put an awakening counter on each. Lands with awakening counters are 8/8 green Elemental creatures (permanent). Place in `ON_COMBAT_DAMAGE_TO_PLAYER` slot. Handled inline in CombatService via multi-permanent choice |

## Keywords / abilities

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `GrantKeywordEffect` | `(Keyword keyword, GrantScope scope)` or `(Keyword keyword, GrantScope scope, PermanentPredicate filter)` | grant keyword. Scope: `SELF`, `TARGET`, `ENCHANTED_CREATURE`, `EQUIPPED_CREATURE`, `OWN_TAPPED_CREATURES`, `OWN_CREATURES`, `ALL_CREATURES`, `ENCHANTED_PLAYER_CREATURES`. Optional predicate filter for conditional grants |
| `GrantKeywordToTargetIfSupertypeEffect` | `(Keyword keyword, CardSupertype supertype)` | grant keyword to targeted permanent until end of turn, but only if it has the specified supertype (e.g. LEGENDARY). Used by Blessing of Belzenlok |
| `GrantFlashToCardTypeEffect` | `(CardType cardType)` | controller may cast spells of the given card type as though they had flash. When `cardType` is `null`, grants flash to all spell types. Checked in `GameBroadcastService.hasFlashGrantForCard()`. Used by Shimmer Myr (ARTIFACT), Leyline of Anticipation (null = all types) |
| `EquippedConditionalEffect` | `(CardEffect wrapped)` | Wraps any effect with "as long as equipped" condition. Analogous to `MetalcraftConditionalEffect`. For static effects: wraps `GrantKeywordEffect`, `StaticBoostEffect`, or `ProtectionFromColorsEffect`, applied only while at least one Equipment is attached (selfOnly handler). Used by Sunspear Shikari |
| `GrantChosenKeywordToTargetEffect` | `(List<Keyword> options)` | on resolution, prompts controller to choose one keyword from `options`, then grants it to target permanent until end of turn. Uses COLOR_CHOICE wire protocol with `KeywordGrantChoice` context. Used by Golem Artisan |
| `GrantActivatedAbilityEffect` | `(ActivatedAbility ability, GrantScope scope, PermanentPredicate filter)` or `(ActivatedAbility ability, GrantScope scope)` | grant activated ability to permanents matching scope + filter. Supported scopes: `OWN_PERMANENTS`, `ENCHANTED_CREATURE`, `EQUIPPED_CREATURE`, `OWN_TAPPED_CREATURES`, `OWN_CREATURES`, `ALL_CREATURES`, and other creature scopes. Replaces the old `GrantActivatedAbilityToEnchantedCreatureEffect` — use `GrantScope.ENCHANTED_CREATURE` instead |
| `GainActivatedAbilitiesOfCreatureCardsInAllGraveyardsEffect` | `()` | this creature has all activated abilities of all creature cards in all graveyards (static, selfOnly). Used by Necrotic Ooze |
| `GainActivatedAbilitiesOfExiledCardsEffect` | `()` | this permanent has all activated abilities of all cards exiled with it via `permanentExiledCards` (static, selfOnly). Used by Myr Welder |
| `GrantAdditionalBlockEffect` | `(int additionalBlocks)` | can block N additional creatures |
| `GrantAdditionalBlockPerEquipmentEffect` | `()` | can block an additional creature for each Equipment attached (static, self-only). Used by Kemba's Legion |
| `RegenerateEffect` | `()` or `(boolean targetsPermanent)` | regenerate self (default) or target creature when `targetsPermanent=true` |
| `RegenerateAllOwnCreaturesEffect` | `()` or `(PermanentPredicate filter)` | regenerate all creatures you control matching filter (or all if null). Used by Full Moon's Rise |
| `GrantCardTypeEffect` | `(CardType cardType, GrantScope scope)` | grant a card type to permanents matching scope (e.g. "is an artifact in addition to its other types"). Flows through static bonus system and updates `isArtifact(GameData, Permanent)` / metalcraft / targeting. Scope: `EQUIPPED_CREATURE`, `ENCHANTED_CREATURE`, etc. Used by Silverskin Armor |
| `GrantColorEffect` | `(CardColor color, GrantScope scope)` or `(CardColor color, GrantScope scope, boolean overriding)` | grant a color to permanents matching scope. When `overriding=true`, replaces all existing colors (e.g. "is a black Zombie"). Scope: `EQUIPPED_CREATURE`, `ENCHANTED_CREATURE`, etc. Used by Nim Deathmantle |
| `GrantSubtypeEffect` | `(CardSubtype subtype, GrantScope scope)` or `(CardSubtype subtype, GrantScope scope, boolean overriding)` | grant a creature subtype to permanents matching scope. When `overriding=true`, replaces all existing creature subtypes (non-creature subtypes like Equipment/Aura are preserved). Scope: `EQUIPPED_CREATURE`, `ENCHANTED_CREATURE`, etc. Used by Nim Deathmantle |
| `GrantSubtypeToTargetCreatureEffect` | `(CardSubtype subtype)` | one-shot effect that permanently grants a subtype to the targeted creature ("becomes a [subtype] in addition to its other types"). Added to `grantedSubtypes`, survives turn resets. Used by Olivia Voldaren |
| `EnchantedPermanentBecomesTypeEffect` | `(CardSubtype subtype)` | static effect for auras that set the enchanted permanent's subtype ("enchanted [permanent] is a [type]"). When the subtype is a basic land type (SWAMP, ISLAND, FOREST, MOUNTAIN, PLAINS), per MTG rule 305.7 replaces all existing basic land subtypes and overrides the land's mana production. Handled by static bonus system (`landSubtypeOverriding` flag) and `AbilityActivationService.tapPermanent`. Used by Evil Presence |
| `EnchantedPermanentBecomesChosenTypeEffect` | `()` | static effect for auras that set the enchanted permanent's subtype to the chosen basic land type stored on the source permanent's `chosenSubtype` field ("enchanted land is the chosen type"). Works like `EnchantedPermanentBecomesTypeEffect` but reads the subtype dynamically. Pair with `ChooseBasicLandTypeOnEnterEffect` in `ON_ENTER_BATTLEFIELD`. Used by Convincing Mirage |
| `GrantColorUntilEndOfTurnEffect` | `(CardColor color)` | target permanent becomes the specified color until end of turn. Per CR 105.3, replaces all previous colors (sets `colorOverridden` flag, clears existing `grantedColors`). Cleared on `resetModifiers()`. Checked by `PermanentColorInPredicate` for static effects, targeting, and costs |
| `GrantProtectionFromCardTypeUntilEndOfTurnEffect` | `(CardType cardType)` | target creature gains protection from the specified card type until end of turn. Adds to `Permanent.protectionFromCardTypes`. Cleared on `resetModifiers()`. Protection prevents blocking by, damage from, and targeting by sources of that card type. Checked via `GameQueryService.hasProtectionFromSourceCardTypes()` in combat, damage, and targeting services |
| `GrantProtectionFromNonSubtypeCreaturesUntilEndOfTurnEffect` | `(CardSubtype excludedSubtype)` | all creatures you control gain protection from creatures that do NOT have the specified subtype until end of turn. Mass grant (no target). Adds to `Permanent.protectionFromNonSubtypeCreaturesUntilEndOfTurn`. Cleared on `resetModifiers()`. Changeling creatures (all subtypes) are NOT affected. Checked via `GameQueryService.hasProtectionFromNonSubtypeCreatures()` integrated into `hasProtectionFromSource()`. Used by Spare from Evil (HUMAN) |
| `GrantProtectionChoiceUntilEndOfTurnEffect` | `(boolean includeArtifacts)` or `()` | on resolution, prompts the controller to choose a color (and optionally "artifacts" when `includeArtifacts=true`), then grants the target permanent protection from that choice until end of turn. Color protection stored in `Permanent.protectionFromColorsUntilEndOfTurn`, artifact protection in `Permanent.protectionFromCardTypes`. Both cleared on `resetModifiers()`. Triggers `ChoiceContext.ProtectionColorChoice` interaction. Used by Apostle's Blessing (`includeArtifacts=true`). The no-arg constructor defaults to `includeArtifacts=false` for cards like Gods Willing |
| `GrantDamageToOpponentCreatureBounceUntilEndOfTurnEffect` | `()` | until end of turn, creatures you control gain "Whenever this creature deals damage to an opponent, you may return target creature that player controls to its owner's hand." Sets `hasDamageToOpponentCreatureBounce` flag on controlled creatures; CombatService creates `ReturnPermanentsOnCombatDamageToPlayerEffect(PermanentIsCreaturePredicate)` trigger with xValue=1. Cleared on `resetModifiers()`. Used by Arm with Aether |
| `GrantFlashbackToGraveyardCardsEffect` | `(Set<CardType> cardTypes)` | grants flashback to cards of the specified types in the controller's graveyard until end of turn. The flashback cost equals the card's mana cost. Tracks granted card IDs in `GameData.cardsGrantedFlashbackUntilEndOfTurn`. Cards that already have native flashback are skipped. Cleared at end of turn by `TurnCleanupService`. Used by Past in Flames |
| `GrantFlashbackToTargetGraveyardCardEffect` | `(Set<CardType> cardTypes)` | targets a single card of a matching type in the controller's graveyard and grants it flashback until end of turn. The flashback cost equals the card's mana cost. Uses multi-graveyard targeting at ETB trigger time (`BattlefieldEntryService.handleGrantFlashbackETBTargeting`). Resolved by `KeywordGrantResolutionService`. Fizzles if target leaves graveyard. Tracks via `GameData.cardsGrantedFlashbackUntilEndOfTurn`. Used by Snapcaster Mage |

## Combat restrictions / evasion

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `CantBeBlockedEffect` | `()` | can't be blocked (static) |
| `CantAttackOrBlockAloneEffect` | `()` | this creature can't attack or block alone (CR 508.1b/509.1b, static) |
| `CantBlockEffect` | `()` | this creature can't block (static) |
| `CantBlockSourceEffect` | `(UUID sourcePermanentId)` | target creature can't block source permanent |
| `CanBeBlockedOnlyByFilterEffect` | `(PermanentPredicate blockerPredicate, String allowedBlockersDescription)` | can only be blocked by matching creatures (static) |
| `CanBeBlockedByAtMostNCreaturesEffect` | `(int maxBlockers)` | can be blocked by at most N creatures (static) |
| `CanBlockAnyNumberOfCreaturesEffect` | `()` | this creature can block any number of creatures (static) |
| `CanBlockOnlyIfAttackerMatchesPredicateEffect` | `(PermanentPredicate attackerPredicate, String allowedAttackersDescription)` | this creature can only block attackers matching predicate (static) |
| `CantAttackOrBlockUnlessEquippedEffect` | `()` | this creature can't attack or block unless it's equipped (static) |
| `CantAttackUnlessBattlefieldHasMatchingPermanentCountEffect` | `(PermanentPredicate permanentPredicate, int minimumCount, String requirementDescription)` | can't attack unless there are N or more matching permanents across all battlefields (static) |
| `CantAttackUnlessDefenderControlsMatchingPermanentEffect` | `(PermanentPredicate defenderPermanentPredicate, String requirementDescription)` | can't attack unless defender controls matching permanent (static) |
| `CreaturesCantAttackUnlessPredicateEffect` | `(PermanentPredicate exemptionPredicate)` | global static: ALL creatures can't attack unless they match the exemption predicate (e.g. flying or islandwalk) |
| `CantAttackUnlessOpponentDealtDamageThisTurnEffect` | `()` | can't attack unless an opponent has been dealt damage this turn from any source (static) |
| `CantBeBlockedIfDefenderControlsMatchingPermanentEffect` | `(PermanentPredicate defenderPermanentPredicate)` | can't be blocked as long as defender controls matching permanent (static) |
| `CanAttackAsThoughNoDefenderEffect` | `()` | this creature can attack as though it didn't have defender (static, typically wrapped in MetalcraftConditionalEffect) |
| `MustAttackEffect` | `()` | this creature must attack each turn if able (static) |
| `MustAttackThisTurnEffect` | `(boolean forceAttackController)` | target creature must attack this turn if able (one-shot, sets transient flag, cleared at end of turn). If forceAttackController=true, also sets mustAttackTargetId to ability controller (e.g. Alluring Siren "attacks you"). If false, creature may attack any legal target (e.g. Incite "attacks this turn if able"). |
| `MustBeBlockedByAllCreaturesEffect` | `()` | all creatures able to block this must do so (static, Lure-style) |
| `MustBlockSourceEffect` | `(UUID sourcePermanentId)` | target creature must block source permanent this turn if able |
| `AssignCombatDamageAsThoughUnblockedEffect` | `()` | assign combat damage as though unblocked (static) |
| `AssignCombatDamageWithToughnessEffect` | `()` | assign combat damage using toughness instead of power (static) |
| `MakeCreatureUnblockableEffect` | `()` or `(boolean selfTargeting)` | target creature is unblockable this turn. No-arg (false) targets another permanent; `true` makes the source creature unblockable (self-targeting, e.g. Trespassing Souleater) |
| `MakeAllCreaturesUnblockableEffect` | `()` | all creatures on all battlefields can't be blocked this turn |
| `TargetCreatureCantBlockThisTurnEffect` | `()` | target creature can't block this turn |
| `TargetPlayerCreaturesCantBlockThisTurnEffect` | `()` | all creatures controlled by target player (or planeswalker's controller) can't block this turn — uses shared target, no own targeting |
| `CantBlockThisTurnEffect` | `(PermanentPredicate filter)` | all creatures on all battlefields matching the predicate can't block this turn — non-targeted mass effect, pass `null` for all creatures |
| `EnchantedCreatureCantAttackOrBlockEffect` | `()` | enchanted creature can't attack or block (static, Pacifism-style) |
| `EnchantedCreatureSubtypeConditionalEffect` | `(CardSubtype subtype, CardEffect ifMatch, CardEffect ifNotMatch)` | conditional wrapper for auras: if enchanted creature has the subtype, `ifMatch` is active; otherwise `ifNotMatch` is active. Composes existing effects (e.g. `StaticBoostEffect` + `EnchantedCreatureCantAttackOrBlockEffect`). Static handler delegates to inner effect's handler; combat system unwraps automatically via `hasAuraWithEffect` |
| `EnchantedCreatureCantAttackEffect` | `()` | enchanted creature can't attack but can still block (static, Forced Worship-style) |
| `EnchantedCreatureCantActivateAbilitiesEffect` | `()` | enchanted creature's activated abilities can't be activated (static, Arrest-style) |

## Tap / untap

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `TapTargetPermanentEffect` | `()` | tap target permanent |
| `TapOrUntapTargetPermanentEffect` | `()` | tap or untap target permanent |
| `UntapTargetPermanentEffect` | `()` | untap target permanent |
| `UntapAllTargetPermanentsEffect` | `()` | untap all target permanents (multi-target variant, iterates over `getTargetIds()`) |
| `UntapSelfEffect` | `()` | untap this permanent |
| `UntapAttackedCreaturesEffect` | `()` | untap creatures that attacked this turn (end of combat) |
| `TapCreaturesEffect` | `(Set<TargetFilter> filters)` | tap all creatures matching filters |
| `DoesntUntapDuringUntapStepEffect` | `()` | this permanent doesn't untap during untap step (static) |
| `MayNotUntapDuringUntapStepEffect` | `()` | controller may choose not to untap this permanent during untap step (static); prompts player via may-ability system |
| `PreventTargetUntapWhileSourceTappedEffect` | `()` | target permanent doesn't untap during its controller's untap step for as long as the source permanent remains tapped; piggybacks on companion targeting effect (e.g. `TapTargetPermanentEffect`) |
| `SkipNextUntapOnTargetEffect` | `()` | target permanent doesn't untap during its controller's next untap step; increments `skipUntapCount` which is decremented each untap step; piggybacks on companion targeting effect (e.g. `TapTargetPermanentEffect`) |
| `TapPermanentsOfTargetPlayerEffect` | `(PermanentPredicate filter)` | tap all permanents matching filter that target player controls; targets a player |
| `SkipNextUntapPermanentsOfTargetPlayerEffect` | `(PermanentPredicate filter)` | all permanents matching filter that target player controls don't untap during that player's next untap step; increments `skipUntapCount`; targets a player |
| `AttachedCreatureDoesntUntapEffect` | `()` | attached creature (aura or equipment) doesn't untap during untap step (static) |
| `UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect` | `(TurnStep step)` or `(TurnStep step, PermanentPredicate filter)` | untap permanents you control during each other player's step. `filter=null` (1-arg form) untaps all; provide a predicate (e.g. `PermanentIsArtifactPredicate`) to untap only matching permanents |
| `UntapAllControlledPermanentsEffect` | `(PermanentPredicate filter)` | untap all permanents you control matching filter (e.g. `PermanentIsLandPredicate` for "untap all lands you control") |
| `UntapEachOtherCreatureYouControlEffect` | `(PermanentPredicate filter)` | untap each other creature you control matching filter; `()` no-arg overload untaps all (ON_ATTACK trigger or activated ability) |
| `UnattachEquipmentFromTargetPermanentsEffect` | `()` | unattach all equipment from target permanents (multi-target) |

## Control / steal

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `TargetPlayerGainsControlOfSourceCreatureEffect` | `()` | target opponent gains control of this creature (ETB) |
| `GainControlOfTargetPermanentEffect` | `(CardSubtype grantedSubtype)` or `()` | gain control of target permanent permanently — optionally grants a subtype (e.g. Captivating Vampire "It becomes a Vampire") |
| `GainControlOfTargetPermanentUntilEndOfTurnEffect` | `()` | gain control of target permanent until end of turn — card's target filter handles type restriction (Threaten, Metallic Mastery) |
| `GainControlOfTargetPermanentWhileSourceEffect` | `()` | gain control of target permanent for as long as you control the source permanent (e.g. Olivia Voldaren). Tracked via `sourceDependentStolenCreatures`; creature returns when source leaves the battlefield or changes controllers |
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
| `PreventCombatDamageExceptBySubtypesEffect` | `(PermanentPredicate exemptPredicate)` | prevent all combat damage this turn by creatures NOT matching the exempt predicate. Used by Moonmist with `PermanentHasAnySubtypePredicate(Set.of(WEREWOLF, WOLF))` |
| `PreventAllCombatDamageToAndByEnchantedCreatureEffect` | `()` | prevent all combat damage to and dealt by enchanted creature (non-combat damage still applies) |
| `PreventAllDamageEffect` | `()` | prevent all damage (e.g. Fog-style) |
| `PreventAllDamageToAndByEnchantedCreatureEffect` | `()` | prevent all damage to and dealt by enchanted creature |
| `PreventAllNoncombatDamageToAttachedCreatureEffect` | `()` | prevent all noncombat damage to attached (equipped/enchanted) creature (e.g. Magebane Armor) |
| `PreventDamageFromColorsEffect` | `(Set<CardColor> colors)` | prevent all damage from sources of specified colors (static) |
| `PreventNextColorDamageToControllerEffect` | `(CardColor chosenColor)` | prevent next damage of chosen color to controller |
| `PreventAllDamageToControllerAndCreaturesEffect` | `()` | prevent all damage to controller and creatures controller controls this turn (Safe Passage-style) |
| `PreventAllDamageByTargetCreatureEffect` | `()` | prevent all damage target creature(s) would deal this turn (multi-target via targetIds) |
| `PreventAllDamageFromChosenSourceEffect` | `()` | prevent all damage a chosen source would deal to controller this turn (prompts permanent choice on resolution) |
| `PreventDamageFromChosenSourceAndRedirectToAnyTargetEffect` | `(int amount)` | prevent next N damage from a chosen source to controller/controller's permanents this turn; redirects to any target (player or creature). Source chosen on resolution, target chosen on cast. `canTargetPlayer()=true`, `canTargetPermanent()=true` (e.g. Harm's Way) |
| `PreventXDamageToControllerAndRedirectToTargetPlayerEffect` | `()` | prevent next X damage to controller this turn; if prevented, source creature deals that much to target player (e.g. Vengeful Archon). Uses X-cost ability. `canTargetPlayer()=true` |
| `PreventDamageFromOpponentSourcesEffect` | `(int amount)` | prevent N damage from each opponent-controlled source that would deal damage to this permanent's controller (static, e.g. Guardian Seraph). Applied per damage event in both combat and non-combat paths |
| `PreventDamageAndAddMinusCountersEffect` | `()` | prevent all damage to this creature and put a -1/-1 counter for each 1 damage prevented (static, e.g. Phyrexian Hydra) |
| `PreventDamageAndRemovePlusOnePlusOneCountersEffect` | `()` or `(boolean removeOneOnly)` | prevent all damage to this creature and remove +1/+1 counters (static). Default `removeOneOnly=false`: removes counters equal to damage (Protean Hydra). With `removeOneOnly=true`: removes exactly one counter per damage event regardless of damage amount (Unbreathing Horde). Can only remove counters up to the number currently on the creature |
| `DelayedPlusOnePlusOneCounterRegrowthEffect` | `()` | whenever a +1/+1 counter is removed from this creature, put two +1/+1 counters on it at the beginning of the next end step (static, e.g. Protean Hydra). Works with PreventDamageAndRemovePlusOnePlusOneCountersEffect. Registers delayed triggers in GameData.pendingDelayedPlusOnePlusOneCounters |
| `ProtectionFromCardTypesEffect` | `(Set<CardType> cardTypes)` | protection from specified card types (static, permanent). Unlike `GrantProtectionFromCardTypeUntilEndOfTurnEffect`, this is NOT cleared by `resetModifiers()` — it lives on the card's STATIC effects. Checked by `GameQueryService.hasProtectionFromSourceCardTypes()` in both overloads (permanent source and card source) |
| `ProtectionFromColorsEffect` | `(Set<CardColor> colors)` | protection from specified colors (static) |
| `ProtectionFromSubtypesEffect` | `(Set<CardSubtype> subtypes)` | protection from specified creature subtypes (static). Prevents blocking, damage, targeting, and enchanting/equipping by sources with any of the listed subtypes. Also checks transient/granted subtypes and Changeling. Checked by `GameQueryService.hasProtectionFromSourceSubtypes()`. Used by Baneslayer Angel (DEMON, DRAGON) |
| `ProtectionFromChosenColorEffect` | `()` | protection from chosen color (static, requires ChooseColorOnEnterEffect) |
| `CantBeTargetedBySpellColorsEffect` | `(Set<CardColor> colors)` | can't be targeted by spells of specified colors (static) |
| `CantBeTargetedByNonColorSourcesEffect` | `(CardColor allowedColor)` | can't be targeted by spells or abilities from sources that don't have the allowed color (static, e.g. Gaea's Revenge) |
| `CantBeTargetOfSpellsOrAbilitiesEffect` | `()` | can't be targeted by opponents' spells or abilities (hexproof behavior, use with GrantEffectEffect) |
| `GrantEffectEffect` | `(CardEffect effect, GrantScope scope)` | grant a CardEffect to permanents matching scope (e.g. OWN_CREATURES) |
| `RedirectPlayerDamageToEnchantedCreatureEffect` | `()` | redirect damage dealt to player to enchanted creature |
| `RedirectUnblockedCombatDamageToSelfEffect` | `()` | redirect unblocked combat damage to this creature |
| `GrantControllerHexproofEffect` | `()` | controller has hexproof (can't be targeted by opponents) (static) |
| `GrantControllerShroudEffect` | `()` | controller has shroud (can't be targeted) (static) |
| `GrantControllerSpellsCantBeCounteredByColorsEffect` | `(Set<CardColor> colors)` | controller's spells can't be countered by spells of specified colors this turn (one-shot, cleared at end of turn) |
| `GrantControllerCreaturesCantBeTargetedByColorsEffect` | `(Set<CardColor> colors)` | controller's creatures can't be targeted by spells of specified colors this turn (one-shot, cleared at end of turn) |

## Mana

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `AwardManaEffect` | `(ManaColor color, int amount)` or `(ManaColor color)` (defaults amount to 1) | add N mana of specified color. Also stack-resolvable via `@HandlesEffect` in LifeResolutionService |
| `AwardAnyColorManaEffect` | `()` | add one mana of any color |
| `AddManaOnEnchantedLandTapEffect` | `(ManaColor color, int amount)` | when enchanted land is tapped, add N mana of color |
| `AddExtraManaOfChosenColorOnLandTapEffect` | `()` | ON_ANY_PLAYER_TAPS_LAND trigger: when a land you control taps for mana of the source permanent's chosen color, add one additional mana of that color. Checks ON_TAP effects of the tapped land. Used by Caged Sun |
| `AddOneOfEachManaTypeProducedByLandEffect` | `()` | ON_ANY_PLAYER_TAPS_LAND trigger: when a land you control taps for mana, add one additional mana of any type that land produced (exactly 1 mana, picks first type if multiple). Used by Vorinclex, Voice of Hunger |
| `OpponentTappedLandDoesntUntapEffect` | `()` | ON_ANY_PLAYER_TAPS_LAND trigger: when an opponent taps a land for mana, increments skipUntapCount on the tapped land so it doesn't untap during its controller's next untap step. Multiple taps stack. Used by Vorinclex, Voice of Hunger |
| `DoubleManaPoolEffect` | `()` | double your mana pool |
| `AddManaPerControlledSubtypeEffect` | `(ManaColor color, CardSubtype subtype)` | add one mana of color for each permanent with subtype you control |
| `AwardRestrictedManaEffect` | `(ManaColor color, int amount, Set<CardType> allowedSpellTypes)` | add N mana of specified color that can only be spent to cast spells of the given types. Currently supports RED via `ManaPool.restrictedRed`; `ManaCost.canPay/pay` accept `restrictedRedContext=true` to include this mana for both colored and generic costs |
| `AddColorlessManaPerChargeCounterOnSourceEffect` | `()` | add {C} for each charge counter on the source permanent. Implements `ManaProducingEffect` so the ability is treated as a mana ability. Used by Shrine of Boundless Growth |
| `AwardArtifactOnlyColorlessManaEffect` | `(int amount)` | add N colorless mana that can only be spent to cast artifact spells or activate abilities of artifacts. Stored in `ManaPool.artifactOnlyColorless`; `ManaCost.canPay/pay` accept `artifactContext=true` to include this mana |
| `AwardMyrOnlyColorlessManaEffect` | `(int amount)` | add N colorless mana that can only be spent to cast Myr spells or activate abilities of Myr. Stored in `ManaPool.myrOnlyColorless`; `ManaCost.canPay/pay` accept `myrContext=true` to include this mana |
| `PreventManaDrainEffect` | `()` | players don't lose unspent mana as steps/phases end (static) |

## Copy / clone

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `CopyPermanentOnEnterEffect` | `(PermanentPredicate filter, String typeLabel)` or `(PermanentPredicate filter, String typeLabel, Integer powerOverride, Integer toughnessOverride)` or `(PermanentPredicate filter, String typeLabel, Integer powerOverride, Integer toughnessOverride, Set<CardType> additionalTypesOverride)` or `(PermanentPredicate filter, String typeLabel, Integer powerOverride, Integer toughnessOverride, Set<CardType> additionalTypesOverride, List<ActivatedAbility> additionalActivatedAbilities)` | enter as copy of permanent matching filter (Clone-style). Optional P/T overrides for "copy except it's X/Y" (e.g. Quicksilver Gargantuan). Optional `additionalTypesOverride` for "copy except it's also an [type]" (e.g. Phyrexian Metamorph is always an artifact). Optional `additionalActivatedAbilities` for "copy except it has [ability]" (e.g. Evil Twin's destroy same-name ability) |
| `CopySpellEffect` | `()` | copy target spell |
| `CopySpellForEachOtherPlayerEffect` | `()` | trigger descriptor: whenever a player casts an instant or sorcery spell, each other player copies that spell. Each of those players may choose new targets for their copy. Place in `ON_ANY_PLAYER_CASTS_SPELL` slot. Resolution snapshot populated at trigger time. Used by Hive Mind |
| `CopySpellForEachOtherSubtypePermanentEffect` | `(CardSubtype subtype)` | trigger descriptor: whenever a player casts an instant or sorcery spell that targets only a single permanent with the given subtype, copy the spell for each other permanent with that subtype the spell could target. Each copy targets a different one of those permanents. Place in `ON_ANY_PLAYER_CASTS_SPELL` slot. Resolution snapshot populated at trigger time by `checkSpellCastTriggers`. Used by Precursor Golem |
| `BecomeCopyOfTargetCreatureEffect` | `()` | source permanent becomes a copy of target creature, retaining the triggered ability that granted this effect. Used by Cryptoplasm. Place in UPKEEP_TRIGGERED wrapped in MayEffect. canTargetPermanent=true |
| `ChangeTargetOfTargetSpellWithSingleTargetEffect` | `()` | change target of target spell with single target |
| `ChangeTargetOfTargetSpellToSourceEffect` | `()` | change a target of target spell or ability to source permanent (e.g. Spellskite). Automatically redirects without player choice. If source is not a legal target or spell has no targets, does nothing. Use with StackEntryHasTargetPredicate to allow targeting spells and abilities on the stack. |
| `ChooseNewTargetsForTargetSpellEffect` | `()` | you may choose new targets for target spell (e.g. Redirect). Targets any spell on the stack. On resolution, if target spell has targets, offers a may-ability prompt to choose new targets. No target filter needed — works on any spell. |

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
| `AnimateTargetLandWhileSourceOnBattlefieldEffect` | `(int power, int toughness, CardColor color, List<CardSubtype> grantedSubtypes)` | target land becomes creature with P/T + color + subtypes for as long as source remains on battlefield; reverts when source leaves. Uses `sourceLinkedAnimations` tracking on GameData. canTargetPermanent=true. Used by Awakener Druid |
| `AnimateNoncreatureArtifactsEffect` | `()` | animate all noncreature artifacts into creatures (March of the Machines-style) |
| `TransformSelfEffect` | `()` | Transforms the source permanent to its back face (or back to front if already transformed). Used by double-faced cards with Transform |
| `TransformAllEffect` | `(PermanentPredicate filter)` | Transforms all permanents on the battlefield matching the given predicate. Each matching permanent flips to its back face (or back to front if already transformed). Used by Moonmist with `PermanentHasSubtypePredicate(HUMAN)` |
| `TapAndTransformSelfEffect` | `()` | Taps the source permanent and then transforms it. Used with `DidntAttackConditionalEffect` for back-face triggers (e.g. Homicidal Brute) |
| `DrawDiscardTransformIfCreatureDiscardedEffect` | `()` | Draw 1, discard 1; if creature discarded, untap + transform source. Loot with conditional transform (e.g. Civilized Scholar) |
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
| `StaticBoostEffect` | `(int powerBoost, int toughnessBoost, GrantScope.EQUIPPED_CREATURE)` | equipped creature gets +X/+Y (static). See Boost Effects section for full constructor signatures |
| `BoostCreaturePerCardsInAllGraveyardsEffect` | `(CardPredicate filter, GrantScope.EQUIPPED_CREATURE)` | equipped creature gets +X/+X where X = cards in all graveyards matching filter (static) |
| `BoostCreaturePerCardsInControllerGraveyardEffect` | `(CardPredicate filter, int powerPerCard, int toughnessPerCard, GrantScope.EQUIPPED_CREATURE)` | equipped creature gets +X/+Y per card in controller's graveyard matching filter (static) |
| `BoostCreaturePerControlledCardTypeEffect` | `(CardType cardType, int powerPerMatch, int toughnessPerMatch, GrantScope.EQUIPPED_CREATURE)` | equipped creature gets +X/+Y per controlled permanent with card type (static). Used by Blackblade Reforged (LAND) |
| `BoostCreaturePerControlledSubtypeEffect` | `(CardSubtype subtype, int powerPerSubtype, int toughnessPerSubtype, GrantScope.EQUIPPED_CREATURE)` | equipped creature gets +X/+Y per controlled permanent with subtype (static) |
| `BoostCreaturePerMatchingLandNameEffect` | `(int powerPerMatch, int toughnessPerMatch, GrantScope.EQUIPPED_CREATURE)` | equipped creature gets +X/+Y per land matching imprinted card name (static) |
| `SacrificeOnUnattachEffect` | `()` | whenever this equipment becomes unattached, sacrifice the previously-equipped creature (static marker) |
| `AttachSourceEquipmentToTargetCreatureEffect` | `()` | attach source equipment to target creature on ETB. Reads sourcePermanentId as equipment, targetId as creature. Used by equipment with "When this Equipment enters, attach it to target creature you control." |
| `AttachTargetEquipmentToTargetCreatureEffect` | `()` | attach target Equipment to target creature (multi-target; reads targetIds[0] as equipment, [1] as creature) |
| `SacrificeSourceEquipmentCost` | `()` | cost effect that sacrifices the equipment granting this ability (not the equipped creature). Used for equipment-granted abilities like Blazing Torch's "{T}, Sacrifice Blazing Torch: ..." where the creature activates the ability but the equipment is sacrificed. The source equipment is identified via `ActivatedAbility.grantSourcePermanentId`, which is set automatically by the static bonus system |

## Static restrictions / taxes

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `CantCastSpellsWithSameNameAsExiledCardEffect` | `()` | no player can cast spells with the same name as the card exiled by the source permanent (static, Exclusion Ritual) |
| `SpellsWithChosenNameCantBeCastEffect` | `()` | no player can cast spells with the same name as the chosen name on the source permanent (static, Nevermore) |
| `CantCastSpellTypeEffect` | `(Set<CardType> restrictedTypes)` | controller can't cast spells of specified types (static) |
| `CantSearchLibrariesEffect` | `()` | players can't search libraries; any player may pay {2} to ignore until end of turn (static, Leonin Arbiter) |
| `PlayersCannotDrawCardsEffect` | `()` | players can't draw cards (static, Omen Machine) |
| `LimitSpellsPerTurnEffect` | `(int maxSpells)` | each player can cast at most N spells per turn (static) |
| `IncreaseOpponentCastCostEffect` | `(Set<CardType> affectedTypes, int amount)` | opponent's spells of types cost N more (static) |
| `RequirePaymentToAttackEffect` | `(int amountPerAttacker)` | must pay N mana per attacking creature (static) |
| `AlternativeCostForSpellsEffect` | `(int cost, CardPredicate filter)` | controller may pay the alternative cost rather than the mana cost for spells matching the filter (static, from battlefield permanent, e.g. Rooftop Storm with cost=0 and creature+zombie filter) |
| `ReduceOwnCastCostForCardTypeEffect` | `(Set<CardType> affectedTypes, int amount)` | reduce controller's spells of given types by N (static, from battlefield permanent, e.g. Heartless Summoning) |
| `ReduceOwnCastCostIfOpponentControlsMoreCreaturesEffect` | `(int minimumCreatureDifference, int amount)` | reduce cast cost by N if opponent has M+ more creatures |
| `ReduceOwnCastCostForSharedCardTypeWithImprintEffect` | `(int amount)` | reduce cast cost of controller's spells by N if they share a card type with the imprinted card (static, Semblance Anvil) |
| `ReduceOwnCastCostIfControlsSubtypeEffect` | `(CardSubtype subtype, int amount)` | reduce this spell's cast cost by N if controller controls a permanent with the given subtype (static, Academy Journeymage) |
| `ReduceOwnCastCostIfMetalcraftEffect` | `(int amount)` | reduce this spell's cast cost by N if controller has metalcraft (3+ artifacts) (static, Stoic Rebuttal) |
| `ReduceOwnCastCostPerCreatureOnBattlefieldEffect` | `(int amountPerCreature)` | reduce this spell's cast cost by N for each creature on the battlefield across all players (static, Blasphemous Act) |
| `NoMaximumHandSizeEffect` | `()` | you have no maximum hand size (static, requires permanent on battlefield) |
| `GrantPermanentNoMaxHandSizeEffect` | `()` | you have no maximum hand size for the rest of the game (one-shot spell effect, persists via GameData.playersWithNoMaximumHandSize) |
| `ReduceOpponentMaxHandSizeEffect` | `(int reduction)` | each opponent's maximum hand size is reduced by N (static, checked during cleanup discard) |
| `EntersTappedEffect` | `()` | this permanent enters the battlefield tapped (static, implements `ReplacementEffect`). Replaces `setEntersTapped(true)` |
| `EnterPermanentsOfTypesTappedEffect` | `(Set<CardType> cardTypes)` or `(Set<CardType> cardTypes, boolean opponentsOnly)` | permanents of specified types enter tapped (static). When `opponentsOnly` is true, only opponents' permanents are affected (e.g. Urabrask the Hidden) |
| `EntersTappedUnlessControlLandSubtypeEffect` | `(List<CardSubtype> requiredSubtypes)` | enters tapped unless you control a permanent with one of the required land subtypes (check lands, static, implements `ReplacementEffect`) |
| `EntersTappedUnlessFewLandsEffect` | `(int maxOtherLands)` | enters tapped unless you control N or fewer other lands (fast lands, static, implements `ReplacementEffect`) |
| `OpponentsCantAttackIfCastSpellThisTurnEffect` | `()` | each opponent who cast a spell this turn can't attack with creatures (static, Angelic Arbiter) |
| `OpponentsCantCastSpellsIfAttackedThisTurnEffect` | `()` | each opponent who attacked with a creature this turn can't cast spells (static, Angelic Arbiter) |
| `OpponentsCantCastSpellsThisTurnEffect` | `()` | opponents of the controller can't cast spells this turn (spell, Silence) |

## Choose / name

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `ChooseCardNameOnEnterEffect` | `()` or `(List<CardType> excludedTypes)` | choose a card name as ETB (implements `ChooseCardNameEffect` marker). No-arg allows any name (Pithing Needle); with excludedTypes filters the name list (e.g. `List.of(LAND)` for Phyrexian Revoker) |
| `ActivatedAbilitiesOfChosenNameCantBeActivatedEffect` | `()` or `(boolean blocksManaAbilities)` | activated abilities of chosen name can't be activated (static). No-arg (false) excludes mana abilities (Pithing Needle); `true` also blocks mana abilities (Phyrexian Revoker) |
| `ActivatedAbilitiesOfMatchingPermanentsCantBeActivatedEffect` | `(PermanentPredicate predicate)` | all activated abilities (including mana abilities) of permanents matching the predicate can't be activated (static). Used by Stony Silence with `PermanentIsArtifactPredicate` |
| `ChooseColorOnEnterEffect` | `()` | choose a color as ETB (implements `ChooseColorEffect` marker) |
| `ChooseSubtypeOnEnterEffect` | `()` | choose a creature type as ETB. Detected by `StackResolutionService.resolveEnchantmentSpell()` to prompt `beginSubtypeChoice`. Stores result in `Permanent.chosenSubtype` |
| `ChooseBasicLandTypeOnEnterEffect` | `()` | choose a basic land type as ETB. Detected by `StackResolutionService.resolveEnchantmentSpell()` (aura path) to prompt `beginBasicLandTypeChoice`. Stores result in `Permanent.chosenSubtype`. Used by Convincing Mirage |
| `GrantChosenSubtypeToOwnCreaturesEffect` | `()` | static effect: each creature you control is the chosen type in addition to its other types. Reads `chosenSubtype` from source permanent. Used by Xenograft |

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
| `KarnRestartGameEffect` | `()` | Karn's ultimate: restart the game per CR 727, leaving non-Aura permanent cards exiled with Karn in exile, then put them onto battlefield under controller's control. Resets life totals, hands, libraries, graveyards, exile zones, battlefields, and all game state. Controller goes first, no mulligans |
| `GenesisWaveEffect` | `()` | reveal top X cards, put any number of permanent cards with MV ≤ X onto battlefield, rest to graveyard. X read from `StackEntry.getXValue()` |
| `SphinxAmbassadorEffect` | `()` | ON_COMBAT_DAMAGE_TO_PLAYER trigger: search damaged player's library for a card, that player names a card, if you found a creature with a different name you may put it onto battlefield under your control, then shuffle |
| `SphinxAmbassadorPutOnBattlefieldEffect` | `()` | Marker effect used in PendingMayAbility for the "you may put it onto the battlefield" step of Sphinx Ambassador's trigger |

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
| Return from exile to hand | `ExileReturnResolutionService` |
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
| Exile (return from exile) | `ExileTargetValidators` | `GameQueryService` |
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
