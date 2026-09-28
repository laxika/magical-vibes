package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
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

@CardUsed({TyranidPrime.class, GrizzlyBears.class, HillGiant.class})
class TyranidPrimeTest extends BaseCardTest {

    @Test
    @DisplayName("Other creatures you control have evolve")
    void grantsEvolveToOtherCreaturesOnly() {
        Permanent prime = harness.addToBattlefieldAndReturn(player1, new TyranidPrime());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, prime, Keyword.EVOLVE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.EVOLVE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.EVOLVE)).isFalse();
    }

    @Test
    @DisplayName("Granted evolve puts a +1/+1 counter on the other creature")
    void grantedEvolveTriggers() {
        Permanent prime = harness.addToBattlefieldAndReturn(player1, new TyranidPrime());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(prime.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
