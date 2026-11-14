# Replacement and damage

Replacement and prevention ordering, death replacements, damage events, losing the game, token-creation replacements.

## Deaths and losses

### Single kill-site for damage (CR 704.5f, 704.5g, 704.5h)
Damage code only records (marked damage, deathtouch flag, counters); never destroy inline from a damage path or duplicate the lethality check.
Owner: `StateBasedActionService` (`performStateBasedActions`).
Why: damage deaths are state-based actions, performed together.

### Single gate for losing the game (CR 104.3)
Every site ending the game on a loss calls `resolveLoss` and finishes only on `LossOutcome.LOSES`; nothing else calls `canPlayerLoseGame` (`LossDispatchRatchetTest`). Win effects use `canPlayerWinGame` and raise no loss.
Owner: `GameOutcomeService` (`resolveLoss`); a new loss replacement is a `LossReplacer` bean.
Why: a hand-rolled check skips loss replacements (Lich's Mirror).

## Ordering replacement and prevention effects

### One ordering service for every modifiable event (CR 616.1)
Damage, land mana, life gain, mills and token creation are `ReplacementEvent`s: the chooser is asked unless one option applies or all are `pure` and every order ends the same; options are re-collected after each application (CR 616.1f), applied keys never return (CR 614.5), self-replacements go first (CR 616.1a).
Owner: `ReplacementOrderService` (`run`).
Why: the affected player picks the order when it matters.

### A new replacement is a rule in a candidate bean
Add it to a `ReplacementCandidateSource` bean under `service/effect/replacement/`, never as a step in a damage or creation path; `applicable` and a pure option's `transform` must not touch the game (they run in one query scope), and a rider, consumed shield or prompt makes an option non-`pure`. Dealing the damage (`DamageSupport.finishPlayerDamage` / `finishPermanentDamage`) is not a replacement.
Owner: `ReplacementCandidateSource`; `DamageCandidateSource` for damage.
Why: an effect applied outside the pool is applied in a fixed order, or twice.

### Effects pass the unmodified damage amount
Multipliers and "plus N" bonuses are pool options, one per effect instance; `applyDamageMultiplier` is only a prediction (`DamageMultiplierRatchetTest`).
Owner: `DamageSupport`; `DamageBonusGrant`.
Why: the recipient may apply a multiplier between two bonuses.

### A replacement that makes the permanent leave asks first (CR 616.1f)
A step that destroys or sacrifices instead of dealing damage (Kill-Suit Cultist) declares the departure, whose questions are answered before the step applies.
Owner: `CreatureDamageStep` (`departs`), `DamageSupport` (`askBeforeDamageDeparture`).
Why: the destruction is the modified event; its replacements apply.

### A resolving effect sees the damage and mill it really caused
An effect's damage and mills are journaled; when one prompts, the effect is resolved again and replays what already happened.
Owner: `DamageJournalService`; `GraveyardService` (`resolveMillPlayer`) for mills.
Why: "if it was dealt damage this way" reads the real result.

### Combat damage is one event per recipient (CR 510.2)
Each recipient's damage is one `DamageEvent` with a `DamagePortion` per source, run in APNAP order of choosers; a prompt parks the step. Prevention, lifelink and "deals combat damage to a player" abilities use each source's recorded player (`CombatDamageState.combatDamagePlayerRecipients`), not the defending player.
Owner: `CombatDamageService` (`processCollectedCombatDamage`).
Why: a step's combat damage is dealt at once; a myriad copy may damage another opponent.

### Redirected damage runs through the new recipient's pool (CR 614.9)
Source-side and global effects already applied stay spent (CR 614.5); the new recipient's own effects still apply.
Owner: `RedirectedDamageRules` (`staysSpent`), `DamageEvent` (`redirectedTo`).
Why: a doubler must not apply twice.

### Replaced land mana is final
A color chosen after a land's replacements applied is marked `withReplacementsApplied()`.
Owner: `LandManaReplacementService`.
Why: False Dawn's color change must not apply twice.

## Token creation

### Token creation is one ordered event (CR 616.1)
The creating player orders count, addition, kind and Jinnie Fay replacements; create through `applyCreateToken` or `TokenCopySupport`, never multiply a count yourself. Tokens of an ordered event are marked `Permanent.tokenKindReplacementsApplied` so entering doesn't replace them again (CR 614.5). Known gap: three handlers still call `GameQueryService.getTokenMultiplier` in a fixed order.
Owner: `PermanentControlSupport` (`applyCreateToken`), `TokenCopySupport`.
Why: a doubler applied after an addition doubles the added tokens too.

### Applicability is judged against the event as it stands (CR 616.1f)
A replacement that didn't apply becomes an option once another adds a token it applies to; added blueprints share constants so equal outcomes need no prompt.
Owner: `TokenAdditionReplacements`, `TokenCreationReplacements`, `TokenCreationReplacementSupport`.
Why: Worldwalker Helm adds a Map once the event creates an artifact token.

### Replaced and added tokens keep the creating effect's instructions
In a token-copy event, substitutes and added tokens get the effect's riders, attack target and per-card instructions, but not the copy exception or copy rider.
Owner: `TokenCopySupport` (`createCopiesAfterReplacementOrder`), `TokenCopyBatch`.
Why: they aren't copies, but "those tokens gain haste" applies (rulings).

### Follow-ups on created tokens run after the creation finishes
Pass the follow-up as `TokenCopyBatch.afterEntry` or splice it as the next step, and look permanents up through the game given.
Owner: `TokenCopySupport` (`createTokenCopiesThen`), `StackEntry` (`insertEffectsToResolve`).
Why: a creation may pause; the follow-up must see every token once.

### Attack targets are chosen after the replacements (CR 508.4)
Tokens entering attacking are created first; each creature token's target is chosen once the event is final (myriad: among its opponent's).
Owner: `ChooseAttackTargetsForCreatedTokensEffectHandler`, `TokenCopyBatch` (`chooseAttackTargets`).
Why: doubled and added tokens each get a choice and enter together.

