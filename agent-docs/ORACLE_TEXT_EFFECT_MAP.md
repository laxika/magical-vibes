# ORACLE_TEXT_EFFECT_MAP

Purpose: quickly map oracle text phrases to the correct effect class + slot. Search this file for keywords from the card's oracle text to find the matching effect without reading EFFECTS_QUICK_REFERENCE.md.

- "When [this] dies, return it to the battlefield transformed under your control at the beginning of the next end step." -> `EffectSlot.ON_DEATH` + `RegisterDelayedReturnSourceTransformedEffect()`.

## Damage

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "deals N damage to any target" | `DealDamageToAnyTargetEffect(N, false)` | SPELL | Targeting auto-derived |
| "deals N damage to the player or planeswalker it's attacking" | `DealDamageToAttackedTargetEffect(N)` | `ON_ALLY_CREATURE_ATTACKS`/attack trigger | Uses the attacking creature's `attackedTargetId` context captured by `CombatAttackService`; not a targeted ability |
| "Whenever a creature attacks you or a planeswalker you control, reveal the top card... if it's a [type], remove that creature from combat. Then put... on the bottom" | `RevealTopCardRemoveTargetFromCombatIfMatchEffect(CardPredicate)` | `ON_CREATURE_ATTACKS_YOU` | Fires once per attacking creature on the defending player's side; the attacking creature is set as the non-targeting `targetId`. Used by Lost in the Woods (`CardSubtypePredicate(FOREST)`) |
| "deals N damage to target creature" | `DealDamageToTargetCreatureEffect(N)` | SPELL | Creature-only targeting |
| "deals N damage to target creature or planeswalker" | `DealDamageToTargetCreatureOrPlaneswalkerEffect(N)` | SPELL | |
| "deals N damage to target opponent or planeswalker" | `DealDamageToTargetOpponentOrPlaneswalkerEffect(N)` | SPELL | |
| "deals N damage to target opponent and N damage to up to M target creatures that player controls" | `DealDamageToTargetOpponentAndUpToCreaturesThatPlayerControlsEffect(N, N, M)` | `ON_TRANSFORM_TO_BACK_FACE` | Two-step transform trigger target choice; use M=1 for "up to one" |
| "deals N damage to target player" | `DealDamageToTargetPlayerEffect(N)` | SPELL | |
| "deals N damage to each opponent" | `DealDamageToEachOpponentEffect(N)` | SPELL/trigger | No targeting. Amount evaluates once — for dynamic amounts pass a `DynamicAmount`, e.g. `new CountersOnSource(PLUS_ONE_PLUS_ONE)` (Hallar). NOT for per-opponent amounts (Molten Psyche keeps its own record) |
| "deals N damage to each player" | `DealDamageToEachPlayerEffect(N)` | SPELL | No targeting |
| "deals N damage to each creature" | `MassDamageEffect(N)` | SPELL | No targeting |
| "deals N damage to each creature and each planeswalker" | `MassDamageEffect(N, false, false, true, null)` | SPELL | damagesPlaneswalkers=true |
| "deals X damage to any target" | `DealDamageToAnyTargetEffect(new XValue())` | SPELL | X-cost; also cost-snapshotted X (Fling's sacrificed power, Soulblast). Add `(…, false, true)` for "if it would die this turn, exile it instead" (Red Sun's Zenith) |
| "deals X damage to target creature" | `DealDamageToTargetCreatureEffect(new XValue())` | SPELL | X-cost; also cost-snapshotted X (Corpse Lunge, Harvest Pyre) |
| "deals damage equal to its power to any target" (ability/trigger) | `DealDamageToAnyTargetEffect(new SourcePower())` | ability/trigger | Spikeshot Elder, Flayer of the Hatebound. Uses live source or last-known snapshot (CR 608.2h) |
| "deals damage equal to its toughness to target creature" | `DealDamageToTargetCreatureEffect(new SourceToughness())` | ability | Steadfast Armasaur |
| "deals damage equal to the number of charge counters on it to any target" | `DealDamageToAnyTargetEffect(new CountersOnSource(CounterType.CHARGE))` | ability | Shrine of Burning Rage; sacrifice-cost sources resolve from the entry's source snapshot |
| "deals damage to target creature equal to the number of SUBTYPEs you control" | `DealDamageToTargetCreatureEffect(new PermanentCount(new PermanentHasSubtypePredicate(SUBTYPE), CountScope.CONTROLLER))` | SPELL/trigger | Seismic Strike, Spitting Earth, Firefist Adept. "…and you gain X life" (Tendrils of Corruption) = add `GainLifeEffect(sameAmount)` |
| "deals damage to target player equal to the number of TYPE cards in your graveyard" | `DealDamageToTargetPlayerEffect(new CardsInGraveyard(new CardTypePredicate(TYPE), CountScope.CONTROLLER))` | SPELL | Scrapyard Salvo |
| "deals X damage to each of up to N targets" | `DealDamageToEachTargetEffect(new XValue())` + `target(1, N)` | SPELL | Jaya's Immolating Inferno — full amount to each target, not divided |
| "deals damage equal to its power to target" | `FirstTargetDealsPowerDamageToSecondTargetEffect()` | SPELL | Bite — multi-target. Effect impl uses `gameQueryService.getPowerBasedDamage(gd, source)` — do NOT call `getEffectivePower` directly; the helper clamps negative power to 0 per CR 510.1a. |
| "target creature deals damage to itself equal to its power" | `TargetCreatureDealsPowerDamageToSelfEffect()` | SPELL | Single-target. Target is both damage source and recipient. Use `getPowerBasedDamage`, not `getEffectivePower`. |
| "fights target creature" | `FirstTargetFightsSecondTargetEffect()` | SPELL | Multi-target. Same rule: use `getPowerBasedDamage`, not `getEffectivePower`. |
| "target creature fights another target creature" | `FirstTargetFightsSecondTargetEffect()` | SPELL | Multi-target, any two creatures; distinct is the default |
| "deals N damage to you" | `DealDamageToControllerEffect(N)` | SPELL/trigger | Self-damage |

