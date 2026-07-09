# ORACLE_TEXT_EFFECT_MAP

Purpose: quickly map oracle text phrases to the correct effect class + slot. Search this file for keywords from the card's oracle text to find the matching effect without reading EFFECTS_QUICK_REFERENCE.md.

- "When [this] dies, return it to the battlefield transformed under your control at the beginning of the next end step." -> `EffectSlot.ON_DEATH` + `RegisterDelayedReturnSourceTransformedEffect()`.
- "Whenever a spell or ability an opponent controls causes a land to be put into your graveyard from the battlefield, return that card to the battlefield." (Sacred Ground) -> `EffectSlot.ON_ALLY_LAND_PUT_INTO_GRAVEYARD_BY_OPPONENT` + `ReturnTriggeringLandFromGraveyardToBattlefieldEffect(null)`. The engine reads `GameData.currentlyResolvingControllerId` (set in `StackResolutionService`) to confirm an opponent's spell/ability caused it.

## Damage

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "deals N damage to any target" | `DealDamageToAnyTargetEffect(N, false)` | SPELL | Targeting auto-derived |
| "deals N damage to the player or planeswalker it's attacking" | `DealDamageToAttackedTargetEffect(N)` | `ON_ALLY_CREATURE_ATTACKS`/attack trigger | Uses the attacking creature's `attackedTargetId` context captured by `CombatAttackService`; not a targeted ability |
| "Whenever a creature attacks you or a planeswalker you control, reveal the top card... if it's a [type], remove that creature from combat. Then put... on the bottom" | `RevealTopCardRemoveTargetFromCombatIfMatchEffect(CardPredicate)` | `ON_CREATURE_ATTACKS_YOU` | Fires once per attacking creature on the defending player's side; the attacking creature is set as the non-targeting `targetId`. Used by Lost in the Woods (`CardSubtypePredicate(FOREST)`) |
| "Reveal cards from the top of your library until you reveal N nonland cards. Put the nonland cards into your hand, then put the rest on the bottom of your library in any order" | `RevealUntilNonlandCardsToHandRestToBottomEffect(N)` | SPELL | Used by Fathom Trawl. Reveals a variable number of cards (until N nonland found); lands go to bottom via async `LibraryReorder` |
| "CARDNAME can't attack unless [condition]" | `CantAttackUnlessEffect(Condition, "unless clause")` | `STATIC` | Map the unless-clause to a Condition: "you control an [X]" = `ControlsPermanentCount(1, filter)`; "defending player controls an [X]" = `DefendingPlayerControlsPermanent(filter)`; "there are N or more [X] on the battlefield" = `AnyPlayerControlsPermanentCount(N, filter)`; "defending player is poisoned" = `DefendingPlayerPoisoned()`; "an opponent was dealt damage this turn" = `OpponentDealtDamageThisTurn()`. Never a new class. |
| "deals N damage to target creature" | `DealDamageToTargetCreatureEffect(N)` | SPELL | Creature-only targeting |
| "deals N damage to target creature or planeswalker" | `DealDamageToTargetCreatureOrPlaneswalkerEffect(N)` | SPELL | |
| "deals N damage to target opponent or planeswalker" | `DealDamageToTargetOpponentOrPlaneswalkerEffect(N)` | SPELL | |
| "deals N damage to target opponent and N damage to up to M target creatures that player controls" | `DealDamageToTargetOpponentAndUpToCreaturesThatPlayerControlsEffect(N, N, M)` | `ON_TRANSFORM_TO_BACK_FACE` | Two-step transform trigger target choice; use M=1 for "up to one" |
| "deals N damage to target player" | `DealDamageToPlayersEffect(N, DamageRecipient.TARGET_PLAYER)` | SPELL | Only recipient that targets. Amount is any `DynamicAmount` |
| "deals N damage to each opponent" | `DealDamageToPlayersEffect(N, DamageRecipient.EACH_OPPONENT)` | SPELL/trigger | No targeting. Amount evaluates once — for dynamic amounts pass a `DynamicAmount`, e.g. `new CountersOnSource(PLUS_ONE_PLUS_ONE)` (Hallar). NOT for per-opponent amounts (Molten Psyche keeps its own record) |
| "deals N damage to each player" | `DealDamageToPlayersEffect(N, DamageRecipient.EACH_PLAYER)` | SPELL | No targeting |
| "deals N damage to each creature" | `MassDamageEffect(N)` | SPELL | No targeting |
| "deals N damage to each creature and each planeswalker" | `MassDamageEffect(N, false, false, true, null)` | SPELL | damagesPlaneswalkers=true |
| "deals X damage to any target" | `DealDamageToAnyTargetEffect(new XValue())` | SPELL | X-cost; also cost-snapshotted X (Fling's sacrificed power, Soulblast). Add `(…, false, true)` for "if it would die this turn, exile it instead" (Red Sun's Zenith) |
| "deals X damage to target creature" | `DealDamageToTargetCreatureEffect(new XValue())` | SPELL | X-cost; also cost-snapshotted X (Corpse Lunge, Harvest Pyre) |
| "deals damage equal to its power to any target" (ability/trigger) | `DealDamageToAnyTargetEffect(new SourcePower())` | ability/trigger | Spikeshot Elder, Flayer of the Hatebound. Uses live source or last-known snapshot (CR 608.2h) |
| "deals damage equal to its toughness to target creature" | `DealDamageToTargetCreatureEffect(new SourceToughness())` | ability | Steadfast Armasaur |
| "deals damage equal to the number of charge counters on it to any target" | `DealDamageToAnyTargetEffect(new CountersOnSource(CounterType.CHARGE))` | ability | Shrine of Burning Rage; sacrifice-cost sources resolve from the entry's source snapshot |
| "deals damage to target creature equal to the number of SUBTYPEs you control" | `DealDamageToTargetCreatureEffect(new PermanentCount(new PermanentHasSubtypePredicate(SUBTYPE), CountScope.CONTROLLER))` | SPELL/trigger | Seismic Strike, Spitting Earth, Firefist Adept. "…and you gain X life" (Tendrils of Corruption) = add `GainLifeEffect(sameAmount)` |
| "deals damage to target player equal to the number of TYPE cards in your graveyard" | `DealDamageToPlayersEffect(new CardsInGraveyard(new CardTypePredicate(TYPE), CountScope.CONTROLLER), DamageRecipient.TARGET_PLAYER)` | SPELL | Scrapyard Salvo |
| "deals damage to target player equal to the number of cards in that player's hand" | `DealDamageToPlayersEffect(new CardsInHand(CountScope.TARGET_PLAYER), DamageRecipient.TARGET_PLAYER)` | SPELL / ON_COMBAT_DAMAGE_TO_PLAYER | Sudden Impact, Sword of War and Peace |
| "deals X damage to each of up to N targets" | `DealDamageToEachTargetEffect(new XValue())` + `target(1, N)` | SPELL | Jaya's Immolating Inferno — full amount to each target, not divided |
| "deals damage equal to its power to target" | `TargetDealsPowerDamageToTargetEffect()` | SPELL | Bite — multi-target. Effect impl uses `gameQueryService.getPowerBasedDamage(gd, source)` — do NOT call `getEffectivePower` directly; the helper clamps negative power to 0 per CR 510.1a. |
| "target creature deals damage to itself equal to its power" | `TargetCreatureDealsPowerDamageToSelfEffect()` | SPELL | Single-target. Target is both damage source and recipient. Use `getPowerBasedDamage`, not `getEffectivePower`. |
| "fights target creature" | `FightTargetsEffect()` | SPELL | Multi-target. Same rule: use `getPowerBasedDamage`, not `getEffectivePower`. |
| "target creature fights another target creature" | `FightTargetsEffect()` | SPELL | Multi-target, any two creatures; distinct is the default |
| "deals N damage to you" | `DealDamageToPlayersEffect(N, DamageRecipient.CONTROLLER)` | SPELL/trigger | Self-damage (pain lands) |
| "deals N damage to that creature's controller" | `DealDamageToPlayersEffect(N, DamageRecipient.TARGET_PERMANENT_CONTROLLER)` | SPELL | Chandra's Outrage (paired with a target-creature damage effect) |

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
| "creatures target player controls get +X/+Y until end of turn" | `BoostAllCreaturesEffect(X, Y, EachPermanentScope.TARGET_PLAYER)` | SPELL | Targets a player (`canTargetPlayer` when `TARGET_PLAYER`); pair with `GrantKeywordEffect(Keyword.CHANGELING, GrantScope.TARGET_PLAYERS_CREATURES)` for "and gain all creature types"; Shields of Velis Vel |
| "CARDNAME gets +X/+Y until end of turn" | `BoostSelfEffect(X, Y)` | ability effect | Self-pump |
| "CARDNAME gets +N/+M for each [permanent] you control" | `BoostSelfEffect(new PermanentCount(predicate, CountScope.CONTROLLER), ...)` | STATIC (or ability/trigger for until-EOT) | Any "for each" self-boost = `BoostSelfEffect` + `DynamicAmount` (see EFFECTS_INDEX "Dynamic amounts") — never a new effect class. "each *other*" → `excludeSource=true`; opponents' → `CountScope.OPPONENTS`; whole battlefield → `ANY_PLAYER` |
| "CARDNAME gets +N/+M for each [card] in your graveyard" | `BoostSelfEffect(new CardsInGraveyard(cardPredicate, CountScope.CONTROLLER), ...)` | STATIC | Multani, Yavimaya's Avatar |
| "CARDNAME gets +N/+M for each Aura/Equipment attached to it" | `BoostSelfEffect(new Scaled(new AttachmentsOnSource(auras, equipment), N), ...)` | STATIC | Champion of the Flame, Goblin Gaveleer |
| "CARDNAME gets +X/+0 until end of turn, where X is the mana spent to cast that spell" | `BoostSelfEffect(new XValue(), new Fixed(0))` inside `SpellCastTriggerEffect` | ON_CONTROLLER_CASTS_SPELL | Aberrant Manawurm; collector snapshots xValue for amounts referencing X |
| "Whenever CARDNAME becomes blocked, it gets +N/+N for each creature blocking it" | `BoostSelfEffect(new CreaturesBlockingSource(), new CreaturesBlockingSource())` | ON_BECOMES_BLOCKED | Elvish Berserker |
| "Enchanted/Equipped creature gets +N/+M for each [X]" | `AttachedBoostEffect(amount, amount, GrantScope.ENCHANTED_CREATURE / EQUIPPED_CREATURE)` | STATIC | Attached-scope sibling of `BoostSelfEffect` — any "for each" aura/equipment boost = `AttachedBoostEffect` + `DynamicAmount`, never a new effect class. `CountScope.CONTROLLER` = the aura/equipment's controller ("you control", CR 109.5). Blanchwood Armor (`PermanentCount(PermanentHasSubtypePredicate(FOREST), CONTROLLER)`), Blackblade Reforged (`PermanentIsLandPredicate`), Wreath of Geists (`CardsInGraveyard(CREATURE, CONTROLLER)`), Bonehoard (`…, ANY_PLAYER`) |
| "Enchanted/Equipped creature gets -N/-M for each [X]" | `AttachedBoostEffect(new Scaled(amount, -1), new Scaled(amount, -1), scope)` | STATIC | Negative per-count boost. Quag Sickness (-1/-1 per Swamp you control) |
| "Equipped creature gets +1/+1 for each land ... with the same name as the exiled card" | `AttachedBoostEffect(new LandsMatchingImprintedName(), same, GrantScope.EQUIPPED_CREATURE)` | STATIC | Strata Scythe (imprint on ETB, count matching-name lands on all battlefields) |
| "CARDNAME's power and toughness are each equal to the number of lands you control" | `SetPowerToughnessToAmountEffect(a, a)` where `a = new PermanentCount(new PermanentIsLandPredicate(), CountScope.CONTROLLER)` | STATIC | Pass the same amount instance for both fields |
| "CARDNAME's power and toughness are each equal to the number of creatures you control" | `SetPowerToughnessToAmountEffect(a, a)` where `a = new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER)` | STATIC | Swap the predicate/amount for other "…are each equal to the number of X" wordings: `CardsInGraveyard`, `CardsInHand`, `ControllerLifeTotal`, etc. |
| "switch target creature's power and toughness" | `SwitchPowerToughnessEffect()` | SPELL | |
| "switch this creature's power and toughness" (activated ability) | `SwitchPowerToughnessEffect(true)` | ability | Self variant; no target, affects the source (e.g. Turtleshell Changeling) |
| "target creature has base power and toughness X/Y" | `SetBasePowerToughnessEffect(X, Y)` | SPELL | scope defaults to `TARGET` (until end of turn); pass a `GrantScope` (e.g. `ENCHANTED_CREATURE`) for continuous static auras |
| "this creature has base power and toughness X/Y until end of turn" (non-targeting self ability) | `SetBasePowerToughnessEffect(X, Y, GrantScope.SELF)` | ability | One-shot on the source (until EOT); no target. Marsh Flitter |
| "creatures you control have base power and toughness X/X until end of turn" | `SetAllOwnCreaturesBasePowerToughnessEffect(X, X)` | ability/SPELL | Group base-P/T set (layer 7b, modifiers apply on top). `DynamicAmount` ctor for X-cost (Mirror Entity: `new XValue()`); `int` ctor for fixed. Pair with `GrantKeywordEffect(Keyword.CHANGELING, GrantScope.OWN_CREATURES)` for "and gain all creature types" |
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
| "whenever this creature blocks two or more creatures, it gains [keyword] until end of turn" | `GrantKeywordEffect(Keyword.X, GrantScope.SELF)` in `EffectSlot.ON_BLOCKS_MULTIPLE_CREATURES` | TRIGGER | fires once (not per blocker) when the creature is declared blocking 2+ attackers; resolved against the blocker itself. Lairwatch Giant. Usually paired with `GrantAdditionalBlockEffect(1)` in STATIC |
| "target creature gains [keyword] until end of turn" | `GrantKeywordEffect(Keyword.X, GrantScope.TARGET)` | SPELL | default duration is `END_OF_TURN` |
| "creatures you control gain [keyword] until end of turn" | `GrantKeywordEffect(Keyword.X, GrantScope.OWN_CREATURES)` | SPELL | |
| "target creature gains [keyword] until your next turn" | `GrantKeywordEffect(Keyword.X, GrantScope.TARGET, GrantDuration.UNTIL_YOUR_NEXT_TURN)` | SPELL | pass `GrantDuration` for non-default expiry |
| "target creature gets +N/+N; if it's a [type/subtype/supertype], it also gains [keyword]" | boost effect + `GrantKeywordEffect.toTargetIf(Keyword.X, predicate)` | SPELL | the boost (or other rider) applies to any legal target; only the keyword grant is conditional on `predicate` at resolution. Vampire's Zeal (`PermanentHasSubtypePredicate(VAMPIRE)`), Blessing of Belzenlok (`PermanentHasSupertypePredicate(LEGENDARY)`). Do NOT put the predicate in `filter` — that would restrict targeting |
| "create a ... token. It gains [keyword] until end of turn" | `CreateTokenEffect(..., innateKeywords, Set.of(Keyword.X))` (the `grantedKeywordsUntilEndOfTurn` arg) | SPELL | Artistic Process — Elemental gains haste. Keep innate keywords separate from the granted set |
| "enchanted creature has [keyword]" | `GrantKeywordEffect(Keyword.X, GrantScope.ENCHANTED_CREATURE)` | STATIC | |
| "equipped creature has [keyword]" | `GrantKeywordEffect(Keyword.X, GrantScope.EQUIPPED_CREATURE)` | STATIC | |
| "other [subtype] creatures you control have [keyword]" | `GrantKeywordEffect(Keyword.X, GrantScope.OWN_CREATURES, predicate)` | STATIC | |
| "as long as a creature card with [keyword] is in a graveyard, this creature has [keyword]" (fixed list incl. flying/first strike/deathtouch/landwalk/etc.) | `GainKeywordsOfCreatureCardsInAllGraveyardsEffect()` | STATIC | Cairn Wanderer. selfOnly; scans all graveyards for creature cards, grants the intersection with a fixed watched keyword set. Protection not modelled |
| "Lands you control have '[activated ability]'" | `GrantActivatedAbilityEffect(ability, GrantScope.OWN_PERMANENTS, new PermanentIsLandPredicate())` | STATIC | Grants an activated ability to all lands you control. Resonating Lute |
| "As an additional cost to cast this spell, discard a card" | `new DiscardCardTypeCost(null, null)` in the SPELL slot | SPELL | Paid at cast from a caller-provided hand index (`castSorceryWithDiscard` / `PlayCardRequest.discardHandCardIndex`); spell is unplayable without another card to discard. Seize the Spoils |
| "As an additional cost to cast this spell, return a creature you control to its owner's hand" | `new ReturnCreatureToHandCost()` in the SPELL slot | SPELL | Creature supplied via `PlayCardRequest.sacrificePermanentId` (reuses the sacrifice-cost id field); paid in `SpellCastingService`. Combine with the actual effect (e.g. `CounterSpellEffect`). Familiar's Ruse |
| "As an additional cost to cast this spell, reveal a [subtype] card from your hand or pay {N}" | `new IncreaseOwnCastCostUnlessRevealSubtypeEffect(N, CardSubtype.X)` in the STATIC slot | STATIC | Spell-self cost modifier (Lorwyn "reveal a creature-type card or pay" cycle, e.g. Goldmeadow Stalwart: Kithkin/{3}). Costs {N} more unless you hold a matching card (other than the spell itself) to reveal — auto-reveal is game-state-equivalent. Handled by `IncreaseOwnCastCostUnlessRevealSubtypeEffectHandler` (spell-self cost handler) |
| "{cost}: [effect]. Activate only if you have N or more cards in your hand." | `new ActivatedAbility(...).withMinCardsInHand(N)` | ability | Validated in `AbilityActivationService`. Resonating Lute |
| "target creature can't be blocked this turn" | `MakeCreatureUnblockableEffect()` | SPELL | |
| "regenerate target creature" | `RegenerateEffect(true)` | ability effect | targetsPermanent=true |
| "regenerate CARDNAME" | `RegenerateEffect()` | ability effect | Self |

