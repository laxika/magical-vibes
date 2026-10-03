package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.SaguArcher;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AlabasterKirin.class, AlpineGrizzly.class, SaguArcher.class})
class AlabasterKirinTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking does not tap Alabaster Kirin")
    void attackingDoesNotTap() {
        Permanent kirin = addCreatureReady(player1, new AlabasterKirin());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(kirin.isAttacking()).isTrue();
        assertThat(kirin.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A creature without flying or reach cannot block Alabaster Kirin")
    void groundCreatureCannotBlock() {
        addCreatureReady(player1, new AlabasterKirin());
        addCreatureReady(player2, new AlpineGrizzly());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("A flying creature can block Alabaster Kirin")
    void flyingCreatureCanBlock() {
        addCreatureReady(player1, new AlabasterKirin());
        Permanent blocker = addCreatureReady(player2, new AlabasterKirin());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A creature with reach can block Alabaster Kirin")
    void reachCreatureCanBlock() {
        addCreatureReady(player1, new AlabasterKirin());
        Permanent blocker = addCreatureReady(player2, new SaguArcher());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Alabaster Kirin can block a ground creature")
    void canBlockGroundCreature() {
        addCreatureReady(player1, new AlpineGrizzly());
        Permanent kirin = addCreatureReady(player2, new AlabasterKirin());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(kirin.isBlocking()).isTrue();
    }
}
