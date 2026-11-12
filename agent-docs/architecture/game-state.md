# Game state

Turn, dungeon, suspend, face-down exile and other state that outlives a resolution; domain events.

## Turns and untapping

### Extra turns do not shift turn order (CR 500.7)
After any number of extra turns, the next normal turn goes to the player after the one who took the last normal turn.
Owner: `GameData.lastNormalTurnPlayerId`, read by `TurnProgressionService.nextPlayerInTurnOrder`.
Why: an extra turn taken by another player would otherwise skip or repeat players.

### An exert-style untap skip belongs to the player who chose it (CR 701.43a)
The skip applies only in the untap step of the controller of the resolution that created it, and expires with that step even if another player controls the permanent.
Owner: `Permanent.skipUntapControllerId`; `UntapStepService.expireControllerStepUntapRestrictions`.
Why: a creature that changed controllers would otherwise stay tapped through the wrong player's untap step, or forever.

## Dungeons

### Venturing follows the arrows (CR 309.5)
The venture marker moves only to a room the current room points to, and the player chooses where the dungeon branches; a room's effect keeps its printed duration (Fungi Cavern's lasts until your next turn).
Owner: `VentureIntoDungeonEffectHandler`.
Why: a wrong room or a shortened duration changes what the dungeon does.

### A dungeon is completed when its last room ability leaves the stack (CR 309.6)
A bottom-room ability completes the dungeon however it leaves the stack — resolved, countered or removed for an illegal target — unless another room ability of that dungeon is still on the stack.
Owner: `TriggerCollectionService.completeDungeonRoomIfReady`.
Why: a countered bottom-room ability would otherwise leave the player stuck in a dungeon that can never be completed.

## Characteristics

### Text changes rewrite printed subtypes before type-changing effects (CR 612.2)
A text change replacing a basic land type or creature type word rewrites the object's own printed subtype in layer 3, before any layer 4 effect.
Owner: `LayerSystemService.applyTextChangesToPrintedLandTypes`.
Why: later type-setting effects must overwrite the rewritten type, not be overwritten by it.

### An Aura's own protection grant does not make it illegal
Attachment legality is checked in a fresh layer pass that leaves out only that Aura's protection grant; other protection still counts, and the cached board is not touched.
Owner: `GameQueryService.hasProtectionFromAuraIgnoringOwnGrant`; `LayerSystemService.beginPassWithoutProtectionFrom`.
Why: an Aura granting protection from its own colour would otherwise fall off the creature it enchants.

### "If you cast it" compares the recorded caster
A permanent records who cast the spell it resolved from, apart from who controls it; `WasCast(true)` compares that player with the ability's controller, `WasCast()` accepts any caster.
Owner: `Permanent.castControllerId`; `ConditionEvaluationService` (`WasCast`).
Why: a permanent that entered under another player's control, or changed controllers, must not count as cast by its new controller.

### +1/+1 counter placements are remembered per placing player
Each placement is recorded under the player who put the counters, stays recorded if the counters are removed, and is cleared when the turn advances; the record is part of the layer fingerprint and is deep-copied for simulations.
Owner: `GameData.permanentsWithPlusOneCountersPutByPlayerThisTurn`, written by `PermanentCounterSupport`.
Why: "a permanent you put a +1/+1 counter on this turn" must not match another player's placements or go stale in the layer cache.

## Exile

### Face-down exile: look and play permissions are separate and bound to a player
Each is granted to the player named by the exiling resolution and does not follow the source's controller; a looked-at card is shown only to that viewer, also when it cannot be cast or its source has left, and leaving exile clears the permission.
Owner: `GameData.exileLookPermissions` and `exilePlayPermissions`; `GameViewProjectionFactory` (`GameStateMessage.lookedAtExileCards`).
Why: hidden information must reach exactly the players the card text names.

### A source that lets its controller look adds viewers as it changes control
For a source such as Bane Alley Broker, each player who gains control of it becomes a viewer of the cards exiled with it and remains one until the card leaves exile, also after the source is gone.
Owner: `GameData.additionalExileLookPermissions`, written by `CreatureControlService`.
Why: a player who has looked at a face-down card keeps that knowledge.

### Suspended spells that exiled themselves keep their countdown (CR 702.62a)
Effects that adjust time counters on suspended cards reach these spells as well as hand-suspended cards, and removing the last counter queues the optional free cast.
Owner: `GameData.suspendedSpellExiles`; `RemoveSuspendCounterFromExiledSpellEffectHandler`.
Why: a time-counter effect that missed them would leave the spell in exile for the wrong number of turns.

## Domain events

### Events observe state; they never own it
`GameData` is authoritative and not event-sourced; facts are recorded under the game monitor inside a coordinator-owned action and dispatched after it is released, and subscribers never mutate game state.
Owner: `GameMutationCoordinator`. Contract, outer boundaries and build invariants: `agent-docs/DOMAIN_EVENTS.md`.
Why: a subscriber that writes state, or a fact sent outside an action, breaks ordering and thread safety.

### Hidden information is private unless an audience says otherwise
A fact with no audience is internal-only, simulation batches reach no subscriber, and only `GameMessageTransport` and `GameSessionTransportAdapter` may depend on `SessionManager`.
Owner: `GameEventDispatcher`, `GameEventProjectionSubscriber`; see `agent-docs/DOMAIN_EVENTS.md`.
Why: a leaked hand or library card, or a simulated action sent to a client, cannot be taken back.

## Unverified

The code contradicts this paragraph: `StaticOrbUntap` is only ever created with an empty `excludedIds`, and its Javadoc says one choice must satisfy every binding cap, so there are no "subsequent choices". Original text:

Overlapping untap caps collect each applicable filter before untapping. StaticOrbUntap retains the remaining restrictions and permanent IDs excluded by earlier choices; subsequent choices operate on the still-permitted candidates. The untap action executes once after every binding cap has been satisfied, preserving unrelated permanents' normal untaps.
