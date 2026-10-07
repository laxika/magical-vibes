package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwaggeringCorsair.class})
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

    @Test
    void raidAppliesToEachCorsairWithoutUsingTheStack() {
        castCorsair(true);
        assertThat(gd.stack).isEmpty();

        castCorsair(false);

        assertThat(findPermanents(player1, "Swaggering Corsair"))
                .hasSize(2)
                .allSatisfy(corsair -> assertThat(corsair.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                        .isEqualTo(1));
        assertThat(gd.stack).isEmpty();
    }

    private void castCorsair(boolean attackedThisTurn) {
        if (attackedThisTurn) {
            gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        }

        harness.castFromHand(player1, new SwaggeringCorsair(), "{2}{R}");
        harness.passBothPriorities();
    }

    private Permanent findCorsair() {
        return findPermanent(player1, "Swaggering Corsair");
    }
}
