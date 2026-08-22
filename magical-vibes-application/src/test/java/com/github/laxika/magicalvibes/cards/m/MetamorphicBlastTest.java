package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MetamorphicBlast.class, GrizzlyBears.class})
class MetamorphicBlastTest extends BaseCardTest {

    @Test
    @DisplayName("The first mode makes a creature a white 0/1 Rabbit until end of turn")
    void transformsTargetCreatureUntilEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(new int[]{0}, List.of(target.getId()), 2);

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.WHITE);
        assertThat(target.getTransientSubtypes()).containsExactly(CardSubtype.RABBIT);
        assertThat(gqs.getEffectivePower(gd, target)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, target)).doesNotContain(CardColor.WHITE);
        assertThat(target.getTransientSubtypes()).doesNotContain(CardSubtype.RABBIT);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("The second mode makes the target player draw two cards")
    void targetPlayerDrawsTwoCards() {
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        cast(new int[]{1}, List.of(player2.getId()), 4);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Both modes resolve and charge both additional costs")
    void bothModesResolve() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        cast(new int[]{0, 1}, List.of(target.getId(), player2.getId()), 5);

        assertThat(target.getTransientSubtypes()).containsExactly(CardSubtype.RABBIT);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("The creature mode rejects a player target")
    void creatureModeRejectsPlayerTarget() {
        assertThatThrownBy(() -> cast(new int[]{0}, List.of(player2.getId()), 2))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int[] modes, List<java.util.UUID> targets, int totalMana) {
        harness.setHand(player1, List.of(new MetamorphicBlast()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, totalMana - 1);
        harness.castModalInstantWithModes(player1, 0, 1, 2, modes, targets);
        harness.passBothPriorities();
    }
}
