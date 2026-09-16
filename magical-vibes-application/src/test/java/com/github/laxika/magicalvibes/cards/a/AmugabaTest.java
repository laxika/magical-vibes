package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(Amugaba.class)
class AmugabaTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card and paying mana returns Amugaba to its owner's hand")
    void returnsItselfToHandAfterAbilityResolves() {
        harness.addToBattlefield(player1, new Amugaba());
        harness.setHand(player1, List.of(new Amugaba()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Amugaba");
        harness.assertInGraveyard(player1, "Amugaba");
        harness.assertInHand(player1, "Amugaba");
    }

    @Test
    @DisplayName("Keeps Amugaba on the battlefield until its ability resolves")
    void returnsItselfOnlyWhenAbilityResolves() {
        harness.addToBattlefield(player1, new Amugaba());
        harness.setHand(player1, List.of(new Amugaba()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Amugaba");
        harness.assertInGraveyard(player1, "Amugaba");
    }

    @Test
    @DisplayName("Returns a controlled Amugaba to its owner's hand")
    void returnsItselfToOwnersHandWhenControllerDiffers() {
        Amugaba amugaba = new Amugaba();
        amugaba.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, amugaba);
        harness.setHand(player2, List.of(new Amugaba()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.activateAbility(player2, 0, null, null);
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Amugaba");
        harness.assertNotInHand(player2, "Amugaba");
        harness.assertNotOnBattlefield(player2, "Amugaba");
        harness.assertInGraveyard(player2, "Amugaba");
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        harness.addToBattlefield(player1, new Amugaba());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
