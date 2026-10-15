package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.m.MycosynthLattice;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.v.VoyagerQuickwelder;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GuidelightSynergist.class, VoyagerQuickwelder.class, Plains.class, MycosynthLattice.class})
class GuidelightSynergistTest extends BaseCardTest {

    @Test
    @DisplayName("Counts itself as an artifact")
    void countsItselfAsAnArtifact() {
        Permanent synergist = harness.addToBattlefieldAndReturn(player1, new GuidelightSynergist());

        assertThat(gqs.getEffectivePower(gd, synergist)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, synergist)).isEqualTo(4);
    }

    @Test
    @DisplayName("Gets +1/+0 for each artifact controlled")
    void getsPowerForEachArtifactControlled() {
        Permanent synergist = harness.addToBattlefieldAndReturn(player1, new GuidelightSynergist());
        harness.addToBattlefield(player1, new VoyagerQuickwelder());
        harness.addToBattlefield(player1, new VoyagerQuickwelder());

        assertThat(gqs.getEffectivePower(gd, synergist)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, synergist)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not count nonartifacts or an opponent's artifacts")
    void ignoresNonArtifactsAndOpponentsArtifacts() {
        Permanent synergist = harness.addToBattlefieldAndReturn(player1, new GuidelightSynergist());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new VoyagerQuickwelder());

        assertThat(gqs.getEffectivePower(gd, synergist)).isEqualTo(1);
    }

    @Test
    @DisplayName("Updates when a controlled artifact leaves")
    void updatesWhenArtifactLeaves() {
        Permanent synergist = harness.addToBattlefieldAndReturn(player1, new GuidelightSynergist());
        harness.addToBattlefield(player1, new VoyagerQuickwelder());

        assertThat(gqs.getEffectivePower(gd, synergist)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Voyager Quickwelder"));

        assertThat(gqs.getEffectivePower(gd, synergist)).isEqualTo(1);
    }

    @Test
    @DisplayName("Updates when another controlled artifact enters")
    void updatesWhenArtifactEnters() {
        Permanent synergist = harness.addToBattlefieldAndReturn(player1, new GuidelightSynergist());
        assertThat(gqs.getEffectivePower(gd, synergist)).isEqualTo(1);

        harness.addToBattlefield(player1, new VoyagerQuickwelder());

        assertThat(gqs.getEffectivePower(gd, synergist)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, synergist)).isEqualTo(4);
    }

    @Test
    @DisplayName("Counts permanents made into artifacts by Mycosynth Lattice")
    void countsArtifactsGrantedByContinuousEffects() {
        Permanent synergist = harness.addToBattlefieldAndReturn(player1, new GuidelightSynergist());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new Plains());
        harness.addToBattlefield(player2, new MycosynthLattice());

        assertThat(gqs.getEffectivePower(gd, synergist)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, synergist)).isEqualTo(4);
    }
}
