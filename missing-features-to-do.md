# Missing features to do

Engine features that don't exist yet, found while fixing card tests. Each entry lists the tests that
fail because the feature is missing, what the rules require, and a ready-to-use prompt that kicks off
the implementation.

To add an entry, copy the template at the bottom, fill it in, and append it above the template.
Remove an entry once the feature has landed and its tests pass.

---

## 53. Ordering token-creation replacements outside the single-blueprint creation path

**Status:** partial · **Found:** 2026-10-25, while finishing entry 35 (token replacement ordering) ·
**Progress:** 2026-10-26, every addition in the single-blueprint path is now ordered;
`DestructionSupport.createTokenForPlayer` now delegates to `applyCreateToken` (ordered, Jinnie Fay asked,
tokens created as one batch), tested in `BeastWithinTest` with Queen Allenal + Parallel Lives; populate now runs a
copy-group `TokenCreationEvent` (`TokenCreationEvent.copies`, `copyOf` = the copied token) and resumes in
`PopulateSupport.createCopiesAfterReplacementOrder`, tested in `RootbornDefensesTest`; `TokenCopySupport` copies of a
single card (no explicit attack targets, no Aura host) now run a copy-group event carrying a `TokenCopyBatch` and resume
in `TokenCopySupport.createCopiesAfterReplacementOrder`, tested in `CacklingCounterpartTest`; copies of several
different cards in the same paths now hold each other card's copies as a copy group (`TokenGroup.copyOf`) of the same
event, tested in `SecondHarvestTest`; copies with explicit attack targets now join the ordered path when each copied card has one attack target (`TokenCopyBatch.attackTargets`, by card identity; added tokens take the first card's target), tested in `MirrorStyleMasterTest`, and the sole-defender branch of `CreateTokenCopyOfTargetPermanentEffectHandler` no longer applies the token multiplier twice; `CreateTokenCopyOfTargetCreatureForTargetPlayerEffectHandler` (Echocasting Symposium) now creates its copy through `TokenCopySupport.createTokenCopies` for the target player (ordered by that player, additions now applied), tested in `EchocastingSymposiumTest`; `CreateTokenCopyOfImprintedCardEffectHandler` (Mimic Vat, Prototype Portal, Soul Foundry, Dollhouse of Horrors, Tawnos) now creates its copy through `TokenCopySupport.createTokenCopies` (its exceptions and riders expressed as a `CreateTokenCopyOfTargetPermanentEffect`), tested in `MimicVatTest`; `ExileDyingCreatureAndCreateSpiritTokenCopyEffectHandler` (Hofri Ghostforge) now creates its Spirit copies through `TokenCopySupport.createTokenCopiesWithCopyException` with a per-copy permanent rider (`TokenCopyBatch.copyRider`) that links each copy (not added tokens) to the exiled card, tested in `HofriGhostforgeTest`; `CreateTokenCopyAndLinkToSourceEffectHandler` (Dance of Many) now creates its copy through `TokenCopySupport.createTokenCopiesWithTokenRider`, whose per-token rider (`TokenCopyBatch.tokenRider`, also carried through Jinnie Fay's choice) links every token the event creates, added Soldiers and Jinnie Fay's Cats/Dogs included (Queen Allenal ruling: the creating effect's instructions apply to the added tokens), with the "when the token leaves, sacrifice" trigger on each token's permanent rather than its copiable card, tested in `DanceOfManyTest`; tapped-and-attacking copies whose attack targets are chosen (`CreateTokenCopyOfTargetPermanentEffect.chooseAttackTarget`: Flamerush Rider, Thousand-Faced Shadow) now go through `TokenCopySupport.createTokenCopiesChoosingAttackTargets` (`TokenCopyBatch.chooseAttackTargets`): the event is ordered first and each creature token, added Soldiers included, then gets its own defending player or planeswalker (CR 508.4) through `PreparedOpponentTokenCopiesAttacking` with a `null` opponent entry, so doubled copies no longer all take the first chosen target, tested in `FlamerushRiderTest`; Redoubled Stormsinger's copies (`CreateTokenCopyOfEachTokenEnteredThisTurnEffectHandler`) now go through `createTokenCopiesChoosingAttackTargets` too (the old pre-count with `getTokenCreationAmount` and the `CreateTokenCopiesOfEnteredThisTurnAttacking` context are gone), tested in `RedoubledStormsingerTest`; Mardu Siegebreaker's per-opponent copies (`CreateTokenCopiesOfExiledCardAttackingOpponentsEffectHandler`) now go through `TokenCopySupport.createTokenCopiesAttackingEachOpponent` (each further opponent's copies key a runtime copy of the card so they hold their own copy group and attack target; a `tokenRider` schedules the "your next end step" sacrifice for every token of the event, added ones included), and noncreature tokens of a tapped-and-attacking copy batch (Worldwalker Helm's Map) now enter tapped but not attacking, tested in `MarduSiegebreakerTest`; `CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffectHandler` (Kharasha Foothills, Shredder, Shadow Master, myriad) now always creates its copies as one ordered event, also with two or more other opponents: the "attacking that opponent" forms through `createTokenCopiesAttackingEachOpponent` (tested in `ShredderShadowMasterTest` with four players), myriad through `TokenCopySupport.createTokenCopiesAttackingEachOpponentOrTheirPlaneswalkers` (per-opponent copy groups with `chooseAttackTargets`, so each creature token, doubled copies included, chooses between its opponent and that opponent's planeswalkers; added tokens choose among the first opponent's), tested in `BroodbirthViperTest`; the unordered `createTokenCopiesChoosingOpponentAttackTargets` is gone; Satya, Aetherflux Genius's copy (`CreateTokenCopyOfTargetPermanentUnlessPayEnergyEqualToManaValueEffectHandler`) now goes through `createTokenCopiesChoosingAttackTargets` with a `tokenRider` that schedules the energy sacrifice for every token of the event (Queen Allenal ruling), and its copy's controller now chooses what it attacks instead of inheriting Satya's target (CR 508.4; 2024-06-07 ruling), tested in `SatyaAetherfluxGeniusTest`; Ashling, the Limitless's copied-card form of `CreateTokenCopyOfSacrificedPermanentEffectHandler` now goes through `createTokenCopiesWithTokenRider` (haste until end of turn and the {W}{U}{B}{R}{G} sacrifice for every token of the event), tested in `AshlingTheLimitlessTest`; Lucy MacLean, Positively Armed's copy (`CreateTokenCopyOfEnteringTokenForTargetPlayerEffectHandler`) now goes through the ordered `TokenCopySupport.createTokenCopies` for the target player, and its "then you draw a card" is spliced as the next step of the trigger's stack entry (`StackEntry.insertEffectsToResolve`) so it waits for the copy even when the creation pauses for the order prompt or Jinnie Fay, tested in `LucyMacLeanPositivelyArmedTest`; the Zombie form of `ExileCreaturesFromGraveyardAndCreateTokensEffectHandler` (Midnight Ritual, Necromancer's Covenant) no longer pre-multiplies with `getTokenCreationAmount` and builds the Zombies itself: it creates them as one event through `PermanentControlSupport.applyCreateToken` (ordered, Jinnie Fay asked, additions such as Queen Allenal's Soldier applied), tested in `MidnightRitualTest`; the copy form of the same handler (Hour of Eternity, Foggy Swamp Visions) no longer goes through `GraveyardReturnSupport.createTokenCopyFromCard`: it creates a copy of each exiled card as one ordered event through `TokenCopySupport.createTokenCopiesWithCopyException` (the copy exception replaces the copy's creature types with the added subtype), and Foggy Swamp Visions' following `SacrificeCreatedPermanentsAtEndStepEffect` still sacrifices every created token, added Soldiers included, after the order prompt, tested in `HourOfEternityTest` and `FoggySwampVisionsTest`; `ExileTargetCardFromGraveyardAndCreateTokenCopyEffectHandler` (The Scarab God, Soul Separator, Séance, Feldon of the Third Path, Tamiyo, Compleated Sage, Gyrus, ...) now creates its copy as one ordered event through `TokenCopySupport` ("except it's a [type]" creature-type replacement via the shared copy exception `TokenCopySupport.replaceCreatureTypes`, also used by Hour of Eternity; tapped-and-attacking copies take the source's attack target, or the entry's attacked target), except for its `exileOtherControlledTokensOfSubtype` form (Abyssal Harvester); Soul Separator's stat-matched Zombie is spliced as the entry's next step, so it waits for the copy and is its own ordered creation, tested in `TheScarabGodTest` and `SoulSeparatorTest`; `ExileRandomPermanentFromGraveyardCreateTappedTokenCopyEffectHandler` (Sin, Spira's Punishment) no longer goes through `GraveyardReturnSupport.createTokenCopyFromCard`: each iteration creates its tapped copy as one ordered event through `TokenCopySupport.createTokenCopies` (`tappedTokenCopy()`, added tokens such as Queen Allenal's Soldier enter tapped too), and the land-card repeat is spliced as the entry's next step (`StackEntry.insertEffectsToResolve`) instead of a loop, so it waits for a paused creation (order prompt, Jinnie Fay) and is its own exile and creation, tested in `SinSpirasPunishmentTest`; Mirror Match's copies (`CreateTokenCopyOfAttackingCreaturesBlockingEffectHandler`) now go through the ordered `TokenCopySupport.createTokenCopiesThen`, whose continuation `TokenCopyBatch.afterEntry` (carried through the Jinnie Fay, attack-target and Aura-attachment contexts) runs once every token of the event has entered, and whose `copyRider` is now given the copied card: each copy, doubled copies included, blocks the attacker it copies, and added tokens (Queen Allenal's Soldier) and Jinnie Fay's substitutes block the first attacker (before, any doubler or addition left every copy unblocked), tested in `MirrorMatchTest`; Human—Time Lord Meta-Crisis (`EachPlayerChoosesOneOrTwoCreaturesCreatesTokenCopyEffectHandler`) now creates each player's copy as that player's own ordered event through `createTokenCopiesThen`, chaining the next player's creation from the previous `afterEntry` and putting the +1/+1 counters (second creature's power measured only then) on every token each player created, added Soldiers included (Queen Allenal ruling), once all have entered; the unordered `createTokenCopiesWithoutReplacementOrder` is gone, tested in `HumanTimeLordMetaCrisisTest`; Abyssal Harvester's `exileOtherControlledTokensOfSubtype` form of `ExileTargetCardFromGraveyardAndCreateTokenCopyEffectHandler` now creates its Nightmare copy through the ordered `createTokenCopiesThen`, whose `afterEntry` exiles the other Nightmare tokens once every token of the event has entered, sparing each token the event created (doubled copies included), tested in `AbyssalHarvesterTest`; God-Pharaoh's Gift's Zombie copy (completed in `GraveyardChoiceHandlerService` after the resolution-time graveyard choice) no longer goes through `GraveyardReturnSupport.createTokenCopyFromCard` (removed with its overloads): it creates the copy as one ordered event through `TokenCopySupport.createTokenCopiesWithCopyException` (4/4 black, `replaceCreatureTypes` to Zombie, haste until end of turn for every token of the event, added Soldiers included), and the paused trigger resumes after the order prompt; `replaceCreatureTypes` now also applies to a transforming copy's back face (CR 707.8a), tested in `GodPharaohsGiftTest`; Aura-host copies no longer go through the unordered `createTokenCopiesAttachedTo` (removed, with the unordered path's `auraHost`): `TokenCopySupport.createTokenCopiesAttachedToThen` creates them as one ordered event whose `copyRider` attaches each Aura copy, doubled ones included, to its copied card's host (an Aura copy that can't enchant its host isn't created, CR 303.4g); Three Dog, Galaxy News DJ's copies for every other attacker are now one event (one runtime copy of the Aura per further host, so Tippy-Toe adds a single Food) instead of one unordered event per attacker, and Stangg, Echo Warrior's Equipment copies are attached in `afterEntry`, with every token of the event, added Maps included, sacrificed at the next end step, tested in `ThreeDogGalaxyNewsDJTest` and `StanggEchoWarriorTest`; Calamity, Galloping Inferno's saddler copies (`CreateTokenCopiesOfSaddledCreatureEffectHandler`) no longer choose one attack target before the creation and let the batch multiply (the unordered `PermanentChoiceContext.CreateTokenCopiesAttacking` and its handler are gone): each copy is one ordered event through `TokenCopySupport.createTokenCopiesChoosingAttackTargetsThen`, so the player orders Queen Allenal against Parallel Lives, then chooses a player or planeswalker for each creature token (CR 508.4), doubled copies and added Soldiers included, every token of the event is sacrificed at the next end step, and "repeat this process once" runs from `afterEntry` after every pause of the first creation (replacement order, Jinnie Fay, attack targets), tested in `CalamityGallopingInfernoTest`; Mirror Match with Jinnie Fay: `createTokenCopiesThen` now takes an `instructionRider` (`TokenCopyBatch.instructionRider`, carried with the per-token copied cards through `ChoiceContext.JinnieFayTokenCopyChoice`) given for every token of the event the card it was created for, so a Cat or Dog substituted for the copy of a later attacker blocks that attacker (Jinnie Fay ruling: anything else the creating effect specifies still applies to her tokens), and one substituted for Queen Allenal's Soldier blocks the first attacker, tested in `MirrorMatchTest`; attacking tokens no longer choose their attack targets before the creation (the pre-counted `PermanentChoiceContext.CreateTokensAttacking` and its handler are gone): `CreateTokensAttackingEffectHandler` (Brimaz, Adeline, Fire Navy Trebuchet, Goblin Traprunner, The Last Ronin's Technique), the tapped-and-attacking forms of `CreateTokenEffectHandler` (Hero of Bladehold), `CreateTokenForTriggeringPlayerEffectHandler` (Invocation of Saint Traft, Najeela) and `PayXManaCreateXTokensEffectHandler` (Tilonalli's Summoner) create the tokens through the ordered `applyCreateToken` and splice a `ChooseAttackTargetsForCreatedTokensEffect` step (`ChooseAttackTargetsForCreatedTokensEffectHandler.createdTokensAttack`) that runs after any pause (order prompt, Jinnie Fay): every creature token of the creation, added Soldiers included, is attacking, its controller chooses each one's player, planeswalker or battle (CR 508.4; no prompt when there is only one), and Fire Navy Trebuchet's end-step sacrifice covers every token of the creation, tested in `FireNavyTrebuchetTest` and `HeroOfBladeholdTest`; `CreateTokenCopyOfSourceEffectHandler` (embalm, eternalize, squad, offspring, Splinter Twin, Nacatl War Pride, land casualty, ...) no longer pre-multiplies with `getTokenCreationAmount` and builds the copies itself: it creates them as one ordered event through `TokenCopySupport.createSourceTokenCopiesThen` (its options mapped onto a `CreateTokenCopyOfTargetPermanentEffect`, the no-mana-cost / starting-loyalty / omitted-ability-and-keyword parts as a copy exception via the new `Card.removeEffectsIf`; tapped-and-attacking copies choose each creature token's attack target, CR 508.4; Furnace-Blessed Conqueror's "put a +1/+1 counter on that token for each ..." runs in `afterEntry` on every token of the event), tested in `SacredCatTest`; encore (`EncoreEffectHandler`, also behind `CreateTokenCopiesOfSourceAttackingOpponentsEffect`) no longer resolves the source handler once per opponent and reads the ids back: its copies for every opponent are one ordered event through `TokenCopySupport.createTokenCopiesForEachOpponent`, and every token of the event, added Soldiers included, gains haste (granted, no longer a copiable keyword, per "They gain haste"), must attack its opponent (added tokens: the first opponent) and is sacrificed at the next end step, tested in `KangeesLieutenantTest`; populate (`PopulateSupport.createCopy`) no longer runs its own copy event: it creates its copy through the ordered `TokenCopySupport.createTokenCopies` (Ghired, Conclave Exile's "enters tapped and attacking" as `tappedTokenCopy()`, made attacking by the following `MakeCreatedPermanentsAttackingEffect`), so Jinnie Fay is asked about the copy and the event's added tokens, and the copy and added tokens such as Queen Allenal's Soldier enter as one batch (`PopulateSupport.createCopiesAfterReplacementOrder` and the batch-less `TokenCreationEvent.copies` are gone), tested in `RootbornDefensesTest`; Jinnie Fay's replacement is no longer asked before a blueprint creation (`PermanentControlSupport.applyCreateToken`) is ordered: it is the non-pure `JinnieFayTokenReplacements` option of the `TokenCreationEvent`, ordered with the count replacements and additions, whose `apply` asks Cat, Dog or original tokens (`ChoiceContext.JinnieFayTokenChoice` now holds the event) and suspends; the answer turns every token the event creates as it stands into "that many" Cats or Dogs (`TokenCreationReplacementSupport.jinnieFayEvent`) and continues the order, so Tippy-Toe's Food or a doubler applied first is replaced too, and additions applied after (or becoming applicable, such as Queen Allenal's Soldier once Treasures become Cats, CR 616.1f) see the Cats or Dogs (`applyCreateTokenAfterJinnieFayChoice` is gone), tested in `JinnieFayJetmirsSecondTest`

