#!/usr/bin/env python3
"""Step 15, run 17z16c4b: agent-docs follow the accepted option that is no longer put on the stack and exert's
authoring form.

Rewrites the lines of `agent-docs/` that describe the changed behaviour and authoring API: a question asked with a
`PendingMayAbility` that has no may handler is carried out in the resolution that asked (never put on the stack), the
exertion of "you may exert this creature as it attacks" is written `SkipNextUntapEffect.exert()`, and the choice is
made as attackers are declared.

Idempotent; fails if an edit doesn't apply.

Usage: python3 scripts/step15-run17z16c4b-docs.py
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"

OLD_EXERTION = "SkipNextUntapEffect(SELF, null, 1, false, false, true)"

EDITS = {
    "ARCHITECTURE.md": [
        ("An ability accepted through a \"you may\" that was asked before it was on the stack (`PendingMayAbility`, "
         "which threads its own fields) has one record too: `ReflexiveAbilitySupport.mayAbilityRecord(ability, "
         "effects, description, sourcePermanentId, xValuePaid)` is its entry when it chooses nothing, and "
         "`MayAbilityHandlerService` puts it on the stack or routes it around the target and graveyard choices, as "
         "the \"you may\" handlers in `service/effect/mayfx` do.",
         "No triggered ability's \"you may\" is asked before the ability is on the stack, and no accepted answer is "
         "put on the stack as an ability (effect-slot-refactor.md step 15, run 17z16c4b; CR 603.5): a collector "
         "puts the ability on the stack with its `MayEffect`, and `MayEffectHandler` asks as that entry resolves "
         "(`GameData.resolvingMayEffectFromStack`). Every other question asked with a `PendingMayAbility` is one of "
         "a replacement or \"as this enters\" effect, a cost, a pregame or turn-based choice, exert as it attacks, or "
         "an option of a spell or ability that is already resolving (CR 608.2d), and its answer is applied at once: "
         "by the `MayEffectHandlerBean` registered for its payload, or, when it has none, by "
         "`MayAbilityHandlerService.resolveAcceptedInAskingResolution`, which resolves the accepted payload as an "
         "entry of its own (`ReflexiveAbilitySupport.mayAbilityRecord(ability, effects, description, "
         "sourcePermanentId, xValuePaid)`: the question's player, the object or player it named as the "
         "non-targeting target, what it kept of the trigger event) through "
         "`EffectResolutionService.resolveNestedInParkedResolution`. That parks the resolution that asked "
         "(`GameData.parkedOuterResolutions`, copied for simulations with its links) and makes it the pending one "
         "again when the nested entry has finished, also after the nested entry stopped for an answer of its own; "
         "`InputCompletionService` resumes it. So \"that player may search their library\", \"each of that "
         "player's opponents may draw a card\" and \"you may exile all other creatures. If you do, ...\" happen in "
         "the resolution that offers them, in order, and a \"when you do\" among the accepted steps "
         "(`QueueReflexiveAbilityEffect`) is the only thing that goes on the stack. A handler-less question that "
         "has a cost, or whose payload needs a target the question did not name, is an `IllegalStateException`: "
         "write a may handler for the cost, and choose a target as the ability is put on the stack (CR 603.3d). "
         "`scripts/step15-run17z16c4b-scan.py` lists every producer of a `PendingMayAbility` with its verdict and "
         "answer route, and every stack entry the answering code builds; a producer outside the handlers of a "
         "resolving object prints `UNCLASSIFIED` until it is read and explained there."),
        ("The exert decision happens during attack declaration; accepting it immediately exerts the creature\n"
         "and puts the \"when you do\" the card writes after the exertion (`QueueReflexiveAbilityEffect`) on the "
         "stack as one reflexive ability. The exertion itself (`SkipNextUntapEffect` with `controllerStepOnly` on "
         "the source) fires",
         "The exert decision happens during attack declaration (CR 701.43a): the attack collector asks a lone "
         "\"you may\" whose effect says it exerts as it attacks (`CardEffect.exertsAsItAttacks()`, the card's "
         "`SkipNextUntapEffect.exert()`) off the stack, and accepting it immediately exerts the creature\n"
         "and puts the \"when you do\" the card writes after the exertion (`QueueReflexiveAbilityEffect`) on the "
         "stack as one reflexive ability, through the same route as any accepted option with no handler of its own. "
         "The exertion itself (`SkipNextUntapEffect` with `controllerStepOnly` on "
         "the source) fires"),
    ],
    "EFFECTS_QUICK_REFERENCE.md": [
        ("`MayEffect(SequenceEffect.of(new SkipNextUntapEffect(SELF, null, 1, false, false, true), new "
         "QueueReflexiveAbilityEffect(<when-you-do effect>)), prompt)`",
         "`MayEffect(SequenceEffect.of(SkipNextUntapEffect.exert(), new "
         "QueueReflexiveAbilityEffect(<when-you-do effect>)), prompt)`"),
        ("Plain exert with no payoff: `MayEffect(SkipNextUntapEffect(SELF, null, 1, false, false, true))` "
         "(Battlefield Scavenger).",
         "Plain exert with no payoff: `MayEffect(SkipNextUntapEffect.exert(), prompt)` (Battlefield Scavenger). "
         "Always write the exertion as `SkipNextUntapEffect.exert()`: it is what tells the attack collector to ask "
         "as attackers are declared (`CardEffect.exertsAsItAttacks()`) instead of putting a \"you may\" ability on "
         "the stack."),
    ],
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("`Triggers.attacks() MayEffect(SkipNextUntapEffect(SELF, null, 1, false, false, true))` + "
         "`Triggers.youExertCreature()",
         "`Triggers.attacks() MayEffect(SkipNextUntapEffect.exert(), prompt)` + `Triggers.youExertCreature()"),
        ("`MayEffect(SequenceEffect.of(SkipNextUntapEffect(SELF, null, 1, false, false, true), "
         "QueueReflexiveAbilityEffect(SequenceEffect.of(BoostSelfEffect(1,1),",
         "`MayEffect(SequenceEffect.of(SkipNextUntapEffect.exert(), "
         "QueueReflexiveAbilityEffect(SequenceEffect.of(BoostSelfEffect(1,1),"),
        ("(pair with `SkipNextUntapEffect(TapUntapScope.SELF, null, 1, false, false, true)` in a `MayEffect` on "
         "`Triggers.attacks()`)",
         "(pair with `SkipNextUntapEffect.exert()` in a `MayEffect` on `Triggers.attacks()`)"),
    ],
    "CARD_IMPLEMENTATION_PLAYBOOK.md": [
        ("the event condition is checked as the creature attacks, and the exert choice is made as the ability "
         "resolves. Combat Celebrant |",
         "the event condition is checked as the creature attacks, and the exert choice is made then, off the stack "
         "(`SkipNextUntapEffect.exert()`). Combat Celebrant |"),
    ],
    "EFFECTS_INDEX.md": [
        ("exert uses `controllerStepOnly` (as it attacks) or `activationCost` (activated-ability cost, Oasis "
         "Ritualist)",
         "exert as it attacks is `SkipNextUntapEffect.exert()` (the `controllerStepOnly` form; it answers "
         "`exertsAsItAttacks()`), exert as an activated-ability cost uses `activationCost` (Oasis Ritualist)"),
    ],
}


def main():
    changed = []
    for name, edits in EDITS.items():
        path = DOCS / name
        text = path.read_text()
        before = text
        for old, new in edits:
            if old in text:
                if text.count(old) != 1:
                    sys.exit(f"{name}: edit is not unique: {old[:70]!r}")
                text = text.replace(old, new)
            elif new not in text:
                sys.exit(f"{name}: edit does not apply: {old[:70]!r}")
        if text != before:
            path.write_text(text)
            changed.append(name)
    print(f"{len(changed)} files changed" + (": " + ", ".join(changed) if changed else ""))


if __name__ == "__main__":
    main()
