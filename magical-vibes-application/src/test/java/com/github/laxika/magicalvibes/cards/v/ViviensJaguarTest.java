package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.j.JaceTheLivingGuildpact;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ViviensJaguarTest extends BaseCardTest {

    @Test
    @DisplayName("Returns from the graveyard to its owner's hand while controlling Vivien")
    void returnsToHandWithVivienPlaneswalker() {
        ViviensJaguar jaguar = new ViviensJaguar();
        harness.setGraveyard(player1, List.of(jaguar));
        harness.addToBattlefield(player1, new VivienReid());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Vivien's Jaguar");
        harness.assertNotInGraveyard(player1, "Vivien's Jaguar");
    }

    @Test
    @DisplayName("Cannot activate without controlling a Vivien planeswalker")
    void cannotActivateWithoutVivienPlaneswalker() {
        harness.setGraveyard(player1, List.of(new ViviensJaguar()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("control a Vivien planeswalker");
    }

    @Test
    @DisplayName("A non-Vivien planeswalker does not satisfy the activation condition")
    void cannotActivateWithOtherPlaneswalker() {
        harness.setGraveyard(player1, List.of(new ViviensJaguar()));
        harness.addToBattlefield(player1, new JaceTheLivingGuildpact());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("control a Vivien planeswalker");
    }
}
