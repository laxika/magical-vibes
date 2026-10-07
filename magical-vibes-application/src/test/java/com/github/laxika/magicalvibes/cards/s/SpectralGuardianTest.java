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
        Permanent ownManaPrism = harness.addToBattlefieldAndReturn(player1, new ManaPrism());
        harness.addToBattlefield(player1, new SpectralGuardian());
        Permanent opponentManaPrism = harness.addToBattlefieldAndReturn(player2, new ManaPrism());

        assertThat(gqs.hasKeyword(gd, ownManaPrism, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentManaPrism, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Artifact creatures are unaffected")
    void artifactCreaturesUnaffected() {
        harness.addToBattlefield(player1, new SpectralGuardian());
        Permanent golem = harness.addToBattlefieldAndReturn(player2, new IgneousGolem());

        assertThat(gqs.hasKeyword(gd, golem, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Nonartifact permanents are unaffected")
    void nonArtifactsUnaffected() {
        harness.addToBattlefield(player1, new SpectralGuardian());
        Permanent knight = harness.addToBattlefieldAndReturn(player2, new ZhalfirinKnight());

        assertThat(gqs.hasKeyword(gd, knight, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Shroud is lost while the Guardian is tapped")
    void shroudLostWhileTapped() {
        Permanent guardian = harness.addToBattlefieldAndReturn(player1, new SpectralGuardian());
        Permanent manaPrism = harness.addToBattlefieldAndReturn(player2, new ManaPrism());

        guardian.tap();

        assertThat(gqs.hasKeyword(gd, manaPrism, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Shroud is lost once the Guardian leaves the battlefield")
    void shroudLostWhenGuardianLeaves() {
        Permanent guardian = harness.addToBattlefieldAndReturn(player1, new SpectralGuardian());
        Permanent manaPrism = harness.addToBattlefieldAndReturn(player2, new ManaPrism());

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

    @Test
    @DisplayName("Shroud returns immediately when the Guardian untaps")
    void shroudReturnsAfterUntapping() {
        Permanent guardian = harness.addToBattlefieldAndReturn(player1, new SpectralGuardian());
        Permanent prism = harness.addToBattlefieldAndReturn(player2, new ManaPrism());

        guardian.tap();
        assertThat(gqs.hasKeyword(gd, prism, Keyword.SHROUD)).isFalse();

        guardian.untap();
        assertThat(gqs.hasKeyword(gd, prism, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("One untapped Guardian continues protecting artifacts when another taps")
    void anotherUntappedGuardianMaintainsShroud() {
        Permanent guardian = harness.addToBattlefieldAndReturn(player1, new SpectralGuardian());
        harness.addToBattlefield(player2, new SpectralGuardian());
        Permanent prism = harness.addToBattlefieldAndReturn(player1, new ManaPrism());

        guardian.tap();

        assertThat(gqs.hasKeyword(gd, prism, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Shroud also prevents the artifact's controller from targeting it")
    void controllerCannotTargetOwnArtifact() {
        harness.addToBattlefield(player2, new SpectralGuardian());
        Permanent prism = harness.addToBattlefieldAndReturn(player1, new ManaPrism());
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, prism.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An artifact can be destroyed while the Guardian is tapped")
    void tappedGuardianAllowsTargeting() {
        Permanent guardian = harness.addToBattlefieldAndReturn(player2, new SpectralGuardian());
        Permanent prism = harness.addToBattlefieldAndReturn(player2, new ManaPrism());
        guardian.tap();
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, prism.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(prism);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(prism.getCard());
    }

    @Test
    @DisplayName("Untapping the Guardian makes an artifact target illegal before resolution")
    void untappingGuardianProtectsPendingTarget() {
        Permanent guardian = harness.addToBattlefieldAndReturn(player2, new SpectralGuardian());
        Permanent prism = harness.addToBattlefieldAndReturn(player2, new ManaPrism());
        guardian.tap();
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, prism.getId());

        guardian.untap();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(prism);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof Disenchant);
    }
}
