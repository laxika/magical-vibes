# EFFECTS_INDEX

Purpose: cut token usage when implementing cards by quickly mapping "card text intent" to existing reusable effects and their constructor signatures.

## How to use this index

1. Parse card text into primitive actions (damage, draw, bounce, etc.).
2. Find each primitive below in the categorized sections and reuse existing effects.
3. Only add new effect records when no existing effect can express the behavior.
4. If you add a new effect record, add an `@HandlesEffect`-annotated resolver method in the matching `*ResolutionService` (see provider map at bottom). No manual registration needed — the annotation auto-registers the handler at startup.
5. If your new effect targets something, override the appropriate `canTarget*()` method(s) on `CardEffect` to return `true` (see targeting section below).
6. If your new effect requires target validation, add a `@ValidatesTarget`-annotated method in the appropriate validator class under `service/validate/` (see target validator map at bottom).

## Effect targeting declarations

Effects declare what they can target via default methods on `CardEffect`. `Card.isNeedsTarget()` and `Card.isNeedsSpellTarget()` are derived automatically — never call `setNeedsTarget`/`setNeedsSpellTarget`.

When creating a new effect, override the relevant method(s) to return `true`:

| Method | Returns `true` on these effects |
|--------|---------------------------------|
| `canTargetPlayer()` | DealDamageToAnyTargetEffect, DealDamageToAnyTargetAndGainLifeEffect, DealDamageToTargetPlayerEffect, DealDamageToTargetPlayerByHandSizeEffect, DealOrderedDamageToAnyTargetsEffect, DealXDamageToAnyTargetEffect, DealXDamageToAnyTargetAndGainXLifeEffect, TargetPlayerLosesLifeAndControllerGainsLifeEffect, TargetPlayerGainsLifeEffect, DoubleTargetPlayerLifeEffect, TargetPlayerDiscardsEffect, ChooseCardFromTargetHandToDiscardEffect, ChooseCardsFromTargetHandToTopOfLibraryEffect, LookAtHandEffect, HeadGamesEffect, RedirectDrawsEffect, MillTargetPlayerEffect, MillTargetPlayerByChargeCountersEffect, MillHalfLibraryEffect, ExtraTurnEffect, SacrificeCreatureEffect, SacrificeAttackingCreaturesEffect, ShuffleGraveyardIntoLibraryEffect, RevealTopCardDealManaValueDamageEffect, RevealTopCardOfLibraryEffect, ReturnArtifactsTargetPlayerOwnsToHandEffect, TargetPlayerGainsControlOfSourceCreatureEffect, PutMinusOneMinusOneCounterOnEachCreatureTargetPlayerControlsEffect |
| `canTargetPermanent()` | DealDamageToAnyTargetEffect, DealDamageToAnyTargetAndGainLifeEffect, DealDamageToTargetCreatureEffect, DealDamageToTargetCreatureEqualToControlledSubtypeCountEffect, DealOrderedDamageToAnyTargetsEffect, DealXDamageToAnyTargetEffect, DealXDamageToAnyTargetAndGainXLifeEffect, DealXDamageToTargetCreatureEffect, DealXDamageDividedAmongTargetAttackingCreaturesEffect, FirstTargetDealsPowerDamageToSecondTargetEffect, DestroyTargetPermanentEffect, DestroyTargetLandAndDamageControllerEffect, DestroyCreatureBlockingThisEffect, ExileTargetPermanentEffect, ReturnTargetPermanentToHandEffect, PutTargetOnBottomOfLibraryEffect, GainControlOfTargetCreatureUntilEndOfTurnEffect, GainControlOfEnchantedTargetEffect, GainControlOfTargetAuraEffect, BoostTargetCreatureEffect, BoostFirstTargetCreatureEffect, GainLifeEqualToTargetToughnessEffect, PreventDamageToTargetEffect, TapTargetPermanentEffect, TapOrUntapTargetPermanentEffect, UntapTargetPermanentEffect, MakeTargetUnblockableEffect, TargetCreatureCantBlockThisTurnEffect, ChangeColorTextEffect, EquipEffect, CantBlockSourceEffect, SacrificeCreatureCost, DestroyTargetAndControllerLosesLifePerCreatureDeathsEffect, GrantKeywordEffect (when scope == Scope.TARGET), GrantChosenKeywordToTargetEffect, PutMinusOneMinusOneCounterOnTargetCreatureEffect, UnattachEquipmentFromTargetPermanentsEffect, ExileTargetPermanentAndReturnAtEndStepEffect |
| `canTargetSpell()` | CounterSpellEffect, CounterUnlessPaysEffect, CopySpellEffect, ChangeTargetOfTargetSpellWithSingleTargetEffect |
| `canTargetGraveyard()` | ReturnCardFromGraveyardEffect (when targetGraveyard=true), PutCardFromOpponentGraveyardOntoBattlefieldEffect |

