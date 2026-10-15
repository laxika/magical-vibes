package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SnakeBasket.class})
class SnakeBasketTest extends BaseCardTest {

    @Test
    @DisplayName("Creates X 1/1 green Snake tokens and sacrifices itself")
    void createsXSnakeTokens() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Permanent basket = harness.addToBattlefieldAndReturn(player1, new SnakeBasket());

        harness.addMana(player1, ManaColor.GREEN, 3);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(basket);
        harness.activateAbility(player1, idx, 0, 3, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertNotOnBattlefield(player1, "Snake Basket");
        harness.assertInGraveyard(player1, "Snake Basket");

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Snake"))
                .hasSize(3)
                .allSatisfy(p -> {
                    assertThat(p.getCard().getPower()).isEqualTo(1);
                    assertThat(p.getCard().getToughness()).isEqualTo(1);
                    assertThat(p.getCard().getType()).isEqualTo(CardType.CREATURE);
                    assertThat(p.getCard().getColor()).isEqualTo(CardColor.GREEN);
                    assertThat(p.getCard().getSubtypes()).contains(CardSubtype.SNAKE);
                });

        // Basket sacrificed as a cost
        harness.assertNotOnBattlefield(player1, "Snake Basket");
        harness.assertInGraveyard(player1, "Snake Basket");
    }

    @Test
    @DisplayName("X=0 creates no tokens but still sacrifices itself")
    void xZeroCreatesNoTokens() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Permanent basket = harness.addToBattlefieldAndReturn(player1, new SnakeBasket());

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(basket);
        harness.activateAbility(player1, idx, 0, 0, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Snake");
        harness.assertInGraveyard(player1, "Snake Basket");
    }

    @Test
    @DisplayName("Requires X generic mana to activate")
    void requiresMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Permanent basket = harness.addToBattlefieldAndReturn(player1, new SnakeBasket());

        // Only 1 mana available, but X=3 requested
        harness.addMana(player1, ManaColor.GREEN, 1);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(basket);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, 0, 3, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Snake Basket");
        harness.assertNotInGraveyard(player1, "Snake Basket");
    }

    @Test
    @DisplayName("Cannot activate at instant speed (sorcery-speed only)")
    void cannotActivateAtInstantSpeed() {
        // Not player1's turn -> sorcery-speed restriction blocks activation
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Permanent basket = harness.addToBattlefieldAndReturn(player1, new SnakeBasket());

        harness.addMana(player1, ManaColor.GREEN, 3);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(basket);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, 0, 3, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Snake Basket");
        harness.assertNotInGraveyard(player1, "Snake Basket");
    }

    @Test
    @DisplayName("Cannot activate outside a main phase on its controller's turn")
    void cannotActivateOutsideMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);

        Permanent basket = harness.addToBattlefieldAndReturn(player1, new SnakeBasket());

        harness.addMana(player1, ManaColor.GREEN, 3);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(basket);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, 0, 3, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Snake Basket");
        harness.assertNotInGraveyard(player1, "Snake Basket");
    }

    @Test
    @DisplayName("Cannot activate while another Basket ability is on the stack")
    void cannotActivateWithNonemptyStack() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new SnakeBasket());
        Permanent secondBasket = harness.addToBattlefieldAndReturn(player1, new SnakeBasket());
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, 0, 2, null);
        harness.assertNotOnBattlefield(player1, "Snake");
        assertThat(gd.stack).hasSize(1);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(secondBasket);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, 0, 2, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(secondBasket);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Snake"))
                .hasSize(2);
        harness.assertNotOnBattlefield(player2, "Snake");
    }

    @Test
    @DisplayName("A tapped Basket can activate during its controller's second main phase")
    void tappedBasketCanActivateInSecondMainPhase() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        Permanent basket = harness.addToBattlefieldAndReturn(player2, new SnakeBasket());
        basket.tap();
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 0, 0, 2, null);

        harness.assertInGraveyard(player2, "Snake Basket");
        harness.assertNotOnBattlefield(player2, "Snake");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Snake"))
                .hasSize(2)
                .allSatisfy(p -> {
                    assertThat(p.getCard().isToken()).isTrue();
                    assertThat(p.isTapped()).isFalse();
                });
        harness.assertNotOnBattlefield(player1, "Snake");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }
}