## Destruction / sacrifice

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "destroy target [permanent type]" | `DestroyTargetPermanentEffect(false)` | SPELL | + PermanentPredicate filter |
| "destroy target creature. It can't be regenerated" | `DestroyTargetPermanentEffect(true)` | SPELL | cantRegenerate=true |
| "destroy target attacking creature. You gain life equal to its power" | `GainLifeEffect(new TargetPower())` + `DestroyTargetPermanentEffect(false)` | SPELL | target = `PermanentPredicateTargetFilter(PermanentIsAttackingPredicate)`; gain life FIRST so power is read before destruction (Chastise) |
| "destroy all creatures" | `DestroyAllPermanentsEffect(PermanentIsCreaturePredicate())` | SPELL | |
| "destroy all [type]" | `DestroyAllPermanentsEffect(predicate)` | SPELL | Filtered wipe |
| "target player sacrifices a creature" | `SacrificePermanentsEffect(1, PermanentIsCreaturePredicate(), SacrificeRecipient.TARGET_PLAYER)` | SPELL | bare creature filter → single-select sacrifice-a-creature primitive |
| "sacrifice a [subtype]: [effect]" | `SacrificePermanentCost(PermanentAllOfPredicate(creature + PermanentHasSubtypePredicate(subtype)), "Sacrifice a [subtype]", false)` then effect | activated ability | Ravenous Demon front face uses `TransformSelfEffect()` with `SORCERY_SPEED` |
| "sacrifice a [subtype]. If you can't, [effects]" | `ForcedCostOrElseEffect(SacrificePermanentCost(PermanentAllOfPredicate(creature + subtype), description, false), elseEffects)` | trigger | Archdemon of Greed uses `TapPermanentsEffect(TapUntapScope.SELF)` + `DealDamageToPlayersEffect(9, DamageRecipient.CONTROLLER)` |
| "you may sacrifice a nontoken creature. If you do, create X 2/2 Wolf tokens, where X is its toughness" | `MayEffect(SacrificeCreatureToCreateTokensEqualToToughnessEffect(template, PermanentNotPredicate(PermanentIsTokenPredicate)))` | trigger | Feed the Pack; X = sacrificed creature's toughness |
| "each opponent sacrifices a creature" | `SacrificePermanentsEffect(1, PermanentIsCreaturePredicate(), SacrificeRecipient.EACH_OPPONENT)` | SPELL/trigger | bare creature filter → per-opponent single-select sacrifice-a-creature |
| "each opponent/each player sacrifices N [type]" | `SacrificePermanentsEffect(N, predicate, SacrificeRecipient.EACH_OPPONENT/EACH_PLAYER)` | SPELL/trigger | non-creature filter → APNAP multi-permanent choice (Yawning Fissure, Destructive Force) |
| "sacrifice CARDNAME" | `SacrificeSelfEffect()` | trigger/ability | |

