package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrazenWolves.class})
class BrazenWolvesTest extends BaseCardTest {

    @Test
    @DisplayName("The attack boost waits for the trigger to resolve and affects only the attacker")
    void boostIsTriggeredAndOnlyAffectsItsSource() {
        Permanent attacker = addCreatureReady(player1, new BrazenWolves());
        Permanent nonattacker = addCreatureReady(player1, new BrazenWolves());
        Permanent opponent = addCreatureReady(player2, new BrazenWolves());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);

        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, nonattacker)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(2);
    }

    @Test
    @DisplayName("Each attacking Brazen Wolves receives its own boost")
    void multipleAttackersEachGetOneBoost() {
        Permanent first = addCreatureReady(player1, new BrazenWolves());
        Permanent second = addCreatureReady(player1, new BrazenWolves());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
    }

    @Test
    @DisplayName("Attacking gives Brazen Wolves +2/+0 until end of turn")
    void attackTriggerBoostsPower() {
        Permanent wolves = addCreatureReady(player1, new BrazenWolves());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(wolves.getPowerModifier()).isEqualTo(2);
        assertThat(wolves.getToughnessModifier()).isZero();
        assertThat(gqs.getEffectivePower(gd, wolves)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, wolves)).isEqualTo(3);
    }

    @Test
    @DisplayName("The attack boost wears off at end of turn")
    void boostWearsOff() {
        Permanent wolves = addCreatureReady(player1, new BrazenWolves());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        assertThat(wolves.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(wolves.getPowerModifier()).isZero();
        assertThat(wolves.getToughnessModifier()).isZero();
    }
}
