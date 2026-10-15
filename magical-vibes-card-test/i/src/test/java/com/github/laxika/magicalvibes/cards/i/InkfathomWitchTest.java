package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InkfathomWitch.class, GrizzlyBears.class})
class InkfathomWitchTest extends BaseCardTest {

    /** Adds the Witch at index 0 and gives player1 enough mana for {2}{U}{B}. */
    private void addWitchAndMana() {
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        addCreatureReady(player1, new InkfathomWitch());
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }

    @Test
    @DisplayName("Unblocked attacking creature gets base power/toughness 4/1")
    void unblockedAttackerBecomes41() {
        addWitchAndMana();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setAttacking(true);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
    }

    @Test
    @DisplayName("Affects any player's unblocked attacker, not just yours")
    void affectsOpponentUnblockedAttacker() {
        addWitchAndMana();
        Permanent oppBears = addCreatureReady(player2, new GrizzlyBears());
        oppBears.setAttacking(true);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, oppBears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, oppBears)).isEqualTo(1);
    }

    @Test
    @DisplayName("A blocked attacker is unaffected")
    void blockedAttackerUnaffected() {
        addWitchAndMana();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(attacker.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("A creature that isn't attacking is unaffected")
    void nonAttackingCreatureUnaffected() {
        addWitchAndMana();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("The 4/1 base wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        addWitchAndMana();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setAttacking(true);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Activating before blockers are declared does not affect attackers")
    void beforeBlockersDoesNothing() {
        addWitchAndMana();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("An attacker remains blocked after its blockers leave combat")
    void blockedWithoutRemainingBlockersUnaffected() {
        addWitchAndMana();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setBlockedWithoutBlockers(true);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("An unblocked Witch can affect itself without tapping")
    void affectsItself() {
        addWitchAndMana();
        Permanent witch = gd.playerBattlefields.get(player1.getId()).getFirst();
        witch.setAttacking(true);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, witch)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, witch)).isEqualTo(1);
        assertThat(witch.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Counters apply on top of the new base power and toughness")
    void countersStillApply() {
        addWitchAndMana();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Affected creatures keep the new base after leaving combat")
    void effectPersistsAfterCombat() {
        addWitchAndMana();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        attacker.setAttacking(false);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        Permanent laterCreature = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, laterCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, laterCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Fear rejects green blockers and permits black blockers")
    void fearRestrictsBlockers() {
        Permanent witch = addCreatureReady(player1, new InkfathomWitch());
        Permanent greenBlocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent blackBlocker = addCreatureReady(player2, new InkfathomWitch());

        assertThat(bls.canBlockAttacker(gd, greenBlocker, witch,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, blackBlocker, witch,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }
}
