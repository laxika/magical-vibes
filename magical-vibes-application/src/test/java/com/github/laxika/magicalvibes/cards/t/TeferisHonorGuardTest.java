package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.p.Python;
import com.github.laxika.magicalvibes.cards.s.SuqAtaLancer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TeferisHonorGuard.class, Python.class, SuqAtaLancer.class})
class TeferisHonorGuardTest extends BaseCardTest {

    @Test
    @DisplayName("Flanking gives a blocker without flanking -1/-1 until end of turn")
    void flankingHitsNonFlankingBlocker() {
        Permanent guard = addCreatureReady(player1, new TeferisHonorGuard());
        guard.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new Python());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isEqualTo(2);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Flanking does not weaken a blocker that has flanking")
    void flankingDoesNotAffectFlankingBlocker() {
        Permanent guard = addCreatureReady(player1, new TeferisHonorGuard());
        guard.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new SuqAtaLancer());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(blocker.getEffectivePower()).isEqualTo(2);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Flanking weakens each non-flanking blocker")
    void flankingHitsEachNonFlankingBlocker() {
        Permanent guard = addCreatureReady(player1, new TeferisHonorGuard());
        guard.setAttacking(true);
        Permanent blocker1 = addCreatureReady(player2, new Python());
        Permanent blocker2 = addCreatureReady(player2, new Python());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(blocker1.getEffectivePower()).isEqualTo(2);
        assertThat(blocker1.getEffectiveToughness()).isEqualTo(1);
        assertThat(blocker2.getEffectivePower()).isEqualTo(2);
        assertThat(blocker2.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("The flanking penalty wears off at end of turn")
    void flankingPenaltyWearsOffAtEndOfTurn() {
        Permanent guard = addCreatureReady(player1, new TeferisHonorGuard());
        guard.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new Python());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isEqualTo(2);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isEqualTo(3);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The {U}{U} ability phases out a tapped Teferi's Honor Guard")
    void phasesOut() {
        Permanent guard = addCreatureReady(player1, new TeferisHonorGuard());
        guard.tap();
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(guard);
        assertThat(gd.phasedOutPermanents.getOrDefault(player1.getId(), List.of())).doesNotContain(guard);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(guard);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(guard);
    }

    @Test
    @DisplayName("The phase-out ability requires two blue mana")
    void phasesOutRequiresTwoBlueMana() {
        Permanent guard = addCreatureReady(player1, new TeferisHonorGuard());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(guard);
        assertThat(gd.phasedOutPermanents.getOrDefault(player1.getId(), List.of())).doesNotContain(guard);
    }

    @Test
    @DisplayName("Phasing out removes Teferi's Honor Guard from combat")
    void phasesOutRemovesItFromCombat() {
        Permanent guard = addCreatureReady(player1, new TeferisHonorGuard());
        guard.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(guard);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(guard);
        assertThat(guard.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Teferi's Honor Guard phases back in during its controller's next untap step")
    void phasesBackIn() {
        Permanent guard = addCreatureReady(player1, new TeferisHonorGuard());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(guard);

        harness.setHand(player2, List.of());
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(guard);

        harness.passUntil(player1, TurnStep.UPKEEP);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(guard);
    }
}
