package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KaraisTechnique.class, GrizzlyBears.class, SerraAngel.class})
class KaraisTechniqueTest extends BaseCardTest {

    @Test
    @DisplayName("Boost mode gives target creature +3/+3 until end of turn")
    void boostModeGivesPlusThreePlusThree() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(new int[]{0}, List.of(target.getId()));

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
    }

    @Test
    @DisplayName("Debuff mode gives target creature -3/-3 until end of turn")
    void debuffModeGivesMinusThreeMinusThree() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        cast(new int[]{1}, List.of(target.getId()));

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("Both modes may target the same creature")
    void bothModesMayTargetTheSameCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(new int[]{0, 1}, List.of(target.getId(), target.getId()));

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Temporary changes wear off at end of turn")
    void changesWearOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(new int[]{0}, List.of(target.getId()));
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("A mode cannot target a player")
    void cannotTargetAPlayer() {
        harness.setHand(player1, List.of(new KaraisTechnique()));
        addNormalMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 2, new int[]{0}, List.of(player2.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sneak returns an unblocked attacker and resolves the chosen mode")
    void sneakReturnsAnUnblockedAttacker() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.setHand(player1, List.of(new KaraisTechnique()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        gs.playCard(gd, player1, 0,
                ChooseOneEffect.encodeModeSelection(1, 2, new int[]{0}),
                null, null, List.of(target.getId()), List.of(), false, null, null,
                List.of(attacker.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(7);
    }

    @Test
    @DisplayName("Both modes affect only their respective targets")
    void bothModesAffectDistinctTargets() {
        Permanent boosted = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent reduced = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        cast(new int[]{0, 1}, List.of(boosted.getId(), reduced.getId()));

        assertThat(gqs.getEffectivePower(gd, boosted)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, boosted)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, reduced)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, reduced)).isEqualTo(1);
    }

    @Test
    @DisplayName("The debuff puts a creature with zero or less toughness into the graveyard")
    void debuffKillsSmallCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(new int[]{1}, List.of(target.getId()));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The debuff wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        cast(new int[]{1}, List.of(target.getId()));
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    @DisplayName("Normal mana payment does not grant sneak timing")
    void cannotCastForNormalCostDuringDeclareBlockers() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.setHand(player1, List.of(new KaraisTechnique()));
        addNormalMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 2, new int[]{0}, List.of(target.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Karai's Technique");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A blocked attacker cannot pay the sneak cost")
    void cannotReturnBlockedAttackerForSneak() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        attacker.setBlockedThisCombat(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        target.setBlocking(true);
        target.addBlockingTargetId(attacker.getId());
        harness.setHand(player1, List.of(new KaraisTechnique()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0,
                ChooseOneEffect.encodeModeSelection(1, 2, new int[]{0}),
                null, null, List.of(target.getId()), List.of(), false, null, null,
                List.of(attacker.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Karai's Technique");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    private void cast(int[] modes, List<java.util.UUID> targets) {
        harness.setHand(player1, List.of(new KaraisTechnique()));
        addNormalMana();
        harness.castModalSorceryWithModes(player1, 0, 1, 2, modes, targets, List.of());
        harness.passBothPriorities();
    }

    private void addNormalMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
