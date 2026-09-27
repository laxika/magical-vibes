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
}