## Bounce / tuck

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "return target [permanent] to its owner's hand" | `ReturnToHandEffect.target()` | SPELL | + card `target(...)` filter; `.targetAndControllerLosesLife(1)` for "its controller loses 1 life" (Vapor Snag) |
| "return all creatures to their owners' hands" | `ReturnToHandEffect.allPermanentsMatching(new PermanentIsCreaturePredicate())` | SPELL | Mass bounce (null filter = every permanent) |
| "return this permanent to its owner's hand" | `ReturnToHandEffect.self()` | ability/trigger | Self-bounce |
| "return all permanents target player controls to their owners' hands" | `ReturnToHandEffect.permanentsTargetPlayerControls(filter)` | SPELL | River's Rebuke |
| "return all artifacts target player owns to their hand" | `ReturnToHandEffect.permanentsTargetPlayerOwns(new PermanentIsArtifactPredicate())` | SPELL | Owner-based (Hurkyl's Recall) |
| "put target [permanent] on top of its owner's library" | `PutTargetOnTopOfLibraryEffect()` | SPELL | |
| "{cost}: Put this [Aura/permanent] on top of its owner's library" | `PutTargetOnTopOfLibraryEffect.self()` | ability | Bounces the source permanent to the top of its owner's library. Soaring Hope |
| "put target [permanent] on the bottom of its owner's library" | `PutTargetOnBottomOfLibraryEffect()` | SPELL | |
| "Clash with an opponent, then return target creature to its owner's hand. If you win, you may put that creature on top of its owner's library instead." | `ClashEffect(null)` + `ConditionalEffect(new WonClash(), new MayEffect(new PutTargetOnTopOfLibraryEffect(), prompt))` + `ReturnToHandEffect.target()` | SPELL | Whirlpool Whelm (LRW). Order matters: the bare clash records its result (read by `WonClash`); on a win the optional tuck resolves **before** the trailing bounce, so accepting puts the creature on top of its library (the bounce then no-ops) while declining/losing falls through to the bounce — the "instead" replacement |

## Counter spells

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "counter target spell" | `CounterSpellEffect()` | SPELL | Targeting auto-derived |
| "When this creature enters, counter target spell with mana value X or less, where X is the number of [type] you control" | `target(new StackEntryPredicateTargetFilter(new StackEntryManaValueAtMostControlledCountPredicate(new PermanentHasAnySubtypePredicate(Set.of(subtype))), msg))` + `CounterSpellEffect()` | ON_ENTER_BATTLEFIELD | Spellstutter Sprite. The ETB spell-target pipeline (`ETBSpellTargetTrigger`) chooses the spell as the ability goes on the stack; when the effect only `canTargetSpell()`, it reads the legal-spell restriction from the card's `StackEntryPredicateTargetFilter`. Faerie count includes the source |
| "counter target spell unless its controller pays {N}" | `CounterUnlessPaysEffect(N)` | SPELL | |
| "Counter target spell unless its controller pays {X}. Clash with an opponent. If you win, that spell's controller mills four cards." | `ClashEffect(new MillEffect(4, MillRecipient.TARGET_SPELL_CONTROLLER))` + `CounterUnlessPaysEffect(0, true, false)` | SPELL | Broken Ambitions (LRW). List the `ClashEffect` mill **before** the counter so the targeted spell is still on the stack when the clash win resolves `TARGET_SPELL_CONTROLLER`; the two instructions are independent so the ordering is rules-equivalent |
| "Counter target spell. Clash with an opponent. If you win, at the beginning of your next main phase, you may add an amount of {C} equal to that spell's mana value." | `ClashEffect(new RegisterDelayedManaEqualToTargetSpellManaValueEffect(ManaColor.COLORLESS))` + `CounterSpellEffect()` | SPELL | Scattering Stroke (LRW). List the `ClashEffect` reward **before** the counter so the targeted spell is still on the stack when its mana value is snapshotted; the delayed "you may add {C}" fires at the caster's next precombat main |
| "counter target spell. You may cast a spell that shares a card type with it from your hand without paying its mana cost" | `CounterlashEffect()` | SPELL | Queues per-card PendingMayAbility with MayCastFromHandWithoutPayingManaCostEffect |
| "this spell can't be countered" | `CantBeCounteredEffect()` | STATIC | |
| "If a spell or ability you control would counter a spell, instead exile that spell and you may play that card without paying its mana cost" | `ReplaceControlledCounterWithExileAndPlayEffect()` | STATIC | Guile. Intercepts every counter effect the permanent's controller controls (in `CounterSupport`); exiles the spell and queues a `MayPlayExiledCounteredCardEffect` free-play. Only spells, not abilities; uncounterable spells unaffected |
| "This creature can't be blocked except by three or more creatures" | `CantBeBlockedByFewerThanNCreaturesEffect(3)` | STATIC | Generalized menace (validated in CombatBlockService). Menace itself = minBlockers 2; use this effect for other minimums (Guile = 3) |

## Draw / discard

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "draw N cards" / "draw a card" | `DrawCardEffect(N)` | SPELL/trigger | |
| "draw X cards" | `DrawCardEffect(new XValue())` | SPELL | X-cost |
| "draw a card for each creature you control" | `DrawCardEffect(new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER))` | trigger | Tishana |
| "draw a card for each creature card in your graveyard" | `DrawCardEffect(new CardsInGraveyard(new CardTypePredicate(CREATURE), CountScope.CONTROLLER))` | SPELL | Grim Flowering |
| "draw a card for each charge counter on [source]" | `DrawCardEffect(new CountersOnSource(CounterType.CHARGE))` | ability | Culling Dais; survives sacrifice via sourcePermanentSnapshot |
| "target player draws N cards" | `DrawCardForTargetPlayerEffect(N)` | SPELL | For explicit `target(player…)` groups use the 3-arg `DrawCardForTargetPlayerEffect(N, false, true)` so the group advertises a player target (unless another effect in the group already `canTargetPlayer()`) |
| "target player draws X cards" | `DrawCardForTargetPlayerEffect(new XValue(), false, true)` | SPELL | Blue Sun's Zenith |
| "target player draws 2ˣ cards" | `DrawTwoToTheXCardsForTargetPlayerEffect()` | SPELL | 2^X from stack xValue; target a player. Mathemagics |
| "each player draws N cards" | `EachPlayerDrawsCardEffect(N)` | SPELL | |
| "draw N cards, then discard M cards" | `DrawAndDiscardCardEffect(N, M)` | SPELL | Loot |
| "discard N cards, then draw M cards" | `DiscardAndDrawCardEffect(N, M)` | SPELL | Rummage |
| "discard up to N cards, then draw that many cards" | `DiscardUpToThenDrawThatManyEffect(N)` | SPELL/ability | Rummage with cap |
| "discard any number of cards, then draw that many cards plus one" | `DiscardUpToThenDrawThatManyEffect(ANY_NUMBER, 1)` | ON_DEATH/trigger | Colossus of the Blood Age |
| "discard all the cards in your hand, then draw that many cards" | `DiscardOwnHandThenDrawThatManyEffect()` | SPELL | Shattered Perception |
| "discard your hand, then draw cards equal to the number of cards in target opponent's hand" | `DiscardOwnHandThenDrawEqualToTargetPlayerHandSizeEffect()` | SPELL | Borrowed Knowledge (modal mode 0) |
| "discard a card" / "discard N cards" | `DiscardEffect(N, DiscardRecipient.CONTROLLER)` | SPELL/trigger | Controller discards |
| "target player discards N cards" | `DiscardEffect(N, DiscardRecipient.TARGET_PLAYER)` | SPELL | |
| "target player reveals X cards from their hand, where X is the number of [type] you control. You choose one of those cards. That player discards that card." | `target(new PlayerPredicateTargetFilter(new PlayerRelationPredicate(PlayerRelation.ANY), msg))` + `RevealCardsChooseOneToDiscardEffect(new PermanentHasAnySubtypePredicate(Set.of(subtype)))` | ON_ENTER_BATTLEFIELD | Thieving Sprite (LRW, `FAERIE`). The **target** picks which X cards to reveal (rest stay hidden), then you pick one to discard — not Duress (`ChooseCardsFromTargetHandEffect`), which reveals the whole hand. X counts the Faerie itself (already on the battlefield when the ETB resolves) |
| "target player discards a card for each charge counter on ~" | `DiscardEffect(new CountersOnSource(CounterType.CHARGE), DiscardRecipient.TARGET_PLAYER)` | ability | Shrine of Limitless Power (with `SacrificeSelfCost`) |
| "target player discards X cards at random" | `DiscardEffect(new XValue(), DiscardRecipient.TARGET_PLAYER, true)` | SPELL | Mind Shatter |
| "you discard N cards at random" (rummaging) | `DiscardEffect(N, DiscardRecipient.CONTROLLER, true)` | SPELL | Goblin Lore, Desperate Ravings |
| "Converge — Target player discards X cards, where X is the number of colors of mana spent to cast this spell." | `TargetPlayerDiscardsByConvergeEffect()` | SPELL | Arcane Omens |
| "Converge — deals X damage to target creature" + excess-damage exile | `DealDamageToTargetCreatureEffect(new XValue())` + `ExileTopCardsMayPlayUntilNextTurnEffect(new EventValue())` | SPELL | Converge snapshotted to xValue; damage handler stores excess on the entry's eventValue, which EventValue reads; Archaic's Agony |
| "each player discards N cards" | `DiscardEffect(N, DiscardRecipient.EACH_PLAYER)` | SPELL | APNAP order; `EACH_PLAYER` + `random=true` for Burning Inquiry |
| "each opponent discards a card" | `DiscardEffect(1, DiscardRecipient.EACH_OPPONENT)` | SPELL/trigger | |
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
| "you lose N life" / "lose N life" | `LoseLifeEffect(N)` (= `(Fixed(N), CONTROLLER, false)`) | SPELL/trigger | |
| "target player gains N life" | `TargetPlayerGainsLifeEffect(N)` | SPELL | |
| "target player loses N life" | `LoseLifeEffect(N, LoseLifeRecipient.TARGET_PLAYER)` | SPELL | Amount is `DynamicAmount` — `EventValue()` for "equal to the life you gained" (Sanguine Bond), `PermanentCount(filter, CONTROLLER)` for "for each … you control" (Bishop) |
| "each player loses N life" | `LoseLifeEffect(N, LoseLifeRecipient.EACH_PLAYER)` | SPELL/trigger | |
| "each opponent loses N life" | `LoseLifeEffect(N, LoseLifeRecipient.EACH_OPPONENT)` | SPELL/trigger | |
| "each opponent loses N life and you gain life equal to the life lost" | `LoseLifeEffect(N, LoseLifeRecipient.EACH_OPPONENT, true)` | SPELL | Drain; Exsanguinate = `LoseLifeEffect(new XValue(), EACH_OPPONENT, true)` |
| "whenever a [Subtype] you control becomes tapped, you may gain 1 life" | `TriggeringPermanentConditionalEffect(new PermanentHasSubtypePredicate(subtype), new MayEffect(new GainLifeEffect(1), "prompt"))` | `ON_ALLY_PERMANENT_BECOMES_TAPPED` | Judge of Currents. Fires on every permanent with this slot on the tapped permanent's controller's battlefield (driven by the same tap call sites as `ON_ENCHANTED_PERMANENT_TAPPED`, incl. attacking/tapping for mana); the conditional filters by the tapped permanent. Includes the source itself if it matches |
| "whenever you gain life, draw a card" | `DrawCardEffect(1)` | ON_CONTROLLER_GAINS_LIFE | Fires once per life-gain event; see `d/DrogskolReaver.java` |
| "whenever you gain life, put a growth counter on this enchantment" | `PutCountersOnSelfEffect(CounterType.GROWTH)` | ON_CONTROLLER_GAINS_LIFE | Fires once per life-gain event; see `c/ComfortingCounsel.java` |
| "as long as there are five or more growth counters on this enchantment, creatures you control get +3/+3" | `ConditionalEffect(new SourceCounterThreshold(5, CounterType.GROWTH), StaticBoostEffect(3, 3, OWN_CREATURES))` | STATIC | |
| "double target player's life total" | `DoubleTargetPlayerLifeEffect()` | SPELL | |
| "each player's life total becomes the highest life total among all players" | `SetEachPlayerLifeToHighestAmongPlayersEffect()` | ON_ENTER_BATTLEFIELD | Arbiter of Knollridge |
| "players can't gain life" | `PlayersCantGainLifeEffect()` | STATIC | |

## Graveyard / library hate (static, global)

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "players can't cast spells from graveyards" | `PlayersCantCastSpellsFromZonesEffect(Set.of(Zone.GRAVEYARD))` | STATIC | Ashes of the Abhorrent. Gated in flashback/graveyard-cast paths via `GameQueryService.canPlayersCastSpellsFromZone(gd, Zone.GRAVEYARD)` |
| "exile the top N cards of your library face down" (controller only, tracked to source) | `ExileTopCardsToSourceEffect(N)` | ON_ENTER_BATTLEFIELD | Colfenor's Plans. Controller-only; pair with `AllowCastFromCardsExiledWithSourceEffect(false)` for "you may play lands and cast spells from among those cards" |
| "Skip your draw step." | `SkipDrawStepEffect()` | STATIC | Colfenor's Plans. Controller's draw step skipped in `StepTriggerService.handleDrawStep` |
| "You can't cast more than one spell each turn." (controller only) | `LimitSpellsForControllerEffect(1)` | STATIC | Colfenor's Plans. Controller-only variant of `LimitSpellsPerTurnEffect` (Rule of Law, which is every player) |
| "players can't cast spells from graveyards or libraries" | `PlayersCantCastSpellsFromZonesEffect(Set.of(Zone.GRAVEYARD, Zone.LIBRARY))` | STATIC | Grafdigger's Cage. Gated via `GameQueryService.canPlayersCastSpellsFromZone(gd, zone)` (graveyard cast/flashback + `playCardFromLibraryTop`). Only `GRAVEYARD`/`LIBRARY` enforced |
| "creature cards in graveyards and libraries can't enter the battlefield" | `CardsCantEnterBattlefieldFromZonesEffect(new CardTypePredicate(CREATURE), Set.of(Zone.GRAVEYARD, Zone.LIBRARY))` | STATIC | Grafdigger's Cage. Filter selects which cards are blocked (null = all); `zones` selects which source zones are blocked (only `GRAVEYARD`/`LIBRARY` enforced). Blocks reanimation/undying + library-search-to-battlefield; gated via `GameQueryService.isCardBlockedFromEnteringFromZone(gd, card, zone)`. Blocked card stays in its zone |

## Library manipulation

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "search your library for a card" (any to hand) | `SearchLibraryEffect()` | SPELL | unified library search; no-arg = unrestricted single card to hand (Diabolic Tutor) |
| "search your library for a card... if cast from a graveyard, instead search for two cards" (to hand) | `SearchLibraryEffect(null, 1, 2)` | SPELL | Increasing Ambition — `(filter, int count, int cfg)` ctor. Count switches on `StackEntry.isCastWithFlashback()` |
| "search your library for a basic land card, put it into your hand" | `SearchLibraryEffect(CardPredicateUtils.basicLand())` | SPELL | basic land = `CardAllOf(CardSupertype BASIC, CardType LAND)`, composed via the `CardPredicateUtils.basicLand()` factory (no dedicated predicate class); single-arg ctor defaults to `HAND` |
| "search your library for a basic land card, put it onto the battlefield tapped" | `SearchLibraryEffect(CardPredicateUtils.basicLand(), LibrarySearchDestination.BATTLEFIELD_TAPPED)` | SPELL | `(count, filter, dest)` for "up to N" |
| "search your library for a [type] card, reveal it, put it into your hand" | `SearchLibraryEffect(predicate)` | SPELL | single-arg ctor = restricted (revealed, may fail to find), destination `HAND` |
| "search your library for up to N cards named X, reveal, put into hand" | `SearchLibraryEffect(new Fixed(N), new CardNamedPredicate("X"), LibrarySearchDestination.HAND)` | SPELL/trigger | Squadron Hawk; by-name search = `CardNamedPredicate` |
| "search your library for a card, then shuffle and put that card on top" | `SearchLibraryEffect(null, LibrarySearchDestination.TOP_OF_LIBRARY)` | ability | Liliana Vess (any card); creature-to-top = `(CardTypePredicate(CREATURE), TOP_OF_LIBRARY)` (Brutalizer Exarch) |
| "search your library for a creature card with mana value X or less / equal to N" | `SearchLibraryEffect(filter, dest, new XManaValueBound(exact, offset))` | ability/SPELL | X-relative MV bound (reads entry `xValue` + offset): Citanul Flute `(false,0)`→HAND, Birthing Pod `(true,1)`→BATTLEFIELD, Green Sun's Zenith `(false,0)` with `CardColorPredicate(GREEN)`+creature→BATTLEFIELD |
| "you may search your library for a Curse card, put it onto the battlefield attached to [target] player" | `MayEffect(SearchLibraryForSubtypeToBattlefieldAttachedToTargetPlayerEffect(CardSubtype.CURSE), prompt)` | trigger | Bitterheart Witch — `canTargetPlayer()=true`, target chosen at trigger time |
| "at the beginning of your upkeep, you may search your library for a Curse card that doesn't have the same name as a Curse attached to enchanted player, put it onto the battlefield attached to that player, then shuffle" | `MayEffect(SearchLibraryForCurseToBattlefieldAttachedToEnchantedPlayerEffect(), prompt)` | UPKEEP_TRIGGERED | Curse of Misfortunes — enchanted player derived from source aura's `attachedTo`; excludes Curses already attached to that player by name |
| "scry N" | `ScryEffect(N)` | SPELL/trigger | |
| "surveil N" | `SurveilEffect(N)` | SPELL/trigger | |
| "shuffle your library" | `ShuffleLibraryEffect()` | SPELL | |
| "exile cards from the top of your library until you exile cards with total mana value N or greater. You may cast any number of spells from among the exiled cards without paying their mana costs" | `ImprovisationCapstoneEffect(N)` | SPELL | `ImprovisationCapstoneCastChoice` interaction + `ImprovisationCapstoneCastSupport` |

## Mill

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "target player mills N cards" / "puts the top N cards into their graveyard" | `MillEffect(N, MillRecipient.TARGET_PLAYER)` | SPELL | |
| "target player mills X cards" | `MillEffect(new XValue(), MillRecipient.TARGET_PLAYER)` | SPELL | For "if cast from a graveyard, twice that many" flashback spells, wrap in `ConditionalReplacementEffect(new CastFromZone(Zone.GRAVEYARD), new MillEffect(new XValue(), TARGET_PLAYER), new MillEffect(new Scaled(new XValue(), 2), TARGET_PLAYER))` (Increasing Confusion) |
| "target player mills X cards, where X is the number of charge counters on ~" | `MillEffect(new CountersOnSource(CounterType.CHARGE), MillRecipient.TARGET_PLAYER)` | ability | Grindclock |
| "that player mills cards equal to the number of cards in their hand" | `MillEffect(new CardsInHand(CountScope.TARGET_PLAYER), MillRecipient.TARGET_PLAYER)` | EACH_UPKEEP_TRIGGERED | Dreamborn Muse — the trigger sets the active player as target; empty hand mills 0 |
| "each opponent mills N cards" | `MillEffect(N, MillRecipient.EACH_OPPONENT)` | SPELL/trigger | |
| "each player mills N cards" | `MillEffect(N, MillRecipient.CONTROLLER)` + `MillEffect(N, MillRecipient.EACH_OPPONENT)` | SPELL/trigger | Combine two effects — no targeting. See `GhoulcallersBell`, `ChillOfForeboding` |
| "mill N cards" (self) | `MillEffect(N, MillRecipient.CONTROLLER)` | SPELL/trigger | |
| "target player mills half their library" | `MillHalfLibraryEffect()` | SPELL | |

## Exile

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "exile target [permanent]" | `ExileTargetPermanentEffect()` | SPELL | |
| "exile target [permanent]. Return it at the beginning of the next end step" | `ExileTargetPermanentEffect(true)` | SPELL | returnEndStep=true |
| "exile all creatures" | `ExileAllCreaturesEffect()` | SPELL | |
| "exile target player's graveyard" | `ExileGraveyardCardsEffect(GraveyardExileScope.TARGET_PLAYER_ENTIRE)` | SPELL | Family record; other scopes: OWN, TARGET_CARDS_ANY_GRAVEYARD, TARGET_CARDS_OPPONENT_GRAVEYARD, ALL_PLAYERS, ALL_OPPONENTS |
| "exile target [type] card from a graveyard" | `ExileGraveyardCardsEffect(1, GraveyardExileScope.TARGET_CARDS_ANY_GRAVEYARD, new CardTypePredicate(...))` | SPELL/ability | filter null = any card |
| "exile N target cards from an opponent's graveyard" | `ExileGraveyardCardsEffect(N, GraveyardExileScope.TARGET_CARDS_OPPONENT_GRAVEYARD)` | ability | |
| "exile all cards from all graveyards" | `ExileGraveyardCardsEffect(GraveyardExileScope.ALL_PLAYERS)` | ability | |
| "exile all opponents' graveyards" | `ExileGraveyardCardsEffect(GraveyardExileScope.ALL_OPPONENTS)` | saga/ability | |
| "that player exiles N cards from their graveyard" | `ExileGraveyardCardsEffect(N, GraveyardExileScope.OWN)` | ENCHANTED_PLAYER_UPKEEP_TRIGGERED | |
| "exile target noncreature, nonland card from your graveyard. Until the end of your next turn, you may cast that card" | `ExileTargetCardFromGraveyardMayPlayUntilNextTurnEffect(CardAllOfPredicate(not creature, not land), true)` | `ON_ENTER_BATTLEFIELD` | Practiced Scrollsmith. Single graveyard target chosen at trigger time; grants play-until-end-of-next-turn permission |
| "exile target nonland permanent and the top card of your library. For each of those cards, its owner may play it until the end of their next turn" | `target(nonland permanent) + ExileTargetPermanentMayPlayUntilNextTurnEffect()` and `ExileTopCardsMayPlayUntilNextTurnEffect(1)` | SPELL | Suspend Aggression. Owner-relative play permission for each exiled card |
| "Exile target instant or sorcery card from an opponent's graveyard. You may cast it this turn, and mana of any type can be spent to cast that spell. If that spell would be put into a graveyard, exile it instead. Activate only as a sorcery" | `{2}` + `SacrificeCreatureCost(false,false,false,true)` + `ExileTargetInstantOrSorceryFromOpponentGraveyardMayCastEffect()` with `ActivationTimingRestriction.SORCERY_SPEED` | activated ability | Nita, Forum Conciliator. This-turn cast permission with any-mana + exile-instead-of-graveyard riders |
| "Whenever you cast a spell you don't own, [effect]" | `SpellCastTriggerEffect(new CardControllerDoesNotOwnPredicate(), List.of(effect))` | `ON_CONTROLLER_CASTS_SPELL` | Nita, Forum Conciliator (effect = `PutCounterOnEachControlledPermanentEffect(PLUS_ONE_PLUS_ONE, 1, PermanentIsCreaturePredicate)`). Requires card ownership stamped at game setup |
| "Whenever an opponent casts a spell, that player draws N cards" | `DrawCardForTargetPlayerEffect(N)` | `ON_OPPONENT_CASTS_SPELL` | Forced Fruition (N=7). Collector sets `targetId` to the casting opponent, who draws |
| "Whenever you cast a spell during an opponent's turn, you may return target creature you control to its owner's hand" | `target(creature-you-control filter) + MayEffect(SpellCastTriggerEffect(null, List.of(ReturnToHandEffect.target()), true), "prompt")` | `ON_CONTROLLER_CASTS_SPELL` | Glen Elendra Pranksters. The `true` is `SpellCastTriggerEffect.onlyDuringOpponentTurn` — gates the trigger to turns where the source's controller is not the active player. For the "may" + target combo, put the target filter on the CARD via `target(new PermanentPredicateTargetFilter(AllOf(PermanentIsCreaturePredicate, PermanentControlledBySourceControllerPredicate), ...))` (the may path reads `sourceCard.getTargetFilter()`, not the trigger's targetFilter) |
| "you may exile target creature card from your graveyard. If you do, create a token that's a copy of that card, except it's a Spirit in addition to its other types. Exile it at the beginning of the next end step" | `EACH_UPKEEP_TRIGGERED` + `MayEffect(ExileTargetCardFromGraveyardAndCreateTokenCopyEffect(CardTypePredicate(CREATURE), true, List.of(SPIRIT), false, true))` | trigger | Séance. `EACH_UPKEEP_TRIGGERED` requires `MayEffect` handling in `StepTriggerService` (queues via `queueMayAbility`) |

