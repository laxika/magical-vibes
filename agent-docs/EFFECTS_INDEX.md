# EFFECTS_INDEX

Purpose: cut token usage when implementing cards by quickly mapping "card text intent" to existing reusable effects and their resolver location.

## How to use this index

1. Parse card text into primitive actions (damage, draw, bounce, etc.).
2. Find each primitive below and reuse existing effects first.
3. Only add new effect records when no existing effect can express the behavior.
4. If you add a new effect record, register it in the matching `*ResolutionService` provider.

## Common intents -> effect classes

- `deal N damage to any target`: `DealDamageToAnyTargetEffect`, `DealXDamageToAnyTargetEffect`
- `deal N damage to target player`: `DealDamageToTargetPlayerEffect`, `DealDamageToTargetPlayerByHandSizeEffect`
- `deal N damage to all creatures`: `DealDamageToAllCreaturesEffect`
- `deal N damage to all creatures and each player`: `DealDamageToAllCreaturesAndPlayersEffect`
- `deal N damage then gain life`: `DealDamageToAnyTargetAndGainLifeEffect`, `DealXDamageToAnyTargetAndGainXLifeEffect`
- `deal damage to yourself/controller`: `DealDamageToControllerEffect`
- `when enchanted land is tapped for mana, add mana`: `AddManaOnEnchantedLandTapEffect`
- `destroy target permanent`: `DestroyTargetPermanentEffect`
- `destroy target creature`: `DestroyTargetPermanentEffect` + `PermanentPredicateTargetFilter(PermanentIsCreaturePredicate)`
- `destroy all creatures/artifacts/enchantments` (optionally only opponents' permanents): `DestroyAllPermanentsEffect`
- `sacrifice creature`: `SacrificeCreatureEffect`, `EachOpponentSacrificesCreatureEffect`
- `counter spell`: `CounterSpellEffect`, `CounterUnlessPaysEffect`
- `creature spells can't be countered`: `CreatureSpellsCantBeCounteredEffect`
- `stack/spell target restrictions`: `StackEntryPredicateTargetFilter` + stack predicates (`StackEntryTypeInPredicate`, `StackEntryColorInPredicate`, `StackEntryIsSingleTargetPredicate`, `StackEntryAnyOfPredicate`, `StackEntryAllOfPredicate`, `StackEntryNotPredicate`)
- `return target permanent/creature`: `ReturnTargetPermanentToHandEffect` (+ creature target filter when needed)
- `at upkeep, choose and return creature`: `BounceCreatureOnUpkeepEffect` (scope + filters)
- `at upkeep, return a [color] creature you control`: `BounceCreatureOnUpkeepEffect` + `ControlledPermanentPredicateTargetFilter` + `PermanentColorInPredicate`
- `return target card from your graveyard to your hand`: `ReturnCardFromGraveyardToHandEffect`
- `return all creatures to hand`: `ReturnCreaturesToOwnersHandEffect`
- `artifacts/lands enter tapped`: `EnterPermanentsOfTypesTappedEffect` (e.g. `Set.of(CardType.ARTIFACT, CardType.LAND)`)
- `draw cards`: `DrawCardEffect`, `DrawCardForTargetPlayerEffect`
- `when an opponent draws a card, deal damage to that player`: `DealDamageToTargetPlayerEffect` on `EffectSlot.ON_OPPONENT_DRAWS`
- `discard`: `DiscardCardEffect`, `TargetPlayerDiscardsEffect`, `RandomDiscardEffect`
- `you may put a [type] card from hand onto battlefield`: `MayEffect(PutCardToBattlefieldEffect(CardType.X), "...")`
- `opponent may put a creature card from hand onto battlefield`: `OpponentMayPlayCreatureEffect`
- `mill`: `MillTargetPlayerEffect`, `MillHalfLibraryEffect`, `MillByHandSizeEffect`
- `search library`: `SearchLibraryForCardToHandEffect`, `SearchLibraryForBasicLandToHandEffect`
- `search library for card type(s) to hand`: `SearchLibraryForCardTypesToHandEffect(Set<CardType>)`
- `search library`: `SearchLibraryForCardToHandEffect`, `SearchLibraryForBasicLandToHandEffect`, `SearchLibraryForCardTypesToBattlefieldEffect`
- `pay mana, then search library for named card and put onto battlefield`: `PayManaAndSearchLibraryForCardNamedToBattlefieldEffect`
- `look at top N cards, may reveal a card of specified type(s) and put it into hand, rest on bottom`: `LookAtTopCardsMayRevealCreaturePutIntoHandRestOnBottomEffect`
- `shuffle into library`: `ShuffleIntoLibraryEffect`, `ShuffleGraveyardIntoLibraryEffect`
- `create creature tokens`: `CreateCreatureTokenEffect`, `CreateCreatureTokenWithColorsEffect`
- `gain life`: `GainLifeEffect`, `GainLifePerGraveyardCardEffect`, `GainLifeEqualToTargetToughnessEffect`
- `target player gains N life`: `TargetPlayerGainsLifeEffect`
- `lose life / drain`: `LoseLifeEffect`, `TargetPlayerLosesLifeAndControllerGainsLifeEffect`, `EnchantedCreatureControllerLosesLifeEffect`
- `target opponent gains control of this creature (ETB)`: `TargetPlayerGainsControlOfSourceCreatureEffect`
- `each player loses life for each creature they control`: `EachPlayerLosesLifePerCreatureControlledEffect`
- `target player loses the game`: `TargetPlayerLosesGameEffect`
- `lose the game if not cast from hand (ETB check)`: `LoseGameIfNotCastFromHandEffect`
- `win the game if condition is met`: `WinGameIfCreaturesInGraveyardEffect`
- `pump target/self/all`: `BoostTargetCreatureEffect`, `BoostSelfEffect`, `BoostAllOwnCreaturesEffect`, `BoostAllCreaturesXEffect`
- `enchanted creature gets +X/+X per controlled subtype`: `BoostEnchantedCreaturePerControlledSubtypeEffect`
- `gets +N/+N for each other creature with same name`: `BoostByOtherCreaturesWithSameNameEffect`
- `when you cast a spell of specific colors, put +1/+1 counter on this`: `PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect`
- `power/toughness each equal number of lands you control`: `PowerToughnessEqualToControlledLandCountEffect`
- `power/toughness each equal number of creatures you control`: `PowerToughnessEqualToControlledCreatureCountEffect`
- `grant keyword`: `GrantKeywordEffect` with `GrantKeywordEffect.Scope` (`TARGET`, `SELF`, `ENCHANTED_CREATURE`, `EQUIPPED_CREATURE`, `OWN_TAPPED_CREATURES`, `OWN_CREATURES`)
- `grant activated mana ability to lands you control`: `GrantActivatedAbilityToOwnLandsEffect` (typically with `AwardAnyColorManaEffect`)
- `can't block (static on creature)`: `CantBlockEffect`
- `can be blocked only by permanents matching a composed predicate`: `CanBeBlockedOnlyByFilterEffect` + permanent predicates (`PermanentHasKeywordPredicate`, `PermanentHasSubtypePredicate`, `PermanentAnyOfPredicate`, `PermanentAllOfPredicate`, `PermanentNotPredicate`)
- `can be blocked by at most N creatures`: `CanBeBlockedByAtMostNCreaturesEffect`
- `assign combat damage as though it weren't blocked`: `AssignCombatDamageAsThoughUnblockedEffect`
- `target restrictions based on permanent properties`: `PermanentPredicateTargetFilter` + permanent predicates (`PermanentIsCreaturePredicate`, `PermanentIsTappedPredicate`, `PermanentColorInPredicate`, `PermanentPowerAtMostPredicate`, etc.)
- `can't be the target of [color] spells`: `CantBeTargetedBySpellColorsEffect`
- `all creatures able to block enchanted creature do so`: `MustBeBlockedByAllCreaturesEffect`
- `tap/untap`: `TapTargetPermanentEffect` (use `Set.of(CardType.CREATURE)` for creature-only), `TapOrUntapTargetPermanentEffect`, `UntapTargetPermanentEffect`, `UntapSelfEffect`
- `untap all permanents you control during each other player's [step]`: `UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect`
- `prevent damage`: `PreventDamageToTargetEffect`, `PreventNextDamageEffect`, `PreventAllCombatDamageEffect`, `PreventDamageFromColorsEffect`
- `copy or retarget spell`: `CopySpellEffect`, `ChangeTargetOfTargetSpellWithSingleTargetEffect`
- `extra turn / additional combat / end turn`: `ExtraTurnEffect`, `AdditionalCombatMainPhaseEffect`, `EndTurnEffect`
- `equip`: `EquipEffect`
- `this spell costs {N} less to cast if an opponent controls M more creatures`: `ReduceOwnCastCostIfOpponentControlsMoreCreaturesEffect`

## Provider map (where effects are resolved)

- Damage: `magical-vibes-backend/src/main/java/com/github/laxika/magicalvibes/service/DamageResolutionService.java`
- Destruction/sacrifice: `magical-vibes-backend/src/main/java/com/github/laxika/magicalvibes/service/DestructionResolutionService.java`
- Bounce: `magical-vibes-backend/src/main/java/com/github/laxika/magicalvibes/service/BounceResolutionService.java`
- Counter: `magical-vibes-backend/src/main/java/com/github/laxika/magicalvibes/service/CounterResolutionService.java`
- Library/search/mill: `magical-vibes-backend/src/main/java/com/github/laxika/magicalvibes/service/LibraryResolutionService.java`
- Graveyard return/exile from graveyard: `magical-vibes-backend/src/main/java/com/github/laxika/magicalvibes/service/GraveyardReturnResolutionService.java`
- Player interaction (draw/discard/choices): `magical-vibes-backend/src/main/java/com/github/laxika/magicalvibes/service/effect/PlayerInteractionResolutionService.java`
- Life: `magical-vibes-backend/src/main/java/com/github/laxika/magicalvibes/service/effect/LifeResolutionService.java`
- Creature mods (tap/pump/temp keyword): `magical-vibes-backend/src/main/java/com/github/laxika/magicalvibes/service/effect/CreatureModResolutionService.java`
- Permanent control/tokens/regeneration: `magical-vibes-backend/src/main/java/com/github/laxika/magicalvibes/service/effect/PermanentControlResolutionService.java`
- Static continuous effects (keywords/stats/granted abilities): `magical-vibes-backend/src/main/java/com/github/laxika/magicalvibes/service/effect/StaticEffectResolutionService.java`
- Prevention: `magical-vibes-backend/src/main/java/com/github/laxika/magicalvibes/service/PreventionResolutionService.java`
- Turn effects: `magical-vibes-backend/src/main/java/com/github/laxika/magicalvibes/service/TurnResolutionService.java`
- Copy/retarget: `magical-vibes-backend/src/main/java/com/github/laxika/magicalvibes/service/CopyResolutionService.java`, `magical-vibes-backend/src/main/java/com/github/laxika/magicalvibes/service/TargetRedirectionResolutionService.java`
- Exile target permanent: `magical-vibes-backend/src/main/java/com/github/laxika/magicalvibes/service/ExileResolutionService.java`
- Card-specific one-offs: `magical-vibes-backend/src/main/java/com/github/laxika/magicalvibes/service/effect/CardSpecificResolutionService.java`
- Land-tap triggered handling (e.g. Manabarbs / Overgrowth): `magical-vibes-backend/src/main/java/com/github/laxika/magicalvibes/service/GameHelper.java` (`checkLandTapTriggers`)
- Win conditions: `magical-vibes-backend/src/main/java/com/github/laxika/magicalvibes/service/effect/WinConditionResolutionService.java`

## Canonical card examples

- Burn spell: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/s/Shock.java`
- Multi-effect targeted spell: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/c/Condemn.java`
- Effect composition in activated ability: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/o/OrcishArtillery.java`
- Spell-copy targeting stack: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/t/Twincast.java`
- Aura static lock: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/p/Pacifism.java`
- Static "can't block" creature: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/s/SpinelessThug.java`
- ETB token + activated cost/effect composition: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/s/SiegeGangCommander.java`
- ETB control handoff + upkeep drawback: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/s/SleeperAgent.java`
- Opponent draw trigger damage: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/u/UnderworldDreams.java`
- Conditional self cast-cost reduction: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/a/AvatarOfMight.java`
- Evasion blocked-only-by-wall-or-flying: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/e/ElvenRiders.java`
