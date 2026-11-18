package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScytheLeopard.class, Forest.class})
class ScytheLeopardTest extends BaseCardTest {

    @Test
    @DisplayName("A land entering without being played triggers landfall and boosts accumulate")
    void landsEnteringWithoutBeingPlayedGiveCumulativeBoosts() {
        Permanent leopard = harness.addToBattlefieldAndReturn(player1, new ScytheLeopard());

        harness.enterBattlefieldAndReturn(player1, new Forest());
        assertThat(gqs.getEffectivePower(gd, leopard)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, leopard)).isEqualTo(1);
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, leopard)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, leopard)).isEqualTo(2);

        harness.enterBattlefieldAndReturn(player1, new Forest());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, leopard)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, leopard)).isEqualTo(3);
    }

    @Test
    @DisplayName("Each Scythe Leopard gets its own boost from a land entering")
    void multipleLeopardsEachGetTheirOwnBoost() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ScytheLeopard());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ScytheLeopard());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
    }

    @Test
    @DisplayName("A nonland entering does not trigger landfall")
    void nonlandEnteringDoesNotTrigger() {
        Permanent leopard = harness.addToBattlefieldAndReturn(player1, new ScytheLeopard());

        harness.enterBattlefieldAndReturn(player1, new ScytheLeopard());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, leopard)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, leopard)).isEqualTo(1);
    }

    @Test
    @DisplayName("Landfall gives Scythe Leopard +1/+1 until end of turn")
    void landfallBoostsScytheLeopard() {
        Permanent leopard = harness.addToBattlefieldAndReturn(player1, new ScytheLeopard());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, leopard)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, leopard)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's land does not trigger Scythe Leopard")
    void opponentLandDoesNotTrigger() {
        Permanent leopard = harness.addToBattlefieldAndReturn(player1, new ScytheLeopard());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, leopard)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, leopard)).isEqualTo(1);
    }

    @Test
    @DisplayName("Landfall boost wears off at end of turn")
    void landfallBoostWearsOff() {
        Permanent leopard = harness.addToBattlefieldAndReturn(player1, new ScytheLeopard());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, leopard)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, leopard)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, leopard)).isEqualTo(1);
    }
}
