package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpicyOatmealPizza.class, Forest.class, GrizzlyBears.class})
class SpicyOatmealPizzaTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield deals 4 damage to a creature and 3 damage to its controller")
    void enteringBattlefieldDamagesCreatureAndController() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new SpicyOatmealPizza()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("The enters-the-battlefield ability can target a player")
    void enteringBattlefieldDamagesPlayerAndController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SpicyOatmealPizza()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("The enters-the-battlefield ability cannot target a land")
    void enteringBattlefieldCannotTargetLand() {
        harness.addToBattlefield(player2, new Forest());

        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.castFromHand(player1, new SpicyOatmealPizza(), "{2}{R}");
        harness.passBothPriorities();
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    @DisplayName("Sacrificing it gains 3 life")
    void sacrificingItGainsThreeLife() {
        harness.addToBattlefield(player1, new SpicyOatmealPizza());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        harness.assertInGraveyard(player1, "Spicy Oatmeal Pizza");
    }

    @Test
    @DisplayName("Targeting yourself deals all seven damage to you")
    void enteringBattlefieldCanTargetItsController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SpicyOatmealPizza()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 13);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Sacrificing the Food in response does not stop its enter trigger")
    void enterTriggerResolvesAfterSourceIsSacrificed() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SpicyOatmealPizza()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Spicy Oatmeal Pizza");
        harness.assertInGraveyard(player1, "Spicy Oatmeal Pizza");
        harness.assertLife(player1, 20);

        harness.passBothPriorities();
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("An illegal creature target prevents both parts of the enter trigger")
    void illegalTargetPreventsControllerDamageAsWell() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SpicyOatmealPizza()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.enterBattlefieldAndReturn(player2, new SpicyOatmealPizza());
        harness.handlePermanentChosen(player2, target.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 17);

        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 17);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Food cannot activate its tap ability")
    void tappedFoodCannotBeSacrificedForLife() {
        Permanent food = harness.addToBattlefieldAndReturn(player1, new SpicyOatmealPizza());
        food.tap();
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Spicy Oatmeal Pizza");
        harness.assertNotInGraveyard(player1, "Spicy Oatmeal Pizza");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Activating the Food requires two mana")
    void insufficientManaDoesNotSacrificeFood() {
        Permanent food = harness.addToBattlefieldAndReturn(player1, new SpicyOatmealPizza());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(food.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Spicy Oatmeal Pizza");
        harness.assertNotInGraveyard(player1, "Spicy Oatmeal Pizza");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }
}
