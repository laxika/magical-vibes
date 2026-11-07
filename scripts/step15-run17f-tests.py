#!/usr/bin/env python3
"""Step 15 run 17f of effect-slot-refactor.md: card test cases for the enters-side event bindings.

Idempotent; run per letter module (``--module o``) or with no argument for every module this run touches.

- Pollywog Prodigy: a creature bigger in power and toughness evolves it once (its authored copy of evolve, which
  triggered a second time, is gone; CR 702.100d).
- Oran-Rief Hydra, Emeria Shepherd: the land is read as the ability resolves, so a Forest (Plains) that stops being
  one before then gives the plain branch.
- Emeria Shepherd: the Plains that leaves before resolution leaves the battlefield through the removal service (the
  case removed it from the battlefield list by hand, which no rules path does), so its last-known subtypes are kept.
- Champion of the Hareish: a creature that left before the ability resolves is read as it last existed.
"""
import argparse
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
TESTS = ROOT / "magical-vibes-card-test"
PKG = "src/test/java/com/github/laxika/magicalvibes/cards"

EDITS = {
    ("p", "PollywogProdigyTest"): {
        "marker": "evolvesOnceForACreatureBiggerInBothPowerAndToughness",
        "replace": [
            ("import com.github.laxika.magicalvibes.cards.f.Forest;\n",
             "import com.github.laxika.magicalvibes.cards.c.CentaurCourser;\n"
             "import com.github.laxika.magicalvibes.cards.f.Forest;\n"),
            ("@CardUsed({PollywogProdigy.class, GrizzlyBears.class, Forest.class, Shock.class, Memnite.class})",
             "@CardUsed({PollywogProdigy.class, GrizzlyBears.class, Forest.class, Shock.class, Memnite.class,\n"
             "        CentaurCourser.class})"),
        ],
        "append": """
    @Test
    @DisplayName("Its one evolve ability triggers once, so a 3/3 entering gives one counter")
    void evolvesOnceForACreatureBiggerInBothPowerAndToughness() {
        Permanent prodigy = harness.addToBattlefieldAndReturn(player1, new PollywogProdigy());

        harness.enterBattlefieldAndReturn(player1, new CentaurCourser());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(prodigy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
""",
    },
    ("o", "OranRiefHydraTest"): {
        "marker": "forestThatStopsBeingAForestBeforeResolutionGivesOneCounter",
        "replace": [],
        "append": """
    @Test
    @DisplayName("A Forest that stops being a Forest before the ability resolves gives one counter")
    void forestThatStopsBeingAForestBeforeResolutionGivesOneCounter() {
        Permanent hydra = harness.addToBattlefieldAndReturn(player1, new OranRiefHydra());
        harness.setHand(player1, List.of(new CanopyVista()));

        harness.playLand(player1, 0);
        harness.addToBattlefield(player1, new BloodMoon());
        harness.passBothPriorities();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
""",
    },
    ("e", "EmeriaShepherdTest"): {
        "marker": "plainsThatStopsBeingAPlainsBeforeResolutionOnlyReturnsToHand",
        "replace": [
            ("import com.github.laxika.magicalvibes.cards.c.CoralhelmGuide;\n",
             "import com.github.laxika.magicalvibes.cards.b.BloodMoon;\n"
             "import com.github.laxika.magicalvibes.cards.c.CanopyVista;\n"
             "import com.github.laxika.magicalvibes.cards.c.CoralhelmGuide;\n"),
            ("import com.github.laxika.magicalvibes.model.PendingInteraction;\n",
             "import com.github.laxika.magicalvibes.model.PendingInteraction;\n"
             "import com.github.laxika.magicalvibes.model.Permanent;\n"),
            ("@CardUsed({EmeriaShepherd.class, Forest.class, CoralhelmGuide.class, Plains.class, Dispel.class, "
             "GoblinWarPaint.class})",
             "@CardUsed({EmeriaShepherd.class, Forest.class, CoralhelmGuide.class, Plains.class, Dispel.class, "
             "GoblinWarPaint.class,\n        BloodMoon.class, CanopyVista.class})"),
            ("""        harness.playLand(player1, 0);
        chooseLandfallTarget(returned);
        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard().getId().equals(plains.getId()));
        harness.setHand(player1, List.of(plains));
        harness.passBothPriorities();""",
             """        harness.playLand(player1, 0);
        chooseLandfallTarget(returned);
        Permanent plainsPermanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(plains.getId()))
                .findFirst().orElseThrow();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, plainsPermanent));
        harness.passBothPriorities();"""),
        ],
        "append": """
    @Test
    void plainsThatStopsBeingAPlainsBeforeResolutionOnlyReturnsToHand() {
        harness.addToBattlefield(player1, new EmeriaShepherd());
        Card returned = new CoralhelmGuide();
        harness.setGraveyard(player1, List.of(returned));
        harness.setHand(player1, List.of(new CanopyVista()));

        harness.playLand(player1, 0);
        chooseLandfallTarget(returned);
        harness.addToBattlefield(player1, new BloodMoon());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(returned);
        harness.assertNotOnBattlefield(player1, "Coralhelm Guide");
    }
""",
    },
    ("c", "ChampionOfTheHareishTest"): {
        "marker": "readsACreatureThatLeftBeforeResolutionAsItLastExisted",
        "replace": [],
        "append": """
    @Test
    void readsACreatureThatLeftBeforeResolutionAsItLastExisted() {
        Permanent champion = harness.enterBattlefieldAndReturn(player1, new ChampionOfTheHareish());
        harness.handleListChoice(player1, "RABBIT");

        Permanent firstBears = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, firstBears));
        harness.passBothPriorities();
        assertThat(gd.getBuddyList(player1.getId())).containsExactlyInAnyOrder(
                CardSubtype.RABBIT, CardSubtype.BEAR);

        Permanent secondBears = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, secondBears));
        harness.passBothPriorities();
        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
""",
    },
}


def convert(letter, name, edit):
    path = TESTS / letter / PKG / letter / f"{name}.java"
    text = path.read_text()
    if edit["marker"] in text:
        print(f"unchanged {path.relative_to(ROOT)}")
        return
    for old, new in edit["replace"]:
        if text.count(old) != 1:
            sys.exit(f"{path}: expected one occurrence of {old!r}")
        text = text.replace(old, new)
    body, _, _ = text.rstrip().rpartition("}")
    text = body.rstrip() + "\n" + edit["append"] + "}\n"
    path.write_text(text)
    print(f"converted {path.relative_to(ROOT)}")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--module", help="letter module to convert (default: all this run touches)")
    args = parser.parse_args()
    for (letter, name), edit in EDITS.items():
        if args.module is None or args.module == letter:
            convert(letter, name, edit)


if __name__ == "__main__":
    main()
