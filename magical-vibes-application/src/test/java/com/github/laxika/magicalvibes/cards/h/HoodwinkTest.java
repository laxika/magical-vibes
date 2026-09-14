package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BargainingTable;
import com.github.laxika.magicalvibes.cards.c.CoastalPiracy;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Hoodwink.class, BargainingTable.class, CoastalPiracy.class, Forest.class,
        FreshVolunteers.class})
class HoodwinkTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target artifact to its owner's hand")
    void returnsArtifactToHand() {
        harness.addToBattlefield(player2, new BargainingTable());

        castHoodwink(harness.getPermanentId(player2, "Bargaining Table"));

        harness.assertNotOnBattlefield(player2, "Bargaining Table");
        harness.assertInHand(player2, "Bargaining Table");
    }

    @Test
    @DisplayName("Returns a target enchantment to its owner's hand")
    void returnsEnchantmentToHand() {
        harness.addToBattlefield(player2, new CoastalPiracy());

        castHoodwink(harness.getPermanentId(player2, "Coastal Piracy"));

        harness.assertNotOnBattlefield(player2, "Coastal Piracy");
        harness.assertInHand(player2, "Coastal Piracy");
    }

    @Test
    @DisplayName("Returns a target land to its owner's hand")
    void returnsLandToHand() {
        harness.addToBattlefield(player2, new Forest());

        castHoodwink(harness.getPermanentId(player2, "Forest"));

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInHand(player2, "Forest");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new FreshVolunteers());
        harness.setHand(player1, List.of(new Hoodwink()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID creatureId = harness.getPermanentId(player2, "Fresh Volunteers");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact, enchantment, or land");
    }

    @Test
    @DisplayName("Returns a controlled permanent to its owner's hand")
    void returnsControlledPermanentToItsOwnerHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BargainingTable());
        gd.stolenCreatures.put(target.getId(), player1.getId());

        castHoodwink(target.getId());

        harness.assertNotOnBattlefield(player2, "Bargaining Table");
        harness.assertInHand(player1, "Bargaining Table");
        harness.assertNotInHand(player2, "Bargaining Table");
    }

    @Test
    @DisplayName("Does nothing when the target leaves before resolution")
    void doesNothingWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BargainingTable());
        harness.setHand(player1, List.of(new Hoodwink()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, target.getId());

        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hoodwink");
    }

    private void castHoodwink(UUID targetId) {
        harness.setHand(player1, List.of(new Hoodwink()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, targetId);
    }
}
