# Next to fix: the `Triggering…` classes

Findings from an audit of the 217 classes with `Triggering` in their name (86 effects and their handlers, 15 amounts,
16 conditions, 14 predicates) at `119ace0cba`. The amounts, conditions and predicates are fine. The work is in the
effects and handlers.

Everything here was found by reading code. Nothing was reproduced with a test and no merge was tried, so each item
starts with a check. If a check shows the item is wrong, skip it and say so in the report.

Do the items in order. Item 1 is a rules bug and matters most; items 2 and 3 are deduplication; item 4 is cleanup.

## Ground rules

- Read `AGENTS.md` first. It applies in full: rules accuracy first, verify every CR number with the `rules` MCP
  `get_rule` tool before writing it anywhere, reuse over creation, no banner comments, `@DisplayName` on every test,
  update `agent-docs/` when an effect or predicate is added, renamed or removed.
- Before touching a handler or effect, read `agent-docs/ARCHITECTURE.md` and grep `agent-docs/architecture/triggers.md`
  and `resolution.md` for the class you are changing.
- Never run the full test suite. Run only the test classes of the cards and services you touched, passing fully
  qualified class names, with the `--tests` filters placed after each module's test task.
- Don't commit. Leave the changes in the working tree.
- Find card classes with `find magical-vibes-card -name 'CardName.java'`, never only in the letter module.
- For any card whose behavior is in doubt, check the Oracle text with the Scryfall MCP `get_card` tool and the rulings
  with `get_rulings`.

Paths used below:

- effects: `magical-vibes-domain/src/main/java/com/github/laxika/magicalvibes/model/effect/`
- handlers: `magical-vibes-engine/src/main/java/com/github/laxika/magicalvibes/service/effect/normalfx/`

## 1. Five handlers act on a graveyard card without checking it is still the same object

**Rule.** CR 400.7 (verified): "An object that moves from one zone to another becomes a new object with no memory of,
or relation to, its previous existence." A triggered ability that says "exile it" about a card put into a graveyard
must do nothing if that card left the graveyard and came back before the ability resolves.

**Problem.** The stack entry records which graveyard stay the trigger event was about
(`StackEntry.getTriggeringCardGraveyardEntryVersion()`, compared against `GameData.graveyardEntryVersion(cardId)`).
Most graveyard handlers compare the two before acting. These five look the card up by id only:

| Handler | Cards |
|---|---|
| `ExileTriggeringCreatureFromGraveyardEffectHandler` | Thijarian Witness, Patron of the Vein, Illicit Masquerade, Curse of Clinging Webs |
| `ExileTriggeringCreatureAndTrackWithSourceEffectHandler` | Shelob Dread Weaver, Serpent's Soul-Jar, Purgatory, Koh the Face Stealer, Gardens of Tranquil Repose, Eater of Virtue, Colfenor's Urn, Chaos Shrine's Black Crystal |
| `ExileTriggeringCreatureAndReturnSourceToHandEffectHandler` | Diabolic Servitude |
| `ExileTriggeringCreatureWithHitCounterEffectHandler` | Mari, the Killing Quill |
| `ExileTriggeringAuraFromGraveyardMayCastUntilNextTurnEffectHandler` | Mazzy, Truesword Paladin |

Card names above are read off class names; confirm them with `grep -rlw <EffectName> magical-vibes-card`.

**Check first.** Confirm that the entry of each of these abilities actually carries a graveyard entry version (a
version below zero means "not recorded", and the existing handlers treat that as "don't check"). If the trigger family
that feeds one of them doesn't record a version, recording it is part of the fix. Then write a failing test for one
card (for example Patron of the Vein): the creature dies, and with the trigger on the stack the card leaves the
graveyard and returns to it; the ability must not exile it.

**Fix.**

- Make all five handlers skip when the card is no longer the graveyard object the event named.
- The handlers that already check spell it three ways: `entry.isTriggeringCardIncarnation(...)`
  (`ReturnTriggeringCardToOwnerHandEffectHandler`), a manual `>= 0 && … != …` comparison
  (`ExileTriggeringCardFromGraveyardEffectHandler` and others), and a local `expectedEntryVersion`
  (`ReturnTriggeringCardFromGraveyardToBattlefieldEffectHandler`). Pick the existing helper and use it in the five
  handlers. Converting the handlers that already check is optional; do it only if it is mechanical.
