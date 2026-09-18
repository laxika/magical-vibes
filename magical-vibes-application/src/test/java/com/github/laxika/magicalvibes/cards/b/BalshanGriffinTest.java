package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CarefulStudy;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BalshanGriffin.class, CarefulStudy.class})
class BalshanGriffinTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1}{U} and discarding a card returns Balshan Griffin to its owner's hand")
    void activateAbilityReturnsToHand() {
        harness.addToBattlefield(player1, new BalshanGriffin());
        harness.setHand(player1, List.of(new CarefulStudy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Balshan Griffin");
        harness.assertNotOnBattlefield(player1, "Balshan Griffin");
        harness.assertInGraveyard(player1, "Careful Study");
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        harness.addToBattlefield(player1, new BalshanGriffin());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without the generic mana in {1}{U}")
    void cannotActivateWithoutGenericMana() {
        harness.addToBattlefield(player1, new BalshanGriffin());
        harness.setHand(player1, List.of(new CarefulStudy()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Balshan Griffin");
        harness.assertInHand(player1, "Careful Study");
    }

    @Test
    @DisplayName("Cannot activate without the blue mana in {1}{U}")
    void cannotActivateWithoutBlueMana() {
        harness.addToBattlefield(player1, new BalshanGriffin());
        harness.setHand(player1, List.of(new CarefulStudy()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Balshan Griffin");
        harness.assertInHand(player1, "Careful Study");
    }
}