## Tokens

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "target player creates a token that's a copy of target creature you control" | `CreateTokenCopyOfTargetCreatureForTargetPlayerEffect()` | SPELL | Two targets (player, then creature); token enters under chosen player |
| "Whenever a [subtype] you control deals combat damage to a player, you may create ... token" | `AllyCombatDamageTriggerEffect(PermanentHasSubtypePredicate(X), MayEffect(CreateTokenEffect(...)))` | `ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER` | Boggart Mob. Generic wrapper: filters the damage dealer by predicate, then puts any effect (wrap in `MayEffect` for "you may") on the stack for the controller. For "... put a +1/+1 counter on it" (effect applies to the dealer, e.g. Rakish Heir) pass `PutCountersOnSourceEffect` with `bindSourceToDealer=true` |
| "create N 1/1 white Spirit creature tokens with flying" | `CreateTokenEffect.whiteSpirit(N)` | SPELL/trigger | Static factory |
| "Whenever another non-[Subtype] creature you control dies, create ..." | `EffectSlot.ON_ALLY_CREATURE_DIES` + `TriggeringCardConditionalEffect(new CardNotPredicate(new CardSubtypePredicate(subtype)), CreateTokenEffect...)` | trigger | Requiem Angel-style; use ally creature death, not nontoken, because token non-Spirits count |
| "create N 2/2 black Zombie creature tokens" | `CreateTokenEffect.blackZombie(N)` | SPELL/trigger | Static factory |
| "create N 1/1 white Soldier creature tokens" | `CreateTokenEffect.whiteSoldier(N)` | SPELL/trigger | Static factory |
| "create a Treasure token" | `CreateTokenEffect.ofTreasureToken(1)` | SPELL/trigger | Static factory |
| "you may put a land/permanent card from your hand onto the battlefield tapped" | `MayEffect(new PutCardToBattlefieldEffect(predicate, "land", true))` | SPELL | `enterTapped=true` (3rd arg). Embrace the Paradox |
| "you may put a permanent card with mana value X or less from your hand onto the battlefield tapped" | `MayEffect(new PutCardToBattlefieldEffect(new CardIsPermanentPredicate(), "permanent", true, true))` | SPELL | `enterTapped=true`, `maxManaValueBoundedByX=true` (filters hand by MV ≤ stack X). Mind into Matter |
| "create a 0/0 green and blue Fractal token, put X +1/+1 counters where X = cards drawn this turn" | `CreateFractalTokenWithCountersFromCardsDrawnThisTurnEffect()` | SPELL | Reads `GameData.cardsDrawnThisTurn`. Fractal Anomaly |
| "create a N/N [color] [Subtype] creature token" | `CreateTokenEffect("name", N, N, color, subtype)` | SPELL/trigger | Custom token |
| "create a 0/0 [color] and [color] [Subtype] creature token. Put N +1/+1 counters on it" | `CreateTokenEffect("name", 0, 0, color, Set.of(colors), List.of(subtype), N)` | SPELL/trigger/ETB | `initialPlusOnePlusOneCounters` on `CreateTokenEffect` (e.g. Additive Evolution) |
| "{X}, {T}: Create a 0/0 [color] and [color] [Subtype] creature token and put X +1/+1 counters on it" | `CreateXTokenWithXCountersEffect("name", 0, 0, color, Set.of(colors), List.of(subtype), CounterType.PLUS_ONE_PLUS_ONE)` | activated ability | X from `StackEntry.getXValue()` (e.g. Berta, Wise Extrapolator) |
| "Increment (Whenever you cast a spell, if the mana you spent is greater than this creature's power or toughness, put a +1/+1 counter on it)" | *(none — keyword-driven)* | — | Increment keyword (SOS). Auto-loaded from Scryfall as `Keyword.INCREMENT`; behavior is driven by the keyword in `TriggerCollectionService.collectIncrementTriggers` (like Undying). Add **nothing** to the card (e.g. Ambitious Augmenter) |
| "Whenever you clash, [effect]. If you won, [win-only effect]." | `addEffect(ON_CONTROLLER_CLASHES, effect)` + `addEffect(ON_CONTROLLER_CLASHES, new IfWonClashEffect(winOnlyEffect))` | `ON_CONTROLLER_CLASHES` | Clash (LRW, rule 701.29). Fired by `TriggerCollectionService.performClash` after the clash ends; routes through `ClashTriggerTarget` (target creature an opponent controls). `IfWonClashEffect` clause applied only on a won clash (winner = strictly higher revealed mana value; 2-player). Inert until some card *initiates* a clash (see `ClashEffect`). E.g. Entangling Trap: `TapPermanentsEffect(TARGET)` + `IfWonClashEffect(SkipNextUntapEffect(TARGET))` |
| "When this enters/[event], clash with an opponent. If you win, [win-only effect]." | `addEffect(ON_ENTER_BATTLEFIELD, new ClashEffect(winOnlyEffect))` | any stack slot (usually `ON_ENTER_BATTLEFIELD`) | Clash-*source* (LRW, rule 701.29). Resolves by calling `performClash` for the controller; on a win dispatches the wrapped effect against the same entry (acts on the source). Pass `null` for a bare "clash with an opponent" with no reward. E.g. Oaken Brawler: `ClashEffect(new PutCountersOnSourceEffect(1, 1, 1))` |
| "[body], then clash with an opponent. If you win, repeat this process." | `addEffect(SPELL, new ClashEffect(List.of(bodyEffects...), null, true))` | SPELL | Clash-*source* loop (LRW). Runs the `beforeClash` body first, then clashes; `repeatWhileWinning=true` repeats the whole sequence while the controller keeps winning `performClash`, stopping on the first loss. E.g. Hoarder's Greed: `ClashEffect(List.of(new LoseLifeEffect(2), new DrawCardEffect(2)), null, true)` |
| "Whenever this creature attacks, clash with an opponent. If you win, target creature gains [keyword] until end of turn." | `target(new PermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), ...))` + `addEffect(ON_ATTACK, new ClashEffect(new GrantKeywordEffect(Keyword.X, GrantScope.TARGET)))` | `ON_ATTACK` | Clash-*source* with a **targeted** win reward. `ClashEffect` delegates targeting to `wrapped`, so the attack pipeline collects the target when the trigger goes on the stack; the clash resolves and the keyword is granted only on a win. E.g. Springjack Knight (double strike) |
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
| "return target creature card ... to the battlefield with a mannequin counter on it. For as long as that creature has a mannequin counter on it, it has 'When this creature becomes the target of a spell or ability, sacrifice it.'" | `ReturnCardFromGraveyardEffect.builder()...enterWithMannequinCounter(true).build()` | SPELL | Makeshift Mannequin. Adds a `CounterType.MANNEQUIN` counter on entry; `TriggerCollectionService` grants the `SacrificeSelfEffect` becomes-target ability while that counter is present (same slot as Illusionary Servant). |
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
| "As this enchantment enters, choose odd or even. Each creature with mana value of the chosen quality has haste. Each creature without … enters tapped." | `ChooseManaValueParityOnEnterEffect()` (`ON_ENTER_BATTLEFIELD`) + `GrantKeywordToCreaturesOfChosenParityEffect(Keyword.HASTE)` (STATIC) + `CreaturesOfUnchosenParityEnterTappedEffect()` (STATIC) | mixed | Odd/even chosen as it enters (zero is even), stored via `Permanent.getChosenManaValueParity()`; both statics affect all creatures regardless of controller. Ashling's Prerogative |
| "Champion a creature" / "Champion a [subtype]" | `ChampionCreatureEffect()` or `ChampionCreatureEffect(CardSubtype.X)` | `ON_ENTER_BATTLEFIELD` | Sacrifice on ETB unless you exile another matching creature you control; exiled card returns when this leaves. Changeling Hero |
| "When a [subtype] is championed with this creature, [targeted effect]" | put the targeted effect in `ON_CHAMPIONED` (alongside the `ChampionCreatureEffect` in `ON_ENTER_BATTLEFIELD`) | `ON_CHAMPIONED` | Fires when the champion ability exiles a creature. Player-targeting effects route through `PermanentChoiceContext.ChampionedTriggerTarget` (choose target player as the ability goes on the stack). Mistbind Clique (`TapPermanentsEffect(TARGET_PLAYERS_PERMANENTS, PermanentIsLandPredicate)`) |
| "target creature gains all creature types until end of turn" | `GrantKeywordEffect(Keyword.CHANGELING, GrantScope.TARGET)` | ability/SPELL | Changeling = every creature type; grant it for the turn. Give the ability a `PermanentPredicateTargetFilter(PermanentIsCreaturePredicate)`. Amoeboid Changeling |
| "target creature loses all creature types until end of turn" | `LoseAllCreatureTypesEffect()` | ability/SPELL | Strips every creature subtype (base/transient/granted) and nullifies Changeling until cleanup. Amoeboid Changeling |
| "creatures target player controls lose all creature types until end of turn" | `LoseAllCreatureTypesEffect(GrantScope.TARGET_PLAYERS_CREATURES)` | SPELL | Targets a player (`canTargetPlayer`); pair with `BoostAllCreaturesEffect(-2, 0, EachPermanentScope.TARGET_PLAYER)` for Ego Erasure ("get -2/-0 and lose all creature types"). Inverse of Shields of Velis Vel |
| "target creature becomes a [subtype] in addition to its other types" | `GrantSubtypeToTargetCreatureEffect(CardSubtype.X)` | ability/SPELL | Permanent grant (survives turn resets). Olivia Voldaren |
| "target land becomes the basic land type of your choice [in addition to its other types]" | `GrantBasicLandTypeToTargetEffect(EffectDuration.UNTIL_END_OF_TURN)` | ability/SPELL | Additive: adds the chosen type + its mana ability, prompts for the type. Navigator's Compass. Use `(duration, fixedSubtype)` for a fixed type (Aquitect's Will = ISLAND, CONTINUOUS) |
| "target land becomes the basic land type of your choice until end of turn" (no "in addition") | `GrantBasicLandTypeToTargetEffect(EffectDuration.UNTIL_END_OF_TURN, null, true)` | ability/SPELL | Type-**replacing** (rule 305.7): the land loses its other land types/mana ability and becomes the chosen type. Tideshaper Mystic |
| "put a +1/+1 counter on target creature" | `PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1)` | SPELL/trigger | |
| "put a stun counter on [target]" / "if it's your turn, put a stun counter on it" | `PutCounterOnTargetPermanentEffect(CounterType.STUN)` (optionally wrapped in `ConditionalEffect(new ControllerTurn(), …)`) | SPELL/ability | Stun counters are consumed instead of untapping (handled in `Permanent.untap()`). Works in multi-target groups (a stun counter is placed on each targeted permanent — Homesickness). Rapier Wit, Homesickness |
| "Whenever one or more +1/+1 counters are put on CARDNAME" | `AwardAnyColorManaEffect()` (or other effects) | `ON_SELF_PLUS_ONE_PLUS_ONE_COUNTERS_PUT` | Fired from `PermanentCounterSupport` after each +1/+1 placement event (once per event). Used by Berta, Wise Extrapolator |
| "put N +1/+1 counters on target creature" | `PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, N)` | SPELL/trigger | `(…, new XValue())` for "X +1/+1 counters" |
| "put a +1/+1 counter on each creature you control" | `PutCounterOnEachControlledPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentIsCreaturePredicate())` | SPELL/trigger | |
| "put a +1/+1 counter on each creature target player controls" | `PutPlusOnePlusOneCounterOnEachCreatureTargetPlayerControlsEffect()` bound to the player group via `target(...).addEffect(...)` | SPELL | Multi-target group binding. Practiced Offense |
| "target creature gains your choice of [keyword] or [keyword] until end of turn" | `GrantChosenKeywordToTargetEffect(List.of(Keyword.X, Keyword.Y))` bound to the creature group via `target(...).addEffect(...)` | SPELL | Multi-target group binding. Practiced Offense |
| "damage to target creature equal to the amount of mana spent to cast this spell" | `DealDamageToTargetCreatureEffect(new ManaSpentToCast())` | SPELL | Total mana spent snapshotted at cast time (`SpellCastingService` keys on the `ManaSpentToCast` amount). Molten Note |
| "put a -1/-1 counter on target creature" | `PutCounterOnTargetPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE, 1)` | SPELL/trigger | `(…, count, true)` regenerates if it survives (Gore Vassal) |
| "put a -1/-1 counter on each attacking creature / each other creature / each creature (X)" | `PutCounterOnEachMatchingPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE, amount, predicate, EachPermanentScope.ALL_PLAYERS)` | SPELL/trigger | predicate = `AllOf(IsCreature, IsAttacking)` / `AllOf(IsCreature, Not(IsSourceCard))` / `IsCreature` (with `XValue()`). Choking Fumes, Carnifex Demon, Black Sun's Zenith |
| "deals N damage to each attacking or blocking creature target player controls" | `DealDamageToEachMatchingPermanentEffect(N, AllOf(IsCreature, AnyOf(IsAttacking, IsBlocking)), EachPermanentScope.TARGET_PLAYER)` | ACTIVATED_ABILITY | Brigid, Hero of Kinsbaile |
| "put a +1/+1 counter on CARDNAME. If this is the Nth time this ability has resolved this turn, remove all +1/+1 counters and it deals that much damage to each creature and each player" | `PutCountersOnSelfEffect(PLUS_ONE_PLUS_ONE)` + `ConditionalEffect(new NthAbilityResolutionThisTurn(N), new RemoveAllCountersFromSelfEffect(PLUS_ONE_PLUS_ONE))` + `ConditionalEffect(new NthAbilityResolutionThisTurn(N), new MassDamageEffect(new EventValue(), true))` | ACTIVATED_ABILITY | Resolutions counted per source permanent this turn (engine-side, in `StackResolutionService`); the condition fires only on the exact Nth. The removal snapshots the removed count as the entry's event value; the mass damage reads it back as "that much". Ashling the Pilgrim (N=3) |
| "put a -1/-1 counter on each creature target player controls" | `PutCounterOnEachMatchingPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE, 1, IsCreature, EachPermanentScope.TARGET_PLAYER)` | SPELL/trigger | targets a player. Contagion Engine |
| "proliferate" | `ProliferateEffect()` | SPELL/trigger | |
| "put N <named> counters on CARDNAME" | `PutCountersOnSelfEffect(CounterType.X, N)` | trigger/ability | for a non-P/T named counter type on the source (e.g. Jar of Eyeballs: `CounterType.EYEBALL, 2`) |
| "Remove all <named> counters from CARDNAME: Look at the top X cards…" | `RemoveAllCountersAsCostEffect(CounterType.X)` + `LookAtTopCardsEffect.chooseOneToHandRestOnBottom(new XValue())` | ability | X = counters removed, snapshotted into xValue (Jar of Eyeballs: `CounterType.EYEBALL`). The look effect reads xValue, so it works for any counter-removal cost that snapshots X |

