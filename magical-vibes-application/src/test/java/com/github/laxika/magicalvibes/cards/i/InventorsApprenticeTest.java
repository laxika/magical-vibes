package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InventorsApprentice.class, PropheticPrism.class, Island.class})
class InventorsApprenticeTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 while its controller controls an artifact")
    void boostedWithControlledArtifact() {
        Permanent apprentice = harness.addToBattlefieldAndReturn(player1, new InventorsApprentice());
        int powerWithoutArtifact = gqs.getEffectivePower(gd, apprentice);
        int toughnessWithoutArtifact = gqs.getEffectiveToughness(gd, apprentice);

        harness.addToBattlefield(player1, new PropheticPrism());

        assertThat(gqs.getEffectivePower(gd, apprentice)).isEqualTo(powerWithoutArtifact + 1);
        assertThat(gqs.getEffectiveToughness(gd, apprentice)).isEqualTo(toughnessWithoutArtifact + 1);
    }

    @Test
    @DisplayName("Loses the boost when the controlled artifact leaves the battlefield")
    void losesBoostWhenArtifactLeaves() {
        Permanent apprentice = harness.addToBattlefieldAndReturn(player1, new InventorsApprentice());
        harness.addToBattlefield(player1, new PropheticPrism());
        int powerWithArtifact = gqs.getEffectivePower(gd, apprentice);
        int toughnessWithArtifact = gqs.getEffectiveToughness(gd, apprentice);

        gd.playerBattlefields.get(player1.getId()).removeIf(permanent ->
                permanent.getCard().getName().equals("Prophetic Prism"));

        assertThat(gqs.getEffectivePower(gd, apprentice)).isEqualTo(powerWithArtifact - 1);
        assertThat(gqs.getEffectiveToughness(gd, apprentice)).isEqualTo(toughnessWithArtifact - 1);
    }

    @Test
    @DisplayName("An opponent's artifact does not grant the boost")
    void opponentArtifactDoesNotCount() {
        Permanent apprentice = harness.addToBattlefieldAndReturn(player1, new InventorsApprentice());
        int powerWithoutOpponentArtifact = gqs.getEffectivePower(gd, apprentice);
        int toughnessWithoutOpponentArtifact = gqs.getEffectiveToughness(gd, apprentice);

        harness.addToBattlefield(player2, new PropheticPrism());

        assertThat(gqs.getEffectivePower(gd, apprentice)).isEqualTo(powerWithoutOpponentArtifact);
        assertThat(gqs.getEffectiveToughness(gd, apprentice)).isEqualTo(toughnessWithoutOpponentArtifact);
    }

    @Test
    @DisplayName("A non-artifact permanent does not grant the boost")
    void nonArtifactPermanentDoesNotCount() {
        Permanent apprentice = harness.addToBattlefieldAndReturn(player1, new InventorsApprentice());
        int powerWithoutNonArtifact = gqs.getEffectivePower(gd, apprentice);
        int toughnessWithoutNonArtifact = gqs.getEffectiveToughness(gd, apprentice);

        harness.addToBattlefield(player1, new Island());

        assertThat(gqs.getEffectivePower(gd, apprentice)).isEqualTo(powerWithoutNonArtifact);
        assertThat(gqs.getEffectiveToughness(gd, apprentice)).isEqualTo(toughnessWithoutNonArtifact);
    }

    @Test
    @DisplayName("Multiple artifacts grant only one boost, retained until the last artifact leaves")
    void multipleArtifactsGrantOnlyOneBoost() {
        Permanent apprentice = harness.addToBattlefieldAndReturn(player1, new InventorsApprentice());
        int basePower = gqs.getEffectivePower(gd, apprentice);
        int baseToughness = gqs.getEffectiveToughness(gd, apprentice);
        Permanent firstArtifact = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());
        Permanent secondArtifact = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());

        assertThat(gqs.getEffectivePower(gd, apprentice)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, apprentice)).isEqualTo(baseToughness + 1);

        gd.playerBattlefields.get(player1.getId()).remove(firstArtifact);

        assertThat(gqs.getEffectivePower(gd, apprentice)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, apprentice)).isEqualTo(baseToughness + 1);

        gd.playerBattlefields.get(player1.getId()).remove(secondArtifact);

        assertThat(gqs.getEffectivePower(gd, apprentice)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, apprentice)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Artifacts outside the battlefield do not grant the boost")
    void artifactsInOtherZonesDoNotCount() {
        Permanent apprentice = harness.addToBattlefieldAndReturn(player1, new InventorsApprentice());
        int basePower = gqs.getEffectivePower(gd, apprentice);
        int baseToughness = gqs.getEffectiveToughness(gd, apprentice);

        harness.setHand(player1, java.util.List.of(new PropheticPrism()));
        harness.setGraveyard(player1, java.util.List.of(new PropheticPrism()));
        harness.setExile(player1, java.util.List.of(new PropheticPrism()));

        assertThat(gqs.getEffectivePower(gd, apprentice)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, apprentice)).isEqualTo(baseToughness);
    }
}
