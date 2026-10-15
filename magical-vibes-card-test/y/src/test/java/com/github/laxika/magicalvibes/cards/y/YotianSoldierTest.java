package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YotianSoldier.class})
class YotianSoldierTest extends BaseCardTest {

    @Test
    void vigilanceKeepsItUntappedWhenItAttacks() {
        Permanent soldier = addCreatureReady(player1, new YotianSoldier());

        declareAttackers(List.of(0));

        assertThat(soldier.isTapped()).isFalse();
    }

    @Test
    void vigilanceDoesNotAllowTappedSoldierToAttack() {
        Permanent soldier = addCreatureReady(player1, new YotianSoldier());
        soldier.tap();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(soldier.isTapped()).isTrue();
        assertThat(soldier.isAttacking()).isFalse();
    }

    @Test
    void vigilanceDoesNotAllowSummoningSickSoldierToAttack() {
        Permanent soldier = harness.addToBattlefieldAndReturn(player1, new YotianSoldier());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(soldier.isTapped()).isFalse();
        assertThat(soldier.isAttacking()).isFalse();
    }
}
