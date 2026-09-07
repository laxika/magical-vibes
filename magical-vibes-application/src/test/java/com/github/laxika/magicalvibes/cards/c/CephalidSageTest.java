package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CephalidSage.class, Forest.class, GrizzlyBears.class})
class CephalidSageTest extends BaseCardTest {

    @Test
    @DisplayName("Threshold ETB draws three cards, then discards two cards")
    void thresholdEtbDrawsAndDiscards() {
        harness.setGraveyard(player1, graveyardCards(7));
        harness.setHand(player1, List.of(new CephalidSage(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        addSageMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(com.github.laxika.magicalvibes.model.Card::getName)
                .containsExactly("Forest", "Forest", "Forest");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(9);
    }

    @Test
    @DisplayName("Threshold ETB does not trigger below seven graveyard cards")
    void thresholdEtbDoesNotTriggerBelowThreshold() {
        harness.setGraveyard(player1, graveyardCards(6));
        harness.setHand(player1, List.of(new CephalidSage()));
        addSageMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void addSageMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private List<com.github.laxika.magicalvibes.model.Card> graveyardCards(int count) {
        List<com.github.laxika.magicalvibes.model.Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new GrizzlyBears());
        }
        return cards;
    }
}
