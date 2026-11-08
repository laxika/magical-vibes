#!/usr/bin/env python3
"""Step 15, run 17w5: card tests for what the rewritten abilities now read from the stack entry.

Adds:

- ThousandYearStormTest#spellCastInResponseIsNotCountedForAnEarlierSpell: "for each other instant and sorcery spell
  you've cast before it this turn" counts as the ability resolves only the spells cast before the triggering spell (the
  stack entry keeps its place among its caster's spells), so an instant cast in response adds no copy of the first one.
- TrialAndErrorTest#mayCopyTheCopyWhenTheCopyFizzles: a copy of Trial and Error that fizzles triggers its own "copy it",
  which copies the copy as it last existed (the copy effect no longer refuses a triggering spell that is a copy).

Idempotent: a test already present is left alone. Per letter module.

Usage: step15-run17w5-tests.py <letter> [<letter> ...]   (or "all")
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
TEST_DIR = "magical-vibes-card-test/{l}/src/test/java/com/github/laxika/magicalvibes/cards/{l}/{n}.java"

STORM_CASE = '''
    @Test
    @DisplayName("An instant cast in response to the ability adds no copy of the spell cast before it")
    void spellCastInResponseIsNotCountedForAnEarlierSpell() {
        harness.addToBattlefield(player1, new ThousandYearStorm());
        gd.recordSpellCast(player1.getId(), new Divination());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        // The first Bolt has one instant or sorcery before it (one copy); the second, cast in response, has two.
        harness.castInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());
        for (int step = 0; step < 30 && (!gd.stack.isEmpty() || gd.interaction.isAwaitingInput()); step++) {
            if (gd.interaction.isAwaitingInput()) {
                harness.handleMayAbilityChosen(player1, false);
            } else {
                harness.passBothPriorities();
            }
        }

        // Two Bolts and three copies: 15 damage.
        harness.assertLife(player2, 5);
        assertThat(gd.stack).isEmpty();
    }
'''

TRIAL_CASE = '''
    @Test
    void mayCopyTheCopyWhenTheCopyFizzles() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TrialAndError()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, bears.getId());
        gd.playerBattlefields.get(player2.getId()).remove(bears);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        // The copy keeps the Bears as its target, so it fizzles too and its own ability triggers.
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
'''

# test -> (letter, method, case, [(old, new)] edits applied before the case is appended)
TESTS = {
    "ThousandYearStormTest": ("t", "spellCastInResponseIsNotCountedForAnEarlierSpell", STORM_CASE, []),
    "TrialAndErrorTest": ("t", "mayCopyTheCopyWhenTheCopyFizzles", TRIAL_CASE, []),
}


def add_case(name, letter, method, case, edits):
    path = ROOT / TEST_DIR.format(l=letter, n=name)
    text = path.read_text()
    if f"void {method}(" in text:
        return
    for old, new in edits:
        if old not in text:
            raise SystemExit(f"{name}: expected text not found:\n{old}")
        text = text.replace(old, new, 1)
    text = text.rstrip()
    if not text.endswith("}"):
        raise SystemExit(f"{name}: the class doesn't end with a brace")
    text = text[:-1].rstrip() + "\n" + case + "}\n"
    path.write_text(text)
    print(f"added {name}#{method}")


def main():
    letters = set("abcdefghijklmnopqrstuvwxyz") if sys.argv[1:] == ["all"] else set(sys.argv[1:])
    if not letters:
        raise SystemExit(__doc__)
    for name, (letter, method, case, edits) in TESTS.items():
        if letter in letters:
            add_case(name, letter, method, case, edits)


if __name__ == "__main__":
    main()
