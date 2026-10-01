package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BallynockCohort;
import com.github.laxika.magicalvibes.cards.b.BoonReflection;
import com.github.laxika.magicalvibes.cards.c.Cinderbones;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MidnightBanshee.class, BallynockCohort.class, BoonReflection.class, Cinderbones.class})
class MidnightBansheeTest extends BaseCardTest {

    @Test
    @DisplayName("Your upkeep puts a -1/-1 counter on each nonblack creature (all players)")
    void upkeepCountersNonblackCreatures() {
        harness.addToBattlefield(player1, new MidnightBanshee());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new BallynockCohort());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new BallynockCohort());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(ownCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Black creatures (including the Banshee) get no counter")
    void blackCreaturesUnaffected() {
        Permanent banshee = harness.addToBattlefieldAndReturn(player1, new MidnightBanshee());
        Permanent blackCreature = harness.addToBattlefieldAndReturn(player2, new Cinderbones());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(banshee.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(0);
        assertThat(blackCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Noncreature permanents get no counter")
    void noncreaturesUnaffected() {
        harness.addToBattlefield(player1, new MidnightBanshee());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new BoonReflection());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(enchantment.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Does not trigger on an opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new MidnightBanshee());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new BallynockCohort());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(opponentCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(0);
    }
}
