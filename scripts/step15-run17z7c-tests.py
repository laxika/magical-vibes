#!/usr/bin/env python3
"""Step 15, run 17z7c: Lucy MacLean, Positively Armed's card test for the "do this only once each turn" on an
opponent's token.

The class-keyed enter handler queued Lucy's target choice for both token-enter watches, and the target-choice handler
marked the once-each-turn use by the wrapped class. Lucy's ability now goes through the token-enter defaults, which
queue the target choice with the ability's limit (CR 603.2h); the opponent-token half reaches the default, not the
"you may" handler. The new case guards that half: after a copy of an opponent's token was made, the ability doesn't
trigger again that turn. It passes at HEAD too.

Idempotent; an optional first argument is the repository root (used for the HEAD worktree).

Usage: step15-run17z7c-tests.py [<repository root>]
"""
import pathlib
import sys

ROOT = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parent.parent
PKG = "com.github.laxika.magicalvibes"


def test_path(letter, name):
    return ROOT / f"magical-vibes-card-test/{letter}/src/test/java" / PKG.replace(".", "/") / f"cards/{letter}/{name}.java"


NEW_CASE = """    @Test
    @DisplayName("Copying an opponent's token uses up the once each turn")
    void copyingOpponentsTokenUsesUpOnceEachTurn() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addLucy(player1);

        createTokens(player2);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        createTokens(player2);
        harness.passBothPriorities();

        assertThat(countTokens(player1)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

"""


def main():
    path = test_path("l", "LucyMacLeanPositivelyArmedTest")
    text = path.read_text()
    if "copyingOpponentsTokenUsesUpOnceEachTurn" in text:
        print("LucyMacLeanPositivelyArmedTest: already converted")
        return
    anchor = """    @Test
    @DisplayName("Simultaneous tokens each offer a copy until one is accepted")"""
    if anchor not in text:
        raise SystemExit(f"{path}: the expected text is missing")
    text = text.replace(anchor, NEW_CASE + anchor, 1)
    path.write_text(text)
    print("LucyMacLeanPositivelyArmedTest: converted")


if __name__ == "__main__":
    main()
