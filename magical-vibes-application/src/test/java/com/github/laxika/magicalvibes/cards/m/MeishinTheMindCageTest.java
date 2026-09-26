package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.h.HandOfHonor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MeishinTheMindCage.class, HandOfHonor.class})
class MeishinTheMindCageTest extends BaseCardTest {

    @Test
    @DisplayName("Reduces the power of all creatures by the controller's hand size")
    void reducesAllCreaturesPowerByControllerHandSize() {
        harness.setHand(player1, List.of(new HandOfHonor(), new HandOfHonor()));
        harness.addToBattlefield(player1, new MeishinTheMindCage());
        Permanent ownCreature = addCreatureReady(player1, new HandOfHonor());
        Permanent opposingCreature = addCreatureReady(player2, new HandOfHonor());

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Counts only the Meishin controller's hand")
    void countsOnlyTheControllerHand() {
        harness.setHand(player1, List.of(new HandOfHonor()));
        harness.setHand(player2, List.of(
                new HandOfHonor(), new HandOfHonor(), new HandOfHonor(), new HandOfHonor()));
        harness.addToBattlefield(player1, new MeishinTheMindCage());
        Permanent ownCreature = addCreatureReady(player1, new HandOfHonor());
        Permanent opposingCreature = addCreatureReady(player2, new HandOfHonor());

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Updates as the controller's hand size changes")
    void updatesWhenControllerHandChanges() {
        harness.setHand(player1, List.of(new HandOfHonor()));
        harness.addToBattlefield(player1, new MeishinTheMindCage());
        Permanent creature = addCreatureReady(player2, new HandOfHonor());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);

        harness.setHand(player1, List.of(new HandOfHonor(), new HandOfHonor()));

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }
}
