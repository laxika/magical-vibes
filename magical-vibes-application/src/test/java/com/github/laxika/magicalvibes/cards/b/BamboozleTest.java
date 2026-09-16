package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Bamboozle.class, Forest.class, Island.class, Mountain.class, Plains.class})
class BamboozleTest extends BaseCardTest {

    @Test
    @DisplayName("Puts two chosen revealed cards into the target player's graveyard and reorders the rest on top")
    void putsTwoChosenCardsIntoGraveyardAndReordersTheRest() {
        harness.setHand(player1, List.of(new Bamboozle()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        Card first = new Island();
        Card second = new Forest();
        Card third = new Mountain();
        Card fourth = new Plains();
        harness.setLibrary(player2, List.of(first, second, third, fourth));

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(firstChoice).isNotNull();
        assertThat(firstChoice.params().playerId()).isEqualTo(player1.getId());
        assertThat(firstChoice.params().targetPlayerId()).isEqualTo(player2.getId());
        assertThat(firstChoice.params().cards()).containsExactly(first, second, third, fourth);
        assertThat(firstChoice.params().reveals()).isTrue();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("reveals") && log.contains(first.getName())
                        && log.contains(second.getName()) && log.contains(third.getName())
                        && log.contains(fourth.getName()));
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 0, 1)));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(third, fourth);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Can target any player")
    void canTargetController() {
        harness.setHand(player1, List.of(new Bamboozle()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.setLibrary(player1, List.of(new Island(), new Forest(), new Mountain(), new Plains()));

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
    }

    @Test
    @DisplayName("Handles a target library with fewer than four cards")
    void handlesShortTargetLibrary() {
        harness.setHand(player1, List.of(new Bamboozle()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        Card first = new Island();
        Card second = new Forest();
        Card third = new Mountain();
        harness.setLibrary(player2, List.of(first, second, third));

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(firstChoice).isNotNull();
        assertThat(firstChoice.params().cards()).containsExactly(first, second, third);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(first, third);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(second);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Does nothing when the target library is empty")
    void emptyTargetLibraryDoesNothing() {
        harness.setHand(player1, List.of(new Bamboozle()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.setLibrary(player2, List.of());

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