Effects that target both players and permanents (any-target): DealDamageToAnyTargetEffect, DealDamageToAnyTargetAndGainLifeEffect, DealOrderedDamageToAnyTargetsEffect, DealXDamageToAnyTargetEffect, DealXDamageToAnyTargetAndGainXLifeEffect.

## Wrapper / modifier effects

| Effect | Constructor | Description |
|--------|-------------|-------------|
| `MayEffect` | `(CardEffect wrapped, String prompt)` | Wraps any effect with "you may" choice |
| `MayPayManaEffect` | `(String manaCost, CardEffect wrapped, String prompt)` | Wraps any effect with "you may pay {X}. If you do, [effect]" choice. The mana cost is charged before resolving. Used for Spellbomb cycle and similar cards |
| `MetalcraftConditionalEffect` | `(CardEffect wrapped)` | Wraps any effect with metalcraft condition (3+ artifacts). For ETB triggers: checked at trigger time and resolution time, delegates targeting to wrapped effect. For static effects: wraps GrantKeywordEffect or StaticBoostEffect, applied only while metalcraft is met (selfOnly handler) |
| `MetalcraftReplacementEffect` | `(CardEffect baseEffect, CardEffect metalcraftEffect)` | Picks between base and upgraded effect at resolution based on metalcraft. Resolves `metalcraftEffect` if 3+ artifacts, otherwise `baseEffect`. Targeting delegates to both inner effects (union). No new handler needed — unwrapped in `EffectResolutionService` |

---

## Damage

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `DealDamageToAnyTargetEffect` | `(int damage, boolean cantRegenerate)` | deal N damage to any target |
| `DealDamageToTargetCreatureEffect` | `(int damage)` | deal N damage to target creature |
| `DealDamageToTargetPlayerEffect` | `(int damage)` | deal N damage to target player |
| `DealDamageToTargetPlayerByHandSizeEffect` | `()` | deal damage equal to hand size to target player |
| `MassDamageEffect` | `(int damage)` or `(int damage, boolean damagesPlayers)` or `(int damage, boolean usesXValue, boolean damagesPlayers, PermanentPredicate filter)` | deal N damage to all creatures (optionally filtered by predicate), optionally to all players too. Use `usesXValue=true` to use X value instead of fixed damage |
| `DealDamageToAnyTargetAndGainLifeEffect` | `(int damage, int lifeGain)` | deal N damage and gain M life |
| `DealDamageToControllerEffect` | `(int damage)` | deal N damage to the card's controller (pain lands, self-damage) |
| `DealDamageToDiscardingPlayerEffect` | `(int damage)` | deal N damage to any player who discards (trigger) |
| `DealDamageToTargetCreatureEqualToControlledSubtypeCountEffect` | `(CardSubtype subtype)` | deal damage to target creature equal to number of controlled permanents of subtype |
| `DealDamageIfFewCardsInHandEffect` | `(int maxCards, int damage)` | deal N damage to target player if they have maxCards or fewer in hand |
| `DealDamageOnLandTapEffect` | `(int damage)` | deal N damage to a player whenever they tap a land (Manabarbs-style) |
| `DealOrderedDamageToAnyTargetsEffect` | `(List<Integer> damageAmounts)` | deal different amounts to multiple targets (e.g. 3 then 1) |
| `DealXDamageToAnyTargetEffect` | `()` | deal X damage to any target (X spell) |
| `DealXDamageToAnyTargetAndGainXLifeEffect` | `()` | deal X damage and gain X life (X spell) |
| `DealXDamageToTargetCreatureEffect` | `()` | deal X damage to target creature (X spell) |
| `DealXDamageDividedAmongTargetAttackingCreaturesEffect` | `()` | deal X damage divided among attacking creatures |
| `FirstTargetDealsPowerDamageToSecondTargetEffect` | `()` | first target creature deals damage equal to its power to second target creature (bite mechanic) |
| `DoubleDamageEffect` | `()` | double all damage dealt (static) |
| `SacrificeOtherCreatureOrDamageEffect` | `(int damage)` | sacrifice another creature or take N damage (upkeep trigger) |
| `DealDamageToAnyTargetOnArtifactCastEffect` | `(int manaCost, int damage)` | trigger descriptor: when you cast an artifact spell, may pay {N} to deal N damage to any target. Place in `ON_ANY_PLAYER_CASTS_SPELL` slot wrapped in `MayEffect`. Resolves into `DealDamageToAnyTargetEffect` |

