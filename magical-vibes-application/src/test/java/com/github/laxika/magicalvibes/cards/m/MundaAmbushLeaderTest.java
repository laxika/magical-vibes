package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.e.ExpeditionEnvoy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MundaAmbushLeader.class, ExpeditionEnvoy.class, GrizzlyBears.class})
class MundaAmbushLeaderTest extends BaseCardTest {

    @Test
    @DisplayName("Its Ally trigger can put any number of Allies on top and the rest on the bottom")
    void allyCardsAreOrderedOnTopAndRestOnBottom() {
        Card firstAlly = new ExpeditionEnvoy();
        Card firstNonAlly = new GrizzlyBears();
        Card secondAlly = new ExpeditionEnvoy();
        Card secondNonAlly = new GrizzlyBears();
        Card untouched = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstAlly, firstNonAlly, secondAlly, secondNonAlly, untouched));
        harness.setHand(player1, List.of(new MundaAmbushLeader()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibraryRevealChoice reveal =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(reveal.validCardIds()).containsExactly(firstAlly.getId(), secondAlly.getId());
        harness.handleMultipleCardsChosen(player1, List.of(secondAlly.getId()));

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder.cards()).containsExactly(firstAlly, firstNonAlly, secondAlly, secondNonAlly);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 0, 3, 1)));

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(secondAlly, untouched, firstAlly, secondNonAlly, firstNonAlly);
    }

    @Test
    @DisplayName("Declining the look leaves the library unchanged")
    void decliningLeavesLibraryUnchanged() {
        harness.addToBattlefield(player1, new MundaAmbushLeader());
        Card top = new ExpeditionEnvoy();
        Card next = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top, next));
        harness.setHand(player1, List.of(new ExpeditionEnvoy()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, next);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A non-Ally creature entering does not trigger it")
    void nonAllyEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new MundaAmbushLeader());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
