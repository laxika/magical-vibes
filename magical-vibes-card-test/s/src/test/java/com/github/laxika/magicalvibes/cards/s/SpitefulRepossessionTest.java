package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpitefulRepossession.class, Forest.class})
class SpitefulRepossessionTest extends BaseCardTest {

    @Test
    void dealsEachOpponentTheirLandSurplusAndCreatesThatManyTreasures() {
        addLands(player1, 2);
        addLands(player2, 5);

        castAndResolve();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 17);
        assertThat(findPermanents(player1, "Treasure")).hasSize(3);
    }

    @Test
    void doesNothingWhenOpponentDoesNotControlMoreLands() {
        addLands(player1, 3);
        addLands(player2, 2);

        castAndResolve();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void countsLandsAtResolution() {
        addLands(player1, 2);
        addLands(player2, 2);
        harness.castFromHand(player1, new SpitefulRepossession(), "{4}{R}");

        harness.addToBattlefield(player2, new Forest());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void doesNothingWhenLandCountsAreEqual() {
        addLands(player1, 3);
        addLands(player2, 3);

        castAndResolve();

        harness.assertLife(player2, 20);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void createsTreasuresWhenControllerHasNoLands() {
        addLands(player2, 2);

        castAndResolve();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    @Test
    void createsTreasuresOnlyForDamageRemainingAfterPrevention() {
        addLands(player1, 2);
        addLands(player2, 5);
        gd.playerDamagePreventionShields.put(player2.getId(), 2);

        castAndResolve();

        harness.assertLife(player2, 19);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void createsNoTreasuresWhenAllDamageIsPrevented() {
        addLands(player1, 2);
        addLands(player2, 5);
        gd.playerDamagePreventionShields.put(player2.getId(), 3);

        castAndResolve();

        harness.assertLife(player2, 20);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void countsControllerLandsAtResolution() {
        addLands(player1, 2);
        addLands(player2, 3);
        harness.castFromHand(player1, new SpitefulRepossession(), "{4}{R}");

        harness.addToBattlefield(player1, new Forest());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    private void castAndResolve() {
        harness.castFromHand(player1, new SpitefulRepossession(), "{4}{R}");
        harness.passBothPriorities();
    }

    private void addLands(com.github.laxika.magicalvibes.model.Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new Forest());
        }
    }
}
