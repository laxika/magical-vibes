package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GeyserfieldStalker.class, Forest.class})
class GeyserfieldStalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall gives Geyserfield Stalker +2/+2 until end of turn")
    void landfallBoostsSelf() {
        Permanent stalker = harness.addToBattlefieldAndReturn(player1, new GeyserfieldStalker());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(stalker.getEffectivePower()).isEqualTo(5);
        assertThat(stalker.getEffectiveToughness()).isEqualTo(4);
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

        assertThat(stalker.getEffectivePower()).isEqualTo(3);
        assertThat(stalker.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's land does not trigger Geyserfield Stalker")
    void opponentLandDoesNotTrigger() {
        Permanent stalker = harness.addToBattlefieldAndReturn(player1, new GeyserfieldStalker());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(stalker.getEffectivePower()).isEqualTo(3);
        assertThat(stalker.getEffectiveToughness()).isEqualTo(2);
    }
}
