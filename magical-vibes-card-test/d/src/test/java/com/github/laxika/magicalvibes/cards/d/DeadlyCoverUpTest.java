package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeadlyCoverUp.class, GrizzlyBears.class, Island.class})
class DeadlyCoverUpTest extends BaseCardTest {

    @Test
    void destroysAllCreaturesWithoutCollectingEvidence() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DeadlyCoverUp()));
        addManaForDeadlyCoverUp();

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void collectingEvidenceExilesChosenBasicLandAndSameNamedCardsAndDrawsForHandExiles() {
        List<Card> evidence = List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        Card chosenBasicLand = new Island();
        Card handCopy = new Island();
        Card libraryCopy = new Island();
        Card cardDrawnForHandExile = new GrizzlyBears();

        harness.setGraveyard(player1, evidence);
        harness.setGraveyard(player2, List.of(chosenBasicLand));
        harness.setHand(player1, List.of(new DeadlyCoverUp()));
        harness.setHand(player2, List.of(handCopy));
        harness.setLibrary(player2, List.of(libraryCopy, cardDrawnForHandExile));
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        addManaForDeadlyCoverUp();

        gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(), false, null, null, null, null, List.of(0, 1, 2));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(chosenBasicLand.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiZoneExileChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(handCopy.getId(), libraryCopy.getId()));

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactlyInAnyOrder(chosenBasicLand, handCopy, libraryCopy);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(cardDrawnForHandExile);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(chosenBasicLand.getId()));
    }

    @Test
    void canChooseNewlyDestroyedCreatureAndLeaveAllOtherMatchingCards() {
        List<Card> evidence = List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        Card destroyedCreature = new GrizzlyBears();
        Card graveyardCopy = new GrizzlyBears();
        Card handCopy = new GrizzlyBears();
        Card libraryCopy = new GrizzlyBears();
        harness.setGraveyard(player1, evidence);
        harness.setGraveyard(player2, List.of(graveyardCopy));
        harness.addToBattlefield(player2, destroyedCreature);
        harness.setHand(player1, List.of(new DeadlyCoverUp()));
        harness.setHand(player2, List.of(handCopy));
        harness.setLibrary(player2, List.of(libraryCopy));
        addManaForDeadlyCoverUp();

        gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(), false, null, null, null, null, List.of(0, 1, 2));

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(evidence);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(destroyedCreature.getId()));
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(destroyedCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(graveyardCopy);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(handCopy);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCopy);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void evidencePaidWithEmptyOpponentGraveyardDoesNotPromptForExile() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setGraveyard(player2, List.of());
        harness.setHand(player1, List.of(new DeadlyCoverUp()));
        harness.addToBattlefield(player2, new Island());
        addManaForDeadlyCoverUp();

        gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(), false, null, null, null, null, List.of(0, 1, 2));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Island");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void rejectsEvidenceWithTotalManaValueBelowSix() {
        List<Card> evidence = List.of(new GrizzlyBears(), new GrizzlyBears(), new Island());
        harness.setGraveyard(player1, evidence);
        harness.setHand(player1, List.of(new DeadlyCoverUp()));
        harness.addToBattlefield(player2, new GrizzlyBears());
        addManaForDeadlyCoverUp();

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(), false, null, null, null, null, List.of(0, 1, 2)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(evidence);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void exilingOnlyGraveyardAndLibraryCopiesDoesNotDrawForUnchosenHandCopy() {
        Card chosenCard = new Island();
        Card graveyardCopy = new Island();
        Card handCopy = new Island();
        Card libraryCopy = new Island();
        Card remainingLibraryCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(chosenCard, graveyardCopy));
        harness.setHand(player1, List.of(new DeadlyCoverUp()));
        harness.setHand(player2, List.of(handCopy));
        harness.setLibrary(player2, List.of(libraryCopy, remainingLibraryCard));
        addManaForDeadlyCoverUp();

        gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(), false, null, null, null, null, List.of(0, 1, 2));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(chosenCard.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(graveyardCopy.getId(), libraryCopy.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactlyInAnyOrder(chosenCard, graveyardCopy, libraryCopy);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(handCopy);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remainingLibraryCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void addManaForDeadlyCoverUp() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
