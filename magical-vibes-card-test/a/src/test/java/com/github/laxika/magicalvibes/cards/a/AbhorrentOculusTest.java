package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.p.PiranhaFly;
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

@CardUsed({AbhorrentOculus.class, Plains.class, PiranhaFly.class})
class AbhorrentOculusTest extends BaseCardTest {

    @Test
    @DisplayName("Casts by exiling six cards from its controller's graveyard")
    void castsByExilingSixGraveyardCards() {
        List<Card> graveyard = List.of(
                new Plains(), new Plains(), new Plains(),
                new Plains(), new Plains(), new Plains());
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
                new Plains(), new Plains(), new Plains(),
                new Plains(), new Plains()));
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
        Card manifestedCard = new Plains();
        Card graveyardCard = new Plains();
        harness.setLibrary(player1, List.of(manifestedCard, graveyardCard));
        Card opponentTopCard = new Plains();
        Card opponentSecondCard = new Plains();
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
    @Test
    @DisplayName("Does not manifest during its controller's upkeep")
    void doesNotManifestDuringOwnUpkeep() {
        harness.addToBattlefield(player1, new AbhorrentOculus());
        Card topCard = new Plains();
        Card secondCard = new Plains();
        harness.setLibrary(player1, List.of(topCard, secondCard));

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, secondCard);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Manifests the only library card without putting a card into the graveyard")
    void manifestsFromOneCardLibrary() {
        harness.addToBattlefield(player1, new AbhorrentOculus());
        Card topCard = new Plains();
        harness.setLibrary(player1, List.of(topCard));
        harness.setGraveyard(player1, List.of());

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(topCard.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.getCard().getId().equals(topCard.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An empty library does not create a manifest or request a choice")
    void emptyLibraryDoesNothing() {
        harness.addToBattlefield(player1, new AbhorrentOculus());
        harness.setLibrary(player1, List.of());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Can choose the second card and turn a manifested creature face up for its mana cost")
    void manifestsSecondCardAndTurnsItFaceUp() {
        harness.addToBattlefield(player1, new AbhorrentOculus());
        Card graveyardCard = new Plains();
        Card manifestedCard = new PiranhaFly();
        harness.setLibrary(player1, List.of(graveyardCard, manifestedCard));

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        var permanent = findPermanent(player1, "Piranha Fly");
        assertThat(permanent.isFaceDown()).isTrue();
        assertThat(permanent.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardCard);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(permanent));

        assertThat(permanent.isFaceDown()).isFalse();
        assertThat(permanent.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot pay the additional cost by selecting the same graveyard card twice")
    void cannotExileTheSameCardTwice() {
        List<Card> graveyard = List.of(new Plains(), new Plains(), new Plains(),
                new Plains(), new Plains(), new Plains());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new AbhorrentOculus()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreatureWithMultipleGraveyardExile(
                player1, 0, List.of(0, 1, 2, 3, 4, 4)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(graveyard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A manifested land cannot be turned face up")
    void cannotTurnManifestedLandFaceUp() {
        harness.addToBattlefield(player1, new AbhorrentOculus());
        Card land = new Plains();
        harness.setLibrary(player1, List.of(land));

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));

        var permanent = findPermanent(player1, "Plains");
        assertThatThrownBy(() -> harness.turnFaceUp(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(permanent)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(permanent.isFaceDown()).isTrue();
    }

    @Test
    @DisplayName("Turning a manifested Oculus face up does not require its additional casting cost")
    void turningManifestedOculusFaceUpDoesNotExileCards() {
        harness.addToBattlefield(player1, new AbhorrentOculus());
        Card manifestedCard = new AbhorrentOculus();
        harness.setLibrary(player1, List.of(manifestedCard));
        harness.setGraveyard(player1, List.of());

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        var permanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(candidate -> candidate.getCard().getId().equals(manifestedCard.getId()))
                .findFirst().orElseThrow();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(permanent));

        assertThat(permanent.isFaceDown()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }
}
