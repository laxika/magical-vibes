package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(StarlightInvoker.class)
class StarlightInvokerTest extends BaseCardTest {

    // ===== Activation =====

    @Test
    @DisplayName("Activating ability puts it on the stack")
    void activatingPutsOnStack() {
        addCreatureReady(player1, new StarlightInvoker());
        harness.addMana(player1, ManaColor.WHITE, 8);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Activating ability does not tap Starlight Invoker")
    void activatingDoesNotTap() {
        Permanent invoker = addCreatureReady(player1, new StarlightInvoker());
        harness.addMana(player1, ManaColor.WHITE, 8);

        harness.activateAbility(player1, 0, null, null);

        assertThat(invoker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumedWhenActivating() {
        addCreatureReady(player1, new StarlightInvoker());
        harness.addMana(player1, ManaColor.WHITE, 10);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    // ===== Resolution =====

    @Test
    @DisplayName("Resolving ability gains 5 life")
    void resolvingGainsFiveLife() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new StarlightInvoker());
        harness.addMana(player1, ManaColor.WHITE, 8);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(25);
    }

    @Test
    @DisplayName("Only the controller gains life")
    void onlyControllerGainsLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new StarlightInvoker());
        harness.addMana(player1, ManaColor.WHITE, 8);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 25);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Can activate multiple times to gain more life")
    void canActivateMultipleTimes() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new StarlightInvoker());
        harness.addMana(player1, ManaColor.WHITE, 16);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(30);
    }

    @Test
    @DisplayName("Resolving ability logs the life gain")
    void resolvingLogsLifeGain() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new StarlightInvoker());
        harness.addMana(player1, ManaColor.WHITE, 8);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("gains") && log.contains("5") && log.contains("life"));
    }

    @Test
    @DisplayName("Can activate the ability while summoning sick because it has no tap cost")
    void canActivateWhileSummoningSick() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new StarlightInvoker());
        harness.addMana(player1, ManaColor.WHITE, 8);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 25);
    }

    // ===== Validation =====

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new StarlightInvoker());
        harness.addMana(player1, ManaColor.WHITE, 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Generic portion of the activation cost can be paid with other mana")
    void canPayGenericCostWithOtherMana() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new StarlightInvoker());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 25);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Eight mana without white mana cannot pay the activation cost")
    void cannotActivateWithoutWhiteMana() {
        addCreatureReady(player1, new StarlightInvoker());
        harness.addMana(player1, ManaColor.BLUE, 8);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(8);
    }

    @Test
    @DisplayName("A tapped Invoker can activate its ability")
    void canActivateWhileTapped() {
        harness.setLife(player1, 20);
        Permanent invoker = addCreatureReady(player1, new StarlightInvoker());
        invoker.tap();
        harness.addMana(player1, ManaColor.WHITE, 8);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 25);
        assertThat(invoker.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Multiple activations can be stacked and life is gained only on resolution")
    void canStackMultipleActivations() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new StarlightInvoker());
        harness.addMana(player1, ManaColor.WHITE, 16);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(2);
        harness.assertLife(player1, 20);

        harness.passBothPriorities();
        harness.assertLife(player1, 25);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.assertLife(player1, 30);
        assertThat(gd.stack).isEmpty();
    }

}

