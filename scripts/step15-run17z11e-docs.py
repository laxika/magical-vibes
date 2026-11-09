#!/usr/bin/env python3
"""Step 15, run 17z11e: the agent-docs rows of the forms the run changed.

Idempotent; fails if a doc or skill still names a removed form outside a "Gone" line. `EFFECT_MERGE_PLAN.md` is
history and is left. Usage: step15-run17z11e-docs.py [repository root]
"""
import pathlib
import sys

ROOT = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"

GONE_EFFECTS = ("DealDamageIfDidntCastSpellThisTurnEffect", "DealDamageToEndStepPlayerIfLifeAtMostEffect")
REMOVED = GONE_EFFECTS + ("EndStepPlayerTargetedEffect", "EndStepPlayerDidntCastCreatureSpell")

EDITS = {
    "ACTIVATED_ABILITY_GUIDE.md": [
        ("The stack entry's `targetId` is `null` unless the effect implements the `EndStepPlayerTargetedEffect` marker, "
         "which makes `StepTriggerService` bake the end-step (active) player into `targetId` — use it for \"at the "
         "beginning of each player's end step, … that player …\" effects (Monsoon's "
         "`TapPlayersPermanentsAndDamageEqualToCountEffect`)",
         "An ability that chooses no target records the end-step (active) player as the entry's triggering player and "
         "non-targeting `targetId` whatever its effects, so \"… that player …\" effects read it (Monsoon's "
         "`TapPlayersPermanentsAndDamageEqualToCountEffect`, `TRIGGERING_PLAYER` recipients); an intervening \"if\" "
         "about that player reads it as the reader's context target (Razor Pendulum's `TargetPlayerLifeAtMost`)"),
        ("| `Triggers.upkeep(ActivePlayer.OPPONENT)` | Each opponent's upkeep |",
         "| `Triggers.upkeep(ActivePlayer.OPPONENT)` | Each opponent's upkeep |\n"
         "| `Triggers.upkeep(ActivePlayer.CHOSEN_PLAYER)` | \"At the beginning of the chosen player's upkeep\": the "
         "player chosen as the source entered (`ChooseOpponentOnEnterEffect`), whoever controls it now; never with no "
         "chosen player (a permanent that becomes a copy has none). The chosen player is the entry's non-targeting "
         "`targetId` (The Rack, Black Vise, Energy Vortex) |"),
    ],
    "EFFECTS_INDEX.md": [
        ("without target selection. Implements `EndStepPlayerTargetedEffect` and `DamageDealingEffect`;",
         "without target selection (every untargeted end-step entry records the end-step player). "
         "`DamageDealingEffect`;"),
        ("Non-targeted \ufffd implements the `EndStepPlayerTargetedEffect` marker, which makes `StepTriggerService`'s "
         "generic end-step branch bake the end-step (active) player into `targetId`.",
         "Non-targeted: every untargeted end-step entry records the end-step (active) player as its `targetId`."),
        ("optionally requires source untapped (intervening-if);",
         "the `requireSourceUntapped` flag is used by no card (Howling Mine writes the intervening `SourceUntapped`);"),
        ("(Energy Vortex, `Triggers.upkeep(ActivePlayer.OPPONENT)` \ufffd its `targetId` is the active player)",
         "(Energy Vortex, `Triggers.upkeep(ActivePlayer.CHOSEN_PLAYER)`: its `targetId` is the chosen player)"),
    ],
    "EFFECTS_QUICK_REFERENCE.md": [
        ("(`entry.getTargetId()`, supplied by the `EndStepPlayerTargetedEffect` marker)",
         "(`entry.getTargetId()`, the end-step player every untargeted end-step entry records)"),
        ("Pair with `Triggers.upkeep(ActivePlayer.OPPONENT)` (\"at the beginning of the chosen player's upkeep\")",
         "Pair with `Triggers.upkeep(ActivePlayer.CHOSEN_PLAYER)` (\"at the beginning of the chosen player's upkeep\")"),
    ],
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("| `DealDamageIfDidntCastSpellThisTurnEffect(N)` | `Triggers.endStep(ActivePlayer.ANY)` | Impat",
         "| `DealDamageToPlayersEffect(N, TRIGGERING_PLAYER)` with `TriggerOptions.DEFAULT.withInterveningIf(new "
         "EndStepPlayerDidntCastSpell(null))` | `Triggers.endStep(ActivePlayer.ANY)` | Impat"),
        ("| `DealDamageToEndStepPlayerIfLifeAtMostEffect(M, N)` | `Triggers.endStep(ActivePlayer.ANY)` | Razor Pendulum "
         "(`(2, 5)`). Non-targeted; `EndStepPlayerTargetedEffect` marker bakes the end-step player into `targetId`.",
         "| `DealDamageToPlayersEffect(M, TRIGGERING_PLAYER)` with `TriggerOptions.DEFAULT.withInterveningIf(new "
         "TargetPlayerLifeAtMost(N))` | `Triggers.endStep(ActivePlayer.ANY)` | Razor Pendulum. Non-targeted; the "
         "end-step player is the entry's `targetId` and the reader's context target."),
        ("Non-targeted; `EndStepPlayerTargetedEffect` supplies the active end-step player,",
         "Non-targeted; the entry records the end-step player as its `targetId`,"),
        ("`ConditionalEffect(new EndStepPlayerDidntCastCreatureSpell(), new CreateTokenEffect(...))`",
         "`TriggerOptions.DEFAULT.withInterveningIf(new EndStepPlayerDidntCastSpell(CardType.CREATURE))` + "
         "`CreateTokenEffect(...)`"),
        ("Non-targeted; implements the `EndStepPlayerTargetedEffect` marker so `StepTriggerService`'s generic end-step "
         "branch bakes the end-step player into `targetId`.",
         "Non-targeted; every untargeted end-step entry records the end-step player as its `targetId`."),
        ("and the `EndStepPlayerDidntCastCreatureSpell` condition reads it.",
         "and the `EndStepPlayerDidntCastSpell` condition reads it."),
    ],
    "ARCHITECTURE.md": [
        ("an ability granted to the creature its controller's (CR 113.8).\n",
         "an ability granted to the creature its controller's (CR 113.8).\n\n"
         "A step or phase trigger's \"that player\" is the player whose step it is, never chosen by the effect's class: "
         "the end-step, each-draw-step, opponent's and chosen player's upkeep and emblem collectors "
         "(`StepTriggerService.recordStepPlayer`) record the active player on an ability that chooses no target "
         "whatever its effects, as its active and triggering player and non-targeting `targetId` (CR 115.10a), and "
         "the reader judges an intervening \"if\" about that player with it as the context target (Razor Pendulum's "
         "`TargetPlayerLifeAtMost`, Impatience's `EndStepPlayerDidntCastSpell`). \"At the beginning of the chosen "
         "player's upkeep\" is `Triggers.upkeep(ActivePlayer.CHOSEN_PLAYER)`, read from the source's chosen player "
         "(never another player when none was chosen). An Aura's or Equipment's \"at the beginning of your end step\" "
         "entry records the permanent it is attached to (`attachedPermanentSnapshot`), whose controller as it left the "
         "departure refresh records, for \"enchanted permanent's controller\" (Gremlin Infestation). No effect tells "
         "the step collectors to record the player (the `EndStepPlayerTargetedEffect` interface is gone).\n"),
    ],
    "TRIGGER_SLOT_TARGETING.md": [
        ("Predatory Advantage's `EndStepPlayerDidntCastCreatureSpell`", "Predatory Advantage's `EndStepPlayerDidntCastSpell`"),
    ],
}

