#!/usr/bin/env python3
"""Step 15, run 17w4: the card test of the copy-for-each family's rules fix.

Adds InkTreaderNephilimTest#noCopiesWhenTheSpellNoLongerTargetsOnlyItAsTheAbilityResolves: Ink-Treader Nephilim's
"if that spell targets only this creature" is an intervening "if" (CR 603.4), checked again as the ability resolves, so
a spell redirected to another creature in response is not copied. Idempotent: a test already present is left alone.
Per letter module.

Usage: step15-run17w4-tests.py <letter> [<letter> ...]   (or "all")
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
TEST_DIR = "magical-vibes-card-test/{l}/src/test/java/com/github/laxika/magicalvibes/cards/{l}/{n}.java"

INK_CASE = '''
    @Test
    @DisplayName("Makes no copies when the spell no longer targets only the Nephilim as the ability resolves")
    void noCopiesWhenTheSpellNoLongerTargetsOnlyItAsTheAbilityResolves() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new InkTreaderNephilim());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new BatteringWurm());
        harness.setHand(player1, List.of(new LightningBolt(), new Shunt()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, source.getId());
        UUID boltId = gd.stack.getFirst().getTargetableId();
        harness.ensurePriority(player1);
        harness.castInstant(player1, 0, boltId);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(bears.getId());

        harness.passBothPriorities();

        assertThat(gd.stack).noneMatch(StackEntry::isCopy);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Lightning Bolt");
    }
}
'''

TESTS = {
    "InkTreaderNephilimTest": ("i", "noCopiesWhenTheSpellNoLongerTargetsOnlyItAsTheAbilityResolves", INK_CASE, [
        ("import com.github.laxika.magicalvibes.cards.g.GiantSolifuge;\n",
         "import com.github.laxika.magicalvibes.cards.g.GiantSolifuge;\n"
         "import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;\n"
         "import com.github.laxika.magicalvibes.cards.l.LightningBolt;\n"),
        ("import com.github.laxika.magicalvibes.cards.s.SeedsOfStrength;\n",
         "import com.github.laxika.magicalvibes.cards.s.SeedsOfStrength;\n"
         "import com.github.laxika.magicalvibes.cards.s.Shunt;\n"),
        ("import java.util.Map;\n", "import java.util.Map;\nimport java.util.UUID;\n"),
        ("        Repeal.class, SeedsOfStrength.class})",
         "        Repeal.class, SeedsOfStrength.class, GrizzlyBears.class, LightningBolt.class, Shunt.class})"),
    ]),
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
    text = text[:-1].rstrip() + "\n" + case
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
