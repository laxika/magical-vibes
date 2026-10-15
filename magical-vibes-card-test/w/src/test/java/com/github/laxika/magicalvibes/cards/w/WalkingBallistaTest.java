package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AetherstreamLeopard;
import com.github.laxika.magicalvibes.cards.t.TezzeretTheSchemer;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WalkingBallista.class, AetherstreamLeopard.class, TezzeretTheSchemer.class})
class WalkingBallistaTest extends BaseCardTest {

    @Test
    @DisplayName("Casting with X=3 enters with three +1/+1 counters")
    void entersWithXCounters() {
        harness.setHand(player1, List.of(new WalkingBallista()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        Permanent ballista = findPermanent(player1, "Walking Ballista");
        assertThat(ballista.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ballista)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ballista)).isEqualTo(3);
    }

    @Test
    @DisplayName("The {4} ability puts a +1/+1 counter on Walking Ballista")
    void addsCounter() {
        Permanent ballista = addReadyBallista(player1, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(ballista.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removing a +1/+1 counter deals 1 damage to a creature")
    void removesCounterAndDealsDamageToCreature() {
        Permanent ballista = addReadyBallista(player1, 2);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AetherstreamLeopard());

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(ballista.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(creature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Removing a +1/+1 counter deals 1 damage to a player")
    void removesCounterAndDealsDamageToPlayer() {
        Permanent ballista = addReadyBallista(player1, 1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(ballista.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("The damage ability cannot be activated without a +1/+1 counter")
    void cannotActivateDamageAbilityWithoutCounter() {
        Permanent ballista = addReadyBallista(player1, 0);
        ballista.setToughnessModifier(1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(ballista.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Casting with X=0 sends Ballista to the graveyard")
    void zeroXDies() {
        harness.setHand(player1, List.of(new WalkingBallista()));

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Walking Ballista");
        harness.assertInGraveyard(player1, "Walking Ballista");
    }

    @Test
    @DisplayName("Both X symbols must be paid when casting Ballista")
    void cannotCastWithManaForOnlyOneX() {
        harness.setHand(player1, List.of(new WalkingBallista()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 3, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertNotOnBattlefield(player1, "Walking Ballista");
    }

    @Test
    @DisplayName("The counter ability requires four mana")
    void cannotAddCounterWithThreeMana() {
        Permanent ballista = addReadyBallista(player1, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(ballista.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removing the last counter kills Ballista before its damage resolves")
    void lastCounterIsPaidBeforeDamageResolves() {
        addReadyBallista(player1, 1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 1, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Walking Ballista");
        harness.assertInGraveyard(player1, "Walking Ballista");
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Both abilities work while Ballista is tapped and summoning sick")
    void abilitiesDoNotRequireTapOrHaste() {
        Permanent ballista = addReadyBallista(player1, 2);
        ballista.setSummoningSick(true);
        ballista.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(ballista.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(ballista.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(ballista.isTapped()).isTrue();
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Ballista can deal damage to a planeswalker")
    void damagesPlaneswalker() {
        addReadyBallista(player1, 2);
        Permanent tezzeret = harness.addToBattlefieldAndReturn(player2, new TezzeretTheSchemer());
        tezzeret.setCounterCount(CounterType.LOYALTY, 5);

        harness.activateAbility(player1, 0, 1, null, tezzeret.getId());
        harness.passBothPriorities();

        assertThat(tezzeret.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("Ballista may target itself")
    void canDamageItself() {
        Permanent ballista = addReadyBallista(player1, 3);

        harness.activateAbility(player1, 0, 1, null, ballista.getId());
        harness.passBothPriorities();

        assertThat(ballista.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(ballista.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Walking Ballista");
    }

    private Permanent addReadyBallista(Player player, int counters) {
        Permanent ballista = harness.addToBattlefieldAndReturn(player, new WalkingBallista());
        ballista.setSummoningSick(false);
        ballista.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, counters);
        return ballista;
    }
}
