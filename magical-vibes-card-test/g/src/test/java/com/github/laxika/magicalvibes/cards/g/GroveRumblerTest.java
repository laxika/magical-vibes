package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GroveRumbler.class, Forest.class})
class GroveRumblerTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall gives Grove Rumbler +2/+2 until end of turn")
    void landfallBoostsSelf() {
        Permanent rumbler = harness.addToBattlefieldAndReturn(player1, new GroveRumbler());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(rumbler.getEffectivePower()).isEqualTo(5);
        assertThat(rumbler.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("An opponent's land does not trigger Grove Rumbler")
    void opponentLandDoesNotTrigger() {
        Permanent rumbler = harness.addToBattlefieldAndReturn(player1, new GroveRumbler());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(rumbler.getEffectivePower()).isEqualTo(3);
        assertThat(rumbler.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Landfall boost wears off at end of turn")
    void landfallBoostWearsOff() {
        Permanent rumbler = harness.addToBattlefieldAndReturn(player1, new GroveRumbler());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        assertThat(rumbler.getEffectivePower()).isEqualTo(5);
        assertThat(rumbler.getEffectiveToughness()).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(rumbler.getEffectivePower()).isEqualTo(3);
        assertThat(rumbler.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Each land entering without being played adds a separate landfall bonus")
    void multipleLandEntriesStack() {
        Permanent rumbler = harness.addToBattlefieldAndReturn(player1, new GroveRumbler());

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.enterBattlefieldAndReturn(player1, new Forest());

        assertThat(gd.stack).hasSize(2);
        assertThat(rumbler.getEffectivePower()).isEqualTo(3);
        assertThat(rumbler.getEffectiveToughness()).isEqualTo(3);

        harness.passBothPriorities();
        assertThat(rumbler.getEffectivePower()).isEqualTo(5);
        assertThat(rumbler.getEffectiveToughness()).isEqualTo(5);

        harness.passBothPriorities();
        assertThat(rumbler.getEffectivePower()).isEqualTo(7);
        assertThat(rumbler.getEffectiveToughness()).isEqualTo(7);
    }

    @Test
    @DisplayName("A creature entering does not trigger landfall")
    void nonlandDoesNotTrigger() {
        Permanent rumbler = harness.addToBattlefieldAndReturn(player1, new GroveRumbler());

        harness.enterBattlefieldAndReturn(player1, new GroveRumbler());

        assertThat(gd.stack).isEmpty();
        assertThat(rumbler.getEffectivePower()).isEqualTo(3);
        assertThat(rumbler.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Landfall-enhanced Grove Rumbler tramples over a blocker")
    void landfallBoostContributesToTrampleDamage() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new GroveRumbler());
        Permanent blocker = addCreatureReady(player2, new GroveRumbler());
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 3,
                player2.getId(), 2
        ));

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player2, "Grove Rumbler");
        harness.assertOnBattlefield(player1, "Grove Rumbler");
    }
}
