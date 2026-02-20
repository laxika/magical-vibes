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
- `destroy target permanent`: `DestroyTargetPermanentEffect`
- `destroy all creatures/artifacts/enchantments`: `DestroyAllCreaturesEffect`, `DestroyAllArtifactsEffect`, `DestroyAllEnchantmentsEffect`
- `destroy all creatures you don't control`: `DestroyAllCreaturesYouDontControlEffect`
- `sacrifice creature`: `SacrificeCreatureEffect`, `EachOpponentSacrificesCreatureEffect`
- `counter spell`: `CounterSpellEffect`, `CounterUnlessPaysEffect`
- `return target permanent/creature`: `ReturnTargetPermanentToHandEffect`, `ReturnTargetCreatureToHandEffect`
- `return all creatures to hand`: `ReturnCreaturesToOwnersHandEffect`
- `draw cards`: `DrawCardEffect`, `DrawCardForTargetPlayerEffect`
- `discard`: `DiscardCardEffect`, `TargetPlayerDiscardsEffect`, `RandomDiscardEffect`
- `mill`: `MillTargetPlayerEffect`, `MillHalfLibraryEffect`, `MillByHandSizeEffect`
- `search library`: `SearchLibraryForCardToHandEffect`, `SearchLibraryForBasicLandToHandEffect`
- `shuffle into library`: `ShuffleIntoLibraryEffect`, `ShuffleGraveyardIntoLibraryEffect`
- `create creature tokens`: `CreateCreatureTokenEffect`, `CreateCreatureTokenWithColorsEffect`
- `gain life`: `GainLifeEffect`, `GainLifePerGraveyardCardEffect`, `GainLifeEqualToTargetToughnessEffect`
- `lose life / drain`: `TargetPlayerLosesLifeAndControllerGainsLifeEffect`, `EnchantedCreatureControllerLosesLifeEffect`
- `pump target/self/all`: `BoostTargetCreatureEffect`, `BoostSelfEffect`, `BoostAllOwnCreaturesEffect`, `BoostAllCreaturesXEffect`
- `grant keyword`: `GrantKeywordToTargetEffect`, `GrantKeywordToSelfEffect`, `GrantKeywordToEnchantedCreatureEffect`
- `tap/untap`: `TapTargetCreatureEffect`, `TapTargetPermanentEffect`, `TapOrUntapTargetPermanentEffect`, `UntapTargetPermanentEffect`, `UntapSelfEffect`
- `prevent damage`: `PreventDamageToTargetEffect`, `PreventNextDamageEffect`, `PreventAllCombatDamageEffect`, `PreventDamageFromColorsEffect`
- `copy or retarget spell`: `CopySpellEffect`, `ChangeTargetOfTargetSpellWithSingleTargetEffect`
- `extra turn / additional combat / end turn`: `ExtraTurnEffect`, `AdditionalCombatMainPhaseEffect`, `EndTurnEffect`
- `equip`: `EquipEffect`

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
- Prevention: `magical-vibes-backend/src/main/java/com/github/laxika/magicalvibes/service/PreventionResolutionService.java`
- Turn effects: `magical-vibes-backend/src/main/java/com/github/laxika/magicalvibes/service/TurnResolutionService.java`
- Copy/retarget: `magical-vibes-backend/src/main/java/com/github/laxika/magicalvibes/service/CopyResolutionService.java`, `magical-vibes-backend/src/main/java/com/github/laxika/magicalvibes/service/TargetRedirectionResolutionService.java`
- Exile target permanent: `magical-vibes-backend/src/main/java/com/github/laxika/magicalvibes/service/ExileResolutionService.java`
- Card-specific one-offs: `magical-vibes-backend/src/main/java/com/github/laxika/magicalvibes/service/effect/CardSpecificResolutionService.java`
- Win conditions: `magical-vibes-backend/src/main/java/com/github/laxika/magicalvibes/service/effect/WinConditionResolutionService.java`

## Canonical card examples

- Burn spell: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/s/Shock.java`
- Multi-effect targeted spell: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/c/Condemn.java`
- Effect composition in activated ability: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/o/OrcishArtillery.java`
- Spell-copy targeting stack: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/t/Twincast.java`
- Aura static lock: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/p/Pacifism.java`
- ETB token + activated cost/effect composition: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/s/SiegeGangCommander.java`
