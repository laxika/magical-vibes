#!/usr/bin/env python3
"""Step 15, run 17z11e: the domain records that bound the step's "that player" by their class.

- `EndStepPlayerTargetedEffect` (a marker interface the end-step collector read) is deleted, and every record drops it;
  `MonarchEndStepTriggeredEffect` extends `CardEffect`.
- `DealDamageIfDidntCastSpellThisTurnEffect` (Impatience) and `DealDamageToEndStepPlayerIfLifeAtMostEffect` (Razor
  Pendulum) and their handlers are deleted: the cards write an intervening "if" and `DealDamageToPlayersEffect`.
- `EndStepPlayerDidntCastCreatureSpell` becomes `EndStepPlayerDidntCastSpell(CardType spellType)`.

Idempotent. Usage: step15-run17z11e-records.py [repository root]
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parent.parent
DOMAIN = ROOT / "magical-vibes-domain/src/main/java/com/github/laxika/magicalvibes/model"
EFFECT = DOMAIN / "effect"
HANDLERS = ROOT / "magical-vibes-engine/src/main/java/com/github/laxika/magicalvibes/service/effect/normalfx"

DELETED = [
    EFFECT / "EndStepPlayerTargetedEffect.java",
    EFFECT / "DealDamageIfDidntCastSpellThisTurnEffect.java",
    EFFECT / "DealDamageToEndStepPlayerIfLifeAtMostEffect.java",
    HANDLERS / "DealDamageIfDidntCastSpellThisTurnEffectHandler.java",
    HANDLERS / "DealDamageToEndStepPlayerIfLifeAtMostEffectHandler.java",
]

NEW_CONDITION = '''package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.CardType;

/**
 * "If that player didn't cast a [type] spell this turn" on an end-step ability: the player whose end step it is
 * (the stack entry's non-targeting {@code targetId}, and the reader's context target as the ability triggers) cast
 * no spell this turn, or none of {@code spellType} when it isn't {@code null}. A countered spell still counts as
 * cast. Used by Impatience (any spell) and Predatory Advantage (a creature spell).
 */
public record EndStepPlayerDidntCastSpell(CardType spellType) implements Condition {

    @Override
    public String conditionName() {
        return spellType == null ? "didn't cast a spell"
                : "didn't cast a " + spellType.name().toLowerCase() + " spell";
    }

    @Override
    public String conditionNotMetReason() {
        return spellType == null ? "that player cast a spell this turn"
                : "that player cast a " + spellType.name().toLowerCase() + " spell this turn";
    }
}
'''


def edit(path, fn):
    if not path.exists():
        return
    text = path.read_text()
    new = fn(text)
    if new != text:
        path.write_text(new)
        print("edited", path.relative_to(ROOT))


def drop_marker(text):
    text = re.sub(r"implements CardEffect, EndStepPlayerTargetedEffect \{", "implements CardEffect {", text)
    text = re.sub(r"implements EndStepPlayerTargetedEffect, DamageDealingEffect \{", "implements DamageDealingEffect {",
                  text)
    text = re.sub(r"implements EndStepPlayerTargetedEffect \{", "implements CardEffect {", text)
    text = text.replace("extends EndStepPlayerTargetedEffect {", "extends CardEffect {")
    return text


def main():
    for path in EFFECT.glob("*.java"):
        edit(path, drop_marker)
    old = DOMAIN / "condition/EndStepPlayerDidntCastCreatureSpell.java"
    new = DOMAIN / "condition/EndStepPlayerDidntCastSpell.java"
    if old.exists():
        old.unlink()
        print("deleted", old.relative_to(ROOT))
    if not new.exists():
        new.write_text(NEW_CONDITION)
        print("created", new.relative_to(ROOT))
    edit(DOMAIN / "condition/Condition.java",
         lambda t: t.replace("EndStepPlayerDidntCastCreatureSpell,", "EndStepPlayerDidntCastSpell,"))
    for path in DELETED:
        if path.exists():
            path.unlink()
            print("deleted", path.relative_to(ROOT))


if __name__ == "__main__":
    main()
