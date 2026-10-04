package com.github.laxika.magicalvibes.cards.f;

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

@CardUsed(FlowstoneKavu.class)
class FlowstoneKavuTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {R} gives Flowstone Kavu +1/-1 until end of turn")
    void activatedAbilityBoostsSelf() {
        Permanent kavu = addCreatureReady(player1, new FlowstoneKavu());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(kavu.getEffectivePower()).isEqualTo(3);
        assertThat(kavu.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The ability can be activated repeatedly")
    void repeatedActivationsStack() {
        Permanent kavu = addCreatureReady(player1, new FlowstoneKavu());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(kavu.getEffectivePower()).isEqualTo(4);
        assertThat(kavu.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent kavu = addCreatureReady(player1, new FlowstoneKavu());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.passUntil(TurnStep.UPKEEP);

        assertThat(kavu.getEffectivePower()).isEqualTo(2);
        assertThat(kavu.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("The ability does not require tapping")
    void abilityDoesNotRequireTapping() {
        Permanent kavu = addCreatureReady(player1, new FlowstoneKavu());
        kavu.tap();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(kavu.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability requires red mana")
    void cannotActivateWithoutRedMana() {
        addCreatureReady(player1, new FlowstoneKavu());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Three activations reduce toughness to zero and put the Kavu into the graveyard")
    void threeActivationsCauseStateBasedDeath() {
        addCreatureReady(player1, new FlowstoneKavu());
        harness.addMana(player1, ManaColor.RED, 3);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        harness.assertNotOnBattlefield(player1, "Flowstone Kavu");
        harness.assertInGraveyard(player1, "Flowstone Kavu");
    }

    @Test
    @DisplayName("A summoning-sick Kavu can activate its ability and boosts only itself")
    void summoningSicknessDoesNotPreventActivation() {
        Permanent kavu = addCreatureReady(player1, new FlowstoneKavu());
        kavu.setSummoningSick(true);
        Permanent otherKavu = addCreatureReady(player1, new FlowstoneKavu());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(kavu.getEffectivePower()).isEqualTo(3);
        assertThat(kavu.getEffectiveToughness()).isEqualTo(2);
        assertThat(otherKavu.getEffectivePower()).isEqualTo(2);
        assertThat(otherKavu.getEffectiveToughness()).isEqualTo(3);
        assertThat(kavu.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Menace prevents a single creature from blocking Flowstone Kavu")
    void menaceRejectsSingleBlocker() {
        addCreatureReady(player1, new FlowstoneKavu());
        addCreatureReady(player2, new FlowstoneKavu());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    @DisplayName("Two creatures can block Flowstone Kavu")
    void menaceAllowsTwoBlockers() {
        addCreatureReady(player1, new FlowstoneKavu());
        Permanent firstBlocker = addCreatureReady(player2, new FlowstoneKavu());
        Permanent secondBlocker = addCreatureReady(player2, new FlowstoneKavu());

        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(
                        new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }
}
