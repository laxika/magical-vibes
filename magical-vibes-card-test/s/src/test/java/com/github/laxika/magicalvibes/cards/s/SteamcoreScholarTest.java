package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BubbleSmuggler;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.e.ExtractAConfession;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SteamcoreScholar.class, Forest.class, BubbleSmuggler.class, Shock.class, ExtractAConfession.class})
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
        castWithDrawnCards(List.of(new SteamcoreScholar(), new Forest()));

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, indexOf(SteamcoreScholar.class));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertInGraveyard(player1, "Steamcore Scholar");
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
        castWithDrawnCards(List.of(new BubbleSmuggler(), new Forest()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    private void castWithDrawnCards(List<com.github.laxika.magicalvibes.model.Card> drawnCards) {
        harness.setHand(player1, List.of(new SteamcoreScholar(), new BubbleSmuggler(), new Forest()));
        harness.setLibrary(player1, drawnCards);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("May discard a sorcery instead of two cards")
    void mayDiscardSorceryInsteadOfTwo() {
        castWithDrawnCards(List.of(new ExtractAConfession(), new Forest()));

        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, indexOf(ExtractAConfession.class));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertInGraveyard(player1, "Extract a Confession");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The single-card alternative rejects a nonflying creature")
    void singleCardAlternativeRejectsNonFlyingCreature() {
        castWithDrawnCards(List.of(new Shock(), new Forest()));

        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, indexOf(BubbleSmuggler.class));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, indexOf(Shock.class));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("Choosing two cards still requires the second discard after discarding an instant")
    void twoCardChoiceDoesNotStopAfterInstant() {
        castWithDrawnCards(List.of(new Shock(), new Forest()));

        harness.handleMayAbilityChosen(player1, false);
        harness.handleCardChosen(player1, indexOf(Shock.class));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, indexOf(Forest.class));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Drawn cards can be the entire hand discarded")
    void discardsEntireHandAfterDrawingTwo() {
        harness.setLibrary(player1, List.of(new Forest(), new BubbleSmuggler()));
        harness.castFromHand(player1, new SteamcoreScholar(), "{2}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Bubble Smuggler");
        assertThat(gd.interaction.activeInteraction()).isNull();
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
