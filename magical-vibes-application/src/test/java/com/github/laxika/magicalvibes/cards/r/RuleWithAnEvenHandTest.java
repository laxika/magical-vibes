package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RuleWithAnEvenHand.class, GrizzlyBears.class})
class RuleWithAnEvenHandTest extends BaseCardTest {

    @Test
    void cannotAttackWithAnOddNumberOfCreatures() {
        gd.playerCommandZones.get(player1.getId()).add(new RuleWithAnEvenHand());
        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("You can't attack with an odd number of creatures.");
    }

    @Test
    void doublesTargetCreaturePowerWhenAnEvenNumberOfCreaturesAttack() {
        gd.playerCommandZones.get(player1.getId()).add(new RuleWithAnEvenHand());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1));
            harness.handlePermanentChosen(player1, target.getId());
            assertThat(gd.interaction.isAwaitingInput()).isFalse();
            resolveAllTriggers();

            assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        });
    }

    @Test
    void powerDoublingWearsOffAtEndOfTurn() {
        gd.playerCommandZones.get(player1.getId()).add(new RuleWithAnEvenHand());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1));
            harness.handlePermanentChosen(player1, target.getId());
            assertThat(gd.interaction.isAwaitingInput()).isFalse();
            resolveAllTriggers();
            assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        });

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
    }

    @Test
    void declaringNoAttackersDoesNotTriggerPowerDoubling() {
        gd.playerCommandZones.get(player1.getId()).add(new RuleWithAnEvenHand());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of());
            assertThat(gd.interaction.isAwaitingInput()).isFalse();
            assertThat(gd.stack).isEmpty();
            assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        });
    }

    @Test
    void opponentCanAttackWithOneCreatureWithoutTriggeringTheConspiracy() {
        gd.playerCommandZones.get(player1.getId()).add(new RuleWithAnEvenHand());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player2, List.of(0));
            assertThat(gd.interaction.isAwaitingInput()).isFalse();
            assertThat(gd.stack).isEmpty();
            assertThat(attacker.isAttacking()).isTrue();
            assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        });
    }

    @Test
    void fourAttackersStillProduceOnlyOnePowerDoubling() {
        gd.playerCommandZones.get(player1.getId()).add(new RuleWithAnEvenHand());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1, 2, 3));
            harness.handlePermanentChosen(player1, target.getId());
            assertThat(gd.interaction.isAwaitingInput()).isFalse();
            resolveAllTriggers();
            assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        });
    }
}
