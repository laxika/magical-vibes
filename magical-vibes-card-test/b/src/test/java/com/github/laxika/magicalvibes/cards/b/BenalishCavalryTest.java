package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DurkwoodBaloth;
import com.github.laxika.magicalvibes.cards.s.SageOfEpityr;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BenalishCavalry.class, DurkwoodBaloth.class, SageOfEpityr.class, BalduvianWarlord.class})
class BenalishCavalryTest extends BaseCardTest {

    @Test
    @DisplayName("Flanking gives a blocker without flanking -1/-1 until end of turn")
    void blockerWithoutFlankingGetsMinusOneMinusOne() {
        Permanent cavalry = addCreatureReady(player1, new BenalishCavalry());
        cavalry.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new DurkwoodBaloth());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isEqualTo(4);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("A blocker that also has flanking is unaffected")
    void blockerWithFlankingIsUnaffected() {
        Permanent cavalry = addCreatureReady(player1, new BenalishCavalry());
        cavalry.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BenalishCavalry());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
        assertThat(blocker.getEffectivePower()).isEqualTo(2);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Flanking weakens each non-flanking blocker")
    void eachNonFlankingBlockerGetsMinusOneMinusOne() {
        Permanent cavalry = addCreatureReady(player1, new BenalishCavalry());
        cavalry.setAttacking(true);
        Permanent blocker1 = addCreatureReady(player2, new DurkwoodBaloth());
        Permanent blocker2 = addCreatureReady(player2, new DurkwoodBaloth());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(blocker1.getEffectivePower()).isEqualTo(4);
        assertThat(blocker1.getEffectiveToughness()).isEqualTo(4);
        assertThat(blocker2.getEffectivePower()).isEqualTo(4);
        assertThat(blocker2.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Flanking's penalty wears off at end of turn")
    void flankingPenaltyWearsOffAtEndOfTurn() {
        Permanent cavalry = addCreatureReady(player1, new BenalishCavalry());
        cavalry.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new DurkwoodBaloth());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(blocker.getEffectivePower()).isEqualTo(4);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isEqualTo(5);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("An unblocked creature with flanking creates no trigger")
    void unblockedCreatesNoTrigger() {
        Permanent cavalry = addCreatureReady(player1, new BenalishCavalry());
        cavalry.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Flanking does not trigger when Benalish Cavalry blocks")
    void flankingDoesNotTriggerWhileBlocking() {
        Permanent attacker = addCreatureReady(player1, new DurkwoodBaloth());
        attacker.setAttacking(true);
        Permanent cavalry = addCreatureReady(player2, new BenalishCavalry());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
        assertThat(attacker.getEffectivePower()).isEqualTo(5);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(5);
        assertThat(cavalry.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Flanking kills a one-toughness blocker before combat damage")
    void flankingKillsBlockerBeforeCombatDamage() {
        Permanent cavalry = addCreatureReady(player1, new BenalishCavalry());
        cavalry.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new SageOfEpityr());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(blocker.getCard());
        assertThat(cavalry.getMarkedDamage()).isZero();

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(cavalry);
        assertThat(cavalry.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Flanking triggers for a new blocker even when Cavalry is already blocked")
    void flankingTriggersForAdditionalBlockCreatedByEffect() {
        Permanent cavalry = addCreatureReady(player1, new BenalishCavalry());
        addCreatureReady(player1, new DurkwoodBaloth());
        addCreatureReady(player2, new BenalishCavalry());
        Permanent reassignedBlocker = addCreatureReady(player2, new DurkwoodBaloth());
        addCreatureReady(player2, new BalduvianWarlord());

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 1)));
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 2, null, reassignedBlocker.getId());
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, harness::passBothPriorities);
        harness.handlePermanentChosen(player2, cavalry.getId());
        resolveAllTriggers();

        assertThat(reassignedBlocker.getBlockingTargetIds()).containsExactly(cavalry.getId());
        assertThat(reassignedBlocker.getEffectivePower()).isEqualTo(4);
        assertThat(reassignedBlocker.getEffectiveToughness()).isEqualTo(4);
    }
}
