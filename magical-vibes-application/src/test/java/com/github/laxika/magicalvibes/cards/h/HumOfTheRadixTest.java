package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.l.LeoninSkyhunter;
import com.github.laxika.magicalvibes.cards.m.MyrRetriever;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HumOfTheRadix.class, Bonesplitter.class, LeoninSkyhunter.class, MyrRetriever.class})
class HumOfTheRadixTest extends BaseCardTest {

    @Test
    @DisplayName("Artifact spells cost one more for each artifact their controller controls")
    void artifactSpellCostScalesWithItsControllersArtifacts() {
        harness.addToBattlefield(player1, new HumOfTheRadix());
        harness.addToBattlefield(player1, new Bonesplitter());
        harness.setHand(player1, List.of(new Bonesplitter()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Hum of the Radix counts the artifact spell controller's artifacts")
    void countsTheArtifactSpellControllersArtifacts() {
        harness.addToBattlefield(player1, new HumOfTheRadix());
        harness.addToBattlefield(player1, new Bonesplitter());
        harness.forceActivePlayer(player2);
        harness.forceStep(gd.currentStep);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Bonesplitter()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castArtifact(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Artifact spell cost increases once for each artifact the caster controls")
    void artifactSpellCostScalesWithMultipleArtifacts() {
        harness.addToBattlefield(player1, new HumOfTheRadix());
        harness.addToBattlefield(player1, new Bonesplitter());
        harness.addToBattlefield(player1, new Bonesplitter());
        harness.setHand(player1, List.of(new Bonesplitter()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Artifact creature spells are taxed as artifact spells")
    void artifactCreatureSpellIsTaxed() {
        harness.addToBattlefield(player1, new HumOfTheRadix());
        harness.addToBattlefield(player1, new Bonesplitter());
        harness.setHand(player1, List.of(new MyrRetriever()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Nonartifact spells are not affected")
    void nonartifactSpellsAreNotAffected() {
        harness.addToBattlefield(player1, new HumOfTheRadix());
        harness.castFromHand(player1, new LeoninSkyhunter(), "{W}{W}");

        assertThat(gd.stack).hasSize(1);
    }
}
