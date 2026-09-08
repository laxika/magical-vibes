package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HoodwinkTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target artifact to its owner's hand")
    void returnsArtifactToHand() {
        harness.addToBattlefield(player2, new AngelsFeather());

        castHoodwink(harness.getPermanentId(player2, "Angel's Feather"));

        harness.assertNotOnBattlefield(player2, "Angel's Feather");
        harness.assertInHand(player2, "Angel's Feather");
    }

    @Test
    @DisplayName("Returns a target enchantment to its owner's hand")
    void returnsEnchantmentToHand() {
        harness.addToBattlefield(player2, new AngelicChorus());

        castHoodwink(harness.getPermanentId(player2, "Angelic Chorus"));

        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInHand(player2, "Angelic Chorus");
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
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Hoodwink()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact, enchantment, or land");
    }

    private void castHoodwink(UUID targetId) {
        harness.setHand(player1, List.of(new Hoodwink()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
