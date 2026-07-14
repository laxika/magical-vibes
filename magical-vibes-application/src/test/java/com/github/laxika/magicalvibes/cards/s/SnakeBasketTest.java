package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SnakeBasketTest extends BaseCardTest {

    @Test
    @DisplayName("Creates X 1/1 green Snake tokens and sacrifices itself")
    void createsXSnakeTokens() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new SnakeBasket());
        Permanent basket = findPermanent(player1, "Snake Basket");

        harness.addMana(player1, ManaColor.GREEN, 3);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(basket);
        harness.activateAbility(player1, idx, 0, 3, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Snake"))
                .hasSize(3)
                .allSatisfy(p -> {
                    assertThat(p.getCard().getPower()).isEqualTo(1);
                    assertThat(p.getCard().getToughness()).isEqualTo(1);
                    assertThat(p.getCard().getColor()).isEqualTo(CardColor.GREEN);
                    assertThat(p.getCard().getSubtypes()).contains(CardSubtype.SNAKE);
                });

        // Basket sacrificed as a cost
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Snake Basket"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Snake Basket"));
    }

    @Test
    @DisplayName("X=0 creates no tokens but still sacrifices itself")
    void xZeroCreatesNoTokens() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new SnakeBasket());
        Permanent basket = findPermanent(player1, "Snake Basket");

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(basket);
        harness.activateAbility(player1, idx, 0, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Snake"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Snake Basket"));
    }

    @Test
    @DisplayName("Requires X generic mana to activate")
    void requiresMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new SnakeBasket());
        Permanent basket = findPermanent(player1, "Snake Basket");

        // Only 1 mana available, but X=3 requested
        harness.addMana(player1, ManaColor.GREEN, 1);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(basket);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, 0, 3, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate at instant speed (sorcery-speed only)")
    void cannotActivateAtInstantSpeed() {
        // Not player1's turn -> sorcery-speed restriction blocks activation
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new SnakeBasket());
        Permanent basket = findPermanent(player1, "Snake Basket");

        harness.addMana(player1, ManaColor.GREEN, 3);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(basket);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, 0, 3, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
