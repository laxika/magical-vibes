package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LandscaperColos.class, Forest.class, GrizzlyBears.class, Shock.class})
class LandscaperColosTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a target card from an opponent's graveyard on the bottom of its owner's library")
    void putsOpponentGraveyardCardOnLibraryBottom() {
        Card target = new GrizzlyBears();
        Card existingTop = new Shock();
        Card existingBottom = new Shock();
        harness.setGraveyard(player2, List.of(target));
        harness.setLibrary(player2, List.of(existingTop, existingBottom));
        harness.setHand(player1, List.of(new LandscaperColos()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(target.getId());
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(existingTop, existingBottom, target);
    }

    @Test
    @DisplayName("Cannot target a card in the controller's graveyard")
    void cannotTargetOwnGraveyard() {
        Card ownTarget = new GrizzlyBears();
        Card opponentTarget = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownTarget));
        harness.setGraveyard(player2, List.of(opponentTarget));
        harness.setHand(player1, List.of(new LandscaperColos()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(opponentTarget.getId());
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(ownTarget.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Basic landcycling discards Landscaper Colos and searches for a basic land")
    void basicLandcyclingSearchesForBasicLand() {
        harness.setHand(player1, List.of(new LandscaperColos()));
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Landscaper Colos");
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Forest");

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
