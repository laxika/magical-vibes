package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.t.TormodsCrypt;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EnsoulArtifact.class, RuneclawBear.class, TormodsCrypt.class,
        Ornithopter.class, Naturalize.class, TurnToFrog.class})
class EnsoulArtifactTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted artifact becomes a 5/5 artifact creature")
    void enchantsArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new TormodsCrypt());

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
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new EnsoulArtifact()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent's artifact can be enchanted without changing control")
    void canEnchantOpponentsArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new TormodsCrypt());

        castEnsoulArtifact(artifact);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(artifact);
        assertThat(gqs.isCreature(gd, artifact)).isTrue();
        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(5);
    }

    @Test
    @DisplayName("Animation preserves flying and applies counters on top of the new base")
    void preservesAbilitiesAndCounters() {
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        artifactCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        castEnsoulArtifact(artifactCreature);

        assertThat(gqs.hasKeyword(gd, artifactCreature, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, artifactCreature)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, artifactCreature)).isEqualTo(7);
    }

    @Test
    @DisplayName("A noncreature artifact stops being a creature when the Aura leaves")
    void animationEndsWhenAuraLeaves() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new TormodsCrypt());
        castEnsoulArtifact(artifact);

        destroyEnsoulArtifact();

        harness.assertOnBattlefield(player1, "Tormod's Crypt");
        harness.assertInGraveyard(player1, "Ensoul Artifact");
        assertThat(gqs.isCreature(gd, artifact)).isFalse();
        assertThat(gqs.isArtifact(gd, artifact)).isTrue();
    }

    @Test
    @DisplayName("An artifact creature regains its original base when the Aura leaves")
    void artifactCreatureRevertsWhenAuraLeaves() {
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        castEnsoulArtifact(artifactCreature);

        destroyEnsoulArtifact();

        assertThat(gqs.isCreature(gd, artifactCreature)).isTrue();
        assertThat(gqs.getEffectivePower(gd, artifactCreature)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, artifactCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, artifactCreature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Ensoul Artifact fails to resolve if its target leaves the battlefield")
    void targetRemovedBeforeResolution() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new TormodsCrypt());
        harness.setHand(player1, List.of(new EnsoulArtifact(), new Naturalize()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, artifact.getId());
        harness.castAndResolveInstant(player1, 0, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Tormod's Crypt");
        harness.assertInGraveyard(player1, "Ensoul Artifact");
        harness.assertNotOnBattlefield(player1, "Ensoul Artifact");
    }

    @Test
    @DisplayName("A later Ensoul Artifact overrides an earlier Turn to Frog's base power and toughness")
    void laterAuraOverridesEarlierBaseSetting() {
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        castTurnToFrog(artifactCreature);

        castEnsoulArtifact(artifactCreature);

        assertThat(gqs.getEffectivePower(gd, artifactCreature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, artifactCreature)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, artifactCreature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("A later Turn to Frog overrides Ensoul Artifact's base power and toughness")
    void laterBaseSettingOverridesEarlierAura() {
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        castEnsoulArtifact(artifactCreature);

        castTurnToFrog(artifactCreature);

        assertThat(gqs.getEffectivePower(gd, artifactCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, artifactCreature)).isEqualTo(1);
        assertThat(gqs.isArtifact(gd, artifactCreature)).isTrue();
    }

    private void castTurnToFrog(Permanent target) {
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void destroyEnsoulArtifact() {
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Ensoul Artifact"));
    }

    private void castEnsoulArtifact(Permanent target) {
        harness.setHand(player1, List.of(new EnsoulArtifact()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
