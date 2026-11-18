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
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);

        // Non-attacking creature is unaffected
        assertThat(gqs.getEffectivePower(gd, nonAttacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nonAttacker)).isEqualTo(2);
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

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
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

        assertThat(gqs.getEffectivePower(gd, opponentAttacker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opponentAttacker)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentNonAttacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentNonAttacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Trumpet Blast cast before combat does not boost later attackers")
    void doesNotBoostCreaturesThatAttackLater() {
        Permanent creature = addCreatureReady(player1, new GoblinBerserker());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new TrumpetBlast(), "{2}{R}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(java.util.List.of(0)));

        assertThat(creature.isAttacking()).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Trumpet Blast boost remains after combat ends")
    void boostRemainsAfterCombat() {
        Permanent attacker = addCreatureReady(player1, new GoblinBerserker());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castFromHand(player1, new TrumpetBlast(), "{2}{R}");
        harness.passBothPriorities();
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(attacker.isAttacking()).isFalse();
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
    }
}
