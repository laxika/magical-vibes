package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.CircleOfTheMoonDruid;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WizenedGithzerai.class, GrizzlyBears.class, CircleOfTheMoonDruid.class})
class WizenedGithzeraiTest extends BaseCardTest {

    @Test
    void becomingBlockedPerpetuallyReducesEachBlockersPower() {
        addCreatureReady(player1, new WizenedGithzerai());
        Permanent firstBlocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondBlocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, firstBlocker)).isZero();
        assertThat(gqs.getEffectivePower(gd, secondBlocker)).isZero();
    }

    @Test
    void perpetualReductionRemainsAfterTheTurnEnds() {
        addCreatureReady(player1, new WizenedGithzerai());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, blocker)).isZero();
    }

    @Test
    void blockingWizenedGithzeraiDoesNotTriggerItsAbility() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new WizenedGithzerai());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
    }

    @Test
    void perpetualReductionAppliesAfterTheBlockersBasePowerChanges() {
        addCreatureReady(player1, new WizenedGithzerai());
        Permanent blocker = addCreatureReady(player2, new CircleOfTheMoonDruid());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, blocker)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(2);
    }

    @Test
    void creaturesThatDoNotBlockAreUnaffected() {
        addCreatureReady(player1, new WizenedGithzerai());
        Permanent blocker = addCreatureReady(player2, new CircleOfTheMoonDruid());
        Permanent nonblocker = addCreatureReady(player2, new CircleOfTheMoonDruid());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, blocker)).isZero();
        assertThat(gqs.getEffectivePower(gd, nonblocker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(4);
    }
}
