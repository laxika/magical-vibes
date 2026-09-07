package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AncientDen;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Vandalize.class, AncientDen.class, Forest.class, Spellbook.class})
class VandalizeTest extends BaseCardTest {

    @Test
    @DisplayName("Artifact mode destroys the target artifact")
    void destroysArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());

        cast(new int[]{0}, List.of(artifact.getId()));

        harness.assertNotOnBattlefield(player2, "Spellbook");
    }

    @Test
    @DisplayName("Land mode destroys the target land")
    void destroysLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        cast(new int[]{1}, List.of(land.getId()));

        harness.assertNotOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Both modes may target the same artifact land")
    void bothModesMayShareArtifactLandTarget() {
        Permanent artifactLand = harness.addToBattlefieldAndReturn(player2, new AncientDen());

        cast(new int[]{0, 1}, List.of(artifactLand.getId(), artifactLand.getId()));

        harness.assertNotOnBattlefield(player2, "Ancient Den");
    }

    @Test
    @DisplayName("A selected mode rejects a target of the wrong type")
    void rejectsMismatchedTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, List.of(new Vandalize()));
        addMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 2, new int[]{0}, List.of(land.getId()), null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int[] modes, List<java.util.UUID> targets) {
        harness.setHand(player1, List.of(new Vandalize()));
        addMana();
        harness.castModalSorceryWithModes(player1, 0, 1, 2, modes, targets, null);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 5);
    }
}
