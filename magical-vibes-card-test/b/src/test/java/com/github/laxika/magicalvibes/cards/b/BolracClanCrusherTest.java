package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DomriChaosBringer;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BolracClanCrusher.class, GrizzlyBears.class, LeoninScimitar.class, DomriChaosBringer.class})
class BolracClanCrusherTest extends BaseCardTest {

    @Test
    void removesCounterFromControlledCreatureAndDealsDamageToPlayer() {
        addReadyCrusher();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    void dealsDamageToTargetCreature() {
        addReadyCrusher();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void cannotUseCounterFromNoncreaturePermanent() {
        addReadyCrusher();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        artifact.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
        assertThat(artifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void paysWithOwnCounterBeforeDamageResolves() {
        Permanent crusher = addReadyCrusher();
        crusher.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(crusher.isTapped()).isTrue();
        assertThat(crusher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);

        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    void choosesWhichControlledCreaturePaysTheCounterCost() {
        Permanent crusher = addReadyCrusher();
        crusher.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(crusher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    void cannotPayWithAnOpponentsCounter() {
        Permanent crusher = addReadyCrusher();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(crusher.isTapped()).isFalse();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent crusher = harness.addToBattlefieldAndReturn(player1, new BolracClanCrusher());
        crusher.setSummoningSick(true);
        crusher.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(crusher.isTapped()).isFalse();
        assertThat(crusher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void dealsDamageToPlaneswalker() {
        Permanent crusher = addReadyCrusher();
        crusher.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent domri = harness.addToBattlefieldAndReturn(player2, new DomriChaosBringer());
        domri.setCounterCount(CounterType.LOYALTY, 5);

        harness.activateAbility(player1, 0, null, domri.getId());
        harness.passBothPriorities();

        assertThat(domri.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void cannotActivateAgainWhileTapped() {
        Permanent crusher = addReadyCrusher();
        crusher.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");

        assertThat(crusher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    void counterCostIsNotRefundedWhenTargetLeavesBattlefield() {
        Permanent crusher = addReadyCrusher();
        crusher.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(crusher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(crusher.isTapped()).isTrue();
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void abilityStillResolvesAfterCrusherLeavesBattlefield() {
        Permanent crusher = addReadyCrusher();
        crusher.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(crusher);
        gd.playerGraveyards.get(player1.getId()).add(crusher.getCard());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    private Permanent addReadyCrusher() {
        Permanent crusher = harness.addToBattlefieldAndReturn(player1, new BolracClanCrusher());
        crusher.setSummoningSick(false);
        return crusher;
    }
}
