#!/usr/bin/env python3
"""Step 15, run 17z14: the rules-fix cases of the planes whose one chaos ability was several stack objects.

Kessig's "each creature you control gets +2/+2, gains trample, and becomes a Werewolf", Windriddle Palaces' "each
player mills a card" and Bad Wolf Bay's "cards can't enter from exile this turn. Then planeswalk." are one ability
each, so one stack object (CR 603.3); the planar reader's per-effect loop put each effect on the stack separately, an
opponent able to respond between them. Each case fails at `5d7a44c112` and passes after run 17z14.

Usage: step15-run17z14-tests.py [REPOSITORY_ROOT]. Fails if an edit didn't apply; idempotent.
"""
import pathlib
import sys

ROOT = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parent.parent
TESTS = ROOT / "magical-vibes-card-test"
PKG = "src/test/java/com/github/laxika/magicalvibes/cards"

CASES = {
    "KessigTest": (
        "    @Test\n    void chaosBoostsYourCreaturesAndMakesThemWerewolvesUntilEndOfTurn() {",
        """    @Test
    void chaosAbilityIsOneStackObject() {
        harness.inMutationScope(() -> planar.chaos(gd));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEffectsToResolve()).hasSize(3);
    }

"""),
    "WindriddlePalacesTest": (
        "    @Test\n    void chaosMakesEachPlayerMillOne() {",
        """    @Test
    void chaosAbilityIsOneStackObject() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        harness.inMutationScope(() -> planar.chaos(gd));

        assertThat(gd.stack).hasSize(1);
    }

"""),
    "BadWolfBayTest": (
        "    @Test\n    void chaosPreventsTheDelayedReturnAndPlaneswalks() {",
        """    @Test
    void chaosAbilityIsOneStackObject() {
        harness.inMutationScope(() -> planar.chaos(gd));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEffectsToResolve()).hasSize(2);
    }

"""),
}


def main():
    failed = []
    for name, (anchor, case) in CASES.items():
        letter = name[0].lower()
        path = TESTS / letter / PKG / letter / f"{name}.java"
        text = path.read_text()
        if "void chaosAbilityIsOneStackObject()" in text:
            continue
        if text.count(anchor) != 1:
            failed.append(name)
            continue
        path.write_text(text.replace(anchor, case + anchor))
    if failed:
        sys.exit("Edits not applied: " + ", ".join(failed))


if __name__ == "__main__":
    main()