## Destruction / sacrifice

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `DestroyTargetPermanentEffect` | `(boolean cannotBeRegenerated)` | destroy target permanent |
| `DestroyAllPermanentsEffect` | `(Set<CardType> targetTypes)` or `(Set<CardType> targetTypes, boolean cannotBeRegenerated)` or `(Set<CardType> targetTypes, boolean onlyOpponents, boolean cannotBeRegenerated)` or `(Set<CardType> targetTypes, boolean onlyOpponents, boolean cannotBeRegenerated, PermanentPredicate filter)` | destroy all permanents of given types. Optional predicate filter for additional conditions |
| `DestroyTargetLandAndDamageControllerEffect` | `(int damage)` | destroy target land and deal N to its controller |
| `DestroyBlockedCreatureAndSelfEffect` | `()` | destroy creature this blocks and itself (Deathtrap-style) |
| `DestroyCreatureBlockingThisEffect` | `()` | destroy creature that blocks this (combat trigger) |
| `DestroyTargetCreatureAndGainLifeEqualToToughnessEffect` | `()` | destroy target creature and gain life equal to its toughness (combat trigger, life gain occurs even if destroy fails). Works with both ON_BLOCK and ON_BECOMES_BLOCKED slots |
| `DestroyTargetAndControllerLosesLifePerCreatureDeathsEffect` | `()` | destroy target creature; its controller loses life equal to the number of creatures put into all graveyards from the battlefield this turn (counts ALL players' creature deaths). Used with `SacrificeCreatureCost` for Flesh Allergy |
| `SacrificeCreatureEffect` | `()` | controller sacrifices a creature |
| `SacrificeAttackingCreaturesEffect` | `(int baseCount, int metalcraftCount)` | target player sacrifices attacking creatures; metalcraft upgrades count |
| `EachOpponentSacrificesCreatureEffect` | `()` | each opponent sacrifices a creature |
| `SacrificeSelfEffect` | `()` | sacrifice this permanent |
| `SacrificeUnlessDiscardCardTypeEffect` | `(CardType requiredType)` | sacrifice unless you discard a card of type (null = any) |
| `SacrificeUnlessReturnOwnPermanentTypeToHandEffect` | `(CardType permanentType)` | sacrifice this permanent unless you return a permanent of the specified type you control to its owner's hand (ETB bounce-or-sacrifice, e.g. Glint Hawk) |
| `SacrificeAtEndOfCombatEffect` | `()` | sacrifice at end of combat |

### Sacrifice costs (for activated abilities)

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `SacrificeSelfCost` | `()` | sacrifice this permanent as cost |
| `SacrificeCreatureCost` | `()` | sacrifice a creature as cost |
| `SacrificeSubtypeCreatureCost` | `(CardSubtype subtype)` | sacrifice a creature of specific subtype as cost |
| `SacrificeArtifactCost` | `()` | sacrifice an artifact as cost |
| `SacrificeAllCreaturesYouControlCost` | `()` | sacrifice all creatures you control as cost |
| `DiscardCardTypeCost` | `(CardType requiredType)` | discard a card of specific type as cost |
| `RemoveCounterFromSourceCost` | `()` | remove a counter from this permanent as cost (prefers -1/-1, then +1/+1) |
| `RemoveChargeCountersFromSourceCost` | `(int count)` | remove N charge counters from source as cost (e.g. "Remove three charge counters: ..."). Validated and paid in `AbilityActivationService` |

## Counter spells

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `CounterSpellEffect` | `()` | counter target spell |
| `CounterUnlessPaysEffect` | `(int amount)` | counter unless controller pays N generic mana |
| `CreatureSpellsCantBeCounteredEffect` | `()` | creature spells can't be countered (static) |

## Bounce / return to hand

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `ReturnTargetPermanentToHandEffect` | `()` | return target permanent to owner's hand |
| `ReturnCreaturesToOwnersHandEffect` | `(Set<TargetFilter> filters)` | return all creatures matching filters to owners' hands |
| `ReturnSelfToHandEffect` | `()` | return this permanent to owner's hand |
| `ReturnSelfToHandOnCoinFlipLossEffect` | `()` | return self to hand if coin flip is lost |
| `ReturnPermanentsOnCombatDamageToPlayerEffect` | `()` | return permanents when combat damage dealt to player (Ninja-style) |
| `ReturnArtifactsTargetPlayerOwnsToHandEffect` | `()` | return all artifacts target player owns to hand |
| `BounceCreatureOnUpkeepEffect` | `(Scope scope, Set<TargetFilter> filters, String prompt)` | at upkeep, return a creature matching filters. Scope: `SOURCE_CONTROLLER`, `TRIGGER_TARGET_PLAYER` |
| `ReturnDamageSourcePermanentToHandEffect` | `()` | whenever a permanent deals damage to controller, return it to owner's hand (Dissipation Field-style). Use with `ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU` slot |
| `PutTargetOnBottomOfLibraryEffect` | `()` | put target permanent on bottom of owner's library |

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
    PermanentPredicate attachmentTarget      // non-null = aura attaches to matching permanent on ETB
)
```

**Convenience constructors:**

| Constructor | Equivalent canonical | When to use |
|-------------|---------------------|-------------|
| `(destination, filter)` | `(destination, filter, CONTROLLERS_GRAVEYARD, false, false, false, null)` | choose one from controller's graveyard (most common) |
| `(destination, filter, source)` | `(destination, filter, source, false, false, false, null)` | choose one from a specific scope (e.g. ALL_GRAVEYARDS) |
| `(destination, filter, targetGraveyard)` | `(destination, filter, CONTROLLERS_GRAVEYARD, targetGraveyard, false, false, null)` | targets graveyard at cast time (for spells like Recollect, Recover) |

**CardPredicate filter system** (in `model/filter/`):

| Predicate | Constructor | Matches |
|-----------|-------------|---------|
| `CardTypePredicate` | `(CardType cardType)` | cards of a given type (CREATURE, ARTIFACT, etc.) |
| `CardSubtypePredicate` | `(CardSubtype subtype)` | cards of a given subtype (ZOMBIE, GOBLIN, etc.) |
| `CardKeywordPredicate` | `(Keyword keyword)` | cards with a given keyword (INFECT, FLYING, etc.) |
| `CardIsSelfPredicate` | `()` | only the source card itself (Squee-style self-return) |
| `CardIsAuraPredicate` | `()` | aura cards |
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
| Squee, Goblin Nabob | `ReturnCardFromGraveyardEffect(HAND, new CardIsSelfPredicate(), CONTROLLERS_GRAVEYARD, false, true, false, null)` — self-return |
| No Rest for the Wicked | `ReturnCardFromGraveyardEffect(HAND, new CardTypePredicate(CREATURE), CONTROLLERS_GRAVEYARD, false, true, true, null)` — all creatures that died this turn |

### Other graveyard effects

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `PutCardFromOpponentGraveyardOntoBattlefieldEffect` | `(boolean tapped)` | put target artifact/creature with MV=X from opponent's graveyard onto battlefield under your control (tapped if `tapped=true`), then mill that player X cards |
| `PutImprintedCreatureOntoBattlefieldEffect` | `()` | when this creature dies, reveal imprinted card; if creature, put onto battlefield (Clone Shell dies trigger) |

## Draw / discard / hand manipulation

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `DrawCardEffect` | `(int amount)` | draw N cards |
| `DrawCardForTargetPlayerEffect` | `(int amount, boolean requireSourceUntapped)` | target player draws N cards; optionally requires source untapped |
| `DrawCardsEqualToChargeCountersOnSourceEffect` | `()` | draw cards equal to charge counters on source (reads snapshotted count from xValue) |
| `DrawAndLoseLifePerSubtypeEffect` | `(CardSubtype subtype)` | draw cards and lose life for each permanent of subtype you control |
| `DiscardCardEffect` | `(int amount)` | discard N cards |
| `TargetPlayerDiscardsEffect` | `(int amount)` | target player discards N cards |
| `RandomDiscardEffect` | `(int amount)` | discard N cards at random |
| `ChooseCardFromTargetHandToDiscardEffect` | `(int count, List<CardType> excludedTypes)` | choose N cards from target's hand to discard (excludedTypes can't be chosen) |
| `ChooseCardsFromTargetHandToTopOfLibraryEffect` | `(int count)` | choose N cards from target hand to put on top of library |
| `LookAtHandEffect` | `()` | look at target player's hand |
| `RevealOpponentHandsEffect` | `()` | reveal all opponents' hands |
| `HeadGamesEffect` | `()` | exchange target player's hand with cards from your library (Head Games) |
| `RedirectDrawsEffect` | `()` | redirect opponent's draws to controller (static, e.g. Plagiarize-style) |

## Library manipulation

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `SearchLibraryForCardToHandEffect` | `()` | search library for any card to hand |
| `SearchLibraryForBasicLandToHandEffect` | `()` | search library for basic land to hand |
| `SearchLibraryForCardTypesToHandEffect` | `(Set<CardType> cardTypes)` | search library for card of specific types to hand |
| `SearchLibraryForCardTypesToBattlefieldEffect` | `(Set<CardType> cardTypes, boolean requiresBasicSupertype, boolean entersTapped)` | search library for card to battlefield |
| `SearchLibraryForCreatureWithMVXOrLessToHandEffect` | `()` | search library for creature with MV X or less to hand |
| `PayManaAndSearchLibraryForCardNamedToBattlefieldEffect` | `(String manaCost, String cardName)` | pay mana, search for named card to battlefield |
| `LookAtTopCardsMayRevealCreaturePutIntoHandRestOnBottomEffect` | `(int count, Set<CardType> cardTypes)` | look at top N, may reveal matching type to hand, rest on bottom |
| `ImprintFromTopCardsEffect` | `(int count)` | look at top N cards, exile one face down (imprint on source), rest on bottom in any order |
| `LookAtTopCardsHandTopBottomEffect` | `(int count)` | look at top N cards, choose hand/top/bottom for each |
| `ReorderTopCardsOfLibraryEffect` | `(int count)` | reorder top N cards of library |
| `RevealTopCardDealManaValueDamageEffect` | `(boolean damageTargetPlayer, boolean damageTargetCreatures, boolean returnToHandIfLand)` | reveal top card of target's library, deal mana value damage to player/creatures, optionally return to hand if land |
| `RevealTopCardOfLibraryEffect` | `()` | reveal top card of library (static/continuous) |
| `ShuffleIntoLibraryEffect` | `()` | shuffle this permanent into owner's library |
| `ShuffleGraveyardIntoLibraryEffect` | `()` | shuffle graveyard into library |

## Mill

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `MillTargetPlayerEffect` | `(int count)` | target player mills N cards |
| `MillHalfLibraryEffect` | `()` | target player mills half their library |
| `MillByHandSizeEffect` | `()` | target player mills cards equal to hand size |
| `MillTargetPlayerByChargeCountersEffect` | `()` | target player mills X cards where X is charge counters on source (reads snapshotted count from xValue) |

## Exile

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `ExileTargetPermanentEffect` | `()` | exile target permanent |
| `ExileCardsFromGraveyardEffect` | `(int maxTargets, int lifeGain)` | exile up to N cards from graveyard, gain lifeGain per card |
| `ExileCreaturesFromGraveyardAndCreateTokensEffect` | `()` | exile creature cards from graveyard, create tokens for each |
| `ExileTopCardsRepeatOnDuplicateEffect` | `(int count)` | exile top N cards, repeat if duplicate names found |
| `ExileSelfAndReturnAtEndStepEffect` | `()` | exile this permanent, return it at beginning of next end step (Argent Sphinx-style) |
| `ExileTargetPermanentAndReturnAtEndStepEffect` | `()` | exile target permanent, return it at beginning of next end step under owner's control (Glimmerpoint Stag-style) |

## Tokens

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `CreateCreatureTokenEffect` | `(String tokenName, int power, int toughness, CardColor color, List<CardSubtype> subtypes, Set<Keyword> keywords, Set<CardType> additionalTypes)` or `(int amount, ...)` or multi-color: `(int amount, String tokenName, int power, int toughness, CardColor color, Set<CardColor> colors, List<CardSubtype> subtypes)` or `(String tokenName, int power, int toughness, CardColor color, Set<CardColor> colors, List<CardSubtype> subtypes)` | create N creature tokens. `color` is primary display color. `colors` (Set&lt;CardColor&gt;, nullable) is full color identity for multi-color tokens. Multi-color constructors default keywords/additionalTypes to empty sets |
| `LivingWeaponEffect` | `()` | living weapon ETB: create 0/0 black Phyrexian Germ token and attach this equipment to it (resolved by PermanentControlResolutionService) |

## Life

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `GainLifeEffect` | `(int amount)` | gain N life |
| `GainLifeForEachSubtypeOnBattlefieldEffect` | `(CardSubtype subtype)` | gain 1 life per permanent with given subtype on the battlefield (all players) |
| `GainLifePerControlledCreatureEffect` | `()` | gain 1 life per creature you control |
| `GainLifePerCreatureOnBattlefieldEffect` | `()` | gain 1 life per creature on the battlefield (all players) |
| `GainLifePerGraveyardCardEffect` | `()` | gain life equal to cards in graveyard |
| `GainLifeEqualToTargetToughnessEffect` | `()` | gain life equal to target creature's toughness |
| `GainLifeEqualToToughnessEffect` | `()` | gain life equal to own toughness (self, e.g. dies trigger) |
| `GainLifeEqualToDamageDealtEffect` | `()` | gain life equal to damage dealt (lifelink-style, static) |
| `GainLifeEqualToChargeCountersOnSourceEffect` | `()` | gain life equal to number of charge counters on source (activated ability sacrifice effect) |
| `GainLifeOnColorSpellCastEffect` | `(CardColor triggerColor, int amount)` | gain N life when a spell of that color is cast (trigger, wrap in MayEffect) |
| `TargetPlayerGainsLifeEffect` | `(int amount)` | target player gains N life |
| `DoubleTargetPlayerLifeEffect` | `()` | double target player's life total |
| `LoseLifeEffect` | `(int amount)` | lose N life |
| `EachOpponentLosesLifeEffect` | `(int amount)` | each opponent loses N life |
| `EachOpponentLosesXLifeAndControllerGainsLifeLostEffect` | `()` | each opponent loses X life, controller gains total life lost |
| `TargetPlayerLosesLifeAndControllerGainsLifeEffect` | `(int lifeLoss, int lifeGain)` | drain: target loses N, you gain M |
| `EnchantedCreatureControllerLosesLifeEffect` | `(int amount, UUID affectedPlayerId)` | enchanted creature's controller loses N life (trigger) |
| `EachPlayerLosesLifePerCreatureControlledEffect` | `(int lifePerCreature)` | each player loses N life per creature they control |

## Win / lose game

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `TargetPlayerLosesGameEffect` | `(UUID playerId)` | target player loses the game |
| `LoseGameIfNotCastFromHandEffect` | `()` | lose the game if not cast from hand (ETB check) |
| `WinGameIfCreaturesInGraveyardEffect` | `(int threshold)` | win if N+ creature cards in graveyard |
| `CantLoseGameEffect` | `()` | you can't lose and opponents can't win (static) |

## Creature pump / boost

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `BoostTargetCreatureEffect` | `(int powerBoost, int toughnessBoost)` | target creature gets +X/+Y until end of turn |
| `BoostSelfEffect` | `(int powerBoost, int toughnessBoost)` | this creature gets +X/+Y until end of turn |
| `BoostAllOwnCreaturesEffect` | `(int powerBoost, int toughnessBoost)` or `(int powerBoost, int toughnessBoost, PermanentPredicate filter)` | all your creatures get +X/+Y until end of turn (one-shot). Optional predicate filter |
| `StaticBoostEffect` | `(int powerBoost, int toughnessBoost, Set<Keyword> grantedKeywords, GrantScope scope, PermanentPredicate filter)` | unified static boost: +X/+Y and keywords with predicate-based filtering. Scope: `OWN_CREATURES`, `ALL_CREATURES`. Filter: optional `PermanentPredicate` (color, subtype, not, etc). Convenience constructors: `(p, t, scope)`, `(p, t, scope, filter)`, `(p, t, keywords, scope)` |
| `BoostAllCreaturesXEffect` | `(int powerMultiplier, int toughnessMultiplier)` or `(int powerMultiplier, int toughnessMultiplier, PermanentPredicate filter)` | all creatures get +X/+X where X is mana paid. Optional `PermanentPredicate filter` to restrict which creatures are affected |
| `BoostAttachedCreatureEffect` | `(int powerBoost, int toughnessBoost)` | enchanted/equipped creature gets +X/+Y (static, works for both auras and equipment) |
| `BoostEnchantedCreaturePerControlledSubtypeEffect` | `(CardSubtype subtype, int powerPerSubtype, int toughnessPerSubtype)` | enchanted creature gets +X/+Y per controlled subtype |
| `BoostByOtherCreaturesWithSameNameEffect` | `(int powerPerCreature, int toughnessPerCreature)` | +X/+Y per other creature with same name (static) |
| `BoostBySharedCreatureTypeEffect` | `()` | +1/+1 for each other creature sharing a creature type (static) |
| `BoostFirstTargetCreatureEffect` | `(int powerBoost, int toughnessBoost)` | first target creature in multi-target spell gets +X/+Y until end of turn |
| `BoostSelfPerEnchantmentOnBattlefieldEffect` | `(int powerPerEnchantment, int toughnessPerEnchantment)` | +X/+Y per enchantment on battlefield (static) |
| `BoostSelfPerBlockingCreatureEffect` | `(int powerPerBlockingCreature, int toughnessPerBlockingCreature)` | +X/+Y for each creature blocking this (combat trigger) |
| `BoostSelfWhenBlockingKeywordEffect` | `(Keyword requiredKeyword, int powerBoost, int toughnessBoost)` | +X/+Y when blocking a creature with the required keyword (e.g. flying). Place in `ON_BLOCK` slot. CombatService converts to BoostSelfEffect at trigger time |

## P/T setting / counters

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `PowerToughnessEqualToControlledLandCountEffect` | `()` | P/T = number of lands you control (static) |
| `PowerToughnessEqualToControlledCreatureCountEffect` | `()` | P/T = number of creatures you control (static) |
| `PowerToughnessEqualToControlledPermanentCountEffect` | `(PermanentPredicate filter)` | P/T = number of permanents you control matching predicate (static). E.g. `new PermanentIsArtifactPredicate()` for artifacts |
| `PowerToughnessEqualToControlledSubtypeCountEffect` | `(CardSubtype subtype)` | P/T = number of permanents of subtype you control (static) |
| `PowerToughnessEqualToCreatureCardsInAllGraveyardsEffect` | `()` | P/T = number of creature cards in all graveyards (static) |
| `PutCountersOnSourceEffect` | `(int powerModifier, int toughnessModifier, int amount)` | put N counters on this creature (e.g. `(1,1,1)` for +1/+1, `(-1,-1,2)` for two -1/-1) |
| `PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect` | `(Set<CardColor> triggerColors, int amount, boolean onlyOwnSpells)` | put +1/+1 counters when spell of matching color is cast |
| `PutMinusOneMinusOneCounterOnEachOtherCreatureEffect` | `()` | put a -1/-1 counter on each other creature (all players' creatures except the source permanent) |
| `EnterWithXChargeCountersEffect` | `()` | enters battlefield with X charge counters (replacement effect, reads X from spell cast) |
| `PutMinusOneMinusOneCounterOnEachCreatureTargetPlayerControlsEffect` | `()` | put a -1/-1 counter on each creature target player controls (targets player) |
| `PutChargeCounterOnSelfEffect` | `()` | put a charge counter on this permanent (self-target, used as activated ability effect) |
| `PutChargeCounterOnSelfOnArtifactCastEffect` | `()` | trigger descriptor: when you cast an artifact spell, put a charge counter on this permanent. Place in `ON_ANY_PLAYER_CASTS_SPELL` slot wrapped in `MayEffect`. Resolves into `PutChargeCounterOnSelfEffect` |
| `PutMinusOneMinusOneCounterOnTargetCreatureEffect` | `()` | put a -1/-1 counter on target creature (targets permanent) |
| `ProliferateEffect` | `()` | proliferate: choose any number of permanents with counters, add one of each counter type already there |

## Keywords / abilities

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `GrantKeywordEffect` | `(Keyword keyword, GrantScope scope)` or `(Keyword keyword, GrantScope scope, PermanentPredicate filter)` | grant keyword. Scope: `SELF`, `TARGET`, `ENCHANTED_CREATURE`, `EQUIPPED_CREATURE`, `OWN_TAPPED_CREATURES`, `OWN_CREATURES`, `ALL_CREATURES`. Optional predicate filter for conditional grants |
| `GrantChosenKeywordToTargetEffect` | `(List<Keyword> options)` | on resolution, prompts controller to choose one keyword from `options`, then grants it to target permanent until end of turn. Uses COLOR_CHOICE wire protocol with `KeywordGrantChoice` context. Used by Golem Artisan |
| `GrantActivatedAbilityEffect` | `(ActivatedAbility ability, GrantScope scope, PermanentPredicate filter)` or `(ActivatedAbility ability, GrantScope scope)` | grant activated ability to permanents matching scope + filter. Supported scopes: `OWN_PERMANENTS`, `ENCHANTED_CREATURE`, `EQUIPPED_CREATURE`, `OWN_TAPPED_CREATURES`, `OWN_CREATURES`, `ALL_CREATURES`, and other creature scopes. Replaces the old `GrantActivatedAbilityToEnchantedCreatureEffect` — use `GrantScope.ENCHANTED_CREATURE` instead |
| `GrantAdditionalBlockEffect` | `(int additionalBlocks)` | can block N additional creatures |
| `RegenerateEffect` | `()` or `(boolean targetsPermanent)` | regenerate self (default) or target creature when `targetsPermanent=true` |

## Combat restrictions / evasion

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `CantBeBlockedEffect` | `()` | can't be blocked (static) |
| `CantBlockEffect` | `()` | this creature can't block (static) |
| `CantBlockSourceEffect` | `(UUID sourcePermanentId)` | target creature can't block source permanent |
| `CanBeBlockedOnlyByFilterEffect` | `(PermanentPredicate blockerPredicate, String allowedBlockersDescription)` | can only be blocked by matching creatures (static) |
| `CanBeBlockedByAtMostNCreaturesEffect` | `(int maxBlockers)` | can be blocked by at most N creatures (static) |
| `CanBlockOnlyIfAttackerMatchesPredicateEffect` | `(PermanentPredicate attackerPredicate, String allowedAttackersDescription)` | this creature can only block attackers matching predicate (static) |
| `CantAttackUnlessDefenderControlsMatchingPermanentEffect` | `(PermanentPredicate defenderPermanentPredicate, String requirementDescription)` | can't attack unless defender controls matching permanent (static) |
| `MustAttackEffect` | `()` | this creature must attack each turn if able (static) |
| `MustBeBlockedByAllCreaturesEffect` | `()` | all creatures able to block this must do so (static, Lure-style) |
| `AssignCombatDamageAsThoughUnblockedEffect` | `()` | assign combat damage as though unblocked (static) |
| `AssignCombatDamageWithToughnessEffect` | `()` | assign combat damage using toughness instead of power (static) |
| `MakeTargetUnblockableEffect` | `()` | target creature is unblockable this turn |
| `TargetCreatureCantBlockThisTurnEffect` | `()` | target creature can't block this turn |
| `EnchantedCreatureCantAttackOrBlockEffect` | `()` | enchanted creature can't attack or block (static, Pacifism-style) |
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
| `EnchantedCreatureDoesntUntapEffect` | `()` | enchanted creature doesn't untap during untap step (static) |
| `UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect` | `(TurnStep step)` | untap all your permanents during each other player's step |
| `UntapEachOtherCreatureYouControlEffect` | `()` | untap each other creature you control (ON_ATTACK trigger) |
| `UnattachEquipmentFromTargetPermanentsEffect` | `()` | unattach all equipment from target permanents (multi-target) |

## Control / steal

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `TargetPlayerGainsControlOfSourceCreatureEffect` | `()` | target opponent gains control of this creature (ETB) |
| `GainControlOfTargetCreatureUntilEndOfTurnEffect` | `()` | gain control of target creature until end of turn (Threaten-style) |
| `GainControlOfEnchantedTargetEffect` | `()` | gain control of enchanted permanent (static, Control Magic-style) |
| `GainControlOfTargetAuraEffect` | `()` | gain control of target aura |
| `ControlEnchantedCreatureEffect` | `()` | control enchanted creature (static) |

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
| `PreventAllDamageFromChosenSourceEffect` | `()` | prevent all damage a chosen source would deal to controller this turn (prompts permanent choice on resolution) |
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
| `AwardManaEffect` | `(ManaColor color)` | add one mana of specified color |
| `AwardAnyColorManaEffect` | `()` | add one mana of any color |
| `AddManaOnEnchantedLandTapEffect` | `(ManaColor color, int amount)` | when enchanted land is tapped, add N mana of color |
| `DoubleManaPoolEffect` | `()` | double your mana pool |
| `PreventManaDrainEffect` | `()` | players don't lose unspent mana as steps/phases end (static) |

## Copy / clone

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `CopyPermanentOnEnterEffect` | `(PermanentPredicate filter, String typeLabel)` | enter as copy of permanent matching filter (Clone-style) |
| `CopySpellEffect` | `()` | copy target spell |
| `ChangeTargetOfTargetSpellWithSingleTargetEffect` | `()` | change target of target spell with single target |

## Turn / phase

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `ExtraTurnEffect` | `(int count)` | take N extra turns |
| `AdditionalCombatMainPhaseEffect` | `(int count)` | get N additional combat + main phases |
| `EndTurnEffect` | `()` | end the turn |

## Animate / transform

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `AnimateLandEffect` | `(int power, int toughness, List<CardSubtype> grantedSubtypes, Set<Keyword> grantedKeywords, CardColor animatedColor)` | land becomes creature until end of turn (manlands) |
| `AnimateSelfEffect` | `(List<CardSubtype> grantedSubtypes)` | this permanent becomes a creature (e.g. Mutavault-style) |
| `AnimateSelfWithStatsEffect` | `(int power, int toughness, List<CardSubtype> grantedSubtypes, Set<Keyword> grantedKeywords)` | this permanent becomes a creature with fixed P/T and keywords until end of turn (e.g. Glint Hawk Idol) |
| `AnimateSelfByChargeCountersEffect` | `(List<CardSubtype> grantedSubtypes)` | becomes creature with P/T equal to charge counters until end of turn |
| `AnimateNoncreatureArtifactsEffect` | `()` | animate all noncreature artifacts into creatures (March of the Machines-style) |

## Enchantment-specific

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `PutAuraFromHandOntoSelfEffect` | `()` | put an aura from hand onto this creature (ETB) |
| `GainControlOfTargetAuraEffect` | `()` | gain control of target aura |
| `ChangeColorTextEffect` | `()` | change color words in enchanted permanent's text (Sleight of Mind-style) |

## Equipment

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `EquipEffect` | `()` | equip to target creature |
| `BoostAttachedCreatureEffect` | `(int powerBoost, int toughnessBoost)` | equipped creature gets +X/+Y (static) |

## Static restrictions / taxes

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `CantCastSpellTypeEffect` | `(Set<CardType> restrictedTypes)` | controller can't cast spells of specified types (static) |
| `LimitSpellsPerTurnEffect` | `(int maxSpells)` | each player can cast at most N spells per turn (static) |
| `IncreaseOpponentCastCostEffect` | `(Set<CardType> affectedTypes, int amount)` | opponent's spells of types cost N more (static) |
| `RequirePaymentToAttackEffect` | `(int amountPerAttacker)` | must pay N mana per attacking creature (static) |
| `ReduceOwnCastCostIfOpponentControlsMoreCreaturesEffect` | `(int minimumCreatureDifference, int amount)` | reduce cast cost by N if opponent has M+ more creatures |
| `NoMaximumHandSizeEffect` | `()` | you have no maximum hand size (static) |
| `EnterPermanentsOfTypesTappedEffect` | `(Set<CardType> cardTypes)` | permanents of specified types enter tapped (static) |
| `EntersTappedUnlessFewLandsEffect` | `(int maxOtherLands)` | enters tapped unless you control N or fewer other lands (fast lands, static) |

## Choose / name

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `ChooseCardNameOnEnterEffect` | `()` | choose a card name as ETB (implements `ChooseCardNameEffect` marker) |
| `ActivatedAbilitiesOfChosenNameCantBeActivatedEffect` | `()` | activated abilities of chosen name can't be activated (static) |
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

## Card-specific / one-off

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `AjaniUltimateEffect` | `()` | Ajani's ultimate: put 100 counters (planeswalker-specific) |
| `GenesisWaveEffect` | `()` | reveal top X cards, put any number of permanent cards with MV ≤ X onto battlefield, rest to graveyard. X read from `StackEntry.getXValue()` |

---

## Provider map (where to add `@HandlesEffect` resolver methods)

| Category | Resolution service |
|----------|--------------------|
| Damage | `DamageResolutionService` |
| Destruction/sacrifice | `DestructionResolutionService` |
| Bounce | `BounceResolutionService` |
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
