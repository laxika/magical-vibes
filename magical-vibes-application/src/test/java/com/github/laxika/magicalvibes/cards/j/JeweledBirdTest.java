package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.r.Rebirth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JeweledBirdTest extends BaseCardTest {

    @Test
    @DisplayName("Antes itself, clears the controller's other ante cards, and draws")
    void antesSelfClearsOtherOwnedAnteCardsAndDraws() {
        CardSetup setup = anteCardForPlayer1();
        harness.addToBattlefield(player1, new JeweledBird());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Jeweled Bird");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName())
                .contains(setup.antedCard().getName());
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .contains(setup.drawnCard().getName());
    }

    @Test
    @DisplayName("With no other ante cards, it still antes itself and draws")
    void antesSelfAndDrawsWithoutOtherAnteCards() {
        HillGiant drawnCard = new HillGiant();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addToBattlefield(player1, new JeweledBird());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Jeweled Bird");
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .contains(drawnCard.getName());
    }

    private CardSetup anteCardForPlayer1() {
        GrizzlyBears antedCard = new GrizzlyBears();
        HillGiant drawnCard = new HillGiant();
        harness.setLibrary(player1, List.of(antedCard, drawnCard));
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new Rebirth()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        return new CardSetup(antedCard, drawnCard);
    }

    private record CardSetup(GrizzlyBears antedCard, HillGiant drawnCard) {
    }
}
