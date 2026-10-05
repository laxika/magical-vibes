package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DrakeHaven;
import com.github.laxika.magicalvibes.cards.d.DuneBeetle;
import com.github.laxika.magicalvibes.cards.w.WaywardServant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LilianaDeathWielder.class, DuneBeetle.class, DrakeHaven.class, WaywardServant.class})
class LilianaDeathWielderTest extends BaseCardTest {

    @Test
    @DisplayName("+2 puts a -1/-1 counter on target creature and raises loyalty")
    void plusTwoPutsMinusOneCounter() {
        Permanent liliana = addReadyLiliana(player1, 5);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new DuneBeetle());

        harness.activateAbility(player1, 0, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
    }

    @Test
    @DisplayName("+2 can activate with no target")
    void plusTwoCanActivateWithNoTarget() {
        Permanent liliana = addReadyLiliana(player1, 5);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
    }

    @Test
    @DisplayName("-3 destroys a creature with a -1/-1 counter")
    void minusThreeDestroysCreatureWithCounter() {
        Permanent liliana = addReadyLiliana(player1, 5);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new DuneBeetle());
        bear.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        harness.activateAbility(player1, 0, 1, null, bear.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bear);
        harness.assertInGraveyard(player2, "Dune Beetle");
        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("-3 rejects a creature without a -1/-1 counter")
    void minusThreeRejectsCreatureWithoutCounter() {
        addReadyLiliana(player1, 5);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new DuneBeetle());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bear);
    }

    @Test
    @DisplayName("-10 returns all creature cards from your graveyard to the battlefield")
    void minusTenReturnsAllCreaturesFromGraveyard() {
        Permanent liliana = addReadyLiliana(player1, 10);
        harness.setGraveyard(player1, List.of(new DuneBeetle(), new DuneBeetle(), new DrakeHaven()));
        harness.setGraveyard(player2, List.of(new DuneBeetle()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(0);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Dune Beetle"))
                .count()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Drake Haven");
        harness.assertInGraveyard(player2, "Dune Beetle");
        assertThat(gd.playerGraveyards.get(player1.getId()).stream()
                .noneMatch(c -> c.getName().equals("Dune Beetle"))).isTrue();
    }

    @Test
    void minusThreeDoesNotDestroyCreatureIfItsLastMinusOneCounterIsRemoved() {
        Permanent liliana = addReadyLiliana(player1, 5);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DuneBeetle());
        creature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        creature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Dune Beetle");
        harness.assertNotInGraveyard(player2, "Dune Beetle");
        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    void minusThreeRejectsNoncreatureWithMinusOneCounter() {
        addReadyLiliana(player1, 5);
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new DrakeHaven());
        enchantment.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Drake Haven");
    }

    @Test
    void minusTenReturnsCreaturesSimultaneouslySoTheySeeEachOtherEnter() {
        addReadyLiliana(player1, 11);
        harness.setGraveyard(player1, List.of(new WaywardServant(), new WaywardServant()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof WaywardServant).count()).isEqualTo(2);
    }

    @Test
    void minusTenResolvesWithAnEmptyGraveyardAfterLilianaDiesToItsCost() {
        addReadyLiliana(player1, 10);
        harness.setGraveyard(player1, List.of());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Liliana, Death Wielder");
        harness.assertInGraveyard(player1, "Liliana, Death Wielder");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyLiliana(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new LilianaDeathWielder());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
