package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.p.Pikemen;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Morale.class, Pikemen.class})
class MoraleTest extends BaseCardTest {

    @Test
    @DisplayName("Morale boosts attacking creatures with +1/+1")
    void boostsAttackingCreatures() {
        Permanent attacker = addCreatureReady(player1, new Pikemen());
        Permanent nonAttacker = addCreatureReady(player1, new Pikemen());

        declareAttackers(List.of(0));

        harness.castFromHand(player1, new Morale(), "{1}{W}{W}");
        harness.passBothPriorities();

        // Attacking creature gets +1/+1
        assertThat(attacker.getEffectivePower()).isEqualTo(2);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(2);

        // Non-attacking creature is unaffected
        assertThat(nonAttacker.getEffectivePower()).isEqualTo(1);
        assertThat(nonAttacker.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    void boostsOpponentsAttackingCreatures() {
        Permanent opponentAttacker = addCreatureReady(player2, new Pikemen());

        declareAttackers(player2, List.of(0));

        harness.castFromHand(player1, new Morale(), "{1}{W}{W}");
        harness.passBothPriorities();

        assertThat(opponentAttacker.getEffectivePower()).isEqualTo(2);
        assertThat(opponentAttacker.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Morale effects wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent attacker = addCreatureReady(player1, new Pikemen());

        declareAttackers(List.of(0));

        harness.castFromHand(player1, new Morale(), "{1}{W}{W}");
        harness.passBothPriorities();

        assertThat(attacker.getEffectivePower()).isEqualTo(2);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.getEffectivePower()).isEqualTo(1);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(1);
    }
}
