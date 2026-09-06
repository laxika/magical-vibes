package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GoldenEgg;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({CourierOfComestibles.class, GoldenEgg.class, GrizzlyBears.class})
class CourierOfComestiblesTest extends BaseCardTest {

    @Test
    @DisplayName("Finds a Food card and puts it into hand")
    void findsFoodCard() {
        castCourier(List.of(new GoldenEgg(), new GrizzlyBears()));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Golden Egg");
        harness.assertNotOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("Creates a Food token when no Food card is found")
    void createsFoodTokenWhenNoFoodCardIsFound() {
        castCourier(List.of(new GrizzlyBears()));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("Creates a Food token when the search is declined")
    void createsFoodTokenWhenSearchIsDeclined() {
        castCourier(List.of(new GoldenEgg()));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("Creates a Food token when no card is chosen from the search")
    void createsFoodTokenWhenNoCardIsChosen() {
        castCourier(List.of(new GoldenEgg()));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        harness.assertOnBattlefield(player1, "Food");
    }

    private void castCourier(List<Card> library) {
        harness.setHand(player1, List.of(new CourierOfComestibles()));
        harness.setLibrary(player1, library);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
    }
}
