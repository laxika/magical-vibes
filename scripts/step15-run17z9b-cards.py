#!/usr/bin/env python3
"""Step 15, run 17z9b: the discard and cast/land-play trigger-condition records are abilities written as printed.

- Gutmorn, Pactbound Servant: `DiscardTriggerCollectorService.handleGutmornOnDiscard` judged "during your turn" and
  bound the discarding player and the discarded card by the effect's class. The card writes one ability, "whenever a
  player discards a card" (`youDiscardCard()` and `opponentDiscardsCard()`), with "during your turn" as its event
  condition (`ControllerTurn`); the discard default records the discarding player and the card on the entry.
- Duelists' Convocation International: the class-keyed `handleDuelistsConvocationInternational` judged the digit match
  and baked the mana value. The card writes one ability, "whenever you play a land or cast a spell", with the intervening
  "if" `TriggeringCardManaValueIsUncrossedChosenDigit`; the effect reads the triggering spell or land from the entry.
- The Fourth Doctor: the cast and land-play handlers of `CreateFoodWhenPlayingCardFromTopOfLibraryEffect` judged the
  zone, the historic filter and the permission's use, then rebuilt the record into a Food token. The card writes the
  zone and filter on its triggers, the permission's use as the event condition
  (`SourceLibraryPlayPermissionUsedThisTurn`) and `CreateTokenEffect.ofFoodToken(1)`.

Idempotent, per letter module.

Usage: step15-run17z9b-cards.py <letter> [<letter> ...]
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
PKG = "com.github.laxika.magicalvibes"

GUTMORN = ("""        addTrigger(Triggers.youDiscardCard(),
                new ConjureDuplicateOfDiscardedCardIntoChosenPlayerHandEffect());
        addTrigger(Triggers.opponentDiscardsCard(),
                new ConjureDuplicateOfDiscardedCardIntoChosenPlayerHandEffect());""",
           """        addTrigger(List.of(Triggers.youDiscardCard(), Triggers.opponentDiscardsCard()),
                TriggerOptions.DEFAULT.withEventCondition(new ControllerTurn()),
                new ConjureDuplicateOfDiscardedCardIntoChosenPlayerHandEffect());""")

DUELISTS = ("""        addTrigger(Triggers.youPlayLand(),
                new DuelistsConvocationInternationalTriggerEffect());
        addTrigger(Triggers.youCastSpell(),
                new DuelistsConvocationInternationalTriggerEffect());""",
            """        addTrigger(List.of(Triggers.youPlayLand(), Triggers.youCastSpell().anySpell()),
                TriggerOptions.DEFAULT.withInterveningIf(new TriggeringCardManaValueIsUncrossedChosenDigit()),
                new DuelistsConvocationInternationalTriggerEffect());""")

DOCTOR = ("""        addTrigger(List.of(Triggers.youCastSpell(), Triggers.youPlayLand()),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                new CreateFoodWhenPlayingCardFromTopOfLibraryEffect(historic));""",
          """        addTrigger(List.of(Triggers.youCastSpell().withFilter(historic).castFrom(Zone.LIBRARY),
                        Triggers.youPlayLand().withFilter(historic).playedFrom(Zone.LIBRARY)),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN)
                        .withEventCondition(new SourceLibraryPlayPermissionUsedThisTurn()),
                CreateTokenEffect.ofFoodToken(1));""")

CARDS = {
    "g": {"GutmornPactboundServant": ([GUTMORN], ["model.condition.ControllerTurn", "model.trigger.TriggerOptions"],
                                      ["java.util.List"], [])},
    "d": {"DuelistsConvocationInternational": (
        [DUELISTS], ["model.condition.TriggeringCardManaValueIsUncrossedChosenDigit", "model.trigger.TriggerOptions"],
        ["java.util.List"], [])},
    "t": {"TheFourthDoctor": (
        [DOCTOR], ["model.Zone", "model.condition.SourceLibraryPlayPermissionUsedThisTurn",
                   "model.effect.CreateTokenEffect"], [],
        ["model.effect.CreateFoodWhenPlayingCardFromTopOfLibraryEffect"])},
}


def add_import(text, line):
    if line in text:
        return text
    lines = text.splitlines(keepends=True)
    imports = [i for i, l in enumerate(lines) if l.startswith("import ")]
    target = line.strip()
    is_java = target.startswith("import java.")
    for i in imports:
        if lines[i].startswith("import java.") != is_java:
            continue
        if lines[i].strip() > target:
            lines.insert(i, line)
            return "".join(lines)
    same = [i for i in imports if lines[i].startswith("import java.") == is_java]
    if same:
        lines.insert(same[-1] + 1, line)
    else:
        lines.insert(imports[-1] + 1, "\n" + line)
    return "".join(lines)


def convert(path, replacements, imports, java_imports, removed_imports):
    text = path.read_text()
    if all(new in text for _, new in replacements):
        return False
    for old, new in replacements:
        if old not in text:
            raise SystemExit(f"{path}: the expected text is missing: {old.strip()[:80]}")
        text = text.replace(old, new)
    for name in imports:
        text = add_import(text, f"import {PKG}.{name};\n")
    for name in java_imports:
        text = add_import(text, f"import {name};\n")
    for name in removed_imports:
        text = text.replace(f"import {PKG}.{name};\n", "")
    text = text.replace(";\n@CardRegistration", ";\n\n@CardRegistration", 1)
    path.write_text(text)
    return True


def main(letters):
    for letter in letters:
        for name, spec in CARDS.get(letter, {}).items():
            path = (ROOT / f"magical-vibes-card/{letter}/src/main/java" / PKG.replace(".", "/")
                    / f"cards/{letter}/{name}.java")
            print(f"{name}: {'converted' if convert(path, *spec) else 'already converted'}")


if __name__ == "__main__":
    main(sys.argv[1:] or sorted(CARDS))