**Failing tests:**
- None yet. Write one per remaining path, e.g. Jinnie Fay with Tippy-Toe: the player must be able to apply
  Tippy-Toe before Jinnie Fay replaces the tokens.

**What the rules require.** CR 616.1 / 616.1e: the player creating the tokens chooses the order of every
replacement that modifies the token creation event. `PermanentControlSupport.applyCreateToken` (the
single-blueprint path) runs a `TokenCreationEvent` where count replacements (`TokenCreationReplacements`)
and every addition (`TokenAdditionReplacements`: Soldier, Frog, Squirrels, Map, Thopter, Mutagen, Tippy-Toe
Food, solved-Case Clue, Bilbo Treasure, Academy Manufactor's "one of each") are ordered. Still fixed, with
no prompt:
- Token-copy events (`TokenCopySupport`, `copyBatch` set) still ask Jinnie Fay's choice after the event is ordered
  (`ChoiceContext.JinnieFayTokenCopyChoice`), so the player can't apply her before Tippy-Toe or a doubler there;
  `JinnieFayTokenReplacements` skips those events.
- `replaceTokenSubtypeIfApplicable` / `replaceCreatureTokenIfApplicable` (Divine Visitation and similar) apply
  per token after the ordered event.

**Why it's large.** Each remaining site creates tokens mid-effect and would need to suspend on the order
prompt and resume (as `ChoiceHandlerService` does for `TokenCreationEvent`). Token copies have no
`CreateTokenEffect` blueprint; `TokenCreationEvent.copies` gives the event a copy-group form, and copy
`TokenGroup`s (`TokenGroup.copies`) hold the copies of further cards. Attacking copies would need each copy group to
carry its source's attack target (and decide whether added tokens such as Queen's Soldier enter attacking).

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 53: route the remaining token creation paths
(TokenCopySupport's batches are done (Calamity via createTokenCopiesChoosingAttackTargetsThen, Aura-host copies via createTokenCopiesAttachedToThen; the
id-returning batches are done: Mirror Match and Meta-Crisis via createTokenCopiesThen / TokenCopyBatch.afterEntry, Jinnie Fay substitutes via TokenCopyBatch.instructionRider; the pre-chosen-target
attacking-token path is done via the spliced ChooseAttackTargetsForCreatedTokensEffect; source copies via createSourceTokenCopiesThen and encore via createTokenCopiesForEachOpponent are done), and the token copy
handlers that call getTokenCreationAmount; ExileTargetCardFromGraveyardAndCreateTokenCopyEffectHandler is done (Abyssal Harvester's exile-other-tokens form via createTokenCopiesThen's afterEntry); God-Pharaoh's Gift is done (GraveyardReturnSupport.createTokenCopyFromCard is gone); Sin, Spira's Punishment's random-exile copies are done (land repeat spliced via StackEntry.insertEffectsToResolve); Midnight Ritual's Zombies now use applyCreateToken and Hour of Eternity's /
Foggy Swamp Visions' copies createTokenCopiesWithCopyException; populate and TokenCopySupport batches without an Aura host whose copied
cards each have at most one attack target are done, see TokenCopyBatch.attackTargets, chosen-attacker copies via TokenCopyBatch.chooseAttackTargets,
PopulateSupport, TokenCopySupport.createCopiesAfterReplacementOrder, TokenCreationEvent.copies, TokenGroup.copies
and TokenCopyBatch, whose copyRider handles per-copy permanent set-up such as links and whose tokenRider
sets up every token of the event, added tokens included; Hofri, Dance of Many, Redoubled Stormsinger and
Mardu Siegebreaker (createTokenCopiesAttackingEachOpponent), myriad / per-other-opponent attacking copies
(createTokenCopiesAttackingEachOpponentOrTheirPlaneswalkers), Satya, Ashling's copied-card form and Lucy MacLean
(follow-up draw spliced via StackEntry.insertEffectsToResolve) are done; Jinnie Fay is ordered in blueprint
events via JinnieFayTokenReplacements, token-copy events still ask her after their order)
through TokenCreationEvent and ReplacementOrderService, as PermanentControlSupport.applyCreateToken does
with TokenCreationReplacements and TokenAdditionReplacements. Write the tests first. Don't run the full
test suite; ask me. Don't commit until I say so.
```

---

## 54. Nafs Asp's pay-before-draw special action has no client action, and the AI never pays

**Status:** open · **Found:** 2026-10-25, while finishing entry 36 (Quenchable Fire's special action)

**Failing tests:**
- None yet. Write one: a human player damaged by Nafs Asp (or Glass Asp) is offered the payment in
  `GameStateMessage.delayedTriggerPayments` while holding priority before their draw step, auto-pass stops
  for it, and paying clears `priorityPassedBy`.

**What the rules require.** CR 116.2c: paying "unless they pay {1} before that draw step" is a special action
the player may take any time they have priority until the step begins; CR 117.4: an earlier pass no longer
counts as passing in succession after an action. Entry 36 added the generic delayed-trigger payment
(`GameActionAvailabilityService.getDelayedTriggerPaymentsOwedBy`, `AbilityActivationService.payDelayedTriggerCost`,
`PAY_DELAYED_TRIGGER_COST`, a side-panel button and an auto-pass stop) for Quenchable Fire and Sabertooth
Cobra. Nafs Asp and Glass Asp (`LoseLifeAtNextDrawStepUnlessPays`) still pay only through
`GameService.payDrawStepLifeLoss`, which no client message reaches, auto-pass doesn't stop for, and which
keeps `priorityPassedBy` (`AbilityActivationServiceTest.drawStepPaymentRemovesOnlyOneIdenticalObligation`
asserts that). The AI never makes any of these payments.

**Why it's large.** Moving the Asps onto the generic payment changes their payment path and the service
tests that pin it; teaching the AI when paying is worth it needs a planner hook for special actions.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 54: add LoseLifeAtNextDrawStepUnlessPays (Nafs Asp, Glass Asp)
to GameActionAvailabilityService.getDelayedTriggerPaymentsOwedBy so it goes through
AbilityActivationService.payDelayedTriggerCost and the PAY_DELAYED_TRIGGER_COST client action, retire
payDrawStepLifeLoss, and let the AI pay delayed-trigger costs when it is worth it. Don't run the full test
suite; ask me. Don't commit until I say so.
```

