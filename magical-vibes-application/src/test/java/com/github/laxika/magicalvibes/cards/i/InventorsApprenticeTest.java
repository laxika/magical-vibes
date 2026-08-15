package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InventorsApprenticeTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 while its controller controls an artifact")
    void boostedWithControlledArtifact() {
        harness.addToBattlefield(player1, new InventorsApprentice());
        Permanent apprentice = findApprentice();
        int powerWithoutArtifact = gqs.getEffectivePower(gd, apprentice);
        int toughnessWithoutArtifact = gqs.getEffectiveToughness(gd, apprentice);

        harness.addToBattlefield(player1, new LeoninScimitar());

        assertThat(gqs.getEffectivePower(gd, apprentice)).isEqualTo(powerWithoutArtifact + 1);
        assertThat(gqs.getEffectiveToughness(gd, apprentice)).isEqualTo(toughnessWithoutArtifact + 1);
    }

    @Test
    @DisplayName("Loses the boost when the controlled artifact leaves the battlefield")
    void losesBoostWhenArtifactLeaves() {
        harness.addToBattlefield(player1, new InventorsApprentice());
        harness.addToBattlefield(player1, new LeoninScimitar());

        Permanent apprentice = findApprentice();
        int powerWithArtifact = gqs.getEffectivePower(gd, apprentice);
        int toughnessWithArtifact = gqs.getEffectiveToughness(gd, apprentice);

        gd.playerBattlefields.get(player1.getId()).removeIf(permanent ->
                permanent.getCard().getName().equals("Leonin Scimitar"));

        assertThat(gqs.getEffectivePower(gd, apprentice)).isEqualTo(powerWithArtifact - 1);
        assertThat(gqs.getEffectiveToughness(gd, apprentice)).isEqualTo(toughnessWithArtifact - 1);
    }

    @Test
    @DisplayName("An opponent's artifact does not grant the boost")
    void opponentArtifactDoesNotCount() {
        harness.addToBattlefield(player1, new InventorsApprentice());
        Permanent apprentice = findApprentice();
        int powerWithoutOpponentArtifact = gqs.getEffectivePower(gd, apprentice);
        int toughnessWithoutOpponentArtifact = gqs.getEffectiveToughness(gd, apprentice);

        harness.addToBattlefield(player2, new LeoninScimitar());

        assertThat(gqs.getEffectivePower(gd, apprentice)).isEqualTo(powerWithoutOpponentArtifact);
        assertThat(gqs.getEffectiveToughness(gd, apprentice)).isEqualTo(toughnessWithoutOpponentArtifact);
    }

    @Test
    @DisplayName("A non-artifact permanent does not grant the boost")
    void nonArtifactPermanentDoesNotCount() {
        harness.addToBattlefield(player1, new InventorsApprentice());
        Permanent apprentice = findApprentice();
        int powerWithoutNonArtifact = gqs.getEffectivePower(gd, apprentice);
        int toughnessWithoutNonArtifact = gqs.getEffectiveToughness(gd, apprentice);

        harness.addToBattlefield(player1, new Island());

        assertThat(gqs.getEffectivePower(gd, apprentice)).isEqualTo(powerWithoutNonArtifact);
        assertThat(gqs.getEffectiveToughness(gd, apprentice)).isEqualTo(toughnessWithoutNonArtifact);
    }

    private Permanent findApprentice() {
        return findPermanent(player1, "Inventor's Apprentice");
    }
}
