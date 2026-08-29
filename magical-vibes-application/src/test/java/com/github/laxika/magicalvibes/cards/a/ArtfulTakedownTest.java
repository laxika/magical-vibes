package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArtfulTakedownTest extends BaseCardTest {

    @Test
    @DisplayName("Tap mode taps the target creature")
    void tapModeTapsTargetCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        cast(new int[]{0}, List.of(creature.getId()));

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Debuff mode gives the target -2/-4 until end of turn")
    void debuffModeReducesPowerAndToughness() {
        GrizzlyBears card = new GrizzlyBears();
        card.setPower(4);
        card.setToughness(6);
        Permanent creature = addCreatureReady(player2, card);

        cast(new int[]{1}, List.of(creature.getId()));

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
    }

    @Test
    @DisplayName("Both modes resolve on the same target creature")
    void bothModesResolveOnSameTarget() {
        GrizzlyBears card = new GrizzlyBears();
        card.setPower(4);
        card.setToughness(6);
        Permanent creature = addCreatureReady(player2, card);

        cast(new int[]{0, 1}, List.of(creature.getId(), creature.getId()));

        assertThat(creature.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Both modes require creature targets")
    void modesRejectNonCreatureTarget() {
        assertThatThrownBy(() -> {
            harness.setHand(player1, List.of(new ArtfulTakedown()));
            addMana();
            harness.castModalInstantWithModes(player1, 0, 1, 2,
                    new int[]{0}, List.of(player2.getId()));
        }).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot target players");
    }

    private void cast(int[] modes, List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new ArtfulTakedown()));
        addMana();
        harness.castModalInstantWithModes(player1, 0, 1, 2, modes, targetIds);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
