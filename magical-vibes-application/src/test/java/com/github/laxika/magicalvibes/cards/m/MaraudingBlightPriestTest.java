package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MaraudingBlightPriestTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent loses 1 life when controller gains life")
    void eachOpponentLosesOneLifeWhenControllerGainsLife() {
        harness.addToBattlefield(player1, new MaraudingBlightPriest());

        int controllerStartingLife = gd.getLife(player1.getId());
        int opponentStartingLife = gd.getLife(player2.getId());

        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerStartingLife + 3);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentStartingLife - 1);
    }

    @Test
    @DisplayName("Does not trigger when an opponent gains life")
    void doesNotTriggerWhenOpponentGainsLife() {
        harness.addToBattlefield(player1, new MaraudingBlightPriest());

        int opponentStartingLife = gd.getLife(player2.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new AngelOfMercy()));
        harness.addMana(player2, ManaColor.WHITE, 5);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentStartingLife + 3);
    }
}
