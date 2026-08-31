package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoblinFireslinger;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({Aurification.class, Disenchant.class, Forest.class, GoblinFireslinger.class, GrizzlyBears.class})
class AurificationTest extends BaseCardTest {

    @Test
    @DisplayName("A creature that deals damage to you gets a gold counter and becomes a Wall with defender")
    void marksCreatureThatDealsDamage() {
        harness.addToBattlefield(player2, new Aurification());
        Permanent fireslinger = addCreatureReady(player1, new GoblinFireslinger());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(fireslinger.getCounterCount(CounterType.GOLD)).isEqualTo(1);
        assertThat(gqs.effectiveCreatureSubtypes(gd, fireslinger)).contains(CardSubtype.WALL);
        assertThat(gqs.hasKeyword(gd, fireslinger, Keyword.DEFENDER)).isTrue();
    }

    @Test
    @DisplayName("Leaving the battlefield removes gold counters from creatures but not other permanents")
    void leavesRemovesGoldCountersFromCreatures() {
        Permanent aurification = harness.addToBattlefieldAndReturn(player1, new Aurification());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        ownCreature.setCounterCount(CounterType.GOLD, 1);
        opposingCreature.setCounterCount(CounterType.GOLD, 2);
        land.setCounterCount(CounterType.GOLD, 3);

        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castInstant(player2, 0, aurification.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.GOLD)).isZero();
        assertThat(opposingCreature.getCounterCount(CounterType.GOLD)).isZero();
        assertThat(land.getCounterCount(CounterType.GOLD)).isEqualTo(3);
    }
}
