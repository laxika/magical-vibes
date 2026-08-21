package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WheelOfFortune.class, GrizzlyBears.class})
class WheelOfFortuneTest extends BaseCardTest {

    @Test
    @DisplayName("Each player discards their hand and draws seven cards")
    void discardsHandsAndDrawsSeven() {
        fillLibraries(10);
        WheelOfFortune wheel = new WheelOfFortune();
        GrizzlyBears player1Discard1 = new GrizzlyBears();
        GrizzlyBears player1Discard2 = new GrizzlyBears();
        GrizzlyBears player2Discard = new GrizzlyBears();
        harness.setHand(player1, List.of(wheel, player1Discard1, player1Discard2));
        harness.setHand(player2, List.of(player2Discard));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(wheel.getId(), player1Discard1.getId(), player1Discard2.getId());
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(player2Discard.getId());
    }

    private void fillLibraries(int cardsEach) {
        List<Card> library = new ArrayList<>();
        for (int i = 0; i < cardsEach; i++) {
            library.add(new GrizzlyBears());
        }
        harness.setLibrary(player1, library);

        library = new ArrayList<>();
        for (int i = 0; i < cardsEach; i++) {
            library.add(new GrizzlyBears());
        }
        harness.setLibrary(player2, library);
    }
}
