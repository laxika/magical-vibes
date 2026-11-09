#!/usr/bin/env python3
"""Step 15, run 17z8: agent-docs for the "choose one that hasn't been chosen" abilities written as their own modal.

`ChooseModeNotYetChosenEffect`, `ChooseModeNotYetChosenThisTurnEffect` and
`ChooseModeNotYetChosenThisTurnOnSpellCastEffect` are gone: a card writes `ChooseOneEffect.notYetChosen(options)`,
`.notYetChosenThisTurn(options)` or `.notChosenDuringLastCombat(options)` (the new `ChooseOneEffect.ModeRestriction`),
judged where the mode is chosen as the ability goes on the stack (CR 603.3c, 700.2b) and recorded on the source
permanent there. Whole-line edits, each found by the line's unique start; the files keep their own encoding. Fails if
a doc or skill still names a removed record.

Idempotent.

Usage: step15-run17z8-docs.py
"""
import pathlib

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"

RESTRICTION = ("A triggered modal's `ChooseOneEffect.ModeRestriction` (`ChooseOneEffect.notYetChosen(options)`, "
               "`.notYetChosenThisTurn(options)`, `.notChosenDuringLastCombat(options)`) is judged where the mode is "
               "chosen as the ability goes on the stack (CR 603.3c): the trigger-time mode choice "
               "(`PermanentChoiceContext.TriggeredModalTrigger` / `UpkeepModalTrigger`, whose `consumeModes` / "
               "`modesResetEachTurn` / `rememberLastChosenMode` it sets) or the stack's mode announcement "
               "(`StackedModalTriggerService`), and the chosen mode is recorded on the source permanent there "
               "(`chosenModeLabels`, `chosenModeLabelsThisTurn`, `chosenMode`), so it counts as chosen even if the "
               "ability is countered; a new object starts over")

