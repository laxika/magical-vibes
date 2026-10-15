package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BadMoon;
import com.github.laxika.magicalvibes.cards.c.CoastalPiracy;
import com.github.laxika.magicalvibes.cards.e.Esperzoa;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.ThoughtReflection;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FiligreeFracture.class, CoastalPiracy.class, BadMoon.class, FountainOfYouth.class,
        GrizzlyBears.class, Esperzoa.class, ThoughtReflection.class})
class FiligreeFractureTest extends BaseCardTest {

    @Test
    @DisplayName("Blue enchantment is destroyed and the spell's controller draws a card")
    void blueEnchantmentDestroyedAndDraws() {
        harness.addToBattlefield(player2, new CoastalPiracy()); // blue enchantment
        int deckSizeBefore = harness.getGameData().playerDecks.get(player1.getId()).size();
        castFiligreeFracture(harness.getPermanentId(player2, "Coastal Piracy"));

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player2, "Coastal Piracy");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Black enchantment is destroyed and the spell's controller draws a card")
    void blackEnchantmentDestroyedAndDraws() {
        harness.addToBattlefield(player2, new BadMoon()); // black enchantment
        int deckSizeBefore = harness.getGameData().playerDecks.get(player1.getId()).size();
        castFiligreeFracture(harness.getPermanentId(player2, "Bad Moon"));

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player2, "Bad Moon");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Colorless artifact is destroyed but the spell's controller does not draw")
    void colorlessArtifactDestroyedNoDraw() {
        harness.addToBattlefield(player2, new FountainOfYouth()); // colorless artifact
        int deckSizeBefore = harness.getGameData().playerDecks.get(player1.getId()).size();
        castFiligreeFracture(harness.getPermanentId(player2, "Fountain of Youth"));

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player2, "Fountain of Youth");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
    }

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

    @Test
    @DisplayName("A blue artifact creature is a legal target and causes one draw")
    void blueArtifactCreatureDestroyedAndDraws() {
        harness.addToBattlefield(player2, new Esperzoa());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castFiligreeFracture(harness.getPermanentId(player2, "Esperzoa"));

        harness.assertNotOnBattlefield(player2, "Esperzoa");
        harness.assertInGraveyard(player2, "Esperzoa");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Destroying your Thought Reflection removes its replacement before drawing")
    void destroysDrawReplacementBeforeDrawing() {
        harness.addToBattlefield(player1, new ThoughtReflection());
        harness.setLibrary(player1, List.of(new FiligreeFracture(), new FiligreeFracture(),
                new FiligreeFracture()));

        castFiligreeFracture(harness.getPermanentId(player1, "Thought Reflection"));

        harness.assertNotOnBattlefield(player1, "Thought Reflection");
        harness.assertInGraveyard(player1, "Thought Reflection");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A target destroyed in response does not cause another draw")
    void removedTargetDoesNotDraw() {
        harness.addToBattlefield(player2, new Esperzoa());
        UUID targetId = harness.getPermanentId(player2, "Esperzoa");
        harness.setHand(player1, List.of(new FiligreeFracture(), new FiligreeFracture()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castInstant(player1, 0, targetId);
        harness.castAndResolveInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Esperzoa");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
        assertThat(gd.stack).isEmpty();
    }

    private void castFiligreeFracture(UUID targetId) {
        harness.setHand(player1, List.of(new FiligreeFracture()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveInstant(player1, 0, targetId);
    }
}
