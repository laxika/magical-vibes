#!/usr/bin/env python3
"""EffectSlot refactor step 15, run 6: card tests of the death counter distributions.

Idempotent; run per letter test module:

    python3 scripts/step15-distribution-tests.py magical-vibes-card-test/j

The targets and division of Jugan's, Quirion Beastcaller's and Grand Ossuary's death distributions are now chosen as
the ability goes on the stack, so a test that staged nothing no longer reaches the ability's resolution:

- JuganTheRisingStarTest: "Accepting with no targets places no counters" becomes a test of choosing the targets and
  the division when nothing is staged.
- QuirionBeastcallerTest: "Death distribution can choose zero targets" becomes a test of choosing among your own
  creatures only, and a test that the ability with no counters to distribute leaves the stack.
- GrandOssuaryTest: a new test that the dying creature's controller chooses among their own creatures.
"""
import pathlib
import sys

PKG = "com.github.laxika.magicalvibes"
PENDING = PKG + ".model.PendingInteraction"

JUGAN_OLD = '''    @Test
    @DisplayName("Accepting with no targets places no counters")
    void deathDistributeCanChooseNoTargets() {
        Permanent jugan = addCreatureReady(player1, new JuganTheRisingStar());
        jugan.tap();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new DeathcurseOgre());

        gd.pendingETBDamageAssignments = Map.of();

        killJugan(jugan);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
'''
JUGAN_NEW = '''    @Test
    @DisplayName("The controller chooses the targets and the division as the ability goes on the stack")
    void controllerChoosesTargetsAndDivisionAsTheAbilityGoesOnTheStack() {
        Permanent jugan = addCreatureReady(player1, new JuganTheRisingStar());
        jugan.tap();
        Permanent ownOgre = harness.addToBattlefieldAndReturn(player1, new DeathcurseOgre());
        Permanent oppOgre = harness.addToBattlefieldAndReturn(player2, new DeathcurseOgre());

        killJugan(jugan);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).contains(ownOgre.getId(), oppOgre.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(ownOgre.getId(), oppOgre.getId()));
        harness.handleListChoice(player1, "3");
        harness.handleListChoice(player1, "2");
        assertThat(gd.stack.getFirst().getDamageAssignments())
                .containsEntry(ownOgre.getId(), 3).containsEntry(oppOgre.getId(), 2);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(ownOgre.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(oppOgre.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
'''

QUIRION_OLD = '''    @Test
    @DisplayName("Death distribution can choose zero targets")
    void deathDistributionCanChooseZeroTargets() {
        Permanent beastcaller = addCreatureReady(player1, new QuirionBeastcaller());
        beastcaller.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        beastcaller.tap();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        gd.pendingETBDamageAssignments = Map.of();

        killBeastcaller(beastcaller);

        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
'''
QUIRION_NEW = '''    @Test
    @DisplayName("The controller chooses among creatures they control as the ability goes on the stack")
    void controllerChoosesAmongOwnCreaturesAsTheAbilityGoesOnTheStack() {
        Permanent beastcaller = addCreatureReady(player1, new QuirionBeastcaller());
        beastcaller.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        beastcaller.tap();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        killBeastcaller(beastcaller);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactly(bears.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(opponentGiant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("With no +1/+1 counters on it, its death distributes nothing and asks for no targets")
    void deathWithNoCountersAsksForNoTargets() {
        Permanent beastcaller = addCreatureReady(player1, new QuirionBeastcaller());
        beastcaller.tap();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        killBeastcaller(beastcaller);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
'''

OSSUARY_ANCHOR = '''    @Test
    void chaosStillPlaneswalksWhenNeitherPlayerControlsCreatures() {'''
OSSUARY_NEW = '''    @Test
    void dyingCreaturesControllerChoosesAmongTheirOwnCreatures() {
        Permanent dying = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        dying.tap();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent firstRecipient = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondRecipient = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Assassinate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, dying.getId());

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(firstRecipient.getId(), secondRecipient.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(firstRecipient.getId(), secondRecipient.getId()));
        harness.handleListChoice(player2, "2");
        harness.handleListChoice(player2, "1");
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        harness.passBothPriorities();

        assertThat(firstRecipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(secondRecipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

''' + OSSUARY_ANCHOR

EDITS = {
    "JuganTheRisingStarTest": [(JUGAN_OLD, JUGAN_NEW)],
    "QuirionBeastcallerTest": [(QUIRION_OLD, QUIRION_NEW)],
    "GrandOssuaryTest": [(OSSUARY_ANCHOR, OSSUARY_NEW)],
}
DONE_MARKERS = {
    "JuganTheRisingStarTest": "controllerChoosesTargetsAndDivisionAsTheAbilityGoesOnTheStack",
    "QuirionBeastcallerTest": "controllerChoosesAmongOwnCreaturesAsTheAbilityGoesOnTheStack",
    "GrandOssuaryTest": "dyingCreaturesControllerChoosesAmongTheirOwnCreatures",
}


def add_import(src, fqcn):
    line = f"import {fqcn};\n"
    if line in src:
        return src
    imports = [l for l in src.splitlines(keepends=True) if l.startswith("import " + PKG)]
    for existing in imports:
        if existing > line:
            return src.replace(existing, line + existing, 1)
    return src.replace(imports[-1], imports[-1] + line, 1)


def convert(path):
    name = path.stem
    src = path.read_text()
    if DONE_MARKERS[name] in src:
        return
    for old, new in EDITS[name]:
        if old not in src:
            raise SystemExit(f"{path}: expected text not found")
        src = src.replace(old, new, 1)
    src = add_import(src, PENDING)
    path.write_text(src)
    print(f"converted {path}")


def main(module):
    for path in sorted(pathlib.Path(module).rglob("*.java")):
        if path.stem in EDITS:
            convert(path)


if __name__ == "__main__":
    main(sys.argv[1])
