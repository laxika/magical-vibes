package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AtalanJackal.class, Forest.class, GrizzlyBears.class, SuntailHawk.class})
class AtalanJackalTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a player creates a may prompt")
    void combatDamageCreatesMayPrompt() {
        Permanent jackal = addCreatureReady(player1, new AtalanJackal());
        jackal.setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting the may ability puts a basic land onto the battlefield tapped")
    void acceptingMayPutsBasicLandOntoBattlefieldTapped() {
        Permanent jackal = addCreatureReady(player1, new AtalanJackal());
        jackal.setAttacking(true);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest()));

        resolveCombat();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        Permanent forest = findPermanent(player1, "Forest");
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Declining the may ability does not search")
    void decliningMaySkipsSearch() {
        Permanent jackal = addCreatureReady(player1, new AtalanJackal());
        jackal.setAttacking(true);

        resolveCombat();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("The search offers only basic lands")
    void searchOffersOnlyBasicLands() {
        Permanent jackal = addCreatureReady(player1, new AtalanJackal());
        jackal.setAttacking(true);
        Forest forest = new Forest();
        SuntailHawk nonBasic = new SuntailHawk();
        harness.setLibrary(player1, List.of(forest, nonBasic));

        resolveCombat();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(forest);
    }

    @Test
    @DisplayName("Blocked combat damage does not trigger the ability")
    void blockedCombatDoesNotTrigger() {
        Permanent jackal = addCreatureReady(player1, new AtalanJackal());
        jackal.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("The controller may fail to find even when a basic land is available")
    void canFailToFindAvailableBasicLand() {
        Permanent jackal = addCreatureReady(player1, new AtalanJackal());
        jackal.setAttacking(true);
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        resolveCombat();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(findPermanents(player1, "Forest")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Accepting the search with an empty library finishes without a selection")
    void emptyLibraryFinishesSearch() {
        Permanent jackal = addCreatureReady(player1, new AtalanJackal());
        jackal.setAttacking(true);
        harness.setLibrary(player1, List.of());

        resolveCombat();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Forest")).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The other player's Jackal searches its controller's library")
    void otherControllerSearchesOwnLibrary() {
        Permanent jackal = addCreatureReady(player2, new AtalanJackal());
        jackal.setAttacking(true);
        Forest controllerForest = new Forest();
        Forest opponentForest = new Forest();
        harness.setLibrary(player2, List.of(controllerForest));
        harness.setLibrary(player1, List.of(opponentForest));

        resolveCombat(player2);
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        Permanent forest = findPermanent(player2, "Forest");
        assertThat(forest.getCard()).isSameAs(controllerForest);
        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(opponentForest);
        assertThat(findPermanents(player1, "Forest")).isEmpty();
    }

    @Test
    @DisplayName("Trample damage to a player triggers the land search even when blocked")
    void trampleDamageTriggersSearch() {
        Permanent jackal = addCreatureReady(player1, new AtalanJackal());
        jackal.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new SuntailHawk());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setLibrary(player1, List.of(new Forest()));

        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0,
                java.util.Map.of(blocker.getId(), 1, player2.getId(), 1));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
    }
}
