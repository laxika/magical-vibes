#!/usr/bin/env python3
"""Step 15, run 17w2: card test cases for cast triggers that read the triggering spell after it left the stack.

Adds two cases for "that spell" read as the ability resolves once the spell was countered in response (CR 608.2h):
Magmablood Archaic's ruling ("It resolves even if that spell is countered or otherwise leaves the stack") boosts by
the colors spent on the countered spell, and Nantuko Shrine counts the cards with the countered spell's name (the
stack entry's snapshot of it). Idempotent. Per letter module.

Usage: step15-run17w2-tests.py <letter> [<letter> ...]   (or "all")
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
TESTS = ROOT / "magical-vibes-card-test/{letter}/src/test/java/com/github/laxika/magicalvibes/cards/{letter}/{name}.java"

MAGMABLOOD_CASE = '''    @Test
    @DisplayName("A countered instant or sorcery still boosts by the colors spent to cast it")
    void counteredSpellStillBoostsByItsColorsSpent() {
        Permanent archaic = addCreatureReady(player1, new MagmabloodArchaic());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Card divination = new Divination();
        harness.setHand(player1, List.of(divination));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castSorcery(player1, 0, 0);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player2, 0, divination.getId());
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(divination);

        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, archaic)).isEqualTo(4);
    }

'''

NANTUKO_CASE = '''    @Test
    @DisplayName("A spell countered before the ability resolves is counted by its name as it last existed")
    void counteredSpellIsCountedByItsName() {
        harness.addToBattlefield(player1, new NantukoShrine());
        harness.setGraveyard(player2, List.of(new AngelicWall()));
        harness.forceActivePlayer(player1);
        Card wall = new AngelicWall();
        harness.setHand(player1, List.of(wall));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player2, 0, wall.getId());
        // The Shrine's ability for Cancel itself resolves first (no card named Cancel in a graveyard), then Cancel.
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(wall);

        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Squirrel")).isEqualTo(2);
    }

'''

TORQUE_CASE = '''    @Test
    @CardUsed({MycosynthLattice.class})
    @DisplayName("A spell of the chosen color made colorless by Mycosynth Lattice does not trigger")
    void spellMadeColorlessByMycosynthLatticeDoesNotTrigger() {
        Permanent torque = harness.addToBattlefieldAndReturn(player1, new JeweledTorque());
        torque.setChosenColor(CardColor.GREEN);
        harness.addToBattlefield(player1, new MycosynthLattice());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFromHand(player1, new DeepwoodWolverine(), "{G}");

        assertThat(gd.stack).hasSize(1);
    }

'''

EDITS = {
    ("j", "JeweledTorqueTest", "spellMadeColorlessByMycosynthLatticeDoesNotTrigger"): [
        ("import com.github.laxika.magicalvibes.cards.f.FlailingSoldier;\n",
         "import com.github.laxika.magicalvibes.cards.f.FlailingSoldier;\nimport com.github.laxika.magicalvibes.cards.m.MycosynthLattice;\n"),
        ("    @Test\n    @DisplayName(\"A spell of another color does not trigger Jeweled Torque\")",
         TORQUE_CASE + "    @Test\n    @DisplayName(\"A spell of another color does not trigger Jeweled Torque\")"),
    ],
    ("m", "MagmabloodArchaicTest", "counteredSpellStillBoostsByItsColorsSpent"): [
        ("import com.github.laxika.magicalvibes.cards.d.Divination;\n",
         "import com.github.laxika.magicalvibes.cards.c.Cancel;\nimport com.github.laxika.magicalvibes.cards.d.Divination;\n"),
        ("import com.github.laxika.magicalvibes.model.CounterType;\n",
         "import com.github.laxika.magicalvibes.model.Card;\nimport com.github.laxika.magicalvibes.model.CounterType;\n"),
        ("@CardUsed({MagmabloodArchaic.class, Divination.class,",
         "@CardUsed({MagmabloodArchaic.class, Cancel.class, Divination.class,"),
        ("    @Test\n    void opponentsInstantDoesNotTriggerBoost() {",
         MAGMABLOOD_CASE + "    @Test\n    void opponentsInstantDoesNotTriggerBoost() {"),
    ],
    ("n", "NantukoShrineTest", "counteredSpellIsCountedByItsName"): [
        ("import com.github.laxika.magicalvibes.cards.d.DuskImp;\n",
         "import com.github.laxika.magicalvibes.cards.c.Cancel;\nimport com.github.laxika.magicalvibes.cards.d.DuskImp;\n"),
        ("import com.github.laxika.magicalvibes.model.CardColor;\n",
         "import com.github.laxika.magicalvibes.model.Card;\nimport com.github.laxika.magicalvibes.model.CardColor;\n"),
        ("import com.github.laxika.magicalvibes.model.CardType;\n",
         "import com.github.laxika.magicalvibes.model.CardType;\nimport com.github.laxika.magicalvibes.model.ManaColor;\n"),
        ("@CardUsed({NantukoShrine.class, AngelicWall.class,", "@CardUsed({NantukoShrine.class, AngelicWall.class, Cancel.class,"),
        ("    @Test\n    @DisplayName(\"Nonmatching graveyard cards do not create Squirrels\")",
         NANTUKO_CASE + "    @Test\n    @DisplayName(\"Nonmatching graveyard cards do not create Squirrels\")"),
    ],
}


def main():
    letters = set("abcdefghijklmnopqrstuvwxyz") if sys.argv[1:] == ["all"] else set(sys.argv[1:])
    if not letters:
        raise SystemExit(__doc__)
    for (letter, name, marker), edits in EDITS.items():
        if letter not in letters:
            continue
        path = pathlib.Path(str(TESTS).format(letter=letter, name=name))
        text = path.read_text()
        if marker in text:
            continue
        for old, new in edits:
            if text.count(old) != 1:
                raise SystemExit(f"{name}: expected text not found once: {old!r}")
            text = text.replace(old, new)
        path.write_text(text)
        print(f"updated {name}")


if __name__ == "__main__":
    main()
