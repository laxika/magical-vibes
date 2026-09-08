package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GuidelightSynergistTest extends BaseCardTest {

    @Test
    @DisplayName("Counts itself as an artifact")
    void countsItselfAsAnArtifact() {
        harness.addToBattlefield(player1, new GuidelightSynergist());

        Permanent synergist = findPermanent(player1, "Guidelight Synergist");
        assertThat(gqs.getEffectivePower(gd, synergist)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, synergist)).isEqualTo(4);
    }

    @Test
    @DisplayName("Gets +1/+0 for each artifact controlled")
    void getsPowerForEachArtifactControlled() {
        harness.addToBattlefield(player1, new GuidelightSynergist());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Ornithopter());

        Permanent synergist = findPermanent(player1, "Guidelight Synergist");
        assertThat(gqs.getEffectivePower(gd, synergist)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, synergist)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not count nonartifacts or an opponent's artifacts")
    void ignoresNonArtifactsAndOpponentsArtifacts() {
        harness.addToBattlefield(player1, new GuidelightSynergist());
        harness.addToBattlefield(player2, new Ornithopter());

        Permanent synergist = findPermanent(player1, "Guidelight Synergist");
        assertThat(gqs.getEffectivePower(gd, synergist)).isEqualTo(1);
    }

    @Test
    @DisplayName("Updates when a controlled artifact leaves")
    void updatesWhenArtifactLeaves() {
        harness.addToBattlefield(player1, new GuidelightSynergist());
        harness.addToBattlefield(player1, new Ornithopter());

        Permanent synergist = findPermanent(player1, "Guidelight Synergist");
        assertThat(gqs.getEffectivePower(gd, synergist)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Ornithopter"));

        assertThat(gqs.getEffectivePower(gd, synergist)).isEqualTo(1);
    }
}
