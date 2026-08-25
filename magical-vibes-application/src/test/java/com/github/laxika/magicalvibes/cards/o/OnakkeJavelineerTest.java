package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DisciplesOfTheInferno;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InvasionOfRegatha;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DisciplesOfTheInferno.class, GrizzlyBears.class, InvasionOfRegatha.class, OnakkeJavelineer.class})
class OnakkeJavelineerTest extends BaseCardTest {

    @Test
    void dealsTwoDamageToTargetPlayer() {
        addReadyJavelineer(player1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    void dealsTwoDamageToTargetBattle() {
        addReadyJavelineer(player1);
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfRegatha());
        battle.setCounterCount(CounterType.DEFENSE, 5);

        harness.activateAbility(player1, 0, null, battle.getId());
        harness.passBothPriorities();

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(3);
    }

    @Test
    void cannotTargetCreature() {
        addReadyJavelineer(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyJavelineer(Player player) {
        Permanent javelineer = new Permanent(new OnakkeJavelineer());
        javelineer.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(javelineer);
        return javelineer;
    }
}
