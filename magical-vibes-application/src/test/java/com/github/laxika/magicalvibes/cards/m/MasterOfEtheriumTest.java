package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MasterOfEtheriumTest extends BaseCardTest {

    // ===== P/T = number of artifacts you control =====

    @Test
    @DisplayName("P/T is 1/1 when only itself is on the battlefield (it is an artifact)")
    void ptIsOneOneWhenAlone() {
        Permanent master = harness.addToBattlefieldAndReturn(player1, new MasterOfEtherium());

        assertThat(gqs.getEffectivePower(gd, master)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, master)).isEqualTo(1);
    }

    @Test
    @DisplayName("P/T increases with additional artifacts you control")
    void ptIncreasesWithArtifacts() {
        Permanent master = harness.addToBattlefieldAndReturn(player1, new MasterOfEtherium());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());

        assertThat(gqs.getEffectivePower(gd, master)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, master)).isEqualTo(3);
    }

    @Test
    @DisplayName("P/T only counts controller's artifacts, not opponent's")
    void ptOnlyCountsOwnArtifacts() {
        Permanent master = harness.addToBattlefieldAndReturn(player1, new MasterOfEtherium());
        harness.addToBattlefield(player2, new Spellbook());
        harness.addToBattlefield(player2, new Spellbook());

        assertThat(gqs.getEffectivePower(gd, master)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, master)).isEqualTo(1);
    }

    @Test
    @DisplayName("Non-artifact creatures do not count toward P/T")
    void nonArtifactCreaturesDoNotCount() {
        Permanent master = harness.addToBattlefieldAndReturn(player1, new MasterOfEtherium());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, master)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, master)).isEqualTo(1);
    }

    // ===== Lord: other artifact creatures you control get +1/+1 =====

    @Test
    @DisplayName("Other artifact creatures you control get +1/+1")
    void boostsOwnArtifactCreatures() {
        harness.addToBattlefield(player1, new MasterOfEtherium());
        Permanent thopter = harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        // Ornithopter is a 0/2 artifact creature -> 1/3
        assertThat(gqs.getEffectivePower(gd, thopter)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, thopter)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not boost your non-artifact creatures")
    void doesNotBoostNonArtifactCreatures() {
        harness.addToBattlefield(player1, new MasterOfEtherium());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost an opponent's artifact creatures")
    void doesNotBoostOpponentArtifactCreatures() {
        harness.addToBattlefield(player1, new MasterOfEtherium());
        Permanent thopter = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        assertThat(gqs.getEffectivePower(gd, thopter)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, thopter)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost itself (only OTHER artifact creatures)")
    void doesNotBoostItself() {
        // With Master alone, its P/T equals artifact count (1); the lord must not add +1/+1 to itself.
        Permanent master = harness.addToBattlefieldAndReturn(player1, new MasterOfEtherium());

        assertThat(gqs.getEffectivePower(gd, master)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, master)).isEqualTo(1);
    }

    @Test
    @DisplayName("Two Masters: each counts both for */* and each gets the other's +1/+1")
    void twoMastersInteract() {
        Permanent masterA = harness.addToBattlefieldAndReturn(player1, new MasterOfEtherium());
        Permanent masterB = harness.addToBattlefieldAndReturn(player1, new MasterOfEtherium());

        // Each sees 2 artifacts for base */*, plus +1/+1 from the other Master → 3/3
        assertThat(gqs.getEffectivePower(gd, masterA)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, masterA)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, masterB)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, masterB)).isEqualTo(3);
    }
}
