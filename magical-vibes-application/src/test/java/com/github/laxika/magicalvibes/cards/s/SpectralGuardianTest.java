package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.g.GoblinTinkerer;
import com.github.laxika.magicalvibes.cards.i.IgneousGolem;
import com.github.laxika.magicalvibes.cards.m.ManaPrism;
import com.github.laxika.magicalvibes.cards.z.ZhalfirinKnight;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpectralGuardian.class, ManaPrism.class, IgneousGolem.class, ZhalfirinKnight.class,
        GoblinTinkerer.class, Disenchant.class})
class SpectralGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("Noncreature artifacts have shroud while the Guardian is untapped")
    void noncreatureArtifactsHaveShroud() {
        harness.addToBattlefield(player1, new ManaPrism());
        harness.addToBattlefield(player1, new SpectralGuardian());
        harness.addToBattlefield(player2, new ManaPrism());
        Permanent ownManaPrism = findPermanent(player1, "Mana Prism");
        Permanent opponentManaPrism = findPermanent(player2, "Mana Prism");

        assertThat(gqs.hasKeyword(gd, ownManaPrism, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentManaPrism, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Artifact creatures are unaffected")
    void artifactCreaturesUnaffected() {
        harness.addToBattlefield(player1, new SpectralGuardian());
        harness.addToBattlefield(player2, new IgneousGolem());
        Permanent golem = findPermanent(player2, "Igneous Golem");

        assertThat(gqs.hasKeyword(gd, golem, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Nonartifact permanents are unaffected")
    void nonArtifactsUnaffected() {
        harness.addToBattlefield(player1, new SpectralGuardian());
        harness.addToBattlefield(player2, new ZhalfirinKnight());
        Permanent knight = findPermanent(player2, "Zhalfirin Knight");

        assertThat(gqs.hasKeyword(gd, knight, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Shroud is lost while the Guardian is tapped")
    void shroudLostWhileTapped() {
        harness.addToBattlefield(player1, new SpectralGuardian());
        Permanent guardian = findPermanent(player1, "Spectral Guardian");
        harness.addToBattlefield(player2, new ManaPrism());
        Permanent manaPrism = findPermanent(player2, "Mana Prism");

        guardian.tap();

        assertThat(gqs.hasKeyword(gd, manaPrism, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Shroud is lost once the Guardian leaves the battlefield")
    void shroudLostWhenGuardianLeaves() {
        harness.addToBattlefield(player1, new SpectralGuardian());
        Permanent guardian = findPermanent(player1, "Spectral Guardian");
        harness.addToBattlefield(player2, new ManaPrism());
        Permanent manaPrism = findPermanent(player2, "Mana Prism");

        assertThat(gqs.hasKeyword(gd, manaPrism, Keyword.SHROUD)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(guardian);

        assertThat(gqs.hasKeyword(gd, manaPrism, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("A noncreature artifact with shroud cannot be targeted by a spell")
    void shroudedArtifactCannotBeTargeted() {
        harness.addToBattlefield(player2, new SpectralGuardian());
        harness.addToBattlefield(player2, new ManaPrism());
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID manaPrismId = harness.getPermanentId(player2, "Mana Prism");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, manaPrismId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A noncreature artifact with shroud cannot be targeted by an ability")
    void shroudedArtifactCannotBeTargetedByAbility() {
        harness.addToBattlefield(player2, new SpectralGuardian());
        harness.addToBattlefield(player2, new ManaPrism());
        addCreatureReady(player1, new GoblinTinkerer());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        UUID manaPrismId = harness.getPermanentId(player2, "Mana Prism");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, manaPrismId))
                .isInstanceOf(IllegalStateException.class);
    }
}
