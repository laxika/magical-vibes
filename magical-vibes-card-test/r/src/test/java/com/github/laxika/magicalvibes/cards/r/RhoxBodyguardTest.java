package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RhoxBodyguard.class})
class RhoxBodyguardTest extends BaseCardTest {

    @Test
    @DisplayName("ETB: controller gains 3 life")
    void etbGainsThreeLife() {
        harness.setHand(player1, List.of(new RhoxBodyguard()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void exaltedBoostsItselfUntilEndOfTurn() {
        Permanent attacker = addCreatureReady(player1, new RhoxBodyguard());

        declareAttackers(player1, List.of(0));
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);
    }

    @Test
    void eachBodyguardBoostsAnotherLoneAttacker() {
        Permanent supporter = addCreatureReady(player1, new RhoxBodyguard());
        Permanent attacker = addCreatureReady(player1, new RhoxBodyguard());

        declareAttackers(player1, List.of(1));
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, supporter)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, supporter)).isEqualTo(3);
    }

    @Test
    void exaltedDoesNotTriggerForMultipleAttackers() {
        Permanent first = addCreatureReady(player1, new RhoxBodyguard());
        Permanent second = addCreatureReady(player1, new RhoxBodyguard());

        declareAttackers(player1, List.of(0, 1));

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
    }

    @Test
    void opponentsBodyguardDoesNotBoostLoneAttacker() {
        addCreatureReady(player1, new RhoxBodyguard());
        Permanent attacker = addCreatureReady(player2, new RhoxBodyguard());

        declareAttackers(player2, List.of(0));
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(4);
    }
}
