package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.j.Juggernaut;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpectralGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("Noncreature artifacts have shroud while the Guardian is untapped")
    void noncreatureArtifactsHaveShroud() {
        harness.addToBattlefield(player1, new SpectralGuardian());
        harness.addToBattlefield(player2, new IcyManipulator());
        Permanent icy = findPermanent(player2, "Icy Manipulator");

        assertThat(gqs.hasKeyword(gd, icy, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Artifact creatures are unaffected")
    void artifactCreaturesUnaffected() {
        harness.addToBattlefield(player1, new SpectralGuardian());
        harness.addToBattlefield(player2, new Juggernaut());
        Permanent juggernaut = findPermanent(player2, "Juggernaut");

        assertThat(gqs.hasKeyword(gd, juggernaut, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Nonartifact permanents are unaffected")
    void nonArtifactsUnaffected() {
        harness.addToBattlefield(player1, new SpectralGuardian());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");

        assertThat(gqs.hasKeyword(gd, bears, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Shroud is lost while the Guardian is tapped")
    void shroudLostWhileTapped() {
        harness.addToBattlefield(player1, new SpectralGuardian());
        Permanent guardian = findPermanent(player1, "Spectral Guardian");
        harness.addToBattlefield(player2, new IcyManipulator());
        Permanent icy = findPermanent(player2, "Icy Manipulator");

        guardian.tap();

        assertThat(gqs.hasKeyword(gd, icy, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Shroud is lost once the Guardian leaves the battlefield")
    void shroudLostWhenGuardianLeaves() {
        harness.addToBattlefield(player1, new SpectralGuardian());
        Permanent guardian = findPermanent(player1, "Spectral Guardian");
        harness.addToBattlefield(player2, new IcyManipulator());
        Permanent icy = findPermanent(player2, "Icy Manipulator");

        assertThat(gqs.hasKeyword(gd, icy, Keyword.SHROUD)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(guardian);

        assertThat(gqs.hasKeyword(gd, icy, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("A noncreature artifact with shroud cannot be targeted by a spell")
    void shroudedArtifactCannotBeTargeted() {
        harness.addToBattlefield(player2, new SpectralGuardian());
        harness.addToBattlefield(player2, new IcyManipulator());
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID icyId = harness.getPermanentId(player2, "Icy Manipulator");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, icyId))
                .isInstanceOf(IllegalStateException.class);
    }
}
