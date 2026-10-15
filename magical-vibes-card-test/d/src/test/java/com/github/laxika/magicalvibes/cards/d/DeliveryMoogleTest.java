package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AstrologiansPlanisphere;
import com.github.laxika.magicalvibes.cards.b.BusterSword;
import com.github.laxika.magicalvibes.cards.w.WorldMap;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeliveryMoogle.class, WorldMap.class, AstrologiansPlanisphere.class, BusterSword.class})
class DeliveryMoogleTest extends BaseCardTest {

    @Test
    @DisplayName("The enter-the-battlefield ability offers artifact cards with mana value 2 or less")
    void searchesEligibleArtifactsFromLibraryAndGraveyard() {
        Card libraryArtifact = new WorldMap();
        Card graveyardArtifact = new AstrologiansPlanisphere();
        Card expensiveArtifact = new BusterSword();
        Card nonArtifact = new DeliveryMoogle();
        setLibrary(libraryArtifact, expensiveArtifact, nonArtifact);
        harness.setGraveyard(player1, List.of(graveyardArtifact));
        castMoogle();

        resolveEnterTheBattlefieldTrigger();

        PendingInteraction.SearchLibraryAndOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                libraryArtifact.getId(), graveyardArtifact.getId());

        harness.handleMultipleCardsChosen(player1, List.of(graveyardArtifact.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(graveyardArtifact);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(graveyardArtifact);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(libraryArtifact, expensiveArtifact, nonArtifact);
    }

    @Test
    @DisplayName("The enter-the-battlefield ability does nothing when no eligible artifact exists")
    void doesNotFindIneligibleCards() {
        Card expensiveArtifact = new BusterSword();
        Card nonArtifact = new DeliveryMoogle();
        setLibrary(expensiveArtifact, nonArtifact);
        castMoogle();

        resolveEnterTheBattlefieldTrigger();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(expensiveArtifact, nonArtifact);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(expensiveArtifact, nonArtifact);
    }

    @Test
    @DisplayName("An artifact with mana value exactly two can be taken from the library")
    void putsLibraryArtifactIntoHand() {
        Card artifact = new AstrologiansPlanisphere();
        Card remaining = new BusterSword();
        Card opposingArtifact = new WorldMap();
        setLibrary(artifact, remaining);
        harness.setGraveyard(player2, List.of(opposingArtifact));
        castMoogle();

        resolveEnterTheBattlefieldTrigger();

        PendingInteraction.SearchLibraryAndOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(artifact.getId());
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingArtifact);
        assertThat(gd.playersWhoSearchedLibraryThisTurn).contains(player1.getId());
    }

    @Test
    @DisplayName("A search of the library may fail to find an eligible artifact")
    void canFailToFindLibraryArtifact() {
        Card artifact = new WorldMap();
        setLibrary(artifact);
        castMoogle();

        resolveEnterTheBattlefieldTrigger();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playersWhoSearchedLibraryThisTurn).contains(player1.getId());
    }

    @Test
    @DisplayName("Taking an artifact into hand does not cause its battlefield entry ability to trigger")
    void returningArtifactDoesNotTriggerItsEntryAbility() {
        Card artifact = new AstrologiansPlanisphere();
        setLibrary();
        harness.setGraveyard(player1, List.of(artifact));
        castMoogle();

        resolveEnterTheBattlefieldTrigger();
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    private void castMoogle() {
        harness.castFromHand(player1, new DeliveryMoogle(), "{3}{W}");
    }

    private void resolveEnterTheBattlefieldTrigger() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