## Creature pump / boost

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "target creature gets +X/+Y until end of turn" | `BoostTargetCreatureEffect(X, Y)` | SPELL | Targeting auto-derived. Fixed `(int,int)` ctor; for dynamic pumps pass `DynamicAmount`s |
| "target creature gets +X/+X until end of turn" (X = mana paid) | `BoostTargetCreatureEffect(new XValue(), new XValue())` | SPELL | Untamed Might; `(new XValue(), new Fixed(0))` for +X/+0 (Kessig Wolf Run) |
| "target creature gets +X/+X, where X is the number of creatures you control" | `BoostTargetCreatureEffect(new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER), same)` | ability | Elder of Laurels; count resolves against the effect's controller |
| "target creature gets +X/+0 until end of turn, where X is 1 plus the number of cards named CARDNAME in your graveyard" | `BoostTargetCreatureEffect(new Sum(new Fixed(1), new CardsInGraveyard(new CardNamedPredicate("CARDNAME"), CountScope.CONTROLLER)), new Fixed(0))` + `GrantKeywordEffect(TRAMPLE, TARGET)` | SPELL | Ancestral Anger; count at resolution (spell not yet in graveyard) |
| "creatures you control get +X/+Y until end of turn" | `BoostAllOwnCreaturesEffect(X, Y)` | SPELL | No targeting; `int` ctor wraps in `Fixed` |
| "creatures you control get +X/+Y until end of turn" (with predicate) | `BoostAllOwnCreaturesEffect(X, Y, predicate)` | SPELL | Filtered |
| "creatures you control get +X/+X, where X is the greatest power among creatures you control" | `BoostAllOwnCreaturesEffect(new GreatestPowerAmongControlled(), new GreatestPowerAmongControlled())` | SPELL | Overwhelming Stampede; amount snapshotted once before boosts land |
| "creatures you control get +X/+X, where X is the number of creature cards in your graveyard" | `BoostAllOwnCreaturesEffect(new CardsInGraveyard(new CardTypePredicate(CREATURE), CONTROLLER), same)` | ability | Garruk, the Veil-Cursed |
| "all creatures get +X/+Y until end of turn" | `BoostAllCreaturesEffect(X, Y)` | SPELL | Affects all; `int` ctor wraps in `Fixed` |
| "all creatures get +X/-X (or -X/-X) until end of turn, where X was paid/sacrificed" | `BoostAllCreaturesEffect(new XValue(), new Scaled(new XValue(), -1))` / `(new Scaled(new XValue(), -1), same)` | SPELL | Flowstone Slide (+X/-X), Ichor Explosion (-X/-X); X comes from stack entry `xValue` |
| "CARDNAME gets +X/+Y until end of turn" | `BoostSelfEffect(X, Y)` | ability effect | Self-pump |
| "CARDNAME gets +N/+M for each [permanent] you control" | `BoostSelfEffect(new PermanentCount(predicate, CountScope.CONTROLLER), ...)` | STATIC (or ability/trigger for until-EOT) | Any "for each" self-boost = `BoostSelfEffect` + `DynamicAmount` (see EFFECTS_INDEX "Dynamic amounts") — never a new effect class. "each *other*" → `excludeSource=true`; opponents' → `CountScope.OPPONENTS`; whole battlefield → `ANY_PLAYER` |
| "CARDNAME gets +N/+M for each [card] in your graveyard" | `BoostSelfEffect(new CardsInGraveyard(cardPredicate, CountScope.CONTROLLER), ...)` | STATIC | Multani, Yavimaya's Avatar |
| "CARDNAME gets +N/+M for each Aura/Equipment attached to it" | `BoostSelfEffect(new Scaled(new AttachmentsOnSource(auras, equipment), N), ...)` | STATIC | Champion of the Flame, Goblin Gaveleer |
| "CARDNAME gets +X/+0 until end of turn, where X is the mana spent to cast that spell" | `BoostSelfEffect(new XValue(), new Fixed(0))` inside `SpellCastTriggerEffect` | ON_CONTROLLER_CASTS_SPELL | Aberrant Manawurm; collector snapshots xValue for amounts referencing X |
| "Whenever CARDNAME becomes blocked, it gets +N/+N for each creature blocking it" | `BoostSelfEffect(new CreaturesBlockingSource(), new CreaturesBlockingSource())` | ON_BECOMES_BLOCKED | Elvish Berserker |
| "CARDNAME's power and toughness are each equal to the number of lands you control" | `PowerToughnessEqualToControlledLandCountEffect()` | STATIC | |
| "CARDNAME's power and toughness are each equal to the number of creatures you control" | `PowerToughnessEqualToControlledCreatureCountEffect()` | STATIC | |
| "switch target creature's power and toughness" | `SwitchPowerToughnessEffect()` | SPELL | |
| "target creature has base power and toughness X/Y" | `SetBasePowerToughnessUntilEndOfTurnEffect(X, Y)` | SPELL | |
| "double target creature's power and toughness" | `DoubleSelfPowerToughnessEffect()` | — | Self only |

## Static boost (permanents on battlefield)

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "other [subtype] creatures you control get +X/+Y" | `StaticBoostEffect(X, Y, Set.of(), OWN_CREATURES, PermanentHasAnySubtypePredicate(subtype))` | STATIC | Lord |
| "creatures you control get +X/+Y" | `StaticBoostEffect(X, Y, Set.of(), OWN_CREATURES, null)` | STATIC | Anthem |
| "fateful hour — as long as you have 5 or less life, other creatures you control get +X/+Y" | `ConditionalEffect(new ControllerLifeAtMost(5), StaticBoostEffect(X, Y, OWN_CREATURES))` | STATIC | Gavony Ironwright |
| "as long as you have N or more life, [self/creatures] get +X/+Y [and keywords]" | `ConditionalEffect(new ControllerLifeAtLeast(N), StaticBoostEffect(X, Y, keywords, scope))` | STATIC | Serra Ascendant |
| "other [subtype] creatures get +X/+Y" (all players) | `StaticBoostEffect(X, Y, Set.of(), ALL_CREATURES, PermanentHasAnySubtypePredicate(subtype))` | STATIC | Global lord |
| "creatures opponents control get -X/-Y" | `StaticBoostEffect(-X, -Y, Set.of(), OPPONENT_CREATURES, null)` | STATIC | |
| "enchanted creature gets +X/+Y" | `StaticBoostEffect(X, Y, Set.of(), ENCHANTED_CREATURE, null)` | STATIC | Aura |
| "equipped creature gets +X/+Y" | `StaticBoostEffect(X, Y, Set.of(), EQUIPPED_CREATURE, null)` | STATIC | Equipment |

## Keywords / abilities

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "target creature gains [keyword] until end of turn" | `GrantKeywordEffect(Keyword.X, GrantScope.TARGET)` | SPELL | default duration is `END_OF_TURN` |
| "creatures you control gain [keyword] until end of turn" | `GrantKeywordEffect(Keyword.X, GrantScope.OWN_CREATURES)` | SPELL | |
| "target creature gains [keyword] until your next turn" | `GrantKeywordEffect(Keyword.X, GrantScope.TARGET, GrantDuration.UNTIL_YOUR_NEXT_TURN)` | SPELL | pass `GrantDuration` for non-default expiry |
| "create a ... token. It gains [keyword] until end of turn" | `CreateTokenEffect(..., innateKeywords, Set.of(Keyword.X))` (the `grantedKeywordsUntilEndOfTurn` arg) | SPELL | Artistic Process — Elemental gains haste. Keep innate keywords separate from the granted set |
| "enchanted creature has [keyword]" | `GrantKeywordEffect(Keyword.X, GrantScope.ENCHANTED_CREATURE)` | STATIC | |
| "equipped creature has [keyword]" | `GrantKeywordEffect(Keyword.X, GrantScope.EQUIPPED_CREATURE)` | STATIC | |
| "other [subtype] creatures you control have [keyword]" | `GrantKeywordEffect(Keyword.X, GrantScope.OWN_CREATURES, predicate)` | STATIC | |
| "target creature can't be blocked this turn" | `MakeCreatureUnblockableEffect()` | SPELL | |
| "regenerate target creature" | `RegenerateEffect(true)` | ability effect | targetsPermanent=true |
| "regenerate CARDNAME" | `RegenerateEffect()` | ability effect | Self |

