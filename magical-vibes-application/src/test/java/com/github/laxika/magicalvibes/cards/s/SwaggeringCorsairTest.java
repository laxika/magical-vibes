package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SwaggeringCorsairTest extends BaseCardTest {

    @Test
    void entersWithoutCounterWhenYouDidNotAttack() {
        castCorsair(false);

        Permanent corsair = findCorsair();
        assertThat(corsair).isNotNull();
        assertThat(corsair.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void entersWithCounterWhenYouAttackedThisTurn() {
        castCorsair(true);

        Permanent corsair = findCorsair();
        assertThat(corsair).isNotNull();
        assertThat(corsair.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void opponentAttackDoesNotEnableRaid() {
        gd.playersDeclaredAttackersThisTurn.add(player2.getId());
        castCorsair(false);

        Permanent corsair = findCorsair();
        assertThat(corsair).isNotNull();
        assertThat(corsair.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castCorsair(boolean attackedThisTurn) {
        if (attackedThisTurn) {
            gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        }

        harness.setHand(player1, List.of(new SwaggeringCorsair()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private Permanent findCorsair() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Swaggering Corsair"))
                .findFirst()
                .orElse(null);
    }
}
