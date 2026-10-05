package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("The mana ability immediately adds exactly one mana of any chosen color")
    void manaAbilityResolvesWithoutUsingTheStack(ManaColor color) {
        harness.addToBattlefield(player1, new OmniCheesePizza());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Omni-Cheese Pizza");
        assertThat(gd.stack).isEmpty();
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, lifeBefore);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Neither ability can be activated while the Pizza is tapped")
    void tappedPizzaCannotBeSacrificedForEitherAbility(int abilityIndex) {
        var pizza = harness.addToBattlefieldAndReturn(player1, new OmniCheesePizza());
        pizza.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Omni-Cheese Pizza");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Neither ability can be activated without paying its mana cost")
    void unpaidManaCostDoesNotSacrificeThePizza(int abilityIndex) {
        var pizza = harness.addToBattlefieldAndReturn(player1, new OmniCheesePizza());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Omni-Cheese Pizza");
        assertThat(pizza.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The life ability pays its sacrifice cost before resolving on the stack")
    void lifeAbilityUsesTheStackAndPaysTwoMana() {
        harness.addToBattlefield(player1, new OmniCheesePizza());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int lifeBefore = gd.getLife(player1.getId());
        int opponentLifeBefore = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertInGraveyard(player1, "Omni-Cheese Pizza");
        harness.assertNotOnBattlefield(player1, "Omni-Cheese Pizza");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, lifeBefore);

        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore + 3);
        harness.assertLife(player2, opponentLifeBefore);
    }

    @Test
    @DisplayName("The enter trigger still draws a card after the Pizza is sacrificed")
    void enterTriggerSurvivesSacrificingItsSource() {
        harness.setHand(player1, List.of(new OmniCheesePizza()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());
        harness.assertInGraveyard(player1, "Omni-Cheese Pizza");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
    }
}
