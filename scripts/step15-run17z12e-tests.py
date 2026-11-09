#!/usr/bin/env python3
"""Step 15, run 17z12e: the card test of the rules fix. "Whenever you seek one or more cards" doesn't trigger for a
seek that found no card (Darigaaz's Whelp's kicked seek with no Dragon in the library): the seek collector skipped an
empty seek only for Lurker in the Deep's effect class, so Vexyr, Ich-Tekik's Heir created a Golem."""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
TESTS = ROOT / "magical-vibes-card-test"


def edit(rel, pairs):
    path = TESTS / rel
    src = path.read_text()
    for old, new in pairs:
        if src.count(old) != 1:
            sys.exit(f"{rel}: expected one match of\n{old}")
        src = src.replace(old, new)
    path.write_text(src)
    print("converted", rel)


edit("v/src/test/java/com/github/laxika/magicalvibes/cards/v/VexyrIchTekiksHeirTest.java", [
    ("import com.github.laxika.magicalvibes.cards.c.ChoiceOfFortunes;\n",
     "import com.github.laxika.magicalvibes.cards.c.ChoiceOfFortunes;\n"
     "import com.github.laxika.magicalvibes.cards.d.DarigaazsWhelp;\n"),
    ("import com.github.laxika.magicalvibes.model.Keyword;\n",
     "import com.github.laxika.magicalvibes.model.Keyword;\n"
     "import com.github.laxika.magicalvibes.model.ManaColor;\n"),
    ("        NornsFetchling.class, ChoiceOfFortunes.class})",
     "        NornsFetchling.class, ChoiceOfFortunes.class, DarigaazsWhelp.class})"),
    ("""    @Test
    void opponentsSeekDoesNotCreateGolem() {""",
     """    @Test
    @DisplayName("A seek that finds no card is no seek of one or more cards")
    void seekThatFindsNothingDoesNotCreateGolem() {
        harness.addToBattlefield(player1, new VexyrIchTekiksHeir());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new DarigaazsWhelp()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(countPermanents(player1, "Phyrexian Golem")).isZero();
    }

    @Test
    void opponentsSeekDoesNotCreateGolem() {"""),
])
