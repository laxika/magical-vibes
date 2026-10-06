package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HighGround;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RoyalTrooper.class, GrizzlyBears.class, HighGround.class})
class RoyalTrooperTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking triggers +2/+2 until end of turn")
    void blockingTriggersBoost() {
        Permanent trooper = addCreatureReady(player2, new RoyalTrooper());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(trooper.getPowerModifier()).isEqualTo(2);
        assertThat(trooper.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Royal Trooper's blocking boost resets at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent trooper = addCreatureReady(player2, new RoyalTrooper());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(trooper.getPowerModifier()).isEqualTo(2);
        assertThat(trooper.getToughnessModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(trooper.getPowerModifier()).isZero();
        assertThat(trooper.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The blocking boost waits for its trigger to resolve")
    void boostWaitsForResolution() {
        Permanent trooper = addCreatureReady(player2, new RoyalTrooper());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));

        assertThat(gd.stack).hasSize(1);
        assertThat(trooper.getPowerModifier()).isZero();
        assertThat(trooper.getToughnessModifier()).isZero();

        harness.passBothPriorities();

        assertThat(trooper.getPowerModifier()).isEqualTo(2);
        assertThat(trooper.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Remaining on the battlefield without blocking gives no boost")
    void notBlockingDoesNotBoost() {
        Permanent trooper = addCreatureReady(player2, new RoyalTrooper());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(trooper.getPowerModifier()).isZero();
        assertThat(trooper.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Becoming blocked while attacking does not grant the blocking boost")
    void attackingDoesNotBoost() {
        Permanent trooper = addCreatureReady(player1, new RoyalTrooper());
        trooper.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
        assertThat(trooper.getPowerModifier()).isZero();
        assertThat(trooper.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Blocking two attackers grants only one +2/+2 boost")
    void blockingMultipleAttackersTriggersOnce() {
        Permanent trooper = addCreatureReady(player2, new RoyalTrooper());
        harness.addToBattlefield(player2, new HighGround());
        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        firstAttacker.setAttacking(true);
        Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());
        secondAttacker.setAttacking(true);

        prepareDeclareBlockers();
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            gs.declareBlockers(gd, player2, List.of(
                    new BlockerAssignment(0, 0), new BlockerAssignment(0, 1)));
            resolveAllTriggers();
        });

        assertThat(trooper.getPowerModifier()).isEqualTo(2);
        assertThat(trooper.getToughnessModifier()).isEqualTo(2);
    }
}
