package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImpelledGiantTest extends BaseCardTest {

    private int index(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    @Test
    @DisplayName("Taps a red creature to get +X/+0 equal to that creature's power")
    void tapsRedCreatureForPowerBoost() {
        Permanent giant = addCreatureReady(player1, new ImpelledGiant());
        Permanent hillGiant = addCreatureReady(player1, new HillGiant()); // red 3/3, only valid tap

        harness.activateAbility(player1, index(giant), null, null);
        harness.passBothPriorities();

        assertThat(hillGiant.isTapped()).isTrue();
        assertThat(giant.isTapped()).isFalse();
        assertThat(giant.getEffectivePower()).isEqualTo(6); // 3 base + 3 tapped power
        assertThat(giant.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost equals the chosen creature's power when multiple red creatures are available")
    void boostMatchesChosenCreaturePower() {
        Permanent giant = addCreatureReady(player1, new ImpelledGiant());
        Permanent hillGiant = addCreatureReady(player1, new HillGiant());   // red 3/3
        Permanent goblin = addCreatureReady(player1, new RagingGoblin());   // red 1/1

        harness.activateAbility(player1, index(giant), null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, goblin.getId());
        harness.passBothPriorities();

        assertThat(goblin.isTapped()).isTrue();
        assertThat(hillGiant.isTapped()).isFalse();
        assertThat(giant.getEffectivePower()).isEqualTo(4); // 3 base + 1 tapped power
        assertThat(giant.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent giant = addCreatureReady(player1, new ImpelledGiant());
        addCreatureReady(player1, new HillGiant());

        harness.activateAbility(player1, index(giant), null, null);
        harness.passBothPriorities();
        assertThat(giant.getEffectivePower()).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(giant.getEffectivePower()).isEqualTo(3);
        assertThat(giant.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot tap itself or a non-red creature")
    void cannotTapSelfOrNonRed() {
        Permanent giant = addCreatureReady(player1, new ImpelledGiant());
        addCreatureReady(player1, new GrizzlyBears()); // green, not a legal tap; self is excluded

        assertThatThrownBy(() -> harness.activateAbility(player1, index(giant), null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