# (file, unique line start, replacement line)
LINES = [
    ("ORACLE_TEXT_EFFECT_MAP.md",
     "| \"As Gollum enters, choose odd or even. Whenever an opponent casts a spell with mana value of the chosen quality, choose one that hasn't been chosen",
     "| \"As Gollum enters, choose odd or even. Whenever an opponent casts a spell with mana value of the chosen quality, "
     "choose one that hasn't been chosen — put a +1/+1 counter on Gollum; each opponent loses 2 life and you gain 2 life; "
     "or draw a card\" | `ChooseManaValueParityOnEnterEffect()` + `.withStackEntryFilter(new "
     "StackEntryManaValueParityMatchesSourceChosenParityPredicate())` on the cast trigger with "
     "`ChooseOneEffect.notYetChosen(...)` | STATIC + `Triggers.opponentCastsSpell()` | Gollum, Riddle Master (HOB 70). "
     "The mode is chosen as the ability goes on the stack among those the source permanent hasn't chosen, so each of the "
     "three modes can be chosen only once per object |"),
    ("ORACLE_TEXT_EFFECT_MAP.md",
     "| \"Whenever you cast a noncreature spell, choose one that hasn't been chosen this turn\" |",
     "| \"Whenever you cast a noncreature spell, choose one that hasn't been chosen this turn\" | "
     "`ChooseOneEffect.notYetChosenThisTurn(options)` | `Triggers.youCastSpell().withFilter(new CardNotPredicate(new "
     "CardTypePredicate(CardType.CREATURE)))` | The Vision. The cast collector queues the mode choice as the ability "
     "goes on the stack; the modes the source permanent chose this turn are left out, and the chosen mode's effects "
     "reuse the triggered-modal target/resolution path. |"),
    ("ORACLE_TEXT_EFFECT_MAP.md",
     "| \"When The Fantastic Four enter and whenever you cast a spell with power, toughness, or mana value 4, choose one",
     "| \"When The Fantastic Four enter and whenever you cast a spell with power, toughness, or mana value 4, choose one "
     "that hasn't been chosen this turn\" | `new ChooseOneAtTriggerTimeEffect(ChooseOneEffect.notYetChosenThisTurn(options))` "
     "+ `ChooseOneEffect.notYetChosenThisTurn(options)` | `Triggers.enters(PermanentAllOfPredicate(Triggers.CREATURE, "
     "PermanentIsSourceCardPredicate()), ObjectRelation.YOU_CONTROL)` + `Triggers.youCastSpell().withStackEntryFilter(new "
     "StackEntryManaValuePowerOrToughnessEqualsPredicate(4))` | The Fantastic Four (MSC 741). One ability with two "
     "trigger conditions, so both share the source's record of the modes chosen this turn. The own enters modal keeps "
     "the trigger-time wrapper so it is never declared with the creature spell (its modes don't make the spell modal, "
     "CR 700.2). The stack-entry filter reads the cast-time mana value (with X), printed power or printed toughness. |"),
    ("ORACLE_TEXT_EFFECT_MAP.md",
     "| \"At the beginning of your upkeep, choose one that hasn't been chosen — A / B / C / D\" |",
     "| \"At the beginning of your upkeep, choose one that hasn't been chosen — A / B / C / D\" | "
     "`ChooseOneEffect.notYetChosen(List.of(ChooseOneOption(...), ...))` | Triggers.upkeep(ActivePlayer.YOU) | Demonic "
     "Pact. The mode AND its targets are picked as the ability goes on the stack (`UpkeepModalTrigger` → "
     "`beginChooseModeChoice(triggerTime=true)` → `queueChosenModeUpkeepTrigger` routes the chosen mode into the "
     "any-target / player-target / non-targeting upkeep pipelines). The chosen labels live on "
     "`Permanent.chosenModeLabels`, so each mode is offered once per object, even if its ability was countered. A mode "
     "carries its own `targetFilter` (\"target opponent\"), which overrides the card's. Tests: `handleListChoice` then "
     "the mode's own target answer |"),
    ("ORACLE_TEXT_EFFECT_MAP.md",
     "| \"Whenever this creature deals combat damage to a player, choose one that hasn't been chosen — put two +1/+1 counters",
     "| \"Whenever this creature deals combat damage to a player, choose one that hasn't been chosen — put two +1/+1 "
     "counters on this creature / draw a card / this creature becomes a copy of another target creature you control\" | "
     "`ChooseOneEffect.notYetChosen(List.of(...))` with `PutCountersOnSelfEffect(PLUS_ONE_PLUS_ONE, 2)`, "
     "`DrawCardEffect()`, and `BecomeCopyOfTargetCreaturePermanentlyEffect()` | Triggers.dealsCombatDamageToPlayer() | "
     "Silent Hallcreeper (DSK). The combat-damage collector queues the mode choice as the ability goes on the stack; "
     "the copy mode uses a controlled-creature filter that excludes the source permanent |"),
    ("ORACLE_TEXT_EFFECT_MAP.md",
     "| \"Whenever you discard a card, choose one that hasn't been chosen this turn — A / B / C\" |",
     "| \"Whenever you discard a card, choose one that hasn't been chosen this turn — A / B / C\" | "
     "`ChooseOneEffect.notYetChosenThisTurn(List.of(ChooseOneOption(...), ...))` | Triggers.youDiscardCard() | Monument "
     "to Endurance. The discard collector queues the mode choice as the ability goes on the stack, with the modes the "
     "source permanent chose this turn removed; once all modes have been chosen, later instances are removed with no "
     "effect; the record resets as the next turn begins. |"),
    ("ORACLE_TEXT_EFFECT_MAP.md",
     "| \"Whenever another creature you control enters, choose one that hasn't been chosen this turn — A / B / C\" |",
     "| \"Whenever another creature you control enters, choose one that hasn't been chosen this turn — A / B / C\" | "
     "`ChooseOneEffect.notYetChosenThisTurn(List.of(ChooseOneOption(...), ...))` | Triggers.enters(Triggers.CREATURE, "
     "ObjectRelation.ANOTHER_YOU_CONTROL) | Gala Greeters, Galadriel, Light of Valinor, Lita, Little Orphan Amphibian. "
     "The enter collector queues one mode choice for each instance as it goes on the stack, the modes the source "
     "permanent chose this turn removed (creatures entering together still get different modes); once all modes have "
     "been chosen, later instances are removed with no effect; the record resets as the next turn begins. |"),
    ("EFFECTS_QUICK_REFERENCE.md",
     "- `ChooseModeNotYetChosenEffect(List<ChooseOneOption>)` —",
     "- `ChooseOneEffect.notYetChosen(options)` / `.notYetChosenThisTurn(options)` / `.notChosenDuringLastCombat(options)` "
     "— a triggered \"choose one that hasn't been chosen [this turn]\" / \"that wasn't chosen during your last combat\" "
     "(Demonic Pact, Galadriel, By Elspeth's Command). " + RESTRICTION + ". Per-mode `targetFilter` overrides the card's"),
    ("EFFECTS_QUICK_REFERENCE.md",
     "- `ChooseModeNotYetChosenThisTurnOnSpellCastEffect(CardPredicate, [StackEntryPredicate,] List<ChooseOneOption>)` —",
     "- Gone (effect-slot-refactor.md step 15, run 17z8): `ChooseModeNotYetChosenEffect`, "
     "`ChooseModeNotYetChosenThisTurnEffect`, `ChooseModeNotYetChosenThisTurnOnSpellCastEffect`; a cast trigger's "
     "filters are the trigger's (`withFilter`, `withStackEntryFilter`) and its modal `ChooseOneEffect.notYetChosenThisTurn`"),
    ("EFFECTS_QUICK_REFERENCE.md",
     "- `ChooseModeNotYetChosenThisTurnEffect(List<ChooseOneOption>)` —",
     None),
    ("EFFECTS_INDEX.md",
     "| `ChooseModeNotYetChosenThisTurnOnSpellCastEffect` |",
     None),
    ("EFFECTS_INDEX.md",
     "**Modal triggered abilities with targets** (\"choose one that hasn't been chosen\", Demonic Pact):",
     "**Modal triggered abilities with targets** (\"choose one that hasn't been chosen\", Demonic Pact): use "
     "`ChooseOneEffect.notYetChosen(List<ChooseOneOption>)` in `Triggers.upkeep(ActivePlayer.YOU)` (\"this turn\": "
     "`.notYetChosenThisTurn`; \"that wasn't chosen during your last combat\": `.notChosenDuringLastCombat`). "
     "`StepTriggerService` queues a `PermanentChoiceContext.UpkeepModalTrigger`, drained BEFORE any targeting, which "
     "opens the mode pick with `beginChooseModeChoice(..., triggerTime=true, permanentId)`. "
     "`ChoiceHandlerService.handleTriggerTimeModeChoice` records the label on `Permanent.chosenModeLabels` (so a mode is "
     "offered once per object) and calls `StepTriggerService.queueChosenModeUpkeepTrigger`, which routes the chosen mode "
     "into the ordinary any-target / player-target / non-targeting upkeep pipelines. The mode's own "
     "`ChooseOneOption.targetFilter` rides on `UpkeepAnyTargetTrigger` / `UpkeepPlayerTargetTrigger` and overrides the "
     "card's filter. " + RESTRICTION + "."),
    ("EFFECTS_INDEX.md",
     "`ChooseModeNotYetChosenEffect` may also be the sole effect of a filtered cast trigger.",
     "A restricted `ChooseOneEffect` may also be the sole effect of a filtered cast trigger, an enter, discard, attack or "
     "combat damage trigger, or a step trigger: each collector queues a `TriggeredModalTrigger`, so the mode is chosen "
     "as the ability is put on the stack and the selected mode's effects resolve normally; one put straight on the "
     "stack (Teval's Judgment) is announced by `StackedModalTriggerService`, which reads the restriction too."),
    ("EFFECTS_INDEX.md",
     "| `ChooseModeNotYetChosenThisTurnEffect` |",
     None),
    ("CARD_PATTERNS_PERMANENTS_STATIC.md",
     "| Modal upkeep: \"choose one that hasn't been chosen\" | `d/DemonicPact.java` |",
     "| Modal upkeep: \"choose one that hasn't been chosen\" | `d/DemonicPact.java` | Triggers.upkeep(ActivePlayer.YOU) "
     "`ChooseOneEffect.notYetChosen` of four modes (any-target damage + gain 4 / target opponent discards 2 / draw 2 / "
     "`ControllerLosesGameEffect`) — mode AND its targets picked as the ability goes on the stack, each mode recorded on "
     "`Permanent.chosenModeLabels`, so the fourth upkeep forces whatever is left. The discard mode carries its own "
     "\"target opponent\" `targetFilter`. Test: `handleListChoice` then the mode's own target answer |"),
]

