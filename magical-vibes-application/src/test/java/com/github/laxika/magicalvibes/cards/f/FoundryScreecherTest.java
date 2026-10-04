package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FoundryScreecher.class, LeoninScimitar.class, Island.class})
class FoundryScreecherTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+0 while its controller controls an artifact")
    void boostedWithControlledArtifact() {
        Permanent screecher = harness.addToBattlefieldAndReturn(player1, new FoundryScreecher());
        int powerWithoutArtifact = gqs.getEffectivePower(gd, screecher);

        harness.addToBattlefield(player1, new LeoninScimitar());

        assertThat(gqs.getEffectivePower(gd, screecher)).isEqualTo(powerWithoutArtifact + 1);
    }

    @Test
    @DisplayName("Loses the boost when the controlled artifact leaves the battlefield")
    void losesBoostWhenArtifactLeaves() {
        Permanent screecher = harness.addToBattlefieldAndReturn(player1, new FoundryScreecher());
        harness.addToBattlefield(player1, new LeoninScimitar());
        int powerWithArtifact = gqs.getEffectivePower(gd, screecher);

        gd.playerBattlefields.get(player1.getId()).removeIf(permanent ->
                permanent.getCard().getName().equals("Leonin Scimitar"));

        assertThat(gqs.getEffectivePower(gd, screecher)).isEqualTo(powerWithArtifact - 1);
    }

    @Test
    @DisplayName("An opponent's artifact does not grant the boost")
    void opponentArtifactDoesNotCount() {
        Permanent screecher = harness.addToBattlefieldAndReturn(player1, new FoundryScreecher());
        int powerWithoutOpponentArtifact = gqs.getEffectivePower(gd, screecher);

        harness.addToBattlefield(player2, new LeoninScimitar());

        assertThat(gqs.getEffectivePower(gd, screecher)).isEqualTo(powerWithoutOpponentArtifact);
    }

    @Test
    @DisplayName("A non-artifact permanent does not grant the boost")
    void nonArtifactPermanentDoesNotCount() {
        Permanent screecher = harness.addToBattlefieldAndReturn(player1, new FoundryScreecher());
        int powerWithoutNonArtifact = gqs.getEffectivePower(gd, screecher);

        harness.addToBattlefield(player1, new Island());

        assertThat(gqs.getEffectivePower(gd, screecher)).isEqualTo(powerWithoutNonArtifact);
    }

    @Test
    @DisplayName("Multiple artifacts grant only one bonus until the last artifact leaves")
    void multipleArtifactsGrantOnlyOneBonus() {
        Permanent screecher = harness.addToBattlefieldAndReturn(player1, new FoundryScreecher());
        int initialPower = gqs.getEffectivePower(gd, screecher);
        int initialToughness = gqs.getEffectiveToughness(gd, screecher);
        Permanent firstArtifact = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent secondArtifact = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        assertThat(gqs.getEffectivePower(gd, screecher)).isEqualTo(initialPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, screecher)).isEqualTo(initialToughness);

        gd.playerBattlefields.get(player1.getId()).remove(firstArtifact);

        assertThat(gqs.getEffectivePower(gd, screecher)).isEqualTo(initialPower + 1);

        gd.playerBattlefields.get(player1.getId()).remove(secondArtifact);

        assertThat(gqs.getEffectivePower(gd, screecher)).isEqualTo(initialPower);
    }

    @Test
    @DisplayName("Artifacts in hand and graveyard do not grant the bonus")
    void artifactsOutsideBattlefieldDoNotCount() {
        Permanent screecher = harness.addToBattlefieldAndReturn(player1, new FoundryScreecher());
        int initialPower = gqs.getEffectivePower(gd, screecher);

        harness.setHand(player1, List.of(new LeoninScimitar()));
        harness.setGraveyard(player1, List.of(new LeoninScimitar()));

        assertThat(gqs.getEffectivePower(gd, screecher)).isEqualTo(initialPower);
    }

    @Test
    @DisplayName("Each Screecher checks its own controller's artifacts independently")
    void eachScreecherUsesItsOwnController() {
        Permanent ownScreecher = harness.addToBattlefieldAndReturn(player1, new FoundryScreecher());
        Permanent opposingScreecher = harness.addToBattlefieldAndReturn(player2, new FoundryScreecher());
        int ownInitialPower = gqs.getEffectivePower(gd, ownScreecher);
        int opposingInitialPower = gqs.getEffectivePower(gd, opposingScreecher);

        harness.addToBattlefield(player1, new LeoninScimitar());

        assertThat(gqs.getEffectivePower(gd, ownScreecher)).isEqualTo(ownInitialPower + 1);
        assertThat(gqs.getEffectivePower(gd, opposingScreecher)).isEqualTo(opposingInitialPower);

        harness.addToBattlefield(player2, new LeoninScimitar());

        assertThat(gqs.getEffectivePower(gd, ownScreecher)).isEqualTo(ownInitialPower + 1);
        assertThat(gqs.getEffectivePower(gd, opposingScreecher)).isEqualTo(opposingInitialPower + 1);
    }
}
