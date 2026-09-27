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
        Permanent land = new Permanent(new Forest());
        gd.playerBattlefields.get(player2.getId()).add(land);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        int kasetoIndex = gd.playerBattlefields.get(player1.getId()).indexOf(kaseto);
        assertThatThrownBy(() -> harness.activateAbility(player1, kasetoIndex, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void activate(Permanent kaseto, Permanent target) {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        int kasetoIndex = gd.playerBattlefields.get(player1.getId()).indexOf(kaseto);
        harness.activateAbility(player1, kasetoIndex, 0, null, target.getId());
        harness.passBothPriorities();
    }
}
