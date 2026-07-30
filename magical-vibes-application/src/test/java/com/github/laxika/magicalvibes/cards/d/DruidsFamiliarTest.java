package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DruidsFamiliarTest extends BaseCardTest {

    private Permanent castAndPairWithBears() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DruidsFamiliar()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve spell -> soulbond may on stack
        harness.passBothPriorities(); // resolve may -> prompt
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        return bears;
    }

    private Permanent familiar() {
        return findPermanent(player1, "Druid's Familiar");
    }

    @Test
    @DisplayName("Soulbond ETB pairs Druid's Familiar with another unpaired creature")
    void soulbondPairsOnEnter() {
        Permanent bears = castAndPairWithBears();
        Permanent familiar = familiar();

        assertThat(familiar.getPairedWithId()).isEqualTo(bears.getId());
        assertThat(bears.getPairedWithId()).isEqualTo(familiar.getId());
    }

    @Test
    @DisplayName("While paired, both creatures get +2/+2")
    void pairedBothGetBoost() {
        Permanent bears = castAndPairWithBears();
        Permanent familiar = familiar();

        assertThat(gqs.getEffectivePower(gd, familiar)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, familiar)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Unpaired Druid's Familiar gets no boost")
    void unpairedHasNoBoost() {
        harness.addToBattlefield(player1, new DruidsFamiliar());
        Permanent familiar = familiar();

        assertThat(familiar.getPairedWithId()).isNull();
        assertThat(gqs.getEffectivePower(gd, familiar)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, familiar)).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining soulbond leaves both unpaired and unboosted")
    void decliningLeavesUnpairedWithoutBoost() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DruidsFamiliar()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent familiar = familiar();
        assertThat(familiar.getPairedWithId()).isNull();
        assertThat(bears.getPairedWithId()).isNull();
        assertThat(gqs.getEffectivePower(gd, familiar)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("An unpaired third creature is not boosted")
    void unpairedBystanderIsNotBoosted() {
        castAndPairWithBears();
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
    }
}