---

## 55. Other "until end of turn" stores under Runed Terror's sequential phases

**Status:** open · **Found:** 2026-10-25, while finishing entry 38 (Runed Terror durations)

**Failing tests:**
- None yet. Write one per store moved, in the style of `RunedTerrorTest.temporaryBoostSurvivesOtherPlayersCleanup`
  (e.g. player 2 casts a "can't block this turn" or damage-prevention spell during their phase; it must still
  apply after player 1's cleanup step and end at player 2's).

**What the rules require.** Runed Terror ruling (2024-11-08): "until end of turn" and "this turn" effects end
during the cleanup step of the player who was active when the effect began (CR 514.2 ends them at cleanup).
Entry 38 made this hold for floating continuous effects (`floatingEffectCreatingActivePlayerIds`,
`GameData.expireEndOfTurnFloatingEffectsCreatedDuring`) and for `Permanent` power/toughness modifiers and
granted keywords (`GameData.phaseSequenceModifierShares`, `TemporaryModifierShare`). Every other store that
`TurnCleanupService.resetEndOfTurnModifiers` / `Permanent.resetModifiers` clears (the other per-permanent
flags such as `cantBlockThisTurn`, `removedKeywords`, protections, transient types/colors, base P/T
overrides, damage-prevention and redirection shields, "this turn" player sets and cast/play permissions)
still ends at whichever player's cleanup comes first. A keyword granted by the active player and again by
another player (same keyword on a `Permanent.grantedKeywords` set) is credited only to the first granter.

