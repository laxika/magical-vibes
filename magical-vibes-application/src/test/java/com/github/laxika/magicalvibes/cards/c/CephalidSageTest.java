package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AngelOfRetribution;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CephalidSage.class, AngelOfRetribution.class})
class CephalidSageTest extends BaseCardTest {

    @Test
    @DisplayName("Threshold ETB draws three cards, then discards two cards")
    void thresholdEtbDrawsAndDiscards() {
        harness.setGraveyard(player1, graveyardCards(7));
        harness.setHand(player1, List.of(new CephalidSage(), new AngelOfRetribution(), new AngelOfRetribution()));
        harness.setLibrary(player1,
                List.of(new AngelOfRetribution(), new AngelOfRetribution(), new AngelOfRetribution()));
        addSageMana();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Angel of Retribution", "Angel of Retribution", "Angel of Retribution");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(9);
    }

    @Test
    @DisplayName("Threshold ETB does not trigger below seven graveyard cards")
    void thresholdEtbDoesNotTriggerBelowThreshold() {
        harness.setGraveyard(player1, graveyardCards(6));
        harness.castFromHand(player1, new CephalidSage(), "{3}{U}");

        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Threshold uses the entering creature controller's graveyard")
    void thresholdUsesEnteringControllersGraveyard() {
        harness.setGraveyard(player2, graveyardCards(7));
        harness.castFromHand(player1, new CephalidSage(), "{3}{U}");

        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Threshold ETB triggers when the creature enters without being cast")
    void thresholdEtbTriggersForNonCastEntry() {
        harness.setGraveyard(player1, graveyardCards(7));
        harness.setHand(player1, List.of());
        harness.setLibrary(player1,
                List.of(new AngelOfRetribution(), new AngelOfRetribution(), new AngelOfRetribution()));

        harness.enterBattlefieldAndReturn(player1, new CephalidSage());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Threshold ETB resolves after the graveyard falls below threshold")
    void thresholdEtbResolvesAfterThresholdIsLost() {
        harness.setGraveyard(player1, graveyardCards(7));
        harness.setLibrary(player1,
                List.of(new AngelOfRetribution(), new AngelOfRetribution(), new AngelOfRetribution()));
        harness.castFromHand(player1, new CephalidSage(), "{3}{U}");

        harness.passBothPriorities();
        harness.setGraveyard(player1, List.of());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    private void addSageMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private List<Card> graveyardCards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new AngelOfRetribution());
        }
        return cards;
    }
}
