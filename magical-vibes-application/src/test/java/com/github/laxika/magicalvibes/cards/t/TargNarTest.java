package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TargNar.class, GrizzlyBears.class})
class TargNarTest extends BaseCardTest {

    @Test
    @DisplayName("Pack tactics boosts all attacking creatures when their total power is at least six")
    void packTacticsBoostsAttackingCreaturesAtThreshold() {
        Permanent targNar = addCreatureReady(player1, new TargNar());
        Permanent attackingBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent attackingBear2 = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonAttackingBear = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, targNar)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, attackingBear)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, attackingBear2)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, nonAttackingBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, targNar)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attackingBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attackingBear2)).isEqualTo(2);
    }

    @Test
    @DisplayName("Pack tactics does not trigger below total attacking power six")
    void packTacticsRequiresSixAttackingPower() {
        Permanent targNar = addCreatureReady(player1, new TargNar());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, targNar)).isEqualTo(2);
    }

    @Test
    @DisplayName("Pack tactics requires Targ Nar to attack")
    void packTacticsRequiresTargNarToAttack() {
        Permanent targNar = addCreatureReady(player1, new TargNar());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1, 2, 3));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, targNar)).isEqualTo(2);
    }

    @Test
    @DisplayName("Activating Targ Nar's ability doubles its power and toughness until end of turn")
    void activatedAbilityDoublesPowerAndToughnessUntilEndOfTurn() {
        Permanent targNar = addCreatureReady(player1, new TargNar());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, targNar)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, targNar)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, targNar)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, targNar)).isEqualTo(2);
    }
}