**Why it's large.** Each store needs the creating active player recorded, or a per-player split like
`TemporaryModifierShare`, and roughly two hundred stores are involved.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 55: extend entry 38's Runed Terror duration handling
(TurnCleanupService.attributeTemporaryModifiersToActivePlayer / phaseSequenceModifierShares and the
floating-effect tagging) to the remaining "until end of turn" / "this turn" stores, so each ends only at the
cleanup step of the player who was active when it began (Runed Terror ruling; CR 514.2). Start with the
most-used ones (Permanent.resetModifiers flags, damage-prevention shields) and add RunedTerrorTest cases for
each. Don't run the full test suite; ask me. Don't commit until I say so.
```

---

## 56. A standalone "any colour" mana activation still holds its non-mana triggers

**Status:** open · **Found:** 2026-10-25, while finishing entry 39 (mana-ability trigger timing)

**Failing tests:**
- None yet. Write one: activate Birds of Paradise (or a land whose mana needs a colour prompt) with no
  payment intent while a non-mana trigger watches it, answer the colour prompt, and expect the trigger on
  the stack.

**What the rules require.** CR 603.3: a triggered ability goes on the stack the next time a player would
receive priority. A mana ability activated with no payment in progress resolves, and its controller then
receives priority (CR 117.3c), so its non-mana triggers belong on the stack then. Since entry 39,
`GameService.finishPriorityManaActivation` does this for activations that finish at once. An activation that
stops for a colour choice parks in `GameData.pendingRevertableManaActivation`. The answer handlers then
complete it into a revertable `ManaActivation`, and `GameService.handleInteractionAnswer` still uses the
old flush, which waits while any revertable activation exists. So with no payment intent, its triggers
wait until the next priority pass or cast.

**Why it's large.** The payment intent is only known on the `tapPermanent`/`activateAbility` call. To
carry it across the colour prompt, `PendingManaActivation` (or a sibling field) has to record "standalone",
and every `completeParkedManaActivation` call site in `ChoiceHandlerService` (about ten) has to commit
instead of recording a revertable activation. Then the answer path needs the standalone flush.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 56: when a mana ability activated with no ManaPaymentIntent
parks for a colour choice (pendingRevertableManaActivation), make its non-mana triggers go on the stack once
the colour is chosen (CR 603.3, CR 117.3c), and make that activation non-revertable, matching what
GameService.finishPriorityManaActivation already does for activations that finish at once. Keep
intent-tagged (payment) activations held and revertable. Add tests next to ManaActivationRevertTest. Don't
run the full test suite; ask me. Don't commit until I say so.
```

