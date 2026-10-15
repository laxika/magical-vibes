package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GoblinHero;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AgentPhilCoulson.class, AgentMariaHill.class, GoblinHero.class})
class AgentPhilCoulsonTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Agent Phil Coulson puts counters on other Heroes you control")
    void putsCountersOnOtherHeroesYouControl() {
        Permanent coulson = addCreatureReady(player1, new AgentPhilCoulson());
        Permanent ownHero = addCreatureReady(player1, new AgentMariaHill());
        Permanent ownNonHero = addCreatureReady(player1, new GoblinHero());
        Permanent opponentHero = addCreatureReady(player2, new AgentMariaHill());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(coulson.isTapped()).isTrue();
        assertThat(coulson.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(ownHero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownNonHero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentHero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The ability can be activated with no other Heroes")
    void canActivateWithNoOtherHeroes() {
        Permanent coulson = addCreatureReady(player1, new AgentPhilCoulson());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(coulson.isTapped()).isTrue();
        assertThat(coulson.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Heroes that enter before resolution receive a counter")
    void checksHeroesAtResolution() {
        Permanent coulson = addCreatureReady(player1, new AgentPhilCoulson());
        harness.activateAbility(player1, 0, null, null);

        Permanent hero = harness.addToBattlefieldAndReturn(player1, new AgentMariaHill());
        assertThat(hero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(hero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(coulson.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The ability resolves after Coulson leaves the battlefield")
    void resolvesWithoutSourceOnBattlefield() {
        Permanent coulson = addCreatureReady(player1, new AgentPhilCoulson());
        Permanent hero = addCreatureReady(player1, new AgentMariaHill());
        harness.activateAbility(player1, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).remove(coulson);
        gd.playerGraveyards.get(player1.getId()).add(coulson.getCard());
        harness.passBothPriorities();

        assertThat(hero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Vigilance lets Coulson attack and then activate his tap ability")
    void canActivateAfterAttacking() {
        Permanent coulson = addCreatureReady(player1, new AgentPhilCoulson());
        Permanent hero = addCreatureReady(player1, new AgentMariaHill());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        assertThat(coulson.isTapped()).isFalse();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(coulson.isTapped()).isTrue();
        assertThat(hero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Summoning sickness prevents activating the tap ability")
    void cannotActivateWhileSummoningSick() {
        Permanent coulson = harness.addToBattlefieldAndReturn(player1, new AgentPhilCoulson());
        Permanent hero = addCreatureReady(player1, new AgentMariaHill());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(coulson.isTapped()).isFalse();
        assertThat(hero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
