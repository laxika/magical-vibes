package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GeyserfieldStalker.class, Forest.class})
class GeyserfieldStalkerTest extends BaseCardTest {

    @Test
    void landsEnteringWithoutBeingPlayedGiveCumulativeBoostsOnlyToTheirController() {
        Permanent stalker = harness.addToBattlefieldAndReturn(player1, new GeyserfieldStalker());
        Permanent opposingStalker = harness.addToBattlefieldAndReturn(player2, new GeyserfieldStalker());

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.enterBattlefieldAndReturn(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, stalker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, stalker)).isEqualTo(2);

        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, stalker)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, stalker)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, opposingStalker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opposingStalker)).isEqualTo(2);
    }

    @Test
    void menaceRejectsASingleBlocker() {
        addCreatureReady(player1, new GeyserfieldStalker());
        addCreatureReady(player2, new GeyserfieldStalker());
        addCreatureReady(player2, new GeyserfieldStalker());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    void menaceAllowsTwoBlockers() {
        addCreatureReady(player1, new GeyserfieldStalker());
        Permanent firstBlocker = addCreatureReady(player2, new GeyserfieldStalker());
        Permanent secondBlocker = addCreatureReady(player2, new GeyserfieldStalker());

        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2,
                        List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Landfall gives Geyserfield Stalker +2/+2 until end of turn")
    void landfallBoostsSelf() {
        Permanent stalker = harness.addToBattlefieldAndReturn(player1, new GeyserfieldStalker());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, stalker)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, stalker)).isEqualTo(4);
    }

    @Test
    @DisplayName("Landfall boost wears off at end of turn")
    void landfallBoostWearsOff() {
        Permanent stalker = harness.addToBattlefieldAndReturn(player1, new GeyserfieldStalker());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, stalker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, stalker)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's land does not trigger Geyserfield Stalker")
    void opponentLandDoesNotTrigger() {
        Permanent stalker = harness.addToBattlefieldAndReturn(player1, new GeyserfieldStalker());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, stalker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, stalker)).isEqualTo(2);
    }
}
