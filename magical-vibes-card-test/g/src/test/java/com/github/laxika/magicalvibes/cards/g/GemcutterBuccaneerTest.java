package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CloudPirates;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GemcutterBuccaneer.class, CloudPirates.class, GrizzlyBears.class})
class GemcutterBuccaneerTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one tapped Treasure when Gemcutter Buccaneer itself enters")
    void createsTappedTreasureForItself() {
        harness.castFromHand(player1, new GemcutterBuccaneer(), "{3}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanent(player1, "Treasure").isTapped()).isTrue();
    }

    @Test
    @DisplayName("The unrestricted equip ability attaches a tapped Treasure to a non-Pirate")
    void equipsTreasureToNonPirate() {
        harness.addToBattlefield(player1, new GemcutterBuccaneer());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castCloudPirates();

        Permanent treasure = findPermanent(player1, "Treasure");
        int powerBefore = gqs.getEffectivePower(gd, bears);
        int toughnessBefore = gqs.getEffectiveToughness(gd, bears);
        int treasureIndex = gd.playerBattlefields.get(player1.getId()).indexOf(treasure);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, treasureIndex, 2, null, bears.getId());
        harness.passBothPriorities();

        assertThat(treasure.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(treasure.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(powerBefore + 2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(toughnessBefore);
    }

    @Test
    @DisplayName("An opponent's Pirate entering does not create a Treasure for you")
    void doesNotTriggerForOpponentsPirate() {
        harness.addToBattlefield(player1, new GemcutterBuccaneer());
        harness.enterBattlefieldAndReturn(player2, new CloudPirates());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

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
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    private void castCloudPirates() {
        harness.castFromHand(player1, new CloudPirates(), "{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
