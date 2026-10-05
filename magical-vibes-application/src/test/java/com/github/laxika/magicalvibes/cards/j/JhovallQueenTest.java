package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JhovallQueen.class})
class JhovallQueenTest extends BaseCardTest {

    @Test
    void vigilanceKeepsJhovallQueenUntappedAfterAttacking() {
        Permanent queen = addCreatureReady(player1, new JhovallQueen());

        declareAttackers(player1, List.of(0));

        assertThat(queen.isTapped()).isFalse();
    }

    @Test
    void vigilanceDoesNotAllowAttackingWhileTapped() {
        Permanent queen = addCreatureReady(player1, new JhovallQueen());
        queen.tap();

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(queen.isAttacking()).isFalse();
        assertThat(queen.isTapped()).isTrue();
    }

    @Test
    void vigilanceDoesNotAllowAttackingWithSummoningSickness() {
        Permanent queen = harness.addToBattlefieldAndReturn(player1, new JhovallQueen());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(queen.isAttacking()).isFalse();
        assertThat(queen.isTapped()).isFalse();
    }
}
