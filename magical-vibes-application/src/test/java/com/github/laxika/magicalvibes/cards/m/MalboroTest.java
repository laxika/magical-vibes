package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Malboro.class, Forest.class, GrizzlyBears.class, Swamp.class})
class MalboroTest extends BaseCardTest {

    @Test
    @DisplayName("ETB makes each opponent discard, lose 2 life, and exile the top three cards")
    void etbAppliesBadBreath() {
        Card discarded = new GrizzlyBears();
        Card remainingHandCard = new Forest();
        Card exiledTop = new Forest();
        Card exiledMiddle = new Swamp();
        Card exiledBottom = new GrizzlyBears();

        harness.setHand(player1, List.of(new Malboro()));
        harness.setHand(player2, new ArrayList<>(List.of(discarded, remainingHandCard)));
        harness.setLife(player2, 20);
        harness.setLibrary(player2, List.of(exiledTop, exiledMiddle, exiledBottom, new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(remainingHandCard);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactly(exiledTop, exiledMiddle, exiledBottom);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Swampcycling discards Malboro and searches for a Swamp")
    void swampcyclingSearchesForSwamp() {
        harness.setHand(player1, List.of(new Malboro()));
        harness.setLibrary(player1, List.of(new Forest(), new Swamp(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Malboro");
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).singleElement().extracting(Card::getName).isEqualTo("Swamp");

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Swamp");
    }
}
