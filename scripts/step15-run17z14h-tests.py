#!/usr/bin/env python3
"""Step 15, run 17z14h: the card tests the run adds.

`CASES`:

- Horde Ambusher ("whenever this creature blocks, it deals 1 damage to you"), Hamlet Captain ("whenever this creature
  attacks or blocks, other Humans you control get +1/+1 until end of turn") and Luminous Wake ("whenever enchanted
  creature attacks or blocks, you gain 4 life") trigger once each combat however many creatures the creature blocks
  (CR 509.3a). Written without a multiplicity, each triggered for each creature blocked; the three cases fail at
  `616716d2cd` and pass after run 17z14h.
- Ashmouth Hound ("whenever this creature blocks or becomes blocked by a creature, this creature deals 1 damage to
  that creature") triggers for each creature it blocks (CR 509.3b; its ruling). The card is unchanged; the case holds
  the other reading in place.
- Chainer's Torment's chapter I ("Chainer's Torment deals 2 damage to each opponent and you gain 2 life") is one
  chapter ability (CR 715.2b), one stack object of both effects. `Card.addTriggeredAbility` joined the card's two
  calls already, so the case passes at `616716d2cd` too.

Usage: step15-run17z14h-tests.py [REPOSITORY_ROOT]. Fails if an edit didn't apply; idempotent.
"""
import pathlib
import sys

ROOT = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parent.parent
TESTS = ROOT / "magical-vibes-card-test"
PKG = "src/test/java/com/github/laxika/magicalvibes/cards"

# test class -> (anchor the case goes before, method signature, case)
CASES = {
    "HordeAmbusherTest": (
        "    @Test\n    void turningFaceUpMakesTargetCreatureUnableToBlockThisTurn() {",
        "void blockingTwoCreaturesTriggersOnce()",
        """    @Test
    @CardUsed({HighGround.class})
    void blockingTwoCreaturesTriggersOnce() {
        addCreatureReady(player2, new HordeAmbusher());
        harness.addToBattlefield(player2, new HighGround());
        addCreatureReady(player1, new WetlandSambar()).setAttacking(true);
        addCreatureReady(player1, new WetlandSambar()).setAttacking(true);
        harness.setLife(player2, 20);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(0, 1)));

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

"""),
    "HamletCaptainTest": (
        "    @Test\n    @DisplayName(\"The attack trigger affects only Humans controlled at resolution\")",
        "void blockingTwoCreaturesTriggersOnce()",
        """    @Test
    @CardUsed({HighGround.class})
    @DisplayName("Blocking two creatures triggers once")
    void blockingTwoCreaturesTriggersOnce() {
        addCreatureReady(player2, new HamletCaptain());
        Permanent human = addCreatureReady(player2, new HamletCaptain());
        harness.addToBattlefield(player2, new HighGround());
        addCreatureReady(player1, new WalkingCorpse()).setAttacking(true);
        addCreatureReady(player1, new WalkingCorpse()).setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(0, 1)));

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(human.getPowerModifier()).isEqualTo(1);
        assertThat(human.getToughnessModifier()).isEqualTo(1);
    }

"""),
    "LuminousWakeTest": (
        "    @Test\n    @DisplayName(\"Blocking multiple creatures gains life only once\")",
        "void blockingMultipleCreaturesTriggersOnce()",
        """    @Test
    @DisplayName("Blocking multiple creatures triggers once")
    void blockingMultipleCreaturesTriggersOnce() {
        harness.setLife(player2, 20);
        Permanent blocker = addCreatureReady(player2, new AvatarOfHope());
        addReadyCreature(player1).setAttacking(true);
        addReadyCreature(player1).setAttacking(true);
        attachWake(player2, blocker);

        declareBlockers(List.of(new BlockerAssignment(0, 0), new BlockerAssignment(0, 1)));

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(24);
    }

"""),
    "AshmouthHoundTest": (
        "    @Test\n    @DisplayName(\"Becoming blocked creates a trigger that deals 1 damage to the blocker\")",
        "void blockingTwoCreaturesTriggersForEach()",
        """    @Test
    @CardUsed({HighGround.class})
    @DisplayName("Blocking two creatures triggers for each of them")
    void blockingTwoCreaturesTriggersForEach() {
        addCreatureReady(player2, new AshmouthHound());
        harness.addToBattlefield(player2, new HighGround());
        Permanent first = addCreatureReady(player1, new DarkthicketWolf());
        Permanent second = addCreatureReady(player1, new DarkthicketWolf());
        first.setAttacking(true);
        second.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(0, 1)));

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();
        assertThat(first.getMarkedDamage()).isEqualTo(1);
        assertThat(second.getMarkedDamage()).isEqualTo(1);
    }

"""),
    "ChainersTormentTest": (
        "    @Test\n    @DisplayName(\"Chapter I resolving deals 2 damage to opponent and gains 2 life\")",
        "void chapterIIsOneStackObject()",
        """    @Test
    @DisplayName("Chapter I is one stack object of both its effects")
    void chapterIIsOneStackObject() {
        harness.setHand(player1, List.of(new ChainersTorment()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).singleElement()
                .satisfies(entry -> assertThat(entry.getEffectsToResolve()).hasSize(2));
    }

"""),
}
HIGH_GROUND = "import com.github.laxika.magicalvibes.cards.h.HighGround;\n"
# test class -> (import the case needs, the import line it goes before)
IMPORTS = {
    "HordeAmbusherTest": (HIGH_GROUND, "import com.github.laxika.magicalvibes.cards.w.WetlandSambar;\n"),
    "HamletCaptainTest": (HIGH_GROUND, "import com.github.laxika.magicalvibes.cards.t.ThinkTwice;\n"),
    "AshmouthHoundTest": (HIGH_GROUND, "import com.github.laxika.magicalvibes.cards.l.Lumberknot;\n"),
}


def test_path(name):
    letter = name[0].lower()
    return TESTS / letter / PKG / letter / f"{name}.java"


def main():
    failed = []
    for name, (anchor, signature, case) in CASES.items():
        path = test_path(name)
        text = path.read_text()
        if signature in text:
            continue
        added, before = IMPORTS.get(name, (None, None))
        if text.count(anchor) != 1 or (before and text.count(before) != 1):
            failed.append(name)
            continue
        text = text.replace(anchor, case + anchor)
        if added and added not in text:
            text = text.replace(before, added + before)
        path.write_text(text)
    if failed:
        sys.exit("Edits not applied: " + ", ".join(failed))


if __name__ == "__main__":
    main()
