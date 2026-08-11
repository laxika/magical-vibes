package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BrinebornCutthroatTest extends BaseCardTest {

    @Test
    @DisplayName("Controller casting a spell during an opponent's turn puts a +1/+1 counter on it")
    void controllerSpellDuringOpponentsTurnAddsCounter() {
        harness.addToBattlefield(player1, new BrinebornCutthroat());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        Permanent cutthroat = findPermanent(player1, "Brineborn Cutthroat");
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(cutthroat.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(harness.getGameQueryService().getEffectivePower(gd, cutthroat)).isEqualTo(3);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, cutthroat)).isEqualTo(2);
    }

    @Test
    @DisplayName("Controller casting a spell during their own turn does not trigger it")
    void controllerSpellDuringOwnTurnDoesNotAddCounter() {
        harness.addToBattlefield(player1, new BrinebornCutthroat());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        Permanent cutthroat = findPermanent(player1, "Brineborn Cutthroat");
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(cutthroat.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent casting a spell does not trigger it")
    void opponentSpellDoesNotAddCounter() {
        harness.addToBattlefield(player1, new BrinebornCutthroat());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        Permanent cutthroat = findPermanent(player1, "Brineborn Cutthroat");
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(cutthroat.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
