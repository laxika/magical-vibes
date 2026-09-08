package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VedalkenMesmeristTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking lets you give an opponent's creature -2/-0 until end of turn")
    void debuffsTargetOpponentCreatureOnAttack() {
        addCreatureReady(player1, new VedalkenMesmerist());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        resolveAttackTrigger();

        assertThat(bears.getPowerModifier()).isEqualTo(-2);
        assertThat(bears.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The -2/-0 debuff wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        gd.playerAutoStopSteps.put(player1.getId(), Set.of(TurnStep.END_STEP));
        addCreatureReady(player1, new VedalkenMesmerist());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        resolveAttackTrigger();

        gs.declareBlockers(gd, player2, List.of());
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The attack trigger cannot target a creature you control")
    void cannotTargetOwnCreature() {
        addCreatureReady(player1, new VedalkenMesmerist());
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownBears.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, opponentBears.getId());
        resolveAttackTrigger();
    }

    @Test
    @DisplayName("The attack trigger is skipped when no opponent creature is available")
    void skippedWhenNoOpponentCreature() {
        addCreatureReady(player1, new VedalkenMesmerist());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void resolveAttackTrigger() {
        harness.passBothPriorities();
    }
}
