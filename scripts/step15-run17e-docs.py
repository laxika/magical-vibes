#!/usr/bin/env python3
"""Step 15 run 17e of effect-slot-refactor.md: agent-docs edits.

Idempotent. Each edit is an exact substring that must occur once in its file, replaced by its new text (an edit
already applied is found by its new text and left alone). The script fails if a removed form is still named in
agent-docs/ or the skills.
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"
SKILLS = ROOT / ".claude" / "skills"

REFLEXIVE = (
    'Reflexive "you may discard a [card]. When you do, destroy target [permanent] that player controls" on a combat '
    "damage trigger (Dokuchi Silencer): `MayEffect(new DiscardCardThenEffect(filter, new DestroyTargetPermanentEffect("
    "new PermanentAllOfPredicate(List.of(<filter>, new PermanentControlledByTriggeringPlayerPredicate()))), "
    '"a creature card"), prompt)`. The combat damage entry has the player dealt damage as its triggering player, the '
    "discard follow-up carries it, and the reflexive target is chosen against it and rechecked as it resolves")

EDITS = [
    (DOCS / "EFFECTS_INDEX.md",
     "| `DestroyPermanentDamagedPlayerControlsEffect` | `(PermanentPredicate predicate)` | Dokuchi Silencer's reflexive "
     "\"when you do, destroy target … that player controls\" inside `DiscardCardThenEffect`: `DiscardCardThenEffectHandler` "
     "binds the damaged player through `forDamagedPlayer` before the reflexive target is chosen. A triggered ability's own "
     "\"destroy target … that player controls\" is `DestroyTargetPermanentEffect` with "
     "`PermanentControlledByTriggeringPlayerPredicate` |\n",
     ""),
    (DOCS / "ARCHITECTURE.md",
     "Dokuchi Silencer's reflexive \"when you do\" target is the one binding left (`DamagedPlayerControlsTargetEffect`, "
     "bound by `DiscardCardThenEffectHandler`).",
     "A reflexive \"when you do\" ability's \"that player\" (Dokuchi Silencer, CR 603.12) is the triggering player of "
     "the entry that created it: the combat damage collector gives a non-targeted \"you may\" entry the player dealt "
     "damage, `DiscardCardThenEffectHandler` gives its `DiscardFollowUp` the entry's triggering player "
     "(`withTriggeringPlayer`), the reflexive target is chosen and validated with it (the filter context, "
     "`TargetLegalityService.validateActivatedAbilityTargeting`'s triggering-player overload) and its "
     "`PermanentChoiceContext.MayAbilityTriggerTarget` (`withTriggeringPlayer`) gives it to the reflexive entry. A "
     "`SpellTargetTriggerAnyTarget` with a triggering player judges its target filter with it (Chandra's Incinerator's "
     "\"that player controls\" on noncombat damage to an opponent)."),
    (DOCS / "EFFECTS_QUICK_REFERENCE.md",
     "\"Of that player's choice\" is the card's `setCombatTriggerTargetChosenByDefendingPlayer(true)` (Bladegriff "
     "Prototype, Erithizon)\n",
     "\"Of that player's choice\" is the card's `setCombatTriggerTargetChosenByDefendingPlayer(true)` (Bladegriff "
     "Prototype, Erithizon)\n- " + REFLEXIVE + "\n"),
    (DOCS / "TRIGGER_SLOT_TARGETING.md",
     "the stack entry keeps it (`StackEntry.triggeringPlayerId`) for the resolution recheck. No collector binds the "
     "player into an effect | Damage |",
     "the stack entry keeps it (`StackEntry.triggeringPlayerId`) for the resolution recheck. A reflexive \"when you "
     "do\" target (Dokuchi Silencer) is chosen against the triggering player its discard follow-up carries "
     "(`DiscardFollowUp.withTriggeringPlayer`, `MayAbilityTriggerTarget.withTriggeringPlayer`); a noncombat damage "
     "collector that queues a `SpellTargetTriggerAnyTarget` gives it the opponent dealt damage "
     "(`withTriggeringPlayer`, Chandra's Incinerator). No collector binds the player into an effect | Damage |"),
    (DOCS / "PREDICATES_REFERENCE.md",
     "(Trygon Predator, Mordant Dragon, Satyr Firedancer); nothing without one",
     "(Trygon Predator, Mordant Dragon, Satyr Firedancer, Chandra's Incinerator), and of a reflexive \"when you do\" "
     "ability (Dokuchi Silencer); nothing without one"),
    (DOCS / "ORACLE_TEXT_EFFECT_MAP.md",
     "| Triggers.dealsCombatDamageToPlayer() | Spark Mage / Mordant Dragon. The creature is targeted as the trigger "
     "goes on the stack; the may is asked as it resolves |\n",
     "| Triggers.dealsCombatDamageToPlayer() | Spark Mage / Mordant Dragon. The creature is targeted as the trigger "
     "goes on the stack; the may is asked as it resolves |\n"
     "| \"Whenever CARDNAME deals combat damage to a player, you may discard a creature card. When you do, destroy "
     "target creature or planeswalker that player controls\" | `MayEffect(new DiscardCardThenEffect(new "
     "CardTypePredicate(CardType.CREATURE), new DestroyTargetPermanentEffect(allOf(anyOf(creature, planeswalker), new "
     "PermanentControlledByTriggeringPlayerPredicate())), \"a creature card\"), prompt)` | "
     "Triggers.dealsCombatDamageToPlayer() | Dokuchi Silencer. The reflexive target is chosen after the discard "
     "against the player dealt damage |\n"),
]

REMOVED = ["DestroyPermanentDamagedPlayerControlsEffect", "DamagedPlayerControlsTargetEffect",
           "ExileDamagedPlayerControls", "SacrificeDamagedPlayerControls", "DestroyDamagedPlayerControls",
           "TapChosenPermanent`", "TapChosenPermanent)", "TapChosenPermanent "]


def main():
    for path, old, new in EDITS:
        text = path.read_text()
        if new and new in text and (old not in text or old in new):
            print(f"unchanged {path.relative_to(ROOT)}")
            continue
        if not new and old not in text:
            print(f"unchanged {path.relative_to(ROOT)}")
            continue
        if text.count(old) != 1:
            sys.exit(f"{path}: expected one occurrence of {old[:80]!r}")
        path.write_text(text.replace(old, new))
        print(f"edited {path.relative_to(ROOT)}")
    stale = []
    for base in (DOCS, SKILLS):
        if not base.exists():
            continue
        for path in base.rglob("*.md"):
            text = path.read_text()
            stale += [f"{path.relative_to(ROOT)}: {name}" for name in REMOVED if name in text]
    if stale:
        sys.exit("still named:\n" + "\n".join(stale))


if __name__ == "__main__":
    main()