## Destruction / sacrifice

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "destroy target [permanent type]" | `DestroyTargetPermanentEffect(false)` | SPELL | + PermanentPredicate filter |
| "destroy target creature. It can't be regenerated" | `DestroyTargetPermanentEffect(true)` | SPELL | cantRegenerate=true |
| "destroy all creatures" | `DestroyAllPermanentsEffect(PermanentIsCreaturePredicate())` | SPELL | |
| "destroy all [type]" | `DestroyAllPermanentsEffect(predicate)` | SPELL | Filtered wipe |
| "sacrifice a creature" | `SacrificeCreatureEffect()` | SPELL | Target player sacrifices |
| "sacrifice a [subtype]: [effect]" | `SacrificePermanentCost(PermanentAllOfPredicate(creature + PermanentHasSubtypePredicate(subtype)), "Sacrifice a [subtype]", false)` then effect | activated ability | Ravenous Demon front face uses `TransformSelfEffect()` with `SORCERY_SPEED` |
| "sacrifice a [subtype]. If you can't, [effects]" | `ForcedCostOrElseEffect(SacrificePermanentCost(PermanentAllOfPredicate(creature + subtype), description, false), elseEffects)` | trigger | Archdemon of Greed uses `TapSelfEffect()` + `DealDamageToControllerEffect(9)` |
| "you may sacrifice a nontoken creature. If you do, create X 2/2 Wolf tokens, where X is its toughness" | `MayEffect(SacrificeCreatureToCreateTokensEqualToToughnessEffect(template, PermanentNotPredicate(PermanentIsTokenPredicate)))` | trigger | Feed the Pack; X = sacrificed creature's toughness |
| "each opponent sacrifices a creature" | `EachOpponentSacrificesCreatureEffect()` | SPELL/trigger | |
| "sacrifice CARDNAME" | `SacrificeSelfEffect()` | trigger/ability | |

## Bounce / tuck

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "return target [permanent] to its owner's hand" | `ReturnTargetPermanentToHandEffect()` | SPELL | + filter |
| "return all creatures to their owners' hands" | `ReturnCreaturesToOwnersHandEffect(filters)` | SPELL | Mass bounce |
| "put target [permanent] on top of its owner's library" | `PutTargetOnTopOfLibraryEffect()` | SPELL | |
| "put target [permanent] on the bottom of its owner's library" | `PutTargetOnBottomOfLibraryEffect()` | SPELL | |

## Counter spells

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "counter target spell" | `CounterSpellEffect()` | SPELL | Targeting auto-derived |
| "counter target spell unless its controller pays {N}" | `CounterUnlessPaysEffect(N)` | SPELL | |
| "counter target spell. You may cast a spell that shares a card type with it from your hand without paying its mana cost" | `CounterlashEffect()` | SPELL | Queues per-card PendingMayAbility with MayCastFromHandWithoutPayingManaCostEffect |
| "this spell can't be countered" | `CantBeCounteredEffect()` | STATIC | |

## Draw / discard

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "draw N cards" / "draw a card" | `DrawCardEffect(N)` | SPELL/trigger | |
| "draw X cards" | `DrawCardEffect(new XValue())` | SPELL | X-cost |
| "draw a card for each creature you control" | `DrawCardEffect(new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER))` | trigger | Tishana |
| "draw a card for each creature card in your graveyard" | `DrawCardEffect(new CardsInGraveyard(new CardTypePredicate(CREATURE), CountScope.CONTROLLER))` | SPELL | Grim Flowering |
| "draw a card for each charge counter on [source]" | `DrawCardEffect(new CountersOnSource(CounterType.CHARGE))` | ability | Culling Dais; survives sacrifice via sourcePermanentSnapshot |
| "target player draws N cards" | `DrawCardForTargetPlayerEffect(N)` | SPELL | |
| "target player draws X cards" | `DrawCardForTargetPlayerEffect(new XValue(), false, true)` | SPELL | Blue Sun's Zenith |
| "each player draws N cards" | `EachPlayerDrawsCardEffect(N)` | SPELL | |
| "draw N cards, then discard M cards" | `DrawAndDiscardCardEffect(N, M)` | SPELL | Loot |
| "discard N cards, then draw M cards" | `DiscardAndDrawCardEffect(N, M)` | SPELL | Rummage |
| "discard up to N cards, then draw that many cards" | `DiscardUpToThenDrawThatManyEffect(N)` | SPELL/ability | Rummage with cap |
| "discard any number of cards, then draw that many cards plus one" | `DiscardUpToThenDrawThatManyEffect(ANY_NUMBER, 1)` | ON_DEATH/trigger | Colossus of the Blood Age |
| "discard all the cards in your hand, then draw that many cards" | `DiscardOwnHandThenDrawThatManyEffect()` | SPELL | Shattered Perception |
| "discard your hand, then draw cards equal to the number of cards in target opponent's hand" | `DiscardOwnHandThenDrawEqualToTargetPlayerHandSizeEffect()` | SPELL | Borrowed Knowledge (modal mode 0) |
| "discard a card" / "discard N cards" | `DiscardCardEffect(N)` | SPELL/trigger | Controller discards |
| "target player discards N cards" | `TargetPlayerDiscardsEffect(N)` | SPELL | |
| "target player discards a card for each charge counter on ~" | `TargetPlayerDiscardsEffect(new CountersOnSource(CounterType.CHARGE))` | ability | Shrine of Limitless Power (with `SacrificeSelfCost`) |
| "target player discards X cards at random" | `TargetPlayerRandomDiscardEffect(new XValue(), true)` | SPELL | Mind Shatter |
| "Converge — Target player discards X cards, where X is the number of colors of mana spent to cast this spell." | `TargetPlayerDiscardsByConvergeEffect()` | SPELL | Arcane Omens |
| "Converge — deals X damage to target creature" + excess-damage exile | `DealDamageToTargetCreatureEffect(new XValue())` + `ExileTopCardsEqualToStackEntryExcessDamageMayPlayUntilNextTurnEffect()` | SPELL | Converge keyword snapshotted to xValue; Archaic's Agony |
| "each player discards N cards" | `EachPlayerDiscardsEffect(N)` | SPELL | |
| "each opponent discards a card" | `EachOpponentDiscardsEffect(1)` | SPELL/trigger | |
| "look at target player's hand" | `LookAtHandEffect()` | SPELL | |

