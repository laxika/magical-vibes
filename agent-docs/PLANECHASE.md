# Planechase

The human 1v1 lobby option uses a shared planar deck of 20 independently created Panopticon cards (MOC 153). Ordinary libraries and sideboards cannot contain planes or phenomena. Additional card implementations belong in separate changes using the normal card implementation workflow.

## State and actions

`GameData.planechase` is null for ordinary games. `PlanechaseState` owns the face-down deck, ordered face-up `PlanarObject` instances, planar controller, per-turn special-action count, latest die result/sequence, and source identities for effects that turn blank planar-die rolls into chaos. Card definitions are frozen. Object identity, timestamps and counters belong to `PlanarObject`, which is never a battlefield `Permanent`. Simulation copies preserve identities and independently copy mutable planar state. Restart returns all planar cards to the shared deck and clears roll costs before the starting-plane procedure.

`PlanechaseService` handles the initial reveal after opening-hand actions, die rolls, chaos, planeswalking and encounters. The starting reveal skips phenomena without triggering abilities. Later reveals trigger the appropriate entry or encounter slot. The planar controller follows the active player. Departing cards lose their object state; multiple departing cards use the existing reorder interaction to choose their order at the bottom of the planar deck.

Rolling the die by special action requires the active player's main phase, priority, no pending decision, and an empty stack. The generic cost counts earlier special-action rolls in that turn. The die action pays immediately and never uses the stack; four sides are blank, one causes chaos, and one creates a source-less, counterable planeswalking triggered ability. Rolls from effects do not increase the special-action count. Spending restrictions are checked by `ManaCost`; ability-only mana cannot pay the special action. Strict affordability and potential mana are kept separate in views. Special-action potential mana includes pooled and untapped Powerstone mana, while ordinary spell estimates retain their spending restrictions.

## Effects and triggers

- `PlaneswalkEffect()` performs the shared-deck transition when it resolves.
- `ChaosEnsuesEffect()` triggers chaos without rolling or increasing a roll cost.
- `RollPlanarDieEffect()` rolls without paying or increasing the special-action cost.
- `BlankPlanarDieRollsCauseChaosEffect()` makes blank planar-die rolls trigger chaos while its source phenomenon remains face up; the source state is cleared when planeswalking away.
- `ReverseTurnOrderEffect()` reverses the authoritative player-order list when it resolves; the phenomenon lifecycle then planeswalks away from Time Distortion.
- `EffectDuration.UNTIL_PLANESWALK` expires on a planeswalk.
- `AllowPlayFromAnyLibraryTopEffect` is a controller-scoped static permission for playing lands and casting spells from any player's library top; it uses the normal costs, timing, and land-play allowance.
- `PLANESWALK_TO_TRIGGERED`, `PLANESWALK_FROM_TRIGGERED`, `CHAOS_TRIGGERED`, and `ENCOUNTER_TRIGGERED` provide planar event slots. Reuse ordinary effects inside them.
- `PlaneswalkIfPlanarSourceHasCountersEffect` is the resolution-time threshold rider for planes whose own counters cause a planeswalk (Aretopolis); it checks the live face-up `PlanarObject` rather than a battlefield permanent.
- Upkeep, draw-step and end-step slots are collected from face-up planar objects for the planar controller. Panopticon reuses `DrawCardEffect` for its arrival, draw-step and chaos abilities.
- Draw-trigger slots on face-up planar objects can watch either the planar controller's or an opponent's actual draw; `RevealEachDrawEffect` uses this for Sea of Sand and preserves the drawing player as trigger context.
- `RevealTopPlanarCardsAndTriggerChaosEffect` reveals from the shared planar deck, queues the revealed chaos abilities, and reuses `LibraryReorder` to put the revealed cards on the deck bottom in the controller's chosen order.
- `RevealPlanarCardsUntilFivePlanesEffect` reveals through five planes, uses `PlanarCardChoice` to select the plane that becomes the next top card, and randomizes all other revealed cards onto the bottom.
- `RevealPlanarCardsUntilTwoPlanesAndPlaneswalkEffect` reveals through two planes, orders any intervening cards onto the bottom, and replaces the face-up planar cards with both revealed planes simultaneously.
- Land-entry slots are collected from face-up planar objects for both the planar controller's lands and opponents' lands; `CreateTokenForTriggeringPlayerEffect` can use the entering land's controller.
- Per-creature attack slots on face-up planar objects are collected during attacker declaration; planar attack effects receive the triggering attacker and attacked player without making the plane a battlefield permanent.
- Targeted planar triggers enter the existing trigger-target interaction with a source snapshot. Phenomena wait while their triggered abilities are on the stack or awaiting target selection. Once those abilities leave the stack, state-based actions planeswalk onward, including after a countered encounter.
- Global creature-leave triggers on face-up planes use the shared leave collector and retain the departing creature's controller for resolution.
- Planar abilities with an optional multi-target group, such as `target(0, 99)`, use the slot-by-slot target walker so the controller can select any number of player targets and the resulting stack entry retains every selected player ID.

`StackEntry.sourcePlanarObject` carries source information through resolution and copying. Planar abilities have independent `getTargetableId()` values even when several abilities share a source. Source-less entries have a nullable card view. Target selection and counter/copy handlers must use the stack targetable identity instead of assuming every entry has a card.

## Continuous and activated abilities

Face-up static effects participate in layer evaluation by timestamp. `StaticEffectContext.sourceId()`, `sourceCard()` and `sourceCounterCount()` work for permanent and planar sources; `source()` is only available for an actual permanent. Shared static handlers use the source controller and the existing target predicates. Controller-scoped turn-action restrictions such as Prahv's are evaluated from both battlefield and face-up planar sources. Never introduce a synthetic battlefield permanent to represent a plane. When adding a plane that needs a new layer operation, replacement effect, restriction or event collector, extend that existing subsystem and add focused behavioral tests for it.

`PlanarAbilityService` supports mana-cost abilities with existing single-target validation and priority/timing checks. Any-player permissions are explicit on `ActivatedAbility`. The command uses the current planar object ID, so stale sources are rejected. Unsupported permanent-specific cost and activation mechanics are rejected before payment; add their proper command-zone semantics alongside the first card that needs them.

## Protocol and frontend

`CreateGameRequest.planechase` defaults to false. Planechase with AI is rejected. `ROLL_PLANAR_DIE` and `ACTIVATE_PLANAR_ABILITY` use the authenticated player's game and existing mutation/error handling. `PlanechaseView` is included in initial and subsequent state messages and contains only face-up card views, public deck size, controller, roll availability/cost and last roll. The face-down order is never projected.

The planar panel renders one or more readable cards, keyboard-accessible previews, counters, roll cost and the latest result. It uses the existing payment flow to let players tap mana before a roll and locks duplicate submissions until a result arrives. The server remains authoritative. State replacement restores the panel after reconnect.

## Focused validation

`PanopticonTest` covers the real card and die timing/payment. `PlanechaseServiceTest` covers synthetic planes/phenomena, command-zone continuous effects, targeting, activation, countering, ordering and projection. `TypeLineParserTest` covers planar types and multiword subtypes. `AutoPassServiceTest` covers retaining priority for a payable roll. `PlanechaseMessageTest` covers authenticated routing, AI rejection and old creation payloads. Frontend panel and targeting service tests cover controls and payment; the panel browser tests check narrow and wide layouts and keyboard focus. Run individual classes/specs only, never the full suite.