## Tap / untap

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "tap enchanted creature" | `TapPermanentsEffect(TapUntapScope.ENCHANTED)` | ability | aura's own activated ability; no targeting |
| "whenever enchanted creature is dealt damage, it deals that much damage to its controller" | `EnchantedCreatureDealsDamageEqualToDealtDamageToControllerEffect()` | `ON_ENCHANTED_CREATURE_DEALT_DAMAGE` | Spiteful Shadows |
| "tap target [permanent]" | `TapPermanentsEffect(TapUntapScope.TARGET)` | SPELL/ability | target filter from the ability/spell target spec |
| "untap target [permanent]" | `UntapPermanentsEffect(TapUntapScope.TARGET[, predicate])` | SPELL/ability | predicate restricts valid targets |
| "tap all [permanents] target player controls" | `TapPermanentsEffect(TapUntapScope.TARGET_PLAYERS_PERMANENTS, predicate)` | SPELL/ability | targets a player (Sleep, Tempest Caller) |
| "untap all [permanents] you control" | `UntapPermanentsEffect(TapUntapScope.CONTROLLED, predicate)` | SPELL | |
| "untap each other [creature] you control" | `UntapPermanentsEffect(TapUntapScope.OTHER_CONTROLLED_CREATURES, predicate)` | trigger/ability | Copperhorn Scout, Myr Galvanizer |
| "tap/untap this permanent" | `TapPermanentsEffect(TapUntapScope.SELF)` / `UntapPermanentsEffect(TapUntapScope.SELF)` | ability/trigger | self as effect (not cost) |
| "CARDNAME doesn't untap during your untap step" | `DoesntUntapEffect.self()` | STATIC | |
| "enchanted/equipped permanent doesn't untap during its controller's untap step" | `DoesntUntapEffect.enchanted()` | STATIC (on aura/equipment) | Claustrophobia, Dehydration, Numbing Dose, Heavy Arbalest |
| "[permanents matching X] don't untap during their controllers' untap steps" (global, any controller) | `MatchingPermanentsDoesntUntapEffect(predicate)` | STATIC | scans all battlefields, locks every matching permanent (incl. the source itself); Marble Titan (`PermanentPowerAtLeastPredicate(3)`) |
| "target permanent doesn't untap … for as long as you control CARDNAME" | `DoesntUntapEffect.targetWhileSourceOnBattlefield()` | ability/trigger/saga | piggybacks on companion `TapPermanentsEffect(TapUntapScope.TARGET)`; Dungeon Geists, Time of Ice |
| "target permanent doesn't untap … for as long as CARDNAME remains tapped" | `DoesntUntapEffect.targetWhileSourceTapped()` | ability | piggybacks on companion `TapPermanentsEffect(TapUntapScope.TARGET)`; Rust Tick |
| "tap all attacking creatures" | `TapPermanentsEffect(TapUntapScope.ALL_CREATURES, new PermanentIsAttackingPredicate())` | SPELL/trigger | no targeting |
| "untap all creatures that attacked this turn" | `UntapPermanentsEffect(TapUntapScope.ATTACKED_CREATURES)` | SPELL/trigger | Relentless Assault |
| "target permanent doesn't untap during its controller's next untap step" | `SkipNextUntapEffect(TapUntapScope.TARGET)` | ability/trigger | piggybacks on companion targeting effect (e.g. `TapPermanentsEffect(TapUntapScope.TARGET)`); Frost Titan, Watertrap Weaver, Wall of Frost |
| "those creatures don't untap during that player's next untap step" (all creatures target player controls) | `SkipNextUntapEffect(TapUntapScope.TARGET_PLAYERS_PERMANENTS, new PermanentIsCreaturePredicate())` | SPELL/trigger | targets a player; pair with `TapPermanentsEffect(TapUntapScope.TARGET_PLAYERS_PERMANENTS, …)`; Sleep |
| "those creatures don't untap during their controller's next untap step" (attacking creatures) | `SkipNextUntapEffect(TapUntapScope.ALL_CREATURES, new PermanentIsAttackingPredicate())` | SPELL/trigger | pair with `TapPermanentsEffect(TapUntapScope.ALL_CREATURES, new PermanentIsAttackingPredicate())`; Clinging Mists |

