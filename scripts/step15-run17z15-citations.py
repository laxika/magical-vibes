#!/usr/bin/env python3
"""Step 15, run 17z15: the stale Saga and legendary-sorcery rule citations.

The Saga rules are CR 715 (714 is substitute cards) and the legendary instant or sorcery casting restriction is
CR 205.4e; each number below was read with the rules MCP `get_rule` and matched against what its line says:

- 715.2 a chapter symbol is a chapter ability, 715.2a the Roman numeral, 715.2b "{rN}—[Effect]" and its lore-counter
  threshold, 715.2c "{rN1}, {rN2}—", 715.2d the final chapter number, 715.2e the final chapter ability;
- 715.3a a Saga enters with a lore counter, 715.3c the precombat main phase lore counter (a turn-based action);
- 715.4 the sacrifice state-based action;
- 205.4e a legendary instant or sorcery needs a legendary creature or planeswalker.

Each edit names its file and the exact text it replaces. Idempotent; fails if a text is neither there nor already
replaced, or if a "714." citation is left in the edited files.

Usage: python3 scripts/step15-run17z15-citations.py [REPOSITORY_ROOT]
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parent.parent
DOMAIN = "magical-vibes-domain/src/main/java/com/github/laxika/magicalvibes/model/"
ENGINE = "magical-vibes-engine/src/main/java/com/github/laxika/magicalvibes/service/"
APPLICATION_TEST = "magical-vibes-application/src/test/java/com/github/laxika/magicalvibes/service/"

EDITS = [
    (APPLICATION_TEST + "state/StateBasedActionServiceTest.java", [
        ("Saga sacrifice — CR 714.4", "Saga sacrifice — CR 715.4"),
    ]),
    (DOMAIN + "TriggeredAbility.java", [
        ("counters on it was less than N and became at least N, [effect]\" (CR 714.2b)",
         "counters on it was less than N and became at least N, [effect]\" (CR 715.2b)"),
    ]),
    (DOMAIN + "Card.java", [
        ("Saga has one chapter ability per chapter number (CR 714.2b): its effects",
         "Saga has one chapter ability per chapter number (CR 715.2b): its effects"),
        ("A Saga has one chapter ability per chapter number (CR 714.2b), so",
         "A Saga has one chapter ability per chapter number (CR 715.2b), so"),
        ("(CR 714.2d). 3 for a three-chapter Saga.", "(CR 715.2d). 3 for a three-chapter Saga."),
    ]),
    (DOMAIN + "GraveyardTargetOperationState.java", [
        ("(used in SBA check CR 714.4)", "(used in SBA check CR 715.4)"),
    ]),
    (DOMAIN + "trigger/Triggers.java", [
        ("(CR 714.2b). Every effect added with the same chapter joins one ability.",
         "(CR 715.2b). Every effect added with the same chapter joins one ability."),
    ]),
    (DOMAIN + "trigger/FinalChapterTrigger.java", [
        ("The final chapter ability of a Saga you control (CR 714.2e)",
         "The final chapter ability of a Saga you control (CR 715.2e)"),
        ("greatest among its chapter abilities (CR 714.2d)", "greatest among its chapter abilities (CR 715.2d)"),
    ]),
    (DOMAIN + "trigger/ChapterTrigger.java", [
        ("A Saga's chapter ability (CR 714.2):", "A Saga's chapter ability (CR 715.2):"),
        ("became at least N, [effect]\" (CR 714.2b)", "became at least N, [effect]\" (CR 715.2b)"),
        ("is two chapter abilities (CR 714.2c)", "is two chapter abilities (CR 715.2c)"),
        ("as printed (CR 714.2a)", "as printed (CR 715.2a)"),
    ]),
    (DOMAIN + "effect/PutCounterOnTargetPermanentEffect.java", [
        ("triggers the appropriate chapter ability per MTG Rule 714.3b.",
         "triggers the appropriate chapter ability per CR 715.2b."),
    ]),
    (ENGINE + "StackResolutionService.java", [
        ("place first lore counter and trigger chapter I (MTG Rule 714.3a)",
         "place first lore counter and trigger chapter I (CR 715.3a)"),
    ]),
    (ENGINE + "turn/StepTriggerService.java", [
        ("add a lore counter to each Saga the active player controls (MTG Rule 714.3b)",
         "add a lore counter to each Saga the active player controls (CR 715.3c)"),
        ("the appropriate chapter ability (MTG Rule 714.3b).",
         "the appropriate chapter ability (CR 715.3c, 715.2b)."),
    ]),
    (ENGINE + "trigger/SagaWatch.java", [
        ("no options (CR 714.2b).", "no options (CR 715.2b)."),
    ]),
    (ENGINE + "trigger/SagaAbilityReader.java", [
        ("triggers or resolves, CR 714.2e)", "triggers or resolves, CR 715.2e)"),
    ]),
    (ENGINE + "input/GraveyardChoiceHandlerService.java", [
        ("include sourcePermanentId for SBA check (CR 714.4)", "include sourcePermanentId for SBA check (CR 715.4)"),
    ]),
    (ENGINE + "input/CardChoiceHandlerService.java", [
        ("place the first lore counter and trigger chapter I (CR 714.3a).",
         "place the first lore counter and trigger chapter I (CR 715.3a)."),
    ]),
    (ENGINE + "effect/normalfx/PermanentCounterSupport.java", [
        ("Lore counters on Sagas trigger chapter abilities (MTG Rule 714.3b)",
         "Lore counters on Sagas trigger chapter abilities (CR 715.2b)"),
    ]),
    (ENGINE + "cast/CastingPermissionService.java", [
        ("MTG rule 714.1: legendary sorceries require", "CR 205.4e: legendary sorceries require"),
    ]),
    (ENGINE + "GameActionAvailabilityService.java", [
        ("(CR 714.1) — but not the status", "(CR 205.4e) — but not the status"),
        ("The 601.2c/601.2b/714.1 filters below", "The 601.2c/601.2b/205.4e filters below"),
        ("MTG rule 714.1: can't cast a legendary sorcery", "CR 205.4e: can't cast a legendary sorcery"),
    ]),
    ("agent-docs/EFFECTS_INDEX.md", [
        ("LORE counters on Sagas trigger chapter abilities (MTG Rule 714.3b)",
         "LORE counters on Sagas trigger chapter abilities (CR 715.2b)"),
    ]),
    ("effect-slot-refactor.md", [
        ("Saga chapters become triggered abilities with a lore-counter threshold (**CR 714.2b**, checked).",
         "Saga chapters become triggered abilities with a lore-counter threshold (**CR 715.2b**, checked)."),
        ("| 10 | Sagas | ~9 | chapter thresholds (CR 714.2b), the final chapter |",
         "| 10 | Sagas | ~9 | chapter thresholds (CR 715.2b), the final chapter |"),
    ]),
]

STALE = re.compile(r"\b714\.\d")


def main() -> int:
    failures = []
    changed = 0
    for relative, replacements in EDITS:
        path = ROOT / relative
        text = path.read_text()
        original = text
        for old, new in replacements:
            if old in text:
                if text.count(old) != 1:
                    failures.append(f"{relative}: {old!r} occurs {text.count(old)} times")
                    continue
                text = text.replace(old, new)
            elif new not in text:
                failures.append(f"{relative}: neither {old!r} nor its replacement found")
        if relative != "effect-slot-refactor.md" and STALE.search(text):
            failures.append(f"{relative}: a 714.x citation is left")
        if text != original:
            path.write_text(text)
            changed += 1
    for failure in failures:
        print("FAILED", failure)
    print(f"{changed} files changed")
    return 1 if failures else 0


if __name__ == "__main__":
    sys.exit(main())
