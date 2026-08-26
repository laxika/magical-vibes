package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ContractFromBelow.class, GrizzlyBears.class, HillGiant.class})
class ContractFromBelowTest extends BaseCardTest {

    @Test
    @DisplayName("Discards the hand, antes the top card, then draws seven")
    void discardsAntesAndDraws() {
        GrizzlyBears antedCard = new GrizzlyBears();
        HillGiant remainingCard = new HillGiant();
        List<Card> library = new ArrayList<>();
        library.add(antedCard);
        for (int i = 0; i < 7; i++) {
            library.add(new GrizzlyBears());
        }
        library.add(remainingCard);

        harness.setHand(player1, List.of(new ContractFromBelow(), new HillGiant()));
        harness.setLibrary(player1, library);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(antedCard.getId());
        assertThat(gd.antedCardIds).containsExactly(antedCard.getId());
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(remainingCard.getId());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .contains("Contract from Below", "Hill Giant");
    }
}
