package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.DivingGriffin;
import com.github.laxika.magicalvibes.cards.r.RhysticCave;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WildMight.class, DivingGriffin.class, RhysticCave.class})
class WildMightTest extends BaseCardTest {

    @Test
    void addsBothPowerAndToughnessBonusesWhenNoPlayerPays() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DivingGriffin());

        castWildMight(creature);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(7);
    }

    @Test
    void anyPlayerCanPayToPreventAdditionalBonus() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DivingGriffin());
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        castWildMight(creature);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    void controllerCanPayToPreventAdditionalBonus() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DivingGriffin());

        castWildMight(creature);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void bonusesWearOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DivingGriffin());

        castWildMight(creature);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void canBoostAnOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DivingGriffin());

        castWildMight(creature);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(7);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void insufficientManaDoesNotPreventAdditionalBonusOrConsumeMana() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DivingGriffin());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        castWildMight(creature);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(7);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        Permanent nonCreature = harness.addToBattlefieldAndReturn(player1, new RhysticCave());
        harness.setHand(player1, List.of(new WildMight()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("After one player pays, the other is still asked, and the additional +4/+4 is prevented")
    void otherPlayerStillAskedAfterAPayment() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DivingGriffin());

        castWildMight(creature);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(2);
    }

    private void castWildMight(Permanent target) {
        giveManaSource(player1);
        giveManaSource(player2);
        harness.setHand(player1, List.of(new WildMight()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
