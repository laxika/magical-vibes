package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LilianasSpoils.class, WalkingCorpse.class, GreenwoodSentinel.class, Shock.class,
        Island.class, Forest.class, Plains.class})
class LilianasSpoilsTest extends BaseCardTest {

    @Test
    @DisplayName("Target opponent discards, then the caster may take a black card from the top five")
    void discardsAndOffersBlackCard() {
        Card blackCard = new WalkingCorpse();
        List<Card> topCards = List.of(blackCard, new Shock(), new Island(), new Forest(), new Plains());
        harness.setHand(player1, List.of(new LilianasSpoils()));
        harness.setHand(player2, new ArrayList<>(List.of(new GreenwoodSentinel())));
        harness.setLibrary(player1, topCards);
        addMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).containsExactlyElementsOf(topCards);
        assertThat(choice.validCardIds()).containsExactly(blackCard.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.randomRemainingToBottom()).isTrue();
    }

    @Test
    @DisplayName("Choosing the black card puts it into hand and randomly bottoms the rest")
    void choosesBlackCardAndBottomsRest() {
        Card blackCard = new WalkingCorpse();
        List<Card> topCards = List.of(new Shock(), blackCard, new Island(), new Forest(), new Plains());
        harness.setHand(player1, List.of(new LilianasSpoils()));
        harness.setHand(player2, new ArrayList<>(List.of(new GreenwoodSentinel())));
        harness.setLibrary(player1, topCards);
        addMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);
        harness.handleMultipleCardsChosen(player1, List.of(blackCard.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(blackCard);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrderElementsOf(List.of(topCards.get(0), topCards.get(2), topCards.get(3), topCards.get(4)));
        harness.assertInGraveyard(player2, "Greenwood Sentinel");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Without a black card, the top five are bottomed without a choice")
    void noBlackCardNeedsNoLibraryChoice() {
        List<Card> topCards = List.of(new Shock(), new Island(), new Forest(), new Plains(), new GreenwoodSentinel());
        harness.setHand(player1, List.of(new LilianasSpoils()));
        harness.setHand(player2, new ArrayList<>(List.of(new GreenwoodSentinel())));
        harness.setLibrary(player1, topCards);
        addMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(topCards);
    }

    @Test
    @DisplayName("Cannot target the caster")
    void cannotTargetCaster() {
        harness.setHand(player1, List.of(new LilianasSpoils()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("Declining a black card bottoms all five below the untouched library")
    void mayDeclineBlackCard() {
        Card blackCard = new WalkingCorpse();
        List<Card> topCards = List.of(blackCard, new Shock(), new Island(), new Forest(), new Plains());
        Card untouched = new GreenwoodSentinel();
        List<Card> library = new ArrayList<>(topCards);
        library.add(untouched);
        harness.setHand(player1, List.of(new LilianasSpoils()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, library);
        addMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 6))
                .containsExactlyInAnyOrderElementsOf(topCards);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Liliana's Spoils");
    }

    @Test
    @DisplayName("An empty opposing hand does not prevent choosing from a short library")
    void emptyOpponentHandAndShortLibrary() {
        Card blackCard = new WalkingCorpse();
        Card otherCard = new Shock();
        harness.setHand(player1, List.of(new LilianasSpoils()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(blackCard, otherCard));
        addMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of(blackCard.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(blackCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(otherCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Liliana's Spoils");
    }

    @Test
    @DisplayName("Cannot take a nonblack card or more than one black card")
    void rejectsIllegalSelections() {
        Card firstBlack = new WalkingCorpse();
        Card secondBlack = new WalkingCorpse();
        Card nonblack = new Shock();
        harness.setHand(player1, List.of(new LilianasSpoils()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(firstBlack, secondBlack, nonblack));
        addMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(nonblack.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(firstBlack.getId(), secondBlack.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(secondBlack.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(secondBlack);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(firstBlack, nonblack);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Only the top five are eligible and the rest go below untouched cards")
    void selectsOnlyFromTopFive() {
        Card selected = new WalkingCorpse();
        Card unseenBlack = new WalkingCorpse();
        Card unseenLand = new Island();
        List<Card> topCards = List.of(selected, new Shock(), new Island(), new Forest(), new Plains());
        List<Card> library = new ArrayList<>(topCards);
        library.addAll(List.of(unseenBlack, unseenLand));
        harness.setHand(player1, List.of(new LilianasSpoils()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, library);
        addMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(unseenBlack.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(selected.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(selected);
        assertThat(gd.playerDecks.get(player1.getId()).subList(0, 2))
                .containsExactly(unseenBlack, unseenLand);
        assertThat(gd.playerDecks.get(player1.getId()).subList(2, 6))
                .containsExactlyInAnyOrderElementsOf(topCards.subList(1, 5));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library still allows the opponent to discard")
    void emptyLibraryStillDiscards() {
        Card discarded = new GreenwoodSentinel();
        Card retained = new Shock();
        harness.setHand(player1, List.of(new LilianasSpoils()));
        harness.setHand(player2, List.of(discarded, retained));
        harness.setLibrary(player1, List.of());
        addMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(retained);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Liliana's Spoils");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
