package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BoonReflection;
import com.github.laxika.magicalvibes.cards.l.LeylineOfPunishment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
    @DisplayName("Conversion happens before the opponent's own life-gain doubler")
    void conversionBeatsOpponentDoubler() {
        harness.addToBattlefield(player1, new TaintedRemedy());
        harness.addToBattlefield(player2, new BoonReflection());
        harness.setLife(player2, 20);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player2.getId(), 3));

        harness.assertLife(player2, 17);
    }
}
