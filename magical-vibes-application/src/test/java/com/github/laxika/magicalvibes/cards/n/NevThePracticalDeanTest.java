package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.FanningTheFlames;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NevThePracticalDean.class, FanningTheFlames.class, GrizzlyBears.class})
class NevThePracticalDeanTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control with any counters have trample")
    void creaturesWithAnyCountersHaveTrample() {
        harness.addToBattlefield(player1, new NevThePracticalDean());
        Permanent counteredCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent uncounteredCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        counteredCreature.setCounterCount(CounterType.CHARGE, 1);

        assertThat(gqs.hasKeyword(gd, counteredCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, uncounteredCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The first X spell each turn puts X +1/+1 counters on Nev")
    void firstXSpellPutsItsXOnNev() {
        Permanent nev = harness.addToBattlefieldAndReturn(player1, new NevThePracticalDean());
        harness.setHand(player1, List.of(new FanningTheFlames()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, 1, player2.getId());
        harness.passBothPriorities();

        assertThat(nev.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
