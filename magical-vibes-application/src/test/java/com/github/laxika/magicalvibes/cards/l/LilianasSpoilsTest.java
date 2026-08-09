package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LilianasSpoilsTest extends BaseCardTest {

    @Test
    @DisplayName("Target opponent discards, then the caster may take a black card from the top five")
    void discardsAndOffersBlackCard() {
        Card blackCard = new WalkingCorpse();
        List<Card> topCards = List.of(blackCard, new Shock(), new Island(), new Forest(), new Plains());
        harness.setHand(player1, List.of(new LilianasSpoils()));
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setLibrary(player1, topCards);
        addMana();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

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
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setLibrary(player1, topCards);
        addMana();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleMultipleCardsChosen(player1, List.of(blackCard.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(blackCard);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrderElementsOf(List.of(topCards.get(0), topCards.get(2), topCards.get(3), topCards.get(4)));
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Without a black card, the top five are bottomed without a choice")
    void noBlackCardNeedsNoLibraryChoice() {
        List<Card> topCards = List.of(new Shock(), new Island(), new Forest(), new Plains(), new GrizzlyBears());
        harness.setHand(player1, List.of(new LilianasSpoils()));
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setLibrary(player1, topCards);
        addMana();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
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

    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