## Life

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "you gain N life" / "gain N life" | `GainLifeEffect(N)` | SPELL/trigger | |
| "you gain 1 life for each [permanent]" | `GainLifeEffect(new PermanentCount(predicate, scope))` | SPELL/trigger | scope: "you control" = CONTROLLER, "on the battlefield" = ANY_PLAYER; "N life for each" = wrap in `Scaled(count, N)`; independent counts summed ("each creature and each artifact", artifact creatures count twice) = `Sum(count1, count2)` (War Report ruling) |
| "you gain 1 life for each card in your hand" | `GainLifeEffect(new CardsInHand(CountScope.CONTROLLER))` | SPELL/trigger | Venser's Journal, Sword of War and Peace |
| "you gain N life for each card in your graveyard" | `GainLifeEffect(new CardsInGraveyard(cardPredicate, CountScope.CONTROLLER))` | SPELL/trigger | `null` predicate = every card; ×N via `Scaled` (Gnaw to the Bone, Archangel's Light) |
| "you gain life equal to the number of charge counters on ~" (sacrifice cost) | `GainLifeEffect(new CountersOnSource(CounterType.CHARGE))` | ability effect | resolves from the stack entry's source snapshot (last-known info) after the source is sacrificed; Golden Urn |
| "you gain life equal to the greatest power among creatures you control" | `GainLifeEffect(new GreatestPowerAmongControlled())` | ability/trigger | Huatli, Warrior Poet +2 |
| "you gain life equal to the sacrificed creature's toughness" | `GainLifeEffect(new XValue())` | ability effect | with `SacrificeCreatureCost(trackToughness)` snapshotting toughness into xValue |
| "you gain twice X life" | `GainLifeEffect(new Scaled(new XValue(), 2))` | SPELL | Sanguine Sacrament |
| "you lose N life" / "lose N life" | `LoseLifeEffect(N)` | SPELL/trigger | |
| "target player gains N life" | `TargetPlayerGainsLifeEffect(N)` | SPELL | |
| "target player loses N life" | `TargetPlayerLosesLifeEffect(N)` | SPELL | |
| "each opponent loses N life" | `EachOpponentLosesLifeEffect(N)` | SPELL/trigger | |
| "each opponent loses N life and you gain life equal to the life lost" | `EachOpponentLosesLifeAndControllerGainsLifeLostEffect(N)` | SPELL | Drain |
| "whenever you gain life, draw a card" | `DrawCardEffect(1)` | ON_CONTROLLER_GAINS_LIFE | Fires once per life-gain event; see `d/DrogskolReaver.java` |
| "whenever you gain life, put a growth counter on this enchantment" | `PutCountersOnSelfEffect(CounterType.GROWTH)` | ON_CONTROLLER_GAINS_LIFE | Fires once per life-gain event; see `c/ComfortingCounsel.java` |
| "as long as there are five or more growth counters on this enchantment, creatures you control get +3/+3" | `ConditionalEffect(new SourceCounterThreshold(5, CounterType.GROWTH), StaticBoostEffect(3, 3, OWN_CREATURES))` | STATIC | |
| "double target player's life total" | `DoubleTargetPlayerLifeEffect()` | SPELL | |
| "players can't gain life" | `PlayersCantGainLifeEffect()` | STATIC | |

## Graveyard / library hate (static, global)

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "players can't cast spells from graveyards" | `PlayersCantCastSpellsFromZonesEffect(Set.of(Zone.GRAVEYARD))` | STATIC | Ashes of the Abhorrent. Gated in flashback/graveyard-cast paths via `GameQueryService.canPlayersCastSpellsFromZone(gd, Zone.GRAVEYARD)` |
| "players can't cast spells from graveyards or libraries" | `PlayersCantCastSpellsFromZonesEffect(Set.of(Zone.GRAVEYARD, Zone.LIBRARY))` | STATIC | Grafdigger's Cage. Gated via `GameQueryService.canPlayersCastSpellsFromZone(gd, zone)` (graveyard cast/flashback + `playCardFromLibraryTop`). Only `GRAVEYARD`/`LIBRARY` enforced |
| "creature cards in graveyards and libraries can't enter the battlefield" | `CardsCantEnterBattlefieldFromZonesEffect(new CardTypePredicate(CREATURE), Set.of(Zone.GRAVEYARD, Zone.LIBRARY))` | STATIC | Grafdigger's Cage. Filter selects which cards are blocked (null = all); `zones` selects which source zones are blocked (only `GRAVEYARD`/`LIBRARY` enforced). Blocks reanimation/undying + library-search-to-battlefield; gated via `GameQueryService.isCardBlockedFromEnteringFromZone(gd, card, zone)`. Blocked card stays in its zone |

## Library manipulation

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "search your library for a card" (any to hand) | `SearchLibraryForCardsToHandEffect()` | SPELL | unified search-to-hand; no-arg = unrestricted single card (Diabolic Tutor) |
| "search your library for a card... if cast from a graveyard, instead search for two cards" (to hand) | `SearchLibraryForCardsToHandEffect(null, 1, 2)` | SPELL | Increasing Ambition. Count switches on `StackEntry.isCastWithFlashback()` |
| "search your library for a basic land card, put it into your hand" | `SearchLibraryForCardsToHandEffect(CardPredicateUtils.basicLand())` | SPELL | basic land = `CardAllOf(CardSupertype BASIC, CardType LAND)`, composed via the `CardPredicateUtils.basicLand()` factory (no dedicated predicate class) |
| "search your library for a basic land card, put it onto the battlefield tapped" | `SearchLibraryForCardTypesToBattlefieldEffect(CardTypePredicate(LAND, basic), true)` | SPELL | |
| "search your library for a [type] card, reveal it, put it into your hand" | `SearchLibraryForCardsToHandEffect(predicate)` | SPELL | single-arg ctor = restricted (revealed, may fail to find) |
| "you may search your library for a Curse card, put it onto the battlefield attached to [target] player" | `MayEffect(SearchLibraryForSubtypeToBattlefieldAttachedToTargetPlayerEffect(CardSubtype.CURSE), prompt)` | trigger | Bitterheart Witch — `canTargetPlayer()=true`, target chosen at trigger time |
| "at the beginning of your upkeep, you may search your library for a Curse card that doesn't have the same name as a Curse attached to enchanted player, put it onto the battlefield attached to that player, then shuffle" | `MayEffect(SearchLibraryForCurseToBattlefieldAttachedToEnchantedPlayerEffect(), prompt)` | UPKEEP_TRIGGERED | Curse of Misfortunes — enchanted player derived from source aura's `attachedTo`; excludes Curses already attached to that player by name |
| "scry N" | `ScryEffect(N)` | SPELL/trigger | |
| "surveil N" | `SurveilEffect(N)` | SPELL/trigger | |
| "shuffle your library" | `ShuffleLibraryEffect()` | SPELL | |
| "exile cards from the top of your library until you exile cards with total mana value N or greater. You may cast any number of spells from among the exiled cards without paying their mana costs" | `ImprovisationCapstoneEffect(N)` | SPELL | `ImprovisationCapstoneCastChoice` interaction + `ImprovisationCapstoneCastSupport` |

## Mill

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "target player mills N cards" / "puts the top N cards into their graveyard" | `MillTargetPlayerEffect(N)` | SPELL | |
| "target player mills X cards" | `MillTargetPlayerEffect(new XValue())` | SPELL | For "if cast from a graveyard, twice that many" flashback spells, wrap in `ConditionalReplacementEffect(new CastFromZone(Zone.GRAVEYARD), new MillTargetPlayerEffect(new XValue()), new MillTargetPlayerEffect(new Scaled(new XValue(), 2)))` (Increasing Confusion) |
| "target player mills X cards, where X is the number of charge counters on ~" | `MillTargetPlayerEffect(new CountersOnSource(CounterType.CHARGE))` | ability | Grindclock |
| "each opponent mills N cards" | `MillEachOpponentEffect(N)` | SPELL/trigger | |
| "each player mills N cards" | `MillControllerEffect(N)` + `EachOpponentMillsEffect(N)` | SPELL/trigger | Combine two effects — no targeting. See `GhoulcallersBell`, `ChillOfForeboding` |
| "mill N cards" (self) | `MillControllerEffect(N)` | SPELL/trigger | |
| "target player mills half their library" | `MillHalfLibraryEffect()` | SPELL | |

## Exile

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "exile target [permanent]" | `ExileTargetPermanentEffect()` | SPELL | |
| "exile target [permanent]. Return it at the beginning of the next end step" | `ExileTargetPermanentEffect(true)` | SPELL | returnEndStep=true |
| "exile all creatures" | `ExileAllCreaturesEffect()` | SPELL | |
| "exile target player's graveyard" | `ExileTargetPlayerGraveyardEffect()` | SPELL | |
| "exile target noncreature, nonland card from your graveyard. Until the end of your next turn, you may cast that card" | `ExileTargetCardFromGraveyardMayPlayUntilNextTurnEffect(CardAllOfPredicate(not creature, not land), true)` | `ON_ENTER_BATTLEFIELD` | Practiced Scrollsmith. Single graveyard target chosen at trigger time; grants play-until-end-of-next-turn permission |
| "exile target nonland permanent and the top card of your library. For each of those cards, its owner may play it until the end of their next turn" | `target(nonland permanent) + ExileTargetPermanentMayPlayUntilNextTurnEffect()` and `ExileTopCardsMayPlayUntilNextTurnEffect(1)` | SPELL | Suspend Aggression. Owner-relative play permission for each exiled card |
| "Exile target instant or sorcery card from an opponent's graveyard. You may cast it this turn, and mana of any type can be spent to cast that spell. If that spell would be put into a graveyard, exile it instead. Activate only as a sorcery" | `{2}` + `SacrificeCreatureCost(false,false,false,true)` + `ExileTargetInstantOrSorceryFromOpponentGraveyardMayCastEffect()` with `ActivationTimingRestriction.SORCERY_SPEED` | activated ability | Nita, Forum Conciliator. This-turn cast permission with any-mana + exile-instead-of-graveyard riders |
| "Whenever you cast a spell you don't own, [effect]" | `SpellCastTriggerEffect(new CardControllerDoesNotOwnPredicate(), List.of(effect))` | `ON_CONTROLLER_CASTS_SPELL` | Nita, Forum Conciliator (effect = `PutCounterOnEachControlledPermanentEffect(PLUS_ONE_PLUS_ONE, 1, PermanentIsCreaturePredicate)`). Requires card ownership stamped at game setup |
| "you may exile target creature card from your graveyard. If you do, create a token that's a copy of that card, except it's a Spirit in addition to its other types. Exile it at the beginning of the next end step" | `EACH_UPKEEP_TRIGGERED` + `MayEffect(ExileTargetCardFromGraveyardAndCreateTokenCopyEffect(CardTypePredicate(CREATURE), true, List.of(SPIRIT), false, true))` | trigger | Séance. `EACH_UPKEEP_TRIGGERED` requires `MayEffect` handling in `StepTriggerService` (queues via `queueMayAbility`) |

## Tokens

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "target player creates a token that's a copy of target creature you control" | `CreateTokenCopyOfTargetCreatureForTargetPlayerEffect()` | SPELL | Two targets (player, then creature); token enters under chosen player |
| "create N 1/1 white Spirit creature tokens with flying" | `CreateTokenEffect.whiteSpirit(N)` | SPELL/trigger | Static factory |
| "Whenever another non-[Subtype] creature you control dies, create ..." | `EffectSlot.ON_ALLY_CREATURE_DIES` + `TriggeringCardConditionalEffect(new CardNotPredicate(new CardSubtypePredicate(subtype)), CreateTokenEffect...)` | trigger | Requiem Angel-style; use ally creature death, not nontoken, because token non-Spirits count |
| "create N 2/2 black Zombie creature tokens" | `CreateTokenEffect.blackZombie(N)` | SPELL/trigger | Static factory |
| "create N 1/1 white Soldier creature tokens" | `CreateTokenEffect.whiteSoldier(N)` | SPELL/trigger | Static factory |
| "create a Treasure token" | `CreateTokenEffect.ofTreasureToken(1)` | SPELL/trigger | Static factory |
| "create a N/N [color] [Subtype] creature token" | `CreateTokenEffect("name", N, N, color, subtype)` | SPELL/trigger | Custom token |
| "create a 0/0 [color] and [color] [Subtype] creature token. Put N +1/+1 counters on it" | `CreateTokenEffect("name", 0, 0, color, Set.of(colors), List.of(subtype), N)` | SPELL/trigger/ETB | `initialPlusOnePlusOneCounters` on `CreateTokenEffect` (e.g. Additive Evolution) |
| "{X}, {T}: Create a 0/0 [color] and [color] [Subtype] creature token and put X +1/+1 counters on it" | `CreateXTokenWithXCountersEffect("name", 0, 0, color, Set.of(colors), List.of(subtype), CounterType.PLUS_ONE_PLUS_ONE)` | activated ability | X from `StackEntry.getXValue()` (e.g. Berta, Wise Extrapolator) |
| "Increment (Whenever you cast a spell, if the mana you spent is greater than this creature's power or toughness, put a +1/+1 counter on it)" | *(none — keyword-driven)* | — | Increment keyword (SOS). Auto-loaded from Scryfall as `Keyword.INCREMENT`; behavior is driven by the keyword in `TriggerCollectionService.collectIncrementTriggers` (like Undying). Add **nothing** to the card (e.g. Ambitious Augmenter) |
| "When this dies, if it had one or more counters on it, create a 0/0 [color] [Subtype] token, then put this creature's counters on that token" | `CreateTokenWithDyingSourceCountersEffect(new CreateTokenEffect("name", 0, 0, color, Set.of(colors), List.of(subtype)))` | `ON_DEATH` | Snapshots dying creature's +1/+1 counters onto the new token (e.g. Ambitious Augmenter's Fractal) |
| "When this dies, if it had counters on it, put those counters on up to one target creature" | `MoveDyingSourceCountersToTargetCreatureEffect()` | `ON_DEATH` | Snapshots every counter type on the dying creature and moves them to a chosen creature (e.g. Scolding Administrator) |
| "create a N/N [color] [Subtype] creature token with [keyword]" | `CreateTokenEffect("name", N, N, color, subtype, keyword)` | SPELL/trigger | With keyword |
| "create a … token for each [thing]" / "create X … tokens, where X is …" | `CreateTokenEffect(DynamicAmount, "name", N, N, color, subtypes, keywords, additionalTypes)` | SPELL/trigger/ability | Any dynamic token count = `CreateTokenEffect` + a `DynamicAmount` (see EFFECTS_INDEX "Dynamic amounts") — never a new effect class. "for each charge counter on ~" = `CountersOnSource(CHARGE)`; "create X" = `XValue()`; "for each Equipment/Aura attached to ~" = `AttachmentsOnSource`; "for each creature you control" = `PermanentCount(IsCreature, CONTROLLER)`; "for each creature card in your graveyard" = `CardsInGraveyard(CardTypePredicate(CREATURE), CONTROLLER)`; "for each Forest you control" = `PermanentCount(HasSubtype(FOREST), CONTROLLER)`; "for each creature put into your graveyard this turn" = `CreatureDeathsThisTurn(CONTROLLER)`; "half the number of …, rounded down" = `Divided(count, 2)`. Non-default token flags (tappedAndAttacking, exileAtEndStep) use the canonical ctor |
| "create N tokens. If this spell was cast from a graveyard, create M of those tokens instead" | `CreateTokenEffect(N, ...)` + `ConditionalEffect(new CastFromZone(Zone.GRAVEYARD), new CreateTokenEffect(M - N, ...))` | SPELL | Add the base amount first, then the conditional extra amount; add `FlashbackCast` separately when appropriate |
| "Then if this spell was cast from anywhere other than your hand, [effect]" | `ConditionalEffect(new CastNotFromHand(), ...)` | SPELL | Broader than graveyard-only; covers flashback and any future non-hand cast paths. E.g. Antiquities on the Loose |

