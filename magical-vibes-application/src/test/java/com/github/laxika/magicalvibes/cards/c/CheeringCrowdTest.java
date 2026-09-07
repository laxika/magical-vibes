package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(CheeringCrowd.class)
class CheeringCrowdTest extends BaseCardTest {

    @Test
    @DisplayName("The active player may add a counter and then receives mana equal to the counters")
    void activePlayerMayAddCounterAndMana() {
        Permanent crowd = harness.addToBattlefieldAndReturn(player1, new CheeringCrowd());
        crowd.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        advanceToPrecombatMain(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(crowd.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Declining the trigger does not add a counter or mana")
    void activePlayerMayDecline() {
        Permanent crowd = harness.addToBattlefieldAndReturn(player1, new CheeringCrowd());

        advanceToPrecombatMain(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(crowd.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    private void advanceToPrecombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
