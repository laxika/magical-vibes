package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnsoulArtifactTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted artifact becomes a 5/5 artifact creature")
    void enchantsArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new IcyManipulator());

        castEnsoulArtifact(artifact);

        assertThat(gqs.isCreature(gd, artifact)).isTrue();
        assertThat(gqs.isArtifact(gd, artifact)).isTrue();
        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(5);
    }

    @Test
    @DisplayName("Ensoul Artifact can target an artifact creature")
    void canTargetArtifactCreature() {
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        castEnsoulArtifact(artifactCreature);

        assertThat(gqs.isCreature(gd, artifactCreature)).isTrue();
        assertThat(gqs.getEffectivePower(gd, artifactCreature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, artifactCreature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Cannot target a nonartifact permanent")
    void cannotTargetNonartifact() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new EnsoulArtifact()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castEnsoulArtifact(Permanent target) {
        harness.setHand(player1, List.of(new EnsoulArtifact()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