## Control / steal

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "gain control of target [permanent]" | `GainControlOfTargetEffect(ControlDuration.PERMANENT)` | SPELL | Permanent |
| "gain control of target creature with power <= creature count" | `GainControlOfTargetEffect(ControlDuration.PERMANENT)` + `PermanentPowerAtMostControlledCreatureCountPredicate` filter | ability | Dynamic power check vs creature count |
| "gain control of target creature until end of turn" | `GainControlOfTargetEffect(ControlDuration.END_OF_TURN)` | SPELL | Threaten |
| "gain control of target [permanent] for as long as you control [source]" | `GainControlOfTargetEffect(ControlDuration.WHILE_SOURCE_ON_BATTLEFIELD)` | ability | Olivia Voldaren |
| "you control enchanted creature" | `GainControlOfEnchantedTargetEffect()` | STATIC | Control Magic |
| "At the beginning of your end step, clash with an opponent. If you win, gain control of enchanted creature. Otherwise, that player gains control of enchanted creature." | `ClashForControlOfEnchantedCreatureEffect()` | CONTROLLER_END_STEP_TRIGGERED | Captivating Glance (Aura). Winner (controller on win, else clash opponent) gains control of the enchanted creature |
| "Enchanted permanent has 'At the beginning of your end step, sacrifice this permanent and attach [this Aura] to a creature or land you control.'" | `SacrificeEnchantedPermanentAndReattachSourceAuraEffect()` | ENCHANTED_PERMANENT_CONTROLLER_END_STEP_TRIGGERED | Nettlevine Blight (Aura, enchant creature or land). Fires on the enchanted permanent's controller's end step; that player sacrifices it, then moves the Aura (keeping its controller) onto one of their other creatures/lands. No legal destination → Aura stays unattached and dies as an SBA. Handler prompts only when 2+ destinations exist |

