package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CentaurRootcaster.class, Forest.class, SuntailHawk.class})
class CentaurRootcasterTest extends BaseCardTest {

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
        harness.setLibrary(player1, List.of(new Forest(), new SuntailHawk()));

        resolveCombat();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        Permanent forest = findPermanent(player1, "Forest");
        assertThat(forest.isTapped()).isTrue();
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
        Permanent blocker = addCreatureReady(player2, new SuntailHawk());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }
}
