package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZodiacSnake;
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

@CardUsed({KasetoOrochiArchmage.class, GrizzlyBears.class, ZodiacSnake.class, Forest.class})
class KasetoOrochiArchmageTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature can't be blocked, but a non-Snake gets no boost")
    void makesNonSnakeUnblockableWithoutBoost() {
        Permanent kaseto = addCreatureReady(player1, new KasetoOrochiArchmage());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        activate(kaseto, target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);

        target.setAttacking(true);
        prepareDeclareBlockers(player1);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int targetIndex = gd.playerBattlefields.get(player1.getId()).indexOf(target);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, targetIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Target Snake gets +2/+2 and can't be blocked")
    void boostsSnakeAndMakesItUnblockable() {
        Permanent kaseto = addCreatureReady(player1, new KasetoOrochiArchmage());
        Permanent target = addCreatureReady(player1, new ZodiacSnake());

        activate(kaseto, target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Kaseto's temporary effects expire at end of turn")
    void effectsExpireAtEndOfTurn() {
        Permanent kaseto = addCreatureReady(player1, new KasetoOrochiArchmage());
        Permanent target = addCreatureReady(player1, new ZodiacSnake());

        activate(kaseto, target);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(target.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(target.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Kaseto can target creatures only")
    void cannotTargetLand() {
        Permanent kaseto = addCreatureReady(player1, new KasetoOrochiArchmage());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        int kasetoIndex = gd.playerBattlefields.get(player1.getId()).indexOf(kaseto);
        assertThatThrownBy(() -> harness.activateAbility(player1, kasetoIndex, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("A tapped, summoning-sick Kaseto can target itself repeatedly")
    void canBoostItselfRepeatedlyWhileTappedAndSummoningSick() {
        Permanent kaseto = harness.addToBattlefieldAndReturn(player1, new KasetoOrochiArchmage());
        kaseto.setSummoningSick(true);
        kaseto.tap();

        activate(kaseto, kaseto);
        activate(kaseto, kaseto);

        assertThat(gqs.getEffectivePower(gd, kaseto)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, kaseto)).isEqualTo(6);
        assertThat(kaseto.isCantBeBlocked()).isTrue();
        assertThat(kaseto.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Kaseto can make an opposing Snake unblockable and boost it")
    void canTargetOpposingSnake() {
        Permanent kaseto = addCreatureReady(player1, new KasetoOrochiArchmage());
        Permanent target = addCreatureReady(player2, new KasetoOrochiArchmage());

        activate(kaseto, target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(target.isCantBeBlocked()).isTrue();
        assertThat(gqs.getEffectivePower(gd, kaseto)).isEqualTo(2);
        assertThat(kaseto.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Kaseto's ability applies only when it resolves")
    void effectsWaitForResolution() {
        Permanent kaseto = addCreatureReady(player1, new KasetoOrochiArchmage());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        int kasetoIndex = gd.playerBattlefields.get(player1.getId()).indexOf(kaseto);

        harness.activateAbility(player1, kasetoIndex, 0, null, kaseto.getId());

        assertThat(gqs.getEffectivePower(gd, kaseto)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, kaseto)).isEqualTo(2);
        assertThat(kaseto.isCantBeBlocked()).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, kaseto)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, kaseto)).isEqualTo(4);
        assertThat(kaseto.isCantBeBlocked()).isTrue();
    }

    private void activate(Permanent kaseto, Permanent target) {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        int kasetoIndex = gd.playerBattlefields.get(player1.getId()).indexOf(kaseto);
        harness.activateAbility(player1, kasetoIndex, 0, null, target.getId());
        harness.passBothPriorities();
    }
}
