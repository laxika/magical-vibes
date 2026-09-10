package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HagraConstrictor.class, GrizzlyBears.class})
class HagraConstrictorTest extends BaseCardTest {

    @Test
    void entersWithTwoPlusOnePlusOneCountersAndHasMenace() {
        harness.setHand(player1, List.of(new HagraConstrictor()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent constrictor = findPermanent(player1, "Hagra Constrictor");
        assertThat(constrictor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, constrictor, Keyword.MENACE)).isTrue();
    }

    @Test
    void grantsMenaceOnlyToControlledCreaturesWithPlusOnePlusOneCounters() {
        harness.addToBattlefield(player1, new HagraConstrictor());
        Permanent counteredOwnCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent uncounteredOwnCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent counteredOpponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        counteredOwnCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        counteredOpponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, counteredOwnCreature, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, uncounteredOwnCreature, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, counteredOpponentCreature, Keyword.MENACE)).isFalse();
    }

    @Test
    void losesGrantedMenaceWhenItsPlusOnePlusOneCountersAreRemoved() {
        Permanent constrictor = harness.enterBattlefieldAndReturn(player1, new HagraConstrictor());

        assertThat(constrictor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, constrictor, Keyword.MENACE)).isTrue();

        constrictor.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        assertThat(gqs.hasKeyword(gd, constrictor, Keyword.MENACE)).isFalse();
    }
}
