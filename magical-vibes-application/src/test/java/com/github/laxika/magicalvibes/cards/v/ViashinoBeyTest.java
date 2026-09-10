package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GiantCockroach;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ViashinoBey.class, GiantCockroach.class})
class ViashinoBeyTest extends BaseCardTest {

    @Test
    @DisplayName("Viashino Bey does not force its controller's creatures to attack when it stays back")
    void ownCreaturesAreNotForcedWhenBeyStaysBack() {
        Permanent bey = addCreatureReady(player1, new ViashinoBey());
        Permanent cockroach = addCreatureReady(player1, new GiantCockroach());

        declareAttackers(List.of());

        assertThat(bey.isAttacking()).isFalse();
        assertThat(cockroach.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("When Viashino Bey attacks, all other able creatures its controller controls must attack")
    void ownCreaturesMustAttackWhenBeyAttacks() {
        addCreatureReady(player1, new ViashinoBey());
        addCreatureReady(player1, new GiantCockroach());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Viashino Bey does not force an opponent's creatures to attack")
    void opponentsCreaturesAreNotForced() {
        harness.addToBattlefield(player1, new ViashinoBey());
        Permanent cockroach = addCreatureReady(player2, new GiantCockroach());

        declareAttackers(player2, List.of());

        assertThat(cockroach.isAttacking()).isFalse();
    }
}
