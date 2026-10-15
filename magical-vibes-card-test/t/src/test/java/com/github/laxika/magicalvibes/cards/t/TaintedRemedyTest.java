package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AlhammarretsArchive;
import com.github.laxika.magicalvibes.cards.c.ClericOfTheForwardOrder;
import com.github.laxika.magicalvibes.cards.l.LeylineOfPunishment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TaintedRemedy.class, AlhammarretsArchive.class, ClericOfTheForwardOrder.class, LeylineOfPunishment.class})
class TaintedRemedyTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent's life gain becomes an equal amount of life loss")
    void opponentLifeGainBecomesLoss() {
        harness.addToBattlefield(player1, new TaintedRemedy());
        harness.setLife(player2, 20);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player2.getId(), 4));

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("The controller's own life gain is unaffected")
    void controllerLifeGainUnaffected() {
        harness.addToBattlefield(player1, new TaintedRemedy());
        harness.setLife(player1, 20);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 4));

        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Setting an opponent's life total higher becomes life loss instead")
    void setLifeTotalHigherBecomesLoss() {
        harness.addToBattlefield(player1, new TaintedRemedy());
        harness.setLife(player2, 20);

        harness.inMutationScope(() -> harness.getLifeSupport().applySetLifeTotal(gd, player2.getId(), 25));

        harness.assertLife(player2, 15); // gaining 5 replaced by losing 5
    }

    @Test
    @DisplayName("Gaining 0 life is not a life-gain event, so nothing is replaced (CR 119.10)")
    void zeroLifeGainIsNotReplaced() {
        harness.addToBattlefield(player1, new TaintedRemedy());
        harness.setLife(player2, 20);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player2.getId(), 0));

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An opponent who can't gain life doesn't lose life either")
    void opponentWhoCantGainLifeDoesNotLoseLife() {
        harness.addToBattlefield(player1, new TaintedRemedy());
        harness.addToBattlefield(player1, new LeylineOfPunishment());
        harness.setLife(player2, 20);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player2.getId(), 4));

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The affected player must choose the order of life-gain replacements")
    void opponentChoosesReplacementOrder() {
        harness.addToBattlefield(player1, new TaintedRemedy());
        harness.addToBattlefield(player2, new AlhammarretsArchive());
        harness.setLife(player2, 20);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player2.getId(), 3));

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A resolved opponent's Cleric trigger loses life instead of gaining it")
    void replacesResolvedLifeGainTrigger() {
        harness.addToBattlefield(player1, new TaintedRemedy());
        harness.castFromHand(player2, new ClericOfTheForwardOrder(), "{1}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
        assertThat(gd.lifeGainedThisTurn.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Two copies do not multiply the life loss")
    void multipleCopiesReplaceGainOnlyOnce() {
        harness.addToBattlefield(player1, new TaintedRemedy());
        harness.addToBattlefield(player1, new TaintedRemedy());

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player2.getId(), 4));

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Setting life lower remains ordinary life loss")
    void settingLifeLowerIsUnaffected() {
        harness.addToBattlefield(player1, new TaintedRemedy());

        harness.inMutationScope(() -> harness.getLifeSupport().applySetLifeTotal(gd, player2.getId(), 15));

        harness.assertLife(player2, 15);
    }
}
