package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AvacynianPriest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeraldOfWar.class, AvacynianPriest.class, SerraAngel.class, GrizzlyBears.class})
class HeraldOfWarTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter when it attacks")
    void getsCounterWhenAttacking() {
        Permanent herald = addCreatureReady(player1, new HeraldOfWar());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).anyMatch(entry ->
                entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && entry.getCard().getName().equals("Herald of War"));

        harness.passBothPriorities();

        assertThat(herald.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, herald)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, herald)).isEqualTo(4);
    }

    @Test
    @DisplayName("With no counters, Human spells are not reduced")
    void noCountersNoReduction() {
        harness.addToBattlefield(player1, new HeraldOfWar());
        // Avacynian Priest costs {1}{W}; a single {W} is not enough without a reduction
        harness.setHand(player1, List.of(new AvacynianPriest()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("One +1/+1 counter makes Human spells cost {1} less")
    void oneCounterReducesHumanSpell() {
        Permanent herald = harness.addToBattlefieldAndReturn(player1, new HeraldOfWar());
        herald.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        // Avacynian Priest costs {1}{W} — with {1} reduction it costs just {W}
        harness.setHand(player1, List.of(new AvacynianPriest()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Avacynian Priest");
    }

    @Test
    @DisplayName("Two +1/+1 counters make Angel spells cost {2} less")
    void twoCountersReduceAngelSpell() {
        Permanent herald = harness.addToBattlefieldAndReturn(player1, new HeraldOfWar());
        herald.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        // Serra Angel costs {3}{W}{W} — with {2} reduction it costs {1}{W}{W}
        harness.setHand(player1, List.of(new SerraAngel()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Serra Angel");
    }

    @Test
    @DisplayName("Reduction never exceeds the generic portion of the cost")
    void reductionDoesNotEatColoredMana() {
        Permanent herald = harness.addToBattlefieldAndReturn(player1, new HeraldOfWar());
        herald.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);
        // Serra Angel costs {3}{W}{W} — at most {3} can be reduced, so {W}{W} remains
        harness.setHand(player1, List.of(new SerraAngel()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Spells that are neither Angel nor Human are not reduced")
    void otherSpellsNotReduced() {
        Permanent herald = harness.addToBattlefieldAndReturn(player1, new HeraldOfWar());
        herald.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        // Grizzly Bears costs {1}{G} — a Bear, so no reduction applies
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Opponent's Angel spells are not reduced")
    void opponentSpellsNotReduced() {
        Permanent herald = harness.addToBattlefieldAndReturn(player1, new HeraldOfWar());
        herald.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player2, List.of(new SerraAngel()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Attack counter is added on resolution only to the attacking Herald")
    void attackCounterWaitsForResolutionAndUsesItsSource() {
        Permanent attacker = addCreatureReady(player1, new HeraldOfWar());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent otherHerald = addCreatureReady(player1, new HeraldOfWar());

        declareAttackers(player1, List.of(0));

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(otherHerald.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(otherHerald.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Multiple Heralds add their reductions using their own counter counts")
    void multipleHeraldsCombineReductions() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new HeraldOfWar());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent second = harness.addToBattlefieldAndReturn(player1, new HeraldOfWar());
        second.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new HeraldOfWar()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Other counter types do not reduce Angel costs")
    void otherCountersDoNotReduceCosts() {
        Permanent herald = harness.addToBattlefieldAndReturn(player1, new HeraldOfWar());
        herald.setCounterCount(CounterType.CHARGE, 3);
        harness.setHand(player1, List.of(new HeraldOfWar()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Excess reduction permits casting an Angel with exactly its colored cost")
    void excessReductionLeavesOnlyColoredCost() {
        Permanent herald = harness.addToBattlefieldAndReturn(player1, new HeraldOfWar());
        herald.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);
        harness.setHand(player1, List.of(new HeraldOfWar()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
