package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BraidwoodCup.class})
class BraidwoodCupTest extends BaseCardTest {

    @Test
    void tappingBraidwoodCupGainsOneLife() {
        Permanent cup = harness.addToBattlefieldAndReturn(player1, new BraidwoodCup());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore + 1);
        assertThat(cup.isTapped()).isTrue();
    }

    @Test
    void cannotActivateWhileTapped() {
        harness.addToBattlefield(player1, new BraidwoodCup());

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void lifeIsGainedOnlyWhenTheAbilityResolves() {
        Permanent cup = harness.addToBattlefieldAndReturn(player1, new BraidwoodCup());
        harness.setLife(player1, 10);
        harness.setLife(player2, 15);

        harness.activateAbility(player1, 0, null, null);

        assertThat(cup.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, 10);
        harness.assertLife(player2, 15);

        harness.passBothPriorities();

        harness.assertLife(player1, 11);
        harness.assertLife(player2, 15);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canActivateAgainAfterUntapping() {
        harness.addToBattlefield(player1, new BraidwoodCup());
        harness.setLife(player1, 10);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.performUntapStep(player1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 12);
    }

    @Test
    void opponentControlledCupGainsLifeForItsController() {
        harness.addToBattlefield(player2, new BraidwoodCup());
        harness.setLife(player1, 10);
        harness.setLife(player2, 15);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 16);
    }
}
