package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AssaultronDominator.class, Memnite.class, GrizzlyBears.class})
class AssaultronDominatorTest extends BaseCardTest {

    @Test
    void entersWithTwoEnergyCounters() {
        Permanent dominator = harness.enterBattlefieldAndReturn(player1, new AssaultronDominator());
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(dominator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @ParameterizedTest
    @CsvSource({
            "Put a +1/+1 counter on that creature, PLUS_ONE_PLUS_ONE",
            "Put a first strike counter on that creature, FIRST_STRIKE",
            "Put a trample counter on that creature, TRAMPLE"
    })
    void paysEnergyAndPutsTheChosenCounterOnTheAttackingArtifactCreature(
            String choice, CounterType counterType) {
        addCreatureReady(player1, new AssaultronDominator());
        Permanent attacker = addCreatureReady(player1, new Memnite());
        gd.playerEnergyCounters.put(player1.getId(), 1);

        declareAttackers(List.of(1));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, choice);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(attacker.getCounterCount(counterType)).isEqualTo(1);
        if (counterType == CounterType.FIRST_STRIKE) {
            assertThat(gqs.hasKeyword(gd, attacker, Keyword.FIRST_STRIKE)).isTrue();
        } else if (counterType == CounterType.TRAMPLE) {
            assertThat(gqs.hasKeyword(gd, attacker, Keyword.TRAMPLE)).isTrue();
        }
    }

    @Test
    void doesNotTriggerForNonartifactCreature() {
        addCreatureReady(player1, new AssaultronDominator());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        gd.playerEnergyCounters.put(player1.getId(), 1);

        declareAttackers(List.of(1));
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(attacker.getCounterCount(CounterType.FIRST_STRIKE)).isZero();
        assertThat(attacker.getCounterCount(CounterType.TRAMPLE)).isZero();
    }

}
