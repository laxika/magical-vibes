package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VampireOpportunist.class})
class VampireOpportunistTest extends BaseCardTest {

    @Test
    void activationMakesEachOpponentLoseLifeAndControllerGainLife() {
        Permanent opportunist = addCreatureReady(player1, new VampireOpportunist());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 18);
        assertThat(opportunist.isTapped()).isFalse();
    }

    @Test
    void canActivateWhileTapped() {
        Permanent opportunist = addCreatureReady(player1, new VampireOpportunist());
        opportunist.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
        assertThat(opportunist.isTapped()).isTrue();
    }

    @Test
    void canActivateWhileSummoningSick() {
        Permanent opportunist = harness.addToBattlefieldAndReturn(player1, new VampireOpportunist());
        opportunist.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
        assertThat(opportunist.isTapped()).isFalse();
    }

    @Test
    void canActivateTwiceWithoutUntapping() {
        addCreatureReady(player1, new VampireOpportunist());
        harness.addMana(player1, ManaColor.COLORLESS, 12);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 16);
    }
}
