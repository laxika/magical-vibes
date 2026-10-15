package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(JaceBeleren.class)
class JaceBelerenTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts planeswalker spell on the stack")
    void castingPutsOnStack() {
        harness.castFromHand(player1, new JaceBeleren(), "{1}{U}{U}");

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.PLANESWALKER_SPELL);
        assertThat(entry.getCard()).isInstanceOf(JaceBeleren.class);
    }

    @Test
    @DisplayName("Resolving puts planeswalker on battlefield with initial loyalty 3")
    void resolvingEntersBattlefieldWithLoyalty() {
        harness.castFromHand(player1, new JaceBeleren(), "{1}{U}{U}");
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        assertThat(bf).anyMatch(p -> p.getCard() instanceof JaceBeleren);
        Permanent jace = bf.stream().filter(p -> p.getCard() instanceof JaceBeleren).findFirst().orElseThrow();
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(jace.isSummoningSick()).isFalse();
    }

    @Test
    @DisplayName("+2 ability makes each player draw a card and increases loyalty")
    void plusTwoEachPlayerDrawsCard() {
        Permanent jace = addReadyJace(player1);

        int p1HandBefore = harness.getGameData().playerHands.get(player1.getId()).size();
        int p2HandBefore = harness.getGameData().playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(5); // 3 + 2
        assertThat(gd.playerHands.get(player1.getId())).hasSize(p1HandBefore + 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(p2HandBefore + 1);
    }

    @Test
    @DisplayName("-1 ability makes target player draw a card and decreases loyalty")
    void minusOneTargetPlayerDrawsCard() {
        Permanent jace = addReadyJace(player1);

        int p2HandBefore = harness.getGameData().playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(2); // 3 - 1
        assertThat(gd.playerHands.get(player2.getId())).hasSize(p2HandBefore + 1);
    }

    @Test
    @DisplayName("-1 ability can target yourself")
    void minusOneCanTargetSelf() {
        Permanent jace = addReadyJace(player1);

        int p1HandBefore = harness.getGameData().playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, player1.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(2); // 3 - 1
        assertThat(gd.playerHands.get(player1.getId())).hasSize(p1HandBefore + 1);
    }

    @Test
    @DisplayName("-10 ability mills twenty cards from target player's library")
    void minusTenMillsTwentyCards() {
        Permanent jace = addReadyJace(player1);
        jace.setCounterCount(CounterType.LOYALTY, 10);

        List<Card> deck = harness.getGameData().playerDecks.get(player2.getId());
        harness.setLibrary(player2, deck.subList(deck.size() - 25, deck.size()));
        int deckSizeBefore = harness.getGameData().playerDecks.get(player2.getId()).size();
        int graveyardBefore = harness.getGameData().playerGraveyards.get(player2.getId()).size();

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(0); // 10 - 10
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 20);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(graveyardBefore + 20);
    }

    @Test
    @DisplayName("-10 ability mills only the cards available in a short library")
    void minusTenMillsOnlyAvailableCards() {
        Permanent jace = addReadyJace(player1);
        jace.setCounterCount(CounterType.LOYALTY, 10);
        harness.setLibrary(player2, List.of(
                new JaceBeleren(), new JaceBeleren(), new JaceBeleren(), new JaceBeleren(), new JaceBeleren()));

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(5);
    }

    @Test
    @DisplayName("-10 ability: Jace goes to graveyard at 0 loyalty")
    void minusTenJaceGoesToGraveyardAtZeroLoyalty() {
        Permanent jace = addReadyJace(player1);
        jace.setCounterCount(CounterType.LOYALTY, 10);

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Jace Beleren");
    }

    @Test
    @DisplayName("+2 adds loyalty as a cost before either player draws")
    void plusTwoPaysLoyaltyBeforeResolution() {
        Permanent jace = addReadyJace(player1);
        GameData gd = harness.getGameData();
        int p1HandBefore = gd.playerHands.get(player1.getId()).size();
        int p2HandBefore = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(p1HandBefore);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(p2HandBefore);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(p1HandBefore + 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(p2HandBefore + 1);
    }

    @Test
    @DisplayName("-1 still draws after paying the last loyalty counter")
    void minusOneResolvesAfterJaceDies() {
        Permanent jace = addReadyJace(player1);
        jace.setCounterCount(CounterType.LOYALTY, 1);
        GameData gd = harness.getGameData();
        int handBefore = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Jace Beleren");
        harness.assertInGraveyard(player1, "Jace Beleren");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("-10 can mill its controller and leaves the opponent's library untouched")
    void minusTenCanTargetSelf() {
        Permanent jace = addReadyJace(player1);
        jace.setCounterCount(CounterType.LOYALTY, 11);
        List<Card> library = java.util.stream.IntStream.range(0, 21)
                .mapToObj(i -> (Card) new JaceBeleren()).toList();
        harness.setLibrary(player1, library);
        GameData gd = harness.getGameData();
        List<Card> opponentLibrary = List.copyOf(gd.playerDecks.get(player2.getId()));

        harness.activateAbility(player1, 0, 2, null, player1.getId());
        harness.passBothPriorities();

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Jace Beleren");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(library.getLast());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyElementsOf(library.subList(0, 20));
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(opponentLibrary);
    }

    @Test
    @DisplayName("Cannot use -10 when loyalty is only 3")
    void cannotActivateMinusTenWithInsufficientLoyalty() {
        addReadyJace(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    @Test
    @DisplayName("Cannot activate loyalty ability during opponent's turn")
    void cannotActivateOnOpponentsTurn() {
        addReadyJace(player1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your turn");
    }

    @Test
    @DisplayName("Cannot activate two loyalty abilities on same planeswalker in one turn")
    void cannotActivateTwicePerTurn() {
        addReadyJace(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one loyalty ability");
    }

    private Permanent addReadyJace(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new JaceBeleren());
        perm.setCounterCount(CounterType.LOYALTY, 3);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
