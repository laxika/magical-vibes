package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MetallicMasteryTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Metallic Mastery puts it on the stack targeting an artifact")
    void castingPutsOnStack() {
        Permanent artifact = addArtifact(player2);
        harness.setHand(player1, List.of(new MetallicMastery()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, artifact.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Metallic Mastery");
        assertThat(entry.getTargetPermanentId()).isEqualTo(artifact.getId());
    }

    @Test
    @DisplayName("Resolving Metallic Mastery gains control, untaps, and grants haste")
    void resolvesGainControlUntapAndHaste() {
        Permanent artifact = addArtifact(player2);
        artifact.tap();
        harness.setHand(player1, List.of(new MetallicMastery()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(artifact.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(artifact.getId()));
        assertThat(artifact.isTapped()).isFalse();
        assertThat(artifact.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.untilEndOfTurnStolenCreatures).contains(artifact.getId());
    }

    @Test
    @DisplayName("Control and haste expire at cleanup step")
    void controlAndHasteExpireAtCleanup() {
        Permanent artifact = addArtifact(player2);
        harness.setHand(player1, List.of(new MetallicMastery()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, artifact.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(artifact.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(artifact.getId()));
        assertThat(artifact.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.untilEndOfTurnStolenCreatures).doesNotContain(artifact.getId());
    }

    @Test
    @DisplayName("Can target own artifact (control change is a no-op, still untaps and grants haste)")
    void canTargetOwnArtifact() {
        Permanent artifact = addArtifact(player1);
        artifact.tap();
        harness.setHand(player1, List.of(new MetallicMastery()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isFalse();
        assertThat(artifact.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.untilEndOfTurnStolenCreatures).doesNotContain(artifact.getId());
    }

    @Test
    @DisplayName("Cannot target a non-artifact permanent")
    void cannotTargetNonArtifact() {
        Permanent creature = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(creature);
        harness.setHand(player1, List.of(new MetallicMastery()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");
    }

    @Test
    @DisplayName("Fizzles if target artifact is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent artifact = addArtifact(player2);
        harness.setHand(player1, List.of(new MetallicMastery()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, artifact.getId());
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Can steal an artifact creature and it can attack due to haste")
    void canStealArtifactCreature() {
        Permanent artifactCreature = new Permanent(new Memnite());
        artifactCreature.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(artifactCreature);

        harness.setHand(player1, List.of(new MetallicMastery()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, artifactCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(artifactCreature.getId()));
        assertThat(artifactCreature.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(artifactCreature.isTapped()).isFalse();
    }

    private Permanent addArtifact(com.github.laxika.magicalvibes.model.Player player) {
        Permanent perm = new Permanent(new MindStone());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
