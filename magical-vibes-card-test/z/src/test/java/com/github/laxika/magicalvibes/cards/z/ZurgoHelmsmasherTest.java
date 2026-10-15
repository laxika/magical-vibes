package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.s.SmokeTeller;
import com.github.laxika.magicalvibes.cards.d.DragonsEyeSavants;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZurgoHelmsmasher.class, SmokeTeller.class, DragonsEyeSavants.class, TurnToFrog.class})
class ZurgoHelmsmasherTest extends BaseCardTest {

    @Test
    @DisplayName("Zurgo must attack each combat when able")
    void mustAttackWhenAble() {
        addReadyZurgo(player1);

        assertThatThrownBy(() -> declareAttackers(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Zurgo has indestructible during its controller's turn only")
    void indestructibleDuringControllerTurnOnly() {
        Permanent zurgo = addReadyZurgo(player1);
        zurgo.setMarkedDamage(2);

        harness.forceActivePlayer(player1);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(zurgo);

        harness.forceActivePlayer(player2);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(zurgo);
    }

    @Test
    @DisplayName("Zurgo gets a +1/+1 counter when a creature it damaged dies")
    void gainsCounterWhenDamagedCreatureDies() {
        Permanent zurgo = addReadyZurgo(player1);
        Permanent bears = addCreatureReady(player2, new SmokeTeller());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(zurgo);
        assertThat(zurgo.getMarkedDamage()).isEqualTo(2);
        assertThat(zurgo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A tapped Zurgo is not required to attack")
    void tappedZurgoCanStayOutOfCombat() {
        Permanent zurgo = addReadyZurgo(player1);
        zurgo.tap();

        declareAttackers(player1, List.of());

        assertThat(zurgo.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Zurgo can attack while summoning sick because it has haste")
    void canAttackWithHaste() {
        Permanent zurgo = addReadyZurgo(player1);
        zurgo.setSummoningSick(true);
        addCreatureReady(player2, new SmokeTeller());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThat(zurgo.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("A creature Zurgo did not damage dying gives no counter")
    void unrelatedDeathDoesNotGiveCounter() {
        Permanent zurgo = addReadyZurgo(player1);
        Permanent creature = addCreatureReady(player2, new SmokeTeller());
        creature.setMarkedDamage(2);

        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(zurgo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Indestructible does not save Zurgo from zero toughness on its controller's turn")
    void zeroToughnessStillKillsZurgo() {
        Permanent zurgo = addReadyZurgo(player1);
        zurgo.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        harness.forceActivePlayer(player1);

        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(zurgo);
    }

    private Permanent addReadyZurgo(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new ZurgoHelmsmasher());
    }

    @Test
    @DisplayName("Zurgo cannot trigger for a later death after losing its abilities")
    void losingAbilitiesBeforeDamagedCreatureDiesPreventsCounter() {
        Permanent zurgo = addReadyZurgo(player1);
        Permanent savants = addCreatureReady(player2, new DragonsEyeSavants());
        savants.setCounterCount(CounterType.PLUS_ZERO_PLUS_ONE, 2);

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveAllTriggers();
        assertThat(savants.getMarkedDamage()).isEqualTo(7);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(savants);

        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, zurgo.getId());
        savants.setMarkedDamage(8);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(savants);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(zurgo);
        assertThat(zurgo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
