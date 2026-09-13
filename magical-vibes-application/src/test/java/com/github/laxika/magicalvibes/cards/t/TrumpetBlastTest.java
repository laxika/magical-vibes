package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GoblinBerserker;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TrumpetBlast.class, GoblinBerserker.class})
class TrumpetBlastTest extends BaseCardTest {

    @Test
    @DisplayName("Trumpet Blast boosts attacking creatures with +2/+0")
    void boostsAttackingCreatures() {
        Permanent attacker = addCreatureReady(player1, new GoblinBerserker());
        attacker.setAttacking(true);
        Permanent nonAttacker = addCreatureReady(player1, new GoblinBerserker());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castFromHand(player1, new TrumpetBlast(), "{2}{R}");
        harness.passBothPriorities();

        // Attacking creature gets +2/+0
        assertThat(attacker.getEffectivePower()).isEqualTo(4);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(2);

        // Non-attacking creature is unaffected
        assertThat(nonAttacker.getEffectivePower()).isEqualTo(2);
        assertThat(nonAttacker.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Trumpet Blast effects wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent attacker = addCreatureReady(player1, new GoblinBerserker());
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castFromHand(player1, new TrumpetBlast(), "{2}{R}");
        harness.passBothPriorities();

        assertThat(attacker.getEffectivePower()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.getEffectivePower()).isEqualTo(2);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Trumpet Blast boosts an opponent's attacking creature")
    void boostsOpponentsAttackingCreature() {
        Permanent opponentAttacker = addCreatureReady(player2, new GoblinBerserker());
        opponentAttacker.setAttacking(true);
        opponentAttacker.setAttackTarget(player1.getId());
        Permanent opponentNonAttacker = addCreatureReady(player2, new GoblinBerserker());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castFromHand(player1, new TrumpetBlast(), "{2}{R}");
        harness.passBothPriorities();

        assertThat(opponentAttacker.getEffectivePower()).isEqualTo(4);
        assertThat(opponentAttacker.getEffectiveToughness()).isEqualTo(2);
        assertThat(opponentNonAttacker.getEffectivePower()).isEqualTo(2);
        assertThat(opponentNonAttacker.getEffectiveToughness()).isEqualTo(2);
    }
}
