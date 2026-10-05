package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DisciplesOfTheInferno;
import com.github.laxika.magicalvibes.cards.i.InvasionOfRegatha;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DisciplesOfTheInferno.class, InvasionOfRegatha.class, OnakkeJavelineer.class})
class OnakkeJavelineerTest extends BaseCardTest {

    @Test
    void dealsTwoDamageToTargetPlayer() {
        addCreatureReady(player1, new OnakkeJavelineer());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    void dealsTwoDamageToTargetBattle() {
        addCreatureReady(player1, new OnakkeJavelineer());
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfRegatha());
        battle.setCounterCount(CounterType.DEFENSE, 5);

        harness.activateAbility(player1, 0, null, battle.getId());
        harness.passBothPriorities();

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(3);
    }

    @Test
    void cannotTargetCreature() {
        addCreatureReady(player1, new OnakkeJavelineer());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new OnakkeJavelineer());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTargetItsControllerAndTapsAsCost() {
        Permanent javelineer = addCreatureReady(player1, new OnakkeJavelineer());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, player1.getId());

        assertThat(javelineer.isTapped()).isTrue();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);

        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    void canTargetBattleControlledByItsController() {
        addCreatureReady(player1, new OnakkeJavelineer());
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfRegatha());
        battle.setCounterCount(CounterType.DEFENSE, 5);

        harness.activateAbility(player1, 0, null, battle.getId());
        harness.passBothPriorities();

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(3);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent javelineer = harness.addToBattlefieldAndReturn(player1, new OnakkeJavelineer());
        javelineer.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(javelineer.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent javelineer = addCreatureReady(player1, new OnakkeJavelineer());
        javelineer.setTapped(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void abilityResolvesAfterSourceLeavesBattlefield() {
        Permanent javelineer = addCreatureReady(player1, new OnakkeJavelineer());
        harness.setLife(player2, 20);
        harness.activateAbility(player1, 0, null, player2.getId());

        gd.playerBattlefields.get(player1.getId()).remove(javelineer);
        harness.setGraveyard(player1, java.util.List.of(javelineer.getCard()));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    void abilityDoesNotDamageBattleThatLeftBattlefield() {
        addCreatureReady(player1, new OnakkeJavelineer());
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfRegatha());
        battle.setCounterCount(CounterType.DEFENSE, 5);
        harness.activateAbility(player1, 0, null, battle.getId());

        gd.playerBattlefields.get(player2.getId()).remove(battle);
        harness.setGraveyard(player2, java.util.List.of(battle.getCard()));
        harness.passBothPriorities();

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }
}