## Graveyard return

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "return target [type] card from your graveyard to your hand" | `ReturnCardFromGraveyardEffect.builder().destination(HAND).filter(predicate).targetGraveyard(true).build()` | SPELL | |
| "return target creature card from your graveyard to the battlefield" | `ReturnCardFromGraveyardEffect.builder().destination(BATTLEFIELD).filter(CardTypePredicate(CREATURE)).targetGraveyard(true).build()` | SPELL | |
| "return target card from your graveyard to the top of your library" | `ReturnCardFromGraveyardEffect.builder().destination(TOP_OF_OWNERS_LIBRARY).targetGraveyard(true).build()` | SPELL | |
| "Undying" (keyword) | none — loaded from Scryfall as `Keyword.UNDYING` | — | Engine handles the return-with-counter in `PermanentRemovalService.collectUndyingTrigger` + `UndyingReturnEffect`. Just register the printing. |
| "Whenever this creature or another creature enters from your graveyard, that creature deals damage equal to its power to any target" | `DealDamageToAnyTargetEffect(new SourcePower())` | `ON_CREATURE_ENTERS_FROM_GRAVEYARD` | Flayer of the Hatebound. The entering creature is the damage source (its power is read at resolution); any-target choice is handled by the `EntersFromGraveyardTriggerTarget` pipeline. |

