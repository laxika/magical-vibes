package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.e.EpitaphGolem;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LupinePrototype.class, EpitaphGolem.class})
class LupinePrototypeTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot attack while both players have cards in hand")
    void cannotAttackWhenEveryoneHasCards() {
        harness.setHand(player1, List.of(new EpitaphGolem()));
        harness.setHand(player2, List.of(new EpitaphGolem()));
        addCreatureReady(player1, new LupinePrototype());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can attack when its controller has no cards in hand")
    void canAttackWithEmptyControllerHand() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new EpitaphGolem()));
        addCreatureReady(player1, new LupinePrototype());
        harness.setLife(player2, 20);

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(20);
    }

    @Test
    @DisplayName("Can attack when the opponent has no cards in hand")
    void canAttackWithEmptyOpponentHand() {
        harness.setHand(player1, List.of(new EpitaphGolem()));
        harness.setHand(player2, List.of());
        addCreatureReady(player1, new LupinePrototype());
        harness.setLife(player2, 20);

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(20);
    }

    @Test
    @DisplayName("Cannot block while both players have cards in hand")
    void cannotBlockWhenEveryoneHasCards() {
        harness.setHand(player1, List.of(new EpitaphGolem()));
        harness.setHand(player2, List.of(new EpitaphGolem()));
        addCreatureReady(player2, new EpitaphGolem());
        addCreatureReady(player1, new LupinePrototype());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can block when a player has no cards in hand")
    void canBlockWithEmptyHand() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new EpitaphGolem()));
        addCreatureReady(player2, new EpitaphGolem());
        addCreatureReady(player1, new LupinePrototype());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Can block when only the attacking player has an empty hand")
    void canBlockWithEmptyOpponentHand() {
        harness.setHand(player1, List.of(new EpitaphGolem()));
        harness.setHand(player2, List.of());
        addCreatureReady(player2, new EpitaphGolem());
        var blocker = addCreatureReady(player1, new LupinePrototype());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Can attack when both players have empty hands")
    void canAttackWithBothHandsEmpty() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        addCreatureReady(player1, new LupinePrototype());
        harness.setLife(player2, 20);

        declareAttackers(player1, List.of(0));

        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Remains attacking and deals damage after the empty hand gains a card")
    void remainsAttackingAfterHandBecomesNonempty() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new EpitaphGolem()));
        var attacker = addCreatureReady(player1, new LupinePrototype());
        harness.setLife(player2, 20);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));
        harness.setHand(player1, List.of(new EpitaphGolem()));

        assertThat(attacker.isAttacking()).isTrue();
        resolveCombat(player1);
        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Remains blocking after the empty hand gains a card")
    void remainsBlockingAfterHandBecomesNonempty() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new EpitaphGolem()));
        addCreatureReady(player2, new EpitaphGolem());
        var blocker = addCreatureReady(player1, new LupinePrototype());
        harness.setLife(player1, 20);

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))));
        harness.setHand(player1, List.of(new EpitaphGolem()));

        assertThat(blocker.isBlocking()).isTrue();
        resolveCombat(player2);
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player2, "Epitaph Golem");
        harness.assertOnBattlefield(player1, "Lupine Prototype");
    }

    @Test
    @DisplayName("Cannot block if the last empty hand gains a card before blockers are chosen")
    void cannotBlockAfterHandBecomesNonemptyBeforeDeclaration() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new EpitaphGolem()));
        addCreatureReady(player2, new EpitaphGolem());
        addCreatureReady(player1, new LupinePrototype());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        harness.setHand(player1, List.of(new EpitaphGolem()));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }
}
