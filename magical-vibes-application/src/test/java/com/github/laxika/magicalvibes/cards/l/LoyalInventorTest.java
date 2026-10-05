package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.h.Hookblade;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.r.RoyalAssassin;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LoyalInventor.class, Hookblade.class, Island.class, RoyalAssassin.class})
class LoyalInventorTest extends BaseCardTest {

    @Test
    void withoutAnAssassinTheArtifactGoesOnTopOfTheLibrary() {
        Hookblade artifact = new Hookblade();
        Island nonArtifact = new Island();
        harness.setLibrary(player1, List.of(nonArtifact, artifact));
        castLoyalInventor();

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.TOP_OF_LIBRARY);
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().cards()).containsExactly(artifact);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(artifact, nonArtifact);
    }

    @Test
    void withAnAssassinTheArtifactGoesToHand() {
        Hookblade artifact = new Hookblade();
        Island nonArtifact = new Island();
        harness.setLibrary(player1, List.of(nonArtifact, artifact));
        harness.addToBattlefield(player1, new RoyalAssassin());
        castLoyalInventor();

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.HAND);
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().cards()).containsExactly(artifact);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(artifact);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonArtifact);
    }

    @Test
    void decliningTheMayAbilityDoesNothing() {
        Hookblade artifact = new Hookblade();
        harness.setLibrary(player1, List.of(artifact));
        castLoyalInventor();

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(artifact);
    }

    @Test
    void anOpponentsAssassinDoesNotPutTheArtifactIntoYourHand() {
        Hookblade artifact = new Hookblade();
        harness.setLibrary(player1, List.of(artifact));
        harness.addToBattlefield(player2, new RoyalAssassin());
        castLoyalInventor();

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(artifact);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void anAssassinGainedAfterTheTriggerPutsTheArtifactIntoHand() {
        Hookblade artifact = new Hookblade();
        harness.setLibrary(player1, List.of(artifact));
        castLoyalInventor();
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new RoyalAssassin());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void anAssassinLostAfterTheTriggerLeavesTheArtifactOnTop() {
        Hookblade artifact = new Hookblade();
        RoyalAssassin assassin = new RoyalAssassin();
        harness.setLibrary(player1, List.of(artifact));
        harness.addToBattlefield(player1, assassin);
        castLoyalInventor();
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard() == assassin);
        harness.setGraveyard(player1, List.of(assassin));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(artifact);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void mayFailToFindEvenWhenAnArtifactIsAvailable() {
        Hookblade artifact = new Hookblade();
        harness.setLibrary(player1, List.of(artifact));
        harness.addToBattlefield(player1, new RoyalAssassin());
        castLoyalInventor();

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(artifact);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void searchingWithoutAnArtifactCompletesWithoutMovingCards() {
        Island nonArtifact = new Island();
        harness.setLibrary(player1, List.of(nonArtifact));
        castLoyalInventor();

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonArtifact);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void searchingAnEmptyLibraryCompletesWithoutASelection() {
        harness.setLibrary(player1, List.of());
        castLoyalInventor();

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void vigilanceAllowsAttackingWithoutTapping() {
        Permanent inventor = addCreatureReady(player1, new LoyalInventor());

        declareAttackers(List.of(0));

        assertThat(inventor.isTapped()).isFalse();
    }

    private void castLoyalInventor() {
        harness.castFromHand(player1, new LoyalInventor(), "{2}{U}");
    }

}
