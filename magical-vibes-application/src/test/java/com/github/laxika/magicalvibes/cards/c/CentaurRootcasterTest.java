package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CentaurRootcaster.class, Forest.class, GrizzlyBears.class})
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
        List<Card> library = gd.playerDecks.get(player1.getId());
        library.clear();
        library.addAll(List.of(new Forest(), new GrizzlyBears()));

        resolveCombat();
        harness.handleMayAbilityChosen(player1, true);
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        Permanent forest = findPermanent(player1, "Forest");
        assertThat(forest.isTapped()).isTrue();
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
}
