package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.c.CrumbAndGetIt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JollyGerbils.class, CrumbAndGetIt.class})
class JollyGerbilsTest extends BaseCardTest {

    @Test
    void drawsWhenPromisedGiftIsActuallyGiven() {
        Permanent gerbils = addCreatureReady(player1, new JollyGerbils());
        harness.setHand(player1, List.of(new CrumbAndGetIt()));
        harness.setLibrary(player1, List.of(new CrumbAndGetIt()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstantWithGift(player1, 0, gerbils.getId(), true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertOnBattlefield(player2, "Food");
    }

    @Test
    void doesNotDrawWhenGiftIsNotPromised() {
        Permanent gerbils = addCreatureReady(player1, new JollyGerbils());
        harness.setHand(player1, List.of(new CrumbAndGetIt()));
        harness.setLibrary(player1, List.of(new CrumbAndGetIt()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstantWithGift(player1, 0, gerbils.getId(), false);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertNotOnBattlefield(player2, "Food");
    }
}
