package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WheelOfMisfortune.class, GrizzlyBears.class})
class WheelOfMisfortuneTest extends BaseCardTest {

    @Test
    void highestNumberPlayerTakesDamageAndOtherPlayersDiscardAndDrawSeven() {
        Card player1Discarded = new GrizzlyBears();
        Card player2Kept = new GrizzlyBears();
        List<Card> player1Draws = cards(7);
        harness.setHand(player1, List.of(new WheelOfMisfortune(), player1Discarded));
        harness.setHand(player2, List.of(player2Kept));
        harness.setLibrary(player1, player1Draws);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castWheelOfMisfortune();

        harness.handleXValueChosen(player1, 5);
        harness.handleXValueChosen(player2, 2);

        harness.assertLife(player1, 15);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(player1Draws);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(player2Kept);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(player1Discarded);
    }

    @Test
    void zeroIsAllowedAndPlayersTiedForLowestKeepTheirHands() {
        Card player1Kept = new GrizzlyBears();
        Card player2Discarded = new GrizzlyBears();
        List<Card> player2Draws = cards(7);
        harness.setHand(player1, List.of(new WheelOfMisfortune(), player1Kept));
        harness.setHand(player2, List.of(player2Discarded));
        harness.setLibrary(player2, player2Draws);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castWheelOfMisfortune();

        assertThatThrownBy(() -> harness.handleXValueChosen(player1, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("between 0 and");
        harness.handleXValueChosen(player1, 0);
        harness.handleXValueChosen(player2, 2);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(player1Kept);
        assertThat(gd.playerHands.get(player2.getId())).containsExactlyElementsOf(player2Draws);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(player2Discarded);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void tiedLowestPlayersAreBothExemptFromDiscardAndDraw() {
        Card player1Kept = new GrizzlyBears();
        Card player2Kept = new GrizzlyBears();
        harness.setHand(player1, List.of(new WheelOfMisfortune(), player1Kept));
        harness.setHand(player2, List.of(player2Kept));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castWheelOfMisfortune();

        harness.handleXValueChosen(player1, 3);
        harness.handleXValueChosen(player2, 3);

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(player1Kept);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(player2Kept);
    }

    private void castWheelOfMisfortune() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.XValueChoice.class);
    }

    private List<Card> cards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new GrizzlyBears());
        }
        return cards;
    }
}
