package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.e.EvolvingWilds;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FissureVent.class, EvolvingWilds.class, PropheticPrism.class, Mountain.class})
class FissureVentTest extends BaseCardTest {

    @Test
    @DisplayName("Artifact mode destroys the target artifact")
    void destroysArtifact() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new PropheticPrism()).getId();

        cast(new int[]{0}, List.of(targetId));

        harness.assertNotOnBattlefield(player2, "Prophetic Prism");
        harness.assertInGraveyard(player2, "Prophetic Prism");
    }

    @Test
    @DisplayName("Nonbasic-land mode destroys the target nonbasic land")
    void destroysNonbasicLand() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new EvolvingWilds()).getId();

        cast(new int[]{1}, List.of(targetId));

        harness.assertNotOnBattlefield(player2, "Evolving Wilds");
        harness.assertInGraveyard(player2, "Evolving Wilds");
    }

    @Test
    @DisplayName("Choosing both modes destroys both targets")
    void destroysArtifactAndNonbasicLand() {
        UUID artifactId = harness.addToBattlefieldAndReturn(player2, new PropheticPrism()).getId();
        UUID landId = harness.addToBattlefieldAndReturn(player2, new EvolvingWilds()).getId();

        cast(new int[]{0, 1}, List.of(artifactId, landId));

        harness.assertNotOnBattlefield(player2, "Prophetic Prism");
        harness.assertNotOnBattlefield(player2, "Evolving Wilds");
    }

    @Test
    @DisplayName("Nonbasic-land mode rejects a basic land")
    void rejectsBasicLand() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Mountain()).getId();
        harness.setHand(player1, List.of(new FissureVent()));
        addMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 2, new int[]{1}, List.of(targetId), null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int[] modes, List<UUID> targetIds) {
        harness.setHand(player1, List.of(new FissureVent()));
        addMana();
        harness.castModalSorceryWithModes(player1, 0, 1, 2, modes, targetIds, null);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Artifact mode rejects a nonartifact land")
    void rejectsNonartifact() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new EvolvingWilds()).getId();
        harness.setHand(player1, List.of(new FissureVent()));
        addMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 2, new int[]{0}, List.of(targetId), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Nonbasic-land mode rejects a nonland artifact")
    void rejectsNonland() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new PropheticPrism()).getId();
        harness.setHand(player1, List.of(new FissureVent()));
        addMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 2, new int[]{1}, List.of(targetId), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Both modes can target permanents controlled by the caster")
    void destroysOwnPermanents() {
        UUID artifactId = harness.addToBattlefieldAndReturn(player1, new PropheticPrism()).getId();
        UUID landId = harness.addToBattlefieldAndReturn(player1, new EvolvingWilds()).getId();

        cast(new int[]{0, 1}, List.of(artifactId, landId));

        harness.assertNotOnBattlefield(player1, "Prophetic Prism");
        harness.assertNotOnBattlefield(player1, "Evolving Wilds");
        harness.assertInGraveyard(player1, "Prophetic Prism");
        harness.assertInGraveyard(player1, "Evolving Wilds");
    }

    @Test
    @DisplayName("Both modes still destroy the land when the artifact target leaves")
    void resolvesWithOnlyLandTargetRemaining() {
        var artifact = harness.addToBattlefieldAndReturn(player2, new PropheticPrism());
        UUID landId = harness.addToBattlefieldAndReturn(player2, new EvolvingWilds()).getId();
        harness.setHand(player1, List.of(new FissureVent()));
        addMana();
        harness.castModalSorceryWithModes(player1, 0, 1, 2,
                new int[]{0, 1}, List.of(artifact.getId(), landId), null);

        gd.playerBattlefields.get(player2.getId()).remove(artifact);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Evolving Wilds");
        harness.assertInGraveyard(player2, "Evolving Wilds");
        harness.assertInGraveyard(player1, "Fissure Vent");
    }

    @Test
    @DisplayName("Both modes still destroy the artifact when the land target leaves")
    void resolvesWithOnlyArtifactTargetRemaining() {
        UUID artifactId = harness.addToBattlefieldAndReturn(player2, new PropheticPrism()).getId();
        var land = harness.addToBattlefieldAndReturn(player2, new EvolvingWilds());
        harness.setHand(player1, List.of(new FissureVent()));
        addMana();
        harness.castModalSorceryWithModes(player1, 0, 1, 2,
                new int[]{0, 1}, List.of(artifactId, land.getId()), null);

        gd.playerBattlefields.get(player2.getId()).remove(land);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Prophetic Prism");
        harness.assertInGraveyard(player2, "Prophetic Prism");
        harness.assertInGraveyard(player1, "Fissure Vent");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
