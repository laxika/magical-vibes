package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.EagerConstruct;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TrustyCompanion.class, EagerConstruct.class})
class TrustyCompanionTest extends BaseCardTest {

    @Test
    @DisplayName("Trusty Companion can't attack alone")
    void cantAttackAlone() {
        addCreatureReady(player1, new TrustyCompanion());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't attack alone");
    }

    @Test
    @DisplayName("Trusty Companion can attack with another creature")
    void canAttackWithAnother() {
        harness.setLife(player2, 20);

        addCreatureReady(player1, new TrustyCompanion());
        addCreatureReady(player1, new EagerConstruct());

        declareAttackers(List.of(0, 1));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Trusty Companion may block alone")
    void canBlockAlone() {
        Permanent attacker = addCreatureReady(player1, new EagerConstruct());
        attacker.setAttacking(true);

        Permanent companion = addCreatureReady(player2, new TrustyCompanion());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(companion.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Another creature must actually attack alongside Trusty Companion")
    void undeclaredCreatureDoesNotAllowAttackingAlone() {
        addCreatureReady(player1, new TrustyCompanion());
        addCreatureReady(player1, new EagerConstruct());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't attack alone");
    }

    @Test
    @DisplayName("Two Trusty Companions can attack together")
    void twoCompanionsCanAttackTogether() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new TrustyCompanion());
        addCreatureReady(player1, new TrustyCompanion());

        declareAttackers(List.of(0, 1));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Attacking with Trusty Companion does not tap it")
    void vigilanceKeepsCompanionUntapped() {
        Permanent companion = addCreatureReady(player1, new TrustyCompanion());
        Permanent construct = addCreatureReady(player1, new EagerConstruct());

        declareAttackersAndPrepareBlockers(List.of(0, 1));

        assertThat(companion.isAttacking()).isTrue();
        assertThat(companion.isTapped()).isFalse();
        assertThat(construct.isTapped()).isTrue();
    }
}
