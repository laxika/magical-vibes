package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JelennSphinx.class, GrizzlyBears.class})
class JelennSphinxTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking pumps other attacking creatures but not itself")
    void pumpsOtherAttackersOnly() {
        Permanent sphinx = addCreatureReady(player1, new JelennSphinx());
        Permanent otherAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent homeBody = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, otherAttacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, otherAttacker)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, sphinx)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, sphinx)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, homeBody)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new JelennSphinx());
        Permanent otherAttacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, otherAttacker)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, otherAttacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherAttacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Attacking alone boosts nothing")
    void attackingAloneBoostsNothing() {
        Permanent sphinx = addCreatureReady(player1, new JelennSphinx());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, sphinx)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, sphinx)).isEqualTo(5);
    }

    @Test
    @DisplayName("Two attacking Sphinxes boost each other and both boost another attacker")
    void multipleSphinxTriggersStack() {
        Permanent first = addCreatureReady(player1, new JelennSphinx());
        Permanent second = addCreatureReady(player1, new JelennSphinx());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(4);
    }

    @Test
    @DisplayName("An attack trigger still boosts other attackers after the Sphinx leaves")
    void triggerResolvesWithoutSource() {
        Permanent sphinx = addCreatureReady(player1, new JelennSphinx());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1));
            assertThat(gd.stack).hasSize(1);
            gd.playerBattlefields.get(player1.getId()).remove(sphinx);
            gd.playerGraveyards.get(player1.getId()).add(sphinx.getCard());
            resolveAllTriggers();
        });

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
    }

    @Test
    @DisplayName("A creature removed from combat before resolution receives no boost")
    void checksAttackingStatusAtResolution() {
        addCreatureReady(player1, new JelennSphinx());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1));
            assertThat(gd.stack).hasSize(1);
            bear.setAttacking(false);
            resolveAllTriggers();
        });

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }
}
