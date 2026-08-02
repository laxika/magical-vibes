package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GlacialCrasherTest extends BaseCardTest {

    @Test
    @DisplayName("Glacial Crasher can attack when there is a Mountain on the battlefield")
    void canAttackWhenMountainIsOnBattlefield() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new Mountain());
        addCreatureReady(player1, new GlacialCrasher());

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Glacial Crasher cannot attack when there is no Mountain on the battlefield")
    void cannotAttackWithoutMountain() {
        addCreatureReady(player1, new GlacialCrasher());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }
}
