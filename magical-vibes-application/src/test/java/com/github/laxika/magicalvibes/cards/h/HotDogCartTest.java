package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HotDogCart.class})
class HotDogCartTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Food token when it enters")
    void createsFoodTokenOnEnter() {
        harness.setHand(player1, List.of(new HotDogCart()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("Tapping adds the chosen color of mana")
    void tappingAddsChosenColor() {
        harness.addToBattlefield(player1, new HotDogCart());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The created Food token can be sacrificed for life")
    void foodTokenCanBeSacrificed() {
        harness.setHand(player1, List.of(new HotDogCart()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertNotOnBattlefield(player1, "Food");
    }
}
