package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GoblinHero;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
}
