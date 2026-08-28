package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SteamcoreScholar.class, AirElemental.class, Forest.class, GrizzlyBears.class, Shock.class})
class SteamcoreScholarTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two cards and may discard an instant instead of two cards")
    void mayDiscardInstantInsteadOfTwo() {
        castWithDrawnCards(List.of(new Shock(), new Forest()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, indexOf(Shock.class));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("May discard a flying creature instead of two cards")
    void mayDiscardFlyingCreatureInsteadOfTwo() {
        castWithDrawnCards(List.of(new AirElemental(), new Forest()));

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, indexOf(AirElemental.class));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertInGraveyard(player1, "Air Elemental");
    }

    @Test
    @DisplayName("Declining the acceptable discard requires two discards")
    void decliningAcceptableDiscardRequiresTwo() {
        castWithDrawnCards(List.of(new Shock(), new Forest()));

        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Requires two discards when no acceptable card is available")
    void requiresTwoDiscardsWithoutAcceptableCard() {
        castWithDrawnCards(List.of(new GrizzlyBears(), new Forest()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    private void castWithDrawnCards(List<com.github.laxika.magicalvibes.model.Card> drawnCards) {
        harness.setHand(player1, List.of(new SteamcoreScholar(), new GrizzlyBears(), new Forest()));
        harness.setLibrary(player1, drawnCards);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private int indexOf(Class<?> cardClass) {
        List<com.github.laxika.magicalvibes.model.Card> hand = gd.playerHands.get(player1.getId());
        for (int i = 0; i < hand.size(); i++) {
            if (cardClass.isInstance(hand.get(i))) {
                return i;
            }
        }
        throw new AssertionError("Card not found in hand: " + cardClass.getSimpleName());
    }
}
