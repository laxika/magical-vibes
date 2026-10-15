package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BoonReflection;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PlagueDrone.class, BoonReflection.class})
class PlagueDroneTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent's life gain becomes an equal amount of life loss")
    void opponentLifeGainBecomesLifeLoss() {
        harness.addToBattlefield(player1, new PlagueDrone());
        harness.setLife(player2, 20);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player2.getId(), 4));

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("The controller's own life gain is unaffected")
    void controllerLifeGainIsUnaffected() {
        harness.addToBattlefield(player1, new PlagueDrone());
        harness.setLife(player1, 20);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 4));

        harness.assertLife(player1, 24);
    }

    @Test
    void increasingOpponentLifeTotalBecomesLifeLoss() {
        harness.addToBattlefield(player1, new PlagueDrone());
        harness.setLife(player2, 20);

        harness.inMutationScope(() -> harness.getLifeSupport().applySetLifeTotal(gd, player2.getId(), 25));

        harness.assertLife(player2, 15);
    }

    @Test
    void multipleDronesDoNotMultiplyLifeLoss() {
        harness.addToBattlefield(player1, new PlagueDrone());
        harness.addToBattlefield(player1, new PlagueDrone());
        harness.setLife(player2, 20);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player2.getId(), 4));

        harness.assertLife(player2, 16);
    }

    @Test
    void dronesOnBothSidesReplaceBothPlayersLifeGain() {
        harness.addToBattlefield(player1, new PlagueDrone());
        harness.addToBattlefield(player2, new PlagueDrone());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.inMutationScope(() -> {
            harness.getLifeSupport().applyGainLife(gd, player1.getId(), 4);
            harness.getLifeSupport().applyGainLife(gd, player2.getId(), 4);
        });

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 16);
    }

    @Test
    void zeroLifeGainDoesNotChangeLifeOrRecordGain() {
        harness.addToBattlefield(player1, new PlagueDrone());
        harness.setLife(player2, 20);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player2.getId(), 0));

        harness.assertLife(player2, 20);
        assertThat(gd.lifeGainedThisTurn.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    void replacedGainIsNotRecordedAsLifeGained() {
        harness.addToBattlefield(player1, new PlagueDrone());
        harness.setLife(player2, 20);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player2.getId(), 4));

        harness.assertLife(player2, 16);
        assertThat(gd.lifeGainedThisTurn.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @CardUsed({PlagueDrone.class, BoonReflection.class})
    void affectedPlayerMustChooseBetweenDroneAndLifeGainDoubler() {
        harness.addToBattlefield(player1, new PlagueDrone());
        harness.addToBattlefield(player2, new BoonReflection());
        harness.setLife(player2, 20);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player2.getId(), 3));

        assertThat(gd.pendingInteractions).isNotEmpty();
        harness.assertLife(player2, 20);
    }
}
