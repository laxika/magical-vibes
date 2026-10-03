package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodriteInvoker.class})
class BloodriteInvokerTest extends BaseCardTest {

    @Test
    @DisplayName("Its ability makes the target player lose 3 life and its controller gain 3 life")
    void drainsTargetPlayer() {
        harness.addToBattlefield(player1, new BloodriteInvoker());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Its ability can target its controller")
    void canTargetController() {
        harness.addToBattlefield(player1, new BloodriteInvoker());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Invoker can activate repeatedly")
    void canActivateRepeatedlyWhileTappedAndSummoningSick() {
        var invoker = harness.addToBattlefieldAndReturn(player1, new BloodriteInvoker());
        invoker.setTapped(true);
        invoker.setSummoningSick(true);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 16);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 26);
        harness.assertLife(player2, 14);
        assertThat(invoker.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Life gain belongs to the activating controller")
    void gainsLifeForOtherController() {
        harness.addToBattlefield(player2, new BloodriteInvoker());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player2, ManaColor.BLACK, 8);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 23);
    }
}
