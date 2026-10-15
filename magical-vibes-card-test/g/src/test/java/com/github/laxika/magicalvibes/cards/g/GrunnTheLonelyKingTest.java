package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BalothGorger;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrunnTheLonelyKing.class, BalothGorger.class})
class GrunnTheLonelyKingTest extends BaseCardTest {

    @Test
    @DisplayName("Cast without kicker — enters as 5/5 with no counters")
    void castWithoutKicker() {
        harness.setHand(player1, List.of(new GrunnTheLonelyKing()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 4); // 4 generic

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent grunn = findPermanent(player1, "Grunn, the Lonely King");
        assertThat(grunn).isNotNull();
        assertThat(grunn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Cast with kicker — enters with five +1/+1 counters")
    void castWithKicker() {
        harness.setHand(player1, List.of(new GrunnTheLonelyKing()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 7); // 4 generic + 3 kicker

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        Permanent grunn = findPermanent(player1, "Grunn, the Lonely King");
        assertThat(grunn).isNotNull();
        assertThat(grunn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Attacking alone puts trigger on the stack")
    void attackingAlonePutsTriggerOnStack() {
        addCreatureReady(player1, new GrunnTheLonelyKing());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grunn, the Lonely King");
    }

    @Test
    @DisplayName("Attacking alone — non-kicked 5/5 becomes 10/10 until end of turn")
    void attackingAloneDoublesPowerToughness() {
        Permanent grunn = addCreatureReady(player1, new GrunnTheLonelyKing());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities(); // resolve trigger

        int power = gqs.getEffectivePower(gd, grunn);
        int toughness = gqs.getEffectiveToughness(gd, grunn);
        assertThat(power).isEqualTo(10);
        assertThat(toughness).isEqualTo(10);
    }

    @Test
    @DisplayName("Attacking alone — kicked 10/10 becomes 20/20 until end of turn")
    void attackingAloneKickedDoubles() {
        Permanent grunn = addCreatureReady(player1, new GrunnTheLonelyKing());
        grunn.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5); // simulate kicked ETB

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities(); // resolve trigger

        int power = gqs.getEffectivePower(gd, grunn);
        int toughness = gqs.getEffectiveToughness(gd, grunn);
        assertThat(power).isEqualTo(20);
        assertThat(toughness).isEqualTo(20);
    }

    @Test
    @DisplayName("Attacking with another creature — trigger does not fire")
    void attackingWithOtherCreatureNoTrigger() {
        addCreatureReady(player1, new GrunnTheLonelyKing());
        addCreatureReady(player1, new BalothGorger());

        declareAttackers(player1, List.of(0, 1));

        // No attacks-alone trigger should be on the stack
        assertThat(gd.stack).noneMatch(e -> e.getCard().getName().equals("Grunn, the Lonely King"));
    }

    @Test
    @DisplayName("Attacking with another creature — power/toughness remain base values")
    void attackingWithOtherCreatureNoPTChange() {
        Permanent grunn = addCreatureReady(player1, new GrunnTheLonelyKing());
        addCreatureReady(player1, new BalothGorger());

        declareAttackers(player1, List.of(0, 1));

        int power = gqs.getEffectivePower(gd, grunn);
        int toughness = gqs.getEffectiveToughness(gd, grunn);
        assertThat(power).isEqualTo(5);
        assertThat(toughness).isEqualTo(5);
    }

    @Test
    @DisplayName("Kicker consumes three additional mana")
    void kickerConsumesAdditionalMana() {
        harness.setHand(player1, List.of(new GrunnTheLonelyKing()));
        harness.addMana(player1, ManaColor.GREEN, 9);

        harness.castKickedCreature(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grunn, the Lonely King")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Nonattacking creatures do not prevent the attacks-alone trigger")
    void otherCreatureStayingBackDoesNotPreventTrigger() {
        Permanent grunn = addCreatureReady(player1, new GrunnTheLonelyKing());
        addCreatureReady(player1, new BalothGorger());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, grunn)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, grunn)).isEqualTo(10);
    }

    @Test
    @DisplayName("Doubling uses power and toughness at resolution and is a fixed bonus afterward")
    void doublingUsesResolutionValues() {
        Permanent grunn = addCreatureReady(player1, new GrunnTheLonelyKing());

        declareAttackers(player1, List.of(0));
        grunn.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, grunn)).isEqualTo(14);
        assertThat(gqs.getEffectiveToughness(gd, grunn)).isEqualTo(14);

        grunn.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        assertThat(gqs.getEffectivePower(gd, grunn)).isEqualTo(15);
        assertThat(gqs.getEffectiveToughness(gd, grunn)).isEqualTo(15);
    }

    @Test
    @DisplayName("Doubling expires at end of turn while counters remain")
    void doublingExpiresAtEndOfTurn() {
        Permanent grunn = addCreatureReady(player1, new GrunnTheLonelyKing());
        grunn.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, grunn)).isEqualTo(14);
        assertThat(gqs.getEffectiveToughness(gd, grunn)).isEqualTo(14);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, grunn)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, grunn)).isEqualTo(7);
        assertThat(grunn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Doubling handles negative power independently of toughness")
    void doublingNegativePower() {
        Permanent grunn = addCreatureReady(player1, new GrunnTheLonelyKing());
        grunn.setPowerModifier(-7);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, grunn)).isEqualTo(-4);
        assertThat(gqs.getEffectiveToughness(gd, grunn)).isEqualTo(10);
    }
}
