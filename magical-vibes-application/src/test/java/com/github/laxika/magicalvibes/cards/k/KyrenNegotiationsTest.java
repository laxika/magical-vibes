package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.cards.n.NicolBolasPlaneswalker;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KyrenNegotiations.class, FreshVolunteers.class, NicolBolasPlaneswalker.class})
class KyrenNegotiationsTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping a creature deals 1 damage to the target player")
    void tapCreatureDealsDamage() {
        harness.addToBattlefield(player1, new KyrenNegotiations());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FreshVolunteers());
        creature.untap();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Tapping a creature deals 1 damage to the target planeswalker")
    void tapCreatureDealsDamageToPlaneswalker() {
        harness.addToBattlefield(player1, new KyrenNegotiations());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FreshVolunteers());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new NicolBolasPlaneswalker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);

        harness.activateAbility(player1, 0, null, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot activate without an untapped creature")
    void requiresUntappedCreature() {
        harness.addToBattlefield(player1, new KyrenNegotiations());
        Permanent creature = addCreatureReady(player1, new FreshVolunteers());
        creature.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("A creature an opponent controls cannot pay the cost")
    void opponentCreatureDoesNotPay() {
        harness.addToBattlefield(player1, new KyrenNegotiations());
        Permanent creature = addCreatureReady(player2, new FreshVolunteers());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
