package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({DrizztDoUrden.class, GrizzlyBears.class})
class DrizztDoUrdenTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a legendary 4/1 green Cat token with trample")
    void createsGuenhwyvar() {
        harness.setHand(player1, List.of(new DrizztDoUrden()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent guenhwyvar = findPermanent(player1, "Guenhwyvar");
        assertThat(guenhwyvar.getCard().isToken()).isTrue();
        assertThat(guenhwyvar.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(guenhwyvar.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(guenhwyvar.getCard().getSubtypes()).containsExactly(CardSubtype.CAT);
        assertThat(guenhwyvar.getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);
        assertThat(guenhwyvar.getEffectivePower()).isEqualTo(4);
        assertThat(guenhwyvar.getEffectiveToughness()).isEqualTo(1);
        assertThat(guenhwyvar.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Puts the power difference in +1/+1 counters when a larger creature dies")
    void putsPowerDifferenceOnDrizzt() {
        Permanent drizzt = harness.addToBattlefieldAndReturn(player1, new DrizztDoUrden());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        bears.setMarkedDamage(gqs.getEffectiveToughness(gd, bears));

        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(drizzt.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Rechecks Drizzt's power when the death trigger resolves")
    void recalculatesDifferenceOnResolution() {
        Permanent drizzt = harness.addToBattlefieldAndReturn(player1, new DrizztDoUrden());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        bears.setMarkedDamage(gqs.getEffectiveToughness(gd, bears));

        harness.runStateBasedActions();
        drizzt.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        resolveAllTriggers();

        assertThat(drizzt.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger for a creature whose power is not greater than Drizzt's")
    void doesNotTriggerForSmallerCreature() {
        Permanent drizzt = harness.addToBattlefieldAndReturn(player1, new DrizztDoUrden());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.setMarkedDamage(gqs.getEffectiveToughness(gd, bears));

        harness.runStateBasedActions();

        assertThat(gd.stack).isEmpty();
        assertThat(drizzt.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
