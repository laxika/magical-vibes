package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MetallicSliver;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FirstSliversChosen.class, MetallicSliver.class, GrizzlyBears.class})
class FirstSliversChosenTest extends BaseCardTest {

    @Test
    @DisplayName("Slivers you control get exalted when attacking alone")
    void sliverAttackingAloneGetsExaltedBoost() {
        addCreatureReady(player1, new FirstSliversChosen());
        Permanent sliver = addCreatureReady(player1, new MetallicSliver());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, sliver)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sliver)).isEqualTo(3);
    }

    @Test
    @DisplayName("A non-Sliver attacking alone gets the boost from Slivers' exalted")
    void nonSliverAttackingAloneGetsExaltedBoost() {
        addCreatureReady(player1, new FirstSliversChosen());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Multiple First Sliver's Chosen grant multiple exalted instances")
    void multipleSourcesGrantMultipleExaltedInstances() {
        addCreatureReady(player1, new FirstSliversChosen());
        addCreatureReady(player1, new FirstSliversChosen());
        Permanent sliver = addCreatureReady(player1, new MetallicSliver());

        declareAttackers(List.of(2));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, sliver)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, sliver)).isEqualTo(7);
    }

    @Test
    @DisplayName("First Sliver's Chosen grants exalted to itself")
    void grantsExaltedToItself() {
        Permanent chosen = addCreatureReady(player1, new FirstSliversChosen());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, chosen)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, chosen)).isEqualTo(4);
    }

    @Test
    @DisplayName("Exalted does not trigger when two creatures attack")
    void multipleAttackersDoNotTriggerExalted() {
        Permanent first = addCreatureReady(player1, new FirstSliversChosen());
        Permanent second = addCreatureReady(player1, new FirstSliversChosen());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0, 1)));

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
    }

    @Test
    @DisplayName("Opposing Slivers do not contribute exalted to your attacker")
    void opposingSliversDoNotContributeExalted() {
        Permanent chosen = addCreatureReady(player1, new FirstSliversChosen());
        addCreatureReady(player2, new FirstSliversChosen());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, chosen)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, chosen)).isEqualTo(4);
    }

    @Test
    @DisplayName("Each exalted instance creates a separate triggered ability")
    void multipleExaltedInstancesTriggerIndependently() {
        Permanent attacker = addCreatureReady(player1, new FirstSliversChosen());
        addCreatureReady(player1, new FirstSliversChosen());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));

        assertThat(gd.stack).hasSize(4);
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(7);
    }

    @Test
    @DisplayName("Exalted triggers survive removal of the granting source")
    void exaltedTriggersSurviveSourceRemoval() {
        Permanent source = addCreatureReady(player1, new FirstSliversChosen());
        Permanent attacker = addCreatureReady(player1, new MetallicSliver());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(1)));
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, source);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);
    }
}
