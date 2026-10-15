package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Reconstruction.class, SolRing.class, LightningBolt.class, Ornithopter.class})
class ReconstructionTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target artifact card from your graveyard to your hand")
    void returnsTargetArtifactFromGraveyardToHand() {
        Card artifact = new SolRing();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setHand(player1, List.of(new Reconstruction()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, artifact.getId());

        harness.assertInHand(player1, "Sol Ring");
        harness.assertNotInGraveyard(player1, "Sol Ring");
        harness.assertInGraveyard(player1, "Reconstruction");
    }

    @Test
    @DisplayName("Returns only the targeted artifact and leaves other graveyard cards there")
    void returnsOnlyTargetedArtifact() {
        Card artifact = new SolRing();
        Card nonartifact = new LightningBolt();
        harness.setGraveyard(player1, List.of(nonartifact, artifact));
        harness.setHand(player1, List.of(new Reconstruction()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, artifact.getId());

        harness.assertInHand(player1, "Sol Ring");
        harness.assertNotInGraveyard(player1, "Sol Ring");
        harness.assertInGraveyard(player1, "Lightning Bolt");
        harness.assertInGraveyard(player1, "Reconstruction");
    }

    @Test
    @DisplayName("Cannot target a nonartifact card in your graveyard")
    void cannotTargetNonArtifactCard() {
        Card nonartifact = new LightningBolt();
        harness.setGraveyard(player1, List.of(nonartifact));
        harness.setHand(player1, List.of(new Reconstruction()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, nonartifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an artifact card in an opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        Card artifact = new SolRing();
        harness.setGraveyard(player2, List.of(artifact));
        harness.setHand(player1, List.of(new Reconstruction()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your graveyard");
    }

    @Test
    @DisplayName("Fizzles if the targeted artifact card leaves the graveyard before resolution")
    void fizzlesIfTargetLeavesGraveyardBeforeResolution() {
        Card artifact = new SolRing();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setHand(player1, List.of(new Reconstruction()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, artifact.getId());
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Sol Ring");
        harness.assertInGraveyard(player1, "Reconstruction");
    }

    @Test
    @DisplayName("Returns an artifact creature card to hand")
    void returnsArtifactCreatureToHand() {
        Card artifactCreature = new Ornithopter();
        harness.setGraveyard(player1, List.of(artifactCreature));
        harness.setHand(player1, List.of(new Reconstruction()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, artifactCreature.getId());

        harness.assertInHand(player1, "Ornithopter");
        harness.assertNotInGraveyard(player1, "Ornithopter");
        harness.assertInGraveyard(player1, "Reconstruction");
    }

    @Test
    @DisplayName("Cannot be cast without a target even when an artifact is available")
    void cannotCastWithoutTarget() {
        harness.setGraveyard(player1, List.of(new SolRing()));
        harness.setHand(player1, List.of(new Reconstruction()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not return another artifact when the targeted artifact leaves the graveyard")
    void doesNotSubstituteAnotherArtifactForMissingTarget() {
        Card target = new SolRing();
        Card otherArtifact = new Ornithopter();
        harness.setGraveyard(player1, List.of(target, otherArtifact));
        harness.setHand(player1, List.of(new Reconstruction()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, target.getId());
        harness.setGraveyard(player1, List.of(otherArtifact));
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Sol Ring");
        harness.assertNotInHand(player1, "Ornithopter");
        harness.assertInGraveyard(player1, "Ornithopter");
        harness.assertInGraveyard(player1, "Reconstruction");
    }
}
