package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JoinedResearchersTest extends BaseCardTest {

    @Test
    @DisplayName("Becomes prepared at each end step when an opponent has more cards in hand")
    void becomesPreparedWhenOpponentHasMoreCardsInHand() {
        Permanent researchers = harness.addToBattlefieldAndReturn(player1, new JoinedResearchers());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new GrizzlyBears()));

        advanceToEndStep(player2);
        harness.passBothPriorities();

        assertThat(researchers.isPrepared()).isTrue();
        assertThat(researchers.getPreparedSpellCardId()).isNotNull();
    }

    @Test
    @DisplayName("Does not become prepared when no opponent has more cards in hand")
    void doesNotBecomePreparedWithoutHandSizeDifference() {
        Permanent researchers = harness.addToBattlefieldAndReturn(player1, new JoinedResearchers());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears()));

        advanceToEndStep(player2);

        assertThat(researchers.isPrepared()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Casting Secret Rendezvous makes each player draw three cards and unprepares the creature")
    void castingPreparedSpellDrawsForBothPlayers() {
        Permanent researchers = harness.addToBattlefieldAndReturn(player1, new JoinedResearchers());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        setDeck(player1, List.of(new Island(), new Island(), new Island()));
        setDeck(player2, List.of(new Island(), new Island(), new Island()));

        advanceToEndStep(player2);
        harness.passBothPriorities();

        UUID spellId = researchers.getPreparedSpellCardId();
        harness.setHand(player1, List.of());
        setDeck(player1, List.of(new Island(), new Island(), new Island()));
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, spellId, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(researchers.isPrepared()).isFalse();
        assertThat(researchers.getPreparedSpellCardId()).isNull();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void setDeck(Player player, List<com.github.laxika.magicalvibes.model.Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
