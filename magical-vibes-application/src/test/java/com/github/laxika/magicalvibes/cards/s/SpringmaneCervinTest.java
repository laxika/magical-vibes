package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({SpringmaneCervin.class})
class SpringmaneCervinTest extends BaseCardTest {

    @Test
    @DisplayName("When Springmane Cervin enters, its controller gains 2 life")
    void gainsLifeWhenItEnters() {
        harness.setHand(player1, List.of(new SpringmaneCervin()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player1, 10);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 12);
    }

    @Test
    @DisplayName("Life gain waits for the enter trigger to resolve")
    void lifeGainUsesTheStack() {
        harness.setHand(player1, List.of(new SpringmaneCervin()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player1, 10);
        harness.setLife(player2, 15);

        harness.castCreature(player1, 0);
        harness.assertLife(player1, 10);

        harness.passBothPriorities();
        harness.assertLife(player1, 10);

        harness.passBothPriorities();
        harness.assertLife(player1, 12);
        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Entering without being cast gains life only for the entering creature's controller")
    void enteringWithoutCastingGainsLifeForOpponentController() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 15);

        harness.enterBattlefieldAndReturn(player2, new SpringmaneCervin());
        harness.assertLife(player2, 15);

        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 17);
    }
}
