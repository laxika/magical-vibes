package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DeadlyInsect;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Armistice.class, DeadlyInsect.class})
class ArmisticeTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card and gives target opponent 3 life")
    void drawsAndGivesOpponentLife() {
        harness.addToBattlefield(player1, new Armistice());
        harness.setLibrary(player1, List.of(new DeadlyInsect()));
        harness.setLife(player2, 10);
        addActivationMana();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Deadly Insect");
        harness.assertLife(player2, 13);
    }

    @Test
    @DisplayName("Can be activated repeatedly without tapping")
    void canBeActivatedRepeatedlyWithoutTapping() {
        harness.addToBattlefield(player1, new Armistice());
        harness.setLibrary(player1, List.of(new DeadlyInsect(), new DeadlyInsect()));
        harness.setLife(player2, 10);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        addActivationMana();
        addActivationMana();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        harness.assertLife(player2, 16);
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

    @Test
    @DisplayName("Draw and life gain wait until the ability resolves")
    void effectsWaitForResolution() {
        harness.addToBattlefield(player1, new Armistice());
        harness.setLibrary(player1, List.of(new DeadlyInsect()));
        harness.setLife(player2, 10);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        addActivationMana();

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        harness.assertLife(player2, 10);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertLife(player2, 13);
    }

    @Test
    @DisplayName("The other controller draws while their opponent gains life")
    void otherControllerDraws() {
        harness.addToBattlefield(player2, new Armistice());
        harness.setLibrary(player2, List.of(new DeadlyInsect()));
        harness.setLife(player1, 10);
        harness.setLife(player2, 15);
        int opponentHandBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Deadly Insect");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(opponentHandBefore);
        harness.assertLife(player1, 13);
        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Cannot activate without an opponent target")
    void cannotActivateWithoutTarget() {
        harness.addToBattlefield(player1, new Armistice());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Generic mana cannot replace the second white mana")
    void requiresTwoWhiteMana() {
        harness.addToBattlefield(player1, new Armistice());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 2);
    }
}