---

## 57. Per-unit copy riders for Pyromancer's Goggles and Primal Wellspring

**Status:** open · **Found:** 2026-10-25, while finishing entry 40 (Sunken Palace's per-unit copy rider)

**Failing tests:**
- `PyromancersGogglesTest` "Each mana produced with a mana multiplier grants its own copy"
- `PyromancersGogglesTest` "Spending Goggles mana on a non-red spell cannot copy a later red spell"
- `PyromancersGogglesTest` "Only Goggles mana actually spent on a spell creates copies"

**What the rules require.** CR 106.6 / 106.6a: "When that mana is spent to cast a red instant or
sorcery spell, copy that spell" is a delayed trigger on the mana itself, one per unit produced. The
engine still arms these per player (`GameData.pendingNextRedInstantSorceryCopyCount`,
`pendingNextInstantSorceryCopyCount`, set in `AnyColorManaChoiceSupport`), so the next matching spell is
copied whatever mana paid for it. Sunken Palace now uses a pool tag instead
(`ManaPool.addCopyGrantingMana` / `takeSpentCopyGrantingMana`, claimed in
`TriggerCollectionService.checkSpellCastTriggers`, discarded at the end of every action by
`GameData.discardUnclaimedSpentManaRiders`). Goggles and Wellspring need the same tag, with the spell
filter (red instant/sorcery, instant/sorcery) checked when the spent units are claimed: units spent on a
spell that doesn't match are spent without effect. Also: Sunken Palace's mana that is added only after a
prompt (a land-mana replacement order or color choice) is not tagged yet (`tagCopyGrantingMana` in
`ActivatedAbilityExecutionService` runs only when no mana choice is pending).

**Why it's large.** Medium: two more tags through every `ManaPool` copy/merge/convert/drain path, and the
any-color choice handlers must tag the chosen color.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 57: replace the per-player pendingNextRedInstantSorceryCopyCount /
pendingNextInstantSorceryCopyCount arming for Pyromancer's Goggles and Primal Wellspring with per-unit
ManaPool tags like Sunken Palace's copyGrantingMana (CR 106.6a), so only the matching spell the tagged
mana actually pays for is copied, once per unit. Make the failing PyromancersGogglesTest cases pass and
add Primal Wellspring tests. Don't run the full test suite; ask me. Don't commit until I say so.
```

---

## 58. Opponent-chosen-target spells cast from another player's hand (Sen Triplets)

**Status:** open · **Found:** 2026-10-25, while finishing entry 41 (casting from another player's hand)

**Failing tests:** none yet. Casting such a spell from another player's hand is refused with an
`IllegalStateException`.

**What the rules require.** Sen Triplets' rulings say a card played from that player's hand is played
"just like playing any other card", so a spell whose targets an opponent chooses (a
`GainControlOfTargetEffect` with `opponentChoosesTarget`, or `Card.getOpponentChosenSpellTargetIndices`)
can be cast from it. The engine stops the cast for the opponent's choice
(`PermanentChoiceContext.OpponentChosenSpellTarget(s)`) and resumes it with
`SpellCastingService.playCardAfterOpponentChosenTarget(s)`, which indexes the caster's own hand, so it
would cast the wrong card. `SpellCastingService.rejectOpponentChosenTargetsFromAnotherHand` refuses the
cast instead. Also open: the hard AI's MCTS search (`GameSimulator`) doesn't list plays from another
player's hand; only the base `AiDecisionEngine.tryPlayFromOpponentHand` heuristic plays them.

**Why it's large.** Small to medium: the two prompt contexts need the hand owner, and their resume path
has to run inside `SpellCastingService.playFromHandOf` so the owner gets stamped.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 58: let a spell whose targets an opponent chooses be cast from
another player's hand under Sen Triplets. Carry the hand owner in PermanentChoiceContext.OpponentChosenSpellTarget
and OpponentChosenSpellTargets, resume the cast through SpellCastingService.playFromHandOf, and remove
rejectOpponentChosenTargetsFromAnotherHand. Optionally add foreign-hand plays to GameSimulator's action list.
Add SenTripletsTest coverage. Don't run the full test suite; ask me. Don't commit until I say so.
```

---

## 59. Split-card names in "choose a card name" prompts and chosen-name checks

**Status:** open · **Found:** 2026-10-25, while finishing entry 42 (aftermath cards as split cards)

**Failing tests:** none yet.

**What the rules require.** "Each split card has two names. If an effect instructs a player to choose a
card name and the player wants to choose a split card's name, the player must choose one of those names
and not both. An object has the chosen name if one of its names is the chosen name" (CR 709.4a).
`PlayerInputService.collectCardNamesInGameExcluding` offers each split card's whole name ("Commit //
Memory") as well as its halves, so a player can choose both names at once. Several chosen-name checks
still compare `getName()` to the chosen name instead of using `Card.hasName`, so they miss a split card
(or a Room) named by one half: `GameQueryService` (player protection from the chosen name, damage from
a source with the chosen name, activated abilities of the chosen name),
`AbilityActivationService.validateNotBlockedByNameLock`, `CastingPermissionService.isLandPlayForbiddenByChosenName`,
`IncreaseCastCostForChosenNameSpellsEffectHandler` and `TargetLegalityService`'s
`StackEntrySharesChosenNameWithSourcePredicate`. Entry 42 switched the card-in-zone checks (Conundrum
Sphinx, Mindblaze, Liar's Pendulum, Booby Trap, `CardHasSourceChosenNamePredicate`) to `hasName`.

**Why it's large.** Small to medium. The string-based checks take a name, not a card, so their callers
need to pass the object; and dropping the whole name from the prompt may break tests that choose it.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 59 (CR 709.4a): stop offering a split card's whole name in
"choose a card name" prompts (PlayerInputService.collectCardNamesInGameExcluding and the catalog name
list), and make the remaining chosen-name checks listed in the entry match either half's name through
Card.hasName. Add card tests with an aftermath or split card (e.g. Commit // Memory, Fire // Ice) for
each touched effect. Verify every CR number with the rules MCP. Don't run the full test suite; ask me.
Don't commit until I say so.
```

---

## 60. Stack-static abilities of a copied instant or sorcery (Torrent of Lava copies)

**Status:** open · **Found:** 2026-10-25, while finishing entry 43 (stack-static abilities)

**Failing tests:**
- none yet (a copy of Torrent of Lava grants no ability; untested)

**What the rules require.** CR 113.6: abilities of an instant or sorcery spell function while it is
on the stack, and a copy of a spell copies its abilities (CR 707.2). A copy of Torrent of Lava
should make each creature have "{T}: Prevent the next 1 damage that [the copy] would deal to this
creature this turn" as long as the copy is on the stack. `LayerSystemService.stackStaticSources`
leaves copies out, because the card bakes the original card's id into
`PreventNextDamageToSourceFromCardEffect` and `CopySupport.createCopyCard` shares the effect list,
so a copy's grant would shield against the original spell instead of the copy.

**Why it's large.** The prevention effect needs to refer to "this spell" by the stack object that
grants it (e.g. resolve the source from the granting stack entry at activation) instead of a card
id fixed at construction, and the copy path needs a test with a spell-copy effect.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 60: let a copy of an instant or sorcery on the stack grant
its WrappedStackStaticEffect abilities (CR 113.6, CR 707.2), with Torrent of Lava's granted
prevention keyed to the copy as the damage source. Stop excluding copies in
LayerSystemService.stackStaticSources once the source reference is per stack object. Verify every
CR number with the rules MCP. Don't run the full test suite; ask me. Don't commit until I say so.
```

---

## 61. Four vote handlers never report their votes as finished

**Status:** open · **Found:** 2026-10-25, while finishing entry 45 (optional additional votes)

**Failing tests:** none yet; a Model of Unity or Erestor of the Council test with one of these votes would fail.

**What the rules require.** "Whenever players finish voting" abilities (Model of Unity, Erestor of the
Council) trigger after every vote (CR 701.38a defines voting). `FatefulTempestEffectHandler`,
`SelvalasStampedeEffectHandler`, `WillOfThePlaneswalkersEffectHandler` and
`VoteForInnocentOrGuiltyEffectHandler` never call `VotingFinishedSupport.finishVoting`, and
`ChoiceHandlerService.recordVotingChoiceIfApplicable` doesn't record their votes, so those triggers
never see these votes.

**Why it's large.** Small: record the votes (all four contexts already implement `VoteChoiceContext`)
and call `finishVoting` where each vote ends, then add a Model of Unity test per card.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 61: make Fateful Tempest, Selvala's Stampede, Will of the
Planeswalkers (Path of the Ghosthunter / Path of the Animist) and the innocent-or-guilty vote record
their votes and call VotingFinishedSupport.finishVoting, so "whenever players finish voting" triggers
(Model of Unity, Erestor of the Council) fire for them. Add tests. Verify every CR number with the
rules MCP. Don't run the full test suite; ask me. Don't commit until I say so.
```

---

## 62. Krark's Thumb result choice for interleaved, nested and non-resolution coin flips

**Status:** open · **Found:** 2026-10-25, while finishing entry 46 (Krark's Thumb result choice)

**Failing tests:** none yet; a Krark's Thumb test with mixed mocked flips on any path below would
show the heads result kept without a prompt.

**What the rules require.** Krark's Thumb: "If you would flip a coin, instead flip two coins and
ignore one." Its controller sees the results and chooses which to ignore (a replacement effect,
CR 614.1a; CR 705.3 covers effects that fix a flip's result). Only the first coin-flip event of a
handler marked `ChoosesCoinFlipResultsFirst` asks: `CoinFlipService` stops the handler with
`CoinFlipChoicePending` and `EffectResolutionService` resolves it again. Everywhere else the engine
still keeps heads when the physical flips disagree:
- handlers that change the game between flips: `FlipUntilLoseEffectHandler`,
  `FlipUntilLosePutCountersOnSourceEffectHandler`, `FlipCoinsUntilLoseEffectHandler`,
  `FlipUntilLoseOrStopSupport` (Fiery Gambit), `ManaClashEffectHandler`,
  `EachPlayerFlipsCoinAndTailsSacrificesCreatureEffectHandler`, `FlipCoinForEachOpponentEffectHandler`,
  `FlipCoinForEachBlockingCreatureEffectHandler`, `RiskyMoveEffectHandler`, `GameOfChaosSupport`,
  `AmuletOfQuozAnteEffectHandler`;
- a marked handler's later flips, and flips of effects it dispatches (a branch that flips again);
- flips outside stack resolution: `CoinFlipCostSupport` (costs), `FlipCoinOnEnterEffectHandler`,
  `PermanentChoiceBattlefieldHandlerService`, and `D20RollService`'s coin flips.

**Why it's large.** Loop handlers need their flips journaled across reruns (log and trigger side
effects between flips must not repeat), or a resumable state machine per handler. Cost and entry
paths need their own pause points outside the parked-resolution mechanism.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 62: extend the Krark's Thumb result choice
(ChoiceContext.CoinFlipResultChoice, CoinFlipService.recordedCoinFlips, CoinFlipChoicePending) to
the coin-flip paths listed in the entry: flip-until-lose loops, Mana Clash, per-player and per-blocker
flips, Risky Move, Game of Chaos, Amulet of Quoz, nested flips, coin-flip costs, entry flips and
D20RollService. Keep replayed flips from repeating logs or triggers. Add Krark's Thumb tests with
mocked mixed flips for each. Verify every CR number with the rules MCP. Don't run the full test
suite; ask me. Don't commit until I say so.
```

---

## 63. Declining "up to one target player" in single-target and graveyard-mixed trigger prompts

**Status:** open · **Found:** 2026-10-26, while finishing entry 48 (declining optional player groups)

**Failing tests:**
- None yet; found by reading the code. Write a test first (a single-group "up to one target player"
  trigger whose controller declines).

**What the rules require.** "Up to one target" lets the controller choose zero targets for that
instance of "target" (CR 601.2c, applied to triggered abilities by CR 603.3d). Entry 48 gave the
slot-by-slot walker (`ETBTokenTargetService.processNextETBTokenMultiTargetTrigger`) an empty-answer
decline for optional groups that can target the controller. Two prompts still treat "choose
yourself" as both "decline" and "target myself": the single-target `ETBTokenTargetTrigger`
(`ETBTokenTargetService.controllerChoiceDeclines` only declines when the controller is not a legal
target, so an optional "up to one target player" trigger can't be declined), and walker groups that
mix player targets with graveyard or exiled-card candidates (they skip the `EtbPlayerTargetGroup`
prompt).

**Why it's large.** The single-target prompt is a `PermanentChoice` shared by many trigger types
and the AI; it needs a distinct decline answer (or routing through a multi-choice prompt) and the
tests and AI that answer it must follow.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 63: let the controller decline an optional ("up to one")
trigger target that they could also legally target themselves, in the single-target
ETBTokenTargetTrigger prompt and in walker groups that mix players with graveyard/exiled cards.
Find a card with a single "up to one target player" trigger, write a failing decline test, then
give the prompt a decline answer distinct from choosing the controller (see how entry 48 used
MultiPermanentChoiceContext.EtbPlayerTargetGroup). Update the AI and harness answering code.
Verify every CR number with the rules MCP. Don't run the full test suite; ask me. Don't commit
until I say so.
```

---

## 64. Own library top hidden while a temporary grant opens another player's library

**Status:** open · **Found:** 2026-10-26, while finishing entry 49 (playing from any player's library)

**Failing tests:**
- none yet (write one: Future Sight plus Xanathar, Guild Kingpin's grant on the opponent's library)

**What the rules require.** Each permission works on its own library: Future Sight still lets its
controller play the top card of their own library while Xanathar lets them play the top card of the
target opponent's library, so both cards should be playable. `CastingPermissionService.findLibraryTopOwners`
only adds the player's own library when no temporary grant opens a nonempty library (and no
any-library permission applies), so the own top card is not offered. Its top-library permission checks
(`canCastFromTopOfLibrary`, `canCastFromTopOfLibraryNormally`, `hasAnyLibraryTopPermission`, the free
and life play permissions) are not library-aware either, so simply adding the own library could let a
permission meant for one library authorize the other's card.

**Why it's large.** Every top-library permission check needs a library owner (own-library sources like
Future Sight, Courser of Kruphix, Bolas's Citadel vs. grants on a specific library), through the
projection, the cast and land paths, and the AI's playability query.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 64: make the top-library permission checks in
CastingPermissionService library-aware (own-library permissions only for the player's own library,
grants only for their granted library, any-library permissions for all), then let
findLibraryTopOwners include the player's own library alongside temporarily granted ones. Add a test
with Future Sight plus Xanathar, Guild Kingpin showing both top cards are offered and playable, and
keep WindriddlePalacesTest, XanatharGuildKingpinTest, FutureSightTest, CourserOfKruphixTest and
BolassCitadelTest passing. Verify every CR number with the rules MCP. Don't run the full test suite;
ask me. Don't commit until I say so.
```

---

## 65. Indomitable Might's damage option and attached restrictions in the client listing

**Status:** open · **Found:** 2026-10-26, while finishing entry 51 (Aura/Equipment restrictions as the attachment's own abilities)

**Failing tests:**
- none yet (write one: Indomitable Might on a creature that loses all abilities to Dress Down, then blocked)

**What the rules require.** "Enchanted creature's controller may have it assign its combat damage as
though it weren't blocked" (Indomitable Might, AFC 40) is a static ability of the Aura, not an ability
granted to the creature (CR 113.10: only "has"/"gains" text adds an ability), so it keeps working after
the creature loses its abilities (CR 613.1f). The card wraps `AssignCombatDamageAsThoughUnblockedEffect`
in `GrantEffectEffect(..., ENCHANTED_CREATURE)`, and `CombatDamageService` only reads that effect from the
creature's own and granted effects. Separately, restrictions now modelled with
`AttachedPermanentRestrictionEffect` (Anti-Magic Aura, Artifact Ward, Spectral Shield, Consecrate Land,
Assault Suit, Airtight Alibi) are no longer granted effects, so `GrantedAbilityViewFactory` no longer
lists them on the enchanted/equipped permanent in the client.

**Why it's large.** Small engine change in `CombatDamageService` (read the effect from attached Auras,
honouring Volrath's Curse and the Aura losing its own abilities) plus a client-listing source for
attachment-imposed restrictions; both need tests.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 65. Make CombatDamageService read
AssignCombatDamageAsThoughUnblockedEffect from Auras attached to the attacker (like
GameQueryService.hasAuraWithEffect) and put Indomitable Might's effect directly in STATIC instead of a
GrantEffectEffect; add a Dress Down test. Then have the permanent view list restrictions that attached
Auras/Equipment impose through AttachedPermanentRestrictionEffect. Verify every CR number with the rules
MCP. Don't run the full test suite; ask me. Don't commit until I say so.
```

## 66. Activated-ability indices shift when abilities drop out of the effective list

**Status:** open · **Found:** 2026-10-26, while finishing entry 52 ("loses this ability" listing and copies)

**Failing tests:** none yet.

**What the rules require.** Each activated ability is its own ability; "activate only once each turn" and similar
limits track that ability. The engine keys per-turn and per-game activation counts
(`GameData.activatedAbilityUsesThisTurn`) by the position in `AbilityActivationService.getEffectiveActivatedAbilities`.
That list drops abilities the permanent lost ("loses this ability", losing all non-mana abilities), so a later
ability's position shifts mid-turn and it can inherit or lose another ability's count. Separately, `CardViewFactory`
leaves exile-only abilities out of the client's list while the engine list keeps them, so a client index after an
exile-only ability points one too far.

**Why it's large.** Activation limits, the AI and the client all use the position; keying counts by ability
identity touches every limit check.

**Prompt to start it:**

```
Fix missing-features-to-do.md entry 66: key activation-limit counts by ability rather than effective-list position,
and make the client ability list and the engine's effective list agree on exile-only abilities. Write tests first:
a permanent with a lost ability before a once-per-turn ability, activated before and after the loss.
Don't run the full test suite; ask me. Don't commit until I say so.
```

---

<!--
Template — copy everything between the lines, give it the next number, and append above.

## N. <Short title>

**Status:** open · **Found:** YYYY-MM-DD, <context>

**Failing tests:**
- `<TestClass>.<test>()` (or the display name)

**What the rules require.** <Verified CR text / rulings, and what the engine does instead.>

**Why it's large.** <Scope and regression risk.>

**Prompt to start it:**

```
<A self-contained prompt that can be pasted to kick off the feature.>
```
-->
