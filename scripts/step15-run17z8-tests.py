#!/usr/bin/env python3
"""Step 15, run 17z8: card cases for By Elspeth's Command's "choose one that wasn't chosen during your last combat".

The card wrote its restriction as "hasn't been chosen this turn", so the mode it chose during your last combat was
offered again in the next turn's combat, and a mode chosen two combats earlier the same turn was not. Its modal is now
`ChooseOneEffect.notChosenDuringLastCombat`, judged against the mode the source chose last as the ability goes on the
stack (CR 603.3c). Both cases are rules fixes: they fail at HEAD.

Idempotent: a class that already has its case is left alone. An optional repository-root argument applies it to
another checkout (the HEAD worktree).

Usage: step15-run17z8-tests.py [<repository root>]
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(sys.argv[1]).resolve() if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parent.parent
TESTS = ROOT / "magical-vibes-card-test"
PKG = "com.github.laxika.magicalvibes"

CASES = {
    "b/ByElspethsCommandTest": ("modeChosenDuringYourLastCombatIsUnavailableDuringYourNextTurnsCombat",
        [], [], '''
    @Test
    @DisplayName("The mode chosen during your last combat is unavailable during your next turn's combat")
    void modeChosenDuringYourLastCombatIsUnavailableDuringYourNextTurnsCombat() {
        harness.addToBattlefield(player1, new ByElspethsCommand());

        advanceToCombat(player1);
        harness.handleListChoice(player1, TOKEN);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.UPKEEP);
        advanceToCombat(player1);

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).contains(TARGET_SOLDIER, HAND_SOLDIER).doesNotContain(TOKEN);
    }
'''),
    "b/ByElspethsCommandTest#2": ("modeChosenBeforeYourLastCombatCanBeChosenAgain",
        [], [], '''
    @Test
    @DisplayName("A mode chosen before your last combat can be chosen again")
    void modeChosenBeforeYourLastCombatCanBeChosenAgain() {
        harness.addToBattlefield(player1, new ByElspethsCommand());
        harness.setHand(player1, List.of(soldier("Hand Soldier", 2, 2)));

        advanceToCombat(player1);
        harness.handleListChoice(player1, TOKEN);
        harness.passBothPriorities();
        advanceToCombat(player1);
        harness.handleListChoice(player1, HAND_SOLDIER);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        advanceToCombat(player1);

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).contains(TOKEN, TARGET_SOLDIER).doesNotContain(HAND_SOLDIER);
    }
'''),
}


def sort_imports(text):
    imports = list(re.finditer(r"^import (?!static)[^;]+;$", text, re.M))
    if not imports:
        return text
    start, end = imports[0].start(), imports[-1].end()
    block = text[start:end]
    if re.search(r"^(?!import |\s*$).+", block, re.M):
        return text
    lines = [m.group(0) for m in imports]
    ours = sorted(l for l in lines if not l.startswith("import java"))
    java = sorted(l for l in lines if l.startswith("import java"))
    if not java:
        return text[:start] + "\n".join(ours) + text[end:]
    first_java = next(i for i, m in enumerate(imports) if m.group(0).startswith("import java"))
    if first_java == 0 or any(not m.group(0).startswith("import java") for m in imports[first_java:]):
        return text
    ours_end = imports[first_java - 1].end()
    return text[:start] + "\n".join(ours) + text[ours_end:]


def apply(rel, method, imports, used, body):
    rel = rel.split("#")[0]
    path = TESTS / f"{rel.split('/')[0]}/src/test/java/com/github/laxika/magicalvibes/cards/{rel}.java"
    text = path.read_text()
    if f"void {method}(" in text:
        return
    for imp in imports:
        line = f"import {PKG}.{imp};"
        if line not in text:
            first = re.search(r"^import [^;]+;$", text, re.M)
            text = text[:first.start()] + line + "\n" + text[first.start():]
    if used:
        m = re.search(r"@CardUsed\(\{([^}]*)\}\)", text)
        names = m.group(1)
        for u in used:
            if u not in names:
                names = names.rstrip() + ", " + u
        text = text[:m.start(1)] + names + text[m.end(1):]
    text = sort_imports(text)
    close = text.rstrip().rfind("}")
    text = text[:close].rstrip() + "\n" + body + "}\n"
    path.write_text(text)
    print(f"added {rel}#{method}")


def main():
    for rel, (method, imports, used, body) in CASES.items():
        apply(rel, method, imports, used, body)


if __name__ == "__main__":
    main()
