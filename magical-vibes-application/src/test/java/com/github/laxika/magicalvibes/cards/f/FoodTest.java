package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(Food.class)
class FoodTest extends BaseCardTest {

    @Test
    @DisplayName("Paying two mana and sacrificing a Food gains three life")
    void sacrificesAndGainsLife() {
        Food foodCard = new Food();
        foodCard.setName("Food");
        Permanent food = harness.addToBattlefieldAndReturn(player1, foodCard);
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(food);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(food.getCard());
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
    }
}
