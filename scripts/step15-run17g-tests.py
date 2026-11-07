#!/usr/bin/env python3
"""Step 15 run 17g of effect-slot-refactor.md: the new card cases.

Idempotent; run per letter module (``--module p``) or with no argument for every module this run touches.

- Pirated Copy: the same-name filter of its granted ability is the watched trigger's, judged by the collector (the
  ability no longer authors it in ``AllyCombatDamageTriggerEffect``): a creature with another name dealing combat
  damage draws nothing.
"""
import argparse
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
TESTS = ROOT / "magical-vibes-card-test"
PKG = "src/test/java/com/github/laxika/magicalvibes/cards"

CASES = {
    ("p", "PiratedCopyTest"): [
        ("import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;\n",
         "import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;\n"
         "import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;\n"),
        ("@CardUsed({PiratedCopy.class, GrizzlyBears.class, Mutavault.class})",
         "@CardUsed({PiratedCopy.class, GrizzlyBears.class, Mutavault.class, ElvishWarrior.class})"),
        ("""    @Test
    @DisplayName("Declining to copy leaves the original 0/0 to die")""",
         """    @Test
    @DisplayName("A creature with another name dealing combat damage draws nothing")
    void differentlyNamedCreatureDoesNotDraw() {
        castCopyOfGrizzlyBears(player1, player2);
        Permanent warrior = addCreatureReady(player1, new ElvishWarrior());
        warrior.setAttacking(true);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Declining to copy leaves the original 0/0 to die")"""),
    ],
}

DONE = {
    "PiratedCopyTest": "differentlyNamedCreatureDoesNotDraw",
}


def convert(letter, name, replacements):
    path = TESTS / letter / PKG / letter / f"{name}.java"
    text = path.read_text()
    if DONE[name] in text:
        print(f"unchanged {path.relative_to(ROOT)}")
        return
    for old, new in replacements:
        if text.count(old) != 1:
            sys.exit(f"{path}: expected one occurrence of\n{old}")
        text = text.replace(old, new)
    path.write_text(text)
    print(f"converted {path.relative_to(ROOT)}")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--module", help="letter module to convert (default: all this run touches)")
    args = parser.parse_args()
    for (letter, name), replacements in CASES.items():
        if args.module is None or args.module == letter:
            convert(letter, name, replacements)


if __name__ == "__main__":
    main()
