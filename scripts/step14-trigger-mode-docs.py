#!/usr/bin/env python3
"""Effect-slot refactor step 14, run 16r: the docs and skills stop naming `TriggerMode`.

Every triggered ability is one object on the stack (CR 603.3) and every collector groups effects by ability, so one
Oracle ability is one `addTrigger` of all its effects and a separate ability is another `addTrigger`; `TriggerMode`
is gone. The passages that told card agents to pass a mode, or described long-gone modes (`ONCE_PER_BATCH`,
`PER_TOKEN`, `TOKEN_CREATION`), are rewritten to the current form. Running it again changes nothing.

Usage: python3 scripts/step14-trigger-mode-docs.py
"""
import os
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

ONE_ABILITY = ("Each \"when this enters\" ability is one object on the stack (CR 603.3): one Oracle ability is one "
               "`addTrigger` of all its effects, never one `addTrigger` per effect, and a second, separate \"when "
               "this enters\" ability is a second `addTrigger`. When an ability's effects target through different "
               "target groups or none (\"target player mills two cards. You gain 2 life\"), declare each targeted "
               "effect as a local variable, bind it with `target(filter).bindEffect(effect)`, and add every effect "
               "in one `addTrigger(Triggers.enters(ObjectRelation.THIS), mill, new GainLifeEffect(2))` (Watcher of "
               "the Wayside, Gurmag Rakshasa).")

EDITS = {
    ".claude/skills/implement-card/SKILL.md": [
        (" (with `TriggerMode.INDEPENDENT` before the options for an ability of its own: Catharsis)", ""),
        ("The enters collector still puts a card's plain \"when this enters\" abilities on the stack as one object "
         "sharing its targets; a second, separate \"when this enters\" ability that must be its own object with its "
         "own targets passes `TriggerMode.INDEPENDENT` after the trigger, and a `MayEffect` or modal one is always "
         "its own.", ONE_ABILITY),
        ("never one `addTrigger` per effect and never `TriggerMode.INDEPENDENT`:", "never one `addTrigger` per effect:"),
        (", an intervening-if `ConditionalEffect` or `TriggerMode.ONCE_PER_BATCH` around a tap ability's effects",
         " or an intervening-if `ConditionalEffect` around a tap ability's effects"),
        ("A `TriggerMode` (`ONCE_PER_BATCH`, `INDEPENDENT`) goes after the trigger: "
         "`addTrigger(Triggers.youAttack(), TriggerMode.INDEPENDENT, effect)`.",
         "Each combat ability is one object on the stack (CR 603.3): one Oracle ability is one `addTrigger` of all "
         "its effects (mobilize's tokens and their end-step sacrifice are one), a separate ability another."),
    ],
    ".claude/skills/review-card/SKILL.md": [
        (", with any `TriggerMode` after the trigger;", ", one `addTrigger` per Oracle ability;"),
        (" / intervening-if `ConditionalEffect` / `TriggerMode.ONCE_PER_BATCH` wrappers;",
         " / intervening-if `ConditionalEffect` wrappers;"),
    ],
    "agent-docs/EFFECTS_QUICK_REFERENCE.md": [
        ("`addSimultaneousEffects(slot, a, b, …)` (an overload takes a `TriggerMode`) instead",
         "`addSimultaneousEffects(slot, a, b, …)` instead"),
        ("A zone-change or permanent-change ability (every one but \"when this enters\") takes no `TriggerMode`: the "
         "ability rejects one as it is built.",
         "One Oracle ability is one `addTrigger` of all its effects; every collector puts each ability on the stack "
         "as one object."),
        ("Register unrelated attack abilities with `TriggerMode.INDEPENDENT`.",
         "Unrelated attack abilities are separate `addTrigger` calls, each one object on the stack (CR 603.3)."),
    ],
    "agent-docs/TRIGGER_SLOT_TARGETING.md": [
        ("`Triggers.enters(Triggers.TOKEN, ObjectRelation.YOU_CONTROL)` uses `TriggerMode.PER_TOKEN` for abilities "
         "that trigger for each token entering, such as Junk Winder's \"whenever a token you control enters\".",
         "`Triggers.enters(Triggers.TOKEN, ObjectRelation.YOU_CONTROL)` triggers for each token entering, such as "
         "Junk Winder's \"whenever a token you control enters\"."),
    ],
    "agent-docs/ACTIVATED_ABILITY_GUIDE.md": [
        ("Register with `TriggerMode.ONCE_PER_BATCH` for \"whenever one or more lands enter\"",
         "Add it with `TriggerOptions.ONE_OR_MORE` for \"whenever one or more lands enter\""),
        ("A registration with `TriggerMode.ONCE_PER_BATCH` instead fires once per source",
         "An ability with `TriggerOptions.ONE_OR_MORE` instead triggers once per source"),
    ],
    "agent-docs/CARD_IMPLEMENTATION_PLAYBOOK.md": [
        ("  - TriggerMode is on the registration, not the effect — keeps effects pure and reusable\n",
         "  - The multiplicity is the ability's option, not the effect's — keeps effects pure and reusable\n"),
        ("`addTrigger(Triggers.putIntoGraveyardFromAnywhere(Triggers.NONTOKEN_CREATURE, ObjectRelation.ANOTHER_YOU_OWN), "
         "effect, TriggerMode.ONCE_PER_BATCH)`",
         "`addTrigger(new TriggerCondition(Triggers.putIntoGraveyardFromAnywhere(Triggers.NONTOKEN_CREATURE, "
         "ObjectRelation.ANOTHER_YOU_OWN)), TriggerOptions.ONE_OR_MORE, effect)`"),
        ("`addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL), effect, TriggerMode.ONCE_PER_BATCH)`",
         "`addTrigger(new TriggerCondition(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL)), "
         "TriggerOptions.ONE_OR_MORE, effect)`"),
        ("`addTrigger(new TriggerCondition(Triggers.enters(Triggers.TOKEN, ObjectRelation.YOU_CONTROL)), "
         "TriggerOptions.ONE_OR_MORE, effect, TriggerMode.TOKEN_CREATION)`",
         "`addTrigger(new TriggerCondition(Triggers.tokensCreated(filter, ObjectRelation.YOU_CONTROL)), "
         "TriggerOptions.ONE_OR_MORE, effect)`"),
    ],
    "agent-docs/ARCHITECTURE.md": [
        ("Additional ETB abilities on token copies can register\nwith `TriggerMode.INDEPENDENT` to keep their targets "
         "and resolution separate from the copied abilities.",
         "Additional ETB abilities on token copies are abilities\nof their own, so they keep their targets and "
         "resolution separate from the copied abilities (CR 603.3)."),
    ],
}


def main():
    missing = []
    for relative, edits in EDITS.items():
        path = os.path.join(ROOT, relative)
        text = open(path).read()
        for old, new in edits:
            if old in text:
                text = text.replace(old, new)
            elif new not in text:
                missing.append("%s: %s" % (relative, old[:80]))
        open(path, "w").write(text)
    for line in missing:
        print("not found: " + line, file=sys.stderr)
    return 1 if missing else 0


if __name__ == "__main__":
    sys.exit(main())