## Counters

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "CARDNAME enters the battlefield with N [type] counters on it" | `EnterWithCountersEffect(CounterType.X, new Fixed(N))` | `ON_ENTER_BATTLEFIELD` | As-enters replacement effect (CR 614.1c), applied during battlefield entry before ETB triggers/statics see the permanent. Trigons/Tumble Magnet (CHARGE), Djinn of Wishes (WISH) |
| "CARDNAME enters the battlefield with X [type] counters on it" (X-cost spell) | `EnterWithCountersEffect(CounterType.X, new XValue())` | `ON_ENTER_BATTLEFIELD` | X read from the resolving spell's stack entry; 0 when the permanent wasn't cast (tokens, reanimation). Chimeric Mass, Protean Hydra, Mikaeus the Lunarch |
| "enters with a +1/+1 counter on it for each [thing]" | `EnterWithCountersEffect(PLUS_ONE_PLUS_ONE, <DynamicAmount>)` | `ON_ENTER_BATTLEFIELD` | Any "enters with … for each …" wording = this effect + an amount, never a new class. "each creature that died this turn" = `CreatureDeathsThisTurn(ANY_PLAYER)` (Bloodcrazed Paladin); "each other Zombie you control and each Zombie card in your graveyard" = `Sum(PermanentCount(HasSubtype(ZOMBIE), CONTROLLER), CardsInGraveyard(CardSubtypePredicate(ZOMBIE), CONTROLLER))` (Unbreathing Horde) |
| "If CARDNAME was kicked, it enters the battlefield with N +1/+1 counters on it" | `ConditionalEffect(new Kicked(), new EnterWithCountersEffect(PLUS_ONE_PLUS_ONE, new Fixed(N)))` | `ON_ENTER_BATTLEFIELD` | Pair with `KickerEffect` on STATIC. Academy Drake, Baloth Gorger, Grunn the Lonely King |
| "Raid — CARDNAME enters the battlefield with a +1/+1 counter on it if you attacked this turn" | `ConditionalEffect(new Raid(), new EnterWithCountersEffect(PLUS_ONE_PLUS_ONE, new Fixed(1)))` | `ON_ENTER_BATTLEFIELD` | Rigging Runner, Storm Fleet Aerialist |
| "put a +1/+1 counter on target creature" | `PutPlusOnePlusOneCounterOnTargetCreatureEffect(1)` | SPELL/trigger | |
| "Whenever one or more +1/+1 counters are put on CARDNAME" | `AwardAnyColorManaEffect()` (or other effects) | `ON_SELF_PLUS_ONE_PLUS_ONE_COUNTERS_PUT` | Fired from `PermanentCounterSupport` after each +1/+1 placement event (once per event). Used by Berta, Wise Extrapolator |
| "put N +1/+1 counters on target creature" | `PutPlusOnePlusOneCounterOnTargetCreatureEffect(N)` | SPELL/trigger | |
| "put a +1/+1 counter on each creature you control" | `PutCounterOnEachControlledPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentIsCreaturePredicate())` | SPELL/trigger | |
| "put a +1/+1 counter on each creature target player controls" | `PutPlusOnePlusOneCounterOnEachCreatureFirstTargetPlayerControlsEffect()` | SPELL | Multi-target: player first (`targetIds[0]`). Practiced Offense |
| "target creature gains your choice of [keyword] or [keyword] until end of turn" | `GrantChosenKeywordToSecondTargetEffect(List.of(Keyword.X, Keyword.Y))` | SPELL | Multi-target: creature second (`targetIds[1]`). Practiced Offense |
| "damage to target creature equal to the amount of mana spent to cast this spell" | `DealDamageToTargetCreatureEffect(new ManaSpentToCast())` | SPELL | Total mana spent snapshotted at cast time (`SpellCastingService` keys on the `ManaSpentToCast` amount). Molten Note |
| "put a -1/-1 counter on target creature" | `PutMinusOneMinusOneCounterOnTargetCreatureEffect(1)` | SPELL/trigger | |
| "proliferate" | `ProliferateEffect()` | SPELL/trigger | |
| "put N <named> counters on CARDNAME" | `PutCountersOnSelfEffect(CounterType.X, N)` | trigger/ability | for a non-P/T named counter type on the source (e.g. Jar of Eyeballs: `CounterType.EYEBALL, 2`) |
| "Remove all <named> counters from CARDNAME: Look at the top X cards…" | `RemoveAllCountersAsCostEffect(CounterType.X)` + `LookAtTopCardsPerChargeCounterChooseOneToHandRestOnBottomEffect()` | ability | X = counters removed, snapshotted into xValue (Jar of Eyeballs: `CounterType.EYEBALL`). The look effect reads xValue, so it works for any counter-removal cost that snapshots X |

## Tap / untap

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "tap enchanted creature" | `TapEnchantedCreatureEffect()` | ability | aura's own activated ability; no targeting |
| "whenever enchanted creature is dealt damage, it deals that much damage to its controller" | `EnchantedCreatureDealsDamageEqualToDealtDamageToControllerEffect()` | `ON_ENCHANTED_CREATURE_DEALT_DAMAGE` | Spiteful Shadows |
| "tap target [permanent]" | `TapTargetPermanentEffect()` | SPELL/ability | + filter |
| "untap target [permanent]" | `UntapTargetPermanentEffect(predicate)` | SPELL/ability | |
| "untap all [permanents] you control" | `UntapAllControlledPermanentsEffect(predicate)` | SPELL | |
| "CARDNAME doesn't untap during your untap step" | `DoesntUntapDuringUntapStepEffect()` | STATIC | |
| "tap all attacking creatures" | `TapAllAttackingCreaturesEffect()` | SPELL/trigger | no targeting |
| "those creatures don't untap during their controller's next untap step" (attacking creatures) | `SkipNextUntapAllAttackingCreaturesEffect()` | SPELL/trigger | pair with `TapAllAttackingCreaturesEffect` |

## Control / steal

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "gain control of target [permanent]" | `GainControlOfTargetPermanentEffect()` | SPELL | Permanent |
| "gain control of target creature with power <= creature count" | `GainControlOfTargetPermanentEffect()` + `PermanentPowerAtMostControlledCreatureCountPredicate` filter | ability | Dynamic power check vs creature count |
| "gain control of target creature until end of turn" | `GainControlOfTargetPermanentUntilEndOfTurnEffect()` | SPELL | Threaten |
| "you control enchanted creature" | `GainControlOfEnchantedTargetEffect()` | STATIC | Control Magic |

## Mana

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "add {C}" / "add one mana of [color]" | `AwardManaEffect(ManaColor.X, 1)` | ON_TAP/ability | |
| "add [color] for each [X] you control" | `AwardManaEffect(ManaColor.X, new PermanentCount(filter, CountScope.CONTROLLER))` | ability | Elvish Archdruid, Cabal Stronghold, Itlimoc, Koth −2, Powerstone Shard |
| "add {C} for each charge counter on it" | `AwardManaEffect(ManaColor.COLORLESS, new CountersOnSource(CounterType.CHARGE))` | ability | Shrine of Boundless Growth |
| "add [color] equal to its power" | `AwardManaEffect(ManaColor.X, new SourcePower())` | ability/trigger | Marwyn, the Nurturer; Molten-Core Maestro |
| "add one mana of any color" | `AwardAnyColorManaEffect()` | ON_TAP/ability/`ON_SELF_PLUS_ONE_PLUS_ONE_COUNTERS_PUT` | |
| "add N mana of any one color" | `AwardAnyColorManaEffect(N)` | ability | |

