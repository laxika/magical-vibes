# EFFECTS_QUICK_REFERENCE

Compact lookup: effect name + constructor signature, organized by category.
For detailed descriptions, targeting info, and examples, see EFFECTS_INDEX.md.

**How to use:** Search this file for keywords from the card text (e.g. "graveyard", "life", "shuffle", "destroy"). Once you find a candidate effect, grep EFFECTS_INDEX.md for its name to get full details.

- `RegisterDelayedReturnSourceTransformedEffect()` — ON_DEATH effect that registers a delayed end-step return from the source card's owner's graveyard to the battlefield transformed. Used by Loyal Cathar-style "When this dies, return it transformed at the beginning of the next end step."
- `RegisterDelayedCreateTokenEffect(CreateTokenEffect tokenEffect)` — registers a delayed trigger that resolves `tokenEffect` at the beginning of the next end step, creating the token(s) under the resolving controller's control. Used by Rukh Egg-style "When this dies, create a ... token at the beginning of the next end step."
- `NecropotenceSetAsideTopCardEffect()` — `CardDrawingEffect` (1). Activated-ability effect (Necropotence `Pay 1 life:`): exile the top library card face down, then return it to hand at the **controller's own** next end step via a `ReturnExiledCardToHandAtEndStep` delayed action (drained in `StepTriggerService.handleEndStepTriggers`, `activePlayerId == ownerId`). Pair with `PayLifeCost(1)` in the ability's effect list.
- `ExileDiscardedCardFromGraveyardEffect()` — `ON_CONTROLLER_DISCARDS` trigger marker: "whenever you discard a card, exile that card from your graveyard" (Necropotence). Collected inline in `DiscardTriggerCollectorService` (moves the just-discarded card from graveyard to exile); no-op if it isn't in the graveyard.
- `RegisterDelayedReturnDamagedCreatureUnderControlEffect()` — `ON_DAMAGED_CREATURE_DIES` effect (Seraph). Returns the creature that died onto the battlefield under your control at the next end step (via `DelayedGraveyardToBattlefieldUnderControl`, drained in `StepTriggerService`), with a PERMANENT control effect so a stolen opponent's creature stays yours yet dies to its owner's graveyard; linked creatures are sacrificed when their controller loses control of the still-present Seraph (`StateBasedActionService`). Reads the dying card id from `StackEntry.triggeringCardId`.
- `RegisterDrawCardsAtNextUpkeepEffect(int count)` / `()` — `CardDrawingEffect`; schedules the resolving controller to draw `count` cards (default 1) at the beginning of the next turn's upkeep via a `DrawCardsAtNextUpkeep` delayed action. Used by Blessed Wine "Draw a card at the beginning of the next turn's upkeep."
- `RegisterCombatDamageReflectionEffect()` — "This turn, whenever an attacking creature deals combat damage to you, it deals that much damage to its controller" (Harsh Justice). Registers a `DelayedCombatDamageReflection` for the rest of the turn; `CombatDamageService` reflects each attacking creature's player-damage back to its controller. Pair with `setSpellCastTimingRestriction(DECLARE_ATTACKERS_IF_ATTACKED)`.

## Targeting rules (summary)

- An effect declares targeting by overriding `targetSpec()` to a non-NONE `TargetSpec` (category + `harmful` flag + optional `PermanentPredicate`); the deleted legacy `canTarget*` booleans derived from it. See `EFFECTS_INDEX.md` § "Effect targeting declarations".
- Effects in SPELL slot whose `targetSpec().category()` includes players force player targeting at cast time; those including permanents force permanent targeting (and the category/predicate type-check it).
- ETB/triggered/saga slots: targeting declarations don't force spell-level targeting.
- `CostEffect` subtypes are excluded from targeting computation.
- Targeting is computed by `EffectResolution.needsTarget(card)` / `needsSpellCastTarget(card)`.
- Never call `setNeedsTarget`/`setNeedsSpellTarget` directly.

## Capability / marker interfaces

DESCRIPTIVE interfaces in `model/effect/` that a family of effects implements so the AI
(`SpellEvaluator`, `InstantCategoryClassifier`, `AiTargetSelector`, `HardAiDecisionEngine`,
`GameSimulator`, `AiManaManager`) can read a FACT about the effect instead of `instanceof`-ing each
concrete type. **When you add a new effect that fits one of these families, implement the interface**
(return existing record components — the impl is purely additive) so the AI scores/targets it without
a code change. Interfaces are auto-exempt from `EffectDispatchRatchetTest`.

- `CostEffect` — additional costs (sacrifice, discard, exile, counter removal, tap creature). AI
  cost-valuation facets describe the resource paying gives up (default to neutral; override only the
  one that matches the record): `consumedPermanentFilter()` (`PermanentPredicate` selecting a
  payer-chosen battlefield permanent to sacrifice/return — creature/artifact/filtered),
  `consumesSourcePermanent()` (sacrifice this permanent), `sacrificesChosenCreature()` (plain
  "sacrifice a creature"), `lifePaid(currentLife)`, `sourceCountersRemoved()`,
  `consumedGraveyardCardCount()` + `consumedGraveyardCardType()`. Read by `GameSimulator`
  (payment-planning: find a sacrifice target / graveyard cards to exile) and
  `HardAiDecisionEngine.evaluateAbilityCosts` (score the cost). Overridden by `SacrificeCreatureCost`,
  `SacrificeArtifactCost`, `SacrificePermanentCost`, `SacrificeSelfCost`, `PayLifeCost`,
  `RemoveChargeCountersFromSourceCost`, `ExileNCardsFromGraveyardCost`; all other cost records inherit
  the neutral defaults (the AI never reasoned about them). Payment EXECUTION stays concrete in
  `AbilityActivationService`.
- `ManaProducingEffect` — mana abilities (CR 605.1a). AI-estimator facets (default to neutral;
  override only if the AI should model the mana): `estimatedManaColor()`, `estimatedManaAmount()`,
  `estimatedCountsAllColors()`, `estimatedWildcardMana()`, `modeledByManaEstimator()`
- `DamageDealingEffect` — deals a `DynamicAmount` to one target category; `damageAmount()`,
  `canDamageCreatures()`, `canDamagePlayers()`. Impl `DealDamageToAnyTargetEffect`,
  `DealDamageToTargetCreatureEffect`, `DealDamageToPlayersEffect`
- `GlobalDamageMultiplyingEffect` — static permanent that scales ALL damage in the game regardless of
  source/target/controller; `damageMultiplierFactor()` (2 for a doubler, stacks multiplicatively). Read
  by `GameQueryService.getDamageMultiplier` and the Myr Battlesphere damage path. Impl `DoubleDamageEffect`
  (Furnace of Rath). NOT for the selective doublers (`DoubleControllerDamageEffect`,
  `DoubleDamageToEnchantedPlayerEffect`, `DoubleEquippedCreatureCombatDamageEffect`) — each selects a
  different permanent subset and stays concrete in its own dedicated query method
- `RemovalEffect` — single-target destroy/exile/bounce; `removalKind()` returns `RemovalKind`
  (`DESTROY`/`EXILE`/`BOUNCE`) or `null` when not single-target removal (e.g. mass bounce).
  Impl `DestroyTargetPermanentEffect`, `ExileTargetPermanentEffect`,
  `ReturnTargetPermanentToHandWithManaValueConditionalEffect`, `ReturnToHandEffect` (TARGET scope)
- `BoardWipeEffect` — recognition-only fact for board sweeps; `sweepsBoard()` (true when the config is
  a mass sweep). Lets the AI's phase/pressure multipliers detect a wipe without naming the type; the
  per-type wipe SCORING stays concrete (each sweep is valued differently). Impl `MassDamageEffect`
  (always), `DestroyAllPermanentsEffect` (always), `ReturnToHandEffect` (only `BounceScope.ALL_MATCHING`)
- `CardDrawingEffect` — `drawnCardAmount()` (`DynamicAmount`). Impl `DrawCardEffect`
- `LifeGainEffect` — `lifeGainAmount()` (`DynamicAmount`). Impl `GainLifeEffect`
- `TokenCreatingEffect` — `tokenAmount()`, `tokenType()`, `tokenPower()`, `tokenToughness()`.
  Impl `CreateTokenEffect`
- `CreatureBoostEffect` — targeted P/T pump; `powerBoost()`, `toughnessBoost()` (`DynamicAmount`).
  Impl `BoostTargetCreatureEffect`
