package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.e.ExplosiveApparatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GearsmithProdigy.class, ExplosiveApparatus.class, Island.class})
class GearsmithProdigyTest extends BaseCardTest {

    @Test
    @DisplayName("Is 1/2 without a controlled artifact")
    void noBoostWithoutArtifact() {
        harness.addToBattlefield(player1, new GearsmithProdigy());

        Permanent prodigy = findProdigy();
        assertThat(gqs.getEffectivePower(gd, prodigy)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, prodigy)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gets +1/+0 while its controller controls an artifact")
    void boostedWithControlledArtifact() {
        harness.addToBattlefield(player1, new GearsmithProdigy());
        harness.addToBattlefield(player1, new ExplosiveApparatus());

        Permanent prodigy = findProdigy();
        assertThat(gqs.getEffectivePower(gd, prodigy)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, prodigy)).isEqualTo(2);
    }

    @Test
    @DisplayName("Loses the boost when the artifact leaves the battlefield")
    void losesBoostWhenArtifactLeaves() {
        harness.addToBattlefield(player1, new GearsmithProdigy());
        harness.addToBattlefield(player1, new ExplosiveApparatus());

        Permanent prodigy = findProdigy();
        assertThat(gqs.getEffectivePower(gd, prodigy)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).removeIf(permanent ->
                permanent.getCard().getName().equals("Explosive Apparatus"));

        assertThat(gqs.getEffectivePower(gd, prodigy)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's artifact does not grant the boost")
    void opponentArtifactDoesNotCount() {
        harness.addToBattlefield(player1, new GearsmithProdigy());
        harness.addToBattlefield(player2, new ExplosiveApparatus());

        assertThat(gqs.getEffectivePower(gd, findProdigy())).isEqualTo(1);
    }

    @Test
    @DisplayName("A non-artifact permanent does not grant the boost")
    void nonArtifactPermanentDoesNotCount() {
        harness.addToBattlefield(player1, new GearsmithProdigy());
        harness.addToBattlefield(player1, new Island());

        assertThat(gqs.getEffectivePower(gd, findProdigy())).isEqualTo(1);
    }

    @Test
    @DisplayName("Gains the boost immediately when an artifact enters")
    void gainsBoostWhenArtifactEnters() {
        Permanent prodigy = harness.addToBattlefieldAndReturn(player1, new GearsmithProdigy());
        assertThat(gqs.getEffectivePower(gd, prodigy)).isEqualTo(1);

        harness.enterBattlefieldAndReturn(player1, new ExplosiveApparatus());

        assertThat(gqs.getEffectivePower(gd, prodigy)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, prodigy)).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple artifacts grant only one boost, which lasts until the last artifact leaves")
    void multipleArtifactsGrantOnlyOneBoost() {
        Permanent prodigy = harness.addToBattlefieldAndReturn(player1, new GearsmithProdigy());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ExplosiveApparatus());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ExplosiveApparatus());

        assertThat(gqs.getEffectivePower(gd, prodigy)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, prodigy)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(first);
        assertThat(gqs.getEffectivePower(gd, prodigy)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(second);
        assertThat(gqs.getEffectivePower(gd, prodigy)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, prodigy)).isEqualTo(2);
    }

    @Test
    @DisplayName("Artifacts in hand, graveyard, and exile do not grant the boost")
    void artifactsOutsideBattlefieldDoNotCount() {
        Permanent prodigy = harness.addToBattlefieldAndReturn(player1, new GearsmithProdigy());
        harness.setHand(player1, List.of(new ExplosiveApparatus()));
        harness.setGraveyard(player1, List.of(new ExplosiveApparatus()));
        harness.setExile(player1, List.of(new ExplosiveApparatus()));

        assertThat(gqs.getEffectivePower(gd, prodigy)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, prodigy)).isEqualTo(2);
    }

    private Permanent findProdigy() {
        return findPermanent(player1, "Gearsmith Prodigy");
    }
}
