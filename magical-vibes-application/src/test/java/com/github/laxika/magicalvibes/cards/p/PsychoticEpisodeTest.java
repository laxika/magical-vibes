package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RavensCrime;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PsychoticEpisode.class, GrizzlyBears.class, RavensCrime.class})
class PsychoticEpisodeTest extends BaseCardTest {

    @Test
    @DisplayName("Offers cards from the target hand and library top")
    void offersHandAndTopCard() {
        Card handCard = new GrizzlyBears();
        Card topCard = new GrizzlyBears();
        Card libraryCard = new GrizzlyBears();
        harness.setHand(player2, List.of(handCard));
        harness.setLibrary(player2, List.of(topCard, libraryCard));

        cast();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(handCard.getId(), topCard.getId());
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard, libraryCard);
    }

    @Test
    @DisplayName("Bottoms a chosen card from the target hand and restores the unchosen top card")
    void choosesHandCard() {
        Card handCard = new GrizzlyBears();
        Card topCard = new GrizzlyBears();
        Card libraryCard = new GrizzlyBears();
        harness.setHand(player2, List.of(handCard));
        harness.setLibrary(player2, List.of(topCard, libraryCard));

        cast();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardsChosen(List.of(handCard.getId())));

        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(handCard);
        assertThat(gd.playerDecks.get(player2.getId()))
                .containsExactly(topCard, libraryCard, handCard);
    }

    @Test
    @DisplayName("Bottoms the revealed top card while leaving the target hand unchanged")
    void choosesTopCard() {
        Card handCard = new GrizzlyBears();
        Card topCard = new GrizzlyBears();
        Card libraryCard = new GrizzlyBears();
        harness.setHand(player2, List.of(handCard));
        harness.setLibrary(player2, List.of(topCard, libraryCard));

        cast();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardsChosen(List.of(topCard.getId())));

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(handCard);
        assertThat(gd.playerDecks.get(player2.getId()))
                .containsExactly(libraryCard, topCard);
    }

    @Test
    @DisplayName("Does nothing when the target has no revealed cards")
    void emptyHandAndLibrary() {
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of());

        cast();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Discarding it offers its madness cost")
    void discardOffersMadness() {
        PsychoticEpisode episode = new PsychoticEpisode();
        harness.setHand(player1, List.of(episode));
        harness.setHand(player2, List.of(new RavensCrime()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(episode.getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    private void cast() {
        harness.setHand(player1, List.of(new PsychoticEpisode()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }
}