## Mana

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "add {C}" / "add one mana of [color]" | `AwardManaEffect(ManaColor.X, 1)` | ON_TAP/ability | |
| "add [color] for each [X] you control" | `AwardManaEffect(ManaColor.X, new PermanentCount(filter, CountScope.CONTROLLER))` | ability | Elvish Archdruid, Cabal Stronghold, Itlimoc, Koth −2, Powerstone Shard |
| "add {C} for each charge counter on it" | `AwardManaEffect(ManaColor.COLORLESS, new CountersOnSource(CounterType.CHARGE))` | ability | Shrine of Boundless Growth |
| "add [color] equal to its power" | `AwardManaEffect(ManaColor.X, new SourcePower())` | ability/trigger | Marwyn, the Nurturer; Molten-Core Maestro |
| "add one mana of any color" | `AwardAnyColorManaEffect()` | ON_TAP/ability/`ON_SELF_PLUS_ONE_PLUS_ONE_COUNTERS_PUT` | |
| "add N mana of any one color. Spend this mana only to cast instant and sorcery spells." | `AwardAnyOneColorInstantSorceryOnlyManaEffect(N)` | ability | Chooses one color, adds N instant/sorcery-only mana. Handled in `ActivatedAbilityExecutionService.resolveManaAbility`. Resonating Lute (granted to lands) |
| "add N mana of any one color" | `AwardAnyColorManaEffect(N)` | ability | |

## Prevention

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "prevent all combat damage that would be dealt this turn" | `PreventAllCombatDamageEffect()` | SPELL | |
| "prevent the next N damage that would be dealt to target" | `PreventDamageToTargetEffect(N)` | SPELL | |
| "prevent all damage that would be dealt to target creature this turn" | `PreventAllDamageToTargetCreatureEffect()` | SPELL/ability | Target creature only. Wellgabber Apothecary — combine with a `PermanentPredicateTargetFilter` for subtype/tapped restrictions |
| "If noncombat damage would be dealt to you, prevent that damage. You gain life equal to the damage prevented this way" | `PreventNoncombatDamageToControllerAndGainLifeEffect()` | STATIC | Purity. Combat damage unaffected (hooked only in the noncombat player-damage path) |
| "The next time a [color] source of your choice would deal damage to you this turn, prevent that damage" | `PreventNextDamageFromChosenColoredSourceEffect(CardColor.COLOR)` | activated ability | Circle of Protection cycle. One-shot: only the *next* damage event from the chosen source is prevented (double strike's second hit still lands). Not `PreventAllDamageFromChosenSourceEffect`, which prevents all damage that turn |
| "The next time a source of your choice would deal damage to you this turn, prevent that damage. You gain life equal to the damage prevented this way" | `PreventNextDamageFromChosenSourceAndGainLifeEffect()` | SPELL | Reverse Damage. Like the Circle of Protection effect but no color restriction (any permanent is a valid source) and the controller gains life equal to the damage prevented. One-shot: only the *next* damage event from the chosen source is prevented (combat or noncombat) |
| "The next time a source of your choice would deal damage to any target this turn, prevent that damage" | `PreventNextDamageFromChosenSourceToAnyTargetEffect()` | activated ability (usually with `SacrificeSelfCost`) | Sanctum Guardian. Like Reverse Damage but protects **any** target (player, planeswalker, or creature), not just the controller, and grants no life. One-shot: only the *next* damage event from the chosen source is prevented (combat or noncombat). Shield keyed by source ID in `GameData.sourceNextDamageToAnyTargetShields`, consumed by `DamagePreventionService.applyChosenSourceNextDamageToAnyTargetShield` (hooked in every player/creature/planeswalker damage path) |
| "When ~ is put into a graveyard from anywhere, shuffle it into its owner's library" | `ShuffleSelfFromGraveyardIntoLibraryEffect()` | `ON_SELF_PUT_INTO_GRAVEYARD_FROM_ANYWHERE` | Purity. Triggered ability — the card enters the graveyard first. For the *replacement* variant ("If ~ would be put into a graveyard from anywhere, ... shuffle it ... instead", e.g. Blightsteel Colossus) use `ShuffleIntoLibraryReplacementEffect` on `STATIC` |
| "{T}: All damage that would be dealt to target creature this turn by a source of your choice is dealt to this creature instead" | `RedirectTargetCreatureDamageFromChosenSourceToSelfEffect()` | activated ability (target creature, `requiresTap`) | Oracle's Attendants. Ability targets the protected creature; the source is chosen on resolution. Redirects to the source permanent (self). Works in both combat and noncombat creature-damage paths. Not Harm's Way (`PreventDamageFromChosenSourceAndRedirectToAnyTargetEffect`), which protects a player's permanents with a capped amount |

## Alternate casting costs / keywords

| Oracle text phrase | How | Notes |
|---|---|---|
| "Evoke {cost}" (cast for alternate cost; sacrificed when it enters) | `addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{W}"))))` + `addEffect(ON_ENTER_BATTLEFIELD, new SacrificeSelfIfEvokedEffect())` | Dawnfluke. Pure-mana alternate cost, so it can't be inferred from a sacrifice list — cast via `GameService.playCardWithEvoke`. The evoke flag rides `StackEntry`/`Permanent`; `EtbEffectResolver` resolves `SacrificeSelfIfEvokedEffect` to a `SacrificeSelfEffect` only when evoked (intervening-if, CR 603.4). Keep the actual ETB effect(s) as separate `ON_ENTER_BATTLEFIELD` entries. |

## Conditional wrappers

| Oracle text phrase | Wrapper | Notes |
|---|---|---|
| "you may [effect]" | `MayEffect(innerEffect, "prompt")` | Player chooses |
| "if you control three or more artifacts, [effect]" | `ConditionalEffect(new Metalcraft(), innerEffect)` | Metalcraft |
| "if a creature died this turn, [effect]" | `ConditionalEffect(new Morbid(), innerEffect)` | Morbid |
| "if a creature died under your control this turn, [effect]" | `ConditionalEffect(new CreatureDiedUnderYourControlThisTurn(), innerEffect)` | Controller-scoped morbid; checks `gameData.creatureDeathCountThisTurn` for the effect's controller (e.g. Essenceknit Scholar end-step draw) |
| "at the beginning of your end step, if a card left your graveyard this turn, [effect]" | `ConditionalEffect(new CardsLeftGraveyardThisTurn(), innerEffect)` on `CONTROLLER_END_STEP_TRIGGERED` | Checks `gameData.playersWhoseCardsLeftGraveyardThisTurn` (set in `GraveyardService.notifyCardsLeftGraveyard`); reset each turn. End-step intervening-if handled in `StepTriggerService` (e.g. Primary Research) |
| "if you attacked this turn, [effect]" | `ConditionalEffect(new Raid(), innerEffect)` | Raid |
| "if five or more mana was spent to cast that spell, [effect]" | `ConditionalEffect(new SpellManaSpentAtLeast(5), innerEffect)` inside `SpellCastTriggerEffect` | Trigger collector snapshots mana spent into stack entry `xValue` |
| Opus "[base]. If five or more mana was spent to cast that spell, [upgraded] instead" (single target/self) | `ConditionalReplacementEffect(new SpellManaSpentAtLeast(5), baseEffect, upgradedEffect)` inside `SpellCastTriggerEffect` | Use for "instead" upgrades where a base+conditional pair would double a target (e.g. Exhibition Tidecaller `MillEffect(3, TARGET_PLAYER)` → `(10, TARGET_PLAYER)`; Deluge Virtuoso boost uses the additive `BoostSelfEffect(1,1)` + `ConditionalEffect(...)` since boosts stack). Player/self targeting and the mana-spent `xValue` are plumbed through the targeted-trigger interaction. Additive stacking effects (`BoostSelfEffect`, `PutCountersOnSelfEffect`) should still use base + `ConditionalEffect` |
| "Repartee — Whenever you cast an instant or sorcery spell that targets a creature, [effect]" | `SpellCastTriggerEffect(CardAnyOfPredicate(INSTANT, SORCERY), List.of(effect), new StackEntryTargetsPermanentPredicate(new PermanentIsCreaturePredicate()))` on `ON_CONTROLLER_CASTS_SPELL` | The 3rd/4th-arg `castSpellTargetCondition` gates the trigger on the cast spell's chosen targets (a card-only `spellFilter` can't). Use the `(spellFilter, effects, TargetFilter, StackEntryPredicate)` overload when the resolved effect also targets (e.g. Graduation Day). Repartee itself is unmapped in Scryfall and adds nothing (Lecturing Scornmage, Rehearsed Debater, Stirring Hopesinger, Informed Inkwright, Inkshape Demonstrator, Forum Necroscribe) |
| "Ward—Discard a card" | `CounterUnlessDiscardsEffect()` on `ON_BECOMES_TARGET_OF_OPPONENT_SPELL` | Mirrors Ward {N} = `CounterUnlessPaysEffect(N)` (e.g. Frost Titan); the discard variant is countered immediately if the controller has no card |
| "if you've cast another instant or sorcery spell this turn, [effect]" | `ConditionalEffect(new ControllerCastAnotherSpellThisTurn(CardAnyOfPredicate(INSTANT, SORCERY)), innerEffect)` | SPELL | Excludes the resolving spell; checked at resolution time |
| "if [base], [effect]. If kicked, [upgraded effect] instead" | `ConditionalReplacementEffect(new Kicked(), baseEffect, upgradedEffect)(base, kicked)` | Kicker replaces |
| "if this spell was kicked, [additional effect]" | `ConditionalEffect(new Kicked(), innerEffect)` | Kicker adds |

