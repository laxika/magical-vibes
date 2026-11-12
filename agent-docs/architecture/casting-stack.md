# Casting and the stack

Casting paths, costs, stack object identity, spell copies, what a stack entry retains.

## Casting paths

### One wire-to-engine cast dispatch
`PlayCardRequest` becomes `GameService` cast calls in one class only, shared by backend and AI.
Owner: `PlayCardRequestDispatchService`.
Why: a second copy of the dispatch drops fields, and with them cost payments.

### One cast path, several source zones (CR 601.2a, 400.3, 110.2)
Cast and land-play code reads its card through `GameData.castingSourceCards`, never `playerHands`. A spell from another player's hand or library carries `StackEntry.ownerIdOverride`; a permanent entering that way is tracked as non-owner-controlled.
Owner: `SpellCastingService` (`playFromHandOf`), `StackResolutionService.keepWithNonOwnerController`.
Why: the card goes to its owner's zones while the permanent stays with its controller.

### A library-top permission covers its own library
An own-library permission never authorizes another library's top card; a request naming a library (`libraryOwnerId`) must name an open one.
Owner: `CastingPermissionService` (`findLibraryTopOwners`, `resolveLibraryTopOwner`).
Why: Future Sight must not play an opponent's card.

### Library-top Adventure and face-down casts (CR 716.3a, 716.3d)
Permissions, timing, targets and cost use the characteristics the spell is cast with; the physical card goes on the stack.
Owner: `SpellCastingService.playCardFromLibraryTop`.
Why: Johann, Apprentice Sorcerer allows a creature card's Adventure; no free alternative replaces a face-down cost.

### Mutate keeps the permanent (CR 702.140b, 702.140c, 731.2b, 731.3)
A legal merge keeps the `Permanent` object and fires mutate, not entry, triggers; an illegal target resolves as a creature spell; one permanent departs, every component moves.
Owner: `MutationSupport`, `Permanent.mutatedComponentCards`.
Why: a re-created permanent loses counters, attachments and combat state.

## Costs

### Validate everything before paying anything (CR 601.2b, 601.2h)
Every cast path validates all additional costs before the first payment and rejects a cost it cannot carry; no check follows a `pay*` call. A new `CostEffect` is classified in `HANDLED_SPELL_COST_TYPES` or `CostEffectClassificationTest`.
Owner: `AdditionalSpellCostService`, `SpellCastingService`.
Why: payments are not rewound, so a late throw leaks what was paid.

### Cost sacrifices ask first and leave together (CR 616.1, 603.10a)
Permanents chosen for sacrifice costs stay on the battlefield until every cost choice is made; their death-replacement questions are asked before anything is paid, then they leave as one batch.
Owner: `ActivatedAbilityExecutionService.completeActivationAfterCosts`, `AbilityActivationService.sacrificeChosenCostPermanents`, `SpellCastingService.askCastCostSacrificesFirst`.
Why: a mid-payment question cannot be undone; separate sacrifices miss each other's dies triggers.

### Sacrificed-to-activate excludes mana abilities
`StackEntry.sacrificedCardIds` holds the chosen permanents, the source and the granting Equipment only.
Owner: `ActivatedAbilityExecutionService`.
Why: Ashnod the Uncaring's ruling: a Treasure sacrificed for mana doesn't count.

### A queued exile cast waits for its discard
The spell stays off the stack (`DiscardFollowUp.pendingSpellCast`) until its mandatory discard is paid.
Owner: `CardChoiceHandlerService`.
Why: no spell sits on the stack with an unpaid cost.

## Stack objects

### Stack object identity
A spell is its card id; each ability has its own `getTargetableId()`. Duplicate a real trigger with `copyForNewStackObject()`.
Owner: `StackEntry`.
Why: two abilities of one source are separately targetable.

### Spell copies choose targets before entering the stack (CR 707.10c)
A copy with a retarget prompt waits in `GameData.stagedSpellCopies`; copy and becomes-the-target triggers fire only when it is committed with its final targets. Ability copies are not staged.
Owner: `CopySupport` (`addCopyToStack`, `commitDecidedStagedCopies`).
Why: ward sees only the final targets.

## What a stack entry retains

### The source's last-known information (CR 608.2h)
`sourcePermanentSnapshot` is refreshed as the source leaves, with its attachments, combat state and damage history.
Owner: `StackEntry`, `PermanentRemovalService`.
Why: an ability of a sacrificed source still reads them.

### Per-resolution facts live on the entry
Removed permanents' controllers, cards milled as a cost, the mana-payment player and the power-cost revealed card are stored on the entry and copied with it. Connive and Channel Harm choices continue the same resolution.
Owner: `StackEntry`.
Why: a later choice or copy must not read another activation's state or add a stack object.

## Unverified

Explicit graveyard targets are retained on the spell entry before paying delve costs.
