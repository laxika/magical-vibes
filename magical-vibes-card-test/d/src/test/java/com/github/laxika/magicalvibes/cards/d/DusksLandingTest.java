package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SengirVampire;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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

        harness.castAndResolveSorcery(player1, 0, 0);

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

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(vampire1, vampire2);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonVampire);
    }

    @ParameterizedTest
    @CsvSource({"true, false", "false, true"})
    void drawsWhenOnlyOneLifeConditionIsMet(boolean gainedLife, boolean opponentLostLife) {
        Card drawn = new Forest();
        Card vampire = new SengirVampire();
        harness.setHand(player1, List.of(new DusksLanding()));
        harness.setLibrary(player1, List.of(drawn, vampire));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.inMutationScope(() -> {
            if (gainedLife) {
                harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1);
            }
            if (opponentLostLife) {
                harness.getLifeSupport().applyLifeLoss(gd, player2.getId(), 1, "test");
            }
        });

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(vampire);
    }

    @Test
    void controllerLifeLossAndOpponentLifeGainDoNotEnableSeek() {
        Card drawn = new Forest();
        Card vampire = new SengirVampire();
        harness.setHand(player1, List.of(new DusksLanding()));
        harness.setLibrary(player1, List.of(drawn, vampire));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.inMutationScope(() -> {
            harness.getLifeSupport().applyLifeLoss(gd, player1.getId(), 1, "test");
            harness.getLifeSupport().applyGainLife(gd, player2.getId(), 1);
        });

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(vampire);
    }

    @ParameterizedTest
    @CsvSource({"0", "1"})
    void seeksAvailableVampiresWithoutDrawingWhenFewerThanTwoExist(int vampireCount) {
        Card first = new Forest();
        Card second = new Forest();
        Card vampire = new SengirVampire();
        harness.setHand(player1, List.of(new DusksLanding()));
        harness.setLibrary(player1, vampireCount == 0
                ? List.of(first, second) : List.of(first, vampire, second));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.inMutationScope(() -> {
            harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1);
            harness.getLifeSupport().applyLifeLoss(gd, player2.getId(), 1, "test");
        });

        harness.castAndResolveSorcery(player1, 0, 0);

        if (vampireCount == 0) {
            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        } else {
            assertThat(gd.playerHands.get(player1.getId())).containsExactly(vampire);
        }
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void checksLifeEventsAtResolutionEvenWhenLifeTotalsReturnToTheirStartingValues() {
        Card first = new Forest();
        Card vampire1 = new SengirVampire();
        Card second = new Forest();
        Card vampire2 = new SengirVampire();
        harness.setHand(player1, List.of(new DusksLanding()));
        harness.setLibrary(player1, List.of(first, vampire1, second, vampire2));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castSorcery(player1, 0);
        harness.inMutationScope(() -> {
            harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1);
            harness.getLifeSupport().applyLifeLoss(gd, player1.getId(), 1, "test");
            harness.getLifeSupport().applyLifeLoss(gd, player2.getId(), 1, "test");
            harness.getLifeSupport().applyGainLife(gd, player2.getId(), 1);
        });

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(vampire1, vampire2);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
