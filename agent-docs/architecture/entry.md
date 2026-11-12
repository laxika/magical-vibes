# Battlefield entry

How permanents enter: batches, simultaneous entry, clone and counter entry replacements, entry pauses.

## Simultaneous entry

### Batch members cannot see each other (CR 614.12)
A caller entering several permanents in one event takes `snapshotEnterTappedTypes` once before the loop and passes the members already placed as `simultaneouslyEntered`. Nothing enforces this at build time.
Owner: `BattlefieldPlacementService` (`hideSimultaneouslyEntered`).
Why: a Bramblewood Paragon entering with a Warrior must not give it a counter.

### Battlefield order is restored, not rebuilt (CR 613.7)
Hidden batch members come back from a per-player snapshot, never by re-adding.
Owner: `BattlefieldPlacementService.restoreHiddenBattlefields`.
Why: list order is the timestamp tiebreak, the trigger order and the wire index.

### Choose everything, then move everything (CR 603.6a, 303.4f)
Multi-card puts collect every pick and every as-enters choice before any card leaves its zone, then enter as one batch; an answered choice resumes the batch, never one member. Auras choose among objects already on the battlefield.
Owner: `BattlefieldEntryBatchSupport`; `GameData.pendingAnyNumberHandPutCardIds`, `PendingGraveyardReturnBatch.heldInGraveyard`.
Why: each entrant's triggers see the others, and zone indices stay stable while choosing.

### Each-player choices are simultaneous (CR 101.4)
Every player chooses in APNAP order before any card moves; picks are only recorded (`deferredDiscardSelections`, `stagedHandExiles`, `heldHandBattlefieldPuts`) and applied as one batch.
Owner: `CardChoiceHandlerService` (`discardDeferredSelections`), `PutCardsOntoBattlefieldTogetherEffectHandler`.
Why: an early mover would leak information and split one event's triggers.

### One graveyard-leave event per queue
`GraveyardReturnSupport.beginGraveyardReturnQueueAsOneEvent` wraps a queue of graveyard choices in one leave batch.
Owner: `GraveyardReturnSupport`.
Why: "one or more cards leave your graveyard" triggers once (Grave Sifter).

## Entry replacements and pauses

### Every entry is offered its copy choice once (CR 614.1c)
An entry reaching `BattlefieldPlacementService.place` unasked pauses for its "enter as a copy" choice and resumes the same request; `Permanent.copyEntryResolved` prevents a second ask. One choice is open at a time, others queue in `GameData.pendingEntryCopyChoices`, and a copy picks only permanents present when it began.
Owner: `CloneService` (`beginEntryCopyChoice`, `beginNextQueuedEntryCopyChoice`), `CloneOperationState`.
Why: a Clone put onto the battlefield by a search still copies, but never a permanent entering with it.

### Unchosen revealed cards wait for the copy choice
A reveal-and-put choice (Collected Company) parks its remainder in `PendingLibraryRevealRemainder` until no copy choice is open or queued.
Owner: `LibraryChoiceHandlerService.resumeLibraryRevealRemainder`.
Why: the copy choice is made as the card enters, before the rest moves.

### As-enters choices pause the request (CR 614.12a)
Riot, unleash, amplify and positioning are answered before physical placement; the paused `BattlefieldEntryRequest` resumes with its spell metadata. Positioning comes after devour.
Owner: `BattlefieldPlacementService.place`, `CreaturePositioningSupport`.
Why: the permanent enters with its counters, haste and slot already set.

### Entry-time effects travel with the request
Reanimation with counters passes `EnterWithCountersEffect` in the `BattlefieldEntryRequest`; echo registration and a static `ChooseOpponentGainsControlOfSourceEffect` (choosing the entering controller) run during placement.
Owner: `BattlefieldPlacementService`.
Why: counters and controller are set before entry triggers are collected.

### Legend rule is a state-based action (CR 704.5j)
`performStateBasedActions` checks every player and defers while another interaction is open; do not add `checkLegendRule` calls to entry paths.
Owner: `StateBasedActionService`, `LegendRuleService`.
Why: token copies and control changes are caught without each path remembering.

## Card state across zones

### Perpetual base power and toughness (CR 613.4b)
`GameData.perpetualCardBasePowerToughness` is keyed by physical card with the effect's timestamp and applies in every zone.
Owner: `LayerSystemService`, `GameQueryService`.
Why: the setter follows the card and orders correctly against other layer 7b effects.

### Stale transform instructions are rejected
`Permanent.transformationSequence` changes with every face change; an ability whose source snapshot holds another value does not transform.
Owner: `TransformSelfEffectHandler`.
Why: two triggers from one face must not flip the permanent twice.

## Unverified

Zimone's Experiment puts its unchosen cards on the bottom before its lands enter, as its Oracle text orders. When a spell resumes after that entry, its returned-permanent reference uses the copied characteristics, with last-known characteristics retained if the permanent leaves before the continuation.

face-down puts pass a prepared face-down permanent, which skips Aura, Siege and naming choices and gets face-down entry triggers. A `LibraryRevealChoice` that puts its chosen cards onto the battlefield returns them to the library and enters them as one batch unless it is cloaked, has an entry replacement or a battlefield-selection follow-up, or a chosen card still sits in some zone; a card that can't enter stays in the library.

Fabricate uses `MayEffect` with counters on the source and token creation as its alternative, chosen during resolution.
