package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WurmsTooth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HumOfTheRadixTest extends BaseCardTest {

    @Test
    @DisplayName("Artifact spells cost one more for each artifact their controller controls")
    void artifactSpellCostScalesWithItsControllersArtifacts() {
        harness.addToBattlefield(player1, new HumOfTheRadix());
        harness.addToBattlefield(player1, new WurmsTooth());
        harness.setHand(player1, List.of(new WurmsTooth()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Hum of the Radix counts the artifact spell controller's artifacts")
    void countsTheArtifactSpellControllersArtifacts() {
        harness.addToBattlefield(player1, new HumOfTheRadix());
        harness.addToBattlefield(player1, new WurmsTooth());
        harness.forceActivePlayer(player2);
        harness.forceStep(gd.currentStep);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new WurmsTooth()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.castArtifact(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Nonartifact spells are not affected")
    void nonartifactSpellsAreNotAffected() {
        harness.addToBattlefield(player1, new HumOfTheRadix());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }
}
