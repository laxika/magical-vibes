#!/usr/bin/env python3
"""agent-docs changes of runs 17z17c and 17z17d (effect-slot-refactor.md step 15).

17z17c: one activation collector (TriggerCollectionService.collectActivationTriggers) reads every "whenever [a
player] activates an ability" ability once the activated ability is on the stack and its costs are paid, with one
record whatever its effects are. 17z17d: delayed actions carry the resolution that scheduled them; Tawnos's
Coffin's return is a delayed trigger per resolution and ReturnCardsExiledWithSourceOnUntapEffect is gone.

    scripts/step15-run17z17cd-docs.py

Idempotent; fails if an edit does not apply.
"""
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent / "agent-docs"

ACTIVATION_OLD = (
    'and so is every activation trigger\'s "copy that ability" (Rings of Brighthearth, Illusionist\'s Bracers), '
    'behind the "you may" the card prints: an activation ability with that effect is collected once the activated '
    'ability is on the stack, its `AbilityTrigger.withStackEntryFilter` and `withPayment` judged then, and its '
    'entry has that ability\'s stack entry as its triggering spell, the `ActivatedAbility` as '
    '`triggeringActivatedAbility` and the life paid to activate it as its event value.')
ACTIVATION_NEW = (
    'and so is every activation trigger\'s "copy that ability" (Rings of Brighthearth, Illusionist\'s Bracers), '
    'behind the "you may" the card prints. Every "whenever [a player] activates an ability" ability is collected '
    'by one collector, `TriggerCollectionService.collectActivationTriggers(gameData, Activation)`, called once per '
    'activation when the activated ability is on the stack and its costs are paid (CR 602.2b, CR 601.2i; a mana '
    'ability\'s entries wait in `pendingManaAbilityTriggers`), whatever the ability\'s effects are: it matches the '
    'trigger\'s event (any ability, not a mana ability, exhaust, embalm or eternalize) and player, judges '
    '`AbilityTrigger.kind`, `source` (against the activated permanent as it last existed; an ability of a card in '
    'a hand, graveyard, exile or the command zone has none, CR 109.2; an Aura or Equipment whose permanent left '
    'paying the costs is attached to nothing), `withStackEntryFilter` and `withPayment`, the limit and the '
    'conditions, and builds one entry: that ability\'s stack entry as its triggering spell, the `ActivatedAbility` '
    'as `triggeringActivatedAbility`, the activated permanent and its controller as the triggering permanent, and '
    'the life paid to activate it as its event value. An ability that chooses a permanent or any target is asked '
    'for it as it goes on the stack. No call site collects a subset, and nothing is collected before the costs are '
    'paid; discarding Circling Vultures is a special action (CR 116.2e) and collects nothing.')

DELAYED_ANCHOR = "`DelayedZoneChangeTrigger` registers a one-shot watch on a permanent's identity"
DELAYED_NEW = (
    "A delayed triggered ability belongs to the resolution that created it (CR 603.7a): "
    "`StackResolutionService.resolveTopOfStack` counts the stack objects that begin to resolve "
    "(`GameData.resolutionOrdinal`, copied for simulations and kept across a turn restart) and "
    "`GameData.queueDelayedAction` stamps each `DelayedAction` with it (`DelayedAction.scheduledBy`; "
    "`DelayedPermanentAction`, `ExilePermanentAtNextUpkeep` and `ExileToOwnerGraveyardAtNextUpkeep` keep it as "
    "`schedulingResolution`). A queue that puts several actions on the stack as one ability groups by the source "
    "card, the controller and that resolution (`PermanentRemovalService.processDelayedPermanentActions`, "
    "`StepTriggerService.queueDelayedPermanentExiles`, the two upkeep queues), so one resolution's \"exile them at "
    "the beginning of the next end step\" is one stack object and two resolutions of one source are two. The "
    "counted \"when you next cast / activate ..., copy it\" abilities are an entry per unit. A \"when [source] "
    "leaves the battlefield or becomes untapped\" ability is a `DelayedSourceUntapsOrLeavesTrigger` per resolution "
    "(Giant Oyster; Tawnos's Coffin, whose effect is `ResolvePendingExileReturnEffect`). "
    "`scripts/step15-run17z17d-scan.py` lists every delayed queue and how it groups.\n\n")

