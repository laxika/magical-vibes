package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DakkonBlackblade.class, DiamondValley.class})
class DakkonBlackbladeTest extends BaseCardTest {

    @Test
    @DisplayName("Dakkon dies to state-based actions with no lands")
    void diesWithNoLands() {
        harness.castFromHand(player1, new DakkonBlackblade(), "{2}{W}{U}{U}{B}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dakkon Blackblade");
        harness.assertInGraveyard(player1, "Dakkon Blackblade");
    }

    @Test
    @DisplayName("Dakkon's power and toughness equal lands you control")
    void ptEqualsControlledLands() {
        Permanent dakkon = addCreatureReady(player1, new DakkonBlackblade());
        harness.addToBattlefield(player1, new DiamondValley());
        harness.addToBattlefield(player1, new DiamondValley());
        harness.addToBattlefield(player1, new DiamondValley());

        assertThat(gqs.getEffectivePower(gd, dakkon)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, dakkon)).isEqualTo(3);
    }

    @Test
    @DisplayName("Dakkon counts only its controller's lands")
    void countsOnlyControllersLands() {
        Permanent dakkon = addCreatureReady(player1, new DakkonBlackblade());
        harness.addToBattlefield(player1, new DiamondValley());
        harness.addToBattlefield(player2, new DiamondValley());
        harness.addToBattlefield(player2, new DiamondValley());

        assertThat(gqs.getEffectivePower(gd, dakkon)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, dakkon)).isEqualTo(1);
    }

    @Test
    @DisplayName("Dakkon's power and toughness update when lands change")
    void ptUpdatesWhenLandsChange() {
        Permanent dakkon = addCreatureReady(player1, new DakkonBlackblade());
        harness.addToBattlefield(player1, new DiamondValley());

        assertThat(gqs.getEffectivePower(gd, dakkon)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, dakkon)).isEqualTo(1);

        harness.addToBattlefield(player1, new DiamondValley());
        assertThat(gqs.getEffectivePower(gd, dakkon)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, dakkon)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().hasType(CardType.LAND));
        assertThat(gqs.getEffectivePower(gd, dakkon)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, dakkon)).isEqualTo(0);
    }

    @Test
    @DisplayName("Dakkon survives resolution with a land and dies when the last land leaves")
    void survivesWithLandThenDiesWithoutLands() {
        harness.addToBattlefield(player1, new DiamondValley());
        harness.castFromHand(player1, new DakkonBlackblade(), "{2}{W}{U}{U}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dakkon Blackblade");

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().hasType(CardType.LAND));
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Dakkon Blackblade");
        harness.assertInGraveyard(player1, "Dakkon Blackblade");
    }

    @Test
    @DisplayName("Dakkon's characteristic-defining ability works in hand")
    void countsOwnersLandsInHand() {
        DakkonBlackblade dakkon = new DakkonBlackblade();
        harness.setHand(player1, java.util.List.of(dakkon));
        harness.addToBattlefield(player1, new DiamondValley());
        harness.addToBattlefield(player2, new DiamondValley());
        harness.addToBattlefield(player2, new DiamondValley());

        assertThat(gqs.getEffectiveCardPower(gd, dakkon)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardToughness(gd, dakkon)).isEqualTo(1);
    }

    @Test
    @DisplayName("Dakkon's characteristic-defining ability updates in the graveyard")
    void countsOwnersLandsInGraveyard() {
        DakkonBlackblade dakkon = new DakkonBlackblade();
        harness.setGraveyard(player1, java.util.List.of(dakkon));
        harness.addToBattlefield(player2, new DiamondValley());

        assertThat(gqs.getEffectiveCardPower(gd, dakkon)).isEqualTo(0);
        assertThat(gqs.getEffectiveCardToughness(gd, dakkon)).isEqualTo(0);

        harness.addToBattlefield(player1, new DiamondValley());
        harness.addToBattlefield(player1, new DiamondValley());

        assertThat(gqs.getEffectiveCardPower(gd, dakkon)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, dakkon)).isEqualTo(2);
    }
}
