package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BlackOakOfOdunosTest extends BaseCardTest {

    @Test
    @DisplayName("Pays by tapping another creature and gets +1/+1 until end of turn")
    void tapsAnotherCreatureToBoostSelf() {
        Permanent oak = addCreatureReady(player1, new BlackOakOfOdunos());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);

        int oakIndex = gd.playerBattlefields.get(player1.getId()).indexOf(oak);
        harness.activateAbility(player1, oakIndex, null, null);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(oak.isTapped()).isFalse();
        assertThat(oak.getEffectivePower()).isEqualTo(1);
        assertThat(oak.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("Cannot activate without another untapped creature")
    void cannotActivateWithoutAnotherUntappedCreature() {
        Permanent oak = addCreatureReady(player1, new BlackOakOfOdunos());
        harness.addMana(player1, ManaColor.BLACK, 1);

        int oakIndex = gd.playerBattlefields.get(player1.getId()).indexOf(oak);
        assertThatThrownBy(() -> harness.activateAbility(player1, oakIndex, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The boost wears off at cleanup")
    void boostWearsOffAtCleanup() {
        Permanent oak = addCreatureReady(player1, new BlackOakOfOdunos());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);

        int oakIndex = gd.playerBattlefields.get(player1.getId()).indexOf(oak);
        harness.activateAbility(player1, oakIndex, null, null);
        harness.passBothPriorities();
        assertThat(oak.getEffectivePower()).isEqualTo(1);
        assertThat(oak.getEffectiveToughness()).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(oak.getEffectivePower()).isEqualTo(0);
        assertThat(oak.getEffectiveToughness()).isEqualTo(5);
    }
}
