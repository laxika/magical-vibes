# Casting and the stack

Casting paths, costs, stack object identity and visibility, spell copies, what a stack entry retains.

## Casting paths

### One wire-to-engine cast dispatch
`PlayCardRequest` becomes `GameService` cast calls in one class only, shared by backend and AI.
Owner: `PlayCardRequestDispatchService`.
Why: a second copy of the dispatch drops fields, and with them cost payments.

### One cast path, several source zones (CR 601.2a, 400.3, 110.2)
Cast and land-play code reads its card through `GameData.castingSourceCards`, never `playerHands`. A spell from another player's hand or library carries `StackEntry.ownerIdOverride`; a permanent entering that way is tracked as non-owner-controlled.
Owner: `SpellCastingService` (`playFromHandOf`), `StackResolutionService.keepWithNonOwnerController`.
Why: the card goes to its owner's zones while the permanent stays with its controller.

### Every cast asks the shared "can't" checks (CR 101.2, 601.3, 608.2g)
A casting path asks `isSpellCastingAllowed` (suspend: `isPlayerPreventedFromCasting`); a cast during a resolution asks `canCastDuringResolution(gameData, player, card)` before anything moves (`mayBeginCastDuringResolution` for a whole card offered before a land play or a split half is chosen; `SplitCardCastSupport` judges each half). A "can't cast spells" restriction is never a type set.
Owner: `CastingPermissionService`.
Why: resolution casts skipped Rule of Law, Grand Abolisher and Teferi, Time Raveler; each flow refused alone ends as its decline does.

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

### A face-down card's identity never leaves for other players (CR 708.2, 708.5)
`getCard()` of a spell or permanent stays the real card: text for a player never uses its `getName()` but asks `FaceDownVisibility` (`publicName` for a permanent, `sourceName` for an ability's source, `nameShownTo`, `descriptionShownTo`), and a copy is made by `CopySupport.createCopyCard(StackEntry)`.
Owner: `FaceDownVisibility`; applied by `GameViewProjectionFactory` (stack views) and `GameLogService.append` (card segments; `appendRevealing` for a reveal).
Why: the card's name, or its printing alone, tells an opponent what was cast face down.

### A player's prompt names the face-down objects that player may look at (CR 708.5, 708.6)
Text only one player reads (their prompt, option labels, error) names a face-down spell or permanent they control, or control the controller of, through `FaceDownVisibility.nameShownTo(gameData, entry or permanent, viewerId)`; the log, events, views and any text several players read keep `publicName`/`sourceName`. A permanent is seen by its current controller, not its owner.
Owner: `FaceDownVisibility.maySee`; applied by `InteractionPromptProjectionRegistry` (combat damage assignment) and `DamageContext.nameShownToChooser` (replacement-order labels, which answers match, so both sides use the event's chooser).
Why: a player must be able to tell their own face-down objects apart.

### A face-down spell is judged by its face-down characteristics (CR 708.2, 708.4)
Anything judging a spell on the stack by color, type, subtype, name, mana value, power or text reads `CastingPermissionService.spellAsEffectsSeeIt(entry)`; both stack-entry predicate matchers do, and `getSpellManaValue()` is 0.
Owner: `CastingPermissionService.spellAsEffectsSeeIt`.
Why: the real characteristics are wrong, and which spells can be targeted gives the card away.

### A copy of a face-down permanent copies only its face-down characteristics (CR 707.2, 708.2)
A token or permanent copying one, live or as it last existed, copies `PermanentCopierService.cardToCopy`/`lastKnownCardToCopy`: a face-up nameless card with the status's types and P/T, ward {2} when disguised or cloaked, no printing.
Owner: `PermanentCopierService.faceDownCopiableCard`.
Why: the hidden card's name, abilities or printing would reach the copy and every player who sees it.

### Effect labels of a face-down source name no card (CR 708.5)
A floating effect labelled with the resolving entry's card while that entry's source is a face-down permanent is relabelled as it is added, whichever handler built it.
Owner: `GameData.addFloatingEffect`.
Why: the label reaches every player through modifier lines and prompts.

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
