package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(MenacingOgre.class)
class MenacingOgreTest extends BaseCardTest {

    @Test
    void highestNumberPlayersLoseLifeAndControllerGetsCountersWhenTied() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent ogre = castMenacingOgre();

        harness.handleXValueChosen(player1, 5);
        harness.handleXValueChosen(player2, 5);

        harness.assertLife(player1, 15);
        harness.assertLife(player2, 15);
        assertThat(ogre.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void controllerDoesNotGetCountersWhenOpponentChoosesHigherNumber() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent ogre = castMenacingOgre();

        harness.handleXValueChosen(player1, 2);
        harness.handleXValueChosen(player2, 6);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 14);
        assertThat(ogre.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void choosingZeroCausesNoLifeLossAndStillAddsCountersWhenTied() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent ogre = castMenacingOgre();

        harness.handleXValueChosen(player1, 0);
        harness.handleXValueChosen(player2, 0);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(ogre.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent castMenacingOgre() {
        harness.castFromHand(player1, new MenacingOgre(), "{3}{R}{R}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.XValueChoice.class);
        return findPermanent(player1, "Menacing Ogre");
    }
}