REMOVED = ["ChooseModeNotYetChosenEffect", "ChooseModeNotYetChosenThisTurnEffect",
           "ChooseModeNotYetChosenThisTurnOnSpellCastEffect"]


def read(path):
    return path.read_bytes().decode("utf-8", errors="surrogateescape")


def write(path, text):
    path.write_bytes(text.encode("utf-8", errors="surrogateescape"))


def main():
    by_file = {}
    for name, start, new in LINES:
        by_file.setdefault(name, []).append((start, new))
    for name, edits in by_file.items():
        path = DOCS / name
        lines = read(path).split("\n")
        changed = False
        for start, new in edits:
            hits = [i for i, line in enumerate(lines) if line.startswith(start)]
            if new is not None and new in lines:
                continue
            if len(hits) > 1:
                raise SystemExit(f"{name}: more than one line starts with: {start[:80]}")
            if not hits:
                if new is None:
                    continue
                raise SystemExit(f"{name}: no line starts with: {start[:80]}")
            if new is None:
                del lines[hits[0]]
            else:
                lines[hits[0]] = new
            changed = True
        if changed:
            write(path, "\n".join(lines))
            print(f"{name}: updated")
    stale = []
    for path in list(DOCS.glob("*.md")) + list((ROOT / ".claude/skills").rglob("*.md")):
        for number, line in enumerate(read(path).split("\n"), 1):
            if line.startswith("- Gone"):
                continue
            stale += [f"{path.name}:{number}: {record}" for record in REMOVED if f"`{record}`" in line
                      or f"{record}(" in line]
    if stale:
        raise SystemExit("stale mentions:\n" + "\n".join(stale))


if __name__ == "__main__":
    main()
