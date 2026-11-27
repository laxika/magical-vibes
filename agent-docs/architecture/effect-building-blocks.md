# Effect building blocks

What the effect cleanup (2026-11-20 to 2026-11-26) established for anyone writing a card's effects or an effect's
handler: the rules every handler keeps, how subjects resolve, durations, and the shared pieces to reuse before adding a
record. Each record's factories and Oracle wording are also in `EFFECTS_INDEX.md` and `ORACLE_TEXT_EFFECT_MAP.md`; the
subject variants are in `SUBJECTS_REFERENCE.md`; how fused Oracle text becomes steps is in
`CARD_IMPLEMENTATION_PLAYBOOK.md` ("Composing fused text from steps").

A run tag ("run U1", "D25", "fold run 12b", "U3b'") names the section of the cleanup plan that built the piece, with its
premises, tests and rulings. The plan is in git: `git show 3b0d60d42d4:effect-cleanup-refactor.md` (step 10 and defect
runs D24 to D26), `git show ef0278a44e:effect-cleanup-refactor.md` (step 9 run 41 to step 10 run U3b') and
`git show 6728593a9a:effect-cleanup-refactor.md` (before 2026-11-23); grep it for the tag's heading.

## Subjects and the one resolver

Three sealed interfaces of small records in `model/effect`, built through one factory class, `Subjects`; they compose
(`controllerOf(triggering())`). `agent-docs/SUBJECTS_REFERENCE.md` has one line per variant: the wording it stands for, whether it
targets, whether a static ability may use it.

- `PermanentSubject`: `source()`, `target([group])`, `attached()`, `triggering()`, `controlledBy(players, filter)` and its named
  sets, `allOf(...)`, `except(...)`, `otherPermanentsSharing(...)`, `searched()`, `targetSpell()`, and the step-result variants
  (`thisWay(kind)`, `createdThisResolution()`, `returnedThisResolution()`, `exiledThisResolution()`).
- `PlayerSubject`: `you()`, `targetPlayer([group])`, `targetPlayerDeclaredElsewhere()`, `eachPlayer()`, `eachOpponent()`,
  `opponentsOf(...)`, `controllerOf(p)`, `ownerOf(p)`, the event players (`triggeringPlayer()`, `activePlayer()`, `defendingPlayer()`,
  `enchantedPlayer()`, `chosenPlayer()`), `playersThisWay(kind)`, `playersDealtDamageThisResolution()`.
- `CardSubject`: cards outside the battlefield. `cardsThisWay(kind[, filter])`, `targetCard(filter, scope)`,
  `triggeringCards(filter)`, `sourceCard()`, `exiledWithSource()`, `exiledCards(...)`, `cardsInYourHand(filter)` and the other zone
  sets, `revealedThisWay()`. It names a card only in the zone stay it is expected in: a card that moved is a new object (CR 400.7);
  the rest of the effect can find a card it moved to a public zone (CR 400.7j).

`SubjectResolutionService` (engine, `service/effect/`) is the only code that turns a subject into permanents, players or cards. Its
entry points: a resolving effect `(GameData, StackEntry, CardEffect, subject)`; a static ability `(GameData, source Permanent,
subject)`, which rejects a subject with `needsStackEntry()`. Its rules:

1. **Only a target variant declares a target.** An effect derives `targetSpec()` from its subject, so declaration and resolution
   cannot disagree. Targets are chosen as the object goes on the stack (CR 115.1). As it resolves, an illegal target is not affected
   and a part that needs information about it does nothing (CR 608.2b). A subject that is not a target never makes anything fizzle.
2. **A reference reads current information, else last-known information** (CR 608.2h). "Its controller" of a creature that died is
   the controller it last had.
3. **Nothing is done to an object that left** (CR 608.2h, CR 609.3); a permanent that left and came back is a new object (CR 400.7).
4. **A set is determined once.** A one-shot effect finds its set when applied (CR 608.2h) and a continuous effect made by a
   resolution keeps it (CR 611.2c); a static ability's set is re-evaluated by the layered pass (`agent-docs/LAYER_SYSTEM.md`).
5. **Several players act in APNAP order**: choices in APNAP order, then the action at once (CR 101.4, CR 608.2e). A verb whose handler
   can't choose-then-act for several players does not accept `eachPlayer()` / `eachOpponent()`.

`source()` is strict: it never falls back to the entry's target.

## Durations

A lasting change made by a resolution is a floating effect with a `Duration` (`model/effect/Duration`: a start, an end event or an
"as long as" condition; its named constants are the durations cards write), ended by `DurationClock`; a delayed
triggered ability is a `TemporaryGlobalTriggeredAbility`. Never a `Permanent` or `GameData` field with a cleanup reset
(`ARCHITECTURE.md`). Records that differed only in duration fold onto one record with a `Duration` component
(`RegisterGlobalTriggeredAbilityEffect`, `GrantStaticEffectToPlayerEffect`, `PlayerRestrictionEffect`,
`ExileTopCardMayPlayThisTurnEffect.until(...)`).

## Conventions

**Face-down and hidden information** (CR 406.3, 400.2, 708.2, 708.4, 708.5):

- `FaceDownVisibility` is the one rule for whether a spell, ability or permanent gives away a face-down card: `nameShownTo` /
  `descriptionShownTo` (per viewer), `publicName` (what every player may read), `sourceName(entry)` (a stack entry's source),
  `maySee` / `maySeeOnStack`. Every prompt, log line, option label and text that names a source or permanent asks it, never
  `getCard().getName()` (text built from a name bypasses the log's redaction; a card segment is redacted by `GameLogService`).
- `Permanent.displayName(card)`: a nameless card's description for text; the card keeps no name for rules that compare names
  (CR 201.2a).
- A copy of a permanent reads `PermanentCopierService.cardToCopy` (face-down copiable values while face down: a nameless 2/2,
  CR 707.2, 708.2a); a copy of a spell `CopySupport.createCopyCard(StackEntry)` (a face-down spell's copy is a nameless 2/2); token
  copies read copiable values.
- An effect that cares about a spell's characteristics judges `CastingPermissionService.spellAsEffectsSeeIt(entry)` (a spell cast
  face down has only its face-down characteristics).
- A face-down permanent's mana value is 0 and it has none of its card's abilities or perpetual changes (CR 708.2a): read the
  permanent, not its card. The reads: `GameQueryService.getPermanentManaValue` (a sacrificed permanent's snapshot too),
  `getEffectiveName` (null while face down) / `permanentHasName`, `hasEffectiveCardType`, `activeStaticEffects` for a static ability;
  a spell being cast face down has no subtypes or abilities (`CostModificationContext.castFaceDown`).
- An ability borrower reads a permanent's activated abilities through one method,
  `GainActivatedAbilitiesOfOwnCreaturesSelfEffectHandler.effectiveActivatedAbilities` (none of the card's while face down or after
  losing them; only its mana abilities under Blood Sun); a graveyard borrower asks `graveyardCardsHaveLostAllAbilities` first; a static
  replacement source asks `hasLostPrintedAbilities`, not only the floating `losesAllAbilitiesByEffect` (CR 613.1f).
- A hand choice is shown to its chooser alone and its log names no card; a prompt over exiled cards masks a face-down card from a
  player who may not look at it; a name prompt offers only the catalog's names and names of cards the chooser can know
  (`PlayerInputService.cardNamesOfferedTo`, checked by `ChoiceHandlerService.isCardNameAllowed`); the AI answers from what it can know.
- **The public log says only what the card's text makes public** (owner's decision 2026-11-23; CR 400.2). The game log is one list every
  player gets (`GameLogService` emits to all players; `GameLogEntry` has no audience), so nothing is logged for one player: the owner's
  hand shows the card. A conjure of a card the text names may name it; a random pick (a random spellbook card, a random library card,
  a sought card) and a drafted or chosen spellbook card going into a hand are "a card" (`GameLog.conjuresIntoHand`), and so is a
  duplicate of a hidden card (a library or hand card, a drawn card); a duplicate of a public card may be named. A perpetual change to
  cards in a hand or library is one line per hand or library with no name and no count, logged also when no card there matches
  (`PerpetualChangeLog`); a choice among hidden cards is one line counting no more than the instruction does (`ChooseCardEffectHandler`);
  a card chosen by `ChooseCardEffect` while it is in a hand is logged without its name. A hand count is public (CR 402.3), so "conjures
  7 cards" is fine. Lines name players, never "you".

**Rules every handler keeps:**

- A card that left a zone and came back is a new object (CR 400.7): returns, delayed returns and "that card" check the card's stay
  (CR 603.7c). Card subjects do this; a handler with its own lookup must too.
- Permanents one instruction moves leave or enter as one event (CR 603.10a, 608.2e); several players choose in APNAP order, then act:
  collect the choices in the choice context, then act once (leave batch or departure batch); reference handlers Sothera, Ol Rin's
  Searing Light. A cost sacrificing several permanents leaves as one batch (`SpellCastingService.paySacrificeCostTogether`).
- A choice with one possible answer is made unasked; a permanent that can't be chosen is neither offered, counted nor drawn at random
  (CR 608.2d); the shared prompts (`PlayerInputService.beginAnyTargetChoice`, `beginMultiPermanentOrPlayerChoice` and the others) drop
  it. A `MayEffect` whose leading step can do nothing isn't asked (a `source()` counter step whose source is gone or can't have the
  counters, a `controllerOf(...)` sacrifice its chooser can't make, a search its searcher can't make; CR 608.2d); gone with no else
  effect, a source counter `MayEffect` still asks (Kresh).
- What a put step moves enters through the entry batch: as-enters, copy and Aura host choices are asked (`handleCreatureEnteredBattlefield`),
  and a card that can't enter stays where it is (CR 101.2, 303.4f, 303.4g); an instant or sorcery card is never put onto the
  battlefield face up (CR 400.4a).
- A ward or ransom decline counters through `CounterSupport.counterSpell(gameData, counterControllerId, description, target)`, so
  Guile's replacement, flashback's exile and the countered events apply.
- Protection is checked at targeting for every target of a spell or ability, benign or harmful (CR 702.16b). A target spec stays
  `harmful` for a harmful verb's `target()`; a non-target subject declares no target.
- Every spell cast is asked through `CastingPermissionService`: `isSpellCastingAllowed` (spell limits, type and name restrictions,
  Ethersworn Canonist, split second; it asks `isPlayerPreventedFromCasting`, the one player-level check) on every casting path, and
  `canCastDuringResolution(gameData, player, card)` / `mayBeginCastDuringResolution` (a land is played, not cast; a split card is
  judged half by half, CR 709.3a) before any cast during a resolution (CR 601.3, 608.2g). Refused, the flow ends as a decline does.
  A free cast pays its cost increases (CR 601.2f) through `FreeCastCostIncreaseSupport.payOrAsk`: from the pool, or after a
  `PayFreeCastCostIncreaseEffect` question in which mana abilities may be activated (CR 601.2g), with a `PausedCast` that says how
  the flow resumes and where an uncast card goes; a flow's "can it be cast" check asks `payableWithManaAbilities`.
- "Until [source] leaves the battlefield" is `UNTIL_SOURCE_LEAVES`, and a control effect with it (or "until you lose control")
  holds while its source is phased out (`DurationClock.holds`; CR 702.26d); a resolution's "for as long as that Aura is attached"
  is `WHILE_REMAINS_ATTACHED` (CR 702.26f).
- A prevention prevents in the damage pools so the affected player orders it (CR 616.1), and every prevention sends the
  damage-prevented event.
- A search that finds nothing (an empty library, no match) is still a search (CR 701.23a): it is recorded, once per search however
  many picks it takes (`LibrarySearchSupport.endSearchFindingNothing`).
- A declined `MayEffect` runs no step and records nothing: an "if you do" after it would read an older step of that kind.
- A new damage, discard or sacrifice primitive calls `recordStep(kind, NONE)` first: central hooks add results to the latest step of
  that kind.
- AI value readers (`SpellEvaluator`) don't look inside a `ConditionalEffect`; a survivor that keeps a capability interface keeps the
  AI's view.
- A fold that merges engine-side stores (several `GameData` maps into one keyed store) is in scope (orchestrator's reading, 2026-11-23).
- The perpetual stores, not the card's printed fields, hold perpetual changes (`PerpetuallyGrantCardCharacteristicsEffectHandler.grant`,
  `perpetualCardBasePowerToughness`, `grantPerpetualKeywords`, `grantPerpetualAlternateCast`, keyed by the physical card id), and they
  stay off copiable values (CR 707.2).

## Shared pieces (reuse before adding)

**The verb records** (each run's Findings in git have the detail):

- `PutCountersEffect` (run Counters): `onSelf(kind[, int | DynamicAmount])`, `onSelfCountingTargetPlayer(kind, amount)`,
  `onSelfWithTargetPermanent(kind)`, `placedByEachController()`, over any subject (`createdThisResolution()`, `returnedThisResolution()`,
  `thisWay(kind)`). One placement: counter events, the CR 616.1 order, PUT_COUNTERS, the source snapshot refreshed; the mana-ability
  path places it inline. A source-relative amount after a token or return step is noted first (`NoteAmountEffect`). Run C2: the
  mass forms `onEachYouControl` / `onEach` / `onEachTargetPlayerControls` (any set subject); a set's amount once against the source
  unless `.evaluatedForEach()` (CR 608.2h); one resolution's placements one batch: one Hero / loyalty event (CR 603.2c), across an order prompt.
- `RemoveCountersEffect` (run Remove counters): `fromSelf(kind, int | DynamicAmount)`, `fromSelfCountingTargetPlayer(kind, amount)`,
  `fromTarget()` (any one kind), `fromTarget(kind, int | DynamicAmount[, targetPredicate])`, `fromTarget(kind, targetPredicate)`,
  `new RemoveCountersEffect(subject, kind[, int])`. One removal path (`PermanentCounterSupport.removeCounterFromPermanent`): vanishing's
  last time counter (CR 702.63a), oil, the controller's LAST_REMOVED event per permanent (CR 603.2c), a Siege's defeat after its last
  defense counter (CR 310.12b), REMOVE_COUNTERS recorded after the kind is chosen (`ANY`). "Remove all counters from it" is
  `PermanentCounterSupport.removeAllCounters`. Run C1: "remove all [kind] counters" is the `all` component, `allFromSelf(kind)`,
  `allFrom(subject, kind)` (`triggering()`, `allCreatures()` / `allLands()` / `allPermanents()`), `allFromTarget(kind, predicate)`; never
  refused for want of counters by a may or a mode; "that many" is `AmountThisWay(REMOVE_COUNTERS)`.
- `DealDamageEffect` (run Damage): one source deals N damage to one recipient. A `Recipient` kind (any target, creature, creature or
  planeswalker, player or planeswalker, permanent) fixes `targetSpec()`; factories `toAnyTarget`, `toAnyTargetInGroup`,
  `toAnyOtherTarget`, `toEventObject`, `toAnyTargetOfAnOpponentsChoice`, `toAnyTargetChosenAtRandom`, `toTargetCreature`,
  `to(PermanentSubject, amount)`, `toTargetCreatureOrPlaneswalker`, `toTargetPlayerOrPlaneswalker`, `toTargetOpponentOrPlaneswalker`,
  `toTargetPermanent`; riders `byTriggeringPermanent()`, `unpreventable()`, `unpreventableWhen(c)`, `unpreventableAndUnredirectableWhen(c)`,
  `cantBeRegenerated()`, `withUnconditionalRegenerationPrevention()`, `exilingDamagedCreaturesThatWouldDie()`, `exilingIfItWouldDie()`,
  `exilingPermanentsDealtDamage()`, `recordingDamageDealt()`, `withDamage(a)`. Run Damage P: the player kinds `to(PlayerSubject, int |
  DynamicAmount)`, `eachEvaluatedFor(amount, players)` (each amount fixed first, CR 608.2h), `toPlayerOrTheirPlaneswalker`, rider
  `byEventDamageSource()`; `subject` is null for them. Run Damage M1: the set kinds `toEach(PermanentSubject set, n)` (found once, CR
  608.2h; `toEachCreatureOfTargetedPlayer`, `toEachControlledByTargetedPlayer`, `toCreaturesOfTargetedPlayer`) and `toEachTarget`
  (`toEachTargetCreature`, `toEachTargetCreatureOrPlaneswalker`), one damage event, no AI one-target view. Run Damage M2: the mass factories
  `toEachCreature(n[, filter])`, `toEachCreatureAndPlaneswalker(n[, filter])` (the filter narrows both) over `controlledBy(eachPlayer(), …)`,
  `eachEvaluatedFor(amount, set)` (per permanent, the permanent the amount's source), riders `andTo(PlayerSubject)` (players in the set's
  event), `exilingDamagedCreaturesThatWouldDie()` / `exilingPermanentsDealtDamage()` / `cantBeRegenerated()` on a set; `sweepsBoard()` for
  an all-player set (`BoardWipeEffect`). Run Damage R: the rider `dealtBy(PermanentSubject dealers)` (each dealer deals its own
  damage through `entryDealtByPermanent`, amount per dealer, recipients found once; a target dealer nothing once gone, CR 608.2b), factories
  `bite([g, g'[, n]])`, `toTargetOfGroup`, `toTargetPermanentOfGroup`, `targetCreatureToItself(n)`, `toEachOther(dealer, set, amount)`. Run X: the
  rider `excessToControllerInstead()` / `excessToControllerInsteadIfDealerHasTrample()` (one target creature; excess worked out before the
  damage, dealt to the controller in the same event, the event value; CR 120.4a). Every kind: one permanent-damage path (D24), DEAL_DAMAGE
  begun, one excess tracker (CR 120.4a, 120.10; the dealer's deathtouch), the win check. The creature kinds loop over the effect's
  targets; the any-target kind can't (Magma Burst kicked: its unbound part would hit the kicked target too).
- `DamageSupport.entryDealtByTriggeringPermanent` is the one "it deals" carrier: a departed dealer is its departure snapshot first (its
  keywords as it left, a static grant's included), then the trigger-time snapshot; an entry that names no triggering permanent is dealt
  by its source. Its damage result is added to the resolving entry's DEAL_DAMAGE step. `DamageSupport.dealerAsItIsOrWas` /
  `dealerControllerId` / `entryDealtByPermanent`: a permanent deals damage as itself, live or last known (CR 120.1, 608.2h).
  `PermanentRemovalService` refreshes the resolving entry's source snapshot as the source leaves, and last known information keeps
  lifelink, deathtouch, infect and wither (CR 608.2h).
- `DestroyTargetPermanentEffect` (run K): wipes are set subjects, `all(filter[, cantRegen])`, `allTargetPlayerControls`, `sparing`,
  `sparingPermanentsCreatedThisResolution`, `readingTarget` (component `referencedTarget`), each-target `eachTarget([cantRegen])` /
  `eachTargetMatching(filter)` over `allTargets([filter])` (a resolution-time filter); a set's filter bound as it applies (CR 608.2h);
  `sweepsBoard()` / `setFilter()`; every batch records DESTROY departures, kept per kind (`ResolutionResults.departures(kind)`,
  `departureOf(id)`). `ExileTargetPermanentEffect.all(filter)` (run K2): a set exile is one occurrence (CR 603.2c).
- `PutCardsOntoBattlefieldEffect` (run R2): "return this card / it to the battlefield" over `sourceObjectCard()` (a copy returns its
  copying card), "that card" over `triggeringCards()`; `withCounters(kind, DynamicAmount)` worked out once before any card moves (CR 608.2h);
  a later step names the returned permanent with `returnedThisResolution()`.
- `DrawCardEffect` (run D): `eachEvaluatedFor(amount, players)` reads each drawing player's own number, all fixed before the first draw
  (CR 608.2h), APNAP (CR 101.4). The AI asks `AiUtils.drawRecipients(draw)` / `drawsForController(effect)` who draws (controller,
  opponents, each player, a target, other); only the controller's draw is its card advantage, an opponents' draw a cost.
- `BoostEffect` (run Boost B1): "[permanent] gets +X/+Y [and gains K] [for a duration]", a layer-7c floating effect per permanent
  (CR 613.4c, 611.2a). Factories `self`, `of(subject, …[, keywords])` (amounts read the boosted permanent, `AmountsRead.BOOSTED`),
  `target`, `targetInGroup`, `withCastTimeXValue`, `targetPermanent` (creature or Vehicle, harmful spec), `each(subject, …)` (only creatures,
  the set found once, CR 611.2c) (amounts read the source, `AmountsRead.SOURCE`, CR 608.2h). B2's mass forms `creaturesYouControl`,
  `allCreatures`, `creaturesTargetPlayerControls` (`[, filter]`, a `controlledBy` subject; `isMassBoost()`: one summary log line, left out of
  the AI's pump timing, the trigger label and text changes) and `each(triggeringAttackers(), …)`. The static self bonus is `self` in the
  `STATIC` slot. `CreatureBoostEffect.targetedBoost(effect)` is the AI's targeted shape (a `BoostEffect` only with a target subject).
- `UnlessPaysEffect(PlayerSubject payer, CostEffect ransom, CardEffect ifNotPaid, CardEffect ifPaid)` (runs U1 to U3b'): "[X] unless [P]
  pays [cost]" (CR 118.12a) and "[P] may pay. If they do" (`ifNotPaid` null). Counter factories `counterUnlessPays(int | String |
  DynamicAmount)`, `counterUnlessPays(mana, life)`, `counterUnlessPaysX()`, `counterUnlessPaysLife(int | DynamicAmount)`,
  `counterUnlessDiscards()` / `(filter, label)`, `counterUnlessDiscardsAtRandom()`, `counterUnlessDiscardsHand()`,
  `counterUnlessDiscardsOrPays(n)`, `counterUnlessSacrifices(filter, description)` / `(count, filter, description)`,
  `counterThatSpellUnlessYouSacrifice(filter, description)`, `counterUnlessBlights(n)`, `counterUnlessCollectsEvidence(n)`,
  `counterUnlessExilesGraveyard()`, `counterUnlessGetsPoisonCounters(n)`, `counterUnlessTakesDamage(n)`, `counterUnlessWaterbends(n)`,
  `changeTargetUnlessPays(n)`, `.exilingIfCountered()`, `counteredSpell()`, `retargets()`; "may pay" factories `youMayPay(mana, ifYouDo)`,
  `youMayPay(mana, life, ifYouDo)`, `youMayPayLife(int | DynamicAmount, ifYouDo)`, `unlessYouPay(mana, effect)`,
  `unlessYouPayLife(life, effect)`, `mayPay(payer, mana, ifTheyDo)`, `unlessPays(payer, mana, effect)`, `unlessPaysLife(payer, life,
  effect)`, `.ifPaid(effect)`, `.ifNotPaid(effect)`, `.withPrompt` (else the ransom's "Pay {1}?"). Rules: the payer is asked even when
  the inner effect can do nothing; a counter whose spell already left the stack asks nothing; a paid ransom never takes `ifNotPaid`
  (CR 118.12); the question is asked unless `couldPossiblyPay` proves no payment is possible; a paid answer takes a "do this only once
  each turn" action and a taken one makes the payment impossible (CR 603.2h); an intervening "if" is a `ConditionalEffect` around the
  record (CR 603.4). Payers: `you()`, `controllerOf(target())` (CR 608.2h), `triggeringPlayer()`, `defendingPlayer()`, `activePlayer()`,
  `chosenPlayer()`, `targetPlayerDeclaredElsewhere()`, and any several-player subject. `.payingTogether()` (run U3c): each answer only
  chooses (`ResolutionCostPaymentService.choose`, a pick made unasked when only that many could be chosen, `completePick`), the choices on
  `UnlessAnswer.choices`, and after the last answer `paymentMadeTogether` / `payTogether` splices one departure, tap, damage or draw step
  before the branch (CR 101.4, 608.2e); for sacrifice, tap, damage and draw ransoms. `unlessPays(payer, CostEffect, effect)`; a life
  ransom is read for each payer (`alternatives(…, payerId)`). How it runs: the payers are `players(payer)`
  (APNAP, CR 101.4), named once; a player who left the game or has no way at all to pay is dropped, every other one asked, even after
  another paid. The state is the entry's `UnlessAnswer(effectIndex, remainingPayers, anyPaid, alternative, payingPlayerId, partsToPay)`
  (`StackEntry.unlessAnswer(effectIndex)` / `setUnlessAnswer` / `clearUnlessAnswer`), not `resolvedMayAccepted`; `UnlessPaysEffectHandler.nextStep` (called by
  `EffectResolutionService` before the handler) returns ASK, PAID, NOT_PAID (the branch, once, after the last answer), NOTHING (the
  spell left) or PAYING (an all-of part asks); `answer` (from `MayAbilityHandlerService.handleResolutionTimeMayChoice`) pays and is
  idempotent for a Lich's Duel Mastery re-ask; `UnlessAnswer.askingAlternative` asks each payable alternative of an `AnyOfCost` in Oracle order;
  `anotherPlayerAlreadyPaid` is the AI's read (`AiDecisionEngine.anyPlayerManaPaymentAlreadyMade`). The chosen branch is put next
  (`spliceSequenceSteps`) and resolved through the loop, so a wrapper in it is judged as at the top level; `Card.bindEffectToTargetGroup`
  binds both branches. U6: `forEach(objects, payer, ransom, unpaid -> X)` is "for each [object], [X to it] unless [P] pays": one question per object (`controllerOf(objects)` / `ownerOf(objects)` read for that object, else every player the payer names), in the payers' APNAP order, objects gone before their turn skipped; after the last answer the unpaid objects are the entry's CHOOSE step and X resolves once over `thisWay(CHOOSE)` (CR 608.2c, 101.4); `UnlessAnswer.remainingObjects` / `unpaidObjectIds`. U6b: `forEachCard(cards, payer, ransom, unpaid -> X)` is the same over revealed cards (every player the payer names asked about each card, a card that left its zone skipped, CR 400.7; the unpaid ones the CHOOSE step where they are, `cardsThisWay(CHOOSE)`); a `DiscardCardTypeCost` ransom can be `.payingTogether()` (nothing picked at the answer; after the last one a `DiscardEffect` over `acceptedPlayers()` has each payer pick in APNAP order and discards at once, CR 101.4, 101.4a); `perPayer()` (set by `eachUnless`) keeps the AI from reading another payer's payment as paying for it. The hard AI scores it as value(`ifPaid`) − value(`ifNotPaid`) for the payer (`SpellEvaluator`), a branch making the payer lose life or take damage (named by the payer's own subject) on the ransom's life scale, an any-of ransom priced by the alternative asked (U3b1). Card tests answer an any-of ransom with `BaseCardTest.payUnlessAlternative(payer, action)` (U3b1). U3b2: `eachUnless(payers, ransom, players -> X(players))` is "each of them [does X] unless they pay": every payer chooses, then X happens to those who didn't, together (CR 101.4), `UnlessAnswer.paidPlayerIds` being the entry's accepted players at the branch; a payer `targetPlayer()` declares the target when no branch does (harmful); ransoms `ReturnPermanentToHandCost.youControl` / `.anyPlayers`, `PutCardFromHandOnTopOfLibraryCost`, `LandDropCost`, `SacrificeCreaturesWithTotalPowerCost`, `PayManaCost.perAmount(symbols, amount)`; amount `PlayerCount(PlayerSubject)`. Engine-built
  "you may pay" steps insert an `UnlessPaysEffect`. U4a: `PayManaCost.forSourceManaCost()` is "its mana cost" (the source live, else as it
  last existed, {X} as 0; face down or none is unpayable and not asked, CR 708.2, 118.6); a sacrifice description may carry its
  determiner ("another creature"); "do X. If you can't, Y" is `SacrificePermanentsEffect` + `ConditionalEffect(NotCondition(sacrificed()), Y)`. U4b
  ransoms (each a service case): `PayEnergyCost` (any amount, read as it resolves; `SourcePermanentManaValue` is "its mana value"),
  `ExileTopCardOfGraveyardCost`, `RemoveCounterFromSourceCost` of one kind, `RemoveCounterFromControlledPermanentCost` (one pick per
  counter, `ChoiceContext.ResolutionCostCounterRemoval`), `PutCardsFromGraveyardOnBottomOfLibraryCost(n)`, `ReturnCardFromGraveyardToHandCost`,
  `MillControllerCost`, `PutTypedCounterOnSourceCost` (put by the payer), `ReturnPermanentToHandCost.youControl(count, filter, words)`; a
  Phyrexian symbol is paid with its colour or 2 life (CR 107.4f); mandatory "pay {E}{E}. If you can't" is `PayEnergyEffect` +
  `ConditionalEffect(NotCondition(new HappenedThisWay(PAY_ENERGY)), …)`. U4d: the cumulative upkeep ransoms `FlipCoinsCost` (in the answer's Krark's Thumb window), `OpponentCreatesTokensCost`, `PutCounterOnOpponentCreatureCost` (a pick per counter), `DrawCardsCost`, `ExileTopCardOfLibraryCost`, `PutCardsFromSingleGraveyardOnBottomOfLibraryCost` (a graveyard per payment), `OpponentGainsLifeCost`, `GainControlOfPermanentsCost` (a different permanent per counter); `CumulativeUpkeepEffectHandler` puts `UnlessPaysEffect(you(), cost x age counters, sacrifice, paid)` next (`spliceSequenceSteps`), a mana ransom keeping `forCumulativeUpkeep` through the answer; every mana ransom payment records PAY_MANA by type (`StepOutcome.manaByColor`, `AmountThisWay.manaPaid(ManaColor...)`). U4c: `PayEchoCost` (echo, `KeywordTriggers.echo`) is read by
  `UnlessPaysEffectHandler.alternatives` as the cost it names and paid through that cost's case; an `AlternativeEchoCostEffect` makes it
  `AnyOfCost({0}, echo cost)`; "when its echo cost is paid" is `ifPaid` = `QueueReflexiveAbilityEffect` (a trigger on the stack).
- `ResolutionCostPaymentService` (`service/effect/cost`, D25): a cost a resolving effect asks any player for (CR 118.12, 118.12a). A new
  ransom kind is a case here. `couldPossiblyPay(gd, payerId, cost, sourceCard)` (takes the resolving entry) answers "no" only when it
  is certain (owner's rule): life alone that `canPayLife` refuses (CR 119.4, 119.8); mana the pool can't pay ({B} as life under K'rrik
  included) **and** no mana source at all (`hasManaSource`: a permanent with an intrinsic or an activated mana ability, printed or
  granted, tapped or not; another player's "any player may activate" mana ability; an owned card in hand, graveyard, exile or the
  command zone with a mana ability that works there; Channel's or the foreign-land permission); every other cost `canPay`; `AnyOfCost`
  any alternative; `AllOfCost` per owner's decision U3b'-1. `canPay(gd, payerId, CostEffect, sourceCard)` (CR 118.3: pool plus makeable
  mana) keeps the battlefield estimate for its other callers. `pay(gd, payer, CostEffect, Payment(sourceCard, sourceControllerId,
  answering))` returns `PAID`, `NOT_PAID` or `ASKING` (a discard / sacrifice / tap prompt or Lich's Duel Mastery's death replacements
  finishes it); `Payment` carries the resolving `entry`. Cases: `PayManaCost` (already read: no reduction, increase or source cost;
  `announcedX`, `lifeAmount`; `PayManaCost.x()`, `PayManaCost.ofTriggeringSpell()`: live spell, else its snapshot, face-down or no mana
  cost unpayable, CR 708.2, 118.6), fixed `PayLifeCost`, `DiscardCardTypeCost`, `DiscardRandomCardCost`, `DiscardHandCost`,
  `SacrificePermanentCost` (candidates judged from the entry's source, `excludeSource`), `SacrificeMultiplePermanentsCost` (exactly N,
  death replacements first, CR 616.1), `SacrificeSelfCost`, blight (`PutCounterOnControlledCreatureCost.blight`, CR 701.68), plain
  `CollectEvidenceCost` (CR 701.59), `ExileGraveyardCost`, `ExileNCardsFromGraveyardCost`, `ExileCardsCost`, `GetPoisonCountersCost`,
  `DamageFromSourceCost` (dealt by `Payment.entry`, CR 608.2h), fixed `WaterbendCost` (CR 701.67a; its mana is a tax),
  `TapMultiplePermanentsCost` (`MultiplePermanentTapCostHandler`), `SourceControllerDrawsCost`, `AnyOfCost`, `AllOfCost`. A sacrifice
  offers only what the payer could sacrifice for that entry (U3c): none while an opponent's effect can't make them sacrifice (Sigarda,
  `canEffectCauseSacrifice`), never a permanent that can't be sacrificed or chosen (`cantBeChosenToSacrifice`), nor a creature token the
  asking triggered ability can't make its controller sacrifice (`GameQueryService.isCreatureTokenProtectedFromTriggeredAbility`, The
  Master, Multiplied); its filter context names the entry's target's card, so "another" than the target (`PermanentIsTargetCardPredicate`, Torment of Venom) is judged (U3b1). `DiscardHandCost` pays through `DiscardHandEffectHandler` (U3b1). Discards and sacrifices go through the shared
  prompts, opponent-caused when the payer isn't the source's controller; one context pair, `PermanentChoiceContext.ResolutionCostPayment`
  / `MultiPermanentChoiceContext.ResolutionCostPayment`, completed by `completePermanentChoice` / `completeMultiPermanentChoice`. Mana
  raises `ActionEvents.taxPaid` for the mana spent when the payer isn't `sourceControllerId`; `payManaFromPool` is the hand-rolled pool
  payment; static `canPayLife(GameQueryService, gd, player, amount)` is the one resolution-time life check (CR 119.4, 119.8). A mana
  ability that works from the hand may be activated while a may-pay mana prompt is open (`GameService.activateHandAbility`,
  `AbilityActivationService.isHandManaAbilityAt`, CR 605.3a).
- Ransom cost types (`model/effect`): `AnyOfCost(List<CostEffect>)` / `AnyOfCost.of(a, b[, c])` (at least two alternatives, none nested;
  payable while any is, CR 118.3; run U7a: also an additional cast cost, `addCastingCost(AnyOfCost.of(...))` of standalone cast costs,
  the alternative read from the request field it consumes by `AdditionalSpellCostService.chosenAlternative` (none: mana, else life;
  two: refused) and swapped into its own `ExtractedCosts` slot by `withChosenAlternative` before validation and payment, CR 601.2b,
  601.2h; a free cast offers the payable ones in record order, `ExileFreeCastQueueSupport`; AI facets read the first alternative;
  run U7b: a `PayManaCost` alternative is `ExtractedCosts.additionalManaCost`, paid as part of the total cost (CR 601.2f) and
  chosen when no other alternative's field is filled; `chosenFrom` keeps the any-of; exile casts take a sacrifice through
  `additionalCostSacrificePermanentIds`; run U7c: `ForageCost()` (CR 701.61a), "forage or pay {B}" an any-of); `AllOfCost(List<CostEffect> parts)` / `AllOfCost.of(...)` (all or none, CR 601.2h, 118.3; at most one
  mana part, no nesting); `ExileGraveyardCost()` (always payable); `GetPoisonCountersCost(int)`; `DamageFromSourceCost(DynamicAmount)`
  (paid even if prevented, CR 118.12; every "unless [P] has [this] deal N damage to them" since U5, `new DamageFromSourceCost(new SourcePower())` for "equal to his power"; `DamageFromSourceCost.of(int)`); `ExileCardsCost(CardSubject)` (the entry's EXILE
  step); `SourceControllerDrawsCost(int)` (U3c: "has you draw a card", the payer chooses and the source's controller draws; paid together
  it draws once for the round, Development's rulings); `DiscardCardTypeCost(null, null, n)` of several cards (Phantasmagorian).
- `PayXManaCost(manaCost[, maximumX])` (run U8): an X chosen while paying (CR 107.3f), `UnlessPaysEffect.youMayPayX`; controller only, asked while X = 1 might be paid, one `XValueChoice` (0 = not paying, re-asked while the pool falls short), paid as `ResolutionCostPaymentService.paidWith(cost, x)`, the X set on the entry (branch and reflexive ability read `XValue`). A life ransom records PAY_LIFE (`AmountThisWay.lifePaid()`).
- `SubjectResolutionService.apnapOpponents(gd, controllerId)` (U3c): the opponents in
  APNAP order, a new mutable list.
- `EachPlayerPaysAnyManaEffect(startingWithYou, PlayerSubject payers, Resource)` / `.byYou(Resource)` (`lifeExactly` went in U3c): Resource `MANA`, `X_MANA`, `ENERGY`, `LIFE`, "pay any amount" at resolution; records PAY_MANA / PAY_ENERGY / PAY_LIFE,
  read by `AmountThisWay.manaPaid()` / `energyPaid()` / `lifePaid()`.
- Static combat taxes: `CantAttackOrBlockUnlessPaysEffect(amount, Side, attackerFilter)` / `.attacking(amount)` / `.blocking(amount,
  attackerFilter)` (unfiltered block cost once per blocker, filtered per matching attacker; CR 508.1h, 509.1d); `RequirePaymentToAttackEffect`
  / `RequirePaymentToBlockEffect` floated on players, `AmountsFixedWhenFloatedEffect.withAmountsFixed` (CR 608.2h), judged for the attacked
  player or planeswalker's controller (CR 506.2a, 508.5), `.includingAttacksOnBattles()`; `getGlobalBlockManaTax`.
- Other costs: `PayLifeCost(DynamicAmount)` / `PayLifeCost.x()`, `PayEnergyCost(DynamicAmount)` / `.x()`, `fixedAmount()`
  (CR 601.2f, 602.2b); `AmountEvaluationService.evaluateCost` / `isUnpayableCost` (CR 903.4f); `GameQueryService.canPayLifeForCosts`
  (CR 119.8); `PutCounterOnControlledCreatureCost.blight(n)` / `.blightX()` (CR 701.68a, 701.68b); `EscalateCost` (`escalate`, `entwine`;
  CR 702.120a, 702.42a); `SacrificeAllMatchingPermanentsCost`; `ActivatedAbility.withMaximumXValue`; `PayZeroRatherThanAbilityCostEffect`
  (CR 118.9, 602.2b); `ModifyKeywordCostEffect`, `ModifyCastCostPerTargetEffect`, `ReduceActivatedAbilityCostEffect`
  (`ActivatedAbilityKind`), `IncreaseSpellCostEffect(predicate, n, PlayerSubject)`, `ReduceCastCostForNextMatchingSpellEffect`,
  `ReduceActivationCostEffect` / `IncreaseActivationCostEffect` (any `DynamicAmount`), `CostModificationScope.names(controller, player)`.

**Steps, choices and readers:**

- `ChooseCardEffect` (`oneOf`, `upTo`, `oneOfEach`, `atRandom`, `fromEachOwner()`, a chooser `.by(...)`) records CHOOSE over cards;
  `ChoosePermanentsEffect` (`exactly`, `upTo`, `anyNumber`, `atRandom`, `chosenBy(player)`, `oneForEach`, `forEachVote`, `toSacrifice()`
  (only candidates the chooser could sacrifice, CR 608.2d, 701.21a), `thatCanBeAttachedTo` / `thatCanHost`,
  `thatTheTriggeringSpellCouldTarget()`) over permanents; `ChoosePlayerEffect` (`opponent()`, `chosenBy(player)`) /
  `ChoosePlayerAtRandomEffect` (`playersThisWay(CHOOSE)`); in a bespoke handler or completion, "an opponent" after an exile or reveal is
  `ChosenOpponentSupport.chooseThen` (a `ChosenOpponentFollowUp` record and its `ChosenOpponentContinuation`; CR 608.2d); `ChooseCardNameAtResolutionEffect` + `CardHasNameChosenThisResolutionPredicate`;
  `ChooseDamageSourceEffect.any()` / `.youControl()` (CR 609.7a candidates); `ChooseOneAtRandomEffect`; `ChooseOneEffect.anOpponentChooses`
  (announced before priority, CR 700.2e; targets after the mode, CR 601.2c; `Subjects.modeChooser()`); `ChoosePermanentOnEnterEffect`.
- Step kinds beyond Design's list, each with its readers: TAP (`TapPermanentsEffect`), GOAD (`GoadTargetCreatureUntilNextTurnEffect.of`),
  SEARCH (`HappenedThisWay.searched()`, `toACard(SEARCH, filter)`, `AmountThisWay.searchedManaValue()`, `Subjects.foundThisWay()`), SEEK
  (`SeekLibraryEffect`), CONJURE (`ConjureCardsEffect`, the drafts), VOTE (`VoteEffect`, `VoteResult`, `VotesFor`, `VotesReceived`,
  `Subjects.mostVoted()` / `votedFor()` / `mostVotedCards()` / `playersWithNoVotes()`), BID (`BidLifeEffect`), ATTACH, FLIP
  (`FlipUntilLoseEffect`), PAY_ENERGY, PAY_LIFE, and MANIFEST (manifest dread, `CloakTopCardOfControllerLibraryEffect`). `ProliferateEffect`
  is a PUT_COUNTERS step; `HappenedThisWay.toAPlayer(kind, PlayerRelation)`; `AmountThisWay.milledManaValue()` (CR 701.17c),
  `chosenManaValue()`, `revealedCards(filter)`, `revealedManaValue()`.
- `NoteAmountEffect` (NOTE, `AmountThisWay.noted()`); `QueueReflexiveAbilityEffect` for "when you do" (CR 603.12),
  `.withEventValue(effect, amount)` fixes the reflexive ability's X; `RepeatEffect(n, effect)`; `SeparateIntoPilesEffect`;
  `TemptingOfferEffect.of` / `.offeredOnlyTo` (all decide, then act; CR 608.2d); `IntuitionEffect`; `RollTwoDiceChooseOneEffect` and
  `DiceRollService.rollDice` (CR 706.6).
- `EffectResolutionService.resolveNestedInCurrentResolution(gameData, nested)`: steps resolved inside whatever is resolving, without the
  stack; if they stop for an answer they resume first.

**Subjects and their resolution:**

- Permanent subjects beyond Design's list: `allTargets()`, `targetDeclaredElsewhere()`, `departedThisWay(kind)` (duplicated as last
  known), `picked()`, `paidAsCost()` (spell tap costs too; copies carry them, CR 707.10), `convokedThisSpell()`, `lastAttachedCreature()`
  (only where a ruling says so; plain `attached()` names nothing once the source left, CR 608.2h, 704.5m), `attachmentsOf(...)`,
  `triggeringAll()`, `granter()`.
- Player subjects: `ownersOf(CardSubject)`, `acceptedPlayers()`, `targetPlayerChosenAtRandom()`, `playersThisWay(kind, CardPredicate |
  PermanentPredicate)`, `matching(players, PlayerPredicate)`.
- Card subjects: `cardsOf(PermanentSubject)` and `sourceObjectCard()` (perpetual verbs; the same object only, CR 400.7),
  `triggeringCardsInHand()`, `triggeringCardsAsTheyLastExisted(filter)`, `cardsYouOwn(filter)`, `ownedBy(cards, owners)`,
  `topmostCardInYourLibrary(filter)`, `cardsInLibraries(players, filter)` (only for `ChooseCardEffect.atRandom`), `cardsOutsideTheGame`,
  `exiledWithSourceMatching(filter)` (a face-down card matches nothing, CR 406.3a), `exiledWithSourceBy(link, filter)` (CR 607.2a,
  607.5), `exiledWithSourceUnlinked(filter)`, `exiledAsCost()`, `targetGraveyardCards(filter, scope, n)` (non-target; the card's target
  group declares them), `cardsInGraveyards(players, filter)`; `sourceCard()` in exile is checked against its exile stay.
- `SubjectResolutionService` helpers: `namesPlayer` (judges `controllerOf(attached())`, `ownerOf(source())`, `controllerOf(source())`,
  `activePlayer()`, `except(...)`), `namedCards` (static card subjects), `namesExiledCard`, `cardsToDuplicate` (last known, CR 608.2h),
  `spellOnStack`, `damageSourceIds`, `cardTypes` / `creatureTypes` (live or last known), `bindChosenValues` (chosen type, card type,
  player, parity, spell colour into a rule's filter as it registers, CR 608.2h, 611.2c), `bindResolutionFilter`, `bindPermanentSubjects`,
  `lastingRule`, `setFilterMatches`; `AmountEvaluationService.bindAmounts` / `bindTriggeringSpellManaValue` /
  `bindTriggeringSpellCardTypes`; `StaticEffectSupport.namesOnBattlefieldOf`.
- Targets: `TargetSpec.chosenAtRandom` / `atRandom()` (fires "becomes the target" and crime, CR 601.2c, 603.3d, 700.13);
  `StackEntry.targetsForGroup` (an activated ability's groups are positional); `ValidTargetService.permanentIdsSpellCouldTarget`.

**Movers, library, casting:**

- Movers over card subjects: `PutCardsOntoBattlefieldEffect` (`underControlOf`, `manifested()`, `attachedTo(host)`, face down, entering
  effects), `PutCardsIntoHandEffect`, `PutCardsIntoLibraryEffect` (`shuffledIn`, `alwaysShuffling`, `onBottomInRandomOrder`; each library
  shuffled once, CR 701.24c, 701.24d), `ShuffleTargetPermanentIntoLibraryEffect.of(subject)`, `ReturnChosenPermanentsToHandEffect`,
  `ExileChosenPermanentsEffect`, `ExileCardFromHandEffect`, `ExileCardsWithSameNameEffect`, `ExileCardAtRandomEffect` / 
  `ExileCardsFromTopUntilEffect` (`.withSource()`, CR 607.2a), `ExileGraveyardCardsEffect.cards(...)` (`.imprintingOnSource()`),
  `ExileTargetSpellEffect` (`.triggeringSpellWithSource()`), `ExileTopCardOfOwnLibraryEffect.exiledFaceDown()`, `RevealCardsFromTopUntilEffect`,
  `RevealCardsFromHandEffect`, `RevealTopCardOfLibraryEffect` (shown to every player, CR 701.20a), `DiscardCardsEffect`,
  `DiscardEffect.matching` / `.eachEvaluatedFor`, `DiscardHandEffectHandler.discardHand` (the one discard-hand primitive),
  `ReturnCardFromGraveyardEffect` (records `returnedThisResolution()`), `EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect.by`.
- Library: `SearchLibraryEffect` (`KEEP_IN_LIBRARY`, `EXILE_IMPRINT`, `REVEAL_ONLY`), `SearchTargetLibraryEffect`,
  `EachPlayerSearchesLibraryEffect` (`cardToTop`, `oneSearchForEach()`, `leavingLibrariesUnshuffled()`), `SearchLibraryAndOrGraveyardForCardToHandEffect.inZones`,
  `SearchOutsideGameForCardToHandEffect`, `SearchLibraryForSameNameAsEachPermanentEffect`, `SearchLibraryForOneCardOfEachSubtypeEffect`,
  `LibrarySearchFollowUp.ToHandPick`, `PlayerCanSearchTheirLibraryPredicate` / `LibrarySearchRestrictions.find`; `SeekLibraryEffect`
  (`.fromAmongTop(n)`, `.by(seeker)`), `SeekCardsToHandEffect.alsoSought`; `LookAtTopCardsAndMayCastEffect` (`lookAt`, `reveal`, records
  REVEAL; split cards by half, CR 709.3a; CR 603.2h), `LookAtTopCardMayRevealMatchingToHandEffect`,
  `LookAtTopCardMayPutMatchingOntoBattlefieldEffect`, `LookAtTopCardsOfTargetLibraryEffect.lookingAt`, `LibrarySelectionFollowUp.inResolution`.
- Casting now: `CastCardsWithoutPayingManaCostEffect` (`anyNumber`, `upTo`, `withTotalManaValueAtMost`; one ordered choice, CR 608.2g),
  `CastCardFromGraveyardEffect.of(CardSubject)` (`.ifCast`), `CastCardWithoutPayingManaCostIfAbleEffect`,
  `MayCastAnySpellFromHandWithoutPayingManaCostEffect` (`anyNumber`, `upTo`, `orElse`), `MayCastDiscardedCardWithoutPayingManaCostEffect.castBy`,
  `CopyImprintedCardAndMayCastCopyEffect.of(cards)` (`.castingUpTo`, `.castingAnyNumber`), `ExileTopCardsAndMayCastSpellsEffect`
  (`controllerMayPlayUpTo`, CR 305.2, 305.3). A producer of the free-cast queue offers each card through
  `ExileFreeCastQueueSupport.offerFaces`: an adventurer card whose Adventure alone fits is cast as it, one whose faces
  both fit is cast as the face its caster chooses (CR 716.3, 716.3a).

**Permissions, restrictions and grants:**

- `AllowPlayCardsFromExileEffect` over a `CardSubject` (`thisTurn`, `untilEndOfYourNextTurn`, `whileExiled`, `whileYouControlSource`,
  `alsoCardsExiledLaterThisTurn()` (CR 611.2c); `playableByOwner()`, `playableOnlyIf`, `withAnyManaType`, `withoutPayingManaCost`,
  `exilingInsteadOfGraveyard`); `ExileTopCardMayPlayThisTurnEffect` (`fromLibraryOf`, `until`, `playableBy`, `choosingOne`, `upTo`,
  `mayOnlyCast`); `PlayFromAmongExiledCardsEffect`; `AllowCastFromTopOfLibraryEffect` (CR 118.9), `AllowCastMatchingCardsFromGraveyardThisTurnEffect`,
  `AllowCastTargetCardFromGraveyardThisTurnEffect` (CR 716.3); `SpendAnyManaTypeToCastEffect`, `SpendManaAsAnyColorEffect`,
  `SpendManaAsAnyColorForActivatedAbilitiesEffect`.
- `PlayerRestrictionEffect(players, PlayerRestriction, duration)` (`PlayerRestriction.castingSpells()`, `castingNoncreatureSpells()`,
  `castingSpellsMatching`, `playingCardsFrom`, `activatingAbilities()` / `activatingAbilitiesOf(types…)`), stored in
  `GameData.restrictionsOnPlayer`; static locks `PlayersCantCastSpellsFromZonesEffect`, `OpponentsCantCastOrActivateDuringYourTurnEffect`,
  `OpponentsCantCastSpellsMatchingPredicateEffect`, `CantSearchLibrariesEffect`, `PlayersCantPayLifeEffect`; readers
  `GameQueryService.isPlayerRestricted`, `findRestriction`, `abilityLockRefusal`, `isHeldToSorceryTiming`; conditions `StackIsEmpty`,
  `TargetPlayerCouldCastSorcery` (CR 307.5), `TargetPlayerMatches(PlayerPredicate)`.
- `GrantStaticEffectToPlayerEffect` (`untilEndOfTurn`, `untilYourNextTurn`, `untilPlaneswalk`, `PERMANENT`, `WHILE_SOURCE_REMAINS`:
  nothing begins if the source left, CR 611.2b): a resolution's "until …, you [rule]" floats the rule's static record on the player, its
  reader asking for the floating copy beside the battlefield (`hasFloatingEffectOnPlayer`). Several rules are several grants; a floated
  `GrantEscapeToGraveyardCardsEffect` reaches only the matching cards in that player's graveyard as it begins (CR 611.2c; run PO). Floated rules include the damage doublers and
  plus-N, `SkipDrawStepEffect`, `PlayWithHandsRevealedEffect`, `ProtectionFromPlayersEffect`, `IgnoreOpponentHexproofEffect`,
  `PlayerHasProtectionEffect`, `EnterPermanentsOfTypesTappedEffect`, `ReplaceDamageAboveThresholdEffect`, the library-top permissions
  (`CastingPermissionService.libraryTopGrants`).
- Next-spell riders: `GrantFlashToNextMatchingSpellEffect`, `GrantFreeCastForNextMatchingSpellEffect`, `EmpowerNextCreatureSpellThisTurnEffect`,
  `GrantAbilityToNextSpellThisTurnEffect`, judged by `DurationClock.waitsForSpell`.
- Perpetual verbs over card subjects, each with one store keyed by the physical card id: `PerpetuallyGrantAbilitiesEffect`
  (`perpetualAbilityGrants`; a graveyard ability with `AbilityZones.GRAVEYARD`, never seeded onto a permanent), `PerpetuallyChangeCastCostEffect`
  / `PerpetualCardCastCostSupport`, `PerpetuallyBoostCardEffect.toCards`, `PerpetuallyGrantAlternateCastEffect` (`perpetualAlternateCasts`),
  `PerpetuallyGrantAnyColorManaToCastEffect`, `GameData.grantPerpetualKeywords`; `GrantGraveyardAbilityToCardsEffect` (`PerCard`, scavenge
  included), `GrantSupertypeEffect` (`(T, attached())` for "enchanted permanent is"). "This card perpetually gets" is
  `PerpetuallyBoostCardEffect(this, …)` or `.toCards(sourceObjectCard(), …)`, a layer-7c modifier in every zone (run PO).

**Damage, prevention, redirection:**

- Player damage is `DealDamageEffect.to(PlayerSubject, …)` (run Damage P; Typhoon's "equal to the number of [X] that player controls" is
  `eachEvaluatedFor(PermanentCount(filter, CONTROLLER), eachOpponent())`); another permanent dealing damage is the rider
  `DealDamageEffect.dealtBy(dealers)` (`bite`, `targetCreatureToItself`, `toEachOther`; run Damage R); fight is `SourceFightsTargetCreatureEffect` (`FightSupport.fight`).
- `PreventDamageToRecipientsEffect` (static prevention: `to(...)`, `toYouAnd(...)`, `toEveryone()`, `fromSpells()`,
  `fromInstantAndSorcerySpells()`, `fromSourcesOfChosenColor()`, `dealtBy(filter)`, `preventing(amount)`); `PreventNextDamageByTargetCreatureEffect`,
  `PreventDamageAndAddPlusCountersEffect` (CR 615.12), `DamageCantBePreventedEffect` (`GameQueryService.isDamagePreventable`).
- Redirects: `RedirectYourDamageToTargetCreatureThisTurnEffect.to(destination, RedirectedDamage)`, `RedirectNextDamageEffect.allThisTurn`,
  `RedirectNextDamageFromChosenSourceToSelfEffect` (one `SourceKeyedRedirectShield` per source, CR 614.9), `RedirectPlayerDamageToSelfEffect`.
- Multipliers and plus-N: `DoubleDamageFromCreaturesEffect.fromSourcesYouControl` / `.onlyDoubling(DamageScope)`,
  `DoubleDamageToOpponentsAndTheirPermanentsEffect`, `DoubleControllerDamageEffect`, `BoostControllerDamageThisTurnEffect`,
  `AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect`, read by `GameQueryService.getAdditionalDamageBonuses` (one option per
  instance); `GameQueryService.asDamageSourcePredicate` (Ghostly Flame, CR 609.7b).

**Counters:**

- `AddOnePlusOneCountersEffect` (every static counter-placement replacement; a placement naming no placer is put by the affected
  controller; CR 614.12) judged by `GameQueryService.placementReplacementApplies` / `placementAffects`; `GameQueryService.entersWithCardType`
  (CR 614.12); `ControlledPermanentsEnterWithAdditionalCountersByAmountEffect.ofEnteringPermanent`.
- Moves and removals among several: `MoveCounterFromSourceToTargetCreatureEffect` (`move`, `all`, `anyNumber`; CR 122.5 per kind;
  `PermanentCounterSupport.moveCounters`), `RemoveUpToCountersFromAllPermanentsEffect` (`anyNumber`, `exactly`, `matchingTarget`,
  `orFromPlayers`; one how-many prompt, `ChoiceContext.MoveCountersAmountChoice`), `RemoveAllCountersFromTargetPermanentEffect`
  (`fromPlayers`, `fromPermanentsAndPlayers`), `RemoveCountersFromExiledCardsEffect`, `DoubleCountersOnTargetPermanentEffect`,
  `AddAnotherCounterOfChosenTypeToTargetEffect`, `AdjustChosenCounterOnTargetEffect`.
- Stores: `GameData.exiledCardCounters` (every kind but time is forgotten as the card leaves exile; `GameData.isSuspended`, CR 702.62b),
  `PlayerCounterType` + `GameData.playerCounterCount` / `removePlayerCounters`; `CantGetCountersEffect`, `CantHaveCountersEffect`.
- Giving a player counters: `LifeSupport.givePlayerCounters(gameData, player, kind, amount, sourceName, placer)`, the one writer
  (the player lock, the replacements for that placer, the log, the "you put counters" event); a cost that puts player counters asks
  `GameQueryService.playerCantGetCounters` and `replacePlayerCounters(gameData, player, kind, amount, placer)`. "You get" is put by
  the effect's controller, proliferate's by the proliferating player, a ransom's by its payer (CR 118.12a).

**Copies, tokens, conjures:**

- `BecomeCopyOfSubjectEffect` (`of(target())`, card targets, `.by(copier)`, `.lasting(...)`, `.except(CopyExceptions)`: copiable-value
  exceptions, CR 707.9a, 707.9b, 707.9d, 205.1a, 205.3d); `PermanentCopierService.copiableCard` / `lastKnownCopiableCard` (a departed
  permanent's copiable values from its departure snapshot, CR 707.2, 608.2h); `CopyCreatureCardInGraveyardOnEnterEffect`.
- `CreateTokenCopyOfTargetPermanentEffect` (`forController(PlayerSubject)`, CR 111.2; `withSubject(departedThisWay(kind))`),
  `CreateTokenCopyOfCardEffect.of`, `AddTokenCreationEffect` and `TokenCreationReplacementSupport.withEventModifiers` (CR 506.3);
  `CreateTokenEffect` investigate N times is N events (CR 701.16a).
  `CreateTokenCopyOfTargetPermanentEffectHandler.replaceFirstTokenCreation(gd, entry, amount)` (run T1): the Esix / Mirrormind Crown /
  Moonlit Meditation offer for a token copy, parked and answered on the re-run (target and source copies).
- `CopyTriggeringSpellEffect` (`copiedBy`, `mayBeCopiedBy`, `targetingEach`), `CopyTargetActivatedOrTriggeredAbilityEffect`,
  `CopyControllerActivatedAbilityEffectHandler.copy` (the one ability copier), `RegisterDelayedControllerSpellCastTriggerEffect.nextThisTurn`.
- `ConjureCardsEffect` + `ConjureDestination` (`atRandomFrom`, `duplicateOf`, `named`, `eachOf(printings)`, `of(List<EachOf.Printing>)`,
  `intoHand()`, `intoLibrary()`, `ontoBattlefield()` / `ontoBattlefieldTapped()`, `.into(players)`, `.conjuredBy(players)`, `.times`; run J:
  onto the battlefield one entry batch from `OUTSIDE_GAME`, one conjure event, CONJURE + `createdThisResolution()` as each enters);
  `DraftCardFromSpellbookEffect` (`.times(n)`); `ConjureDuplicateOntoBattlefieldEffect`.

**Delayed and global triggers:**

- `RegisterDelayedStepTriggerEffect` (`atNext`, `atNextMainPhase`, `aboutSourceAtNext`, `aboutCardAtNext` (`.ofThatPlayer`),
  `aboutAtNext(step, Subjects.target(), effects…)` (the registering object declares the harmful target), `aboutCreated`,
  `aboutPutOntoBattlefield`); `RegisterDelayedEventTriggerEffect` (`aboutWhenSourceLeavesThisTurn`, `whenLeavesThisTurn`, ...);
  `RegisterGlobalTriggeredAbilityEffect` with a `Duration`, `.nextThisTurn(ability)` (used up once, CR 603.7b);
  `ScheduleCreatedPermanentsEffect`, `SacrificeCreatedPermanentsAtEndStepEffect`; `AiUtils.withEffectsDelayedAboutTheTarget`.

**Combat, types, colours, protection, mana:**

- Combat: `MustBlockEachCombatEffect`, `MustBeBlockedIfAbleEffect`, `MatchingCreaturesMustAttackThisTurnEffect` (`attacking`, `lasting`,
  `controlledBy`), `CantAttackThisTurnEffect`, `CantBlockThisTurnEffect(subject, Duration)`, `CantAttackOrBlockAloneEffect`,
  `CantAttackCardOwnerEffect`, `CreaturesCantAttackControllerUntilNextTurnEffect`, `OnlyMatchingCreaturesCanAttackThisCombatEffect`,
  `MakeTargetAttackingCreatureBlockedEffect` (CR 509.1h), `CanBlockCreaturesWithShadowEffect`, `CantBeBlockedIfDefenderControlsMatchingPermanentEffect.whileDefendingPlayer`
  (CR 508.5, 509.1b), `AttachedPermanentRestrictionEffect`, `ExploreEffect` (CR 701.44d). Run S: `CantAttackOrBlockUnlessEffect.attack` / `block` /
  `attackOrBlock(condition, text)` (CR 508.1c, 509.1b), `CreaturesCantAttackUnlessPredicateEffect.youControl(exemption)`, `EnchantedPermanentCantActivateAbilitiesEffect.includingManaAbilities()` (CR 602.5).
  Run U: `MakeCreatureUnblockableEffect` over one permanent subject (`()` target, `self([Duration])`, `of(subject[, Duration])`,
  `targetPermanent(filter)`, `.byCreaturesYourOpponentsControl()`; a set refused, CR 611.2c), a floating rule for every duration (CR 613.11).
  Run G: `GoadTargetCreatureUntilNextTurnEffect.eachCreature(filter)` / `creaturesControlledBy(player)` / `creaturesTargetPlayerControls()` (a
  `controlledBy(targetPlayer(), …)` subject declares the harmful player target; the set found once, CR 608.2h; CR 701.15d).
- Types, colours, names: `SetCardTypesEffect`, `LoseAllLandTypesEffect`, `GrantSubtypeEffect.chosenSubtype` (CR 305.7),
  `GrantBasicLandTypeToTargetEffect.of` (CR 611.2c), `LosesAllAbilitiesEffect.exceptManaAbilities`, `AnimateNoncreatureArtifactsEffect`
  (CR 613.6), `SetBasePowerToughnessEffect` / `SetBasePowerToughnessToAmountEffect` (keep negative toughness: `SourceToughness(true)`,
  CR 107.1b), `BecomeChosenColorsUntilEndOfTurnEffect`, `GrantColorEffect.chosenColor`, `SetChosenColorUntilEndOfTurnEffect`,
  `SetNameEffect`.
- Protection and hexproof: `ProtectionFromColorsEffect.ColorSet` + `GameQueryService.protectionColors`, `ProtectionFromManaValueEffect`,
  `ProtectionFromMulticoloredEffect`, `ProtectionFromPermanentsMatchingEffect`, `PlayerHasProtectionEffect` +
  `GameQueryService.playerProtectionFrom` (CR 702.16k), `GrantControllerAndPermanentsHexproofFromColorsEffect`, `IgnoreOpponentHexproofEffect`.
- Mana: `ReplaceLandManaWithColorEffect` (`GameQueryService.landManaColorReplacements`), `AddManaOfTypeProducedByTappedPermanentEffect`,
  `AwardPersistentManaEffect`, `AwardAnyColorManaEffect(DynamicAmount)`, `AwardManaToActivePlayerEffect.to`, `ArtifactManaSpentToCast`.
- Players and turns: `SkipDrawStepEffect(SkipStepOrPhaseKind, PlayerSubject)`, `SkipDrawsEffect`, `SetControllerMaximumHandSizeEffect`,
  `AdditionalUpkeepStepEffect`, `ControllerLosesGameEffect` over several players (CR 104.4a, 104.1), `LossReplacer` (CR 616.1),
  `GrantControllerKeywordUntilEndOfTurnEffect`, `IgnoreLegendRuleEffect`; player predicates `PlayerHasMostLifePredicate`,
  `PlayerHasLeastLifePredicate`, `PlayerLifeExactlyPredicate`, `PlayerControlsMostCreaturesPredicate`, `PlayerWithoutMaxSpeedPredicate`
  (CR 702.179e).
