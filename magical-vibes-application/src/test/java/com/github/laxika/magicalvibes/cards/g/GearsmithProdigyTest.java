package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
        harness.addToBattlefield(player1, new LeoninScimitar());

        Permanent prodigy = findProdigy();
        assertThat(gqs.getEffectivePower(gd, prodigy)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, prodigy)).isEqualTo(2);
    }

    @Test
    @DisplayName("Loses the boost when the artifact leaves the battlefield")
    void losesBoostWhenArtifactLeaves() {
        harness.addToBattlefield(player1, new GearsmithProdigy());
        harness.addToBattlefield(player1, new LeoninScimitar());

        Permanent prodigy = findProdigy();
        assertThat(gqs.getEffectivePower(gd, prodigy)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).removeIf(permanent ->
                permanent.getCard().getName().equals("Leonin Scimitar"));

        assertThat(gqs.getEffectivePower(gd, prodigy)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's artifact does not grant the boost")
    void opponentArtifactDoesNotCount() {
        harness.addToBattlefield(player1, new GearsmithProdigy());
        harness.addToBattlefield(player2, new LeoninScimitar());

        assertThat(gqs.getEffectivePower(gd, findProdigy())).isEqualTo(1);
    }

    @Test
    @DisplayName("A non-artifact permanent does not grant the boost")
    void nonArtifactPermanentDoesNotCount() {
        harness.addToBattlefield(player1, new GearsmithProdigy());
        harness.addToBattlefield(player1, new Island());

        assertThat(gqs.getEffectivePower(gd, findProdigy())).isEqualTo(1);
    }

    private Permanent findProdigy() {
        return findPermanent(player1, "Gearsmith Prodigy");
    }
}
