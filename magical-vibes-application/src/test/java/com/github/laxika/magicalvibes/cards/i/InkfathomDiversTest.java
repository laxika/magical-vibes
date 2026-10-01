package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InkfathomDivers.class, Island.class})
class InkfathomDiversTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Inkfathom Divers enters battlefield and triggers ETB")
    void resolvingEntersBattlefieldAndTriggersEtb() {
        harness.castFromHand(player1, new InkfathomDivers(), "{3}{U}{U}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Inkfathom Divers");

        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("Resolving ETB enters library reorder state for top 4 cards")
    void resolvingEtbEntersLibraryReorderState() {
        harness.castFromHand(player1, new InkfathomDivers(), "{3}{U}{U}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).playerId())
                .isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(4);
    }

    @Test
    @DisplayName("Library reorder changes the top cards of the library")
    void libraryReorderChangesTopCards() {
        harness.castFromHand(player1, new InkfathomDivers(), "{3}{U}{U}");

        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card originalTop0 = deck.get(0);
        Card originalTop1 = deck.get(1);
        Card originalTop2 = deck.get(2);
        Card originalTop3 = deck.get(3);
        Card originalTop4 = deck.get(4);

        resolveAllTriggers();

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(3, 2, 1, 0)));

        assertThat(deck.get(0)).isSameAs(originalTop3);
        assertThat(deck.get(1)).isSameAs(originalTop2);
        assertThat(deck.get(2)).isSameAs(originalTop1);
        assertThat(deck.get(3)).isSameAs(originalTop0);
        assertThat(deck.get(4)).isSameAs(originalTop4);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Library with fewer than 4 cards reorders available cards")
    void libraryWithFewerThanFourCards() {
        harness.castFromHand(player1, new InkfathomDivers(), "{3}{U}{U}");

        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        Card cardA = new InkfathomDivers();
        Card cardB = new InkfathomDivers();
        deck.add(cardA);
        deck.add(cardB);

        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(2);

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(deck.get(0)).isSameAs(cardB);
        assertThat(deck.get(1)).isSameAs(cardA);
    }

    @Test
    @DisplayName("Empty library skips reorder entirely")
    void emptyLibrarySkipsReorder() {
        harness.castFromHand(player1, new InkfathomDivers(), "{3}{U}{U}");

        gd.playerDecks.get(player1.getId()).clear();

        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("library is empty"));
    }

    @Test
    @DisplayName("Islandwalk prevents blocking while the defending player controls an Island")
    void islandwalkPreventsBlockingWithIsland() {
        Permanent attacker = addCreatureReady(player1, new InkfathomDivers());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new InkfathomDivers());
        harness.addToBattlefield(player2, new Island());

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Islandwalk allows blocking when the defending player controls no Island")
    void islandwalkAllowsBlockingWithoutIsland() {
        Permanent attacker = addCreatureReady(player1, new InkfathomDivers());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new InkfathomDivers());

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
