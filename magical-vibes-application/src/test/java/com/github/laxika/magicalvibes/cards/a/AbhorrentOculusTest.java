package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AbhorrentOculus.class, GrizzlyBears.class})
class AbhorrentOculusTest extends BaseCardTest {

    @Test
    @DisplayName("Casts by exiling six cards from its controller's graveyard")
    void castsByExilingSixGraveyardCards() {
        List<Card> graveyard = List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new AbhorrentOculus()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1, 2, 3, 4, 5));

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(graveyard);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Abhorrent Oculus");
    }

    @Test
    @DisplayName("Cannot cast without six cards in its controller's graveyard")
    void cannotCastWithoutSixGraveyardCards() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new AbhorrentOculus()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreatureWithMultipleGraveyardExile(
                player1, 0, List.of(0, 1, 2, 3, 4)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Manifests dread from its controller's library during each opponent's upkeep")
    void manifestsDreadDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new AbhorrentOculus());
        Card manifestedCard = new GrizzlyBears();
        Card graveyardCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(manifestedCard, graveyardCard));
        Card opponentTopCard = new GrizzlyBears();
        Card opponentSecondCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(opponentTopCard, opponentSecondCard));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.allCards()).containsExactly(manifestedCard, graveyardCard);

        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.getCard().getId().equals(manifestedCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCard);
        assertThat(gd.playerDecks.get(player2.getId()))
                .containsExactly(opponentTopCard, opponentSecondCard);
    }
}
