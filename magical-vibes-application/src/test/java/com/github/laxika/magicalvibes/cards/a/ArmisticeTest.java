package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArmisticeTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card and gives target opponent 3 life")
    void drawsAndGivesOpponentLife() {
        harness.addToBattlefield(player1, new Armistice());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player2, 10);
        addActivationMana();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertLife(player2, 13);
    }

    @Test
    @DisplayName("Cannot target its controller")
    void cannotTargetController() {
        harness.addToBattlefield(player1, new Armistice());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutEnoughMana() {
        harness.addToBattlefield(player1, new Armistice());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 2);
    }
}
