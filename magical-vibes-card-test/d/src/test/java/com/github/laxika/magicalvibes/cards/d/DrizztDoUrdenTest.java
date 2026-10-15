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
        resolveAllTriggers();

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

    @Test
    @DisplayName("Does not trigger when the dying creature has equal power")
    void doesNotTriggerForEqualPower() {
        Permanent drizzt = harness.addToBattlefieldAndReturn(player1, new DrizztDoUrden());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        bears.setMarkedDamage(gqs.getEffectiveToughness(gd, bears));

        harness.runStateBasedActions();

        assertThat(gd.stack).isEmpty();
        assertThat(drizzt.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Adds no counters if Drizzt catches up before the trigger resolves")
    void addsNoCountersWhenPowerBecomesEqual() {
        Permanent drizzt = harness.addToBattlefieldAndReturn(player1, new DrizztDoUrden());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        bears.setMarkedDamage(gqs.getEffectiveToughness(gd, bears));

        harness.runStateBasedActions();
        assertThat(gd.stack).hasSize(1);
        drizzt.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        resolveAllTriggers();

        assertThat(drizzt.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Simultaneous deaths compare against Drizzt's updated power on each resolution")
    void simultaneousDeathsDoNotAddTheDifferencesTogether() {
        Permanent drizzt = harness.addToBattlefieldAndReturn(player1, new DrizztDoUrden());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        second.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        first.setMarkedDamage(gqs.getEffectiveToughness(gd, first));
        second.setMarkedDamage(gqs.getEffectiveToughness(gd, second));

        harness.runStateBasedActions();
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(drizzt.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The difference uses Drizzt's actual negative power")
    void subtractsNegativeDrizztPower() {
        Permanent drizzt = harness.addToBattlefieldAndReturn(player1, new DrizztDoUrden());
        drizzt.setPowerModifier(-4);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        bears.setMarkedDamage(gqs.getEffectiveToughness(gd, bears));

        harness.runStateBasedActions();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(drizzt.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    @DisplayName("The difference preserves a dying creature's negative power")
    void preservesNegativeDyingPower() {
        Permanent drizzt = harness.addToBattlefieldAndReturn(player1, new DrizztDoUrden());
        drizzt.setPowerModifier(-5);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.setPowerModifier(-3);
        bears.setMarkedDamage(gqs.getEffectiveToughness(gd, bears));

        harness.runStateBasedActions();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(drizzt.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Guenhwyvar dying gives Drizzt a counter even though it is a token")
    void triggersForGuenhwyvarDeath() {
        harness.setHand(player1, List.of(new DrizztDoUrden()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        Permanent drizzt = findPermanent(player1, "Drizzt Do'Urden");
        Permanent guenhwyvar = findPermanent(player1, "Guenhwyvar");
        guenhwyvar.setMarkedDamage(1);

        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Guenhwyvar");
        assertThat(drizzt.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Double strike deals damage in both combat damage steps")
    void dealsDoubleStrikeCombatDamage() {
        addCreatureReady(player1, new DrizztDoUrden());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 14);
    }
}
