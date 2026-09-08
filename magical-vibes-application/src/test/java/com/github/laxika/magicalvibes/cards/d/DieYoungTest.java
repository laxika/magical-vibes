package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FireElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DieYoungTest extends BaseCardTest {

    @Test
    @DisplayName("Gets two energy, then pays chosen energy to give target creature -1/-1 per energy")
    void paysEnergyForTargetCreatureShrink() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FireElemental());
        cast(target);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        PendingInteraction.XValueChoice choice = (PendingInteraction.XValueChoice)
                gd.interaction.activeInteraction();
        assertThat(choice.maxValue()).isEqualTo(2);
        assertThat(choice.manaPayment()).isFalse();

        harness.handleXValueChosen(player1, 2);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Paying zero energy leaves the target unchanged")
    void mayPayZeroEnergy() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FireElemental());
        cast(target);

        harness.handleXValueChosen(player1, 0);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("The shrink wears off at the end of the turn")
    void shrinkWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FireElemental());
        cast(target);
        harness.handleXValueChosen(player1, 1);

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent invalidTarget = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new DieYoung()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, invalidTarget.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Fizzles without giving energy when the target leaves before resolution")
    void fizzlesWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FireElemental());
        harness.setHand(player1, List.of(new DieYoung()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new DieYoung()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
