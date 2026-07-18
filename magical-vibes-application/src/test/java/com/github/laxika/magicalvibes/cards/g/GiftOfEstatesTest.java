package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GiftOfEstatesTest extends BaseCardTest {

    @Test
    @DisplayName("When an opponent controls more lands, resolving presents only Plains cards")
    void opponentControlsMoreLandsPresentsPlains() {
        setupAndCast();
        givePlayerLands(player2, 1);
        setupLibrary();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.getName().equals("Plains"));
    }

    @Test
    @DisplayName("Chosen Plains cards go to hand")
    void chosenPlainsGoToHand() {
        setupAndCast();
        givePlayerLands(player2, 1);
        setupLibrary();

        harness.passBothPriorities();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals("Plains"));
    }

    @Test
    @DisplayName("When you control at least as many lands, no search happens")
    void noSearchWhenNotFewerLands() {
        setupAndCast();
        givePlayerLands(player1, 1);
        givePlayerLands(player2, 1);
        setupLibrary();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(c -> c.getName().equals("Plains"));
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new GiftOfEstates()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castSorcery(player1, 0, 0);
    }

    private void givePlayerLands(com.github.laxika.magicalvibes.model.Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new Forest());
        }
    }

    private void setupLibrary() {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Plains(), new Plains(), new Plains(), new GrizzlyBears()));
    }
}