GONE_LINE = ("- Gone (step 15, run 17z11e): `EndStepPlayerTargetedEffect` (every untargeted end-step entry records the "
             "end-step player), `DealDamageIfDidntCastSpellThisTurnEffect` and `DealDamageToEndStepPlayerIfLifeAtMostEffect` "
             "(an intervening \"if\" with `DealDamageToPlayersEffect(N, TRIGGERING_PLAYER)`), "
             "`EndStepPlayerDidntCastCreatureSpell` (now `EndStepPlayerDidntCastSpell(CardType)`).")


def drop_rows(text):
    lines = text.split("\n")
    kept = [l for l in lines if not (any(f"`{e}`" in l or f"`{e}(" in l for e in GONE_EFFECTS)
                                    and (l.startswith("| `") or l.startswith("- `")))]
    return "\n".join(kept)


def main():
    for name, edits in EDITS.items():
        path = DOCS / name
        text = path.read_text()
        new = text
        for old, rep in edits:
            if rep in new:
                continue
            if old not in new:
                raise SystemExit(f"{name}: expected text not found: {old[:80]}")
            new = new.replace(old, rep)
        if name in ("EFFECTS_INDEX.md", "EFFECTS_QUICK_REFERENCE.md"):
            new = drop_rows(new)
        if name == "EFFECTS_QUICK_REFERENCE.md" and GONE_LINE not in new:
            anchor = next(l for l in new.split("\n") if l.startswith("- `DealDamageToEndStepPlayerEffect("))
            new = new.replace(anchor, anchor + "\n" + GONE_LINE, 1)
        if new != text:
            path.write_text(new)
            print("edited", path.relative_to(ROOT))
    bad = []
    for path in list(DOCS.glob("*.md")) + list((ROOT / ".claude/skills").rglob("*.md")):
        if path.name == "EFFECT_MERGE_PLAN.md":
            continue
        for i, line in enumerate(path.read_text().split("\n"), 1):
            if "gone" in line.lower():
                continue
            for form in REMOVED:
                if form in line:
                    bad.append(f"{path.relative_to(ROOT)}:{i}: {form}")
    if bad:
        raise SystemExit("removed forms still named:\n" + "\n".join(bad))


if __name__ == "__main__":
    main()
