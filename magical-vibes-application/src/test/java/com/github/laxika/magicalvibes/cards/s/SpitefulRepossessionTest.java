package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
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
        harness.setHand(player1, List.of(new SpitefulRepossession()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castSorcery(player1, 0, 0);

        harness.addToBattlefield(player2, new Forest());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    private void castAndResolve() {
        harness.setHand(player1, List.of(new SpitefulRepossession()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    private void addLands(com.github.laxika.magicalvibes.model.Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new Forest());
        }
    }
}
