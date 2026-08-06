package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AxegrinderGiant;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LupinePrototypeTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot attack while both players have cards in hand")
    void cannotAttackWhenEveryoneHasCards() {
        harness.setHand(player1, List.of(new AxegrinderGiant()));
        harness.setHand(player2, List.of(new AxegrinderGiant()));
        addCreatureReady(player1, new LupinePrototype());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can attack when its controller has no cards in hand")
    void canAttackWithEmptyControllerHand() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new AxegrinderGiant()));
        addCreatureReady(player1, new LupinePrototype());
        harness.setLife(player2, 20);

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(20);
    }

    @Test
    @DisplayName("Can attack when the opponent has no cards in hand")
    void canAttackWithEmptyOpponentHand() {
        harness.setHand(player1, List.of(new AxegrinderGiant()));
        harness.setHand(player2, List.of());
        addCreatureReady(player1, new LupinePrototype());
        harness.setLife(player2, 20);

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(20);
    }

    @Test
    @DisplayName("Cannot block while both players have cards in hand")
    void cannotBlockWhenEveryoneHasCards() {
        harness.setHand(player1, List.of(new AxegrinderGiant()));
        harness.setHand(player2, List.of(new AxegrinderGiant()));
        addCreatureReady(player2, new AxegrinderGiant());
        addCreatureReady(player1, new LupinePrototype());

        declareAttackers(player2, List.of(0));

        harness.beginBlockerDeclarationInput();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can block when a player has no cards in hand")
    void canBlockWithEmptyHand() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new AxegrinderGiant()));
        addCreatureReady(player2, new AxegrinderGiant());
        addCreatureReady(player1, new LupinePrototype());

        declareAttackers(player2, List.of(0));

        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isBlocking()).isTrue();
    }
}