## Death replacements and departures

### One option per distinct death outcome (CR 616.1)
Death replacements are not `ReplacementEvent`s: the controller picks from options ordered card-keeping first, then one plain exile, then exiles with riders (the AI takes the first). A new distinct outcome needs an option kind.
Owner: `PermanentRemovalService` (`deathExileReplacementOptions`).
Why: a fixed pipeline order must not override the choice.

### Departures are batches, asked before anything leaves
New code builds a `DepartureBatch` and calls `depart` / `departNow`, never a legacy wrapper (`LegacyDepartureWrapperRatchetTest`); questions are answered in APNAP order while every member is on the battlefield. A removal needing an answer where its caller can't be stopped and rerun is an engine error (`LegacyDepartureFallback`), never a late prompt.
Owner: `DepartureService`.
Why: permanents of one event leave together, each with its chosen replacement.

### State-based deaths of one check are one batch (CR 704.3)
It waits in `GameData.pendingDepartureBatch` while a question is open; Auras whose graveyard move has a question are left for it (CR 704.5m).
Owner: `StateBasedActionService`, `PermanentRemovalService` (`aurasLeftForStateBasedDepartures`).
Why: a replacement whose source dies in the same batch still applies.

### Only handlers that change nothing before their first removal may rerun
`AsksDeathReplacementsFirst` marks them; a question stops the handler and it is resolved again with the answers. Any removal closes the window; a re-entrant handler asks before clearing its progress.
Owner: `AsksDeathReplacementsFirst`, `EffectResolutionService`.
Why: a rerun repeats every change made before the question.

### Choice completions and costs that kill ask first too (CR 601.2h)
An answer that changes nothing before it removes is completed again from a stored replay (a second removal gets its own window); casts, activations, attack and block costs ask before any cost is paid.
Owner: `PermanentChoiceHandlerService` (`runAskingDeathReplacementsFirst`), `DepartureService` (`askBeforePayment`).
Why: a payment or completion can't stop halfway.

### A sacrifice replacing life loss is asked before any life changes (CR 119.4, 119.5, 120.3a)
Loss, payment, a lowered total and damage to a player all ask Lich's Duel Mastery's sacrifice first: compute every loss, then `applyLifeLosses` / `askBeforeLifeTotalsChange`, or insert the loss as its own step.
Owner: `LifeSupport`; `DepartureService` (`askBeforeEvent`) outside a window.
Why: the first life total that changes closes the rerun window.

## Damage events of a resolution

### Each effect is its own damage event unless declared simultaneous
`SequenceEffect.simultaneous`, `Card.addSimultaneousEffects` and `addSimultaneousTrigger` share one event; a card dealing damage at the same time must use them (`EFFECTS_QUICK_REFERENCE.md`).
Owner: `StackEntry` (`damageEventAt`), `DamageSupport` (`beginDamageEvent`).
Why: it decides how often preventions and triggers apply.

### Simultaneous damage is combined before it is dealt
One event's damage is held and dealt as one total per recipient and source; a handler gets the unmodified amount back for held damage.
Owner: `DamageSupport` (`endOpenDamageEvent`).
Why: a prevention applies once, and no step sees what another dealt.

### Lifelink gains life once per source per damage event (CR 702.15b)
Apply lifelink through `checkSpellLifelink`, never `LifeSupport.applyGainLife` directly.
Owner: `DamageSupport` (`checkSpellLifelink`), `CombatDamageService` (`processLifelink`).
Why: "whenever you gain life" fires once.
