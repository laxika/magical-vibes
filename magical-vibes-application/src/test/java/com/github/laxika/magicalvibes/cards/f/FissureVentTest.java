package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GhostQuarter;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FissureVentTest extends BaseCardTest {

    @Test
    @DisplayName("Artifact mode destroys the target artifact")
    void destroysArtifact() {
        harness.addToBattlefield(player2, new Millstone());
        UUID targetId = harness.getPermanentId(player2, "Millstone");

        cast(new int[]{0}, List.of(targetId));

        harness.assertNotOnBattlefield(player2, "Millstone");
        harness.assertInGraveyard(player2, "Millstone");
    }

    @Test
    @DisplayName("Nonbasic-land mode destroys the target nonbasic land")
    void destroysNonbasicLand() {
        harness.addToBattlefield(player2, new GhostQuarter());
        UUID targetId = harness.getPermanentId(player2, "Ghost Quarter");

        cast(new int[]{1}, List.of(targetId));

        harness.assertNotOnBattlefield(player2, "Ghost Quarter");
        harness.assertInGraveyard(player2, "Ghost Quarter");
    }

    @Test
    @DisplayName("Choosing both modes destroys both targets")
    void destroysArtifactAndNonbasicLand() {
        harness.addToBattlefield(player2, new Millstone());
        harness.addToBattlefield(player2, new GhostQuarter());
        UUID artifactId = harness.getPermanentId(player2, "Millstone");
        UUID landId = harness.getPermanentId(player2, "Ghost Quarter");

        cast(new int[]{0, 1}, List.of(artifactId, landId));

        harness.assertNotOnBattlefield(player2, "Millstone");
        harness.assertNotOnBattlefield(player2, "Ghost Quarter");
    }

    @Test
    @DisplayName("Nonbasic-land mode rejects a basic land")
    void rejectsBasicLand() {
        harness.addToBattlefield(player2, new Mountain());
        UUID targetId = harness.getPermanentId(player2, "Mountain");
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

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
