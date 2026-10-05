package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.m.Mindstab;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PsychoticEpisode.class, AshcoatBear.class, Mindstab.class})
class PsychoticEpisodeTest extends BaseCardTest {

    @Test
    @DisplayName("Offers cards from the target hand and library top")
    void offersHandAndTopCard() {
        Card handCard = new AshcoatBear();
        Card topCard = new AshcoatBear();
        Card libraryCard = new AshcoatBear();
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
        Card handCard = new AshcoatBear();
        Card topCard = new AshcoatBear();
        Card libraryCard = new AshcoatBear();
        harness.setHand(player2, List.of(handCard));
        harness.setLibrary(player2, List.of(topCard, libraryCard));

        cast();
        harness.handleMultipleCardsChosen(player1, List.of(handCard.getId()));

        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(handCard);
        assertThat(gd.playerDecks.get(player2.getId()))
                .containsExactly(topCard, libraryCard, handCard);
    }

    @Test
    @DisplayName("Bottoms the revealed top card while leaving the target hand unchanged")
    void choosesTopCard() {
        Card handCard = new AshcoatBear();
        Card topCard = new AshcoatBear();
        Card libraryCard = new AshcoatBear();
        harness.setHand(player2, List.of(handCard));
        harness.setLibrary(player2, List.of(topCard, libraryCard));

        cast();
        harness.handleMultipleCardsChosen(player1, List.of(topCard.getId()));

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(handCard);
        assertThat(gd.playerDecks.get(player2.getId()))
                .containsExactly(libraryCard, topCard);
    }

    @Test
    @DisplayName("Offers only hand cards when the target library is empty")
    void offersOnlyHandWhenLibraryIsEmpty() {
        Card handCard = new AshcoatBear();
        harness.setHand(player2, List.of(handCard));
        harness.setLibrary(player2, List.of());

        cast();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(handCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(handCard.getId()));

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(handCard);
    }

    @Test
    @DisplayName("Offers only the library top when the target hand is empty")
    void offersOnlyTopWhenHandIsEmpty() {
        Card topCard = new AshcoatBear();
        Card libraryCard = new AshcoatBear();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(topCard, libraryCard));

        cast();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(topCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(topCard.getId()));

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
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
        PsychoticEpisode episode = discardEpisode();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(episode.getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Declining madness puts Psychotic Episode into its owner's graveyard")
    void decliningMadnessPutsCardInGraveyard() {
        PsychoticEpisode episode = discardEpisode();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(episode.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(episode);
    }

    @Test
    @DisplayName("Accepting madness casts Psychotic Episode for {1}{B} and resolves its effect")
    void acceptingMadnessCastsAndResolvesSpell() {
        PsychoticEpisode episode = discardEpisode();
        Card handCard = new AshcoatBear();
        Card topCard = new AshcoatBear();
        Card libraryCard = new AshcoatBear();
        harness.setHand(player2, List.of(handCard));
        harness.setLibrary(player2, List.of(topCard, libraryCard));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.validIds()).contains(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice revealChoice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(revealChoice).isNotNull();
        assertThat(revealChoice.validCardIds()).containsExactly(handCard.getId(), topCard.getId());
        harness.handleMultipleCardsChosen(player1, List.of(handCard.getId()));

        assertThat(gd.playerDecks.get(player2.getId()))
                .containsExactly(topCard, libraryCard, handCard);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(episode);
    }

    @Test
    @DisplayName("Can target its controller and bottom a card from their own hand")
    void targetsController() {
        PsychoticEpisode episode = new PsychoticEpisode();
        Card handCard = new AshcoatBear();
        Card topCard = new AshcoatBear();
        Card libraryCard = new AshcoatBear();
        harness.setHand(player1, List.of(episode, handCard));
        harness.setLibrary(player1, List.of(topCard, libraryCard));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(handCard.getId(), topCard.getId());
        harness.handleMultipleCardsChosen(player1, List.of(handCard.getId()));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(topCard, libraryCard, handCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(episode);
    }

    @Test
    @DisplayName("Bottoming a madness card from hand does not discard it or trigger madness")
    void bottomingDoesNotTriggerMadness() {
        PsychoticEpisode handCard = new PsychoticEpisode();
        Card topCard = new AshcoatBear();
        harness.setHand(player2, List.of(handCard));
        harness.setLibrary(player2, List.of(topCard));

        cast();
        harness.handleMultipleCardsChosen(player1, List.of(handCard.getId()));

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard, handCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(handCard);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void cast() {
        harness.setHand(player1, List.of(new PsychoticEpisode()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
    }

    private PsychoticEpisode discardEpisode() {
        PsychoticEpisode episode = new PsychoticEpisode();
        harness.setHand(player1, List.of(episode, new AshcoatBear(), new AshcoatBear()));
        harness.setHand(player2, List.of(new Mindstab()));
        harness.addMana(player2, ManaColor.BLACK, 6);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player2, 0, player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        return episode;
    }
}
