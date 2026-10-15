package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(PlunderingPirate.class)
class PlunderingPirateTest extends BaseCardTest {

    @Test
    @DisplayName("When Plundering Pirate enters, it creates a Treasure token")
    void etbCreatesTreasureToken() {
        harness.castFromHand(player1, new PlunderingPirate(), "{2}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Treasure creation waits for the enters trigger to resolve")
    void treasureCreationWaitsForTriggerResolution() {
        harness.castFromHand(player1, new PlunderingPirate(), "{2}{R}");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Plundering Pirate");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, mode = EnumSource.Mode.EXCLUDE, names = "COLORLESS")
    @DisplayName("The Treasure can immediately be sacrificed for one mana of any color")
    void treasureProducesOneManaOfAnyColor(ManaColor color) {
        harness.castFromHand(player1, new PlunderingPirate(), "{2}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Treasure").isTapped()).isFalse();
        harness.activateAbility(player1, 1, 0, null, null);
        harness.handleListChoice(player1, color.name());

        harness.assertNotOnBattlefield(player1, "Treasure");
        harness.assertOnBattlefield(player1, "Plundering Pirate");
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
