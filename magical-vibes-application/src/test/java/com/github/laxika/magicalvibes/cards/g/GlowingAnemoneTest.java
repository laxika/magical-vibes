package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GlowingAnemoneTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may return a target land to its owner's hand")
    void etbReturnsTargetLandWhenAccepted() {
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new GlowingAnemone()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        UUID islandId = harness.getPermanentId(player2, "Island");
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, islandId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Glowing Anemone");
        harness.assertNotOnBattlefield(player2, "Island");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> card.getName())
                .contains("Island");
    }

    @Test
    @DisplayName("Declining the ETB ability leaves the target land on the battlefield")
    void decliningMayLeavesLand() {
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new GlowingAnemone()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        UUID islandId = harness.getPermanentId(player2, "Island");
        harness.castCreature(player1, 0);
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
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GlowingAnemone()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Glowing Anemone");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }
}
