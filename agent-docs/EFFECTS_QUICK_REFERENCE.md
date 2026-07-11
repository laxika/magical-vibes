# EFFECTS_QUICK_REFERENCE

Compact lookup: effect name + constructor signature, organized by category.
For detailed descriptions, targeting info, and examples, see EFFECTS_INDEX.md.

**How to use:** Search this file for keywords from the card text (e.g. "graveyard", "life", "shuffle", "destroy"). Once you find a candidate effect, grep EFFECTS_INDEX.md for its name to get full details.

- `RegisterDelayedReturnSourceTransformedEffect()` — ON_DEATH effect that registers a delayed end-step return from the source card's owner's graveyard to the battlefield transformed. Used by Loyal Cathar-style "When this dies, return it transformed at the beginning of the next end step."
- `RegisterDelayedCreateTokenEffect(CreateTokenEffect tokenEffect)` — registers a delayed trigger that resolves `tokenEffect` at the beginning of the next end step, creating the token(s) under the resolving controller's control. Used by Rukh Egg-style "When this dies, create a ... token at the beginning of the next end step."
- `RegisterCombatDamageReflectionEffect()` — "This turn, whenever an attacking creature deals combat damage to you, it deals that much damage to its controller" (Harsh Justice). Registers a `DelayedCombatDamageReflection` for the rest of the turn; `CombatDamageService` reflects each attacking creature's player-damage back to its controller. Pair with `setSpellCastTimingRestriction(DECLARE_ATTACKERS_IF_ATTACKED)`.

## Targeting rules (summary)

- Effects in SPELL slot with `canTargetPlayer()=true` force player targeting at cast time.
- Effects in SPELL slot with `canTargetPermanent()=true` force permanent targeting at cast time.
- ETB/triggered/saga slots: targeting declarations don't force spell-level targeting.
- `CostEffect` subtypes are excluded from targeting computation.
- Targeting is computed by `EffectResolution.needsTarget(card)` / `needsSpellCastTarget(card)`.
- Never call `setNeedsTarget`/`setNeedsSpellTarget` directly.

## Marker interfaces

- `CostEffect` — additional costs (sacrifice, discard, exile, counter removal, tap creature)
- `ManaProducingEffect` — mana abilities (CR 605.1a)

## Wrapper / modifier effects

Core wrappers (all take `CardEffect wrapped` as first/only effect arg):
- `MayEffect(CardEffect, String prompt)` — "you may"
- `MayPayManaEffect(String manaCost, CardEffect, String prompt)` — "you may pay {X}"
- `MayPayTapPermanentsEffect(TapMultiplePermanentsCost, CardEffect, String prompt)` — "you may tap N permanents"
- `ConditionalEffect(new Metalcraft(), CardEffect)` — 3+ artifacts
- `ConditionalEffect(new SpellManaSpentAtLeast(minMana), wrapped)` — mana spent to cast triggering spell >= N
- `ConditionalEffect(new Morbid(), CardEffect)` — creature died this turn
- `ConditionalEffect(new Raid(), CardEffect)` — attacked this turn
- `ConditionalEffect(new ControllerCastAnotherSpellThisTurn(filter), wrapped)` — another spell matching filter cast this turn (excludes resolving spell)
- `TriggeringCardConditionalEffect(CardPredicate, CardEffect)` — triggering card matches predicate
- `TriggeringPermanentConditionalEffect(PermanentPredicate, CardEffect)` — triggering permanent matches predicate
- `ConditionalEffect(new ControlsAnotherPermanent(filter), wrapped)` — controls another matching permanent
- `ConditionalEffect(new ControllerLifeAtLeast(threshold), wrapped)` — life >= N
- `ConditionalEffect(new ControllerTurn(), CardEffect)` — during your turn
- `ConditionalEffect(new NotControllerTurn(), CardEffect)` — during turns other than yours
- `ConditionalEffect(new ControlsPermanent(filter), wrapped)` — controls matching
- `EnchantedPermanentConditionalEffect(PermanentPredicate, CardEffect ifMatch, CardEffect ifNotMatch)` — aura active branch based on enchanted permanent predicate
- `ConditionalEffect(new OpponentControlsPermanent(filter), wrapped)` — opponent controls matching
- `ConditionalEffect(new OpponentControlsMoreLands(), wrapped)` — an opponent controls strictly more lands than you (Gift of Estates)
- `ConditionalEffect(new HasAttacker(predicate), wrapped)` — one or more matching attackers
- `CantAttackUnlessEffect(Condition, "unless clause")` — STATIC attack restriction; condition = `ControlsPermanentCount(1, filter)` / `DefendingPlayerControlsPermanent(filter)` / `AnyPlayerControlsPermanentCount(N, filter)` / `DefendingPlayerPoisoned()` / `OpponentDealtDamageThisTurn(minAmount)`
- `CreaturesCantAttackUnlessPredicateEffect(PermanentPredicate exemption)` — STATIC global: no creature can attack unless it matches exemption (Stormtide Leviathan)
- `CreaturesCantAttackControllerUnlessPredicateEffect(PermanentPredicate exemption)` — STATIC defender-scoped: creatures not matching exemption can't attack THIS controller only ("creatures without flying can't attack you", Form of the Dragon → exemption `PermanentHasKeywordPredicate(Keyword.FLYING)`)
- `ConditionalEffect(new GraveyardCardThreshold(threshold, filter), wrapped)` — graveyard threshold
- `ConditionalEffect(new CardsInLibraryAtLeast(threshold), wrapped)` — controller has N+ cards in library (Battle of Wits: upkeep + WinGameEffect)
- `ConditionalEffect(new AnyLibraryAtMost(threshold), wrapped)` — some player's library has N or fewer cards (Shelldock Isle: `{U}, {T}` + PlayImprintedCardWithoutPayingManaCostEffect, threshold 20)
- `ConditionalEffect(new CardsInHandAtLeast(threshold), wrapped)` — controller has N+ cards in hand (Imaginary Pet: upkeep + ReturnToHandEffect.self())
- `ConditionalEffect(new SourceCounterThreshold(threshold, counterType), wrapped)` — source counter threshold (e.g. 5+ growth counters)
- `ConditionalEffect(new ControlledCreaturesTotalPowerAtLeast(threshold), wrapped)` — total power of creatures you control >= N (Mosswort Bridge)
- `EnteringCreatureMinPowerConditionalEffect(int, CardEffect)` — entering power >= N
- `EnteringCreatureMaxPowerConditionalEffect(int, CardEffect)` — entering power <= N

Replacement wrappers (pick between base/upgraded at resolution):
- `ConditionalReplacementEffect(new Metalcraft(), baseEffect, upgradedEffect)(CardEffect base, CardEffect metalcraft)`
- `ConditionalReplacementEffect(new Morbid(), baseEffect, upgradedEffect)(CardEffect base, CardEffect morbid)`
- `ConditionalReplacementEffect(new Raid(), baseEffect, upgradedEffect)(CardEffect base, CardEffect raid)`
- `ConditionalReplacementEffect(new Kicked(), baseEffect, upgradedEffect)(CardEffect base, CardEffect kicked)`
- `ConditionalReplacementEffect(new ControlsPermanent(filter), baseEffect, upgradedEffect)(PermanentPredicate, CardEffect base, CardEffect upgraded)`
- `ConditionalReplacementEffect(new TargetPermanentMatches(filter), baseEffect, upgradedEffect)(PermanentPredicate, CardEffect base, CardEffect upgraded)` — target permanent predicate

Other wrappers:
- `ChooseOneEffect(List<ChooseOneOption>)` — modal spell
- `FlipCoinWinEffect(CardEffect)` — coin flip
- `ManaClashEffect()` — repeat: you + target opponent flip coins, 1 dmg per tails, until both heads
- `NthSpellCastTriggerEffect(int, List<CardEffect>)` — Nth spell trigger
- `ConditionalEffect(new NoSpellsCastLastTurn(), CardEffect)` — werewolf front
- `ConditionalEffect(new TwoOrMoreSpellsCastLastTurn(), CardEffect)` — werewolf back
- `ConditionalEffect(new CastFromZone(sourceZone), wrapped)` — resolves wrapped effect only if cast from that zone (`Zone.HAND` / `Zone.GRAVEYARD`)
- `ConditionalEffect(new CastNotFromHand(), CardEffect)` — resolves wrapped effect only if cast from anywhere other than hand (e.g. flashback)
- `ConditionalEffect(new Kicked(), CardEffect)` — kicked adds effect

See EFFECTS_INDEX.md for 20+ additional conditional wrappers (poison, blocker count, etc.)

## Damage

> **Power-based damage convention.** Any effect that deals damage equal to a creature's power
> (fight, bite, Pack Hunt, Berserker, Arc-Lightning-style source damage, planeswalker
> power-to-loyalty, `TargetDealsPowerDamageToTargetEffect`,
> `FightTargetsEffect`, `MassFightTargetCreatureEffect`,
> `SourceFightsTargetCreatureEffect`, the `SourcePower` dynamic amount,
> `PackHuntEffect`) must read the amount via
> `gameQueryService.getPowerBasedDamage(gameData, source)` — **never** via
> `getEffectivePower` with a manual `> 0` guard. The helper clamps negative power to 0 per
> CR 510.1a so the damage primitives never see negative values.

