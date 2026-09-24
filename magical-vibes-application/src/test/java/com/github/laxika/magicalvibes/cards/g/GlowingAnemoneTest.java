package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CloudSprite;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GlowingAnemone.class, CloudSprite.class, Island.class})
class GlowingAnemoneTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may return a target land to its owner's hand")
    void etbReturnsTargetLandWhenAccepted() {
        harness.addToBattlefield(player2, new Island());

        UUID islandId = harness.getPermanentId(player2, "Island");
        harness.castFromHand(player1, new GlowingAnemone(), "{3}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, islandId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Glowing Anemone");
        harness.assertNotOnBattlefield(player2, "Island");
        harness.assertInHand(player2, "Island");
    }

    @Test
    @DisplayName("ETB may return a land its controller owns to its owner's hand")
    void etbReturnsControllersLandWhenAccepted() {
        harness.addToBattlefield(player1, new Island());

        UUID islandId = harness.getPermanentId(player1, "Island");
        harness.castFromHand(player1, new GlowingAnemone(), "{3}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, islandId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Glowing Anemone");
        harness.assertNotOnBattlefield(player1, "Island");
        harness.assertInHand(player1, "Island");
    }

    @Test
    @DisplayName("Declining the ETB ability leaves the target land on the battlefield")
    void decliningMayLeavesLand() {
        harness.addToBattlefield(player2, new Island());

        UUID islandId = harness.getPermanentId(player2, "Island");
        harness.castFromHand(player1, new GlowingAnemone(), "{3}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, islandId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Glowing Anemone");
        harness.assertOnBattlefield(player2, "Island");
    }

    @Test
    @DisplayName("The ETB ability is not offered when no land is available")
    void noTriggerWithoutLandTarget() {
        harness.addToBattlefield(player2, new CloudSprite());

        harness.castFromHand(player1, new GlowingAnemone(), "{3}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Glowing Anemone");
        harness.assertOnBattlefield(player2, "Cloud Sprite");
    }
}
