#!/usr/bin/env python3
"""Step 15, run 17z15d: the cards write the limits and conditions three collectors kept for them (rows 17z15d, 17z15e).

Oracle text checked with the Scryfall MCP:

- Aurelia, the Warleader (GTC 143), Godo, Bandit Warlord (CHK 169), Scourge of the Throne (VMA 184) and Fear of
  Missing Out (DSK 136): "Whenever [this] attacks for the first time each turn, ...". The attack collector kept one
  mark for each attacker and spent it before any filter or condition; "for the first time each turn" is part of the
  event, so the cards write `TriggerLimit.FIRST_TIME_EACH_TURN` (the creature's first attack this turn, by its attack
  count) and `ONCE_EACH_TURN` on an attack ability is "this ability triggers only once each turn", marked for the
  ability as it triggers.
- Mirror-Shield Hoplite (MOM 247): "Whenever a creature you control becomes the target of a backup ability, copy that
  ability. You may choose new targets for the copy. This ability triggers only once each turn." The limit was a flag
  on the permanent the collector set for any ability of the watch; the card writes `TriggerLimit.ONCE_EACH_TURN`.
- Komainu Battle Armor (NEC 21): "Whenever this creature or equipped creature deals combat damage to a player, goad
  each creature that player controls." One ability of two trigger conditions; written as two abilities, the attached
  combat-damage collector had to skip every printed "deals combat damage to a player" ability of a card that also had
  an "equipped creature deals combat damage" one.

Idempotent; fails if an edit doesn't apply.

Usage: python3 scripts/step15-run17z15d-cards.py [LETTER...]
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
CARDS = "magical-vibes-card/{letter}/src/main/java/com/github/laxika/magicalvibes/cards/{letter}/{name}.java"

TRIGGER = "import com.github.laxika.magicalvibes.model.trigger."

EDITS = {
    "AureliaTheWarleader": [
        ("""        // Flying, vigilance and haste are auto-loaded keywords. "Whenever Aurelia attacks for the
        // first time each turn": both halves are limited to once each turn (TriggerLimit.ONCE_EACH_TURN), so
        // attacking again in the extra combat phase this grants doesn't re-trigger and loop.
        addTrigger(Triggers.attacks(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
""",
         """        // Flying, vigilance and haste are auto-loaded keywords. "Whenever Aurelia attacks for the
        // first time each turn" is its first attack of the turn (TriggerLimit.FIRST_TIME_EACH_TURN), so
        // attacking again in the extra combat phase this grants doesn't re-trigger and loop.
        addTrigger(Triggers.attacks(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.FIRST_TIME_EACH_TURN),
"""),
    ],
    "GodoBanditWarlord": [
        ("""        // "Whenever Godo attacks for the first time each turn": every half is limited to once each turn
        // (TriggerLimit.ONCE_EACH_TURN), so attacking again in the extra combat phase this grants doesn't loop. Godo is
        // not a Samurai, so untapping itself is a separate SELF scope from the Samurai sweep.
        addTrigger(Triggers.attacks(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
""",
         """        // "Whenever Godo attacks for the first time each turn" is its first attack of the turn
        // (TriggerLimit.FIRST_TIME_EACH_TURN), so attacking again in the extra combat phase this grants doesn't loop.
        // Godo is not a Samurai, so untapping itself is a separate SELF scope from the Samurai sweep.
        addTrigger(Triggers.attacks(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.FIRST_TIME_EACH_TURN),
"""),
    ],
    "ScourgeOfTheThrone": [
        ("""                        new SourceMatchesPermanentPredicate(new PermanentAttacksPlayerWithMostLifePredicate())).withLimit(TriggerLimit.ONCE_EACH_TURN),
""",
         """                        new SourceMatchesPermanentPredicate(new PermanentAttacksPlayerWithMostLifePredicate()))
                        .withLimit(TriggerLimit.FIRST_TIME_EACH_TURN),
"""),
    ],
    "FearOfMissingOut": [
        ("""                TriggerOptions.DEFAULT.withInterveningIf(new Delirium()).withLimit(TriggerLimit.ONCE_EACH_TURN),
""",
         """                TriggerOptions.DEFAULT.withInterveningIf(new Delirium())
                        .withLimit(TriggerLimit.FIRST_TIME_EACH_TURN),
"""),
    ],
    "MirrorShieldHoplite": [
        ("""        addTrigger(Triggers.creatureYouControlBecomesTargetOfBackupAbility(),
                new CopyTargetTriggeredAbilityEffect());
""",
         """        addTrigger(Triggers.creatureYouControlBecomesTargetOfBackupAbility(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                new CopyTargetTriggeredAbilityEffect());
"""),
        (TRIGGER + "Triggers;\n",
         TRIGGER + "TriggerLimit;\n" + TRIGGER + "TriggerOptions;\n" + TRIGGER + "Triggers;\n"),
    ],
    "KomainuBattleArmor": [
        ("""        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new GoadCreaturesDamagedPlayerControlsUntilNextTurnEffect());
        addTrigger(Triggers.equippedCreatureDealsCombatDamageToPlayer(),
                new GoadCreaturesDamagedPlayerControlsUntilNextTurnEffect());
""",
         """        // "Whenever this creature or equipped creature deals combat damage to a player": one ability of two
        // trigger conditions.
        addTrigger(List.of(Triggers.dealsCombatDamageToPlayer(),
                        Triggers.equippedCreatureDealsCombatDamageToPlayer()),
                new GoadCreaturesDamagedPlayerControlsUntilNextTurnEffect());
"""),
    ],
}


def main() -> int:
    letters = {letter.lower() for letter in sys.argv[1:]}
    failures = []
    changed = 0
    for name, replacements in EDITS.items():
        letter = name[0].lower()
        if letters and letter not in letters:
            continue
        path = ROOT / CARDS.format(letter=letter, name=name)
        text = path.read_text()
        original = text
        for old, new in replacements:
            if new in text and (old not in text or old in new):
                continue
            if text.count(old) != 1:
                failures.append(f"{name}: {old.strip()[:60]!r} occurs {text.count(old)} times")
                continue
            text = text.replace(old, new)
        if text != original:
            path.write_text(text)
            changed += 1
    for failure in failures:
        print("FAILED", failure)
    print(f"{changed} card files changed")
    return 1 if failures else 0


if __name__ == "__main__":
    sys.exit(main())
