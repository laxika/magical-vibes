package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KnightOfTheTusk.class})
class KnightOfTheTuskTest extends BaseCardTest {

    @Test
    void attackingDoesNotTapKnight() {
        Permanent knight = addCreatureReady(player1, new KnightOfTheTusk());
        addCreatureReady(player2, new KnightOfTheTusk());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(knight.isAttacking()).isTrue();
        assertThat(knight.isTapped()).isFalse();
    }

    @Test
    void vigilanceDoesNotAllowTappedKnightToAttack() {
        Permanent knight = addCreatureReady(player1, new KnightOfTheTusk());
        knight.tap();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(knight.isAttacking()).isFalse();
        assertThat(knight.isTapped()).isTrue();
    }

    @Test
    void vigilanceDoesNotAllowSummoningSickKnightToAttack() {
        Permanent knight = addCreatureReady(player1, new KnightOfTheTusk());
        knight.setSummoningSick(true);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(knight.isAttacking()).isFalse();
    }
}
