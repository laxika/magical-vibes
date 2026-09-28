package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
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
        harness.setHand(player1, java.util.List.of(new DakkonBlackblade()));
        addManaForDakkon();

        harness.castCreature(player1, 0);
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

    private void addManaForDakkon() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
