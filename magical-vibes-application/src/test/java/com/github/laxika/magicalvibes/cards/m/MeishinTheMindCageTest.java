package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MeishinTheMindCageTest extends BaseCardTest {

    @Test
    @DisplayName("Reduces the power of all creatures by the controller's hand size")
    void reducesAllCreaturesPowerByControllerHandSize() {
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addToBattlefield(player1, new MeishinTheMindCage());
        Permanent ownCreature = addCreatureReady(player1, new HillGiant());
        Permanent opposingCreature = addCreatureReady(player2, new HillGiant());

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Updates as the controller's hand size changes")
    void updatesWhenControllerHandChanges() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new MeishinTheMindCage());
        Permanent creature = addCreatureReady(player2, new HillGiant());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);

        gd.playerHands.get(player1.getId()).add(new GrizzlyBears());
        gd.playerHands.get(player1.getId()).add(new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }
}