## Own cast-cost reduction ("This spell costs {N} less to cast …")

All spell-self cost reductions use the single `ReduceOwnCastCostEffect(DynamicAmount)` on `EffectSlot.STATIC`. **Never add a per-variant `ReduceOwnCastCostIf*`/`Per*` record.** See `COST_MODIFICATION_HANDLERS.md`.

| Oracle text phrase | Effect (EffectSlot.STATIC) | Notes |
|---|---|---|
| "costs {N} less to cast" (flat) | `ReduceOwnCastCostEffect(new Fixed(N))` | |
| "costs {N} less to cast for each creature card in your graveyard" | `ReduceOwnCastCostEffect(new CardsInGraveyard(new CardTypePredicate(CREATURE), CountScope.CONTROLLER))` | N=1 → the amount already counts once per card; use `Scaled` for N>1. Ghoultree |
| "costs {N} less to cast for each creature on the battlefield" | `ReduceOwnCastCostEffect(new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.ANY_PLAYER))` | Blasphemous Act. Use `CONTROLLER`/`OPPONENTS` scope for "you control"/"an opponent controls" wordings |
| "costs {N} less to cast if you control a [permanent]" | `ConditionalEffect(new ControlsPermanent(predicate), new ReduceOwnCastCostEffect(new Fixed(N)))` | Academy Journeymage / Wizard's Retort / Wizard's Lightning (WIZARD), Lookout's Dispersal (PIRATE) |
| "costs {N} less to cast if you control three or more artifacts" | `ConditionalEffect(new Metalcraft(), new ReduceOwnCastCostEffect(new Fixed(N)))` | Stoic Rebuttal |
| "costs {N} less to cast if an opponent controls at least M more creatures than you" | `ConditionalEffect(new OpponentControlsMoreCreatures(M), new ReduceOwnCastCostEffect(new Fixed(N)))` | Avatar of Might (M=4, N=6) |
| "if an opponent controls more lands than you, [effect]" | `ConditionalEffect(new OpponentControlsMoreLands(), wrapped)` | SPELL | Gift of Estates: wraps `SearchLibraryEffect(new Fixed(3), new CardSubtypePredicate(CardSubtype.PLAINS), LibrarySearchDestination.HAND)`. Condition is strictly-more (difference ≥ 1) |
| "costs {N} less to cast if one or more cards left your graveyard this turn" | `ConditionalEffect(new CardsLeftGraveyardThisTurn(), new ReduceOwnCastCostEffect(new Fixed(N)))` | Wilt in the Heat |
| "costs {N} less to cast if it targets [permanent/spell]" | `ReduceOwnCastCostIfTargetingPermanentEffect` / `…IfTargetingControlledPermanentEffect` / `…IfTargetingStackEntryEffect` | **Kept as their own records** — the reduction gates on the being-cast spell's chosen first target (resolved inline in `CastingCostService.computeTargetBasedCostReduction`). Ajani's Response, Savage Stomp, Brush Off |
| "if you control a [subtype], [effect]" | `ConditionalEffect(new ControlsPermanent(new PermanentHasSubtypePredicate(subtype)), innerEffect)` | Permanent predicate check |
| "if you control a [matching permanent], [effect]" | `ConditionalEffect(new ControlsPermanent(predicate), innerEffect)` | Permanent check |
| "if you control a [subtype], [upgraded effect] instead" | `ConditionalReplacementEffect(new ControlsPermanent(filter), baseEffect, upgradedEffect)(new PermanentHasSubtypePredicate(subtype), baseEffect, upgradedEffect)` | Resolution-time replacement |
| "if that/target creature is a [subtype], [upgraded effect] instead" | `ConditionalReplacementEffect(new TargetPermanentMatches(filter), baseEffect, upgradedEffect)(new PermanentHasSubtypePredicate(subtype), baseEffect, upgradedEffect)` | Target permanent checked at resolution; falls back to base if missing or nonmatching |
| "Infusion — if you gained life this turn, [additional effect]" | `ConditionalEffect(new GainedLifeThisTurn(), innerEffect)` | Life gained tracked in `GameData.lifeGainedThisTurn` (via `LifeSupport`), cleared each turn; also valid as a static self-buff (`StaticBoostEffect(...GrantScope.SELF)`) e.g. Ulna Alley Shopkeep |
| "[base effect]. Infusion — if you gained life this turn, [upgraded effect] instead" | `ConditionalReplacementEffect(new GainedLifeThisTurn(), baseEffect, upgradedEffect)` | Resolution-time replacement, e.g. Withering Curse (mass -2/-2 upgraded to destroy all creatures) |
| "Infusion — At the beginning of your end step, [downside] unless you gained life this turn" | `ConditionalEffect(new DidntGainLifeThisTurn(), downsideEffect)` in `CONTROLLER_END_STEP_TRIGGERED` | `DidntGainLifeThisTurn` is the negation of `GainedLifeThisTurn`; for "sacrifice a permanent" use `SacrificePermanentThenEffect(new PermanentTruePredicate(), null, "a permanent")` (null thenEffect = no follow-up). E.g. Tragedy Feaster |
| "choose one —" | `ChooseOneEffect(List<ChooseOneOption>)` | Modal |
| "choose two —" | `ChooseOneEffect(List<ChooseOneOption>, 2)` | Modal | Pass `choicesRequired=2`; cast with negative bitmask via `ChooseOneEffect.encodeModeSelection(2, mode0, mode1)`. Chosen modes resolve in card-text order (Austere Command) |

## Turn / phase

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "take an extra turn after this one" | `ControllerExtraTurnEffect(1)` | SPELL | Non-targeting |
| "untap all creatures that attacked this turn. After this main phase, there is an additional combat phase followed by an additional main phase" | `AdditionalCombatMainPhaseEffect(1)` | SPELL | |
| "that player skips their next combat phase" (combat-damage trigger) | `SkipNextCombatPhaseEffect()` | ON_COMBAT_DAMAGE_TO_PLAYER | Non-targeting; the damaged player is baked in as `targetId`. Increments per-player `GameData.skipNextCombatPhaseCount`; that player jumps from precombat main straight to postcombat main. Blinding Angel |

## Copy

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "copy target instant or sorcery spell" | `CopySpellEffect()` | SPELL | Filter on the Card's SpellTarget via `target(...)`. Add `StackEntryControlledByPredicate` for "...spell you control" |
| "copy target creature spell you control. The copy gains haste and 'at the beginning of the end step, sacrifice this token.'" | `CopySpellEffect(null, true, true)` | SPELL | The creature-spell copy becomes a token, gains `HASTE`, and is registered for end-step sacrifice (`GameData.delayedActions` (a `SacrificeAtEndStep`)). Restrict targeting to creature spells you control via the mode's `StackEntryPredicateTargetFilter(StackEntryAllOfPredicate(StackEntryTypeInPredicate(CREATURE_SPELL), StackEntryControlledByPredicate))`. See Choreographed Sparks |
| "this spell can't be copied" | `card.setCantBeCopied(true)` (not an effect) | constructor | Honored by every copy handler (`CopySpellEffectHandler`, `CopySpellForEachOtherPlayerEffectHandler`, `CopyControllerCastSpellEffectHandler`) — the copy is simply not created. Choreographed Sparks |
| "choose one or both — copy target instant/sorcery you control • copy target creature spell you control" | `ChooseOneEffect` with 3 modes; the "both" mode uses `ChooseOneOption(label, List.of(copyIS, copyCreature), List.of(isFilter, creatureFilter))` | SPELL | Two distinct spell targets in one mode — see the "choose one or both with two distinct spell targets" row in CARD_PATTERNS_LANDS_SPELLS.md. Choreographed Sparks |
| "copy target instant or sorcery spell. If this spell was cast from a graveyard, copy that spell twice instead" | `CopySpellEffect()` + `ConditionalEffect(new CastFromZone(Zone.GRAVEYARD), new CopySpellEffect())` | SPELL | Increasing Vengeance — base copy plus a graveyard-conditional second copy; add `FlashbackCast` separately. Flashback can target a spell on the stack |
| "whenever a player casts an instant or sorcery spell, each other player copies that spell" | `CopySpellForEachOtherPlayerEffect()` | ON_ANY_PLAYER_CASTS_SPELL | Hive Mind (mandatory) |
| "whenever enchanted player casts an instant or sorcery spell, each other player may copy that spell" | `CopySpellForEachOtherPlayerEffect(true, new StackEntryControlledByEnchantedPlayerPredicate())` | ON_ANY_PLAYER_CASTS_SPELL | Curse of Echoes — the `StackEntryControlledByEnchantedPlayerPredicate` filter gates to casts by the enchanted player; each other player *may* copy |
| "whenever you cast an instant or sorcery spell, you may tap three untapped creatures you control. If you do, copy that spell" | `CopyControllerCastSpellOnSpellCastEffect(CardAnyOfPredicate(INSTANT, SORCERY), new TapMultiplePermanentsCost(3, PermanentIsCreaturePredicate))` | ON_CONTROLLER_CASTS_SPELL | Aziza, Mage Tower Captain — snapshots cast spell, `MayPayTapPermanentsEffect` + `CopyControllerCastSpellEffect` at resolution |
| "enters the battlefield as a copy of any creature on the battlefield" | `CopyPermanentOnEnterEffect(predicate, label)` | ON_ENTER_BATTLEFIELD | Clone |
| "target Shapeshifter becomes a copy of target creature until your next turn" | `MakeTargetCopyOfTargetCreatureUntilNextTurnEffect()` | activated ability | Two targets (Shapeshifter, creature); wire via multi-target `ActivatedAbility` ctor with `PermanentHasSubtypePredicate(SHAPESHIFTER)` + `PermanentIsCreaturePredicate` filters, min/max 2. Reverts at the controller's next turn. Shapesharer |

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