- `ExileTriggeringCreatureAndTrackWithSourceEffectHandler` removes the card straight from `gameData.playerGraveyards`.
  That skips what `PermanentRemovalService.removeCardFromGraveyardByIdForExile` does (clearing the
  put-into-graveyard-this-turn sets and ending floating effects on the card). Route it through that method unless there
  is a reason it must not; if there is one, write it in the handler's Javadoc.
- `ExileTriggeringCreatureAndReturnSourceToHandEffectHandler` calls `removeCardFromGraveyardById`, not the `ForExile`
  variant. Check which is right for an exile and fix it if it is wrong.
- Diabolic Servitude returns itself to hand whether or not the creature card is still there. Keep that: check the
  rulings and assert it in the test.

**Tests.** One test per handler for the "left and came back" case, in the test class of one card that uses it, plus
the existing tests of every card in the table still passing.

## 2. Effects that duplicate each other

For each group: diff the handlers, confirm the differences are only the ones named, merge into one effect with a
parameter (an enum or a boolean, following how sibling effects in the same package do it), update every card that
uses the removed classes, delete the removed effect and handler, and update `agent-docs/`. If the handlers differ in
ways not named here, stop and report what differs before merging.

1. **`ExileTriggeringCreatureFromGraveyardEffect` and `ExileTriggeringCardFromGraveyardEffect`.** Same effect; the
   first lacks the version check (item 1) and logs differently. Keep the `Card` one. Check whether
   `removeCardFromGraveyardById` versus `removeCardFromGraveyardByIdForExile` is a real difference or an oversight.
2. **Marking the triggering spell for exile as it resolves.** `ExileTriggeringSpellEffect`,
   `ExileTriggeringSpellWithDreamCounterEffect`, `ExileTriggeringSpellWithPlotEffect`,
   `ExileTriggeringSpellWithSourceEffect`, `ExileTriggeringSpellWithSuspendCountersOnResolutionEffect`,
   `ExileTriggeringSpellAndReturnToHandAtNextEndStepEffect`. The dream-counter and plot handlers differ by two lines.
   `ExileTriggeringSpellWithSuspendCountersEffect` (exiles at once, takes a follow-up) is a different thing; leave it.
3. **`PutTriggeringCardFromGraveyardOnTopOfLibraryEffect` and `…OnBottomOfLibraryEffect`.** Note the Javadoc says one
   reads the owner's graveyard and the other the controller's; settle which is right from the Oracle text of Mortuary
   and of the bottom-of-library cards before merging.
4. **`PerpetuallyBoostTriggeringCardByPowerEffect`** into `PerpetuallyBoostTriggeringCardEffect` with a power amount,
   if an existing `DynamicAmount` gives the same value at the same moment.
5. **`GrantAdditionalCounterToTriggeringCreatureSpellEffect` and
   `GrantAdditionalPlusOnePlusOneCountersToTriggeringCreatureSpellEffect`.** One takes a counter type and a fixed
   count, the other a dynamic amount and `countedOnEntry`.
6. **`RevealTopCardAndCounterTriggeringSpellIfCardTypeMatchesEffect` and `…IfManaValueMatchesEffect`.** The card-type
   one also offers a free cast; merge only if that stays a clean parameter.
7. **The four battlefield returns.** `ReturnTriggeringCreatureToBattlefieldEffect`,
   `ReturnTriggeringCardFromGraveyardToBattlefieldEffect`, `ReturnTriggeringPermanentToBattlefieldEffect`,
   `ReturnTriggeringLandFromGraveyardToBattlefieldEffect`. Lowest confidence of this list: they differ in whose
   graveyard, whose control, one card versus several, and graveyard versus exile. Merge the creature one into the card
   one if it is a pure subset; leave the others unless the overlap is obvious.
8. **The five conjures.** `ConjureDuplicateOfTriggeringCardIntoHandEffect`,
   `ConjureDuplicateOfTriggeringCreatureToHandEffect`, `ConjureDuplicateOfTriggeringSpellIntoHandEffect`,
   `ConjureDuplicateOfTriggeringPermanentOntoBattlefieldEffect`,
   `ConjureDuplicateOfTriggeringSpellOntoBattlefieldEffect`. Candidates for one effect with "what is duplicated" and
   "where it goes" parameters. Also low confidence; each is used by one card.

## 3. Effects that duplicate a generic effect with a triggering scope

