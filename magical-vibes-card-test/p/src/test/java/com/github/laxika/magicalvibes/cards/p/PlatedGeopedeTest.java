package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PlatedGeopede.class, Forest.class})
class PlatedGeopedeTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall gives Plated Geopede +2/+2 until end of turn")
    void landfallBoostsPlatedGeopede() {
        Permanent geopede = harness.addToBattlefieldAndReturn(player1, new PlatedGeopede());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(geopede.getEffectivePower()).isEqualTo(3);
        assertThat(geopede.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("An opponent's land does not trigger Plated Geopede")
    void opponentLandDoesNotTrigger() {
        Permanent geopede = harness.addToBattlefieldAndReturn(player1, new PlatedGeopede());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(geopede.getEffectivePower()).isEqualTo(1);
        assertThat(geopede.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Landfall boost wears off at end of turn")
    void landfallBoostWearsOff() {
        Permanent geopede = harness.addToBattlefieldAndReturn(player1, new PlatedGeopede());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        assertThat(geopede.getEffectivePower()).isEqualTo(3);
        assertThat(geopede.getEffectiveToughness()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(geopede.getEffectivePower()).isEqualTo(1);
        assertThat(geopede.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Landfall waits for resolution and triggers for lands put onto the battlefield")
    void landEnteringWithoutBeingPlayedTriggersLandfall() {
        Permanent geopede = harness.addToBattlefieldAndReturn(player1, new PlatedGeopede());

        harness.enterBattlefieldAndReturn(player1, new Forest());

        assertThat(gd.stack).hasSize(1);
        assertThat(geopede.getEffectivePower()).isEqualTo(1);
        assertThat(geopede.getEffectiveToughness()).isEqualTo(1);

        harness.passBothPriorities();

        assertThat(geopede.getEffectivePower()).isEqualTo(3);
        assertThat(geopede.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Multiple landfall triggers stack and all boosts expire at end of turn")
    void multipleLandfallBoostsAccumulate() {
        Permanent geopede = harness.addToBattlefieldAndReturn(player1, new PlatedGeopede());

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.enterBattlefieldAndReturn(player1, new Forest());
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        assertThat(geopede.getEffectivePower()).isEqualTo(3);
        assertThat(geopede.getEffectiveToughness()).isEqualTo(3);
        harness.passBothPriorities();
        assertThat(geopede.getEffectivePower()).isEqualTo(5);
        assertThat(geopede.getEffectiveToughness()).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(geopede.getEffectivePower()).isEqualTo(1);
        assertThat(geopede.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Each Plated Geopede boosts itself rather than other creatures")
    void eachGeopedeBoostsOnlyItself() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new PlatedGeopede());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new PlatedGeopede());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new PlatedGeopede());

        harness.enterBattlefieldAndReturn(player1, new Forest());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getEffectivePower()).isEqualTo(3);
        assertThat(first.getEffectiveToughness()).isEqualTo(3);
        assertThat(second.getEffectivePower()).isEqualTo(3);
        assertThat(second.getEffectiveToughness()).isEqualTo(3);
        assertThat(opponent.getEffectivePower()).isEqualTo(1);
        assertThat(opponent.getEffectiveToughness()).isEqualTo(1);
    }
}