- `DealDamageToAnyTargetEffect(DynamicAmount, boolean cantRegenerate, boolean exileInsteadOfDie)`; `(int)`, `(int, boolean)`, `(DynamicAmount)` — any target. Amounts: `Fixed`, `XValue` (X spells / cost-snapshotted power), `SourcePower`, `CountersOnSource(CHARGE)`, …
- `DealDamageToAttackedTargetEffect(int damage)` — damage to the player or planeswalker attacked by the creature that caused the attack trigger
- `SourceFightsTargetCreatureEffect()` — source fights target
- `PackHuntEffect(CardSubtype)` — pack hunt
- `DealDamageToTargetAndTheirCreaturesEffect(int)` — player + their creatures
- `DealDamageToEachCreatureDamagedPlayerControlsEffect()` — damage to damaged player's creatures
- `DealDamageToTargetCreatureEffect(DynamicAmount, boolean unpreventable)`; `(int)`, `(int, boolean)`, `(DynamicAmount)` — target creature. Amounts: `Fixed`, `XValue`, `SourceToughness`, `PermanentCount` (subtype counts), `ManaSpentToCast`
- `DealDamageToTargetCreatureOrPlaneswalkerEffect(int)` — creature or planeswalker
- `DealDamageToTargetOpponentOrPlaneswalkerEffect(int)` — opponent or planeswalker
- `DealDamageToTargetPlayerOrPlaneswalkerEffect(DynamicAmount)` / `(int)` — any player (incl. controller) or planeswalker (Boggart Shenanigans; Brion Stoutarm's sacrificed-power `XValue`)
- `DealDamageToTargetOpponentAndUpToCreaturesThatPlayerControlsEffect(int opponentDamage, int creatureDamage, int maxCreatureTargets)` — target opponent plus up to N creatures that player controls
- `DealDamageToAllCreaturesAndPlaneswalkersTargetControlsEffect(int)` — all target controls
- `DealDamageToAllCreaturesTargetControlsEffect(int)` — creatures target controls
- `DealDamageToEachMatchingPermanentEffect(int, PermanentPredicate, EachPermanentScope)` — damage each matching permanent across `ALL_PLAYERS`/`TARGET_PLAYER`
- "If this is the Nth time this ability has resolved this turn, [X]" — `ConditionalEffect(new NthAbilityResolutionThisTurn(n), X)` on an activated ability; the engine counts resolutions per source permanent (`GameData.permanentAbilityResolutionsThisTurn`), condition is met only on the exact n-th resolution. Ashling the Pilgrim = `PutCountersOnSelfEffect(PLUS_ONE_PLUS_ONE)` + `ConditionalEffect(NthAbilityResolutionThisTurn(3), RemoveAllCountersFromSelfEffect(PLUS_ONE_PLUS_ONE))` + `ConditionalEffect(NthAbilityResolutionThisTurn(3), MassDamageEffect(new EventValue(), true))`
- `InnerFlameIgniterEffect()` — **card-specific.** On the exact third resolution this turn, creatures you control gain first strike until end of turn. Pair with `BoostAllOwnCreaturesEffect(1, 0)` in the same ability for the unconditional +1/+0 (Inner-Flame Igniter)
- `DealDamageToPlayersEffect(DynamicAmount, DamageRecipient)`; `(int, recipient)`; `.enchantedAttachedCount(PermanentPredicate)` — **unified player damage.** Recipients: `TARGET_PLAYER` (only targeting one; `Fixed`/`CardsInGraveyard` Scrapyard Salvo/`CardsInHand(TARGET_PLAYER)` Sudden Impact + Sword of War and Peace), `EACH_OPPONENT` (single eval, same value; `Fixed`/`CountersOnSource` Hallar), `EACH_PLAYER` (Slagstorm), `CONTROLLER` (self/pain lands), `ENCHANTED_PLAYER` (curse upkeep; `.enchantedAttachedCount` Curse of Thirst), `TARGET_PERMANENT_CONTROLLER` (Chandra's Outrage), `TRIGGERING_PERMANENT_CONTROLLER` (Magnetic Mine)
- `DealDamageToAnyTargetEffect.forTargetGroup(int damage, int targetGroup)` — damage aimed at a target group's chosen target (Goblin Barrage kicked target)
- `MassDamageEffect(DynamicAmount, damagesPlayers, damagesPlaneswalkers, PermanentPredicate)` — mass damage; convenience ctors `(int)`, `(int, damagesPlayers)`, `(DynamicAmount, damagesPlayers)`, `(int, usesXValue, damagesPlayers, filter)` (+ planeswalker overload)
- `DealDamageToAnyTargetAndGainLifeEffect(int damage, int lifeGain)` — damage + life gain
- `DealDamageToAnyTargetEqualToControlledSubtypeCountAndGainLifeEffect(CardSubtype, boolean)` — any target = subtype count
- `DealDividedDamageEffect` (unified divided/multi-target damage) — factories: `.chosenAmongAnyTargets(int)` (Fight with Fire kicked), `.chosenAmongTargetCreatures(int)` (Ignite Disorder), `.chosenAmongAnyTargetsEtb(int,int)` (Inferno Titan/Bogardan ETB), `.xAmongAttackingCreatures()` (Hail of Arrows), `.xAmongTargetCreaturesCantBlock()` (Huatli −X), `.xDividedEvenly()` (Fireball), `.ordered(List<Integer>)` (Cone of Flame/Arc Trail)
- `DealXDamageToAnyTargetAndGainXLifeEffect()` — X damage + X life
- `DealDamageToEachTargetEffect(DynamicAmount)` — full amount to each of multiple targets (Jaya's Immolating Inferno with `XValue`)
- `TargetDealsPowerDamageToTargetEffect()` — bite (group indices `(sourceTargetGroup, victimTargetGroup)` default to 0, 1)
- `TargetCreatureDealsPowerDamageToSelfEffect()` — target deals its power to itself
- `FightTargetsEffect()` — fight (group indices `(firstTargetGroup, secondTargetGroup)` default to 0, 1)
- `MassFightTargetCreatureEffect()` — Alpha Brawl-style mass fight
- `PreventNoncombatDamageToControllerAndGainLifeEffect()` — STATIC: prevent all noncombat damage to controller; they gain life equal to the damage prevented (Purity). Hooked in `DamageSupport.dealDamageToPlayer`
- `PreventCombatDamageToAttackingCreaturesYouControlEffect()` — STATIC: prevent all combat damage dealt to attacking creatures the source's controller controls (Dolmen Gate). Checked in `DamagePreventionService.applyCreaturePreventionShield` via `permanent.isAttacking()` + `isCombatDamage`
- `PreventSpellDamageToOpponentAndCreateTokensEffect(CreateTokenEffect token)` — STATIC: if a spell you control would deal damage to an opponent, prevent it and create one `token` per 1 damage prevented (Hostility). Hooked in `DamageSupport.dealDamageToPlayer`
- `PreventAllDamageToTargetCreatureEffect()` — prevent all damage to target creature this turn (Wellgabber Apothecary). Adds target to `GameData.creaturesWithAllDamagePrevented`, checked in `DamagePreventionService.applyCreaturePreventionShield`, cleared at turn cleanup
- `PreventAllDamageToControllerFromAttackingCreaturesEffect()` — SPELL: prevent all damage attacking creatures would deal to the controller this turn (Deep Wood). Adds controller to `GameData.playersWithDamageFromAttackersPrevented`; combat damage prevented in `CombatDamageService.applyPlayerDamage`, noncombat only when the source permanent is attacking. Cleared at turn cleanup. Pair with `setSpellCastTimingRestriction(DECLARE_ATTACKERS_IF_ATTACKED)`
- `PreventDamageToOtherCreaturesAndAddPlusCountersEffect()` — STATIC: prevent all damage (combat or noncombat, any source) to *another* creature you control and put a +1/+1 counter on it per 1 damage prevented (Vigor). Checked in `DamagePreventionService.applyCreaturePreventionShield`; the effect is on a different permanent than the one being damaged
- `PreventNextDamageFromChosenColoredSourceEffect(CardColor color)` — one-shot: prevent the *next* damage event a chosen source of that color would deal to you this turn (Circle of Protection cycle). Source chosen on resolution; shield in `GameData.playerSourceNextDamageShields`, consumed by `DamagePreventionService.applyPlayerNextSourceDamageShield`
- `PreventNextDamageFromChosenSourceAndGainLifeEffect()` — one-shot: prevent the *next* damage event a chosen source (any color) would deal to you this turn and gain that much life (Reverse Damage). Source chosen on resolution; shield in `GameData.playerSourceNextDamageShields` with `gainLife=true`, consumed by `DamagePreventionService.applyPlayerNextSourceDamageShield` (which grants the life via `LifeSupport`)
- `PreventNextDamageFromChosenSourceToAnyTargetEffect()` — one-shot: prevent the *next* damage event a chosen source (any color) would deal to **any** target this turn — player, planeswalker, or creature, combat or noncombat (Sanctum Guardian; usually an activated ability with `SacrificeSelfCost`). Source chosen on resolution; shield (source ID) in `GameData.sourceNextDamageToAnyTargetShields`, consumed by `DamagePreventionService.applyChosenSourceNextDamageToAnyTargetShield` (hooked in `DamageSupport` and `CombatDamageService` player/creature/planeswalker paths)
- `RedirectTargetCreatureDamageFromChosenSourceToSelfEffect()` — activated ability targeting a creature: all damage a chosen source (picked on resolution) would deal to that creature this turn is dealt to the source permanent instead (Oracle's Attendants). Shield in `GameData.creatureDamageRedirectShields`, checked in both combat and noncombat creature-damage paths via `DamagePreventionService.applyCreatureRedirectShields`; reuses `pendingSourceRedirectDamage`
- `DoubleDamageEffect()` — double all damage (static)
- `DoubleDamageToEnchantedPlayerEffect()` — double damage dealt to enchanted player (static Curse)
- `DoubleControllerDamageEffect(StackEntryPredicate, boolean)` — double controller's damage
- `SacrificePermanentThenEffect(PermanentPredicate, CardEffect, String)` — sacrifice then effect
- `SpellCastTriggerEffect(CardPredicate, List<CardEffect>)` + overloads — spell cast trigger
- `BecomePreparedEffect()` — source becomes "prepared" (Strixhaven); exiles a castable copy of its prepare spell (back face)
- `MakeTargetCreaturePreparedEffect()` — target creature becomes prepared; no-op if already prepared or no prepare spell
- `MakeTargetCreatureUnpreparedEffect()` — target creature becomes unprepared; no-op if not prepared

See EFFECTS_INDEX.md "Damage" section for 15+ additional niche damage effects.

## Destruction / sacrifice

- `DestroyTargetPermanentEffect(boolean cantRegen)` or `(boolean, CreateTokenEffect)` — destroy target
- `DestroyTargetPermanentAtEndStepEffect()` — destroy at end step
- `SacrificeTargetPermanentAtEndStepEffect()` — sacrifice the target at next end step (Lowland Oaf); sacrifice, not destruction (ignores indestructible/regeneration)
- `DestroyAllPermanentsEffect(PermanentPredicate)` or `(PermanentPredicate, boolean)` — board wipe
- `DestroyAllPermanentsAndGainLifePerDestroyedEffect(PermanentPredicate, int)` — wipe + life
- `EachPlayerChoosesCreatureDestroyRestEffect()` — choose one, destroy rest
- `DestroyAllCreaturesAndCreateTokenFromDestroyedCountEffect(String, List, Set)` — wipe + X/X token
- `DestroyTargetPermanentAndControllerSearchesLibraryToBattlefieldEffect(CardPredicate, boolean may[, boolean tapped])` — destroy + controller searches to battlefield (tapped optional, e.g. Erode)
- `DestroyTargetAndEachPlayerSearchesBasicLandToBattlefieldEffect()` — destroy + each searches
- `EachOpponentMaySearchLibraryForBasicLandToBattlefieldTappedEffect()` — opponents search
- `EachPlayerMaySearchLibraryForCreaturesToHandEffect(DynamicAmount count)` — each player (APNAP) may tutor up to `count` creatures to hand; `()` = X (Weird Harvest)
- `DestroyTargetLandAndDamageControllerEffect(int)` — destroy land + damage
- `DestroyTargetPermanentAndDamageControllerIfDestroyedEffect(int)` — destroy + conditional damage
- `DestroyUpToTargetsThenReturnFromGraveyardEffect()` — destroy each targeted permanent and return cards put into graveyard this way under your control (multi-target via ability `minTargets`/`maxTargets`)
- `DestroyTargetPermanentThenEffect(EventStat, CardEffect thenEffect, ThenEffectRecipient[, PermanentPredicate])` — collapsed destroy-plus-value family. Destroy the target, then resolve an existing then-effect. `recipient` CONTROLLER (you) / TARGET_CONTROLLER (destroyed permanent's controller). `EventStat` NONE/MANA_VALUE/TOUGHNESS snapshots the destroyed permanent's last-known stat onto `eventValue` for a `GainLifeEffect(EventValue())` / `BoostSelfEffect(EventValue(), Fixed(0))` then-effect. Then-effects: `GainLifeEffect`, `BoostSelfEffect`, `LoseLifeEffect`, `GivePoisonCountersEffect`. Optional `PermanentPredicate` gates the then-effect on the destroyed permanent's state (Death's Caress HUMAN). Then-effect happens even if destruction fails (indestructible)
- `DestroySourcePermanentEffect()` — destroy source
- `DestroyCreatureBlockingThisEffect()` — destroy blocker
- `DestroyCombatOpponentAtEndOfCombatEffect(PermanentPredicate filter, boolean cannotBeRegenerated)` — Basilisk-style "blocks or becomes blocked by a [filter] creature, destroy that creature at end of combat". Put on ON_BLOCK + ON_BECOMES_BLOCKED (`TriggerMode.PER_BLOCKER`); filter re-checked at resolution (Deathgazer nonblack). Destroys at end of combat, not immediately
- `SacrificePermanentsEffect(count, PermanentPredicate, SacrificeRecipient)` — collapsed forced-sacrifice family. `SacrificeRecipient` = CONTROLLER / TARGET_PLAYER / EACH_PLAYER / EACH_OPPONENT. Bare `PermanentIsCreaturePredicate` → single-select "sacrifice a creature" (Cruel Edict, Grave Pact, Stitcher's Apprentice); any other filter → multi-permanent choice (Storm Fleet Arsonist, Yawning Fissure, Destructive Force). int-count sugar ctor
- `SacrificeCreatureAndControllerGainsLifeEqualToToughnessEffect()` — sacrifice + life = toughness
- `SacrificeCreatureToCreateTokensEqualToToughnessEffect(CreateTokenEffect template, PermanentPredicate filter)` — controller sacrifices a matching creature, then creates X copies of `template` where X = sacrificed creature's toughness (template `amount` ignored). Wrap in `MayEffect` for "you may sacrifice" (e.g. Feed the Pack)
- `ForcedCostOrElseEffect(CostEffect, List<CardEffect>[, boolean optional])` — cost-like instruction; if it cannot be performed, resolve fallback effects. `optional=true` makes it a "you may [cost]. If you don't, [fallback]" choice (Yawgmoth Demon); default `false` is mandatory (Archdemon of Greed). Supported costs: `SacrificePermanentCost` (single) and `SacrificeMultiplePermanentsCost` (N of a filter, e.g. Rathi Dragon). Supported fallbacks: `TapPermanentsEffect(SELF)`, `DealDamageToPlayersEffect(CONTROLLER, Fixed)`, `SacrificeSelfEffect`
- `SacrificeAttackingCreaturesEffect(int base, int metalcraft)` — sacrifice attackers
- `EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect(int, CardPredicate)` — mass reanimate
- `SacrificeSelfEffect()` — sacrifice self
- `SacrificeSelfThenDealDamageToTargetPlayerEffect(int damage)` — sac source; if sacrificed, deal N to stack entry's targetId player (Booby Trap trigger)
- `SacrificeSelfIfEvokedEffect()` — evoke sacrifice; ON_ENTER_BATTLEFIELD, fires only when cast for evoke cost
- `SacrificeUnlessDiscardCardTypeEffect(CardType)` / `(CardType, boolean random)` — sacrifice unless discard (`random=true` = discard at random, Pillaging Horde; `null` type = any card)
- `SacrificeUnlessReturnOwnPermanentTypeToHandEffect(CardType)` — sacrifice unless bounce own
- `ChampionCreatureEffect(CardSubtype)` — champion a creature (null subtype = any creature); exile on ETB, return when source leaves
- `SacrificeSelfAndDrawCardsEffect(int)` — sacrifice + draw
- `SacrificeAtEndOfCombatEffect()` — sacrifice at EOC
- `SacrificeTargetThenRevealUntilTypeToBattlefieldEffect(Set<CardType>)` — Polymorph
- `RevealUntilNonlandCardsToHandRestToBottomEffect(int)` — reveal until N nonland to hand, rest (lands) to bottom in any order (Fathom Trawl)

See EFFECTS_INDEX.md "Destruction" section for 10+ additional niche destruction/sacrifice effects.

### Sacrifice costs

- `ExileSelfCost()` — exile self as cost
- `SacrificeSelfCost()` — sacrifice self as cost
- `RemoveAllCountersAsCostEffect(CounterType)` — remove all counters of a type as cost; count snapshotted into xValue (Jar of Eyeballs: `EYEBALL`)
- `RemoveAllCountersFromSelfEffect(CounterType)` — resolution effect: remove all counters of a type from self; count snapshotted as the entry's event value so a later effect reads "that much" via `EventValue` (Ashling the Pilgrim + `MassDamageEffect(new EventValue(), true)`)
- `SacrificeCreatureCost()` or `(boolean trackMV)` or `(boolean trackMV, boolean trackPower)` or `(boolean, boolean, boolean trackToughness)` or `(boolean, boolean, boolean, boolean excludeSelf)` — sacrifice creature
- `SacrificeArtifactCost()` — sacrifice artifact
- `SacrificePermanentCost(PermanentPredicate, String[, excludeSource])` — sacrifice matching permanent; use creature+subtype predicates with `excludeSource=false` for source-eligible "sacrifice a [subtype]"
- `DiscardCardTypeCost(CardPredicate, String)` — discard matching card
- `RemoveCounterFromSourceCost(int, CounterType)` — remove counters from self
- `CrewCost(int)` — crew
- `TapCreatureCost(PermanentPredicate)` — tap creature
- `PayLifeCost(int)` — pay life
- `ExileCardFromGraveyardCost(CardType)` / `(CardSubtype)` + overloads — exile graveyard card (subtype ctor for "Exile an Elf card", Scarred Vinebreeder)
- `ReturnCreatureToHandCost()` — additional spell cost: return a creature you control to hand (Familiar's Ruse)

See EFFECTS_INDEX.md "Sacrifice costs" for additional cost effects.

## Counter spells

- `CounterSpellEffect()` — counter target spell
- `CounterSpellAndCreateTreasureTokensEffect()` — counter + treasures
- `CounterSpellAndExileEffect()` — counter + exile
- `CounterSpellIfControllerPoisonedEffect()` — counter if poisoned
- `TargetSpellControllerLosesLifeEffect(int)` — target spell controller loses life
- `TargetSpellControllerDiscardsEffect(int)` — target spell controller discards
- `CounterUnlessPaysEffect(int)` or `(int, boolean useX, boolean exileIfCountered)` — counter unless pays
- `CounterUnlessDiscardsEffect()` — counter unless controller discards a card (Ward—Discard a card)
- `CounterlashEffect()` — counter target spell, then may cast from hand sharing a card type without paying mana cost
- `RegisterDelayedManaEqualToTargetSpellManaValueEffect(ManaColor)` — Scattering Stroke clash reward: wrap in `ClashEffect` before the counter; may add {C} equal to the countered spell's mana value at your next main phase
- `MayCastFromHandWithoutPayingManaCostEffect()` — marker for may-cast-from-hand routing in PendingMayAbility
- `MayCastFromHandSharingNameWithSpellCastThisTurnEffect()` — Twinning Glass activated ability: offer to cast a nonland hand card for free if its name matches a spell any player cast this turn (reuses the Counterlash routing)
- `ReplaceControlledCounterWithExileAndPlayEffect()` — STATIC (Guile): your counters exile the spell instead and you may play it free
- `MayPlayExiledCounteredCardEffect()` — marker for the Guile free-play routing in PendingMayAbility
- `CantBeCounteredEffect()` — can't be countered (static)
- `CreatureSpellsCantBeCounteredEffect()` — creatures can't be countered (static)
- `CreatureEnteringDontCauseTriggersEffect()` — Torpor Orb (static)
- `ETBDoubleTriggerEffect(CardPredicate)` — double ETB triggers (static)
- `CreaturesEnterAsCopyOfSourceEffect()` — Essence of the Wild (static)
- `ExileOpponentCardsInsteadOfGraveyardEffect()` — Leyline of the Void (static)

## Bounce / return to hand

- `ReturnToHandEffect` — unified bounce, **static factories only**: `.target()` (bounce target), `.targetAndControllerLosesLife(1)` (Vapor Snag), `.self()` (bounce source), `.allPermanentsMatching(filter)` (mass bounce matching permanents; null = every permanent — pass `PermanentIsCreaturePredicate` for creatures), `.permanentsTargetPlayerControls(filter)` (River's Rebuke), `.permanentsTargetPlayerOwns(filter)` (Hurkyl's Recall, owner-based)
- `ReturnTargetPermanentToHandWithManaValueConditionalEffect(int, CardEffect)` — bounce + MV bonus
- `ReturnSelfToHandOnCoinFlipLossEffect()` — bounce self on coin flip loss
- `ReturnPermanentsOnCombatDamageToPlayerEffect()` or `(PermanentPredicate)` — Ninja-style
- `PutTargetOnBottomOfLibraryEffect()` — tuck bottom
- `PutTargetOnTopOfLibraryEffect()` — tuck top
- `PutTargetPermanentIntoLibraryNFromTopEffect(int)` — tuck N from top
- `PutSourceCardFromGraveyardOnTopOfOwnersLibraryEffect()` — ON_DEATH: put dying source on top of owner's library (Undying Beast)

## Graveyard return

- `ReturnCardFromGraveyardEffect.builder().destination(HAND|BATTLEFIELD|TOP_OF_OWNERS_LIBRARY)...build()` — unified graveyard return (see EFFECTS_INDEX.md for full builder API)
- `ReturnTriggeringLandFromGraveyardToBattlefieldEffect(UUID landCardId)` — Sacred Ground's trigger effect: return the identified land from the graveyard to the battlefield under its owner's control. Register the template with `null` on the `ON_ALLY_LAND_PUT_INTO_GRAVEYARD_BY_OPPONENT` slot; the collector stamps the real card id.
- `ReturnOneOfEachSubtypeFromGraveyardToHandEffect(List<CardSubtype>)` — one of each subtype
- `PutTargetCardsFromGraveyardOnTopOfLibraryEffect(CardPredicate)` — graveyard to top of library
- `ReturnTargetCardsFromGraveyardToHandEffect(CardPredicate, int)` — up to N cards to hand
- `ShuffleTargetCardsFromGraveyardIntoLibraryEffect(CardPredicate, int)` — target player shuffles N cards
- `ReturnDyingCreatureToBattlefieldAndAttachSourceEffect()` — reanimate + equip
- `PutCardFromOpponentGraveyardOntoBattlefieldEffect(boolean tapped)` — opponent's card to battlefield
- `UndyingReturnEffect()` — Undying (CR 702.93) resolution: return the dying card from its owner's graveyard to the battlefield with a +1/+1 counter. Do NOT add to a card directly; it is pushed automatically by `PermanentRemovalService` when a creature with the `UNDYING` keyword dies with no +1/+1 counters. The keyword is loaded from Scryfall.
- `PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect()` — opponent's creature with exile
- `GrantTargetCreatureCardGraveyardCastAndCopyActivatedAbilitiesEffect()` — target creature card in any graveyard may be cast this turn; when cast, source gains its activated abilities
- `GrantSourceActivatedAbilitiesUntilEndOfTurnEffect(List<ActivatedAbility>, String)` — delayed source grant used after casting the selected graveyard creature

## Draw / discard / hand manipulation

- `DrawCardEffect(DynamicAmount)` or `(int)` — controller draws that many; use `XValue` for "draw X", `PermanentCount`/`CardsInGraveyard`/`CountersOnSource` for "draw a card for each …"
- `EachPlayerDrawsCardEffect(DynamicAmount)` or `(int)` — each player (turn order) draws that many; `XValue()` for "each player draws X" (Prosperity), `int` for a fixed count
- `DrawCardForTargetPlayerEffect(DynamicAmount, boolean requiresUntapped, boolean targets)` or `(int)` — target/entry player draws; `XValue` for "target player draws X"
- `DrawAndDiscardCardEffect(int draw, int discard)` — loot
- `DiscardAndDrawCardEffect(int discard, int draw)` — rummage
- `DiscardEffect(DynamicAmount, DiscardRecipient, boolean random)` — the whole discard family; `recipient` ∈ {`CONTROLLER`, `TARGET_PLAYER`, `EACH_PLAYER`, `EACH_OPPONENT`}, `random` picks chosen vs random discard. `(int, recipient, random)` / `(DynamicAmount, recipient)` / `(int, recipient)` convenience ctors (last two non-random). `CountersOnSource(CHARGE)` for per-charge-counter (Shrine of Limitless Power), `XValue()` for Mind Shatter (`TARGET_PLAYER`, random)
- `DiscardHandEffect(DiscardRecipient)` — discard entire hand(s); no-arg = controller
- `DiscardOwnHandThenDrawThatManyEffect()` — discard entire hand, then draw that many
- `DiscardOwnHandThenDrawEqualToTargetPlayerHandSizeEffect()` — discard entire hand, then draw equal to target player's hand size (counted at draw time)
- `EachPlayerDiscardsHandThenDrawsThatManyEffect()` — each player (APNAP) discards their entire hand, then draws that many
- `EachPlayerDiscardsAnyNumberThenDrawsThatManyEffect()` — each player (APNAP) discards any number of cards (their choice), then draws that many (Flux)
- `ExileTopCardsMayPlayUntilNextTurnEffect(DynamicAmount count)` or `(int count)` — exile top N from library, may play until end of your next turn (owner-relative expiry via `ExileSupport.grantPlayUntilOwnersNextTurn`). Use `EventValue()` for "equal to the excess damage dealt this way" (Archaic's Agony)
- `ExileTargetPermanentMayPlayUntilNextTurnEffect()` — exile the target permanent, its owner may play it until end of their next turn (e.g. Suspend Aggression; pair with a permanent target filter). Tokens exiled this way cease to exist
- `ExileTargetCardFromGraveyardMayPlayUntilNextTurnEffect(CardPredicate filter, boolean ownGraveyardOnly)` — exile a targeted graveyard card matching the filter, controller may play it until end of their next turn (e.g. Practiced Scrollsmith; ETB graveyard-target flow via `MultiGraveyardChoice`)
- `ExileTargetInstantOrSorceryFromOpponentGraveyardMayCastEffect()` — exile a targeted instant/sorcery from an opponent's graveyard; controller may cast it **this turn**, spending mana of any type, and it is exiled instead of going to a graveyard (Nita, Forum Conciliator). Uses `exilePlayPermissions` + `exilePlayPermissionsExpireEndOfTurn` + `exilePlayAnyManaType` + `exileInsteadOfGraveyard`. Targets graveyard (`canTargetGraveyard()`/`canTargetAnyGraveyard()`)
- `PlayTargetCardFromGraveyardWithoutPayingManaCostEffect(CardPredicate filter)` — "you may play target [filter] card from your **own** graveyard without paying its mana cost" (Horde of Notions). On resolution offers a may-play: land → battlefield, else cast for free. Targets graveyard (own-only via `targetsControllersGraveyardOnly()`); routed by `MayCastHandlerService.handlePlayFromGraveyardChoice`
- `PlayImprintedCardWithoutPayingManaCostEffect()` — Hideaway "you may play the exiled card without paying its mana cost" activated ability (Howltooth Hollow). Offers a may-play of the source permanent's imprinted (face-down exiled) card: land → battlefield (counts as the land play for the turn), else cast from exile for free. Routed by `MayCastHandlerService.handlePlayImprintedCardChoice`. Gate with `ConditionalEffect(<play condition>, …)` (e.g. `NoPlayerHasCardsInHand`); pair with `ImprintFromTopCardsEffect(N)` on ON_ENTER_BATTLEFIELD
- `ChooseCardsFromTargetHandEffect(int count, List<CardType> excludedTypes[, List<CardType> includedTypes], HandChoiceDestination destination[, boolean returnOnSourceLeave])` — reveal target's hand, caster chooses N card(s) → `DISCARD` / `EXILE` / `TOP_OF_LIBRARY` (Duress, Kitesail Freebooter, Agonizing Memories)
- `RevealCardsChooseOneToDiscardEffect(PermanentPredicate countFilter)` — target reveals X cards **of their choice** (X = number of the caster's permanents matching `countFilter`), then the caster picks one for the target to discard (Thieving Sprite, `PermanentHasAnySubtypePredicate(FAERIE)`). Unlike `ChooseCardsFromTargetHandEffect` the rest of the hand stays hidden; two-phase interaction (`RevealCardsFromHandChoice` → `ChooseRevealedCardToDiscardChoice`), phase 1 skipped when the hand is already ≤ X
- `TargetRevealsCardsControllerChoosesDiscardEffect(int revealCount)` — target player reveals `revealCount` cards **of their choice** from hand (whole hand if fewer); the controller sees only those and picks one for the target to discard (Blackmail). Two-stage `RevealCardsDiscardChoice` interaction; `canTargetPlayer()`. Contrast `ChooseCardsFromTargetHandEffect` (whole hand revealed, controller chooses)
- `RevealTargetHandDrawPerMatchingCardEffect(List<CardSubtype> subtypes, List<CardColor> colors)` — target opponent reveals hand; draw one card per card matching any subtype/color (counted once). Pair with `target(PlayerPredicateTargetFilter(OPPONENT))` (Baleful Stare)
- `DiscardAllCardsOfChosenColorEffect()` — caster chooses a color, target player discards all cards of that color from hand. Pair with `target(PlayerPredicateTargetFilter(ANY))` (Persecute)
- `LookAtHandEffect()` — look at hand
- `ShuffleHandIntoLibraryAndDrawEffect()` — wheel
- `PutHandOnBottomOfLibraryAndDrawEffect()` — target player puts hand on bottom of library, draws that many (Teferi's Puzzle Box, `EACH_DRAW_TRIGGERED`)
- `EachPlayerShufflesHandAndGraveyardIntoLibraryEffect()` — Timetwister-style

## Library manipulation

- `SearchLibraryEffect(DynamicAmount count, CardPredicate filter, LibrarySearchDestination destination, XManaValueBound manaValueBound, int castFromGraveyardCount)` — unified library search (collapsed the `SearchLibraryFor*` family). Convenience: `()` unrestricted-to-hand (Diabolic Tutor), `(filter)` filtered-to-hand, `(filter, destination)`, `(count, filter, destination)`, `(filter, int count, int cfg)` flashback tutor (Increasing Ambition `(null,1,2)`), `(filter, destination, bound)`. destination ∈ `HAND`/`BATTLEFIELD`/`BATTLEFIELD_TAPPED`/`TOP_OF_LIBRARY`; by-name via `CardNamedPredicate` (Squadron Hawk); creature MV/colour/subtype via filter + `XManaValueBound` (Citanul Flute, Birthing Pod `(true,1)`, Green Sun's Zenith `CardColorPredicate(GREEN)`, Myr Turbine)
- `SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect()` — Cultivate
- `SearchLibraryForCurseToBattlefieldAttachedToEnchantedPlayerEffect()` — Curse (name not shared with one already on enchanted player) onto battlefield attached to enchanted player; Curse of Misfortunes
- `SearchTargetLibraryForCardsToGraveyardEffect(int, Set<CardType>)` — target library to graveyard
- `SearchTargetLibraryForCardsToExileEffect(int count)` — search target player's library for up to `count` cards, exile them, then that player shuffles (Jester's Cap, count=3); no play permission. Targets player
- `RevealTopCardOfLibraryEffect()` or overloads — reveal top card
- `RevealTopCardRemoveTargetFromCombatIfMatchEffect(CardPredicate)` — reveal top; if match, remove the engine-set attacking creature (targetId) from combat; then bottom the card (Lost in the Woods, ON_CREATURE_ATTACKS_YOU)
- `RevealTopCardsAndSeparateEffect(int)` — reveal + separate into piles
- `ScryEffect(int)` — scry N
- `SurveilEffect(int)` — surveil N
- `ShuffleLibraryEffect(boolean targetPlayer)` — shuffle library (false=controller's, true=target player's)
- `ShuffleIntoLibraryEffect()` — shuffle spell into library
- `ShuffleSelfAndGraveyardIntoLibraryEffect()` — shuffle self + graveyard into library
- `ShuffleSelfFromGraveyardIntoLibraryEffect()` — triggered ability: shuffle the source card from its owner's graveyard into their library (pair with `ON_SELF_PUT_INTO_GRAVEYARD_FROM_ANYWHERE`, e.g. Purity)
- `ShuffleGraveyardIntoLibraryEffect(boolean targetPlayer)` — shuffle graveyard into library (targetPlayer=true targets, false=controller's)
- `ShuffleTargetCardsFromGraveyardIntoLibraryEffect(CardPredicate, int)` — shuffle N cards from graveyard
- `CastTopOfLibraryWithoutPayingManaCostEffect(Set<CardType>)` — cast top free
- `ImprovisationCapstoneEffect(int totalManaValueThreshold)` — exile from library until total MV ≥ threshold; `ImprovisationCapstoneCastChoice` interaction lets controller cast any number of exiled instants/sorceries/etc. without paying (`ImprovisationCapstoneCastSupport`)
- `RevealTopCardMayPlayFreeOrExileEffect()` — reveal top, play free or exile

## Mill

- `MillEffect(DynamicAmount, MillRecipient)` — the recipient mills cards. `recipient` ∈ {`CONTROLLER`, `TARGET_PLAYER`, `EACH_OPPONENT`}; `(int, recipient)` ctor for a fixed count. `XValue()` for mills X, `CountersOnSource(CHARGE)` for Grindclock, `CardsInHand(TARGET_PLAYER)` for Dreamborn Muse's hand-size mill. "Each player mills N" = `(N, CONTROLLER)` + `(N, EACH_OPPONENT)`. Flashback "twice X" via `ConditionalReplacementEffect(CastFromZone(GRAVEYARD), Mill(XValue(),TARGET_PLAYER), Mill(Scaled(XValue(),2),TARGET_PLAYER))` (Increasing Confusion)
- `MillControllerAndMayPlayFromGraveyardThisTurnEffect()` — mill 1, grant play-from-graveyard permission until end of turn
- `PlayAdditionalLandsEffect(int count)` — grant controller `count` extra land plays this turn (Summer Bloom)
- `MillHalfLibraryEffect()` — mill half (target player)
- `NameCardMillTargetGainLifeEffect()` — controller names a card, target player mills 1; if the milled card matches the name, controller gains life = its mana value (Lammastide Weave; targets a player)

## Exile

- `ExileTargetPermanentEffect()` or `(boolean returnEndStep)` — exile target
- `FlickerEffect.exileTargetReturnAtEndStep([boolean tapped])` — exile target + return at end step (SELF: `exileSelfReturnAtEndStep()`; mass: `exilePlayersPermanentsReturnAtStep(PermanentPredicate, TurnStep)`; immediate: `flickerTarget()` / `flickerTargetWithCounters(int)` / `flickerTargetWithBonus(CardSubtype, CardEffect)`)
- `ExileGraveyardCardsEffect(GraveyardExileScope.TARGET_PLAYER_ENTIRE)` — exile target player's whole graveyard (also: `OWN`, `TARGET_CARDS_ANY_GRAVEYARD` [+`CardTypePredicate`], `TARGET_CARDS_OPPONENT_GRAVEYARD`, `ALL_PLAYERS`, `ALL_OPPONENTS`)
- `ExileAllCreaturesEffect()` — exile all creatures
- `ExileAllPermanentsEffect(PermanentPredicate)` — exile matching permanents
- `ExileTargetPermanentAndTrackWithSourceEffect()` — exile + track exiled card with source permanent (cards "exiled with" it)
- `ReturnAllCardsExiledWithSourceEffect()` — ON_DEATH trigger: return all cards exiled with the source to the battlefield under owners' control (Helvault)
- `ReturnTargetCardFromExileToHandEffect(CardPredicate, boolean ownedOnly)` — exile to hand

## Tokens

- `CreateTokenEffect(...)` — create tokens (many constructors, see EFFECTS_INDEX.md). The count is a `DynamicAmount` (`int` ctors are `Fixed` sugar): any "create a token for each …" or "create X tokens" = this effect + an amount (`XValue`, `PermanentCount`, `CardsInGraveyard`, `CountersOnSource`, `AttachmentsOnSource`, `OpponentPoisonCounters`, `CreatureDeathsThisTurn`, `Divided`, …) — never a new effect class
- `CreateTokenEffect.whiteSpirit(int)` — 1/1 white Spirit creature token with flying
- `CreateTokenEffect.blackZombie(int)` — 2/2 black Zombie creature token
- `CreateTokenEffect.whiteSoldier(int)` — 1/1 white Soldier creature token
- `CreateTokenEffect.ofTreasureToken(int)` — treasure tokens
- `CreateTokenWithDyingSourceCountersEffect(CreateTokenEffect template)` — `ON_DEATH`: if the dying creature had ≥1 +1/+1 counter, create `template` with that many +1/+1 counters (e.g. Ambitious Augmenter's Fractal)
- `MoveDyingSourceCountersToTargetCreatureEffect()` — `ON_DEATH`: if the dying creature had ≥1 counter (any type), move all of its counters onto up to one target creature (e.g. Scolding Administrator). Intervening-if snapshots the counters at death; targets any creature
- For "create a token that gains [keyword] until end of turn", set `CreateTokenEffect`'s `grantedKeywordsUntilEndOfTurn` (e.g. `new CreateTokenEffect(amount, name, p, t, color, colors, subtypes, innateKeywords, Set.of(Keyword.HASTE))` — Artistic Process Elemental gains haste). Distinct from the token's innate `keywords`.
- `CreateXTokenWithXCountersEffect(String tokenName, int power, int toughness, CardColor color, Set<CardColor> colors, List<CardSubtype> subtypes, CounterType counterType)` — create one token with X counters of `counterType` from ability/spell X value (e.g. Berta's Fractal with `PLUS_ONE_PLUS_ONE`)
- `ExileTargetCardFromGraveyardAndCreateTokenCopyEffect(CardPredicate, ownGraveyardOnly, additionalSubtypes, grantHaste, exileAtEndStep)` — exile graveyard target, create token copy with optional extra subtypes/haste/end-step exile
- `CreateTokenCopyOfTargetPermanentEffect()` or `(grantHaste, exileAtEndStep)` or `(additionalSubtypes, additionalTypes, powerOverride, toughnessOverride, Map<CounterType, Integer> initialCounters)` — create token copy of targeted permanent; optional type/subtype/P/T overrides, post-ETB counters, granted haste, and exile at next end step (Heat Shimmer)
- `CreateTokenCopyOfTargetCreatureForTargetPlayerEffect()` — target player creates a token copy of target creature you control (two targets: player + creature); Echocasting Symposium

## Life

- `GainLifeEffect(DynamicAmount[, GainLifeRecipient])` or `(int)` — gain life; dynamic derivations via `DynamicAmount` (PermanentCount, CardsInHand, CardsInGraveyard, CountersOnSource, GreatestPowerAmongControlled, XValue, Scaled, Sum, …). `recipient=TARGET_CONTROLLER` gives the life to the target permanent's controller: "its controller gains life = its toughness" = `GainLifeEffect(new TargetToughness(), GainLifeRecipient.TARGET_CONTROLLER)` (Condemn). `TargetPower()` is the power analogue: "you gain life = target's power, then destroy it" = `GainLifeEffect(new TargetPower())` + `DestroyTargetPermanentEffect(false)` (Chastise)
- `TargetPlayerGainsLifeEffect(DynamicAmount|int)` — target gains life (`XValue` for "target player gains X life", Stream of Life)
- `DoubleTargetPlayerLifeEffect()` — double target life
- `SetTargetPlayerLifeToSpecificValueEffect(int)` — set life to value
- `SetEachPlayerLifeToHighestAmongPlayersEffect()` — each player's life total becomes the highest among all players (Arbiter of Knollridge)
- `SetEachPlayerLifeToCreatureCountEffect()` — each player's life total becomes the number of creatures they control (Biorhythm)
- `SetControllerLifeToSpecificValueEffect(int)` — non-targeting "your life total becomes N" (Form of the Dragon end-step trigger)
- `LoseLifeEffect(DynamicAmount amount, LoseLifeRecipient recipient, boolean controllerGainsLifeLost)` — the whole life-loss family. `recipient` = CONTROLLER / TARGET_PLAYER / EACH_PLAYER / EACH_OPPONENT; `controllerGainsLifeLost` drains total life lost back to you. Sugar: `(int)` = `(Fixed, CONTROLLER, false)` (lose N life), `(int, recipient)`, `(DynamicAmount, recipient)`, `(int, recipient, boolean)`. Amount: `EventValue()` for "equal to the life you gained" (Sanguine Bond `(new EventValue(), TARGET_PLAYER)`); `PermanentCount(filter, CONTROLLER)` for "1 life for each … you control" (Bishop); `new XValue()` for Exsanguinate `(new XValue(), EACH_OPPONENT, true)`. `canTargetPlayer()` = recipient==TARGET_PLAYER
- `TargetPlayerLosesLifeAndControllerGainsLifeEffect(int, int)` — drain target (fixed gain, NOT gains-life-lost)
- `PlayersCantGainLifeEffect()` — can't gain life (static)

## Poison counters

- `GivePoisonCountersEffect(int, PoisonRecipient)` — give poison; recipient routes CONTROLLER (self) / TARGET_PLAYER / EACH_PLAYER / ENCHANTED_PERMANENT_CONTROLLER
- `GivePoisonCountersEffect(int, TARGET_PLAYER, CardPredicate spellFilter)` — `ON_CONTROLLER_CASTS_SPELL` trigger descriptor (Hand of the Praetors)

## Creature pump / boost

- `BoostTargetCreatureEffect(DynamicAmount power, DynamicAmount toughness)` or `(int, int)` — target +X/+Y. Any "for each …", "+X/+X" (X paid), or "where X is …" target-pump = this effect + a `model/amount/DynamicAmount` — never a new per-variant class. The amount evaluates against the SOURCE, so counting refers to the effect's controller, not the pumped target. E.g. `(new XValue(), new XValue())` (Untamed Might), `(new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER), same)` (Elder of Laurels), `(new Sum(new Fixed(1), new CardsInGraveyard(filter, CountScope.CONTROLLER)), new Fixed(0))` (Ancestral Anger)
- `CardNamedPredicate(String cardName)` — card filter for exact name match (use with graveyard-count boosts above)
- `BoostSelfEffect(DynamicAmount, DynamicAmount)` or `(int, int)` — self +X/+Y; one-shot in trigger/ability slots, continuous in STATIC. Any "for each …" self-boost = this effect + a `model/amount/DynamicAmount` (`PermanentCount`, `CardsInGraveyard`, `AttachmentsOnSource`, `CreaturesBlockingSource`, `OpponentPoisonCounters`, `ImprintedCreaturePower/Toughness`, `LandsMatchingImprintedName`, `XValue`, `Scaled`, `Fixed`) — never a new per-variant effect class
- `AttachedBoostEffect(DynamicAmount, DynamicAmount, GrantScope)` — STATIC +X/+Y on the enchanted/equipped creature (`ENCHANTED_CREATURE`/`EQUIPPED_CREATURE`). Attached-scope sibling of `BoostSelfEffect`; any "for each …" aura/equipment boost = this effect + a `DynamicAmount`. `CountScope.CONTROLLER` = the aura/equipment's controller (CR 109.5). Negative per-count = wrap in `Scaled(…, -1)`. Blanchwood Armor, Blackblade Reforged, Bonehoard, Runechanter's Pike, Quag Sickness, Strata Scythe — never a new `BoostCreaturePer*` class
- `DoubleSelfPowerToughnessEffect()` — double self P/T
- `BoostAllOwnCreaturesEffect(DynamicAmount, DynamicAmount)` or `(…, PermanentPredicate)` — all own +X/+Y; `(int, int[, PermanentPredicate])` convenience wraps in `Fixed`. Any "where X is …" / power- or graveyard-derived mass own-pump = this effect + a `DynamicAmount` (evaluated once at resolution) — e.g. `new GreatestPowerAmongControlled()` (Overwhelming Stampede), `new CardsInGraveyard(new CardTypePredicate(CREATURE), CONTROLLER)` (Garruk, the Veil-Cursed). Never a new per-variant class
- `BoostAllCreaturesEffect(DynamicAmount, DynamicAmount)` or `(…, PermanentPredicate)` or `(…, PermanentPredicate, EachPermanentScope)` — creatures +X/+Y; `(int, int[, PermanentPredicate])` / `(int, int, EachPermanentScope)` convenience wraps in `Fixed`. Scope `ALL_PLAYERS` (default, both sides) or `TARGET_PLAYER` ("creatures target player controls", `canTargetPlayer`, Shields of Velis Vel). "X paid" mass pump = `new Scaled(new XValue(), mult)` / `new XValue()` (Ichor Explosion, Flowstone Slide)
- `StaticBoostEffect(int, int, Set<Keyword>, GrantScope, PermanentPredicate)` — static +X/+Y + keywords
- `SetBasePowerToughnessEffect(int, int)` — set target creature's base P/T until end of turn; `(int, int, GrantScope)` for continuous static (e.g. `ENCHANTED_CREATURE`, Deep Freeze)
- `SetAllOwnCreaturesBasePowerToughnessEffect(DynamicAmount, DynamicAmount)` or `(int, int)` — set base P/T of all creatures you control to X/X until end of turn (layer 7b, modifiers apply on top). X-cost ability = `new XValue()` (Mirror Entity)
- `SwitchPowerToughnessEffect()` — switch P/T

## P/T setting / counters

- `SetPowerToughnessToAmountEffect(DynamicAmount power, DynamicAmount toughness)` — CDA that sets P/T on a 0/0 base (pass the same amount for both). Replaced the `PowerToughnessEqualTo*` family + `BoostSelfBySlimeCountersOnLinkedPermanentEffect`. Amounts: `PermanentCount(IsLand/IsCreature/IsArtifact/HasSubtype…, CONTROLLER)` (lands/creatures/artifacts/Swamps you control), `CardsInGraveyard(filter, CONTROLLER|ANY_PLAYER)`, `CardsInHand(CONTROLLER)` (hand size), `ControllerLifeTotal()` (life total), `CountersOnLinkedPermanent(type, id)` (linked-permanent counters)
- `PutCountersOnSourceEffect(int power, int toughness, int amount)` — counters on self
- `PutCountersOnSourceEqualToEnteringPowerEffect(int power, int toughness, boolean optional)` — ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD: put counters on self = entering creature's power; `optional` = "you may" (Hamletback Goliath)
- `PutCountersOnSelfEffect(CounterType)` — one counter of a type on self (charge, +1/+1, study, etc.)
- `PutCountersOnSelfEffect(CounterType, int count)` — N counters of a type on self (e.g. Withengar Unbound: 13 +1/+1)
- `PutCounterOnTargetPermanentEffect(CounterType, int)` — counters on target permanent (`PLUS_ONE_PLUS_ONE`/`MINUS_ONE_MINUS_ONE`/…); `(…, new XValue())` for "X counters"; `(…, count, boolean regenerateIfSurvives)` (Gore Vassal); `withTargetRestriction(…, targetPredicate)` to restrict legal targets; `(…, count, PermanentPredicate)` for a non-targeting own-permanent choice
- `PutPlusOnePlusOneCounterOnEachCreatureTargetPlayerControlsEffect()` — +1/+1 on each creature the target player controls (bind to the player target group via `target(...).addEffect(...)`)
- `PutCounterOnEachControlledPermanentEffect(CounterType, int, PermanentPredicate)` — counters on each own permanent matching predicate (use `PermanentIsCreaturePredicate` for "each creature you control")
- `PutCounterOnEachMatchingPermanentEffect(CounterType, int|DynamicAmount, PermanentPredicate, EachPermanentScope)` — counters on each matching permanent across `ALL_PLAYERS`/`TARGET_PLAYER` (each attacking / other / all creatures; each creature target player controls)
- `PutCounterOnEnchantedCreatureEffect(CounterType)` or `(CounterType, int)` — counter(s) on enchanted creature
- `EnterWithCountersEffect(CounterType, DynamicAmount)` — "enters the battlefield with … counters" (as-enters replacement effect): fixed = `Fixed(n)`, X paid = `XValue()`, "for each …" = a counting amount (`CreatureDeathsThisTurn`, `Sum(PermanentCount(...), CardsInGraveyard(...))`, …). "If kicked" / "Raid —" variants wrap it in `ConditionalEffect(new Kicked()/new Raid(), …)`
- Increment keyword — keyword-driven (`Keyword.INCREMENT`, auto-loaded from Scryfall): +1/+1 counter on self when mana spent on a cast spell exceeds self's current power or toughness. Add nothing to the card; behavior lives in `TriggerCollectionService.collectIncrementTriggers` (resolution effect: `IncrementTriggerEffect`). E.g. Ambitious Augmenter
- `ProliferateEffect()` — proliferate
- `KickerEffect(String cost)` — kicker declaration

## Keywords / abilities

- `GrantKeywordEffect(Keyword, GrantScope)` or `(Keyword, GrantScope, PermanentPredicate)` or `(Set<Keyword>, GrantScope)` — grant keywords. Add a trailing `GrantDuration` (`(Keyword, GrantScope, GrantDuration)` / `(Set<Keyword>, GrantScope, GrantDuration)`) for one-shot duration: `END_OF_TURN` (default) or `UNTIL_YOUR_NEXT_TURN`. In `STATIC` slot the grant is continuous and the duration is ignored. `GrantKeywordEffect.toTargetIf(Keyword, PermanentPredicate grantCondition)` — grant to target ONLY if it matches `grantCondition` (checked at resolution; target stays legal, only the grant is conditional — Vampire's Zeal, Blessing of Belzenlok). `grantCondition` is distinct from `filter`/`targetPredicate()`.
- `GrantChosenKeywordToTargetEffect(List<Keyword> options)` — prompt to choose one keyword from options, grant to target permanent until end of turn (bind to its target group in multi-target spells — Practiced Offense)
- `GrantFlashToCardTypeEffect(CardPredicate)` — flash to card types (static)
- `ActivateCreatureAbilitiesAsThoughHasteEffect()` — controller may activate abilities of creatures they control as though they had haste (static; lifts summoning-sickness on ability activation only, does NOT grant haste — Thousand-Year Elixir)
- `GrantActivatedAbilityEffect(ActivatedAbility, GrantScope)` or `(ActivatedAbility, GrantScope, PermanentPredicate)` — grant ability
- `GrantAdditionalBlockEffect(int)` — block N additional
- `RegenerateEffect()` or `(boolean targetsPermanent)` — regenerate
- `ProtectionFromColorsEffect(Set<CardColor>)` — protection from colors (static)
- `ProtectionFromSubtypesEffect(Set<CardSubtype>)` — protection from subtypes (static)
- `GrantSubtypeToTargetCreatureEffect(CardSubtype)` — target creature "becomes a [subtype] in addition to its other types" (permanent, added to `grantedSubtypes`)
- `GrantBasicLandTypeToTargetEffect(EffectDuration[, CardSubtype fixedSubtype][, boolean replacing])` — target land becomes a chosen basic land type. Default adds "in addition to its other types" (Navigator's Compass / Aquitect's Will); `replacing=true` makes the land **become** the type, losing its others per rule 305.7 (Tideshaper Mystic, UNTIL_END_OF_TURN only)
- `NonbasicLandsBecomeTypeEffect(CardSubtype)` — STATIC, global: every nonbasic land (any controller) becomes the basic land type, losing its other land types/abilities and producing that type's mana per rule 305.7. Basic lands unaffected. Blood Moon (MOUNTAIN)
- `LoseAllCreatureTypesEffect(GrantScope)` — creatures lose all creature types until end of turn; `TARGET` = single creature (Amoeboid Changeling), `TARGET_PLAYERS_CREATURES` = all creatures target player controls (Ego Erasure); "gains all creature types" = `GrantKeywordEffect(Keyword.CHANGELING, sameScope)`
- **Paradigm** (`Keyword.PARADIGM` on card, not an effect) — engine handled by `ParadigmService`: first resolve exiles spell + registers `GameData.ParadigmDelayedTrigger`; each precombat main fires `ParadigmCastCopyEffect` → copy in exile + `ParadigmMayCastFromExileEffect` may-cast (`ParadigmCastSupport`)

## Combat restrictions / evasion

- `CantBeBlockedEffect()` — unblockable (static)
- `CantBeBlockedByFewerThanNCreaturesEffect(int minBlockers)` — generalized menace: can't be blocked except by N+ creatures (static). Menace = 2; Guile = 3
- `CantBeBlockedIfAttackingAloneEffect()` — can't be blocked while attacking alone (static)
- `CantBlockEffect()` — can't block (static)
- `MustAttackEffect()` — must attack (static)
- `MustAttackControllerNextTurnEffect()` — targets a player: during their next turn every creature they control attacks you (the controller) if able (Taunt). SPELL slot; `canTargetPlayer`
- `MustBeBlockedIfAbleEffect()` — must be blocked (static)
- `MustBeBlockedByAllCreaturesEffect()` — Lure (static)
- `MustBeBlockedByAllCreaturesThisTurnEffect()` — one-shot targeted Lure: all creatures able to block target creature this turn do so (Alluring Scent); SPELL slot, `canTargetPermanent`
- `MustBlockTargetCreatureEffect()` — two-target spell: blocker group (0) must block blocked group (1) this turn if able (Hunt Down)
- `EnchantedCreatureCantAttackOrBlockEffect()` — Pacifism (static)
- `MakeCreatureUnblockableEffect()` — target unblockable this turn
- `CanBeBlockedOnlyByFilterEffect(PermanentPredicate blockerPredicate, String allowedBlockersDescription)` — static evasion on the source: it can be blocked only by blockers matching the predicate (Fear-like, e.g. Dread Warlock = black creatures)
- `GrantCanBeBlockedOnlyByFilterToOwnCreaturesEffect(PermanentPredicate creatureFilter, PermanentPredicate blockerPredicate, String allowedBlockersDescription)` — SPELL one-shot: your creatures matching `creatureFilter` (null = all) can be blocked only by blockers matching `blockerPredicate` until end of turn. Dread Charge = both filters `PermanentColorInPredicate(BLACK)`. Affected creatures snapshotted at resolution; restriction stored transiently on each `Permanent`
- `CantBlockThisTurnEffect(TapUntapScope scope[, PermanentPredicate filter])` — creature(s) can't block this turn (one-shot). `TARGET` (target creature, multi-target-group), `TARGET_PLAYERS_PERMANENTS` (target player's / targeted planeswalker's controller's creatures), `ALL_CREATURES` (mass, filtered). NOT the static `CantBlockEffect()`.

## Tap / untap

- `EnchantedCreatureDealsDamageEqualToDealtDamageToControllerEffect()` — enchanted creature deals damage equal to amount dealt to its controller (ON_ENCHANTED_CREATURE_DEALT_DAMAGE)
- `TapPermanentsEffect(TapUntapScope.TARGET)` — tap target
- `TapPermanentsEffect(TapUntapScope.SELF)` — tap self · `.ENCHANTED` — tap aura's enchanted creature
- `TapPermanentsEffect(TapUntapScope.TARGET_PLAYERS_PERMANENTS, filter)` — tap that player's matching permanents
- `TapPermanentsEffect(TapUntapScope.ALL_CREATURES, filter)` — tap all creatures matching filter (`PermanentIsAttackingPredicate` = all attackers)
- `UntapPermanentsEffect(TapUntapScope.TARGET[, PermanentPredicate])` — untap target (predicate restricts targets)
- `UntapPermanentsEffect(TapUntapScope.SELF)` — untap self · `.ALL_TARGETS` — untap all targets
- `UntapPermanentsEffect(TapUntapScope.CONTROLLED, filter)` — untap all you control matching · `.OTHER_CONTROLLED_CREATURES` — untap each other creature you control · `.ATTACKED_CREATURES` — untap creatures that attacked this turn
- `UntapPermanentsEffect(TapUntapScope.TARGET_PLAYERS_PERMANENTS, filter)` — untap all of target player's permanents matching filter (Early Harvest: `PermanentAllOf(land, BASIC supertype)` = target player's basic lands)
- `MatchingPermanentsDoesntUntapEffect(PermanentPredicate)` — global static: every permanent matching the predicate (any controller, incl. the source) doesn't untap during its controller's untap step; Marble Titan (`PermanentPowerAtLeastPredicate(3)`)
- `StorageMatrixEffect()` — global static (Storage Matrix): while any permanent carrying it is untapped, each player's untap step pauses (`UntapStepService.storageMatrixRestrictionApplies`) so the active player chooses artifact/creature/land (a `ColorChoice` with `ChoiceContext.StorageMatrixUntapChoice`); only permanents of the chosen type untap that step. No card-side targeting/config — just `addEffect(EffectSlot.STATIC, new StorageMatrixEffect())`.
- `DoesntUntapEffect.self()` — this permanent doesn't untap (static) · `.enchanted()` — attached aura/equipment's host doesn't untap (static) · `.targetWhileSourceOnBattlefield()` — target doesn't untap while source on battlefield (Dungeon Geists / Time of Ice) · `.targetWhileSourceTapped()` — while source stays tapped (Rust Tick); TARGET factories piggyback on a companion `TapPermanentsEffect(TapUntapScope.TARGET)`
- `SkipNextUntapEffect(TapUntapScope.TARGET)` — target permanent skips next untap (piggybacks on companion targeting effect) · `.TARGET_PLAYERS_PERMANENTS, filter` — that player's matching permanents · `.ALL_CREATURES, filter` — all creatures matching filter (`PermanentIsAttackingPredicate` = all attackers)
- `IfWonClashEffect(wrapped)` — clash-only marker on `EffectSlot.ON_CONTROLLER_CLASHES`: the wrapped effect applies only if the controller won the clash ("If you won, ..."). Consumed by `TriggerCollectionService.fireClashTriggers` at trigger time (not a stack effect). See Entangling Trap: tap target opponent creature + `IfWonClashEffect(SkipNextUntapEffect(TARGET))`. Clash is performed via `performClash` (2-player: both reveal top card, strictly-higher mana value wins).
- `IfLostClashEffect(wrapped)` — mirror of `IfWonClashEffect`: applies only when the controller did **not** win. Pair a won- and lost-variant so exactly one branch fires when the base effect happens regardless of outcome but only a detail differs (Rebellion of the Flamekin: two `MayPayManaEffect("{1}", CreateTokenEffect(...))` differing only in granted `HASTE` on the won branch). Non-targeting clash triggers go straight onto the stack.
- `ClashEffect(List<CardEffect> beforeClash, CardEffect onWin, boolean repeatWhileWinning)` — the clash-*source* stack effect; convenience ctor `ClashEffect(onWin)` = `(List.of(), onWin, false)`. Each iteration dispatches `beforeClash` (via each effect's own handler, against the same entry), performs the clash for the controller via `performClash`, dispatches `onWin` on a win, and with `repeatWhileWinning` repeats the whole sequence until a lost clash (deck-out counts as a loss). "Clash with an opponent. If you win, [X]" = `ClashEffect(X)`; `onWin` may be null for a bare "clash with an opponent". Mirrors `FlipCoinWinEffect`. E.g. Oaken Brawler = `ClashEffect(new PutCountersOnSourceEffect(1, 1, 1))`. "[body], then clash with an opponent. If you win, repeat this process" = `ClashEffect(body, null, true)`: Hoarder's Greed = `ClashEffect(List.of(new LoseLifeEffect(2), new DrawCardEffect(2)), null, true)`. Wrap in `MayEffect` for "you may clash" (Sentry Oak = `MayEffect(ClashEffect(new BoostSelfAndLoseKeywordEffect(2, 0, Keyword.DEFENDER)), ...)`). Delegates `canTargetPermanent`/`canTargetPlayer` to `onWin`/`beforeClash`, so a **targeted** win reward works on any targeting slot: e.g. Springjack Knight "whenever this attacks, clash; if you win, target creature gains double strike" = `target(...)` + `ClashEffect(new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.TARGET))` on `ON_ATTACK` (target chosen when the trigger goes on the stack; grant only on a win). Do **not** wrap an interactive `MayEffect` as a `ClashEffect` win reward — the may-pause re-runs the `ClashEffect` (re-clash). For an *optional* win reward, use a bare `ClashEffect(null)` (records its result on the entry) followed by `ConditionalEffect(new WonClash(), new MayEffect(reward, prompt))`: Whirlpool Whelm = `ClashEffect(null)` + `ConditionalEffect(new WonClash(), new MayEffect(new PutTargetOnTopOfLibraryEffect(), prompt))` + `ReturnToHandEffect.target()`.

## Control / steal

- `GainControlOfTargetEffect(ControlDuration.PERMANENT[, CardSubtype])` — gain control permanently
- `GainControlOfTargetEffect(ControlDuration.END_OF_TURN)` — gain control until EOT
- `GainControlOfTargetEffect(ControlDuration.WHILE_SOURCE_ON_BATTLEFIELD)` — control while source on battlefield
- `GainControlOfEnchantedTargetEffect()` — Control Magic (static)
- `ClashForControlOfEnchantedCreatureEffect()` — Captivating Glance: `CONTROLLER_END_STEP_TRIGGERED` Aura effect; clash, then the winner (controller on win, else clash opponent) gains control of the enchanted creature
- `SacrificeEnchantedPermanentAndReattachSourceAuraEffect()` — Nettlevine Blight: `ENCHANTED_PERMANENT_CONTROLLER_END_STEP_TRIGGERED` Aura effect; the enchanted permanent's controller sacrifices it and moves this Aura (keeping its controller) onto another creature/land they control

## Mana

- `AwardManaEffect(ManaColor, DynamicAmount)`, `(ManaColor, int)`, or `(ManaColor)` — add mana; dynamic quantity: `PermanentCount(filter, CONTROLLER)` for "for each X you control", `CountersOnSource(CHARGE)` for "per charge counter", `SourcePower()` for "equal to its power", `FixedIfControlsAllNamed(List<String> names, amount, otherwise)` for the Urza-land ("Tron") boost — `amount` if you control a permanent of every named card, else `otherwise` (e.g. Urza's Mine `AwardManaEffect(COLORLESS, new FixedIfControlsAllNamed(List.of("Urza's Power-Plant", "Urza's Tower"), 2, 1))`)
- `AwardAnyColorManaEffect(int)` or `()` — add any color mana
- `AwardManaOfColorsOpponentLandsCouldProduceEffect()` — add one mana of any color an opponent's land could produce (Fellwar Stone)
- `DoubleManaPoolEffect()` — double mana pool
- `AwardRestrictedManaEffect(ManaColor, int, ManaRestriction)` — restricted mana (`ManaRestriction`: `SpellTypes(Set<CardType>)`, `ArtifactSpells()`, `SubtypeSpells(CardSubtype)`, `KickedCosts()`)
- `AwardFlashbackOnlyAnyColorManaEffect(int)` — flashback-only mana (any-color choice; separate record)
- `AwardAnyColorChosenSubtypeCreatureManaEffect()` — one mana of any color, spendable only on creature spells of the source's chosen subtype (Pillar of Origins / Unclaimed Territory; spell-only)
- `AwardAnyColorSubtypeSpellOrAbilityManaEffect(int, CardSubtype)` — N mana in any combination of colors, spendable only to cast spells of the subtype **or** activate abilities of permanents of that subtype (Smokebraider = `(2, ELEMENTAL)`)

## Copy / clone

- `CopyPermanentOnEnterEffect(PermanentPredicate, String)` + overloads — Clone-style
- `MakeTargetCopyOfTargetCreatureUntilNextTurnEffect()` — **two targets**: target Shapeshifter (`targetIds[0]`) becomes a copy of target creature (`targetIds[1]`) until the controller's next turn (Shapesharer). Wire via the multi-target `ActivatedAbility` ctor
- `BecomeCopyOfTargetCreatureUntilEndOfTurnEffect()` — source permanent becomes a copy of target creature until end of turn (Tilonalli's Skinshifter); `BecomeCopyOfTargetCreatureEffect()` — same, retaining the granting ability (Cryptoplasm)
- `CopySpellEffect()` or `(StackEntryPredicate)` — copy target spell; for "copy twice if cast from a graveyard" add `ConditionalEffect(new CastFromZone(Zone.GRAVEYARD), new CopySpellEffect())` (Increasing Vengeance). Full form `(StackEntryPredicate spellFilter, boolean tokenWithHaste, boolean sacrificeAtEndStep)`: for "copy target **creature** spell; the copy gains haste and is sacrificed at the beginning of the end step", use `new CopySpellEffect(null, true, true)` — the copy becomes a token, gains `HASTE`, and its permanent is registered in `GameData.delayedActions` (a `SacrificeAtEndStep`) (drained by `StepTriggerService.handleEndStepTriggers` via `removePermanentToGraveyard`). `tokenWithHaste` also suppresses the "choose new targets" retarget prompt. Filter which spells are targetable via the mode's `target(...)`/`ChooseOneOption` filter, not `spellFilter`. To make a spell uncopyable, set `card.setCantBeCopied(true)` — honored by every copy handler. See Choreographed Sparks.
- `CopyThisSpellIfConditionEffect(Condition)` — "When you cast this spell, copy it if <condition>. You may choose new targets for the copy." Place in the `ON_SELF_CAST` slot (the spell's own cast trigger); the copy is created with an optional choose-new-targets prompt only when the condition holds at resolution. Used by the SOS Infusion copy cycle (e.g. Lumaret's Favor with `new GainedLifeThisTurn()`)
- `CopyControllerCastSpellOnSpellCastEffect(CardPredicate, TapMultiplePermanentsCost)` — ON_CONTROLLER_CASTS_SPELL: copy cast instant/sorcery; optional tap cost wraps `MayPayTapPermanentsEffect` + `CopyControllerCastSpellEffect` (Aziza, Mage Tower Captain)
- `CopyControllerActivatedAbilityTriggerEffect(String manaCost)` — ON_CONTROLLER_ACTIVATES_NONMANA_ABILITY: "whenever you activate a non-mana ability, you may pay `manaCost` to copy it" — snapshots the ability once it's on the stack, wraps `MayPayManaEffect` + `CopyControllerActivatedAbilityEffect`; single-target copies are retargetable (Rings of Brighthearth)
- `ChangeTargetOfTargetSpellWithSingleTargetEffect()` — redirect spell
- `ChooseNewTargetsForTargetSpellEffect()` — choose new targets

## Turn / phase

- `ControllerExtraTurnEffect(int)` — extra turns (non-targeting)
- `RegisterLoseGameAtEndStepEffect()` — schedules "at the beginning of the next turn's end step, you lose the game" (Last Chance); skips the current turn's end step, fires on the extra turn's
- `ExtraTurnEffect(int)` — target extra turns
- `AdditionalCombatMainPhaseEffect(int)` — additional combat phases
- `SkipNextCombatPhaseEffect()` — ON_COMBAT_DAMAGE_TO_PLAYER: the damaged player skips their next combat phase (Blinding Angel). `(true)` = targeted spell variant where the caster picks the affected player (False Peace)
- `EndTurnEffect()` — end the turn

## Animate / transform

- `AnimatePermanentsEffect(power, toughness, subtypes, keywords, color, cardTypes, GrantScope, EffectDuration, filter)` (+ int-P/T sugar ctors) — one/many permanents become creatures. Scope SELF (manland/self, UEOT), TARGET (PERMANENT or WHILE_SOURCE_ON_BATTLEFIELD), OWN_LANDS (Sylvan Awakening), ALL_LANDS (every land on the battlefield, both players — Natural Affinity), OWN_PERMANENTS+filter (The Antiquities War). P/T `DynamicAmount` (`XValue`/`CountersOnSource`); null P/T = printed
- `AnimatePermanentsEffect.crew()` — vehicle crew (printed P/T, +CREATURE)
- `TransformSelfEffect()` — transform DFC
- `TransformSelfAndAttachToCreatureDamagedPlayerControlsEffect()` — combat-damage "you may transform; if you do, attach to target creature that player controls" (wrap in `MayEffect`)
- `TransformAllEffect(PermanentPredicate)` — transform all matching
- `PreventTransformEffect(PermanentPredicate)` — STATIC: permanents you control matching the predicate can't transform (e.g. Immerwolf)

## Static restrictions / taxes

- `EntersTappedEffect()` — enters tapped
- Conditional enters-tapped (check/fast/slow lands): `ConditionalReplacementEffect(condition, new EntersTappedEffect())` where the condition is the **negated** unless-clause (true ⇒ enters tapped), evaluated at entry against the entering permanent's controller (the permanent isn't on the battlefield yet, so counts exclude it). Check land = `ControlsPermanentCountAtMost(0, PermanentHasAnySubtypePredicate)` (tapped unless you control a matching permanent); fast land "unless N-or-fewer other lands" = `ControlsPermanentCount(N+1, new PermanentIsLandPredicate())`; slow land "unless N-or-more other lands" = `ControlsPermanentCountAtMost(N-1, new PermanentIsLandPredicate())`. **Never add a per-cycle enters-tapped record.**
- `RevealSubtypeOrEntersTappedEffect(subtype)` — STATIC: "you may reveal a [subtype] card from hand as it enters; if you don't (or can't), it enters tapped" (Lorwyn dual lands, e.g. Ancient Amphitheater = GIANT)
- `NoMaximumHandSizeEffect()` — no max hand size (static)
- `IncreaseOpponentCastCostEffect(Set<CardType>, int)` — opponents' spells cost more
- `IncreaseSpellCostExceptOnControllersTurnEffect(int)` — every spell costs N more (symmetric), except during the spell's controller's own turn (Defense Grid, {3})
- `IncreaseOpponentCostForTargetingControlledPermanentEffect(PermanentPredicate, int)` — opponent spells/abilities targeting your matching permanent cost more
- `IncreaseOwnCastCostUnlessRevealSubtypeEffect(int amount, CardSubtype)` — spell-self (STATIC): costs `amount` more unless you can reveal a matching-subtype card from hand (other than the spell). Lorwyn "reveal a creature-type card or pay {N}" cycle (Goldmeadow Stalwart: Kithkin/{3})
- `ReduceOwnCastCostEffect(DynamicAmount)` — **THE spell-self cost reduction.** `Fixed(N)` for a flat amount; a counting amount for "for each …" (Ghoultree `CardsInGraveyard(CardTypePredicate(CREATURE), CONTROLLER)`, Blasphemous Act `PermanentCount(PermanentIsCreaturePredicate, ANY_PLAYER)`). Conditional reductions wrap it: `ConditionalEffect(condition, ReduceOwnCastCostEffect(Fixed(N)))` — Metalcraft (Stoic Rebuttal), ControlsPermanent (Academy Journeymage / Wizard's Retort / Wizard's Lightning / Lookout's Dispersal), OpponentControlsMoreCreatures (Avatar of Might), CardsLeftGraveyardThisTurn (Wilt in the Heat). **Never add a per-variant record for this.** Exception: when the gating condition would read effective P/T, do **not** use `ConditionalEffect` on `STATIC` — the static-bonus path evaluates the condition and computing effective toughness recurses. Instead fold the gate into a threshold amount so it's only evaluated by the cost handler at cast time: `ReduceOwnCastCostEffect(new FixedIfControlledCreaturesTotalToughnessAtLeast(M, N))` — "costs {N} less if creatures you control have total toughness M+" (Orysa, Tide Choreographer).
- `ReduceOwnCastCostForCardTypeEffect(Set<CardType>, DynamicAmount)` — own spells of the given types cost less (battlefield permanent, Heartless Summoning)
- `ReduceOwnCastCostForSharedCardTypeWithImprintEffect(DynamicAmount)` — controller's spells sharing a card type with the imprinted card cost less (Semblance Anvil)
- `ReduceCastCostForMatchingSpellsEffect(CardPredicate, int, CostModificationScope)` — matching spells cost less (SELF = yours, OPPONENT = opponents'; e.g. CardSubtypePredicate, CardIsHistoricPredicate, CardAnyOfPredicate)
- `ReduceOwnCastCostIfTargetingControlledPermanentEffect(PermanentPredicate, int)` — this spell costs less if first target is your matching permanent (kept as its own record — target-gated)
- `ReduceOwnCastCostIfTargetingStackEntryEffect(StackEntryPredicate, int)` — this spell costs less if first target is a spell on the stack matching the predicate (kept — target-gated)
- `ReduceOwnCastCostIfTargetingPermanentEffect(PermanentPredicate, int)` — this spell costs less if first target matches predicate, any controller (kept — target-gated)
- `ReduceActivationCostPerCounterEffect(CounterType, int reductionPerCounter)` — CostEffect placed in an ActivatedAbility's effect list; reduces the generic mana of the activation cost by N per counter of the given type on the source (floored at 0). Applied in `AbilityActivationService`. Used by Diary of Dreams (page counters)
- `LimitSpellsPerTurnEffect(int)` — max spells per turn (all players)
- `LimitSpellsForEnchantedPlayerEffect(int)` — max spells per turn for the enchanted player (Curse Aura)
- `CantSearchLibrariesEffect()` — can't search (static)
- `NoncreatureSpellsCantBeCastEffect(int minManaValue, boolean restrictXSpells)` — global/symmetric: no player can cast a noncreature spell with mana value >= `minManaValue`, or (if `restrictXSpells`) with `{X}` in its cost (static, Gaddock Teeg `(4, true)`)
- `AlternativeCostForSpellsEffect(String, CardPredicate)` — alternative cast cost
- `PlayersCantCastSpellsFromZonesEffect(Set<Zone> zones)` — no player can cast from any zone in `zones` (static, global; only `GRAVEYARD`/`LIBRARY` enforced — Ashes of the Abhorrent passes `Set.of(GRAVEYARD)`, Grafdigger's Cage passes `Set.of(GRAVEYARD, LIBRARY)`)
- `CardsCantEnterBattlefieldFromZonesEffect(CardPredicate filter, Set<Zone> zones)` — cards matching `filter` (null = all) can't enter the battlefield from any zone in `zones`; blocks reanimation/undying/library-search-to-battlefield (static, global; only `GRAVEYARD`/`LIBRARY` enforced — Grafdigger's Cage passes `CardTypePredicate(CREATURE)` and `Set.of(GRAVEYARD, LIBRARY)`)

## Choose / name

- `ChooseCardNameOnEnterEffect()` — choose card name ETB
- `BoobyTrapEffect()` — STATIC marker; chosen player reveals draws + name-match sac/10-damage trigger (Booby Trap), detected in DrawService
- `ChooseColorOnEnterEffect()` — choose color ETB
- `ChooseSubtypeOnEnterEffect()` — choose creature type ETB

## Provider map

All normal (stack-resolution) effects: one `NormalEffectHandlerBean` `@Component` per effect in `service/effect/normalfx/`, auto-registered by `GameEngineConfig`. Shared logic in `*Support` classes in the same package.

| Category | Handler package | Shared helpers |
|----------|-----------------|----------------|
| Damage | `normalfx/*EffectHandler` | `DamageSupport` |
| Destruction | `normalfx/*EffectHandler` | `DestructionSupport` |
| Bounce | `normalfx/*EffectHandler` | `BounceSupport` |
| Counter | `normalfx/*EffectHandler` | `CounterSupport` |
| Library/search/mill/shuffle | `normalfx/*EffectHandler` | `LibraryRevealSupport`, `LibrarySearchSupport`, `LibraryShuffleSupport` |
| Graveyard/exile | `normalfx/*EffectHandler` | `GraveyardReturnSupport` |
| Draw/discard/choices | `normalfx/*EffectHandler` | `PlayerInteractionSupport` |
| Life | `normalfx/*EffectHandler` | `LifeSupport` |
| Boost/tap/keyword/animation | `normalfx/*EffectHandler` | `TapUntapSupport`, `AnimationSupport` |
| Permanent control/tokens/counters | `normalfx/*EffectHandler` | `PermanentControlSupport`, `PermanentCounterSupport` |
| Static effects | `staticfx/*Handler` (see **STATIC_EFFECT_HANDLERS.md**) | `StaticEffectSupport` |
| Cast-cost modifiers (reductions/taxes) | `cast/costmod/*Handler` (see **COST_MODIFICATION_HANDLERS.md**) | `CostModificationSupport` |
| Prevention | `normalfx/*EffectHandler` | `PreventionSupport` |
| Turn | `normalfx/*EffectHandler` | `TurnSupport` |
| Copy/retarget | `normalfx/*EffectHandler` | `CopySupport`, `TargetRedirectionSupport` |
| Exile / return from exile | `normalfx/*EffectHandler` | `ExileSupport` |
| Combat restriction / equip / win | `normalfx/*EffectHandler` | `CardSpecificSupport` (card-specific) |
