package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OmniCheesePizza.class, Forest.class})
class OmniCheesePizzaTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield draws a card")
    void enteringTheBattlefieldDrawsACard() {
        harness.setHand(player1, List.of(new OmniCheesePizza()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Sacrificing it adds one mana of the chosen color")
    void sacrificingItAddsManaOfChosenColor() {
        harness.addToBattlefield(player1, new OmniCheesePizza());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Omni-Cheese Pizza");
        harness.assertInGraveyard(player1, "Omni-Cheese Pizza");
    }

    @Test
    @DisplayName("Sacrificing it gains 3 life")
    void sacrificingItGainsThreeLife() {
        harness.addToBattlefield(player1, new OmniCheesePizza());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
        harness.assertInGraveyard(player1, "Omni-Cheese Pizza");
    }
}
