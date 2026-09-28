package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CloudPirates;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GemcutterBuccaneer.class, CloudPirates.class, GrizzlyBears.class})
class GemcutterBuccaneerTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a tapped Treasure when a Pirate enters under your control")
    void createsTappedTreasureForPirate() {
        harness.addToBattlefield(player1, new GemcutterBuccaneer());

        castCloudPirates();

        Permanent treasure = findPermanent(player1, "Treasure");
        assertThat(treasure.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Turns your Treasures into Equipment with both equip abilities")
    void equipsTreasureToPirateAndBoostsIt() {
        harness.addToBattlefield(player1, new GemcutterBuccaneer());

        castCloudPirates();

        Permanent pirate = findPermanent(player1, "Cloud Pirates");
        Permanent treasure = findPermanent(player1, "Treasure");
        assertThat(gqs.hasEffectiveSubtype(gd, treasure, CardSubtype.EQUIPMENT)).isTrue();

        int powerBefore = gqs.getEffectivePower(gd, pirate);
        int treasureIndex = gd.playerBattlefields.get(player1.getId()).indexOf(treasure);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, treasureIndex, 1, null, pirate.getId());
        harness.passBothPriorities();

        assertThat(treasure.getAttachedTo()).isEqualTo(pirate.getId());
        assertThat(gqs.getEffectivePower(gd, pirate)).isEqualTo(powerBefore + 2);
    }

    @Test
    @DisplayName("The Pirate-restricted equip ability cannot target a non-Pirate creature")
    void restrictedEquipCannotTargetNonPirate() {
        harness.addToBattlefield(player1, new GemcutterBuccaneer());

        castCloudPirates();

        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent treasure = findPermanent(player1, "Treasure");
        int treasureIndex = gd.playerBattlefields.get(player1.getId()).indexOf(treasure);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, treasureIndex, 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Pirate");
    }

    @Test
    @DisplayName("Does not create a Treasure for a non-Pirate creature")
    void doesNotTriggerForNonPirate() {
        harness.addToBattlefield(player1, new GemcutterBuccaneer());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    private void castCloudPirates() {
        harness.setHand(player1, List.of(new CloudPirates()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