## Prevention

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "prevent all combat damage that would be dealt this turn" | `PreventAllCombatDamageEffect()` | SPELL | |
| "prevent the next N damage that would be dealt to target" | `PreventDamageToTargetEffect(N)` | SPELL | |

## Conditional wrappers

| Oracle text phrase | Wrapper | Notes |
|---|---|---|
| "you may [effect]" | `MayEffect(innerEffect, "prompt")` | Player chooses |
| "if you control three or more artifacts, [effect]" | `ConditionalEffect(new Metalcraft(), innerEffect)` | Metalcraft |
| "if a creature died this turn, [effect]" | `ConditionalEffect(new Morbid(), innerEffect)` | Morbid |
| "if you attacked this turn, [effect]" | `ConditionalEffect(new Raid(), innerEffect)` | Raid |
| "if five or more mana was spent to cast that spell, [effect]" | `ConditionalEffect(new SpellManaSpentAtLeast(5), innerEffect)` inside `SpellCastTriggerEffect` | Trigger collector snapshots mana spent into stack entry `xValue` |
| Opus "[base]. If five or more mana was spent to cast that spell, [upgraded] instead" (single target/self) | `ConditionalReplacementEffect(new SpellManaSpentAtLeast(5), baseEffect, upgradedEffect)` inside `SpellCastTriggerEffect` | Use for "instead" upgrades where a base+conditional pair would double a target (e.g. Exhibition Tidecaller `MillTargetPlayerEffect(3)` → `(10)`; Deluge Virtuoso boost uses the additive `BoostSelfEffect(1,1)` + `ConditionalEffect(...)` since boosts stack). Player/self targeting and the mana-spent `xValue` are plumbed through the targeted-trigger interaction. Additive stacking effects (`BoostSelfEffect`, `PutCountersOnSelfEffect`) should still use base + `ConditionalEffect` |
| "Repartee — Whenever you cast an instant or sorcery spell that targets a creature, [effect]" | `SpellCastTriggerEffect(CardAnyOfPredicate(INSTANT, SORCERY), List.of(effect), new StackEntryTargetsPermanentPredicate(new PermanentIsCreaturePredicate()))` on `ON_CONTROLLER_CASTS_SPELL` | The 3rd/4th-arg `castSpellTargetCondition` gates the trigger on the cast spell's chosen targets (a card-only `spellFilter` can't). Use the `(spellFilter, effects, TargetFilter, StackEntryPredicate)` overload when the resolved effect also targets (e.g. Graduation Day). Repartee itself is unmapped in Scryfall and adds nothing (Lecturing Scornmage, Rehearsed Debater, Stirring Hopesinger, Informed Inkwright, Inkshape Demonstrator, Forum Necroscribe) |
| "Ward—Discard a card" | `CounterUnlessDiscardsEffect()` on `ON_BECOMES_TARGET_OF_OPPONENT_SPELL` | Mirrors Ward {N} = `CounterUnlessPaysEffect(N)` (e.g. Frost Titan); the discard variant is countered immediately if the controller has no card |
| "if you've cast another instant or sorcery spell this turn, [effect]" | `ConditionalEffect(new ControllerCastAnotherSpellThisTurn(CardAnyOfPredicate(INSTANT, SORCERY)), innerEffect)` | SPELL | Excludes the resolving spell; checked at resolution time |
| "if [base], [effect]. If kicked, [upgraded effect] instead" | `ConditionalReplacementEffect(new Kicked(), baseEffect, upgradedEffect)(base, kicked)` | Kicker replaces |
| "if this spell was kicked, [additional effect]" | `ConditionalEffect(new Kicked(), innerEffect)` | Kicker adds |
| "if you control a [subtype], [effect]" | `ConditionalEffect(new ControlsPermanent(new PermanentHasSubtypePredicate(subtype)), innerEffect)` | Permanent predicate check |
| "if you control a [matching permanent], [effect]" | `ConditionalEffect(new ControlsPermanent(predicate), innerEffect)` | Permanent check |
| "if you control a [subtype], [upgraded effect] instead" | `ConditionalReplacementEffect(new ControlsPermanent(filter), baseEffect, upgradedEffect)(new PermanentHasSubtypePredicate(subtype), baseEffect, upgradedEffect)` | Resolution-time replacement |
| "if that/target creature is a [subtype], [upgraded effect] instead" | `ConditionalReplacementEffect(new TargetPermanentMatches(filter), baseEffect, upgradedEffect)(new PermanentHasSubtypePredicate(subtype), baseEffect, upgradedEffect)` | Target permanent checked at resolution; falls back to base if missing or nonmatching |
| "Infusion — if you gained life this turn, [additional effect]" | `ConditionalEffect(new GainedLifeThisTurn(), innerEffect)` | Life gained tracked in `GameData.lifeGainedThisTurn` (via `LifeSupport`), cleared each turn; also valid as a static self-buff (`StaticBoostEffect(...GrantScope.SELF)`) e.g. Ulna Alley Shopkeep |
| "[base effect]. Infusion — if you gained life this turn, [upgraded effect] instead" | `ConditionalReplacementEffect(new GainedLifeThisTurn(), baseEffect, upgradedEffect)` | Resolution-time replacement, e.g. Withering Curse (mass -2/-2 upgraded to destroy all creatures) |
| "Infusion — At the beginning of your end step, [downside] unless you gained life this turn" | `ConditionalEffect(new DidntGainLifeThisTurn(), downsideEffect)` in `CONTROLLER_END_STEP_TRIGGERED` | `DidntGainLifeThisTurn` is the negation of `GainedLifeThisTurn`; for "sacrifice a permanent" use `SacrificePermanentThenEffect(new PermanentTruePredicate(), null, "a permanent")` (null thenEffect = no follow-up). E.g. Tragedy Feaster |
| "choose one —" | `ChooseOneEffect(List<ChooseOneOption>)` | Modal |

## Turn / phase

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "take an extra turn after this one" | `ControllerExtraTurnEffect(1)` | SPELL | Non-targeting |
| "untap all creatures that attacked this turn. After this main phase, there is an additional combat phase followed by an additional main phase" | `AdditionalCombatMainPhaseEffect(1)` | SPELL | |

