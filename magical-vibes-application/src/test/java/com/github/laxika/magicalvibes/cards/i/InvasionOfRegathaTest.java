package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DisciplesOfTheInferno;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DisciplesOfTheInferno.class, GrizzlyBears.class, InvasionOfRegatha.class, Shock.class})
class InvasionOfRegathaTest extends BaseCardTest {

    @Test
    void entersAndDealsDamageToAnotherBattleAndCreature() {
        Permanent otherBattle = harness.addToBattlefieldAndReturn(player2, new InvasionOfRegatha());
        otherBattle.setCounterCount(CounterType.DEFENSE, 5);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new InvasionOfRegatha()));
        addInvasionMana();
        gs.playCard(gd, player1, 0, 0, null, null,
                List.of(otherBattle.getId(), creature.getId()), List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(otherBattle.getCounterCount(CounterType.DEFENSE)).isEqualTo(1);
        assertThat(creature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void entersAndDealsFourDamageToAnOpponentWithoutCreatureTarget() {
        harness.setHand(player1, List.of(new InvasionOfRegatha()));
        addInvasionMana();
        harness.setLife(player2, 20);

        gs.playCard(gd, player1, 0, 0, null, null,
                List.of(player2.getId()), List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    void defeatingTheSiegeCastsTheCreatureBackFace() {
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfRegatha());
        battle.setCounterCount(CounterType.DEFENSE, 0);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));

        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent transformed = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.isTransformed())
                .findFirst()
                .orElseThrow();
        assertThat(transformed.getCard()).isInstanceOf(DisciplesOfTheInferno.class);
    }

    @Test
    void backFaceAddsTwoDamageFromNoncreatureSourcesToCreatures() {
        harness.addToBattlefield(player1, new InvasionOfRegatha().getBackFaceCard());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
    }

    @Test
    void backFaceAddsTwoDamageFromNoncreatureSourcesToOpponents() {
        harness.addToBattlefield(player1, new InvasionOfRegatha().getBackFaceCard());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    private void addInvasionMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
