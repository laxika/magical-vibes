#!/usr/bin/env python3
"""Step 15, run 17z11e: the cards whose step abilities the step collector bound by their effect's class.

- Impatience: "if that player didn't cast a spell this turn" is an intervening "if"
  (`EndStepPlayerDidntCastSpell(null)`), "deals 2 damage to that player" `DealDamageToPlayersEffect(2,
  TRIGGERING_PLAYER)`.
- Razor Pendulum: "if that player has 5 or less life" is an intervening "if" (`TargetPlayerLifeAtMost(5)`, read on the
  end-step player the entry and the reader's context record), the damage as Impatience's.
- Howling Mine: "if Howling Mine is untapped" is an intervening "if" (`SourceUntapped`), the draw
  `DrawCardForTargetPlayerEffect(1)`.
- Predatory Advantage: `EndStepPlayerDidntCastSpell(CardType.CREATURE)`.
- The Rack, Black Vise, Energy Vortex: "at the beginning of the chosen player's upkeep" is
  `Triggers.upkeep(ActivePlayer.CHOSEN_PLAYER)`.

Idempotent, per letter module. Usage: step15-run17z11e-cards.py [repository root]
"""
import pathlib
import sys

ROOT = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parent.parent
CARDS = ROOT / "magical-vibes-card"


def card(letter, name):
    return CARDS / letter / f"src/main/java/com/github/laxika/magicalvibes/cards/{letter}/{name}.java"


def add_import(text, imp):
    line = f"import {imp};"
    lines = text.split("\n")
    if line in lines:
        return text
    plain = [i for i, l in enumerate(lines) if l.startswith("import ") and not l.startswith("import static")]
    at = next((i for i in plain if lines[i] > line), plain[-1] + 1)
    lines.insert(at, line)
    return "\n".join(lines)


def drop_import(text, imp):
    return text.replace(f"import {imp};\n", "")


def edit(path, replacements, imports=(), dropped=()):
    text = path.read_text()
    new = text
    for old, rep in replacements:
        if rep in new:
            continue
        if old not in new:
            raise SystemExit(f"{path.name}: expected text not found: {old}")
        new = new.replace(old, rep)
    for imp in dropped:
        new = drop_import(new, imp)
    for imp in imports:
        new = add_import(new, imp)
    if new != text:
        path.write_text(new)
        print("edited", path.relative_to(ROOT))


M = "com.github.laxika.magicalvibes.model"


def main():
    edit(card("i", "Impatience"), [(
        "        addTrigger(Triggers.endStep(ActivePlayer.ANY), new DealDamageIfDidntCastSpellThisTurnEffect(2));",
        "        addTrigger(Triggers.endStep(ActivePlayer.ANY),\n"
        "                TriggerOptions.DEFAULT.withInterveningIf(new EndStepPlayerDidntCastSpell(null)),\n"
        "                new DealDamageToPlayersEffect(2, DamageRecipient.TRIGGERING_PLAYER));")],
        imports=[f"{M}.condition.EndStepPlayerDidntCastSpell", f"{M}.effect.DamageRecipient",
                 f"{M}.effect.DealDamageToPlayersEffect", f"{M}.trigger.TriggerOptions"],
        dropped=[f"{M}.effect.DealDamageIfDidntCastSpellThisTurnEffect"])
    edit(card("r", "RazorPendulum"), [(
        "        addTrigger(Triggers.endStep(ActivePlayer.ANY), new DealDamageToEndStepPlayerIfLifeAtMostEffect(2, 5));",
        "        addTrigger(Triggers.endStep(ActivePlayer.ANY),\n"
        "                TriggerOptions.DEFAULT.withInterveningIf(new TargetPlayerLifeAtMost(5)),\n"
        "                new DealDamageToPlayersEffect(2, DamageRecipient.TRIGGERING_PLAYER));")],
        imports=[f"{M}.condition.TargetPlayerLifeAtMost", f"{M}.effect.DamageRecipient",
                 f"{M}.effect.DealDamageToPlayersEffect", f"{M}.trigger.TriggerOptions"],
        dropped=[f"{M}.effect.DealDamageToEndStepPlayerIfLifeAtMostEffect"])
    edit(card("h", "HowlingMine"), [(
        "        addTrigger(Triggers.drawStep(ActivePlayer.ANY), new DrawCardForTargetPlayerEffect(1, true));",
        "        // At the beginning of each player's draw step, if this artifact is untapped, that player draws an\n"
        "        // additional card.\n"
        "        addTrigger(Triggers.drawStep(ActivePlayer.ANY),\n"
        "                TriggerOptions.DEFAULT.withInterveningIf(new SourceUntapped()),\n"
        "                new DrawCardForTargetPlayerEffect(1));")],
        imports=[f"{M}.condition.SourceUntapped", f"{M}.trigger.TriggerOptions"])
    edit(card("p", "PredatoryAdvantage"), [(
        "new EndStepPlayerDidntCastCreatureSpell()", "new EndStepPlayerDidntCastSpell(CardType.CREATURE)")],
        imports=[f"{M}.condition.EndStepPlayerDidntCastSpell", f"{M}.CardType"],
        dropped=[f"{M}.condition.EndStepPlayerDidntCastCreatureSpell"])
    for letter, name in (("t", "TheRack"), ("b", "BlackVise"), ("e", "EnergyVortex")):
        edit(card(letter, name), [("Triggers.upkeep(ActivePlayer.OPPONENT)", "Triggers.upkeep(ActivePlayer.CHOSEN_PLAYER)")])


if __name__ == "__main__":
    main()
