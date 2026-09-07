package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(GildedGoose.class)
class GildedGooseTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a Food token")
    void entersWithFoodToken() {
        castGoose();

        Permanent food = findPermanent(player1, "Food");
        assertThat(food.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(food.getCard().getSubtypes()).contains(CardSubtype.FOOD);
        assertThat(food.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Creates a Food token from its activated ability")
    void createsFoodTokenFromActivatedAbility() {
        castGoose();
        findPermanent(player1, "Gilded Goose").setSummoningSick(false);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isEqualTo(2);
    }

    @Test
    @DisplayName("Sacrificing a Food adds one mana of the chosen color")
    void sacrificesFoodForAnyColorMana() {
        castGoose();
        findPermanent(player1, "Gilded Goose").setSummoningSick(false);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(countPermanents(player1, "Food")).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Food tokens can be sacrificed to gain 3 life")
    void foodTokenCanBeSacrificedForLife() {
        castGoose();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(countPermanents(player1, "Food")).isZero();
    }

    private void castGoose() {
        harness.setHand(player1, List.of(new GildedGoose()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
