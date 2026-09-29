package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SengirVampire;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DusksLanding.class, Forest.class, SengirVampire.class})
class DusksLandingTest extends BaseCardTest {

    @Test
    void drawsACardWhenConditionIsNotMet() {
        Card drawn = new Forest();
        harness.setHand(player1, List.of(new DusksLanding()));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void seeksTwoVampiresWhenAnOpponentLostLifeAndControllerGainedLife() {
        Card vampire1 = new SengirVampire();
        Card nonVampire = new Forest();
        Card vampire2 = new SengirVampire();
        harness.setHand(player1, List.of(new DusksLanding()));
        harness.setLibrary(player1, List.of(vampire1, nonVampire, vampire2));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.inMutationScope(() -> {
            harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1);
            harness.getLifeSupport().applyLifeLoss(gd, player2.getId(), 1, "test");
        });

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(vampire1, vampire2);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonVampire);
    }
}