## Copy

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "copy target instant or sorcery spell" | `CopySpellEffect()` | SPELL | Filter on the Card's SpellTarget via `target(...)`. Add `StackEntryControlledByPredicate` for "...spell you control" |
| "copy target creature spell you control. The copy gains haste and 'at the beginning of the end step, sacrifice this token.'" | `CopySpellEffect(null, true, true)` | SPELL | The creature-spell copy becomes a token, gains `HASTE`, and is registered for end-step sacrifice (`GameData.permanentsToSacrificeAtEndStep`). Restrict targeting to creature spells you control via the mode's `StackEntryPredicateTargetFilter(StackEntryAllOfPredicate(StackEntryTypeInPredicate(CREATURE_SPELL), StackEntryControlledByPredicate))`. See Choreographed Sparks |
| "this spell can't be copied" | `card.setCantBeCopied(true)` (not an effect) | constructor | Honored by every copy handler (`CopySpellEffectHandler`, `CopySpellForEachOtherPlayerEffectHandler`, `CopyControllerCastSpellEffectHandler`) — the copy is simply not created. Choreographed Sparks |
| "choose one or both — copy target instant/sorcery you control • copy target creature spell you control" | `ChooseOneEffect` with 3 modes; the "both" mode uses `ChooseOneOption(label, List.of(copyIS, copyCreature), List.of(isFilter, creatureFilter))` | SPELL | Two distinct spell targets in one mode — see the "choose one or both with two distinct spell targets" row in CARD_PATTERNS_LANDS_SPELLS.md. Choreographed Sparks |
| "copy target instant or sorcery spell. If this spell was cast from a graveyard, copy that spell twice instead" | `CopySpellEffect()` + `ConditionalEffect(new CastFromZone(Zone.GRAVEYARD), new CopySpellEffect())` | SPELL | Increasing Vengeance — base copy plus a graveyard-conditional second copy; add `FlashbackCast` separately. Flashback can target a spell on the stack |
| "whenever a player casts an instant or sorcery spell, each other player copies that spell" | `CopySpellForEachOtherPlayerEffect()` | ON_ANY_PLAYER_CASTS_SPELL | Hive Mind (mandatory) |
| "whenever enchanted player casts an instant or sorcery spell, each other player may copy that spell" | `CopySpellForEachOtherPlayerEffect(true, new StackEntryControlledByEnchantedPlayerPredicate())` | ON_ANY_PLAYER_CASTS_SPELL | Curse of Echoes — the `StackEntryControlledByEnchantedPlayerPredicate` filter gates to casts by the enchanted player; each other player *may* copy |
| "whenever you cast an instant or sorcery spell, you may tap three untapped creatures you control. If you do, copy that spell" | `CopyControllerCastSpellOnSpellCastEffect(CardAnyOfPredicate(INSTANT, SORCERY), new TapMultiplePermanentsCost(3, PermanentIsCreaturePredicate))` | ON_CONTROLLER_CASTS_SPELL | Aziza, Mage Tower Captain — snapshots cast spell, `MayPayTapPermanentsEffect` + `CopyControllerCastSpellEffect` at resolution |
| "enters the battlefield as a copy of any creature on the battlefield" | `CopyPermanentOnEnterEffect(predicate, label)` | ON_ENTER_BATTLEFIELD | Clone |

## Mana cost → test `addMana` reference

`ManaColor` enum values: `WHITE` (W), `BLUE` (U), `BLACK` (B), `RED` (R), `GREEN` (G), `COLORLESS` (C).

**Key rule:** Generic mana (the number in a mana cost like `{2}`) can be paid with any color. In tests, pay generic mana using the card's primary color for simplicity — one `addMana` call covers both the generic and colored portions.

### Single-color cards

For a card with one colored symbol, add total CMC of that color:

| Mana cost | `addMana` call | Breakdown |
|---|---|---|
| `{W}` | `addMana(player, WHITE, 1)` | 1W |
| `{U}` | `addMana(player, BLUE, 1)` | 1U |
| `{B}` | `addMana(player, BLACK, 1)` | 1B |
| `{R}` | `addMana(player, RED, 1)` | 1R |
| `{G}` | `addMana(player, GREEN, 1)` | 1G |
| `{1}{W}` | `addMana(player, WHITE, 2)` | 1 generic + 1W |
| `{1}{U}` | `addMana(player, BLUE, 2)` | 1 generic + 1U |
| `{1}{B}` | `addMana(player, BLACK, 2)` | 1 generic + 1B |
| `{1}{R}` | `addMana(player, RED, 2)` | 1 generic + 1R |
| `{1}{G}` | `addMana(player, GREEN, 2)` | 1 generic + 1G |
| `{2}{W}` | `addMana(player, WHITE, 3)` | 2 generic + 1W |
| `{2}{U}` | `addMana(player, BLUE, 3)` | 2 generic + 1U |
| `{2}{B}` | `addMana(player, BLACK, 3)` | 2 generic + 1B |
| `{2}{R}` | `addMana(player, RED, 3)` | 2 generic + 1R |
| `{2}{G}` | `addMana(player, GREEN, 3)` | 2 generic + 1G |
| `{3}{W}` | `addMana(player, WHITE, 4)` | 3 generic + 1W |
| `{4}{B}` | `addMana(player, BLACK, 5)` | 4 generic + 1B |
| `{W}{W}` | `addMana(player, WHITE, 2)` | 2W |
| `{U}{U}` | `addMana(player, BLUE, 2)` | 2U |
| `{B}{B}` | `addMana(player, BLACK, 2)` | 2B |
| `{R}{R}` | `addMana(player, RED, 2)` | 2R |
| `{G}{G}` | `addMana(player, GREEN, 2)` | 2G |
| `{1}{W}{W}` | `addMana(player, WHITE, 3)` | 1 generic + 2W |
| `{2}{U}{U}` | `addMana(player, BLUE, 4)` | 2 generic + 2U |
| `{2}{B}{B}` | `addMana(player, BLACK, 4)` | 2 generic + 2B |
| `{3}{R}{R}` | `addMana(player, RED, 5)` | 3 generic + 2R |
| `{3}{G}{G}` | `addMana(player, GREEN, 5)` | 3 generic + 2G |

**Pattern:** For `{N}{C}{C}` where C is a single color, use `addMana(player, COLOR, N + coloredCount)`.

### Multi-color cards

For cards with multiple colored symbols, add each color separately. Pay generic mana as COLORLESS:

| Mana cost | `addMana` calls | Breakdown |
|---|---|---|
| `{R}{W}` | `addMana(player, RED, 1); addMana(player, WHITE, 1)` | 1R + 1W |
| `{U}{B}` | `addMana(player, BLUE, 1); addMana(player, BLACK, 1)` | 1U + 1B |
| `{B}{G}` | `addMana(player, BLACK, 1); addMana(player, GREEN, 1)` | 1B + 1G |
| `{R}{G}` | `addMana(player, RED, 1); addMana(player, GREEN, 1)` | 1R + 1G |
| `{W}{U}` | `addMana(player, WHITE, 1); addMana(player, BLUE, 1)` | 1W + 1U |
| `{1}{R}{W}` | `addMana(player, RED, 1); addMana(player, WHITE, 1); addMana(player, COLORLESS, 1)` | 1R + 1W + 1 generic |
| `{1}{B}{G}` | `addMana(player, BLACK, 1); addMana(player, GREEN, 1); addMana(player, COLORLESS, 1)` | 1B + 1G + 1 generic |
| `{2}{W}{U}` | `addMana(player, WHITE, 1); addMana(player, BLUE, 1); addMana(player, COLORLESS, 2)` | 1W + 1U + 2 generic |
| `{2}{B}{R}` | `addMana(player, BLACK, 1); addMana(player, RED, 1); addMana(player, COLORLESS, 2)` | 1B + 1R + 2 generic |
| `{1}{W}{U}{B}` | `addMana(player, WHITE, 1); addMana(player, BLUE, 1); addMana(player, BLACK, 1); addMana(player, COLORLESS, 1)` | 3-color + 1 generic |

**Pattern:** Add exact colored amounts per color, then add generic as `COLORLESS`.

### X-cost cards

X is chosen at cast time. Add the colored requirement plus X of any color:

| Mana cost | `addMana` call for X=3 | Breakdown |
|---|---|---|
| `{X}{R}` | `addMana(player, RED, 4)` | X(3) + 1R |
| `{X}{U}` | `addMana(player, BLUE, 4)` | X(3) + 1U |
| `{X}{B}{B}` | `addMana(player, BLACK, 5)` | X(3) + 2B |
| `{X}{R}{G}` | `addMana(player, RED, 1); addMana(player, GREEN, 1); addMana(player, COLORLESS, 3)` | 1R + 1G + X(3) generic |

### Colorless / artifact cards

| Mana cost | `addMana` call | Notes |
|---|---|---|
| `{0}` | *(no mana needed)* | Free cast |
| `{1}` | `addMana(player, COLORLESS, 1)` | Any color also works |
| `{2}` | `addMana(player, COLORLESS, 2)` | |
| `{3}` | `addMana(player, COLORLESS, 3)` | |
| `{5}` | `addMana(player, COLORLESS, 5)` | |
