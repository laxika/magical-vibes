package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ValakutFireboar.class})
class ValakutFireboarTest extends BaseCardTest {

    @Test
    @DisplayName("Switches power and toughness when it attacks")
    void switchesPowerAndToughnessWhenAttacking() {
        Permanent fireboar = addCreatureReady(player1, new ValakutFireboar());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, fireboar)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, fireboar)).isEqualTo(1);
    }

    @Test
    @DisplayName("The switch wears off at end of turn")
    void switchWearsOffAtEndOfTurn() {
        Permanent fireboar = addCreatureReady(player1, new ValakutFireboar());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, fireboar)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, fireboar)).isEqualTo(7);
    }

    @Test
    @DisplayName("The attack trigger switches only its source after resolving")
    void switchesOnlyAttackingSourceAfterResolution() {
        Permanent attacker = addCreatureReady(player1, new ValakutFireboar());
        Permanent other = addCreatureReady(player1, new ValakutFireboar());
        Permanent opponent = addCreatureReady(player2, new ValakutFireboar());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(7);

        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(7);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponent)).isEqualTo(7);
    }

    @Test
    @DisplayName("Attacking again in the same turn switches power and toughness back")
    void secondAttackInSameTurnSwitchesBack() {
        Permanent fireboar = addCreatureReady(player1, new ValakutFireboar());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, fireboar)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, fireboar)).isEqualTo(1);

        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        fireboar.untap();
        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, fireboar)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, fireboar)).isEqualTo(7);
    }

    @Test
    @DisplayName("The switch persists through the end step before cleanup")
    void switchPersistsThroughEndStep() {
        Permanent fireboar = addCreatureReady(player1, new ValakutFireboar());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gqs.getEffectivePower(gd, fireboar)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, fireboar)).isEqualTo(1);
    }
}
