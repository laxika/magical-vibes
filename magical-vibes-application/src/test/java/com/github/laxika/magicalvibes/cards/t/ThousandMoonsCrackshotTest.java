package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DiamondPickAxe;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThousandMoonsCrackshot.class, DiamondPickAxe.class})
class ThousandMoonsCrackshotTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking puts a nontargeted payment trigger on the stack")
    void attackQueuesPaymentWithoutTargetSelection() {
        addCreatureReady(player1, new ThousandMoonsCrackshot());
        addCreatureReady(player2, new ThousandMoonsCrackshot());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Paying {2}{W} creates a separate targeted trigger before tapping")
    void payingManaTapsTargetCreature() {
        addCreatureReady(player1, new ThousandMoonsCrackshot());
        Permanent target = addCreatureReady(player2, new ThousandMoonsCrackshot());
        addPaymentMana();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Lacking the payment leaves the creature untapped without choosing a target")
    void lackingManaLeavesTargetUntapped() {
        addCreatureReady(player1, new ThousandMoonsCrackshot());
        Permanent target = addCreatureReady(player2, new ThousandMoonsCrackshot());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("The reflexive trigger rejects noncreature targets after payment")
    void reflexiveTriggerRejectsNoncreatureTargets() {
        addCreatureReady(player1, new ThousandMoonsCrackshot());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new DiamondPickAxe());
        addCreatureReady(player2, new ThousandMoonsCrackshot());
        addPaymentMana();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, equipment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    @DisplayName("Declining an affordable payment creates no targeted trigger")
    void decliningPaymentLeavesCreatureUntapped() {
        addCreatureReady(player1, new ThousandMoonsCrackshot());
        Permanent target = addCreatureReady(player2, new ThousandMoonsCrackshot());
        addPaymentMana();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("The paid reflexive trigger can target a creature you control")
    void canTapFriendlyCreature() {
        addCreatureReady(player1, new ThousandMoonsCrackshot());
        Permanent target = addCreatureReady(player1, new ThousandMoonsCrackshot());
        addCreatureReady(player2, new ThousandMoonsCrackshot());
        addPaymentMana();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        assertThat(target.isTapped()).isFalse();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    private void addPaymentMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
