package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KrosanVerge;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CentaurRootcaster.class, Forest.class, GrizzlyBears.class, SuntailHawk.class, KrosanVerge.class})
class CentaurRootcasterTest extends BaseCardTest {

    @Test
    @DisplayName("A nonbasic land cannot be found by the search")
    void nonbasicLandIsExcluded() {
        Permanent rootcaster = addCreatureReady(player1, new CentaurRootcaster());
        rootcaster.setAttacking(true);
        Forest forest = new Forest();
        KrosanVerge verge = new KrosanVerge();
        harness.setLibrary(player1, List.of(verge, forest));

        resolveCombat();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(forest);
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(verge);
        assertThat(gameLogContains("is shuffled")).isTrue();
    }

    @Test
    @DisplayName("The controller may fail to find even when a basic land is available")
    void mayFailToFindAvailableBasicLand() {
        Permanent rootcaster = addCreatureReady(player1, new CentaurRootcaster());
        rootcaster.setAttacking(true);
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        resolveCombat();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(findPermanents(player1, "Forest")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gameLogContains("is shuffled")).isTrue();
    }

    @Test
    @DisplayName("The attacking controller searches their own library and controls the found land")
    void secondPlayerSearchesOwnLibrary() {
        Permanent rootcaster = addCreatureReady(player2, new CentaurRootcaster());
        rootcaster.setAttacking(true);
        Forest forest = new Forest();
        SuntailHawk opposingCard = new SuntailHawk();
        harness.setLibrary(player2, List.of(forest));
        harness.setLibrary(player1, List.of(opposingCard));

        resolveCombat(player2);
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(findPermanent(player2, "Forest").isTapped()).isTrue();
        assertThat(findPermanents(player1, "Forest")).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(opposingCard);
    }

    @Test
    @DisplayName("Combat damage to a player creates a may prompt")
    void combatDamageCreatesMayPrompt() {
        Permanent rootcaster = addCreatureReady(player1, new CentaurRootcaster());
        rootcaster.setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting the may ability puts a basic land onto the battlefield tapped")
    void acceptingMayPutsBasicLandOntoBattlefieldTapped() {
        Permanent rootcaster = addCreatureReady(player1, new CentaurRootcaster());
        rootcaster.setAttacking(true);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest()));

        resolveCombat();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        Permanent forest = findPermanent(player1, "Forest");
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Accepting with no basic land does not put another card onto the battlefield")
    void acceptingWithoutBasicLandDoesNothing() {
        Permanent rootcaster = addCreatureReady(player1, new CentaurRootcaster());
        rootcaster.setAttacking(true);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        resolveCombat();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
    }

    @Test
    @DisplayName("Declining the may ability does not search")
    void decliningMaySkipsSearch() {
        Permanent rootcaster = addCreatureReady(player1, new CentaurRootcaster());
        rootcaster.setAttacking(true);

        resolveCombat();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Blocked combat damage does not trigger the ability")
    void blockedCombatDoesNotTrigger() {
        Permanent rootcaster = addCreatureReady(player1, new CentaurRootcaster());
        rootcaster.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Accepting the may ability offers only basic land cards")
    void acceptingMayOffersOnlyBasicLands() {
        Permanent rootcaster = addCreatureReady(player1, new CentaurRootcaster());
        rootcaster.setAttacking(true);
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
    @DisplayName("Accepting the may ability with no basic land does not open a search")
    void acceptingMayWithNoBasicLandSkipsSearch() {
        Permanent rootcaster = addCreatureReady(player1, new CentaurRootcaster());
        rootcaster.setAttacking(true);
        harness.setLibrary(player1, List.of(new SuntailHawk()));

        resolveCombat();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }
}
