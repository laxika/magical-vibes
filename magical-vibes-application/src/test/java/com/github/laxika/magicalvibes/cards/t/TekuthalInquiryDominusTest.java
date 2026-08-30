package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IronStar;
import com.github.laxika.magicalvibes.cards.v.ViralDrake;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TekuthalInquiryDominusTest extends BaseCardTest {

    @Test
    @DisplayName("Doubles a proliferate event into two sequential choices")
    void doublesProliferate() {
        harness.addToBattlefield(player1, new TekuthalInquiryDominus());
        harness.addToBattlefield(player1, new ViralDrake());
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Removes three counters from eligible other permanents and adds an indestructible counter")
    void removesThreeCountersFromOtherEligiblePermanents() {
        Permanent tekuthal = harness.addToBattlefieldAndReturn(player1, new TekuthalInquiryDominus());
        tekuthal.setCounterCount(CounterType.INDESTRUCTIBLE, 1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        otherCreature.setCounterCount(CounterType.OIL, 1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new IronStar());
        artifact.setCounterCount(CounterType.CHARGE, 1);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(otherCreature.getCounterCount(CounterType.OIL)).isZero();
        assertThat(artifact.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(tekuthal.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, tekuthal, Keyword.INDESTRUCTIBLE)).isTrue();
    }
}