- `StaticCreatureBoostEffect` — continuous anthem/aura boost; `powerBoost()`, `toughnessBoost()`
  (int), `grantedKeywords()`, `scope()`, `filter()`. Impl `StaticBoostEffect`. The
  `StaticBoostEffect(int, int, GrantScope, CounterType scalingCounter)` ctor multiplies the flat
  boost by the count of that counter on the source ("All creatures get +1/+0 for each time counter
  on this" — Infinite Hourglass, `GrantScope.ALL_CREATURES`, `CounterType.TIME`)
- `KeywordGrantingEffect` — `keywords()`, `scope()`, `filter()` (predicate narrowing the scope, or
  `null`). Impl `GrantKeywordEffect`
- `ControlStealingEffect` — `controlDuration()`. Impl `GainControlOfTargetEffect`, `StealDyingOpponentPermanentUnlessPaysLifeEffect`
- `CounterSpellingEffect` — marker for "counter target spell". Impl `CounterSpellEffect`,
  `CounterSpellAndExileEffect`, `CounterUnlessPaysEffect`
- `CounterUnlessEffect` — "counter unless controller pays a ransom" family; `ransomKind()`
  (`PAY_MANA`/`DISCARD_CARD`) + `ransomMagnitude()` (mana amount, or 1 for a discard). Lets the
  trigger-collection and may-ability choice-flow services recognise the family and route on kind
  without `instanceof`-ing each variant (the pay/discard orchestration itself stays in the engine).
  Impl `CounterUnlessPaysEffect`, `CounterUnlessDiscardsEffect`
- `ClashOutcomeConditionalEffect` — clash-trigger effect applied only on one outcome; `wrapped()` +
  `appliesOnWin()`. Read by `TriggerCollectionService.fireClashTriggers` to resolve the won/lost
  clause without branching on the two wrapper types. Impl `IfWonClashEffect`, `IfLostClashEffect`
- `RegenerationEffect` — marker for regeneration. Impl `RegenerateEffect`
- `ProtectionGrantingEffect` — static "protection from …" facts read by the engine query layer
  (`GameQueryService`), not the AI: `protectionFromColors()` + `protectionScope()`
  (`GrantScope` null = self / `EQUIPPED_CREATURE`), `protectionFromCardTypes()`,
  `protectionFromSubtypes()`, `protectionFromManaValueAtLeast()` (`OptionalInt`). A record fills only
  its facet and inherits the empty/self defaults. Impl `ProtectionFromColorsEffect`,
  `ProtectionFromCardTypesEffect`, `ProtectionFromSubtypesEffect`, `ProtectionFromManaValueEffect`.
  NOT for runtime-only protection: chosen-color (`ProtectionFromChosenColorEffect`, a
  `ChooseColorEffect`) or "protection from non-[subtype] creatures" (tracked on the `Permanent`)
- `BlockabilityRestrictionEffect` — attacker-side evasion facts read by the engine query layer
  (`GameQueryService`): `cantBeBlocked()`, `unblockableIfDefenderControls()` (`PermanentPredicate`),
  `unblockableIfControllerCastHistoricSpellThisTurn()`, `unblockableWhileAttackingAlone()`,
  `blockableOnlyBy()` + `blockableOnlyByDescription()`, `cantBeBlockedByCreaturesMatching()`. The engine
  evaluates the predicates and supplies engine-computed facts (attacking-alone, historic-spell). Impl
  `CantBeBlockedEffect`, `CanBeBlockedOnlyByFilterEffect`, `CantBeBlockedByCreaturesMatchingPredicateEffect`,
  `CantBeBlockedIfDefenderControlsMatchingPermanentEffect`,
  `CantBeBlockedIfControllerCastHistoricSpellThisTurnEffect`, `CantBeBlockedIfAttackingAloneEffect`
- `BlockingRestrictionEffect` — blocker-side restriction facts (`GameQueryService`): `cantBlock()`,
  `canBlockOnlyAttackersMatching()` + `canBlockOnlyAttackersDescription()`, and the board-wide
  `globalCantBlockBlockerMatcher()` / `globalCantBlockAttackerMatcher()` / `globalCantBlockDescription()`.
  Impl `CantBlockEffect`, `CanBlockOnlyIfAttackerMatchesPredicateEffect`,
  `MatchingCreaturesCantBlockMatchingCreaturesEffect`
- `BlockCostEffect` — per-block mana tax fact (`CombatBlockService.declareBlockers`):
  `blockCost(int attackerPower)` = generic mana to declare the block (0 = free). Impl
  `CantBlockHighPowerCreaturesUnlessPaysEffect`
- `AttackOrBlockRestrictionEffect` — combined "can't attack or block" facts (attack side in
  `CombatAttackService`, block side in `GameQueryService`): `globallyCantAttackOrBlock()`
  (`PermanentPredicate`), `cantAttackOrBlockUnless()` (`Condition`), `restrictionDescription()`. Impl
  `MatchingCreaturesCantAttackOrBlockEffect`, `CantAttackOrBlockUnlessEffect`
- `NoDefenderAttackPermissionEffect` — "attack as though no defender" facts
  (`GameQueryService.canAttackDespiteDefender`): `grantsCarrierAttackAsThoughNoDefender()` (self grant,
  incl. `ConditionalEffect`-wrapped and floating grants) and `noDefenderAttackMatcher()`
  (`PermanentPredicate`, board-wide). Impl `CanAttackAsThoughNoDefenderEffect`,
  `MatchingCreaturesCanAttackAsThoughNoDefenderEffect`
- `CombatDamageTriggerContextEffect` — how `CombatDamageService` should populate a fired
  `ON_COMBAT_DAMAGE_TO_PLAYER` trigger's stack entry: `combatDamageTriggerContext()` returns a
  `TriggerContext` — `DAMAGED_PLAYER_WITH_DAMAGE_AMOUNT` (xValue = damage dealt, targetId = damaged
  player, no source; Balefire Dragon), `SOURCE_SELF` (source = the dealing creature, no target;
  explore / self-counter), `DAMAGED_PLAYER` (targetId = damaged player, source = the dealing creature;
  "that player discards/mills/…") — or `null` for the plain no-context entry (also the default for
  effects that don't implement it). Recipient-dependent effects return `DAMAGED_PLAYER` only when their
  recipient is the target player and `null` otherwise. Impl `ReturnPermanentsOnCombatDamageToPlayerEffect`,
  `DealDamageToEachCreatureDamagedPlayerControlsEffect`,
  `LookAtTopXCardsPermanentsToBattlefieldRestToGraveyardEffect`, `PutCountersOnSourceEffect`,
  `RemoveAllCountersFromSelfEffect`, `ExploreEffect`, `ExileTopCardsRepeatOnDuplicateEffect`,
  `TargetPlayerRandomDiscardOrControllerDrawsEffect`, `RevealRandomCardFromTargetPlayerHandEffect`,
  `LookAtRandomCardInTargetPlayerHandEffect`,
  `SphinxAmbassadorEffect`, `TargetPlayerExilesFromHandEffect`, `ChooseCardsFromTargetHandEffect`,
  `SkipNextCombatPhaseEffect`, `TargetPlayerCantGainLifeRestOfGameEffect`, `DiscardEffect` /
  `MillEffect` / `DealDamageToPlayersEffect` (only when recipient is the target player). NOT for the
  bespoke `MayEffect`-wrapped or destroy/sacrifice "damaged player controls" flows, which stay in
  `CombatDamageService`
- `GraveyardAbilityGrantingEffect` — static "each creature card in your graveyard has '[graveyard
  activated ability]'" facts read by `GameQueryService.computeGrantedGraveyardAbilitiesForOwnedCreatureCard`
  (and appended to the effective graveyard abilities by `AbilityActivationService` + the graveyard
  `CardView`): `grantedGraveyardAbility()`. Impl `GrantGraveyardAbilityToCreatureCardsEffect` (Sedris,
  the Traitor King — unearth {2}{B})
- `CombatOpponentReferencingEffect` — marker for an effect that, when fired from an **Aura/Equipment**
  combat trigger (`ON_BLOCK` / `ON_BECOMES_BLOCKED PER_BLOCKER`), acts on the enchanted/equipped
  creature's combat opponent. `CombatTriggerService` bakes that opponent in as the trigger's
  non-targeting `targetId` (so it can't fizzle) instead of prompting the controller for a target. Impl
  `DestroyCombatOpponentAtEndOfCombatEffect` (Venom — Basilisk-style Aura, non-Wall filter)

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
- `ConditionalEffect(new NotCondition(inner), wrapped)` — logical negation of any condition ("unless …"), e.g. Hotheaded Giant enters with -1/-1 counters `new NotCondition(new ControllerCastAnotherSpellThisTurn(new CardColorPredicate(RED)))`
- `TriggeringCardConditionalEffect(CardPredicate, CardEffect)` — triggering card matches predicate
- `TriggeringPermanentConditionalEffect(PermanentPredicate, CardEffect)` — triggering permanent matches predicate
- `ConditionalEffect(new ControlsAnotherPermanent(filter), wrapped)` — controls another matching permanent
- `ConditionalEffect(new ControllerLifeAtLeast(threshold), wrapped)` — life >= N
- `ConditionalEffect(new ControllerHasMoreLifeThanAnOpponent(), wrapped)` — you have strictly more life than at least one opponent (Feudkiller's Verdict)
- `ConditionalEffect(new TargetPermanentMatches(PermanentPredicate), wrapped)` — the targeted permanent matches the predicate at resolution (e.g. "destroy target permanent if it's red": wrap `DestroyTargetPermanentEffect` with `PermanentColorInPredicate(Set.of(RED))`). Targeting stays unrestricted (any permanent); the color check is a resolution-time intervening-if. Hydroblast mode 1
- `ConditionalEffect(new TargetSpellMatches(StackEntryPredicate), wrapped)` — the targeted spell on the stack matches the predicate at resolution (e.g. "counter target spell if it's red": wrap `CounterSpellEffect` with `StackEntryColorInPredicate(Set.of(RED))`). Targeting stays unrestricted (any spell); the color check is a resolution-time intervening-if. Hydroblast mode 0

Metalcraft / Morbid / Raid / ControlsAnotherPermanent are ETB trigger gates
(`Condition.isEtbTriggerGate()`): a targeted ETB wrapped in one never asks for its target at cast
time — the target is chosen as the trigger goes on the stack (see TRIGGER_SLOT_TARGETING.md,
"ON_ENTER_BATTLEFIELD targeted triggers"). Override `isEtbTriggerGate()` when adding a new
intervening-if condition used to gate a targeted ETB.
- `ConditionalEffect(new ControllerTurn(), CardEffect)` — during your turn
- `ConditionalEffect(new NotControllerTurn(), CardEffect)` — during turns other than yours
- `ConditionalEffect(new ControlsPermanent(filter), wrapped)` — controls matching
- `EnchantedPermanentConditionalEffect(PermanentPredicate, CardEffect ifMatch, CardEffect ifNotMatch)` — aura active branch based on enchanted permanent predicate
- `ConditionalEffect(new OpponentControlsPermanent(filter), wrapped)` — opponent controls matching
- `ConditionalEffect(new OpponentControlsMoreLands(), wrapped)` — an opponent controls strictly more lands than you (Gift of Estates)
- `ConditionalEffect(new HasAttacker(predicate), wrapped)` — one or more matching attackers
- `CantAttackUnlessEffect(Condition, "unless clause")` — STATIC attack restriction; condition = `ControlsPermanentCount(1, filter)` / `DefendingPlayerControlsPermanent(filter)` / `AnyPlayerControlsPermanentCount(N, filter)` / `DefendingPlayerPoisoned()` / `OpponentDealtDamageThisTurn(minAmount)`
- `CantAttackOrBlockUnlessGreaterPowerAlsoDoesEffect()` — STATIC combat-set restriction (Okk): can't attack unless another declared attacker has strictly greater power; can't block unless another declared blocker has strictly greater power. Validated against the current combat's declared set in `CombatAttackService`/`CombatBlockService`, not via `Condition`
- `CantAttackOrBlockUnlessCountAlsoDoesEffect(int otherCount)` — STATIC combat-set restriction (Orcish Conscripts, `otherCount = 2`): can't attack unless at least N other creatures are declared as attackers; can't block unless at least N other creatures are declared as blockers. Validated against the current combat's declared set in `CombatAttackService`/`CombatBlockService`, not via `Condition`
- `CreaturesCantAttackUnlessPredicateEffect(PermanentPredicate exemption)` — STATIC global: no creature can attack unless it matches exemption (Stormtide Leviathan)
- `CreaturesCantAttackControllerUnlessPredicateEffect(PermanentPredicate exemption)` — STATIC defender-scoped: creatures not matching exemption can't attack THIS controller only ("creatures without flying can't attack you", Form of the Dragon → exemption `PermanentHasKeywordPredicate(Keyword.FLYING)`)
- `RequirePaymentToAttackEffect(int amountPerAttacker)` — STATIC uniform attack tax read from the DEFENDER's battlefield; controller pays N per attacker (Windborn Muse / Ghostly Prison)
- `EnchantedCreatureCantAttackUnlessPaysEffect(int amount)` — Aura STATIC: the enchanted creature can't attack unless its controller pays {amount} generic (Brainwash, {3}). Per-creature — summed per declared attacker in `CombatAttackService`; contrast the uniform `RequirePaymentToAttackEffect`
- `CantAttackUnlessSacrificeEffect(count, PermanentPredicate, description)` — STATIC additional attack cost: this creature can't attack unless its controller sacrifices `count` matching permanents (paid as attackers declared). Marker read by `AttackSacrificeCostService` (no dispatch handler). **Pair with** a `CantAttackUnlessEffect(new ControlsPermanentCount(count, filter), …)` legality gate so the declaration is only allowed when the cost is payable (Leviathan — `(2, Island)`, "can't attack unless you sacrifice two Islands")
- `CantBlockHighPowerCreaturesUnlessPaysEffect(int minAttackerPower, int amount)` — STATIC block-side tax (Hipparion, `(3, 1)`): this creature can't block a creature with power ≥ `minAttackerPower` unless its controller pays {amount} generic. Charged per qualifying block in `CombatBlockService.declareBlockers` via the `BlockCostEffect` capability interface; block stays legal, only the cost gates it
- `CantBlockCreaturesWithPowerAtLeastEffect(int minAttackerPower)` — STATIC hard restriction (Ironclaw Orcs, `(2)`): this creature can't block an attacker with effective power ≥ `minAttackerPower`. No cost, not self-referential; compared in `GameQueryService.findBlockDenial`. Facet `cantBlockCreaturesWithPowerAtLeast()` on `BlockingRestrictionEffect`
- `ControlledCreaturesCantAttackUnlessPredicateEffect(PermanentPredicate exemption)` — STATIC controller-scoped: creatures the source's controller controls that don't match exemption can't attack (own team only; source exempt if it matches). "Non-Eye creatures you control can't attack", Evil Eye of Orms-by-Gore → exemption `PermanentHasSubtypePredicate(EYE)`
- `CreaturesWithPowerGreaterThanAmountCantAttackEffect(DynamicAmount amount)` — STATIC global: any creature (either player's) whose effective power is strictly greater than `amount` can't attack. `amount` is evaluated from the source's controller (`AmountContext.forStaticEffect`). Ensnaring Bridge → `new CardsInHand(CountScope.CONTROLLER)` ("power greater than the number of cards in your hand can't attack")
- `ConditionalEffect(new GraveyardCardThreshold(threshold, filter), wrapped)` — graveyard threshold
- `ConditionalEffect(new CardsInLibraryAtLeast(threshold), wrapped)` — controller has N+ cards in library (Battle of Wits: upkeep + WinGameEffect)
- `ConditionalEffect(new AnyLibraryAtMost(threshold), wrapped)` — some player's library has N or fewer cards (Shelldock Isle: `{U}, {T}` + PlayImprintedCardWithoutPayingManaCostEffect, threshold 20)
- `ConditionalEffect(new CardsInHandAtLeast(threshold), wrapped)` — controller has N+ cards in hand (Imaginary Pet: upkeep + ReturnToHandEffect.self())
- `ConditionalEffect(new SourceCounterThreshold(threshold, counterType), wrapped)` — source counter threshold (e.g. 5+ growth counters)
- `ConditionalEffect(new SourceIsTapped(), wrapped)` — source permanent is tapped (Mana Vault draw-step self-damage)
- `ConditionalEffect(new ControlledCreaturesTotalPowerAtLeast(threshold), wrapped)` — total power of creatures you control >= N (Mosswort Bridge)
- `EnteringCreatureMinPowerConditionalEffect(int, CardEffect)` — entering power >= N
- `EnteringCreatureMaxPowerConditionalEffect(int, CardEffect)` — entering power <= N
- `PutCountersOnEnteringCreatureEffect(int count)` — ON_ALLY_CREATURE_ENTERS_BATTLEFIELD: "you may put `count` +1/+1 counters on it" (the entering creature). Enter collector bakes the entering permanent id and queues `MayEffect(PutCounterOnTargetPermanentEffect(PLUS_ONE_PLUS_ONE, count))`. Wrap in `EnteringCreatureMinPowerConditionalEffect` for the "with power N or greater" gate (Mighty Emergence: `(5, new PutCountersOnEnteringCreatureEffect(2))`)

Replacement wrappers (pick between base/upgraded at resolution):
- `ConditionalReplacementEffect(new Metalcraft(), baseEffect, upgradedEffect)(CardEffect base, CardEffect metalcraft)`
- `ConditionalReplacementEffect(new Morbid(), baseEffect, upgradedEffect)(CardEffect base, CardEffect morbid)`
- `ConditionalReplacementEffect(new Raid(), baseEffect, upgradedEffect)(CardEffect base, CardEffect raid)`
- `ConditionalReplacementEffect(new Kicked(), baseEffect, upgradedEffect)(CardEffect base, CardEffect kicked)`
- `ConditionalReplacementEffect(new ControlsPermanent(filter), baseEffect, upgradedEffect)(PermanentPredicate, CardEffect base, CardEffect upgraded)`
- `ConditionalReplacementEffect(new TargetPermanentMatches(filter), baseEffect, upgradedEffect)(PermanentPredicate, CardEffect base, CardEffect upgraded)` — target permanent predicate

Other wrappers:
- `ChooseOneEffect(List<ChooseOneOption>)` — modal spell
- `FlipCoinWinEffect(CardEffect win)` or `(CardEffect win, CardEffect lost)` — flip a coin; run `win` if won, `lost` if lost (`lost` optional, defaults to nothing). Bottle of Suleiman: `(CreateTokenEffect(5/5 Djinn), DealDamageToPlayersEffect(5, CONTROLLER))`
- `ManaClashEffect()` — repeat: you + target opponent flip coins, 1 dmg per tails, until both heads
- `GameOfChaosEffect()` — target opponent; flip a coin, on a win you gain / opp loses the stake life (opp gains / you lose on a loss), the flip's winner may flip again, stakes double each flip (Game of Chaos). Self-driven loop via `GameOfChaosFlipAgainEffect` + the may-ability yes/no prompt
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
- `DealDamageToSourceEffect(DynamicAmount)`; `(int)` — the source permanent deals damage to itself (no target). Pair with `DealDamageToAnyTargetEffect` for "deals X to any target and X to itself" (Sunflare Shaman)
- `DealDamageToAttackedTargetEffect(int damage)` — damage to the player or planeswalker attacked by the creature that caused the attack trigger
- `DealDamageToTriggeringAttackerEffect(int damage, PermanentPredicate attackerCondition)` — `ON_CREATURE_ATTACKS_YOU`: damage to the attacking creature; `attackerCondition` restricts which attackers trigger it (Raking Canopy: flyers, 4)
- `SourceFightsTargetCreatureEffect()` — source fights target
- `PackHuntEffect(CardSubtype)` — pack hunt
- `DealDamageToTargetAndTheirCreaturesEffect(int)` — player + their creatures
- `DealDamageToEachCreatureDamagedPlayerControlsEffect()` — damage to damaged player's creatures
- `DestroyPermanentDamagedPlayerControlsEffect(PermanentPredicate, int minimumDamage)` — ON_DAMAGE_TO_PLAYER, mandatory: destroy target matching permanent (e.g. `PermanentIsLandPredicate`) the damaged player controls, only when `minimumDamage`+ dealt (Deus of Calamity). Destroy analog of `ExilePermanentDamagedPlayerControlsEffect`
- `SacrificePermanentDamagedPlayerControlsEffect(PermanentPredicate, int minimumDamage)` — ON_COMBAT_DAMAGE_TO_PLAYER, mandatory: controller chooses a target matching permanent (e.g. `PermanentIsCreaturePredicate`) the damaged player controls, that player **sacrifices** it (only when `minimumDamage`+ dealt). Ashling, the Extinguisher (minimumDamage=0). Sacrifice analog of `DestroyPermanentDamagedPlayerControlsEffect`
- `DealDamageToTargetCreatureEffect(DynamicAmount, boolean unpreventable)`; `(int)`, `(int, boolean)`, `(DynamicAmount)` — target creature. Amounts: `Fixed`, `XValue`, `SourceToughness`, `PermanentCount` (subtype counts), `ManaSpentToCast`
- `DealDamageToTargetCreatureEqualToChosenTypeCountEffect()` — "Choose a creature type. Deals damage to target creature equal to the number of permanents you control of the chosen type" (Coordinated Barrage). Resolution-time creature-type choice (`beginSpellCreatureTypeChoice`, stored on `GameData.chosenSpellSubtype`), Changeling-aware count; pair with an attacking/blocking target filter
- `DealDamageToAnyTargetEqualToChosenTypeCountEffect()` — "Choose a creature type. Deals damage to any target equal to the number of permanents you control of the chosen type" (Roar of the Crowd). Any-target sibling of the above; self-declares any-target (creature/planeswalker/player)
- `DealDamageToTargetCreatureOrPlaneswalkerEffect(int)` — creature or planeswalker
- `DealDamageToTargetOpponentOrPlaneswalkerEffect(int)` — opponent or planeswalker
- `DealDamageToTargetPlayerOrPlaneswalkerEffect(DynamicAmount)` / `(int)` — any player (incl. controller) or planeswalker (Boggart Shenanigans; Brion Stoutarm's sacrificed-power `XValue`)
- `DealDamageToTargetOpponentAndUpToCreaturesThatPlayerControlsEffect(int opponentDamage, int creatureDamage, int maxCreatureTargets)` — target opponent plus up to N creatures that player controls
- `DealDamageToAllCreaturesAndPlaneswalkersTargetControlsEffect(int)` — all target controls
- `DealDamageToAllCreaturesTargetControlsEffect(int)` — creatures target controls
- `DealDamageDividedEvenlyAmongCreaturesTargetControlsEffect(DynamicAmount)` — `floor(amount/creatureCount)` to each creature target player controls (Dwarven Catapult); pair with card-level opponent-only `PlayerPredicateTargetFilter`
- `DealDamageToEachMatchingPermanentEffect(int, PermanentPredicate, EachPermanentScope)` — damage each matching permanent across `ALL_PLAYERS`/`TARGET_PLAYER`
- `DealDamageToEachPlayerControllingMatchingPermanentEffect(int, PermanentPredicate)` — damage each player controlling a matching permanent (Disorder)
- "If this is the Nth time this ability has resolved this turn, [X]" — `ConditionalEffect(new NthAbilityResolutionThisTurn(n), X)` on an activated ability; the engine counts resolutions per source permanent (`GameData.permanentAbilityResolutionsThisTurn`), condition is met only on the exact n-th resolution. Ashling the Pilgrim = `PutCountersOnSelfEffect(PLUS_ONE_PLUS_ONE)` + `ConditionalEffect(NthAbilityResolutionThisTurn(3), RemoveAllCountersFromSelfEffect(PLUS_ONE_PLUS_ONE))` + `ConditionalEffect(NthAbilityResolutionThisTurn(3), MassDamageEffect(new EventValue(), true))`
- `InnerFlameIgniterEffect()` — **card-specific.** On the exact third resolution this turn, creatures you control gain first strike until end of turn. Pair with `BoostAllOwnCreaturesEffect(1, 0)` in the same ability for the unconditional +1/+0 (Inner-Flame Igniter)
- `DealDamageToPlayersEffect(DynamicAmount, DamageRecipient)`; `(int, recipient)`; `.enchantedAttachedCount(PermanentPredicate)` — **unified player damage.** Recipients: `TARGET_PLAYER` (only targeting one; `Fixed`/`CardsInGraveyard` Scrapyard Salvo/`CardsInHand(TARGET_PLAYER)` Sudden Impact + Sword of War and Peace), `EACH_OPPONENT` (single eval, same value; `Fixed`/`CountersOnSource` Hallar), `EACH_PLAYER` (Slagstorm), `CONTROLLER` (self/pain lands), `ENCHANTED_PLAYER` (curse upkeep; `.enchantedAttachedCount` Curse of Thirst), `TARGET_PERMANENT_CONTROLLER` (Chandra's Outrage), `TRIGGERING_PERMANENT_CONTROLLER` (Magnetic Mine)
- `DealDamageIfDidntCastSpellThisTurnEffect(int damage)` — `END_STEP_TRIGGERED`: deals N damage to the end-step player (`entry.getTargetId()`, baked to the active player by StepTriggerService) if that player didn't cast a spell this turn. Intervening-if checked at trigger and resolution (Impatience)
- `DealDamageToAnyTargetEffect.forTargetGroup(int damage, int targetGroup)` — damage aimed at a target group's chosen target (Goblin Barrage kicked target)
- `MassDamageEffect(DynamicAmount, damagesPlayers, damagesPlaneswalkers, PermanentPredicate)` — mass damage; convenience ctors `(int)`, `(int, damagesPlayers)`, `(DynamicAmount, damagesPlayers)`, `(int, usesXValue, damagesPlayers, filter)` (+ planeswalker overload)
- "deal N damage to any target and you gain M life" (fixed life, e.g. Essence Drain, Dark Nourishment, Ajani Vengeant −2) — **compose** `DealDamageToAnyTargetEffect(N)` + `GainLifeEffect(M)` as two effects on the same slot. Targeting auto-derives from the damage effect; the fixed life is gained on resolution independent of damage dealt, and the single-target spell fizzles (no life) if the target becomes illegal. (For "gain life equal to the damage dealt", use `GainLifeEffect(new XValue())` like Drain Life, or `DealDamageToAnyTargetEqualToControlledSubtypeCountAndGainLifeEffect` for Corrupt's dynamic case.)
- `DealDamageToAnyTargetEqualToControlledSubtypeCountAndGainLifeEffect(CardSubtype, boolean)` — any target = subtype count
- `DealDividedDamageEffect` (unified divided/multi-target damage) — factories: `.chosenAmongAnyTargets(int)` (Fight with Fire kicked), `.chosenAmongAnyTargets(DynamicAmount)` (Jaws of Stone — dynamic total, e.g. Mountains you control, divided among any targets), `.chosenAmongTargetCreatures(int)` (Ignite Disorder), `.chosenAmongAnyTargetsEtb(int,int)` (Inferno Titan/Bogardan ETB), `.xAmongAttackingCreatures()` (Hail of Arrows), `.xAmongTargetCreaturesCantBlock()` (Huatli −X), `.xDividedEvenly()` (Fireball), `.ordered(List<Integer>)` (Cone of Flame/Arc Trail)
- `DealXDamageToAnyTargetAndGainXLifeEffect()` — X damage + X life
- `DealDamageToEachTargetEffect(DynamicAmount[, PermanentPredicate filter])` — full amount to each of multiple targets (Jaya's Immolating Inferno with `XValue`). Optional `filter` restricts damage to targeted permanents matching it (targeted players/non-matching skipped) — Winter Blast: `targetX(creatureFilter, cap)` bound to `TapPermanentsEffect(TARGET)` + `DealDamageToEachTargetEffect(new Fixed(2), new PermanentHasKeywordPredicate(FLYING))` ("tap X target creatures; 2 damage to each of those with flying")
- `TargetDealsPowerDamageToTargetEffect()` — bite (group indices `(sourceTargetGroup, victimTargetGroup)` default to 0, 1)
- `TargetCreatureDealsPowerDamageToSelfEffect()` — target deals its power to itself
- `TargetCreatureDealsPowerDamageToControllerEffect()` — target creature deals its power to that creature's controller; the creature is the damage source (Dong Zhou, the Tyrant, ETB)
- `TargetCreatureDealsPowerDamageToAnyTargetEffect()` — group 0 creature deals its power to any target (group 1); the creature is the damage source (Soul's Fire). Groups `(sourceTargetGroup, victimTargetGroup)` default to 0, 1
- `DiscardRandomCardDealDiscardedPowerToTargetPlayerOrPlaneswalkerEffect()` — discard a card at random; if it's a creature card, source deals damage equal to that card's power to target player/planeswalker (Cragganwick Cremator ETB). Target always chosen; non-creature discard = no damage
- `FightTargetsEffect()` — fight (group indices `(firstTargetGroup, secondTargetGroup)` default to 0, 1)
- `MassFightTargetCreatureEffect()` — Alpha Brawl-style mass fight
- `PreventNextDamageToSelfEffect(int amount)` — activated ability (no target): "Prevent the next N damage that would be dealt to this creature this turn" (Ethereal Champion). Shields the ability's own source permanent via its `damagePreventionShield`; next N damage from any source (combat or noncombat), then consumed. Hooked in `PreventNextDamageToSelfEffectHandler`
- `PreventNextDamageToControllerEffect(int amount)` — activated ability (no target): "Prevent the next N damage that would be dealt to you this turn" (Esper Battlemage). Shields the ability's controller via `GameData.playerDamagePreventionShields`; next N damage from any source (combat or noncombat), then consumed, cleared at end of turn. Hooked in `PreventNextDamageToControllerEffectHandler`
- `PreventNextDamageToTargetAndAddToughnessCountersEffect(int amount)` — SPELL (benign CREATURE target): "Prevent the next N damage to target creature this turn; at the next end step put a +0/+1 counter on it for each 1 damage prevented this way" (Sacred Boon). Sets the target's `damageToCounterPreventionShield`; prevented damage is accumulated into `DelayedPlusZeroPlusOneCounters` (drained by `StepTriggerService` as a `PutCountersOnSourceEffect(0, 1, total)` trigger). +0/+1 counters use `CounterType.PLUS_ZERO_PLUS_ONE` (toughness only). Hooked in `DamagePreventionService.applyCreaturePreventionShield`
- `PreventDividedDamageEffect(int amount)` — SPELL (no `target()` call): "Prevent the next N damage that would be dealt this turn to any number of targets, divided as you choose" (Remedy). Per-target shield amounts come from the cast-time `damageAssignments` map (harness `castInstant(player, idx, Map)`), summing to N; each target (creature and/or player) gets a "next X damage" shield via `PreventDividedDamageEffectHandler`. Reuses the damage-distribution cast flow (`EffectResolution.needsDamageDistribution`). Shields expire at end of turn
- `PreventNoncombatDamageToControllerAndGainLifeEffect()` — STATIC: prevent all noncombat damage to controller; they gain life equal to the damage prevented (Purity). Hooked in `DamageSupport.dealDamageToPlayer`
- `PreventDamageToControllerPerClericEffect()` — STATIC: "If a source would deal damage to a player, you may prevent X of that damage, where X = Clerics you control" (Battletide Alchemist). Modeled controller-only; prevents up to (Clerics controlled × number of these permanents) per source, combat and noncombat. Via `DamagePreventionService.applyControllerPerClericDamagePrevention`, hooked in `DamageSupport.dealDamageToPlayer` (noncombat) and `CombatDamageService.accumulatePlayerDamage` (combat, per attacker)
- `EyeForAnEyeEffect()` — SPELL: "The next time a source of your choice would deal damage to you this turn, instead that source deals that much damage to you and Eye for an Eye deals that much damage to that source's controller" (Eye for an Eye). Reflection, not prevention — you still take the damage; an equal reflected event is dealt at the chosen source's controller. Source chosen on resolution (any battlefield permanent). Shield in `GameData.eyeForAnEyeShields`, applied via `DamagePreventionService.applyEyeForAnEyeReflection` (hooked in `DamageSupport.dealDamageToPlayer` + `CombatDamageService.accumulatePlayerDamage`), reflected damage queued in `pendingEyeForAnEyeReflections`
- `PreventFixedDamagePerSourceToControllerEffect(int amount)` — STATIC: "If a source would deal damage to you, prevent N of that damage" (Urza's Armor). Prevents a fixed `amount` per source, combat and noncombat, to the controller; multiple copies stack. Via `DamagePreventionService.applyControllerFixedPerSourceDamagePrevention`, hooked in `DamageSupport.dealDamageToPlayer` (noncombat) and `CombatDamageService.accumulatePlayerDamage` (combat, per attacker)
- `PreventCombatDamageToAttackingCreaturesYouControlEffect()` — STATIC: prevent all combat damage dealt to attacking creatures the source's controller controls (Dolmen Gate). Checked in `DamagePreventionService.applyCreaturePreventionShield` via `permanent.isAttacking()` + `isCombatDamage`
- `PreventSpellDamageToOpponentAndCreateTokensEffect(CreateTokenEffect token)` — STATIC: if a spell you control would deal damage to an opponent, prevent it and create one `token` per 1 damage prevented (Hostility). Hooked in `DamageSupport.dealDamageToPlayer`
- `PreventAllDamageToControllerAndExileFromGraveyardEffect()` — STATIC: prevent ALL damage (combat + noncombat) to the controller and exile a card from their graveyard for each 1 damage prevented (Immortal Coil). Via `DamageSupport.applyImmortalCoilPrevention`, hooked in both `DamageSupport.dealDamageToPlayer` (noncombat) and `CombatDamageService.applyPlayerDamage` (combat). Pair with the empty-graveyard `StateTriggerEffect` + `ControllerLosesGameEffect`
- `PreventAllDamageToTargetCreatureEffect()` — prevent all damage to target creature this turn (Wellgabber Apothecary). Adds target to `GameData.creaturesWithAllDamagePrevented`, checked in `DamagePreventionService.applyCreaturePreventionShield`, cleared at turn cleanup
- `PreventColorDamageToEnchantedCreatureEffect()` — STATIC marker on an Aura: prevent all damage to enchanted creature from sources of the Aura's chosen colour (Prismatic Ward). Pair with `ON_ENTER_BATTLEFIELD ChooseColorOnEnterEffect()`; damage-only, NOT protection. Queried in `GameQueryService.isColorDamageToEnchantedCreaturePrevented` from `DamageSupport.dealCreatureDamage` + `CombatDamageService.applyCombatCreatureDamage`
- `PreventAllDamageByTargetCreatureEffect()` / `(boolean combatOnly)` — prevent all damage target creature(s) would deal this turn (Soul Parry, Inquisitor's Snare). `combatOnly=true` prevents only combat damage via `GameData.creaturesPreventedFromDealingCombatDamage`, gating attacker participation in `CombatDamageService` (Resistance Fighter). Cleared at turn cleanup
- `PreventAllDamageToControllerFromAttackingCreaturesEffect()` — SPELL: prevent all damage attacking creatures would deal to the controller this turn (Deep Wood). Adds controller to `GameData.playersWithDamageFromAttackersPrevented`; combat damage prevented in `CombatDamageService.applyPlayerDamage`, noncombat only when the source permanent is attacking. Cleared at turn cleanup. Pair with `setSpellCastTimingRestriction(DECLARE_ATTACKERS_IF_ATTACKED)`
- `PreventDamageToOtherCreaturesAndAddPlusCountersEffect()` — STATIC: prevent all damage (combat or noncombat, any source) to *another* creature you control and put a +1/+1 counter on it per 1 damage prevented (Vigor). Checked in `DamagePreventionService.applyCreaturePreventionShield`; the effect is on a different permanent than the one being damaged
- `PreventDamageToSelfAndSourceControllerDrawsEffect()` — STATIC: "If a source would deal damage to this creature, prevent that damage. The source's controller draws cards equal to the damage prevented this way" (Swans of Bryn Argoll). Prevents all damage (combat + noncombat, any source) to the permanent carrying it; the source's controller draws one card per point prevented. Via `DamagePreventionService.applySwansSourceControllerDraw`, hooked in `DamageSupport.dealCreatureDamage` (noncombat, covers burn/mass/bite) and `CombatDamageService.applyCombatCreatureDamage` (combat, per source)
- `PreventNextDamageFromChosenSourceMatchingEffect(PermanentPredicate sourceFilter, String sourceLabel)` — one-shot: prevent the *next* damage event a chosen source matching `sourceFilter` would deal to you this turn (Circle of Protection cycle — colored variants pass `PermanentColorInPredicate`, Circle of Protection: Artifacts passes `PermanentIsArtifactPredicate`). Source chosen on resolution; shield in `GameData.playerSourceNextDamageShields`, consumed by `DamagePreventionService.applyPlayerNextSourceDamageShield`
- `PreventNextDamageFromChosenSourceEffect(boolean gainLife)` — one-shot: prevent the *next* damage event a chosen source (any color) would deal to you this turn (Reverse Damage with `gainLife=true`, also gains that much life; Pentagram of the Ages with `gainLife=false`, no life gain, as a `{4},{T}` ability). Source chosen on resolution; shield in `GameData.playerSourceNextDamageShields` with the given `gainLife`, consumed by `DamagePreventionService.applyPlayerNextSourceDamageShield` (which grants any life via `LifeSupport`)
- `PreventNextDamageFromChosenSourceToAnyTargetEffect()` — one-shot: prevent the *next* damage event a chosen source (any color) would deal to **any** target this turn — player, planeswalker, or creature, combat or noncombat (Sanctum Guardian; usually an activated ability with `SacrificeSelfCost`). Source chosen on resolution; shield (source ID) in `GameData.sourceNextDamageToAnyTargetShields`, consumed by `DamagePreventionService.applyChosenSourceNextDamageToAnyTargetShield` (hooked in `DamageSupport` and `CombatDamageService` player/creature/planeswalker paths)
- `RedirectTargetCreatureDamageFromChosenSourceToSelfEffect()` — activated ability targeting a creature: all damage a chosen source (picked on resolution) would deal to that creature this turn is dealt to the source permanent instead (Oracle's Attendants). Shield in `GameData.creatureDamageRedirectShields`, checked in both combat and noncombat creature-damage paths via `DamagePreventionService.applyCreatureRedirectShields`; reuses `pendingSourceRedirectDamage`
- `RedirectTargetCreatureNextDamageFromChosenSourceToControllerEffect()` — activated ability targeting a creature: the next single damage event a chosen source (picked on resolution) would deal to that creature this turn is dealt to the ability's controller (you) instead, then consumed (Jade Monolith). Same `creatureDamageRedirectShields` machinery as Oracle's Attendants, but redirects to a player and only for the next event (`CreatureDamageRedirectShield.NEXT_EVENT`)
- `RedirectNextDamageToSelfToOwnerEffect(int amount)` — activated ability (no target): the next `amount` damage that would be dealt to this ability's source creature this turn (any source) is dealt to its owner (the source's controller, a player) instead, then consumed (Personal Incarnation). Same amount-limited `creatureDamageRedirectShields` machinery as Zealous Inquisitor's `RedirectNextDamageToTargetCreatureEffect`, but no target and `redirectTargetId` = the owner player (the player branch of `processSourceRedirectDamage` deals it to life)
- `DoubleDamageEffect()` — double all damage (static)
- `DoubleDamageToEnchantedPlayerEffect()` — double damage dealt to enchanted player (static Curse)
- `DoubleControllerDamageEffect(StackEntryPredicate, boolean)` — double controller's damage
- `SacrificePermanentThenEffect(PermanentPredicate, CardEffect, String)` — sacrifice then effect
- `SpellCastTriggerEffect(CardPredicate, List<CardEffect>)` + overloads — spell cast trigger
- `BoostSelfByCastSpellManaValueEffect(CardPredicate spellFilter)` — ON_CONTROLLER_CASTS_SPELL: source gets +X/+X until end of turn where X = cast spell's mana value (`null` filter = any spell). Manaplasm
- `CasterLosesLifeOnSpellCastEffect(CardPredicate spellFilter, int amount)` — ON_ANY_PLAYER_CASTS_SPELL: "that player" (the caster) loses N life (Soot Imp)
- `BecomePreparedEffect()` — source becomes "prepared" (Strixhaven); exiles a castable copy of its prepare spell (back face)
- `MakeTargetCreaturePreparedEffect()` — target creature becomes prepared; no-op if already prepared or no prepare spell
- `MakeTargetCreatureUnpreparedEffect()` — target creature becomes unprepared; no-op if not prepared

See EFFECTS_INDEX.md "Damage" section for 15+ additional niche damage effects.

## Destruction / sacrifice

- `DestroyTargetPermanentEffect(boolean cantRegen)` or `(boolean, CreateTokenEffect)` — destroy target
- `DestroyEachTargetPermanentEffect(boolean cantRegen)` or `()` — destroy every target in the group; bind to one multi-target group. Pair with `targetX(filter, cap)` for "Destroy X target …" (Dregs of Sorrow)
- `DestroyTargetPermanentAtEndStepEffect()` — destroy at end step
- `SacrificeTargetPermanentAtEndStepEffect()` — sacrifice the target at next end step (Lowland Oaf); sacrifice, not destruction (ignores indestructible/regeneration)
- `DestroyTargetCreatureAndCreateSpiritCopyToSacrificeEffect()` — destroy target creature (can't be regenerated), then create a black Spirit token for the controller with the destroyed creature's last-known power/toughness, sacrificed at next end step. Token created even if destruction fails (indestructible). Card supplies the target filter (Broken Visage: nonartifact attacking creature)
- `ReturnTargetPermanentToHandAtEndStepEffect()` — return the target to its owner's hand at next end step (Dragon Mask); pair with a pump on the shared target
- `ExileSelfAtEndStepEffect()` — exile the source permanent (no target) at next end step (Dark Maze); pair with `CanAttackAsThoughNoDefenderEffect` on a `{0}` attack ability
- `DestroyAllPermanentsEffect(PermanentPredicate)` or `(PermanentPredicate, boolean)` — board wipe
- `DestroyAllPermanentsAndGainLifePerDestroyedEffect(PermanentPredicate, int)` — wipe + life
- `DestroyCreaturesTargetPlayerControlsAndLoseLifePerDestroyedEffect(int)` — destroy creatures target player controls; controller loses N life per destroyed. Needs `target(...)` a player
- `DestroyPermanentsTargetPlayerControlsEffect(PermanentPredicate)` — destroy every permanent matching the predicate that the target player controls (targets a player, harmful; honours indestructible/regeneration). Ajani Vengeant −7 `PermanentIsLandPredicate` = "destroy all lands target player controls"
- `EachPlayerChoosesCreatureDestroyRestEffect()` — choose one, destroy rest
- `DestroyAllCreaturesAndCreateTokenFromDestroyedCountEffect(String, List, Set)` — wipe + X/X token
- `DestroyTargetPermanentAndControllerSearchesLibraryToBattlefieldEffect(CardPredicate, boolean may[, boolean tapped])` — destroy + controller searches to battlefield (tapped optional, e.g. Erode)
- `DestroyTargetAndEachPlayerSearchesBasicLandToBattlefieldEffect()` — destroy + each searches
- `EachOpponentMaySearchLibraryForBasicLandToBattlefieldTappedEffect()` — opponents search
- `EachOpponentMaySearchLibraryForCreatureToBattlefieldEffect()` — each opponent may tutor a creature onto the battlefield (untapped), then shuffle; APNAP; Boldwyr Heavyweights
- `PutCreatureFromHandThenSacrificeUnlessPayReducedEffect(int genericReduction)` — SPELL: you may put a creature from your hand onto the battlefield; then sacrifice it unless you pay its mana cost reduced by `{genericReduction}` (generic only, floored at 0). Declinable card choice + a resolution-time pay-or-sacrifice may ability; Flash (`2`)
- `EachPlayerMaySearchLibraryForCreaturesToHandEffect(DynamicAmount count)` — each player (APNAP) may tutor up to `count` creatures to hand; `()` = X (Weird Harvest)
- `DestroyTargetLandAndDamageControllerEffect(int)` — destroy land + damage
- `DestroyTargetPermanentAndDamageControllerIfDestroyedEffect(int)` — destroy + conditional damage
- `DestroyUpToTargetsThenReturnFromGraveyardEffect()` — destroy each targeted permanent and return cards put into graveyard this way under your control (multi-target via ability `minTargets`/`maxTargets`)
- `DestroyTargetPermanentThenEffect(EventStat, CardEffect thenEffect, ThenEffectRecipient[, PermanentPredicate])` — collapsed destroy-plus-value family. Destroy the target, then resolve an existing then-effect. `recipient` CONTROLLER (you) / TARGET_CONTROLLER (destroyed permanent's controller). `EventStat` NONE/MANA_VALUE/TOUGHNESS snapshots the destroyed permanent's last-known stat onto `eventValue` for a `GainLifeEffect(EventValue())` / `BoostSelfEffect(EventValue(), Fixed(0))` then-effect. Then-effects: `GainLifeEffect`, `BoostSelfEffect`, `LoseLifeEffect`, `GivePoisonCountersEffect`. Optional `PermanentPredicate` gates the then-effect on the destroyed permanent's state (Death's Caress HUMAN). Then-effect happens even if destruction fails (indestructible)
- `DestroySourcePermanentEffect()` / `(boolean cannotBeRegenerated)` — destroy source; pass `true` to ignore regeneration shields (Aether Storm)
- `DestroyEnchantedPermanentEffect()` — destroy the permanent the source Aura is attached to (Spreading Algae, on `ON_ENCHANTED_PERMANENT_TAPPED`)
- `DestroyCreatureBlockingThisEffect()` — destroy blocker
- `DestroyCombatOpponentAtEndOfCombatEffect(PermanentPredicate filter, boolean cannotBeRegenerated)` — Basilisk-style "blocks or becomes blocked by a [filter] creature, destroy that creature at end of combat". Put on ON_BLOCK + ON_BECOMES_BLOCKED (`TriggerMode.PER_BLOCKER`); filter re-checked at resolution (Deathgazer nonblack). Destroys at end of combat, not immediately. Also works on an **Aura/Equipment** — it implements `CombatOpponentReferencingEffect`, so `CombatTriggerService` auto-targets the enchanted/equipped creature's combat opponent (Venom = non-Wall filter, `PermanentNotPredicate(PermanentHasSubtypePredicate(WALL))`)
- `PutCounterOnCombatOpponentAtEndOfCombatEffect(CounterType counterType, int amount)` — Greater Werewolf-style "At end of combat, put a [counter] on each creature blocking or blocked by this creature". Put on ON_BLOCK + ON_BECOMES_BLOCKED (`TriggerMode.PER_BLOCKER`); the combat opponent is queued a `PutCounterOnPermanentAtEndOfCombat` delayed action, applied in `CombatService.processEndOfCombatOpponentCounters` (respects `cantHaveCounters`). Greater Werewolf = `(CounterType.MINUS_ZERO_MINUS_TWO, 1)`. The `MINUS_ZERO_MINUS_TWO` counter subtracts 2 toughness each (`Permanent.getEffectiveToughness`/`getToughnessModifiers`)
- `DestroySelfAtEndOfCombatEffect()` — schedule the **source** permanent for destruction at end of combat (regeneration/indestructible apply, unlike `SacrificeAtEndOfCombatEffect`). "When this creature blocks/attacks, destroy it at end of combat." Put on ON_BLOCK / ON_ATTACK. Cinder Wall
- `PutMinusOneCounterOnSourceAtEndOfCombatEffect()` — schedule the **source** permanent to get a -1/-1 counter at end of combat (delayed, so it stays full size during combat damage — unlike immediate `PutCountersOnSourceEffect(-1,-1,1)`). "Whenever this creature attacks or blocks, put a -1/-1 counter on it at end of combat." Put on ON_ATTACK and/or ON_BLOCK. Wicker Warcrawler
- `RemoveCounterFromSourceAtEndOfCombatEffect(CounterType)` — schedule the **source** permanent to have one counter of the given type removed at end of combat (delayed, so it stays full size during combat damage), clamped at zero. "At end of combat, if this creature attacked or blocked this combat, remove a [type] counter from it." Put on ON_ATTACK and/or ON_BLOCK (scheduling only from those slots encodes the "attacked or blocked" condition). Removal-analog of `PutMinusOneCounterOnSourceAtEndOfCombatEffect`; drained in `CombatService.processEndOfCombatCounterRemovals`. Clockwork Beast (`CounterType.PLUS_ONE_PLUS_ZERO`)
- `PutCappedCountersOnSourceEffect(CounterType, DynamicAmount amount, int cap)` — put up to `amount` counters of the type on the **source**, never raising the total of that type above `cap`. "Put up to X [type] counters on this creature. This ability can't cause the total to be greater than [cap]." Clockwork Beast's `{X}, {T}` upkeep ability = `(CounterType.PLUS_ONE_PLUS_ZERO, new XValue(), 7)`. `PLUS_ONE_PLUS_ZERO` counters add 1 power each (`Permanent.getEffectivePower`/`getPowerModifiers`)
- `SacrificePermanentsEffect(count, PermanentPredicate, SacrificeRecipient)` — collapsed forced-sacrifice family. `SacrificeRecipient` = CONTROLLER / TARGET_PLAYER / EACH_PLAYER / EACH_OPPONENT. Bare `PermanentIsCreaturePredicate` → single-select "sacrifice a creature" (Cruel Edict, Grave Pact, Stitcher's Apprentice); any other filter → multi-permanent choice (Storm Fleet Arsonist, Yawning Fissure, Destructive Force). int-count sugar ctor
- `TargetPlayerChoosesCreatureDestroyEffect()` — SPELL, player-targeting spec: target opponent chooses a creature they control, then it is **destroyed** (regeneration/indestructible apply — this is the destroy analog of the "sacrifice a creature" edict). 0 creatures ⇒ nothing; 1 ⇒ auto; 2+ ⇒ target picks. Imperial Edict
- `OpponentChoosesCreatureToDestroyEffect()` — non-targeting: an opponent of the controller chooses **any** creature on the battlefield and it is destroyed (regeneration/indestructible apply). 0 ⇒ nothing; 1 ⇒ auto; 2+ ⇒ opponent picks. Pair after `DestroyTargetPermanentEffect` for "destroy target creature of your choice, then destroy target creature of an opponent's choice" (Diaochan, Artful Beauty)
- `PlayerDestroysPermanentsEffect(count, PermanentPredicate, DestroyRecipient)` — a player chooses and **destroys** N of their own permanents matching the filter (regeneration/indestructible apply — the destroy analog of `SacrificePermanentsEffect`). `DestroyRecipient` = CONTROLLER / TARGET_PLAYER (TARGET_PLAYER makes the spec player-targeting). ≤N matching ⇒ all destroyed, no choice; >N ⇒ player picks which. int-count sugar ctor. "You destroy four lands you control" = `(4, PermanentIsLandPredicate, CONTROLLER)`; Burning of Xinye uses both recipients + `MassDamageEffect(4)`
- `SacrificeCreatureAndControllerGainsLifeEqualToToughnessEffect(boolean sacrificerIsController)` — sacrifice + life = toughness. `false` = target sacrifices (edict, Tribute to Hunger); `true` = controller sacrifices, non-targeting (Doomgape upkeep)
- `EachPlayerSacrificesGreatestManaValueCreatureUnlessPaysEffect()` — non-targeting SPELL: in APNAP order, each player sacrifices the creature they control with the greatest mana value **unless they pay that creature's mana cost**; a player with ties picks which tied creature is at risk. Punisher prompt via the may-ability system (decline or can't-pay ⇒ sacrifice). Tariff. Sequenced by `TariffSupport` + `gameData.tariffRemainingPlayers`; tie-break uses `PermanentChoiceContext.TariffTieBreak`
- `SacrificeCreatureToCreateTokensEqualToToughnessEffect(CreateTokenEffect template, PermanentPredicate filter)` — controller sacrifices a matching creature, then creates X copies of `template` where X = sacrificed creature's toughness (template `amount` ignored). Wrap in `MayEffect` for "you may sacrifice" (e.g. Feed the Pack)
- `SacrificeCreatureCreateSizedTokenEqualToPowerEffect(CreateTokenEffect template, PermanentPredicate filter)` — controller sacrifices a matching creature, then creates ONE `template` token whose power and toughness are each equal to the sacrificed creature's power (template `power`/`toughness`/`amount` ignored). The single-token, power-sized analog of the toughness effect above. Ooze Garden (green Ooze template; `PermanentNotPredicate(PermanentHasSubtypePredicate(OOZE))` for "non-Ooze")
- `TargetPlayerSacrificesCreatureThenCreateTokensIfSubtypeEffect(CardSubtype requiredSubtype, CreateTokenEffect tokenTemplate)` — targets a player (player-targeting spec); that player sacrifices a creature of their choice, and if it had `requiredSubtype` (last-known info) the same player creates the template tokens under their own control. Warren Weirding ("gains haste until end of turn" → template's `grantedKeywordsUntilEndOfTurn`)
- `SacrificeTargetCreatureThenCreateTokensEqualToPowerEffect(CreateTokenEffect tokenTemplate)` — targets a creature (permanent-targeting spec); its controller sacrifices it, then that same player creates X copies of `template` where X = the creature's effective power captured before removal (template `amount` ignored). Mercy Killing (1/1 green-and-white Elf Warrior template). The power-based, targeted, controller-creates analog of `SacrificeCreatureToCreateTokensEqualToToughnessEffect`
- `ForcedCostOrElseEffect(CostEffect, List<CardEffect>[, boolean optional])` — cost-like instruction; if it cannot be performed, resolve fallback effects. `optional=true` makes it a "you may [cost]. If you don't, [fallback]" choice (Yawgmoth Demon); default `false` is mandatory (Archdemon of Greed). Supported costs: `SacrificePermanentCost` (single) and `SacrificeMultiplePermanentsCost` (N of a filter, e.g. Rathi Dragon). Supported fallbacks: `TapPermanentsEffect(SELF)`, `DealDamageToPlayersEffect(CONTROLLER, Fixed)`, `SacrificeSelfEffect`
- `AddCounterThenPayCountersOrTapAndDamageEffect(CounterType)` — single upkeep trigger: "put a [counter] on this, then you may pay {X} (X = that counter type on it); if you don't, tap this and it deals X damage to you." Places the counter, snapshots X, then reuses the `ForcedCostOrElse` may-pay-or-tap-self-and-damage-controller machinery (Primordial Ooze). One effect, not two, so counter+X stay in one ability
- `SacrificeAttackingCreaturesEffect(int base, int metalcraft)` — sacrifice attackers
- `EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect(int, CardPredicate)` or `(int, CardPredicate, CounterType)` — mass reanimate; optional trailing `CounterType` = each returned card enters with one such counter (Pyrrhic Revival: `Integer.MAX_VALUE, CardTypePredicate(CREATURE), MINUS_ONE_MINUS_ONE`)
- `ReturnCardsFromControllerGraveyardToBattlefieldEffect(CardPredicate, int)` — return up to N of controller's own graveyard cards to the battlefield (resolution-time choice; non-targeting). Reveillark
- `SacrificeSelfEffect()` — sacrifice self
- `SacrificeSelfThenDealDamageToTargetPlayerEffect(int damage)` — sac source; if sacrificed, deal N to stack entry's targetId player (Booby Trap trigger)
- `SacrificeSelfIfEvokedEffect()` — evoke sacrifice; ON_ENTER_BATTLEFIELD, fires only when cast for evoke cost
- `SacrificeUnlessDiscardCardTypeEffect(CardType)` / `(CardType, boolean random)` — sacrifice unless discard (`random=true` = discard at random, Pillaging Horde; `null` type = any card)
- `SacrificeUnlessReturnOwnPermanentTypeToHandEffect(CardType)` — sacrifice unless bounce own
- `ChampionCreatureEffect(CardSubtype...)` — champion a creature (no subtype = any creature; multiple = inclusive, e.g. Goblin or Shaman); exile on ETB, return when source leaves
- `SacrificeSelfAndDrawCardsEffect(int)` — sacrifice + draw
- `SacrificeSelfAndTargetPlayerDiscardsEffect(int)` — "sacrifice this, then that player discards N cards." Wrap in `MayEffect` for "you may". `targetId` = the affected player (e.g. defending player on `ON_ATTACKS_UNBLOCKED`), `sourcePermanentId` = source creature. Mindstab Thrull (3, unblocked-attack)
- `SacrificeAtEndOfCombatEffect()` / `SacrificeAtEndOfCombatEffect(int damageToController)` — sacrifice the source at end of combat (Fog Elemental). With `damageToController > 0` the source also deals that much damage to its controller at end of combat ("sacrifice it and it deals N damage to you", Time Elemental = 5); the damage is a delayed trigger and fires even if the creature already left the battlefield. Put on ON_ATTACK / ON_BLOCK
- `SacrificeTargetThenRevealUntilTypeToBattlefieldEffect(Set<CardType>)` — Polymorph
- `RevealUntilNonlandCardsToHandRestToBottomEffect(int)` — reveal until N nonland to hand, rest (lands) to bottom in any order (Fathom Trawl)
- `RevealUntilLandToBattlefieldRestToBottomEffect()` — reveal until a land, put that land onto the battlefield, rest to bottom in any order (Recross the Paths; used as a `ClashEffect` pre-clash body)

See EFFECTS_INDEX.md "Destruction" section for 10+ additional niche destruction/sacrifice effects.

### Sacrifice costs

- `ExileSelfCost()` — exile self as cost
- `SacrificeSelfCost()` — sacrifice self as cost
- `RemoveAllCountersAsCostEffect(CounterType)` — remove all counters of a type as cost; count snapshotted into xValue (Jar of Eyeballs: `EYEBALL`)
- `RemoveAllCountersFromSelfEffect(CounterType)` — resolution effect: remove all counters of a type from self; count snapshotted as the entry's event value so a later effect reads "that much" via `EventValue` (Ashling the Pilgrim + `MassDamageEffect(new EventValue(), true)`)
- `SacrificeCreatureCost()` or `(boolean trackMV)` or `(boolean trackMV, boolean trackPower)` or `(boolean, boolean, boolean trackToughness)` or `(boolean, boolean, boolean, boolean excludeSelf)` or `(ManaColor trackColorSymbols)` — sacrifice creature. The `ManaColor` ctor snapshots the number of that color's mana symbols in the sacrificed creature's mana cost into xValue (Fiery Bombardment: `RED` + `DealDamageToAnyTargetEffect(new XValue())`)
- `SacrificeArtifactCost()` — sacrifice artifact
- `SacrificePermanentCost(PermanentPredicate, String[, excludeSource])` — sacrifice matching permanent; use creature+subtype predicates with `excludeSource=false` for source-eligible "sacrifice a [subtype]"
- `SacrificePermanentsSequenceCost(List<PermanentPredicate> filters, List<String> descriptions)` — sacrifice one distinct permanent per per-slot filter, in order (Angel's Herald: "a green creature, a white creature, and a blue creature"). Use this single cost rather than several `SacrificePermanentCost` entries — the activation resume path carries only one cost effect through interactive picks, so multiple distinct sacrifice costs on one ability silently skip the 2nd/3rd. Dead-end-safe (only offers picks that keep the remaining slots matchable)
- `DiscardCardTypeCost(CardPredicate, String)` — discard matching card
- `DiscardRandomCardCost()` — discard one card at random from hand (no choice); requires a non-empty hand (Coral Helm)
- `RemoveCounterFromSourceCost(int, CounterType)` — remove counters from self
- `RemoveCounterFromSourceEffect(CounterType, int amount)` — resolved (not a cost): remove up to `amount` counters of a type from the SOURCE permanent, clamped at zero (no-op if none). Self-targeting, so trigger collectors carry the source id. Pair with a `SpellCastTriggerEffect(new CardColorPredicate(...), ...)` for "whenever you cast a [color] spell, remove a -1/-1 counter from this" (Shrewd Hatchling)
- `RemoveCounterFromTargetAndGainLifeEffect(CounterType, int lifeGain)` — remove one counter of a type from target permanent; gain `lifeGain` life only if a counter was removed ("If you do") (Woeleecher: `MINUS_ONE_MINUS_ONE`, 2)
- `RemoveCounterFromSourceAndGainLifeEffect(CounterType, int lifeGain)` — SOURCE analogue of the above: remove one counter from this permanent; gain `lifeGain` life only if a counter was removed ("If you do"). Non-targeting; wrap in `MayEffect` for the "you may" form (Living Artifact: `VITALITY`, 1, on `UPKEEP_TRIGGERED`)
- `RemoveCounterFromTargetPermanentEffect()` — remove one counter of any kind currently on target permanent (first present type when several); no-op if none. "Remove a counter from target permanent" (Medicine Runner)
- `CrewCost(int)` — crew
- `TapCreatureCost(PermanentPredicate)` — tap creature
- `PayLifeCost(int)` — pay life; `PayLifeCost.halfLife()` pays half your life rounded up
- `ExileCardFromGraveyardCost(CardType)` / `(CardSubtype)` + overloads — exile graveyard card (subtype ctor for "Exile an Elf card", Scarred Vinebreeder)
- `ReturnCreatureToHandCost()` — additional spell cost: return a creature you control to hand (Familiar's Ruse)
- `PutCounterOnControlledCreatureCost(CounterType, int count)` — additional spell cost: put counter(s) on a creature you control (Scarscale Ritual: `MINUS_ONE_MINUS_ONE, 1`); creature supplied via `sacrificePermanentId`, paid in `SpellCastingService`

See EFFECTS_INDEX.md "Sacrifice costs" for additional cost effects.

## Counter spells

- `CounterSpellEffect()` — counter target spell
- `CounterSpellAndCreateTreasureTokensEffect()` — counter + treasures
- `CounterSpellAndExileEffect()` — counter + exile
- `CounterSpellAndExileAllWithSameNameEffect()` — counter + exile all same-name cards from controller's graveyard/hand/library, then shuffle (Counterbore)
- `CounterSpellIfControllerPoisonedEffect()` — counter if poisoned
- `TargetSpellControllerLosesLifeEffect(int)` — target spell controller loses life
- `TargetSpellControllerDiscardsEffect(int)` — target spell controller discards
- `TargetSpellControllerDrawsCardEffect()` — target spell controller draws a card; place before the counter (Dream Fracture)
- `CounterUnlessPaysEffect(int)` or `(int, boolean useX, boolean exileIfCountered)` or `(DynamicAmount)` — counter unless pays (`DynamicAmount` scales the cost, e.g. `PermanentCount(PermanentColorInPredicate(BLUE), CONTROLLER)` = "{1} for each blue permanent you control", Spell Syphon)
- `CounterUnlessDiscardsEffect()` — counter unless controller discards a card (Ward—Discard a card)
- `CounterSpellsNamedLikeCardsExiledWithSourceEffect()` — non-targeting: counter all stack spells named like a card exiled with the source (Grimoire Thief; pair with `SacrificeSelfCost`)
- `CounterSpellAndPutOnTopOfLibraryEffect()` — counter target spell, put it on top of its owner's library instead of the graveyard (Memory Lapse)
- `CounterSpellAndGainControlIfArtifactOrCreatureEffect()` — counter target spell; if it was an artifact or creature spell, put that card onto the battlefield under your control instead of the graveyard (Desertion)
- `CounterlashEffect()` — counter target spell, then may cast from hand sharing a card type without paying mana cost
- `RegisterDelayedManaEqualToTargetSpellManaValueEffect(ManaColor)` — Scattering Stroke clash reward: wrap in `ClashEffect` before the counter; may add {C} equal to the countered spell's mana value at your next main phase
- `MayCastFromHandWithoutPayingManaCostEffect()` — marker for may-cast-from-hand routing in PendingMayAbility
- `MayCastFromHandSharingNameWithSpellCastThisTurnEffect()` — Twinning Glass activated ability: offer to cast a nonland hand card for free if its name matches a spell any player cast this turn (reuses the Counterlash routing)
- `ReplaceControlledCounterWithExileAndPlayEffect()` — STATIC (Guile): your counters exile the spell instead and you may play it free
- `MayPlayExiledCounteredCardEffect()` — marker for the Guile free-play routing in PendingMayAbility
- `CantBeCounteredEffect()` — can't be countered (static)
- `MakeTargetSpellUncounterableEffect()` — target spell can't be countered (activated/spell; targets a spell on the stack, Vexing Shusher)
- `CreatureSpellsCantBeCounteredEffect()` — creatures can't be countered (static)
- `CreatureEnteringDontCauseTriggersEffect()` — Torpor Orb (static)
- `ETBDoubleTriggerEffect(CardPredicate)` — double ETB triggers (static)
- `CreaturesEnterAsCopyOfSourceEffect()` — Essence of the Wild (static)
- `ExileOpponentCardsInsteadOfGraveyardEffect()` — Leyline of the Void (static)
- `RevealAndPutOnBottomOfLibraryInsteadOfGraveyardEffect()` — Wheel of Sun and Moon (static, player aura; cards to enchanted player's graveyard go to bottom of their library instead; pair with `setEnchantPlayer(true)`)
- `ExileOwnCardsInsteadOfGraveyardEffect()` — controller's own cards are exiled instead of going to their graveyard (static, Forbidden Crypt)
- `ReturnFromGraveyardInsteadOfDrawEffect()` — if you would draw, return a card from your graveyard to hand instead; lose if you can't (static, Forbidden Crypt)
- `PutOnTopOfLibraryInsteadOfDyingEffect()` — if this creature would die, put it on top of its owner's library instead (static replacement, Gravebane Zombie)

## Bounce / return to hand

- `ReturnToHandEffect` — unified bounce, **static factories only**: `.target()` (bounce target), `.targetAndControllerLosesLife(1)` (Vapor Snag), `.self()` (bounce source permanent), `.selfSpell()` (the resolving instant/sorcery returns itself to its owner's hand off the stack instead of the graveyard — Redeem the Lost's won-clash reward), `.allPermanentsMatching(filter)` (mass bounce matching permanents; null = every permanent — pass `PermanentIsCreaturePredicate` for creatures), `.permanentsTargetPlayerControls(filter)` (River's Rebuke), `.permanentsTargetPlayerOwns(filter)` (Hurkyl's Recall, owner-based)
- `ReturnTargetPermanentToHandWithManaValueConditionalEffect(int, CardEffect)` — bounce + MV bonus
- `ReturnTargetPermanentToHandOrLibraryTopByPredicateEffect(PermanentPredicate)` — bounce to hand, or to top of library instead when target matches predicate (Consign to Dream)
- `ReturnSelfToHandOnCoinFlipLossEffect()` — bounce self on coin flip loss
- `ReturnPermanentsOnCombatDamageToPlayerEffect()` or `(PermanentPredicate)` — Ninja-style
- `PutTargetOnBottomOfLibraryEffect()` — tuck bottom
- `PutTargetOnTopOfLibraryEffect()` — tuck top
- `PutTargetPermanentIntoLibraryNFromTopEffect(int)` — tuck N from top
- `PutSourceCardFromGraveyardOnTopOfOwnersLibraryEffect()` — ON_DEATH: put dying source on top of owner's library (Undying Beast)

## Graveyard return

- `ReturnCardFromGraveyardEffect.builder().destination(HAND|BATTLEFIELD|TOP_OF_OWNERS_LIBRARY)...build()` — unified graveyard return (see EFFECTS_INDEX.md for full builder API)
- `ReturnTriggeringLandFromGraveyardToBattlefieldEffect(UUID landCardId)` — Sacred Ground's trigger effect: return the identified land from the graveyard to the battlefield under its owner's control. Register the template with `null` on the `ON_ALLY_LAND_PUT_INTO_GRAVEYARD_BY_OPPONENT` slot; the collector stamps the real card id.
- `ReturnTargetCardOnDeathThisTurnEffect()` — SPELL delayed trigger (Graceful Reprieve): if the targeted creature dies this turn, return that card to the battlefield under its owner's control. Pair with a creature `target(...)`.
- `CreateTokenOnTargetDeathThisTurnEffect(CreateTokenEffect token)` — SPELL delayed trigger (Skeletonize): if the targeted creature dies this turn, the effect's controller creates `token`'s token(s). Pair with a creature `target(...)` and (typically) a `DealDamageToTargetCreatureEffect` in the same slot. Death pipeline pushes the token effect under the recorded controller.
- `ReturnTriggeringCardFromGraveyardToBattlefieldEffect()` — triggered-ability effect the death pipeline pushes for the above; returns the stack entry's card from its owner's graveyard to the battlefield. Not added to a card directly.
- `ReturnOneOfEachSubtypeFromGraveyardToHandEffect(List<CardSubtype>)` — one of each subtype
- `PutTargetCardsFromGraveyardOnTopOfLibraryEffect(CardPredicate)` — graveyard to top of library
- `ReturnTargetCardsFromGraveyardToHandEffect(CardPredicate, int)` — up to N cards to hand
- `ShuffleTargetCardsFromGraveyardIntoLibraryEffect(CardPredicate, int)` — target player shuffles N cards
- `ReturnDyingCreatureToBattlefieldAndAttachSourceEffect()` — reanimate + equip
- `ReturnDyingOpponentCreatureUnderYourControlEffect()` — ON_OPPONENT_CREATURE_DIES: steal the dying creature from its owner's graveyard onto your battlefield (Necroskitter); collector adds the "you may" and stamps the dying card id
- `StealDyingOpponentPermanentUnlessPaysLifeEffect(int lifeCost)` — ON_OPPONENT_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD punisher (`ControlStealingEffect`): any-type opponent permanent dies; that opponent pays `lifeCost` life or you put that card onto the battlefield under your control (Prince of Thralls, 3). Can't-pay = auto-steal; collector stamps dying card id + paying opponent
- `PutCardFromOpponentGraveyardOntoBattlefieldEffect(boolean tapped, CardPredicate filter, boolean requireManaValueEqualsX)` — put target card matching `filter` from an opponent's graveyard onto battlefield under your control, correctly tracked as stolen (returns to owner on leaving). `(boolean tapped)` and `()` default to artifact-or-creature + MV==X + mill-X (Geth). Ashen Powder: `(false, new CardTypePredicate(CREATURE), false)` — creature only, any MV, no mill
- `UndyingReturnEffect()` — Undying (CR 702.93) resolution: return the dying card from its owner's graveyard to the battlefield with a +1/+1 counter. Do NOT add to a card directly; it is pushed automatically by `PermanentRemovalService` when a creature with the `UNDYING` keyword dies with no +1/+1 counters. The keyword is loaded from Scryfall.
- `PersistReturnEffect()` — Persist (CR 702.79) resolution: return the dying card from its owner's graveyard to the battlefield with a -1/-1 counter. Do NOT add to a card directly; it is pushed automatically by `PermanentRemovalService` when a creature with the `PERSIST` keyword dies with no -1/-1 counters. The keyword is loaded from Scryfall.
- `PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect()` — opponent's creature with exile
- `GrantTargetCreatureCardGraveyardCastAndCopyActivatedAbilitiesEffect()` — target creature card in any graveyard may be cast this turn; when cast, source gains its activated abilities
- `GrantSourceActivatedAbilitiesUntilEndOfTurnEffect(List<ActivatedAbility>, String)` — delayed source grant used after casting the selected graveyard creature

## Draw / discard / hand manipulation

- `DrawCardEffect(DynamicAmount)` or `(int)` — controller draws that many; use `XValue` for "draw X", `PermanentCount`/`CardsInGraveyard`/`CountersOnSource` for "draw a card for each …"
- `DrawCardPerChosenTypeCountEffect()` — "Choose a creature type. Draw a card for each permanent you control of that type" (Distant Melody). Resolution-time creature-type choice (`beginSpellCreatureTypeChoice`, stored on `GameData.chosenSpellSubtype`), Changeling-aware count; draw sibling of `DealDamageToTargetCreatureEqualToChosenTypeCountEffect`
- `GainLifePerChosenTypeCountEffect(int lifePerPermanent)` — "Choose a creature type. You gain `lifePerPermanent` life for each permanent you control of that type" (Luminescent Rain = `(2)`). Resolution-time creature-type choice (`beginSpellCreatureTypeChoice`, stored on `GameData.chosenSpellSubtype`), Changeling-aware count; gains `count*lifePerPermanent` life. Life sibling of `DrawCardPerChosenTypeCountEffect`
- `BoostTargetCreaturePerChosenTypeCountEffect(int powerPer, int toughnessPer)` — "Choose a creature type. Target creature gets `powerPer`/`toughnessPer` until end of turn for each permanent of the chosen type you control" (Pack's Disdain = `(-1, -1)`). Resolution-time creature-type choice (`beginSpellCreatureTypeChoice`, stored on `GameData.chosenSpellSubtype`), Changeling-aware count; applies `count*powerPer`/`count*toughnessPer` as a until-end-of-turn P/T modifier. Boost sibling of `DealDamageToTargetCreatureEqualToChosenTypeCountEffect`; pair with a `PermanentIsCreaturePredicate` target filter
- `PayXLifeDrawXCardsEffect()` — SPELL: resolution-time X choice — controller picks X (capped at current life), pays X life, draws X cards (Necrologia). Pair with `setSpellCastTimingRestriction(YOUR_END_STEP)` for "cast only during your end step"
- `EachPlayerDrawsCardEffect(DynamicAmount)` or `(int)` — each player (turn order) draws that many; the amount is re-evaluated per drawing player, so player-relative amounts (`CardsInGraveyard(..., CONTROLLER)` = each player's own graveyard, Nature's Resurgence) count that player's objects. `XValue()` for "each player draws X" (Prosperity), `int` for a fixed count
- `DrawCardForTargetPlayerEffect(DynamicAmount, boolean requiresUntapped, boolean targets)` or `(int)` — target/entry player draws; `XValue` for "target player draws X"
- `DyingCreatureControllerMayDrawCardEffect()` — ON_ANY_CREATURE_DIES marker: whenever any creature dies, the DYING creature's controller (may be an opponent of the source) may draw a card (Fecundity). Unlike a plain `MayEffect(DrawCardEffect())` on that slot, which offers the draw to the source's controller
- `DefendingPlayerMayDrawCardEffect()` — ON_ATTACK marker: "whenever this creature attacks, defending player may draw a card" (Sibilant Spirit). `CombatAttackService` routes the optional draw to the defending player (or the attacked planeswalker's controller), not the attacking creature's controller. Unlike a plain `MayEffect(DrawCardEffect())`, which offers the draw to the source's controller
- `DyingCreatureControllerDiscardsCardEffect()` — ON_ANY_CREATURE_DIES marker: whenever a qualifying creature dies, the DYING creature's controller (may be an opponent) discards a card (mandatory). Bereavement wraps it in `TriggeringCardConditionalEffect(CardColorPredicate(GREEN), …)` for "a green creature dies"
- `DrawAndDiscardCardEffect(int draw, int discard)` — loot
- `DiscardAndDrawCardEffect(int discard, int draw)` — rummage
- `DiscardEffect(DynamicAmount, DiscardRecipient, boolean random)` — the whole discard family; `recipient` ∈ {`CONTROLLER`, `TARGET_PLAYER`, `TARGET_PERMANENT_CONTROLLER`, `TARGET_PLAYER_OR_PERMANENT_CONTROLLER`, `EACH_PLAYER`, `EACH_OPPONENT`}, `random` picks chosen vs random discard. `TARGET_PLAYER_OR_PERMANENT_CONTROLLER` (Blightning) declares no target and piggybacks on a companion player-or-planeswalker effect's `targetId`, routing the discard to the targeted player or the targeted planeswalker's controller. `(int, recipient, random)` / `(DynamicAmount, recipient)` / `(int, recipient)` convenience ctors (last two non-random). `CountersOnSource(CHARGE)` for per-charge-counter (Shrine of Limitless Power), `XValue()` for Mind Shatter (`TARGET_PLAYER`, random)
- `RevealHandAndRandomDiscardCardTypeEffect(CardType)` — target player reveals hand, discards one card of type at random (Rag Man, CREATURE); player-targeting spec
- `RevealTargetHandEffect()` — target player reveals their whole hand (logged); nothing further happens. Pair with a companion targeted effect on the same player, e.g. `LoseLifeEffect(1, TARGET_PLAYER)` (Thoughtcutter Agent). `TargetSpec.benign(PLAYER)`
- `DiscardHandEffect(DiscardRecipient)` — discard entire hand(s); no-arg = controller. `TARGET_PLAYER` implements `CombatDamageTriggerContextEffect` (DAMAGED_PLAYER), so it works as an ON_DAMAGE_TO_PLAYER / ON_COMBAT_DAMAGE_TO_PLAYER trigger with the damaged player baked in (Nicol Bolas)
- `DiscardEffect(DynamicAmount, DiscardRecipient, boolean random)` — the whole discard family; `recipient` ∈ {`CONTROLLER`, `TARGET_PLAYER`, `EACH_PLAYER`, `EACH_OPPONENT`}, `random` picks chosen vs random discard. `(int, recipient, random)` / `(DynamicAmount, recipient)` / `(int, recipient)` convenience ctors (last two non-random). `CountersOnSource(CHARGE)` for per-charge-counter (Shrine of Limitless Power), `XValue()` for Mind Shatter (`TARGET_PLAYER`, random)
- `RevealHandAndRandomDiscardCardTypeEffect(CardType)` — target player reveals hand, discards one card of type at random (Rag Man, CREATURE); `canTargetPlayer`
- `DiscardHandEffect(DiscardRecipient)` — discard entire hand(s); no-arg = controller
- `EachPlayerDiscardsFractionOfHandRoundedUpEffect(int divisor)` — each player discards `ceil(handSize/divisor)` cards of their choice, recomputed per player (APNAP). Use (not `DiscardEffect(EACH_PLAYER)`, which applies one shared amount) when the count is a per-player fraction. Pox (3)
- `EachPlayerSacrificesFractionRoundedUpEffect(int divisor, PermanentPredicate filter)` — each player sacrifices `ceil(matching/divisor)` of their matching permanents of their choice, recomputed per player (APNAP simultaneous, CR 101.4). Pox: `(3, new PermanentIsCreaturePredicate())` + `(3, new PermanentIsLandPredicate())`
- `DiscardHandUnlessPaysLifeEffect(int lifeCost)` — target player discards their entire hand unless they pay `lifeCost` life; target chooses (can't-pay → auto-discard). Pair with `PlayerPredicateTargetFilter`. Tyrannize (7)
- `DiscardOwnHandThenDrawThatManyEffect()` — discard entire hand, then draw that many
- `TargetPlayerDiscardsThenDrawsThatManyEffect(N)` — target player discards N cards, then draws as many as they discarded (draw = `min(N, hand size)`); player-targeting spec. Forget
- `DiscardThenReturnFromGraveyardToHandEffect(DynamicAmount)` — controller discards `amount` cards, then returns a card from their graveyard to hand for each card discarded this way (returns `min(amount, hand size)`, chosen one at a time). Recall = `XValue()`; pair with a trailing `ExileSpellEffect()`
- `DiscardOwnHandThenDrawEqualToTargetPlayerHandSizeEffect()` — discard entire hand, then draw equal to target player's hand size (counted at draw time)
- `DiscardOwnHandThenDrawEffect(DynamicAmount)` — discard entire hand, then draw equal to a DynamicAmount (evaluated at draw time; independent of discard count). Knollspine Dragon = `DamageDealtToTargetPlayerThisTurn`
- `EachPlayerDiscardsHandThenDrawsThatManyEffect()` — each player (APNAP) discards their entire hand, then draws that many
- `EachPlayerDiscardsAnyNumberThenDrawsThatManyEffect()` — each player (APNAP) discards any number of cards (their choice), then draws that many (Flux)
- `EachPlayerMayDiscardUpToThenTakeDamageEffect(int amount)` — each player (APNAP) may discard up to `amount` cards (their choice, capped at hand size), then is dealt `amount - discarded` damage; empty/zero-discard players take the full `amount` (Mind Bomb, `amount` = 3)
- `EachPlayerCreatesTokenEffect(CreateTokenEffect token)` — each player (turn order) creates the wrapped `token` under their own control; the token's dynamic amount is re-evaluated per creating player, so `CountScope.CONTROLLER` reads each player's own board (Waiting in the Weeds)
- `EachPlayerPaysAnyLifeForTokensEffect(CreateTokenEffect token)` — starting with controller, each player may pay any amount of life, round-robin until a full round of no payments; each creates one `token` per life paid (Plague of Vermin)
- `ExileTopCardsMayPlayUntilNextTurnEffect(DynamicAmount count)` or `(int count)` — exile top N from library, may play until end of your next turn (owner-relative expiry via `ExileSupport.grantPlayUntilOwnersNextTurn`). Use `EventValue()` for "equal to the excess damage dealt this way" (Archaic's Agony)
- `ExileTopCardMayPlayUntilNextUpkeepEffect()` — exile the top card of **your** library; may play it (any type) until the beginning of your next upkeep, then it just loses play permission and stays in exile (Elkin Bottle). Grants `exilePlayPermissions` + queues a `RevokeExilePlayPermissionAtNextUpkeep` delayed action (drained in `StepTriggerService.handleUpkeepTriggers` when the controller is active). Unlike `ExileTopCardsMayPlayUntilNextTurnEffect` the window ends one turn earlier (upkeep, not turn-end); unlike Grinning Totem the card is never moved to a graveyard
- `ExileTopCardOfOpponentLibraryControllerMayPlayThisTurnEffect()` — target opponent exiles the top card of their library; the source's **controller** may play that card (lands and spells, normal costs/timing) until **end of turn** (Knacksaw Clique). Card owned by the opponent; grants `exilePlayPermissions` to the controller + `exilePlayPermissionsExpireEndOfTurn`. Two-player: single opponent derived
- `ExileTargetPermanentMayPlayUntilNextTurnEffect()` — exile the target permanent, its owner may play it until end of their next turn (e.g. Suspend Aggression; pair with a permanent target filter). Tokens exiled this way cease to exist
- `ExileTargetCardFromGraveyardMayPlayUntilNextTurnEffect(CardPredicate filter, boolean ownGraveyardOnly)` — exile a targeted graveyard card matching the filter, controller may play it until end of their next turn (e.g. Practiced Scrollsmith; ETB graveyard-target flow via `MultiGraveyardChoice`)
- `ExileTargetInstantOrSorceryFromOpponentGraveyardMayCastEffect()` — exile a targeted instant/sorcery from an opponent's graveyard; controller may cast it **this turn**, spending mana of any type, and it is exiled instead of going to a graveyard (Nita, Forum Conciliator). Uses `exilePlayPermissions` + `exilePlayPermissionsExpireEndOfTurn` + `exilePlayAnyManaType` + `exileInsteadOfGraveyard`. Targets graveyard (per-instance spec: any graveyard unless `ownGraveyardOnly`)
- `PlayTargetCardFromGraveyardWithoutPayingManaCostEffect(CardPredicate filter)` — "you may play target [filter] card from your **own** graveyard without paying its mana cost" (Horde of Notions). On resolution offers a may-play: land → battlefield, else cast for free. Targets the controller's own graveyard (kept `@ValidatesTarget` validator enforces own-only); routed by `MayCastHandlerService.handlePlayFromGraveyardChoice`
- `PlayImprintedCardWithoutPayingManaCostEffect()` — Hideaway "you may play the exiled card without paying its mana cost" activated ability (Howltooth Hollow). Offers a may-play of the source permanent's imprinted (face-down exiled) card: land → battlefield (counts as the land play for the turn), else cast from exile for free. Routed by `MayCastHandlerService.handlePlayImprintedCardChoice`. Gate with `ConditionalEffect(<play condition>, …)` (e.g. `NoPlayerHasCardsInHand`); pair with `ImprintFromTopCardsEffect(N)` on ON_ENTER_BATTLEFIELD
- `ChooseCardsFromTargetHandEffect(int|DynamicAmount count, List<CardType> excludedTypes[, List<CardType> includedTypes], HandChoiceDestination destination[, boolean returnOnSourceLeave])` — reveal target's hand, caster chooses N card(s) → `DISCARD` / `EXILE` / `TOP_OF_LIBRARY` (Duress, Kitesail Freebooter, Agonizing Memories). `count` accepts an `XValue()` for "choose X cards" (Mind Warp)
- `RevealCardsChooseOneToDiscardEffect(PermanentPredicate countFilter)` — target reveals X cards **of their choice** (X = number of the caster's permanents matching `countFilter`), then the caster picks one for the target to discard (Thieving Sprite, `PermanentHasAnySubtypePredicate(FAERIE)`). Unlike `ChooseCardsFromTargetHandEffect` the rest of the hand stays hidden; two-phase interaction (`RevealCardsFromHandChoice` → `ChooseRevealedCardToDiscardChoice`), phase 1 skipped when the hand is already ≤ X
- `TargetRevealsCardsControllerChoosesDiscardEffect(int revealCount[, int discardCount])` — target player reveals `revealCount` cards **of their choice** from hand (whole hand if fewer); the controller sees only those and picks `discardCount` of them (default 1; fewer if the hand held fewer) for the target to discard (Blackmail = reveal 3/discard 1; Noggin Whack = reveal 3/discard 2). Two-stage `RevealCardsDiscardChoice` interaction, discard picks looped one at a time; player-targeting spec. Contrast `ChooseCardsFromTargetHandEffect` (whole hand revealed, controller chooses)
- `RevealTargetHandDrawPerMatchingCardEffect(List<CardSubtype> subtypes, List<CardColor> colors)` — target opponent reveals hand; draw one card per card matching any subtype/color (counted once). Pair with `target(PlayerPredicateTargetFilter(OPPONENT))` (Baleful Stare)
- `RevealHandChooseCreatureGainLifeDiscardEffect(List<CardColor> colors)` — target opponent reveals hand; caster chooses one creature card whose colors include any of `colors` (empty = any color), gains life equal to its toughness, then the target discards it. Pair with `target(PlayerPredicateTargetFilter(OPPONENT))`; player-targeting spec (Talara's Bane = `List.of(GREEN, WHITE)`)
- `DiscardAllCardsOfChosenColorEffect()` — caster chooses a color, target player discards all cards of that color from hand. Pair with `target(PlayerPredicateTargetFilter(ANY))` (Persecute)
- `LookAtHandEffect()` — look at hand
- `LookAtHandChooseNonlandToBottomAndDrawEffect()` — look at target player's hand; caster **may** choose a nonland card (optional decline); if chosen, target reveals it, bottoms it, then draws a card. Pair with `target(PlayerPredicateTargetFilter(ANY))` (Vendilion Clique, `ON_ENTER_BATTLEFIELD`)
- `ShuffleHandIntoLibraryAndDrawEffect()` — wheel
- `PutHandOnBottomOfLibraryAndDrawEffect()` — target player puts hand on bottom of library, draws that many (Teferi's Puzzle Box, `EACH_DRAW_TRIGGERED`)
- `SylvanLibraryDrawEffect()` — controller draws two additional cards, then must resolve up to two cards drawn this turn (still in hand): put each on top of library or pay 4 life (Sylvan Library; wrap in `MayEffect` in `DRAW_TRIGGERED`). Reads `GameData.cardsDrawnThisTurnIds`; prompts `PendingInteraction.SylvanLibraryChoice`
- `DrawThenPutCardsFromHandOnTopOrBottomOfLibraryEffect(int drawCount, int putCount)` — draw `drawCount`, then choose `putCount` hand cards (multi-select) and put them **all** on top or **all** on the bottom of your library (single top/bottom pick applied to every chosen card). Dream Cache `(3, 2)`. Two chained interactions (`PutCardsFromHandOnLibraryCardChoice` → `PutCardsFromHandOnLibraryDestinationChoice`), reusing the existing choose-multiple-cards + choose-from-list frontend flows
- `EachPlayerShufflesHandAndGraveyardIntoLibraryEffect()` — Timetwister-style
- `EachPlayerKeepsCardsShufflesRestIntoLibraryEffect(int keepCount)` — each player (APNAP order) chooses up to `keepCount` cards in their hand to keep, shuffles the rest into their library; interactive per-player choice (Worldpurge, `keepCount=7`)
- `EachPlayerLosesUnspentManaEffect()` — each player's mana pool is emptied ("loses all unspent mana"; Worldpurge)

## Library manipulation

- `SearchLibraryEffect(DynamicAmount count, CardPredicate filter, LibrarySearchDestination destination, ManaValueBound manaValueBound, int castFromGraveyardCount)` — unified library search (collapsed the `SearchLibraryFor*` family). Convenience: `()` unrestricted-to-hand (Diabolic Tutor), `(filter)` filtered-to-hand, `(filter, destination)`, `(count, filter, destination)`, `(filter, int count, int cfg)` flashback tutor (Increasing Ambition `(null,1,2)`), `(filter, destination, bound)`. destination ∈ `HAND`/`BATTLEFIELD`/`BATTLEFIELD_TAPPED`/`TOP_OF_LIBRARY`; by-name via `CardNamedPredicate` (Squadron Hawk); MV bound via filter + `ManaValueBound` — `(exact, offset)` = X-relative (Citanul Flute, Birthing Pod `(true,1)`, Green Sun's Zenith `CardColorPredicate(GREEN)`), or `(DynamicAmount, exact, offset)` for a board-derived bound (Beseech the Queen — `PermanentCount` of lands controlled, null filter)
- `LibraryOfLatNamEffect()` — SPELL, "an opponent chooses one" of two modes for you: accept schedules a `DrawCardsAtNextUpkeep` delayed draw-3, decline pushes `SearchLibraryEffect()` (tutor to hand). The opponent decides via the may-ability accept/decline prompt (`LibraryOfLatNamEffectHandler` → `MayPenaltyChoiceHandlerService.handleLibraryOfLatNamChoice`). Library of Lat-Nam
- `CovenantOfMindsEffect()` — SPELL, targets an opponent: reveal top 3 cards of your library; the targeted opponent chooses via the may-ability accept/decline prompt. Accept → the revealed cards go to your hand; decline → they go to your graveyard and you draw five. (`CovenantOfMindsEffectHandler` → `MayPenaltyChoiceHandlerService.handleCovenantOfMindsChoice`). Covenant of Minds
- `SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect()` — Cultivate
- `TargetPlayerSearchesLibraryForBasicLandToBattlefieldTappedEffect()` — target player searches their library for a basic land card, puts it onto the battlefield tapped, then shuffles; targets a player (player-targeting spec), mandatory search that may fail to find; Fertilid
- `SacrificeAnyNumberOfLandsAndSearchThatManyLandsToBattlefieldTappedEffect()` — controller sacrifices any number of their lands (multi-permanent choice, 0 to all), then searches their library for up to that many land cards to the battlefield tapped, then shuffles; search count = lands sacrificed, may fail to find; Scapeshift
- `SacrificeAnyNumberOfPermanentsThenDrawPerSacrificedEffect(PermanentPredicate filter)` — controller sacrifices any number of their permanents matching `filter` (multi-permanent choice, 0 to all), then draws a card for each one sacrificed; Reprocess (`PermanentAnyOfPredicate` of artifact/creature/land)
- `ChooseFivePermanentsSearchSameNameToBattlefieldTappedEffect()` — controller chooses up to five different permanents they control (multi-permanent choice, capped at 5), then for each may search their library for a same-named card put onto the battlefield tapped, then shuffles; one optional single-name search per chosen permanent; Clarion Ultimatum
- `TargetPlayerLosesLifeAndSearchesLibraryToHandEffect(int lifeLoss)` — EACH_DRAW_TRIGGERED: the draw-step player (`entry.getTargetId()`) loses `lifeLoss` life, then does a mandatory unrestricted tutor of their own library to hand, then shuffles; Maralen of the Mornsong (with static `PlayersCannotDrawCardsEffect()`)
- `SearchLibraryForCurseToBattlefieldAttachedToEnchantedPlayerEffect()` — Curse (name not shared with one already on enchanted player) onto battlefield attached to enchanted player; Curse of Misfortunes
- `SearchLibraryForEquipmentToBattlefieldAndAttachEffect()` — search for an Equipment card, put it onto the battlefield, then choose a creature you control to attach it to, then shuffle; Stonehewer Giant. Controller picks the creature via a follow-up `PermanentChoiceContext.AttachEquipmentToCreature` (no interaction if no creatures)
- `SearchTargetLibraryForCardsToGraveyardEffect(int, Set<CardType>)` — target library to graveyard
- `SearchTargetLibraryForCardsToExileEffect(int count)` / `(DynamicAmount count, boolean upTo)` — search target player's library for up to `count` cards, exile them, then that player shuffles (Jester's Cap, count=3); `upTo=true` = "up to X" (may exile fewer), `count` may be a `PermanentCount` (Nightmare Incursion = number of Swamps you control). No play permission. Targets player
- `SearchTargetPlayerLibraryAndCastEffect(Set<CardType> castableTypes)` — search target opponent's library for a card of one of the types, caster may cast it without paying its mana cost, then that player shuffles (Knowledge Exploitation, INSTANT/SORCERY). Targets player; uses `CAST_WITHOUT_PAYING`
- `SearchTargetLibraryForCardToBattlefieldUnderControlEffect(CardPredicate filter)` — search target opponent's library for a card matching `filter`, put it onto the battlefield under the SEARCHER's control (owner unchanged), then that player shuffles (Bribery, `new CardTypePredicate(CardType.CREATURE)`). Targets player, may fail to find; uses `BATTLEFIELD_UNDER_SEARCHER`
- `RevealTopCardOfLibraryEffect()` or overloads — reveal top card
- `RevealTopCardCreatureToBattlefieldElseGraveyardEffect(boolean grantHaste, boolean sacrificeAtEndStep)` — reveal top card; creature → battlefield, otherwise → graveyard (mandatory). No-arg `()` = both false (Call of the Wild `{2}{G}{G}`). `(true, true)` = entering creature gains haste and is sacrificed at the next end step (Impromptu Raid `{2}{R/G}`)
- `RevealTopCardPutLandsIntoGraveyardRepeatEffect()` — reveal the controller's library one card at a time, binning each land into the graveyard until a non-land (stays on top) or empty (Countryside Crusher, `UPKEEP_TRIGGERED`)
- `RevealTopCardRemoveTargetFromCombatIfMatchEffect(CardPredicate)` — reveal top; if match, remove the engine-set attacking creature (targetId) from combat; then bottom the card (Lost in the Woods, ON_CREATURE_ATTACKS_YOU)
- `RevealTopCardsChosenSubtypeToHandRestToBottomEffect(int count)` — reveal top `count`; creature cards of the source permanent's chosen creature type (Changeling-aware) → hand, rest → bottom in any order (async `LibraryReorder`). Reads `Permanent.getChosenSubtype()`; pair with `ChooseSubtypeOnEnterEffect` (Brass Herald, count=4)
- `RevealTopCardCreatureGainToughnessLosePowerToHandEffect()` — reveal top; if creature, gain life = toughness, lose life = power, then → hand; non-creature stays on top (Sapling of Colfenor, `ON_ATTACK`)
- `RevealTopCardsAndSeparateEffect(int)` — reveal + separate into piles
- `RevealTopCardsBottomThenDamageIfCopyRevealedEffect(int count, int damage)` — reveal top `count`, bottom them in any order (async `LibraryReorder`); if a card sharing the source's name was revealed, deal `damage` to the any-target (Stomping Slabs 7/7). Any-target chosen on cast; no damage if no copy revealed
- `ScryEffect(int)` — scry N
- `SurveilEffect(int)` — surveil N
- `ShuffleLibraryEffect(boolean targetPlayer)` — shuffle library (false=controller's, true=target player's)
- `ShuffleIntoLibraryEffect()` — shuffle spell into library
- `ShuffleSelfAndGraveyardIntoLibraryEffect()` — shuffle self + graveyard into library
- `ShuffleSelfFromGraveyardIntoLibraryEffect()` — triggered ability: shuffle the source card from its owner's graveyard into their library (pair with `ON_SELF_PUT_INTO_GRAVEYARD_FROM_ANYWHERE`, e.g. Purity)
- `ShuffleGraveyardIntoLibraryEffect(boolean targetPlayer)` — shuffle graveyard into library (targetPlayer=true targets, false=controller's)
- `ShuffleTargetCardsFromGraveyardIntoLibraryEffect(CardPredicate, int)` — shuffle N cards from graveyard
- `ShuffleCardFromControllerGraveyardIntoLibraryEffect(CardPredicate)` — "you may shuffle up to one card from your graveyard into your library"; resolution-time optional single-card choice from controller's own graveyard (non-targeted, pairs with `CounterSpellEffect`; Put Away)
- `ShuffleTargetPermanentIntoLibraryEffect()` — target permanent's owner shuffles it into their library (Deglamer; constrain to artifact/enchantment etc. via the card's target filter)
- `CastTopOfLibraryWithoutPayingManaCostEffect(Set<CardType>)` — cast top free
- `ImprovisationCapstoneEffect(int totalManaValueThreshold)` — exile from library until total MV ≥ threshold; `ImprovisationCapstoneCastChoice` interaction lets controller cast any number of exiled instants/sorceries/etc. without paying (`ImprovisationCapstoneCastSupport`)
- `BrilliantUltimatumEffect(int count)` — exile top `count` of your library; an opponent separates them into two piles (reuses the card-pile flow, `PendingPileSeparation` with `playFromExile=true`); controller picks a pile via may-ability, then a `BrilliantUltimatumPlayChoice` interaction lets them play lands (one-land-per-turn enforced) and cast spells for free from it, rest stays exiled (`BrilliantUltimatumEffectHandler` + `BrilliantUltimatumSupport`)
- `RevealTopCardMayPlayFreeOrExileEffect(boolean exileIfNotPlayed)` — reveal top, may play free; `true` = exile if not played (Djinn of Wishes), `false` = leave on top (Leaf-Crowned Elder Kinship)
- `KinshipEffect(List<CardEffect> revealEffects)` — Morningtide Kinship (`UPKEEP_TRIGGERED`): look at top card; if it shares a creature type with the source, you may reveal it, and on reveal the `revealEffects` resolve against the source (Kithkin Zephyrnaut)

## Mill

- `MillEffect(DynamicAmount, MillRecipient)` — the recipient mills cards. `recipient` ∈ {`CONTROLLER`, `TARGET_PLAYER`, `EACH_OPPONENT`}; `(int, recipient)` ctor for a fixed count. `XValue()` for mills X, `CountersOnSource(CHARGE)` for Grindclock, `CardsInHand(TARGET_PLAYER)` for Dreamborn Muse's hand-size mill. "Each player mills N" = `(N, CONTROLLER)` + `(N, EACH_OPPONENT)`. Flashback "twice X" via `ConditionalReplacementEffect(CastFromZone(GRAVEYARD), Mill(XValue(),TARGET_PLAYER), Mill(Scaled(XValue(),2),TARGET_PLAYER))` (Increasing Confusion)
- `MillControllerAndMayPlayFromGraveyardThisTurnEffect()` — mill 1, grant play-from-graveyard permission until end of turn
- `PlayAdditionalLandsEffect(int count)` — grant controller `count` extra land plays this turn (Summer Bloom)
- `EachPlayerPlaysAdditionalLandEffect()` — STATIC; standing +1 land play for every player while on the battlefield (Storm Cauldron)
- `ReturnTappedLandToHandEffect()` — ON_ANY_PLAYER_TAPS_LAND; bounces any tapped land to its owner's hand, mana kept (Storm Cauldron)
- `MillHalfLibraryEffect()` — mill half (target player)
- `RevealTopCardsMillTargetByColorSymbolsEffect(int count, ManaColor color)` — chroma mill: reveal top `count` cards, target player mills 1 per `color` mana symbol among them (hybrid/Phyrexian of that color count once), then revealed cards bottomed in any order. Targets a player; pair with `target(PlayerPredicateTargetFilter(OPPONENT))`. Sanity Grinding `(10, BLUE)`
- `NameCardMillTargetGainLifeEffect()` — controller names a card, target player mills 1; if the milled card matches the name, controller gains life = its mana value (Lammastide Weave; targets a player)
- `TargetPlayerNameCardRevealTopEffect(damageOnMiss)` — target player names a card, then reveals their top library card; match → their hand, mismatch → their graveyard + source deals `damageOnMiss` damage to them (`0` = no damage) (Vexing Arcanix with `2`; targets a player)
- `DestroyTargetWallDealManaValueDamageToControllerEffect()` — destroy target Wall (can't be regenerated), then deal its mana value as damage to its controller (snapshotted pre-destruction). `targetSpec()` narrows to a WALL permanent (Word of Blasting)

## Exile

- `ExileTargetPermanentEffect()` or `(boolean returnEndStep)` — exile target
- `FlickerEffect.exileTargetReturnAtEndStep([boolean tapped])` — exile target + return at end step (SELF: `exileSelfReturnAtEndStep()`; mass: `exilePlayersPermanentsReturnAtStep(PermanentPredicate, TurnStep)`; immediate: `flickerTarget()` / `flickerTargetWithCounters(int)` / `flickerTargetWithBonus(CardSubtype, CardEffect)`)
- `ExileGraveyardCardsEffect(GraveyardExileScope.TARGET_PLAYER_ENTIRE)` — exile target player's whole graveyard (also: `OWN`, `TARGET_CARDS_ANY_GRAVEYARD` [+`CardTypePredicate`], `TARGET_CARDS_OPPONENT_GRAVEYARD`, `ALL_PLAYERS`, `ALL_OPPONENTS`)
- `ExileAllCreaturesEffect()` — exile all creatures
- `ExileAllPermanentsEffect(PermanentPredicate)` — exile matching permanents
- `ExileAllPermanentsUntilSourceLeavesEffect(PermanentPredicate, boolean returnTapped)` — O-ring style mass exile: exile all matching permanents until the source leaves, then return each under its owner's control (tapped iff `returnTapped`). Realm Razer = `(new PermanentIsLandPredicate(), true)`
- `PutAllPermanentsOnBottomOfLibraryEffect(PermanentPredicate)` — put all matching permanents on the bottom of their owners' libraries (Hallowed Burial, `new PermanentIsCreaturePredicate()`)
- `PermanentAuctionEffect()` — SPELL: exile all nontoken permanents, then players take turns (controller first) claiming one exiled card each onto the battlefield tapped until the pool empties (Thieves' Auction)
- `IllicitAuctionEffect()` — SPELL (targets a creature): each player may bid life for control of target creature; controller opens at 0, players top the high bid in turn order, high bidder loses that much life (a life loss — can exceed their life total) and gains control indefinitely (Illicit Auction)
- `ExileTargetPermanentAndTrackWithSourceEffect()` — exile + track exiled card with source permanent (cards "exiled with" it)
- `ExileTopCardsToSourceEffect(int)` / `EachPlayerExilesTopCardsToSourceEffect(int)` / `ExileTopCardsOfOpponentLibraryToSourceEffect(int)` — exile top N of a library face down, tracked with source (controller / each player / target opponent — the last is Grimoire Thief, two-player resolves against the single opponent)
- `ExileTopCardsOfTargetOpponentCreateTokenPerChosenColorEffect(DynamicAmount count, CreateTokenEffect tokenTemplate)` — player-targeting spec; on resolution the controller chooses a colour (`ChoiceContext.ExileTopCardsChosenColorTokensChoice`), the target opponent exiles the top `count` cards of their library, and the controller creates one `tokenTemplate` token per exiled card of that colour (lands excluded, per printed colours). Use `new XValue()` for an `{X}...` cost (Oona, Queen of the Fae)
- `CreateTokenPerPermanentOfChosenColorEffect(CreateTokenEffect tokenTemplate)` — on resolution the controller chooses a colour (`ChoiceContext.CreateTokensPerPermanentOfChosenColorChoice`), then creates one `tokenTemplate` token per permanent of that colour on the battlefield (any controller, lands excluded per effective colours). No target; template `amount` ignored. Wrap in `MayPayManaEffect` for a "you may pay" gate (Rith, the Awakener)
- `SearchLibraryForCardsToExileWithSourceEffect(CardPredicate filter)` — ON_ENTER_BATTLEFIELD: search library for any number of matching cards, exile each tracked with the source, then shuffle (Endless Horizons, PLAINS). Pair with `PutCardExiledWithSourceIntoHandEffect`
- `PutCardExiledWithSourceIntoHandEffect()` — put one card the controller owns exiled with the source into hand (chooses if several); wrap in `MayEffect`. Endless Horizons upkeep
- `ReturnAllCardsExiledWithSourceEffect()` — ON_DEATH trigger: return all cards exiled with the source to the battlefield under owners' control (Helvault)
- `ReturnEnchantedCreatureToOwnerHandOnDeathEffect()` — aura `ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD` trigger: when the enchanted creature dies, return it to its owner's hand (Demonic Vigor)
- `ReturnEnchantedCreatureToBattlefieldUnderOwnersControlOnDeathEffect()` — aura `ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD` trigger: when the enchanted creature dies, return it to the battlefield under its owner's control (Abduction)
- `ReturnTargetCardFromExileToHandEffect(CardPredicate, boolean ownedOnly)` — exile to hand

## Tokens

- `CreateTokenEffect(...)` — create tokens (many constructors, see EFFECTS_INDEX.md). The count is a `DynamicAmount` (`int` ctors are `Fixed` sugar): any "create a token for each …" or "create X tokens" = this effect + an amount (`XValue`, `PermanentCount`, `CardsInGraveyard`, `CountersOnSource`, `AttachmentsOnSource`, `OpponentPoisonCounters`, `CreatureDeathsThisTurn`, `ColorManaSymbolsAmongControlledPermanents`, `Divided`, …) — never a new effect class
- `CreateTokenEffect.whiteSpirit(int)` — 1/1 white Spirit creature token with flying
- `CreateTokenEffect.blackZombie(int)` — 2/2 black Zombie creature token
- `CreateTokenEffect.whiteSoldier(int)` — 1/1 white Soldier creature token
- `CreateTokenEffect.ofTreasureToken(int)` — treasure tokens
- `PayXManaCreateXTokensEffect(CreateTokenEffect token)` — resolution-time "you may pay {X}. If you do, create X [tokens]": prompts X (≤ available mana), pays it, creates X copies of `token` (X=0 = decline). `token`'s own amount is ignored. Rise of the Hobgoblins. Use this, NOT `MayPayManaEffect("{X}", …)`, which can't pay/plumb `{X}` at resolution
- `PayXManaDealXDamageToAnyTargetEffect(String manaCost)` — "you may pay `manaCost` (contains {X}). If you do, it deals X damage to any target". Target chosen when the ability goes on the stack (harmful ANY_TARGET spec → Attack pipeline); on resolution prompts X (≤ `ManaCost.calculateMaxX`, reserving the colored part), pays `manaCost`, deals X damage (X=0 = decline). Flameblast Dragon (`"{X}{R}"`, ON_ATTACK). Use this, NOT `MayPayManaEffect("{X}…", …)`
- `CreateTokenWithDyingSourceCountersEffect(CreateTokenEffect template)` — `ON_DEATH`: if the dying creature had ≥1 +1/+1 counter, create `template` with that many +1/+1 counters (e.g. Ambitious Augmenter's Fractal)
- `CreateTokensForEachDyingSourceCounterEffect(CreateTokenEffect template)` — `ON_DEATH`: "create one `template` token for each counter on it." The death collector snapshots the dying creature's total counter count (every concrete counter type) and creates that many copies of `template` (e.g. Kinsbaile Borderguard's 1/1 white Kithkin Soldier)
- `MoveDyingSourceCountersToTargetCreatureEffect()` — `ON_DEATH`: if the dying creature had ≥1 counter (any type), move all of its counters onto up to one target creature (e.g. Scolding Administrator). Intervening-if snapshots the counters at death; targets any creature
- `DrawCardForEachDyingSourceCounterEffect(CounterType counterType)` — `ON_DEATH`: "draw a card for each `counterType` counter on it." Snapshots the dying creature's count of that type at death and draws that many (e.g. Dusk Urchins, MINUS_ONE_MINUS_ONE)
- `PutCounterOnTargetForEachDyingSourceCounterEffect(CounterType counterType)` — `ON_DEATH`: "put a `counterType` counter on target creature for each `counterType` counter on it." Snapshots the dying creature's count at death and puts that many on a targeted creature (mandatory creature target); e.g. Grief Tyrant (MINUS_ONE_MINUS_ONE)
- `MoveCounterFromTargetCreatureToTargetCreatureEffect(boolean moveAll)` — move counters from the first target creature onto the second (reads flat multi-target positions 0/1). `moveAll=false` (also `()`) moves one counter of the first kind present; `moveAll=true` moves every counter of every kind. No-op if the first creature has no counters or either target is gone. As an activated ability pair with the multi-target `ActivatedAbility` constructor + two creature filters (Leech Bonder `{U}, {Q}`); as a spell use two `target(creatureFilter)` groups (Fate Transfer, `moveAll=true`)
- For "create a token that gains [keyword] until end of turn", set `CreateTokenEffect`'s `grantedKeywordsUntilEndOfTurn` (e.g. `new CreateTokenEffect(amount, name, p, t, color, colors, subtypes, innateKeywords, Set.of(Keyword.HASTE))` — Artistic Process Elemental gains haste). Distinct from the token's innate `keywords`.
- `CreateXTokenWithXCountersEffect(String tokenName, int power, int toughness, CardColor color, Set<CardColor> colors, List<CardSubtype> subtypes, CounterType counterType)` — create one token with X counters of `counterType` from ability/spell X value (e.g. Berta's Fractal with `PLUS_ONE_PLUS_ONE`)
- `ExileTargetCardFromGraveyardAndCreateTokenCopyEffect(CardPredicate, ownGraveyardOnly, additionalSubtypes, grantHaste, exileAtEndStep)` — exile graveyard target, create token copy with optional extra subtypes/haste/end-step exile
- `CreateTokenCopyOfTargetPermanentEffect()` or `(grantHaste, exileAtEndStep)` or `(grantHaste, exileAtEndStep, sacrificeAtEndStep)` or `(additionalSubtypes, additionalTypes, powerOverride, toughnessOverride, Map<CounterType, Integer> initialCounters)` — create token copy of targeted permanent; optional type/subtype/P/T overrides, post-ETB counters, granted haste, and exile (Heat Shimmer) **or** sacrifice (Minion Reflector — dies-triggers fire) at next end step
- `CreateTokenCopyOfTargetCreatureForTargetPlayerEffect()` — target player creates a token copy of target creature you control (two targets: player + creature); Echocasting Symposium
- `CreateTokenCopyOfEachControlledCreatureTokenEffect()` — "For each creature token you control, create a token that's a copy of that creature" (populate-all). Snapshots your creature tokens first (new copies aren't copied), respects the token multiplier. No target (Rhys the Redeemed)
- `CreateTokenCopyAndLinkToSourceEffect()` — ON_ENTER_BATTLEFIELD, targets a nontoken creature: create a token copy of it AND forge a two-way `chosenPermanentId` bond between the source permanent and the token, attaching an `ON_SELF_LEAVES_BATTLEFIELD` `RemoveLinkedPermanentEffect(SACRIFICE)` to the token. Dance of Many
- `RemoveLinkedPermanentEffect(Mode)` (`EXILE` / `SACRIFICE`) — ON_SELF_LEAVES_BATTLEFIELD: removes the permanent linked to the source via `chosenPermanentId`. A dedicated trigger collector bakes the linked id into the effect (not the entry's `targetId`, which would fizzle against a spell target filter) and the handler clears the partner's back-reference before removal to avoid the reciprocal removal bouncing back. Dance of Many (enchantment leaves → `EXILE` token; token leaves → `SACRIFICE` enchantment)

## Life

- `GainLifeEffect(DynamicAmount[, GainLifeRecipient])` or `(int)` — gain life; dynamic derivations via `DynamicAmount` (PermanentCount, CardsInHand, CardsInGraveyard, CountersOnSource, GreatestPowerAmongControlled, XValue, Scaled, Sum, `ColorManaSymbolsInHand`, …). Chroma-from-hand "gain N life for each [color] mana symbol in cards in your hand" = `GainLifeEffect(new Scaled(new ColorManaSymbolsInHand(ManaColor.GREEN), N))` (Phosphorescent Feast, N=2; "reveal any number" modelled as the whole hand). `recipient=TARGET_CONTROLLER` gives the life to the target permanent's controller: "its controller gains life = its toughness" = `GainLifeEffect(new TargetToughness(), GainLifeRecipient.TARGET_CONTROLLER)` (Condemn). `TargetPower()` is the power analogue: "you gain life = target's power, then destroy it" = `GainLifeEffect(new TargetPower())` + `DestroyTargetPermanentEffect(false)` (Chastise)
- `TargetPlayerGainsLifeEffect(DynamicAmount|int)` — target gains life (`XValue` for "target player gains X life", Stream of Life)
- `DoubleTargetPlayerLifeEffect()` — double target life
- `SetTargetPlayerLifeToSpecificValueEffect(int)` — set life to value
- `SetEachPlayerLifeToHighestAmongPlayersEffect()` — each player's life total becomes the highest among all players (Arbiter of Knollridge)
- `SetEachPlayerLifeToCreatureCountEffect()` — each player's life total becomes the number of creatures they control (Biorhythm)
- `SetControllerLifeToAmountEffect(DynamicAmount)` — non-targeting "your life total becomes N/that number" (Form of the Dragon `new Fixed(5)`; Invincible Hymn `new CardsInLibrary(CountScope.CONTROLLER)`)
- `ExchangeTargetPlayersLifeTotalsEffect()` — two target players exchange life totals (Soul Conduit, Axis of Mortality)
- `PsychicTransferEffect()` — targets a player; if controller's and target's life totals differ by 5 or less, they exchange life totals (Psychic Transfer)
- `LoseLifeEffect(DynamicAmount amount, LoseLifeRecipient recipient, boolean controllerGainsLifeLost)` — the whole life-loss family. `recipient` = CONTROLLER / TARGET_PLAYER / EACH_PLAYER / EACH_OPPONENT; `controllerGainsLifeLost` drains total life lost back to you. Sugar: `(int)` = `(Fixed, CONTROLLER, false)` (lose N life), `(int, recipient)`, `(DynamicAmount, recipient)`, `(int, recipient, boolean)`. Amount: `EventValue()` for "equal to the life you gained" (Sanguine Bond `(new EventValue(), TARGET_PLAYER)`); `PermanentCount(filter, CONTROLLER)` for "1 life for each … you control" (Bishop); `new XValue()` for Exsanguinate `(new XValue(), EACH_OPPONENT, true)`. the spec targets a player iff `recipient == TARGET_PLAYER`
- `TargetPlayerLosesLifeAndControllerGainsLifeEffect(int, int)` — drain target (fixed gain, NOT gains-life-lost)
- `SpellCastLifeDrainEffect(int lifeLoss, int lifeGain, CardPredicate spellFilter)` — `ON_OPPONENT_CASTS_SPELL` drain: opponent who casts a matching spell loses `lifeLoss`, you gain `lifeGain` (filter null = any). Yawgmoth's Edict
- `LoseLifeUnlessPaysEffect(int lifeLoss, int payAmount, CardPredicate spellFilter)` — `ON_OPPONENT_CASTS_SPELL` punisher: casting opponent loses `lifeLoss` life unless they pay `{payAmount}` (filter null = any). Can't-pay auto-loses; else may-ability prompt. Isolation Cell (2, 2, CREATURE)
- `DamageUnlessPaysEffect(int damage, int payAmount, CardPredicate spellFilter)` — `ON_OPPONENT_CASTS_SPELL` punisher, **damage** version of `LoseLifeUnlessPaysEffect` (goes through the damage system: preventable, infect, redirect). `DamageDealingEffect`. Can't-pay auto-deals; else may-ability prompt. Soul Barrier (2, 2, CREATURE)
- `PlayersCantGainLifeEffect()` — can't gain life (static)
- `TargetPlayerCantGainLifeRestOfGameEffect()` — the stack entry's target player can't gain life for the rest of the game (persistent, per-player). Non-targeting on `ON_DAMAGE_TO_PLAYER`; Stigma Lasher
- `AllDamageDealtWithWitherEffect()` — STATIC global: all damage is dealt as though its source had wither (creature damage becomes -1/-1 counters; player damage normal). Everlasting Torment
- `DoubleLifeGainEffect()` — STATIC: controller's life gain is doubled (Boon Reflection). Applied in `LifeSupport.applyGainLife`; multiple copies stack multiplicatively (2^count)

## Poison counters

- `GivePoisonCountersEffect(int, PoisonRecipient)` — give poison; recipient routes CONTROLLER (self) / TARGET_PLAYER / EACH_PLAYER / ENCHANTED_PERMANENT_CONTROLLER. On an `ON_COMBAT_DAMAGE_TO_PLAYER` trigger, `TARGET_PLAYER` binds to the damaged player (Pit Scorpion: "whenever this deals damage to a player, that player gets a poison counter")
- `GivePoisonCountersEffect(int, TARGET_PLAYER, CardPredicate spellFilter)` — `ON_CONTROLLER_CASTS_SPELL` trigger descriptor (Hand of the Praetors)

## Creature pump / boost

- `BoostTargetCreatureEffect(DynamicAmount power, DynamicAmount toughness)` or `(int, int)` — target +X/+Y. Any "for each …", "+X/+X" (X paid), or "where X is …" target-pump = this effect + a `model/amount/DynamicAmount` — never a new per-variant class. The amount evaluates against the SOURCE, so counting refers to the effect's controller, not the pumped target. E.g. `(new XValue(), new XValue())` (Untamed Might), `(new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER), same)` (Elder of Laurels), `(new Sum(new Fixed(1), new CardsInGraveyard(filter, CountScope.CONTROLLER)), new Fixed(0))` (Ancestral Anger)
- `BuffTargetCreatureIndefinitelyEffect(int power, int toughness, Set<Keyword> keywords)` or `(int, int)` — target creature gets +power/+toughness and gains `keywords` **indefinitely** (no duration, CR 611.2b). Use for "this effect lasts indefinitely" pumps (Riding the Dilu Horse); NOT for until-EOT pumps (use `BoostTargetCreatureEffect` + `GrantKeywordEffect`). Recorded as a `PERMANENT` floating continuous effect on the target — +P/+T in sublayer 7c, keywords in layer 6 (read off the float by `GameQueryService.assembleStaticBonus`); copies stack additively
- `BoostTargetCreatureWhileSourceTappedEffect(int power, int toughness)` — target creature gets +power/+toughness **for as long as the source permanent remains tapped** (CR 611.2b; Tawnos's Weaponry). Records a `WHILE_SOURCE_TAPPED` floating `BuffTargetCreatureIndefinitelyEffect` keyed to the source, read in sublayer 7c; expired when the source untaps (`CreatureControlService.onSourceUntapped` → `GameData.expireTappedSourceFloatingEffects`) or leaves. Pair with `MayNotUntapDuringUntapStepEffect` static + a `{T}` cost so the controller can keep the source tapped
- `CardNamedPredicate(String cardName)` — card filter for exact name match (use with graveyard-count boosts above)
- `BoostSelfEffect(DynamicAmount, DynamicAmount)` or `(int, int)` — self +X/+Y; one-shot in trigger/ability slots, continuous in STATIC. Any "for each …" self-boost = this effect + a `model/amount/DynamicAmount` (`PermanentCount`, `CardsInGraveyard`, `AttachmentsOnSource`, `CreaturesBlockingSource`, `OpponentPoisonCounters`, `ImprintedCreaturePower/Toughness`, `LandsMatchingImprintedName`, `ChosenPermanentPower`, `XValue`, `Scaled`, `Fixed`) — never a new per-variant effect class. `ChosenPermanentPower` = effective power (at resolution) of the permanent chosen during activation, e.g. the creature tapped by `TapCreatureCost(…, trackTappedCreaturePower=true)` — Impelled Giant's "+X/+0 where X is the power of the creature tapped this way"
- `AttachedBoostEffect(DynamicAmount, DynamicAmount, GrantScope)` — STATIC +X/+Y on the enchanted/equipped creature (`ENCHANTED_CREATURE`/`EQUIPPED_CREATURE`). Attached-scope sibling of `BoostSelfEffect`; any "for each …" aura/equipment boost = this effect + a `DynamicAmount`. `CountScope.CONTROLLER` = the aura/equipment's controller (CR 109.5). Negative per-count = wrap in `Scaled(…, -1)`. Blanchwood Armor, Blackblade Reforged, Bonehoard, Runechanter's Pike, Quag Sickness, Strata Scythe — never a new `BoostCreaturePer*` class
- `BoostSelfWhenCombatOpponentMatchesEffect(PermanentPredicate opponentFilter, int power, int toughness)` — "Whenever this creature blocks or becomes blocked by one or more [filter] creatures, it gets +X/+Y until end of turn." Place in ON_BLOCK and/or ON_BECOMES_BLOCKED (plain, non-PER_BLOCKER). Non-targeting: the trigger always goes on the stack; the handler applies the boost only if a combat opponent (a creature this blocks, or a creature blocking it) matches the filter, and fires once regardless of how many match. Dwarven Soldier (`PermanentHasSubtypePredicate(ORC)`, +0/+2). The opponent-filtered, self-boost analog of Deathgazer's `DestroyCombatOpponentAtEndOfCombatEffect`
- `DoubleSelfPowerToughnessEffect()` — double self P/T
- `BoostAllOwnCreaturesEffect(DynamicAmount, DynamicAmount)` or `(…, PermanentPredicate)` — all own +X/+Y; `(int, int[, PermanentPredicate])` convenience wraps in `Fixed`. Any "where X is …" / power- or graveyard-derived mass own-pump = this effect + a `DynamicAmount` (evaluated once at resolution) — e.g. `new GreatestPowerAmongControlled()` (Overwhelming Stampede), `new CardsInGraveyard(new CardTypePredicate(CREATURE), CONTROLLER)` (Garruk, the Veil-Cursed). Never a new per-variant class
- `BoostAllCreaturesEffect(DynamicAmount, DynamicAmount)` or `(…, PermanentPredicate)` or `(…, PermanentPredicate, EachPermanentScope)` — creatures +X/+Y; `(int, int[, PermanentPredicate])` / `(int, int, EachPermanentScope)` convenience wraps in `Fixed`. Scope `ALL_PLAYERS` (default, both sides) or `TARGET_PLAYER` ("creatures target player controls", player-targeting spec, Shields of Velis Vel). "X paid" mass pump = `new Scaled(new XValue(), mult)` / `new XValue()` (Ichor Explosion, Flowstone Slide)
- `StaticBoostEffect(int, int, Set<Keyword>, GrantScope, PermanentPredicate)` — static +X/+Y + keywords
- `BoostOwnCreaturesByManaSymbolEffect(ManaColor, int powerPerSymbol, int toughnessPerSymbol)` — chroma anthem: each creature you control gets +P/+T per mana symbol of that color in its own cost (hybrid/Phyrexian symbols of that color count). Light from Within
- `SetBasePowerToughnessEffect(int, int)` — set target creature's base P/T until end of turn; `(int, int, GrantScope)` for continuous static (e.g. `ENCHANTED_CREATURE`, Deep Freeze)
- `SetAllOwnCreaturesBasePowerToughnessEffect(DynamicAmount, DynamicAmount)` or `(int, int)` — set base P/T of all creatures you control to X/X until end of turn (layer 7b, modifiers apply on top). X-cost ability = `new XValue()` (Mirror Entity)
- `SetAllUnblockedCreaturesBasePowerToughnessEffect(int, int)` — set base P/T of every unblocked creature (any player's) until end of turn (layer 7b). "Unblocked" = attacking + no blocker, locked in at resolution (Inkfathom Witch)
- `BecomeCreatureTypeWithBasePowerToughnessEffect(int power, int toughness, CardSubtype addedSubtype[, CardSubtype requiredSubtype])` — one-shot non-targeting SELF effect: permanently adds `addedSubtype` (into `grantedSubtypes`) and sets base P/T **indefinitely** (permanent base override, layer 7b via fresh timestamp — not until end of turn). Optional `requiredSubtype` = intervening "if" checked at resolution (source must already have that subtype, granted counts). Figure of Destiny's level-up chain. Pair permanent keyword grants (flying/first strike) as STATIC `ConditionalEffect(new SourceHasSubtype(subtype), new GrantKeywordEffect(kw, SELF))`
- `SwitchPowerToughnessEffect()` — switch P/T

## P/T setting / counters

- `SetPowerToughnessToAmountEffect(DynamicAmount power, DynamicAmount toughness)` — CDA that sets P/T on a 0/0 base (pass the same amount for both). Replaced the `PowerToughnessEqualTo*` family + `BoostSelfBySlimeCountersOnLinkedPermanentEffect`. Amounts: `PermanentCount(IsLand/IsCreature/IsArtifact/HasSubtype…, CONTROLLER)` (lands/creatures/artifacts/Swamps you control), `CardsInGraveyard(filter, CONTROLLER|ANY_PLAYER)`, `CardsInHand(CONTROLLER)` (hand size), `ControllerLifeTotal()` (life total), `CountersOnLinkedPermanent(type, id)` (linked-permanent counters), `ColorManaSymbolsInGraveyard(color, CONTROLLER)` (chroma of graveyard cards — Umbra Stalker = black), `ChosenNumberOnSource()` (the number last chosen for this permanent — Shapeshifter: power = `ChosenNumberOnSource()`, toughness = `Sum(Fixed(7), Scaled(ChosenNumberOnSource, -1))`). Compose offsets/negation with `Sum`/`Scaled`/`Fixed`. Pair "choose a number" with `ChooseNumberOnEnterEffect`/`ChooseNumberEffect`
- `PutCountersOnSourceEffect(int power, int toughness, int amount)` — counters on self
- `PutCountersOnSourceEqualToEnteringPowerEffect(int power, int toughness, boolean optional)` — ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD: put counters on self = entering creature's power; `optional` = "you may" (Hamletback Goliath)
- `PutCountersOnSourceEqualToDyingPowerEffect(int power, int toughness, boolean optional)` — ON_ANY_CREATURE_DIES: put counters on self = the dying creature's last-known effective power; `optional` = "you may" (Kresh the Bloodbraided). Death sibling of the entering-power effect
- `PutCountersOnSelfEffect(CounterType)` — one counter of a type on self (charge, +1/+1, study, etc.)
- `PutCountersOnSelfEffect(CounterType, int count)` — N counters of a type on self (e.g. Withengar Unbound: 13 +1/+1)
- `PutCountersOnSelfEffect(CounterType, DynamicAmount)` — dynamic count on self, e.g. `(CounterType.TOWER, new XValue())` for "{X}: Put X tower counters" (Helix Pinnacle)
- `PutCounterOnTargetPermanentEffect(CounterType, int)` — counters on target permanent (`PLUS_ONE_PLUS_ONE`/`MINUS_ONE_MINUS_ONE`/…); `(…, new XValue())` for "X counters"; `(…, count, boolean regenerateIfSurvives)` (Gore Vassal); `withTargetRestriction(…, targetPredicate)` to restrict legal targets; `(…, count, PermanentPredicate)` for a non-targeting own-permanent choice
- `PutPlusOnePlusOneCounterOnEachCreatureTargetPlayerControlsEffect()` — +1/+1 on each creature the target player controls (bind to the player target group via `target(...).addEffect(...)`)
- `PutCounterOnEachControlledPermanentEffect(CounterType, int, PermanentPredicate)` — counters on each own permanent matching predicate (use `PermanentIsCreaturePredicate` for "each creature you control")
- `RemoveCounterFromEachControlledPermanentEffect(CounterType, int, PermanentPredicate)` — remove up to N counters from each own permanent matching predicate, clamped at zero (Heartmender's "remove a -1/-1 counter from each creature you control")
- `PutCounterOnEachMatchingPermanentEffect(CounterType, int|DynamicAmount, PermanentPredicate, EachPermanentScope)` — counters on each matching permanent across `ALL_PLAYERS`/`TARGET_PLAYER` (each attacking / other / all creatures; each creature target player controls)
- `PutCounterOnEnchantedCreatureEffect(CounterType)` or `(CounterType, int)` — counter(s) on enchanted creature
- `EnterWithCountersEffect(CounterType, DynamicAmount)` — "enters the battlefield with … counters" (as-enters replacement effect): fixed = `Fixed(n)`, X paid = `XValue()`, "for each …" = a counting amount (`CreatureDeathsThisTurn`, `Sum(PermanentCount(...), CardsInGraveyard(...))`, …). "If kicked" / "Raid —" variants wrap it in `ConditionalEffect(new Kicked()/new Raid(), …)`
- `DevourEffect(int multiplier)` — Devour N as-enters replacement (`ON_ENTER_BATTLEFIELD`): controller may sacrifice any number of their *other* creatures; enters with `multiplier ×` that many +1/+1 counters and records the count. Read it with the `CreaturesDevoured` amount. Tar Fiend = `new DevourEffect(2)` + `DiscardEffect(new CreaturesDevoured(), DiscardRecipient.TARGET_PLAYER)`
- `GraveyardEnterWithAdditionalCountersEffect(CardSubtype, int)` — graveyard static: while in your graveyard, creatures of that subtype you control enter with N extra +1/+1 counters (Dearly Departed / HUMAN)
- `ControlledCreaturesEnterWithAdditionalCountersEffect(CardSubtype, int)` — battlefield static: while on the battlefield, each other creature of that subtype you control enters with N extra +1/+1 counters (Sage of Fables / WIZARD)
- Increment keyword — keyword-driven (`Keyword.INCREMENT`, auto-loaded from Scryfall): +1/+1 counter on self when mana spent on a cast spell exceeds self's current power or toughness. Add nothing to the card; behavior lives in `TriggerCollectionService.collectIncrementTriggers` (resolution effect: `IncrementTriggerEffect`). E.g. Ambitious Augmenter
- `ProliferateEffect()` — proliferate
- `KickerEffect(String cost)` — kicker declaration

## Keywords / abilities

- `GrantKeywordEffect(Keyword, GrantScope)` or `(Keyword, GrantScope, PermanentPredicate)` or `(Set<Keyword>, GrantScope)` — grant keywords. Add a trailing `GrantDuration` (`(Keyword, GrantScope, GrantDuration)` / `(Set<Keyword>, GrantScope, GrantDuration)`) for one-shot duration: `END_OF_TURN` (default) or `UNTIL_YOUR_NEXT_TURN`. In `STATIC` slot the grant is continuous and the duration is ignored. `GrantKeywordEffect.toTargetIf(Keyword, PermanentPredicate grantCondition)` — grant to target ONLY if it matches `grantCondition` (checked at resolution; target stays legal, only the grant is conditional — Vampire's Zeal, Blessing of Belzenlok). `grantCondition` is distinct from `filter`/the spec predicate.
- `RemoveKeywordEffect(Keyword, GrantScope)` or `(Keyword, GrantScope, PermanentPredicate filter)` — one-shot keyword removal until end of turn (floating layer-6 removal). Scopes handled by the normal handler: `SELF`, `TARGET`, and `OPPONENT_CREATURES` (mass removal from every creature opponents control — Invert the Skies strips flying). In `STATIC` slot it's a continuous removal via the static handler's creature-scope matcher.
- `GrantChosenKeywordToTargetEffect(List<Keyword> options)` — prompt to choose one keyword from options, grant to target permanent until end of turn (bind to its target group in multi-target spells — Practiced Offense)
- `GrantChosenKeywordToSelfEffect(List<Keyword> options)` — non-targeted SELF variant: prompt to choose one keyword from options, grant it to the source permanent (this creature) until end of turn (Urza's Avenger)
- `GrantFlashToCardTypeEffect(CardPredicate)` — flash to card types (static)
- `GrantConspireToSpellsEffect(CardPredicate)` — spells you cast matching the predicate have conspire (static; Wort, the Raidmother)
- `ActivateCreatureAbilitiesAsThoughHasteEffect()` — controller may activate abilities of creatures they control as though they had haste (static; lifts summoning-sickness on ability activation only, does NOT grant haste — Thousand-Year Elixir)
- `GrantActivatedAbilityEffect(ActivatedAbility, GrantScope)` or `(ActivatedAbility, GrantScope, PermanentPredicate)` — grant ability
- `GrantAdditionalBlockEffect(int)` or `(int, PermanentPredicate controlledFilter)` — block N additional (filter → each controlled permanent matching predicate, e.g. Cenn's Tactician)
- `RegenerateEffect()` or `(boolean targetsPermanent)` — regenerate
- `RegeneratesIfWouldBeDestroyedEffect()` — STATIC self-replacement: "if this creature would be destroyed, regenerate it" — always-on intrinsic regeneration (regenerates every time, no shield consumed); honored by `GraveyardService.tryRegenerate`. Mossbridge Troll
- `ProtectionFromColorsEffect(Set<CardColor>)` — protection from colors (static)
- `ProtectionFromSubtypesEffect(Set<CardSubtype>)` — protection from subtypes (static)
- `ProtectionFromManaValueEffect(int minManaValue)` — protection from sources with mana value ≥ N (static, Mistmeadow Skulk)
- `GrantSubtypeToTargetCreatureEffect(CardSubtype)` — target creature "becomes a [subtype] in addition to its other types" (permanent, added to `grantedSubtypes`)
- `TargetCreatureBecomesSubtypeUntilEndOfTurnEffect(CardSubtype)` — target creature **becomes** the given creature type until end of turn, **replacing** all its other creature types (Boldwyr Intimidator: "target creature becomes a Coward"). Sets `Permanent.transientCreatureTypeOverride`, read by the layered pass; contrast the additive/permanent `GrantSubtypeToTargetCreatureEffect`
- `GrantBasicLandTypeToTargetEffect(EffectDuration[, CardSubtype fixedSubtype][, boolean replacing])` — target land becomes a chosen basic land type. Default adds "in addition to its other types" (Navigator's Compass / Aquitect's Will); `replacing=true` makes the land **become** the type, losing its others per rule 305.7 (Tideshaper Mystic, UNTIL_END_OF_TURN; Orcish Farmer = fixed SWAMP, UNTIL_CONTROLLERS_NEXT_UNTAP_STEP — survives end of turn, cleared at the land controller's next untap step)
- `NonbasicLandsBecomeTypeEffect(CardSubtype)` — STATIC, global: every nonbasic land (any controller) becomes the basic land type, losing its other land types/abilities and producing that type's mana per rule 305.7. Basic lands unaffected. Blood Moon (MOUNTAIN)
- `OwnLandsBecomeChosenTypeUntilEndOfTurnEffect()` — no target: prompts the controller for a basic land type, then each land they control **becomes** that type until end of turn (type-replacing per rule 305.7, reuses `applyBasicLandType(..., replacing=true)`). Applied once at resolution to lands controlled then. Elsewhere Flask
- `LoseAllCreatureTypesEffect(GrantScope)` — creatures lose all creature types until end of turn; `TARGET` = single creature (Amoeboid Changeling), `TARGET_PLAYERS_CREATURES` = all creatures target player controls (Ego Erasure); "gains all creature types" = `GrantKeywordEffect(Keyword.CHANGELING, sameScope)`
- **Paradigm** (`Keyword.PARADIGM` on card, not an effect) — engine handled by `ParadigmService`: first resolve exiles spell + registers `GameData.ParadigmDelayedTrigger`; each precombat main fires `ParadigmCastCopyEffect` → copy in exile + `ParadigmMayCastFromExileEffect` may-cast (`ParadigmCastSupport`)

## Combat restrictions / evasion

- `CantBeBlockedEffect()` — unblockable (static)
- `CantBeBlockedByFewerThanNCreaturesEffect(int minBlockers)` — generalized menace: can't be blocked except by N+ creatures (static). Menace = 2; Guile = 3
- `CantBeBlockedByCreaturesMatchingPredicateEffect(PermanentPredicate blockerPredicate)` — can't be blocked by blockers matching the predicate (static). Taoist Mystic = `PermanentHasKeywordPredicate(HORSEMANSHIP)`
- `CantBeBlockedIfAttackingAloneEffect()` — can't be blocked while attacking alone (static)
- `AssignCombatDamageAsThoughUnblockedEffect()` — while blocked, may assign combat damage as though unblocked (Rhox/Thorn Elemental) (static)
- `AssignCombatDamageToDefendingCreatureWhenUnblockedEffect()` — while unblocked, may assign all combat damage to one defending creature instead of the player (Cunning Giant) (static). Prompts the attacker via the combat-damage-assignment interaction when the defender has a creature; single recipient only.
- `AssignNoCombatDamageAndDefendingPlayerDiscardsEffect()` — the trigger's source (attacking) creature assigns no combat damage this turn (added to `creaturesPreventedFromDealingCombatDamage`) and the defending player (trigger `targetId`) discards a card at random. Wrap in `MayEffect` for "you may". Used by Cloak of Confusion on `ON_ENCHANTED_CREATURE_ATTACKS_UNBLOCKED`.
- `CantBlockEffect()` — can't block (static)
- `CantBlockCreaturesWithPowerGreaterOrEqualToOwnToughnessEffect()` — STATIC self-referential restriction (Ironclaw Curse): this creature can't block an attacker whose effective power ≥ this creature's own effective toughness. Evaluated in `GameQueryService.findBlockDenial`; recognized on the blocker's own STATIC effects or via an aura attached to it (`hasAuraWithEffect`). Facet on `BlockingRestrictionEffect` (`cantBlockCreaturesWithPowerAtLeastOwnToughness()`). Pair with `StaticBoostEffect(0, -1, ENCHANTED_CREATURE)` for the full Aura.
- `CantBlockCreaturesWithPowerAtLeastEffect(int minAttackerPower)` — STATIC hard fixed-threshold restriction (Ironclaw Orcs, `(2)`): this creature can't block an attacker with effective power ≥ `minAttackerPower`. Facet `cantBlockCreaturesWithPowerAtLeast()` on `BlockingRestrictionEffect`; evaluated in `GameQueryService.findBlockDenial`.
- `MustAttackEffect()` — must attack (static)
- `MatchingCreaturesMustAttackEffect(PermanentPredicate matcher)` — STATIC global: every creature matching `matcher` (any controller) must attack each combat if able. Global analogue of self-only `MustAttackEffect`. Goblin Assault (`PermanentHasSubtypePredicate(GOBLIN)`)
- `MustAttackControllerNextTurnEffect()` — targets a player: during their next turn every creature they control attacks you (the controller) if able (Taunt). SPELL slot; player-targeting spec
- `MustBeBlockedIfAbleEffect()` — must be blocked (static)
- `MustBeBlockedByAllCreaturesEffect()` — Lure (static)
- `MustBeBlockedByAllCreaturesThisTurnEffect()` — one-shot targeted Lure: all creatures able to block target creature this turn do so (Alluring Scent); SPELL slot, permanent-targeting spec
- `MustBlockTargetCreatureEffect()` — two-target spell: blocker group (0) must block blocked group (1) this turn if able (Hunt Down)
- `EnchantedCreatureCantAttackOrBlockEffect()` — Pacifism (static)
- `EnchantedCreatureCanOnlyAttackAloneEffect()` — Errantry: enchanted creature can attack only if it's the sole attacker (static)
- `ExileEnchantedCreatureEffect()` — exile the creature the source Aura is attached to (exile variant of `SacrificeEnchantedCreatureEffect`); use in an Aura's activated ability (Weight of Conscience). Pairs with the `TapTwoCreaturesSharingTypeCost` activated-ability cost.
- `SacrificeEnchantedCreatureOnLeaveEffect()` — "When this Aura leaves the battlefield, that creature's controller sacrifices it." Use in `ON_SELF_LEAVES_BATTLEFIELD` on a reanimation Aura (Animate Dead); the enchanted creature's permanent id is baked in at trigger time so it resolves after the Aura is gone.
- `EnchantedControllerSacrificesCreatureOnLeaveEffect()` — "When enchanted creature leaves the battlefield, its controller sacrifices a creature of their choice." Use in `ON_ENCHANTED_PERMANENT_LEAVES_BATTLEFIELD` on an Aura (Funeral March); the leaving creature's controller (not the Aura controller) is baked in at trigger time and sacrifices at resolution.
- `MakeCreatureUnblockableEffect()` — target unblockable this turn
- `CanBeBlockedOnlyByFilterEffect(PermanentPredicate blockerPredicate, String allowedBlockersDescription)` — static evasion on the source: it can be blocked only by blockers matching the predicate (Fear-like, e.g. Dread Warlock = black creatures)
- `GrantCanBeBlockedOnlyByFilterToOwnCreaturesEffect(PermanentPredicate creatureFilter, PermanentPredicate blockerPredicate, String allowedBlockersDescription)` — SPELL one-shot: your creatures matching `creatureFilter` (null = all) can be blocked only by blockers matching `blockerPredicate` until end of turn. Dread Charge = both filters `PermanentColorInPredicate(BLACK)`. Affected creatures snapshotted at resolution; restriction stored transiently on each `Permanent`
- `MakeCreatureBlockableOnlyByFilterThisTurnEffect(PermanentPredicate blockerPredicate, String allowedBlockersDescription)` — targeted one-shot (single creature): target creature can be blocked only by blockers matching `blockerPredicate` until end of turn. The single-target counterpart of `GrantCanBeBlockedOnlyByFilterToOwnCreaturesEffect`. Joven's Tools = `PermanentHasSubtypePredicate(WALL)`, "Walls"
- `MatchingCreaturesCantBlockMatchingCreaturesEffect(PermanentPredicate blockerPredicate, PermanentPredicate attackerPredicate, String description)` — global STATIC: while the source is on the battlefield, any creature matching `blockerPredicate` can't block any creature matching `attackerPredicate`, board-wide (Boldwyr Intimidator: "Cowards can't block Warriors"). Evaluated in `GameQueryService.getBlockRestriction`
- `MatchingCreaturesCantAttackOrBlockEffect(PermanentPredicate affectedPredicate, String description)` — global STATIC: while the source is on the battlefield, any creature matching `affectedPredicate` can't attack OR block, board-wide. Predicate is evaluated relative to the source's controller (`sourceControllerId`/`sourceCardId` set), so source-relative predicates like `PermanentControlledBySourceControllerPredicate` resolve "you"/"your opponents". Kulrath Knight: "Creatures your opponents control with counters on them can't attack or block" = `PermanentAllOfPredicate(PermanentNotPredicate(PermanentControlledBySourceControllerPredicate()), PermanentHasCountersPredicate(ANY))`. Light of Day: "Black creatures can't attack or block" = `PermanentColorInPredicate(BLACK)`. Attack side in `CombatAttackService.isCantAttackDueToGlobalRestriction`, block side in `GameQueryService` (`canBlock` + `buildBlockerFacts`)
- `CantBlockThisTurnEffect(TapUntapScope scope[, PermanentPredicate filter])` — creature(s) can't block this turn (one-shot). `TARGET` (target creature, multi-target-group), `TARGET_PLAYERS_PERMANENTS` (target player's / targeted planeswalker's controller's creatures), `ALL_CREATURES` (mass, filtered). NOT the static `CantBlockEffect()`.
- `TargetPlayerChoosesCreatureRestCantBlockEffect()` — SPELL, player-targeting spec: the targeted player chooses one creature they control (kept able to block); all their OTHER creatures can't block this turn. Pair with a `PlayerPredicateTargetFilter(OPPONENT)`. 0-1 creatures ⇒ no choice, resolves harmlessly (Goblin War Cry).

## Tap / untap

- `EnchantedCreatureDealsDamageEqualToDealtDamageToControllerEffect()` — enchanted creature deals damage equal to amount dealt to its controller (ON_ENCHANTED_CREATURE_DEALT_DAMAGE)
- `ReflectSourceDamageToItsControllerEffect(CardColor.RED)` — global watcher (ON_ANY_SOURCE_DEALS_DAMAGE): whenever any red creature or spell deals damage, this permanent deals that much to that source's controller (Justice). Damage from one source is summed into one reflection (CR ruling)
- `TapPermanentsEffect(TapUntapScope.TARGET)` — tap target
- `TapPermanentsEffect(TapUntapScope.SELF)` — tap self · `.ENCHANTED` — tap aura's enchanted creature
- `TapPermanentsEffect(TapUntapScope.TARGET_PLAYERS_PERMANENTS, filter)` — tap that player's matching permanents
- `TapPermanentsEffect(TapUntapScope.ALL_CREATURES, filter)` — tap all creatures matching filter (`PermanentIsAttackingPredicate` = all attackers)
- `UntapPermanentsEffect(TapUntapScope.TARGET[, PermanentPredicate])` — untap target (predicate restricts targets)
- `UntapPermanentsEffect(TapUntapScope.SELF)` — untap self · `.ALL_TARGETS` — untap all targets
- `UntapPermanentsEffect(TapUntapScope.CONTROLLED, filter)` — untap all you control matching · `.OTHER_CONTROLLED_CREATURES` — untap each other creature you control · `.ATTACKED_CREATURES` — untap creatures that attacked this turn · `.ALL_CREATURES[, filter]` — untap every creature on every battlefield matching filter (null = all creatures); Intruder Alarm
- `UntapPermanentsEffect(TapUntapScope.TARGET_PLAYERS_PERMANENTS, filter)` — untap all of target player's permanents matching filter (Early Harvest: `PermanentAllOf(land, BASIC supertype)` = target player's basic lands)
- `UntapEquippedCreatureEffect()` — untap the source Equipment's attached creature (fizzles if unattached). Place on the Equipment in a trigger slot (e.g. `ON_ANY_CREATURE_DIES`) to model equipment-granted untap triggers; Thornbite Staff
- `MatchingPermanentsDoesntUntapEffect(PermanentPredicate)` — global static: every permanent matching the predicate (any controller, incl. the source) doesn't untap during its controller's untap step; Marble Titan (`PermanentPowerAtLeastPredicate(3)`)
- `StorageMatrixEffect()` — global static (Storage Matrix): while any permanent carrying it is untapped, each player's untap step pauses (`UntapStepService.storageMatrixRestrictionApplies`) so the active player chooses artifact/creature/land (a `ColorChoice` with `ChoiceContext.StorageMatrixUntapChoice`); only permanents of the chosen type untap that step. No card-side targeting/config — just `addEffect(EffectSlot.STATIC, new StorageMatrixEffect())`.
- `StaticOrbEffect(maxUntap, filter, requiresUntappedSource)` — global "can't untap more than N [permanents]" lock (Static Orb, Stoic Angel). While an active copy is present (`requiresUntappedSource` → only while its source is untapped), each player's untap step pauses (`UntapStepService.bindingUntapRestriction`, only when more than `maxUntap` of the matching permanents would untap) so the active player picks up to `maxUntap` of the permanents that match `filter` (`null` = all) and would otherwise untap (a `MultiPermanentChoice` with `MultiPermanentChoiceContext.StaticOrbUntap`); only those, plus any permanents the filter excludes, untap that step. Static Orb: `new StaticOrbEffect()` (cap 2, all permanents, while untapped). Stoic Angel and Smoke: `new StaticOrbEffect(1, new PermanentIsCreaturePredicate(), false)` (cap 1, creatures only, always-on). Winter Orb: `new StaticOrbEffect(1, new PermanentIsLandPredicate(), true)` (cap 1, lands only, while untapped).
- `DoesntUntapEffect.self()` — this permanent doesn't untap (static) · `.enchanted()` — attached aura/equipment's host doesn't untap (static) · `.targetWhileSourceOnBattlefield()` — target doesn't untap while source on battlefield (Dungeon Geists / Time of Ice) · `.targetWhileSourceTapped()` — while source stays tapped (Rust Tick); TARGET factories piggyback on a companion `TapPermanentsEffect(TapUntapScope.TARGET)`
- `SkipNextUntapEffect(TapUntapScope.TARGET)` — target permanent skips next untap (piggybacks on companion targeting effect) · `.SELF` — source permanent itself skips next untap, non-targeting, for self-referential triggers like `ON_ATTACK` (Lead Golem) · `.TARGET_PLAYERS_PERMANENTS, filter` — that player's matching permanents · `.ALL_CREATURES, filter` — all creatures matching filter (`PermanentIsAttackingPredicate` = all attackers)
- `IfWonClashEffect(wrapped)` — clash-only marker on `EffectSlot.ON_CONTROLLER_CLASHES`: the wrapped effect applies only if the controller won the clash ("If you won, ..."). Consumed by `TriggerCollectionService.fireClashTriggers` at trigger time (not a stack effect). See Entangling Trap: tap target opponent creature + `IfWonClashEffect(SkipNextUntapEffect(TARGET))`. Clash is performed via `performClash` (2-player: both reveal top card, strictly-higher mana value wins).
- `IfLostClashEffect(wrapped)` — mirror of `IfWonClashEffect`: applies only when the controller did **not** win. Pair a won- and lost-variant so exactly one branch fires when the base effect happens regardless of outcome but only a detail differs (Rebellion of the Flamekin: two `MayPayManaEffect("{1}", CreateTokenEffect(...))` differing only in granted `HASTE` on the won branch). Non-targeting clash triggers go straight onto the stack.
- `ClashEffect(List<CardEffect> beforeClash, CardEffect onWin, boolean repeatWhileWinning)` — the clash-*source* stack effect; convenience ctor `ClashEffect(onWin)` = `(List.of(), onWin, false)`. Each iteration dispatches `beforeClash` (via each effect's own handler, against the same entry), performs the clash for the controller via `performClash`, dispatches `onWin` on a win, and with `repeatWhileWinning` repeats the whole sequence until a lost clash (deck-out counts as a loss). "Clash with an opponent. If you win, [X]" = `ClashEffect(X)`; `onWin` may be null for a bare "clash with an opponent". Mirrors `FlipCoinWinEffect`. E.g. Oaken Brawler = `ClashEffect(new PutCountersOnSourceEffect(1, 1, 1))`. "[body], then clash with an opponent. If you win, repeat this process" = `ClashEffect(body, null, true)`: Hoarder's Greed = `ClashEffect(List.of(new LoseLifeEffect(2), new DrawCardEffect(2)), null, true)`. Wrap in `MayEffect` for "you may clash" (Sentry Oak = `MayEffect(ClashEffect(new BoostSelfAndLoseKeywordEffect(2, 0, Keyword.DEFENDER)), ...)`). Delegates its `targetSpec()` to `onWin`/`beforeClash`, so a **targeted** win reward works on any targeting slot: e.g. Springjack Knight "whenever this attacks, clash; if you win, target creature gains double strike" = `target(...)` + `ClashEffect(new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.TARGET))` on `ON_ATTACK` (target chosen when the trigger goes on the stack; grant only on a win). Do **not** wrap an interactive `MayEffect` as a `ClashEffect` win reward — the may-pause re-runs the `ClashEffect` (re-clash). For an *optional* win reward, use a bare `ClashEffect(null)` (records its result on the entry) followed by `ConditionalEffect(new WonClash(), new MayEffect(reward, prompt))`: Whirlpool Whelm = `ClashEffect(null)` + `ConditionalEffect(new WonClash(), new MayEffect(new PutTargetOnTopOfLibraryEffect(), prompt))` + `ReturnToHandEffect.target()`.

## Control / steal

- `GainControlOfTargetEffect(ControlDuration.PERMANENT[, CardSubtype])` — gain control permanently
- `GainControlOfTargetEffect(ControlDuration.END_OF_TURN)` — gain control until EOT
- `GainControlUntapAndHasteTargetEffect()` — Threaten bundle in one effect (gain control until EOT + untap + haste); use when it must be gated by a single `MayEffect` (Dominus of Fealty)
- `GainControlOfTargetEffect(ControlDuration.WHILE_SOURCE_ON_BATTLEFIELD)` — control while source on battlefield
- `GainControlOfAllLandsTargetPlayerControlsEffect()` — gain permanent control of every land the target player controls (player-targeting spec, Gilt-Leaf Archdruid)
- `GainControlOfTargetEffect(ControlDuration.WHILE_SOURCE_TAPPED)` — control while source stays on battlefield, its creator still controls it, AND it remains tapped; ends (and does not resume) the moment the source untaps (Seasinger). Untap hook lives in `TapUntapSupport.untapPermanent` / `MayMiscHandlerService.handleMayNotUntapChoice` → `CreatureControlService.onSourceUntapped`
- `GainControlOfAllLandsTargetPlayerControlsEffect()` — gain permanent control of every land the target player controls (`canTargetPlayer`, Gilt-Leaf Archdruid)
- `GainControlOfLandDefendingPlayerControlsAndAssignNoCombatDamageEffect()` — Orcish Squatters. Wrap in `MayEffect` on `ON_ATTACKS_UNBLOCKED` (`targetId`=defending player, `sourcePermanentId`=attacker). On resolve presents a max-1 choice among lands the defending player controls; taking one gains control of it `WHILE_SOURCE_ON_BATTLEFIELD` (wraps a plain `GainControlOfTargetEffect`) and — only when a land is taken — adds the source to `creaturesPreventedFromDealingCombatDamage` ("assigns no combat damage this turn"). Choice completion lives in `MultiPermanentChoiceHandlerService` (`GainControlOfLandAndAssignNoCombatDamage` context)
- `GainControlOfCombatOpponentAtEndOfCombatEffect()` — The Wretched-style "At end of combat, gain control of all creatures blocking this creature for as long as you control this creature". Put on `ON_BECOMES_BLOCKED` with `TriggerMode.PER_BLOCKER` (blockers only). Each trigger queues a `GainControlOfPermanentAtEndOfCombat` delayed action drained in `CombatService.processEndOfCombatControlGains()`; control is applied `WHILE_SOURCE_ON_BATTLEFIELD` keyed to the source
- `PlayerWithMostLifeGainsControlOfSourceCreatureEffect()` — Ghazbán Ogre: `UPKEEP_TRIGGERED`; the player with strictly the most life gains permanent control of this creature. No-op on a tie for most life (intervening-if)
- `GainControlOfEnchantedTargetEffect()` — Control Magic (static)
- `ClashForControlOfEnchantedCreatureEffect()` — Captivating Glance: `CONTROLLER_END_STEP_TRIGGERED` Aura effect; clash, then the winner (controller on win, else clash opponent) gains control of the enchanted creature
- `SacrificeEnchantedPermanentAndReattachSourceAuraEffect()` — Nettlevine Blight: `ENCHANTED_PERMANENT_CONTROLLER_END_STEP_TRIGGERED` Aura effect; the enchanted permanent's controller sacrifices it and moves this Aura (keeping its controller) onto another creature/land they control
- `AttachSourceAuraToEnteringCreatureEffect()` — Prison Term: `ON_OPPONENT_CREATURE_ENTERS_BATTLEFIELD` marker; "you may attach this Aura to that creature" moves the Aura onto the entering opponent creature (enter collector queues `MayEffect(AttachSourceAuraToTargetCreatureEffect)`)
- `GatherSpecimensEffect()` — Gather Specimens: `SPELL`, no target. Turn-scoped control-changing replacement (CR 614.1) — adds the controller to `GameData.playersGatheringSpecimensThisTurn`; a creature that would enter under any opponent's control instead enters under the gatherer's control. Applied via `BattlefieldEntryService.resolveEnteringController` (universal placement) + `StackResolutionService` creature/artifact spell paths (ETB/log/legend attribution). Cleared at turn cleanup
- `JuxtaposeEffect()` — Juxtapose: `SPELL`, self-targets a single player (player-targeting spec). Controller and target player exchange control of their greatest-mana-value creature, then artifact (creatures first, then artifacts on the updated board; artifact creatures can move twice). Ties prompt the controlling player (`JuxtaposeSupport`, `PermanentChoiceContext.JuxtaposeTieBreak`); missing type on a side skips that exchange. Permanent swap via two `GainControlOfTargetEffect(PERMANENT)` floating effects
- `JuxtaposeEffect()` — Juxtapose: `SPELL`, self-targets a single player (`canTargetPlayer()`). Controller and target player exchange control of their greatest-mana-value creature, then artifact (creatures first, then artifacts on the updated board; artifact creatures can move twice). Ties prompt the controlling player (`JuxtaposeSupport`, `PermanentChoiceContext.JuxtaposeTieBreak`); missing type on a side skips that exchange. Permanent swap via two `GainControlOfTargetEffect(PERMANENT)` floating effects
- `ExchangeControlOfSharedTypeTargetsAndDestroyAurasEffect()` — Gauntlets of Chaos: multi-target activated ability (`{5}`, `SacrificeSelfCost`). Reads two `targetIds` — `[0]` an artifact/creature/land you control, `[1]` a permanent an opponent controls sharing one of those types. The "shares one of those types" cross-target restriction is enforced at announcement via `ActivatedAbility.withMultiTargetConstraint(MultiTargetConstraint.SHARE_ARTIFACT_CREATURE_OR_LAND_TYPE)` (per-position filter [1] is just "artifact/creature/land an opponent controls"). At resolution re-checks legality (CR 701.10), swaps controllers permanently (two `GainControlOfTargetEffect(PERMANENT)` floating effects), then destroys every Aura attached to either permanent.
- `ExchangeControlOfTargetPermanentsEffect()` — Puca's Mischief: `UPKEEP_TRIGGERED`, wrap in `MayEffect`. Reads two `targetIds` — `[0]` a nonland permanent you control, `[1]` a nonland permanent an opponent controls with mana value ≤ target [0]. The two interdependent targets are chosen at trigger time via a bespoke two-step permanent choice (`StepTriggerService.processNextPucasMischiefTarget` → `PucasMischiefOwnTarget`/`PucasMischiefOpponentTarget`, mirroring Capricious Efreet); the "you may" resolves like Axis of Mortality. Re-checks legality at resolution and swaps controllers permanently (CR 701.10)

## Mana

- `AwardManaEffect(ManaColor, DynamicAmount)`, `(ManaColor, int)`, or `(ManaColor)` — add mana; dynamic quantity: `PermanentCount(filter, CONTROLLER)` for "for each X you control", `CountersOnSource(CHARGE)` for "per charge counter", `SourcePower()` for "equal to its power", `FixedIfControlsAllNamed(List<String> names, amount, otherwise)` for the Urza-land ("Tron") boost — `amount` if you control a permanent of every named card, else `otherwise` (e.g. Urza's Mine `AwardManaEffect(COLORLESS, new FixedIfControlsAllNamed(List.of("Urza's Power-Plant", "Urza's Tower"), 2, 1))`)
- `AwardAnyColorManaEffect(int)` or `()` — add any color mana
- `AwardXAnyColorManaEffect()` — add X mana of one chosen color, where X is the ability's xValue (e.g. permanents sacrificed via `SacrificeXPermanentsCost`). X-scaled sibling of `AwardAnyColorManaEffect`; pair with `GainLifeEffect(new XValue())` for "…you gain X life" (Springjack Pasture)
- `AwardManaOfColorsEffect(List<ManaColor>)` or `(List<ManaColor>, int amount)` — add `amount` mana (default 1), each chosen individually from a **fixed list** (single-color list auto-adds, no prompt). Dual/tri producers like Manaforge Cinder (`List.of(BLACK, RED)` = "Add {B} or {R}"). With `amount > 1` each mana's color is picked separately from the same list, re-prompting per pick — filter lands: Fire-Lit Thicket `(List.of(RED, GREEN), 2)` = "Add {R}{R}, {R}{G}, or {G}{G}"
- `AwardOneManaOfEachColorAmongControlledEffect(PermanentPredicate)` — "For each color among permanents you control, add one mana of that color." Adds one mana of **every** color found at once (no choice; contrast `AwardManaOfColorsAmongControlledEffect` which picks one). Bloom Tender = `PermanentTruePredicate`
- `AwardManaOfColorsLandsCouldProduceEffect(ManaColorLandScope, PermanentPredicate)` — add one mana of any color a land in scope matching the predicate could produce. `OPPONENTS` + `PermanentIsLandPredicate` = Fellwar Stone ("a land an opponent controls"); `CONTROLLER` + basic-land predicate = Star Compass ("a basic land you control")
- `MayTapLandsYouDontControlForSpellsUntilEndOfTurnEffect()` — SPELL slot; until EOT, controller may tap lands they don't control for spell-only mana via `GameService.tapForeignLandForMana(...)` (Piracy)
- `RemoveCountersForManaEffect(ManaColor, CounterType)` — mana ability for the storage-land cycle: "Remove any number of [type] counters from this permanent: Add [color] for each counter removed this way." At resolution it prompts the controller for how many counters (0..the count present) to remove via a `RemoveCountersForManaChoice`, then removes them and adds that much mana (the counter removal is the cost but is modelled together with the mana). Implements `ManaProducingEffect`. Pair with `EntersTappedEffect` + `MayNotUntapDuringUntapStepEffect` + an `UPKEEP_TRIGGERED ConditionalEffect(new SourceIsTapped(), new PutCountersOnSelfEffect(CounterType.STORAGE))`. Bottomless Vault uses `(ManaColor.BLACK, CounterType.STORAGE)`
- `DoubleManaPoolEffect()` — double mana pool
- `ManaReflectionEffect()` — STATIC: tapping a permanent for mana produces twice as much of that mana (Mana Reflection). Applied in the mana-ability resolution via `GameQueryService.manaProductionMultiplier`; multiple stack multiplicatively (2^count)
- `TargetPlayerLosesAllUnspentManaEffect()` — targeted player empties their mana pool (all buckets incl. persistent); pair with `PlayerPredicateTargetFilter` (Mana Short)
- `DrainTargetPlayersLandManaEffect()` — targeted player taps a mana ability of each untapped land they control (mana into their own pool), then loses all unspent mana and the controller adds an equal amount; pair with `PlayerPredicateTargetFilter` (Drain Power). Fixed single-color lands exact; any-color producers contribute colorless
- `AwardRestrictedManaEffect(ManaColor, int, ManaRestriction)` — restricted mana (`ManaRestriction`: `SpellTypes(Set<CardType>)`, `ArtifactSpells()`, `SubtypeSpells(CardSubtype)`, `KickedCosts()`, `XCosts()`). `XCosts()` = colorless mana spendable only on spells/abilities whose cost contains {X} (Rosheen Meanderer); routes to the `xCostOnlyColorless` pool bucket, usable for any generic portion of an {X} cost
- `AwardFlashbackOnlyAnyColorManaEffect(int)` — flashback-only mana (any-color choice; separate record)
- `AwardAnyColorChosenSubtypeCreatureManaEffect()` — one mana of any color, spendable only on creature spells of the source's chosen subtype (Pillar of Origins / Unclaimed Territory; spell-only)
- `AwardAnyColorSubtypeSpellOrAbilityManaEffect(int, CardSubtype)` — N mana in any combination of colors, spendable only to cast spells of the subtype **or** activate abilities of permanents of that subtype (Smokebraider = `(2, ELEMENTAL)`)

## Copy / clone

- `CopyPermanentOnEnterEffect(PermanentPredicate, String)` + overloads — Clone-style
- `MakeTargetCopyOfTargetCreatureUntilNextTurnEffect()` — **two targets**: target Shapeshifter (`targetIds[0]`) becomes a copy of target creature (`targetIds[1]`) until the controller's next turn (Shapesharer). Wire via the multi-target `ActivatedAbility` ctor
- `BecomeCopyOfTargetCreatureUntilEndOfTurnEffect()` — source permanent becomes a copy of target creature until end of turn (Tilonalli's Skinshifter); `BecomeCopyOfTargetCreatureEffect()` — same, retaining the granting ability (Cryptoplasm)
- `BecomeCopyOfDyingCreatureEffect()` — source permanent becomes a copy of a creature that just died (last-known info from the graveyard), retaining its own death-copy trigger ("except it has this ability"). Cemetery Puca. Place in `ON_ANY_CREATURE_DIES` wrapped in `MayPayManaEffect` for the "you may pay {1}" gate
- `EachOtherCreatureBecomesCopyOfTargetCreatureUntilEndOfTurnEffect()` — every creature on the battlefield **except** the target becomes a copy of the target creature until end of turn (Mirrorweave). Single target; pair with a nonlegendary-creature `PermanentPredicateTargetFilter`. Each copy reverts at cleanup via a per-permanent `BecomeCopyOfTargetCreatureUntilEndOfTurnEffect` floating effect
- `CopySpellEffect()` or `(StackEntryPredicate)` — copy target spell; for "copy twice if cast from a graveyard" add `ConditionalEffect(new CastFromZone(Zone.GRAVEYARD), new CopySpellEffect())` (Increasing Vengeance). Full form `(StackEntryPredicate spellFilter, boolean tokenWithHaste, boolean sacrificeAtEndStep)`: for "copy target **creature** spell; the copy gains haste and is sacrificed at the beginning of the end step", use `new CopySpellEffect(null, true, true)` — the copy becomes a token, gains `HASTE`, and its permanent is registered in `GameData.delayedActions` (a `SacrificeAtEndStep`) (drained by `StepTriggerService.handleEndStepTriggers` via `removePermanentToGraveyard`). `tokenWithHaste` also suppresses the "choose new targets" retarget prompt. Filter which spells are targetable via the mode's `target(...)`/`ChooseOneOption` filter, not `spellFilter`. To make a spell uncopyable, set `card.setCantBeCopied(true)` — honored by every copy handler. See Choreographed Sparks.
- `CopyThisSpellIfConditionEffect(Condition)` — "When you cast this spell, copy it if <condition>. You may choose new targets for the copy." Place in the `ON_SELF_CAST` slot (the spell's own cast trigger); the copy is created with an optional choose-new-targets prompt only when the condition holds at resolution. Used by the SOS Infusion copy cycle (e.g. Lumaret's Favor with `new GainedLifeThisTurn()`)
- `CopyControllerCastSpellOnSpellCastEffect(CardPredicate, TapMultiplePermanentsCost)` — ON_CONTROLLER_CASTS_SPELL: copy cast instant/sorcery; optional tap cost wraps `MayPayTapPermanentsEffect` + `CopyControllerCastSpellEffect` (Aziza, Mage Tower Captain)
- `StormEffect()` — Storm keyword. Place in the `ON_SELF_CAST` slot; at cast time queues a `StormCopyEffect` that copies the spell once per spell cast before it this turn (all players). Each copy may choose new targets (Dragonstorm)
- `CopyControllerActivatedAbilityTriggerEffect(String manaCost)` — ON_CONTROLLER_ACTIVATES_NONMANA_ABILITY: "whenever you activate a non-mana ability, you may pay `manaCost` to copy it" — snapshots the ability once it's on the stack, wraps `MayPayManaEffect` + `CopyControllerActivatedAbilityEffect`; single-target copies are retargetable (Rings of Brighthearth)
- `ChangeTargetOfTargetSpellWithSingleTargetEffect()` — redirect spell
- `ChooseNewTargetsForTargetSpellEffect()` — choose new targets

## Turn / phase

- `ControllerExtraTurnEffect(int)` / `ControllerExtraTurnEffect(int, boolean skipUntapStep)` — extra turns (non-targeting); `skipUntapStep=true` makes each granted turn skip its untap step (Savor the Moment)
- `RegisterLoseGameAtEndStepEffect()` — schedules "at the beginning of the next turn's end step, you lose the game" (Last Chance); skips the current turn's end step, fires on the extra turn's
- `ExtraTurnEffect(int)` — target extra turns
- `AdditionalCombatMainPhaseEffect(int)` — additional combat phases
- `SkipNextCombatPhaseEffect()` — ON_COMBAT_DAMAGE_TO_PLAYER: the damaged player skips their next combat phase (Blinding Angel). `(true)` = targeted spell variant where the caster picks the affected player (False Peace)
- `EndTurnEffect()` — end the turn

## Animate / transform

- `AnimatePermanentsEffect(power, toughness, subtypes, keywords, color, cardTypes, GrantScope, EffectDuration, filter)` (+ int-P/T sugar ctors) — one/many permanents become creatures. Scope SELF (manland/self, UEOT), TARGET (single, UEOT via Elvish Branchbender / UNTIL_YOUR_NEXT_TURN via Xenic Poltergeist / PERMANENT via Tezzeret / WHILE_SOURCE_ON_BATTLEFIELD via Awakener Druid; **multi-target UEOT reads `targetIds` for "up to N target" abilities — Fendeep Summoner**), OWN_LANDS (Sylvan Awakening), ALL_LANDS (every land on the battlefield, both players — Natural Affinity), OWN_PERMANENTS+filter (The Antiquities War). Wire multi-target via the multi-target `ActivatedAbility` ctor (per-position filters, minTargets/maxTargets). P/T `DynamicAmount` (`XValue`/`CountersOnSource`/`TargetManaValue` — "P/T each equal to its mana value", Xenic Poltergeist); null P/T = printed. The animation flag alone grants creature-ness (no CREATURE cardType needed); for a noncreature artifact it becomes an artifact creature
- `AnimatePermanentsEffect.crew()` — vehicle crew (printed P/T, +CREATURE)
- `AllLandsAreCreaturesEffect(power, toughness[, CardSubtype requiredSubtype])` — STATIC global: lands (both players') are fixed-P/T creatures that are still lands while the source is out. No subtype = every land (Nature's Revolt = 2/2); with a land subtype = only lands carrying it (Living Lands = all Forests become 1/1). Reverts when it leaves; anthems/lords see the animated lands
- `AnimateNoncreatureArtifactsEffect([boolean losesAllAbilities])` — STATIC global: every noncreature artifact becomes a creature with P/T = its mana value (March of the Machines). Pass `true` to also strip the artifacts' abilities in layer 6 (Titania's Song); no-arg / `false` = animate only
- `AllArtifactsUpkeepSacrificeUnlessPayEffect(String manaCost)` — STATIC global marker: grants every artifact "At the beginning of your upkeep, sacrifice this artifact unless you pay {N}." Read by `StepTriggerService` (not the layer system) — one `ForcedCostOrElseEffect` pay-or-sacrifice trigger per artifact the active player controls, sourced at that artifact. Grant is global regardless of who controls the enchantment (Energy Flux)
- `TransformSelfEffect()` — transform DFC
- `TransformSelfAndAttachToCreatureDamagedPlayerControlsEffect()` — combat-damage "you may transform; if you do, attach to target creature that player controls" (wrap in `MayEffect`)
- `TransformAllEffect(PermanentPredicate)` — transform all matching
- `PreventTransformEffect(PermanentPredicate)` — STATIC: permanents you control matching the predicate can't transform (e.g. Immerwolf)

## Static restrictions / taxes

- `EntersTappedEffect()` — enters tapped
- Conditional enters-tapped (check/fast/slow lands): `ConditionalReplacementEffect(condition, new EntersTappedEffect())` where the condition is the **negated** unless-clause (true ⇒ enters tapped), evaluated at entry against the entering permanent's controller (the permanent isn't on the battlefield yet, so counts exclude it). Check land = `ControlsPermanentCountAtMost(0, PermanentHasAnySubtypePredicate)` (tapped unless you control a matching permanent); fast land "unless N-or-fewer other lands" = `ControlsPermanentCount(N+1, new PermanentIsLandPredicate())`; slow land "unless N-or-more other lands" = `ControlsPermanentCountAtMost(N-1, new PermanentIsLandPredicate())`. **Never add a per-cycle enters-tapped record.**
- `RevealSubtypeOrEntersTappedEffect(subtype)` — STATIC: "you may reveal a [subtype] card from hand as it enters; if you don't (or can't), it enters tapped" (Lorwyn dual lands, e.g. Ancient Amphitheater = GIANT)
- `SacrificePermanentsToUntapSelfEffect(count, PermanentPredicate, description)` — resolution: all-or-nothing "sacrifice `count` [matching]. If you do, untap this creature." Untaps only when the controller controls ≥`count` matching permanents (auto-selects which to sacrifice). Wrap in `MayEffect` for the "you may" variant (Leviathan — UPKEEP_TRIGGERED, `count=2`, Island filter)
- `NoMaximumHandSizeEffect()` — no max hand size (static)
- `DiscardToTopOfLibraryInsteadEffect()` — static replacement (Library of Leng): the controller's effect-caused discards go on top of their library instead of the graveyard. Checked in `GraveyardService.discardCard()`; modeled as always keeping the card. Pair with `NoMaximumHandSizeEffect()`
- `IncreaseOpponentCastCostEffect(Set<CardType>, int)` — opponents' spells cost more
- `IncreaseOwnCastCostEffect(CardPredicate, int)` — matching spells cost N more, but only when cast by the source's controller (self-scoped; e.g. Derelor `CardColorPredicate(BLACK)`, 1 — "Black spells you cast cost {B} more", {B} modeled as +1 generic)
- `IncreaseSpellCostExceptOnControllersTurnEffect(int)` — every spell costs N more (symmetric), except during the spell's controller's own turn (Defense Grid, {3})
- `IncreaseOpponentCostForTargetingControlledPermanentEffect(PermanentPredicate, int)` — opponent spells/abilities targeting your matching permanent cost more
- `IncreaseActivatedAbilityCostEffect(PermanentPredicate, int)` — activated abilities of permanents matching the predicate cost N more generic mana to activate (STATIC, symmetric; e.g. Gloom with white-enchantment predicate, 3). Taxes ability activation, not spells; folded in by `AbilityActivationService` via `CastingCostService.getActivatedAbilityActivationTax`
- `IncreaseOwnCastCostUnlessRevealSubtypeEffect(int amount, CardSubtype)` — spell-self (STATIC): costs `amount` more unless you can reveal a matching-subtype card from hand (other than the spell). Lorwyn "reveal a creature-type card or pay {N}" cycle (Goldmeadow Stalwart: Kithkin/{3})
- `ReduceOwnCastCostEffect(DynamicAmount)` — **THE spell-self cost reduction.** `Fixed(N)` for a flat amount; a counting amount for "for each …" (Ghoultree `CardsInGraveyard(CardTypePredicate(CREATURE), CONTROLLER)`, Blasphemous Act `PermanentCount(PermanentIsCreaturePredicate, ANY_PLAYER)`, Draco `Scaled(BasicLandTypesAmongControlledLands(), 2)` = Domain, {2} per basic land type). Conditional reductions wrap it: `ConditionalEffect(condition, ReduceOwnCastCostEffect(Fixed(N)))` — Metalcraft (Stoic Rebuttal), ControlsPermanent (Academy Journeymage / Wizard's Retort / Wizard's Lightning / Lookout's Dispersal), OpponentControlsMoreCreatures (Avatar of Might), CardsLeftGraveyardThisTurn (Wilt in the Heat). **Never add a per-variant record for this.** Exception: when the gating condition would read effective P/T, do **not** use `ConditionalEffect` on `STATIC` — the static-bonus path evaluates the condition and computing effective toughness recurses. Instead fold the gate into a threshold amount so it's only evaluated by the cost handler at cast time: `ReduceOwnCastCostEffect(new FixedIfControlledCreaturesTotalToughnessAtLeast(M, N))` — "costs {N} less if creatures you control have total toughness M+" (Orysa, Tide Choreographer).
- `ReduceOwnCastCostForCardTypeEffect(Set<CardType>, DynamicAmount)` — own spells of the given types cost less (battlefield permanent, Heartless Summoning)
- `ReduceOwnCastCostForSharedCardTypeWithImprintEffect(DynamicAmount)` — controller's spells sharing a card type with the imprinted card cost less (Semblance Anvil)
- `ReduceCastCostForMatchingSpellsEffect(CardPredicate, int, CostModificationScope)` — matching spells cost less (SELF = yours, OPPONENT = opponents'; e.g. CardSubtypePredicate, CardIsHistoricPredicate, CardAnyOfPredicate)
- `ReduceOwnCastCostIfTargetingControlledPermanentEffect(PermanentPredicate, int)` — this spell costs less if first target is your matching permanent (kept as its own record — target-gated)
- `ReduceOwnCastCostIfTargetingStackEntryEffect(StackEntryPredicate, int)` — this spell costs less if first target is a spell on the stack matching the predicate (kept — target-gated)
- `ReduceOwnCastCostIfTargetingPermanentEffect(PermanentPredicate, int)` — this spell costs less if first target matches predicate, any controller (kept — target-gated)
- `ReduceActivationCostPerCounterEffect(CounterType, int reductionPerCounter)` — CostEffect placed in an ActivatedAbility's effect list; reduces the generic mana of the activation cost by N per counter of the given type on the source (floored at 0). Applied in `AbilityActivationService`. Used by Diary of Dreams (page counters)
- `LimitSpellsPerTurnEffect(int)` — max spells per turn (all players)
- `LimitSpellsForEnchantedPlayerEffect(int)` — max spells per turn for the enchanted player (Curse Aura)
- `CantSearchLibrariesEffect(boolean payableToIgnore)` — can't search (static); `true`=pay {2} to ignore (Leonin Arbiter, no-arg default), `false`=absolute (Mindlock Orb)
- `CantSearchLibrariesEffect()` — can't search (static)
- `CantCastSpellTypeEffect(Set<CardType>)` / `(Set<CardType>, boolean appliesToAllPlayers)` — can't cast spells of these types (static). Default controller-only (Steel Golem); pass `true` for symmetric "these spells can't be cast" (Aether Storm creature clause)
- `NoncreatureSpellsCantBeCastEffect(int minManaValue, boolean restrictXSpells)` — global/symmetric: no player can cast a noncreature spell with mana value >= `minManaValue`, or (if `restrictXSpells`) with `{X}` in its cost (static, Gaddock Teeg `(4, true)`)
- `CantCastAdditionalNonartifactSpellsEffect()` — global/symmetric: each player who has already cast a nonartifact spell this turn can't cast additional nonartifact spells; artifact spells and each player's first nonartifact spell are unrestricted (static, Ethersworn Canonist; enforced in `CastingPermissionService.isAdditionalNonartifactSpellRestricted`)
- `AlternativeCostForSpellsEffect(String, CardPredicate)` — alternative cast cost
- `PlayersCantCastSpellsFromZonesEffect(Set<Zone> zones)` — no player can cast from any zone in `zones` (static, global; only `GRAVEYARD`/`LIBRARY` enforced — Ashes of the Abhorrent passes `Set.of(GRAVEYARD)`, Grafdigger's Cage passes `Set.of(GRAVEYARD, LIBRARY)`)
- `TargetPlayerCantPlayLandsThisTurnEffect()` — target player can't play lands for the rest of this turn (spell; declare a player target via `target(...)`). Moonhold ({R} clause)
- `TargetPlayerCantCastCreatureSpellsThisTurnEffect()` — target player can't cast creature spells for the rest of this turn (spell; declare a player target via `target(...)`). Moonhold ({W} clause)
- `WardOfBonesEffect()` — static; each opponent controlling more creatures/artifacts/enchantments than the controller can't cast spells of that type (compared independently), and each opponent controlling more lands can't play lands. Controller never restricted. Ward of Bones
- `CardsCantEnterBattlefieldFromZonesEffect(CardPredicate filter, Set<Zone> zones)` — cards matching `filter` (null = all) can't enter the battlefield from any zone in `zones`; blocks reanimation/undying/library-search-to-battlefield (static, global; only `GRAVEYARD`/`LIBRARY` enforced — Grafdigger's Cage passes `CardTypePredicate(CREATURE)` and `Set.of(GRAVEYARD, LIBRARY)`)

## Choose / name

- `ChooseCardNameOnEnterEffect()` — choose card name ETB
- `PlayerHasProtectionFromChosenNameEffect()` — STATIC; controller has protection from the chosen card name (Runed Halo); pair with `ChooseCardNameOnEnterEffect()`
- `BoobyTrapEffect()` — STATIC marker; chosen player reveals draws + name-match sac/10-damage trigger (Booby Trap), detected in DrawService
- `RevealFirstDrawDrawOnBasicLandEffect()` — STATIC marker; controller reveals the first card they draw each turn, and if it's a basic land a "draw a card" trigger goes on the stack (Rowen), detected in DrawService (only the turn's first draw is revealed)
- `ChooseColorOnEnterEffect()` — choose color ETB
- `AllNonlandPermanentsAreChosenColorEffect()` — STATIC layer-5 color setter: all nonland permanents (any controller, incl. source) become the source's chosen color, replacing other colors. Pair with `ChooseColorOnEnterEffect` (Shifting Sky)
- `AllPermanentsGainChosenColorEffect()` — STATIC layer-5 additive color grant: all permanents (any controller, including lands and the source) gain the source's chosen color *in addition to* their other colors (not replacing). Pair with `ChooseColorOnEnterEffect` (Painter's Servant). Battlefield permanents only — does not recolor spells on the stack or cards in other zones
- `BecomeAllColorsUntilEndOfTurnEffect()` — self-scoped layer-5 color set: the source permanent becomes all five colors until end of turn (no target/choice). Floats a `BecomeChosenColorsUntilEndOfTurnEffect` with every color on the source (Scrapbasket `{1}:`)
- `ChooseSubtypeOnEnterEffect()` — choose creature type ETB

## Provider map

All normal (stack-resolution) effects: one `NormalEffectHandlerBean` `@Component` per effect in `service/effect/normalfx/`, auto-registered by `GameEngineConfig`. Shared logic in `*Support` classes in the same package.

"You may …" accept/decline abilities follow the mirror pattern: one `MayEffectHandlerBean` `@Component` per effect in `service/effect/mayfx/` (declares `handledEffect()`, implements `handle(GameData, Player, boolean accepted, PendingMayAbility)`), auto-registered into `MayEffectHandlerRegistry` by `GameEngineConfig`. `MayAbilityHandlerService.handleMayAbilityChosen` runs its non-effect preambles, then iterates the ability's `effects()` in list order and dispatches to the first registered handler. The effect-keyed routing chain is now fully migrated; the residue in `MayAbilityHandlerService` is the generic accept/stack-construction path, the graveyard target-selection helpers, the CR 603.5 resolution-time may path, and Galvanoth's guarded `CastTopOfLibraryWithoutPayingManaCostEffect` branch — all kept because they are entangled with target-selection/construction state, not because they are unmigrated routing. Beans that just orchestrate a sub-service (`MayCastHandlerService`, `MayMiscHandlerService`, `MayCopyHandlerService`, `MayPenaltyChoiceHandlerService`, `TariffSupport`) inject it; inline beans inject `GameBroadcastService`/`GameQueryService`/`InputCompletionService`/etc. directly. An interface-typed may-effect gets one bean per concrete implementor (registry keys on `effect.getClass()`), e.g. `CounterUnlessEffect` → `CounterUnlessPaysEffect` + `CounterUnlessDiscardsEffect`.

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
