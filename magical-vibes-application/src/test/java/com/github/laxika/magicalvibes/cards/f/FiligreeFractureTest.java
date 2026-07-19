package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BadMoon;
import com.github.laxika.magicalvibes.cards.c.CoastalPiracy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FiligreeFractureTest extends BaseCardTest {

    // ===== Blue permanent: destroyed + draw =====

    @Test
    @DisplayName("Blue enchantment is destroyed and its controller draws a card")
    void blueEnchantmentDestroyedAndDraws() {
        harness.addToBattlefield(player2, new CoastalPiracy()); // blue enchantment
        int deckSizeBefore = harness.getGameData().playerDecks.get(player1.getId()).size();
        castFiligreeFracture(harness.getPermanentId(player2, "Coastal Piracy"));

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Coastal Piracy"));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    // ===== Black permanent: destroyed + draw =====

    @Test
    @DisplayName("Black enchantment is destroyed and its controller draws a card")
    void blackEnchantmentDestroyedAndDraws() {
        harness.addToBattlefield(player2, new BadMoon()); // black enchantment
        int deckSizeBefore = harness.getGameData().playerDecks.get(player1.getId()).size();
        castFiligreeFracture(harness.getPermanentId(player2, "Bad Moon"));

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Bad Moon"));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    // ===== Colorless permanent: destroyed, no draw =====

    @Test
    @DisplayName("Colorless artifact is destroyed but its controller does not draw")
    void colorlessArtifactDestroyedNoDraw() {
        harness.addToBattlefield(player2, new FountainOfYouth()); // colorless artifact
        int deckSizeBefore = harness.getGameData().playerDecks.get(player1.getId()).size();
        castFiligreeFracture(harness.getPermanentId(player2, "Fountain of Youth"));

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Fountain of Youth"));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
    }

    // ===== Targeting =====

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FiligreeFracture()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private void castFiligreeFracture(UUID targetId) {
        harness.setHand(player1, List.of(new FiligreeFracture()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
