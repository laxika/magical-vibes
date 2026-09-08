package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FoundryScreecherTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+0 while its controller controls an artifact")
    void boostedWithControlledArtifact() {
        harness.addToBattlefield(player1, new FoundryScreecher());
        Permanent screecher = findScreecher();
        int powerWithoutArtifact = gqs.getEffectivePower(gd, screecher);

        harness.addToBattlefield(player1, new LeoninScimitar());

        assertThat(gqs.getEffectivePower(gd, screecher)).isEqualTo(powerWithoutArtifact + 1);
    }

    @Test
    @DisplayName("Loses the boost when the controlled artifact leaves the battlefield")
    void losesBoostWhenArtifactLeaves() {
        harness.addToBattlefield(player1, new FoundryScreecher());
        harness.addToBattlefield(player1, new LeoninScimitar());

        Permanent screecher = findScreecher();
        int powerWithArtifact = gqs.getEffectivePower(gd, screecher);

        gd.playerBattlefields.get(player1.getId()).removeIf(permanent ->
                permanent.getCard().getName().equals("Leonin Scimitar"));

        assertThat(gqs.getEffectivePower(gd, screecher)).isEqualTo(powerWithArtifact - 1);
    }

    @Test
    @DisplayName("An opponent's artifact does not grant the boost")
    void opponentArtifactDoesNotCount() {
        harness.addToBattlefield(player1, new FoundryScreecher());
        Permanent screecher = findScreecher();
        int powerWithoutOpponentArtifact = gqs.getEffectivePower(gd, screecher);

        harness.addToBattlefield(player2, new LeoninScimitar());

        assertThat(gqs.getEffectivePower(gd, screecher)).isEqualTo(powerWithoutOpponentArtifact);
    }

    @Test
    @DisplayName("A non-artifact permanent does not grant the boost")
    void nonArtifactPermanentDoesNotCount() {
        harness.addToBattlefield(player1, new FoundryScreecher());
        Permanent screecher = findScreecher();
        int powerWithoutNonArtifact = gqs.getEffectivePower(gd, screecher);

        harness.addToBattlefield(player1, new Island());

        assertThat(gqs.getEffectivePower(gd, screecher)).isEqualTo(powerWithoutNonArtifact);
    }

    private Permanent findScreecher() {
        return findPermanent(player1, "Foundry Screecher");
    }
}