The engine already lets generic effects name the triggering object: `PermanentReference.TRIGGERING`,
`GrantScope.TRIGGERING_PERMANENT`, `BounceScope.TRIGGERING`, and the `TRIGGERING_…` constants of `LoseLifeRecipient`,
`GainLifeRecipient`, `DamageRecipient`, `DiscardRecipient`, `SacrificeRecipient` and `MillRecipient`. For each item
below, check that the generic effect reads the same object at the same moment (live permanent, else last known), then
replace the bespoke effect in its cards and delete it.

1. **`GrantSubtypeToTriggeringPermanentEffect`.** Its handler builds a `GrantSubtypeEffect` and adds it to the
   permanent. Use `GrantSubtypeEffect` with `GrantScope.TRIGGERING_PERMANENT` if that scope gives a permanent-duration
   grant with the same log line; otherwise report why not.
2. **`LoseLifeToOwnerOfTriggeringPermanentEffect`** (Brainstealer Dragon). Candidate:
   `LoseLifeEffect(new TriggeringPermanentManaValue(), LoseLifeRecipient.TRIGGERING_CARD_OWNER)`. The handler has a
   longer owner fallback chain than the recipient may have; compare them before replacing.
3. **`GoadTriggeringCreatureUntilNextTurnEffect`.** The effect record itself is stored in the floating effect, so this
   may need to stay. Check whether a goad effect can take a `PermanentReference`; if it can't cheaply, leave it.
4. **`SacrificeTriggeringPermanentEffect`.** Same fields as `SacrificeSelfEffect`. Candidate: one effect taking a
   `PermanentReference`. It has nine engine references, so check every reader before changing it.
5. **The triggering draws.** `DrawCardForTriggeringPermanentControllerEffect`, `DrawCardForTriggeringPlayerEffect`,
   `DrawCardsForEachOpponentOfTriggeringPlayerEffect`, `EachOpponentOfTriggeringPlayerMayDrawCardEffect`. They exist
   because `DrawCardEffect` has no recipient, unlike life loss, damage, discard and mill. Minimum: merge the first two
   (the controller one is the player one with amount 1, but it prefers the permanent's current controller over the
   recorded one; keep whichever the Oracle texts of their cards require, as a parameter if both are needed). Giving
   `DrawCardEffect` a recipient is a larger change that touches `DrawCardForOwnerEffect`,
   `DrawCardForTargetPlayerEffect`, `DrawCardForDefendingPlayerEffect` and `DrawCardForDamagedPlayerEffect` too; do not
   do it here, describe it as a new entry in `refactors-to-do.md` in that file's existing format.

## 4. Cleanup

1. **Dead condition.** `TriggeringPermanentPowerGreaterThanEachOtherCreature` (in `model/condition/`) is referenced
   only by the sealed `Condition` permits list and `ConditionEvaluationService`. Confirm with a repo-wide grep
   including tests, then delete it and its evaluator branch.
2. **Javadoc that describes the deleted model.** Rewrite to say what the code does now (the stack entry's record, the
   trigger and its matcher), not slots or collectors:
   - `CreateTokenAndAttachTriggeringAuraEffect`: "Placed in the `ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD` slot."
   - `CreateTokenForTriggeringPlayerEffect`: "The trigger collector carries that player…" and "the collector…".
   - `ExileTriggeringCreatureAndReturnSourceToHandEffect`: "The death collector puts the ability on the stack…".
   - `TriggeringPermanentPower`: "the value the collector recorded…".
   - `TriggeringSpellXValue`: "spell-cast collector gives the ability…".
   - `TriggeringCardManaValue`: check the wording around "Warlock Collector" is only the card name (Lorcan, Warlock
     Collector); if so leave it.
3. **Broken link.** `ReturnTriggeringCreatureToBattlefieldEffect` has `{@link ReturnTriggeringCreatureToOwnerHandEffect}`,
   a class that no longer exists. Point it at the current hand-return effect (`ReturnTriggeringCardToOwnerHandEffect`)
   or drop the sentence; if item 2.7 removes this class the link goes with it.
4. **`LoseLifeToOwnerOfTriggeringSpellEffect`.** No card uses it; `ExileTopCardOfRandomOpponentLibraryAndGrantPlayPermissionUntilEndOfTurnEffectHandler`
   builds it. Leave it, but if item 3.2 works out, check whether the same `LoseLifeEffect` form replaces it.

## Report

When done, list: each item as fixed, skipped (with the reason) or changed from this plan (with the reason); the test
classes you ran and their results; the classes deleted; the `agent-docs/` files updated; and anything you found along
the way that looks wrong but is outside this file.