EDITS = [
    ("ARCHITECTURE.md", [(ACTIVATION_OLD, ACTIVATION_NEW), (DELAYED_ANCHOR, DELAYED_NEW + DELAYED_ANCHOR)]),
    ("CARD_PATTERNS_PERMANENTS_ARTIFACTS.md", [
        ("`STATIC MayNotUntapDuringUntapStepEffect()` + `Triggers.becomesUntapped() "
         "ReturnCardsExiledWithSourceOnUntapEffect()`; the linkage restores every noted counter kind",
         "`STATIC MayNotUntapDuringUntapStepEffect()`; the exile effect creates the delayed \"when this artifact "
         "leaves the battlefield or becomes untapped, return that exiled card\" ability itself, one "
         "`DelayedSourceUntapsOrLeavesTrigger` per resolution, which restores every noted counter kind"),
        ("`ExileTargetCreatureAndAurasUntilSourceLeavesEffect()` + `Triggers.becomesUntapped()` "
         "`ReturnCardsExiledWithSourceOnUntapEffect()`; the return path restores all counters",
         "`ExileTargetCreatureAndAurasUntilSourceLeavesEffect()` (no printed untap trigger: each resolution "
         "creates its own delayed return ability); the return path restores all counters"),
        ("Collected by `TriggerCollectionService.checkControllerActivatesNonManaAbilityTriggers` after the ability "
         "is on the stack, the activated ability its triggering object",
         "Collected by `TriggerCollectionService.collectActivationTriggers` after the ability is on the stack, "
         "the activated ability its triggering object"),
    ]),
    ("EFFECTS_INDEX.md", [
        ("| `ReturnCardsExiledWithSourceOnUntapEffect` | `()` | returns the linked cards exiled with Tawnos's Coffin "
         "as its untap trigger fired (the stack entry's triggering cards) |\n", ""),
        ("| `ReturnCardsExiledWithSourceOnUntapEffect` | `()` | `Triggers.becomesUntapped()` source event: drains all "
         "`PendingExileReturn` entries linked to the source while the source remains on the battlefield. Used by "
         "Tawnos's Coffin |\n",
         "| `ReturnCardsExiledWithSourceOnUntapEffect` | (gone) | **Gone** (effect-slot-refactor.md step 15, run "
         "17z17d): `ExileTargetCreatureAndAurasUntilSourceLeavesEffect` creates the delayed \"when this artifact "
         "leaves the battlefield or becomes untapped, return that exiled card\" ability itself, one per resolution; "
         "Tawnos's Coffin prints no untap trigger |\n"),
        ("the activation collectors find it there and judge the trigger's `withStackEntryFilter` and `withPayment` "
         "once the ability is on the stack |",
         "the one activation collector (`TriggerCollectionService.collectActivationTriggers`) judges the trigger's "
         "`withStackEntryFilter` and `withPayment` once the ability is on the stack and records the activation the "
         "same way for an ability with any other effect |"),
    ]),
    ("EFFECTS_QUICK_REFERENCE.md", [
        ("- `ReturnCardsExiledWithSourceOnUntapEffect()` — return the linked cards exiled with the source as its "
         "untap trigger fired (the stack entry's triggering cards).\n", ""),
        ("- `ReturnCardsExiledWithSourceOnUntapEffect()` — `Triggers.becomesUntapped()` payload that drains the "
         "source-linked exile returns while the source remains on the battlefield; used with "
         "`ExileTargetCreatureAndAurasUntilSourceLeavesEffect()` for Tawnos's Coffin.\n", ""),
    ]),
    ("ORACLE_TEXT_EFFECT_MAP.md", [
        ("| `ExileTargetCreatureAndAurasUntilSourceLeavesEffect()` + `ReturnCardsExiledWithSourceOnUntapEffect()` | "
         "activated ability + Triggers.becomesUntapped() | Tawnos's Coffin (ME1).",
         "| `ExileTargetCreatureAndAurasUntilSourceLeavesEffect()` | activated ability; the effect creates the "
         "delayed leaves-or-untaps return ability, one per resolution | Tawnos's Coffin (ME1)."),
        ("| activated ability `ExileTargetCreatureAndAurasUntilSourceLeavesEffect()` + `Triggers.becomesUntapped() "
         "ReturnCardsExiledWithSourceOnUntapEffect()`; source-leave linkage returns the same cards | ability + "
         "untap trigger |",
         "| activated ability `ExileTargetCreatureAndAurasUntilSourceLeavesEffect()`, which creates the delayed "
         "return ability (`DelayedSourceUntapsOrLeavesTrigger`) as it resolves; no printed trigger | ability |"),
    ]),
    ("ACTIVATED_ABILITY_GUIDE.md", [
        ("`Triggers.youActivateAbility().withKind(AbilityTrigger.AbilityKind.NINJUTSU)`; the hand-ninjutsu\n"
         "activation path collects that trigger after the activation costs are paid.",
         "`Triggers.youActivateAbility().withKind(AbilityTrigger.AbilityKind.NINJUTSU)`; a ninjutsu activation\n"
         "goes through the one activation collector like any other ability, after its costs are paid, so\n"
         "every \"whenever [a player] activates an ability\" trigger sees it."),
        ("controller's exhaust activations. The trigger is collected before the activated ability resolves.",
         "controller's exhaust activations. The trigger is collected once the exhaust ability is on the stack."),
        ("the graveyard collector scans every card in the activating player's graveyard.",
         "the activation collector reads the cards in every graveyard that have such an ability there."),
    ]),
]


def main():
    failed = False
    for name, edits in EDITS:
        path = ROOT / name
        text = path.read_text(encoding="utf-8")
        changed = False
        for old, new in edits:
            if text.count(old) == 1 and not (new and new in text and old in new):
                text = text.replace(old, new)
                changed = True
            elif new and new in text:
                continue
            elif not new and old not in text:
                continue
            else:
                print(f"FAILED (edit does not apply): {name}: {old[:70]}")
                failed = True
        if changed and not failed:
            path.write_text(text, encoding="utf-8")
            print(f"changed: {name}")
        elif not failed:
            print(f"already done: {name}")
    sys.exit(1 if failed else 0)


if __name__ == "__main__":
    main()
