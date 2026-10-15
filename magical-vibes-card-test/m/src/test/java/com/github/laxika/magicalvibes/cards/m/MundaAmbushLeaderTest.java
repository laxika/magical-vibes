package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.cards.e.ExpeditionEnvoy;
import com.github.laxika.magicalvibes.cards.o.OranRiefInvoker;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MundaAmbushLeader.class, ExpeditionEnvoy.class, OranRiefInvoker.class, Conspiracy.class})
class MundaAmbushLeaderTest extends BaseCardTest {

    @Test
    @DisplayName("Its Ally trigger can put any number of Allies on top and the rest on the bottom")
    void allyCardsAreOrderedOnTopAndRestOnBottom() {
        Card firstAlly = new ExpeditionEnvoy();
        Card firstNonAlly = new OranRiefInvoker();
        Card secondAlly = new ExpeditionEnvoy();
        Card secondNonAlly = new OranRiefInvoker();
        Card untouched = new OranRiefInvoker();
        harness.setLibrary(player1, List.of(firstAlly, firstNonAlly, secondAlly, secondNonAlly, untouched));
        harness.castFromHand(player1, new MundaAmbushLeader(), "{2}{R}{W}");
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
        Card next = new OranRiefInvoker();
        harness.setLibrary(player1, List.of(top, next));
        harness.castFromHand(player1, new ExpeditionEnvoy(), "{W}");
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
        harness.castFromHand(player1, new OranRiefInvoker(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Selected Allies are revealed publicly before being returned to the library")
    void selectedAlliesAreRevealed() {
        Card ally = new ExpeditionEnvoy();
        Card nonAlly = new OranRiefInvoker();
        harness.setLibrary(player1, List.of(ally, nonAlly));
        castMundaAndAcceptLook();

        int logStart = gd.gameLog.size();
        harness.handleMultipleCardsChosen(player1, List.of(ally.getId()));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1)));

        assertThat(gd.gameLog.subList(logStart, gd.gameLog.size()).stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("reveals") && log.contains("Expedition Envoy"));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ally, nonAlly);
    }

    @Test
    @DisplayName("Multiple selected Allies and the unselected cards can each be reordered")
    void multipleAlliesCanBeOrderedOnTop() {
        Card firstAlly = new ExpeditionEnvoy();
        Card firstNonAlly = new OranRiefInvoker();
        Card secondAlly = new ExpeditionEnvoy();
        Card secondNonAlly = new OranRiefInvoker();
        Card untouched = new OranRiefInvoker();
        harness.setLibrary(player1, List.of(firstAlly, firstNonAlly, secondAlly, secondNonAlly, untouched));
        castMundaAndAcceptLook();

        harness.handleMultipleCardsChosen(player1, List.of(firstAlly.getId(), secondAlly.getId()));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 3, 0, 1)));

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(secondAlly, firstAlly, untouched, secondNonAlly, firstNonAlly);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Revealing zero Allies puts all four looked-at cards on the bottom")
    void mayRevealZeroAllies() {
        Card ally = new ExpeditionEnvoy();
        Card first = new OranRiefInvoker();
        Card second = new OranRiefInvoker();
        Card third = new OranRiefInvoker();
        Card untouched = new ExpeditionEnvoy();
        harness.setLibrary(player1, List.of(ally, first, second, third, untouched));
        castMundaAndAcceptLook();

        harness.handleMultipleCardsChosen(player1, List.of());
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(3, 2, 1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, third, second, first, ally);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A short library with no Allies is reordered entirely onto the bottom")
    void shortLibraryWithoutAllies() {
        Card first = new OranRiefInvoker();
        Card second = new OranRiefInvoker();
        harness.setLibrary(player1, List.of(first, second));
        castMundaAndAcceptLook();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Accepting the look with an empty library finishes the ability")
    void emptyLibraryFinishesAbility() {
        harness.setLibrary(player1, List.of());
        castMundaAndAcceptLook();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Ally entering does not trigger Munda")
    void opponentAllyDoesNotTrigger() {
        harness.addToBattlefield(player1, new MundaAmbushLeader());
        Card top = new ExpeditionEnvoy();
        harness.setLibrary(player1, List.of(top));
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new ExpeditionEnvoy(), "{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    @DisplayName("Munda's own entry triggers rally even when its Ally type has been replaced")
    void selfEntryTriggersWithoutAllySubtype() {
        harness.castFromHand(player1, new Conspiracy(), "{3}{B}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());
        Card top = new OranRiefInvoker();
        harness.setLibrary(player1, List.of(top));

        harness.castFromHand(player1, new MundaAmbushLeader(), "{2}{R}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    private void castMundaAndAcceptLook() {
        harness.castFromHand(player1, new MundaAmbushLeader(), "{2}{R}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
    }
}
