package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.EagerFirstYear;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThrillingDiscovery.class, EagerFirstYear.class})
class ThrillingDiscoveryTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving gains 2 life without discarding or drawing when declined")
    void declineOnlyGainsLife() {
        int lifeBefore = gd.getLife(player1.getId());
        harness.setHand(player1, new ArrayList<>(List.of(new ThrillingDiscovery(), new EagerFirstYear())));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Discarding two cards draws three cards after gaining life")
    void discardTwoDrawsThree() {
        int lifeBefore = gd.getLife(player1.getId());
        harness.setLibrary(player1, List.of(new EagerFirstYear(), new EagerFirstYear(), new EagerFirstYear()));
        harness.setHand(player1, new ArrayList<>(List.of(
                new ThrillingDiscovery(), new EagerFirstYear(), new EagerFirstYear())));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("One card cannot pay the optional two-card discard cost")
    void oneCardCannotPayDiscardCost() {
        ThrillingDiscovery kept = new ThrillingDiscovery();
        harness.setHand(player1, List.of(new ThrillingDiscovery(), kept));
        harness.setLibrary(player1, List.of(new ThrillingDiscovery(), new ThrillingDiscovery(), new ThrillingDiscovery()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castAndResolveSorcery(player1, 0, 0);
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An empty hand still gains life but draws nothing")
    void emptyHandOnlyGainsLife() {
        harness.setHand(player1, List.of(new ThrillingDiscovery()));
        harness.setLibrary(player1, List.of(new ThrillingDiscovery(), new ThrillingDiscovery(), new ThrillingDiscovery()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castAndResolveSorcery(player1, 0, 0);
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Life is gained before choosing two cards from a larger hand, then drawing")
    void choosesTwoCardsFromLargerHand() {
        ThrillingDiscovery kept = new ThrillingDiscovery();
        ThrillingDiscovery firstDiscard = new ThrillingDiscovery();
        ThrillingDiscovery secondDiscard = new ThrillingDiscovery();
        List<ThrillingDiscovery> draws = List.of(new ThrillingDiscovery(), new ThrillingDiscovery(), new ThrillingDiscovery());
        harness.setHand(player1, List.of(new ThrillingDiscovery(), kept, firstDiscard, secondDiscard));
        harness.setLibrary(player1, draws);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept, firstDiscard, secondDiscard);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept, secondDiscard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(draws);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4).contains(kept).containsAll(draws);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firstDiscard, secondDiscard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
