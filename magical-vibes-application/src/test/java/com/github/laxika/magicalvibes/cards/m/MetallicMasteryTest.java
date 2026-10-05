package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.b.BlisterstickShaman;
import com.github.laxika.magicalvibes.cards.i.IchorWellspring;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MetallicMastery.class, IchorWellspring.class, MyrSire.class, BlisterstickShaman.class})
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
        assertThat(entry.getTargetId()).isEqualTo(artifact.getId());
    }

    @Test
    @DisplayName("Resolving Metallic Mastery gains control, untaps, and grants haste")
    void resolvesGainControlUntapAndHaste() {
        Permanent artifact = addArtifact(player2);
        artifact.tap();
        harness.setHand(player1, List.of(new MetallicMastery()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, artifact.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(artifact.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(artifact.getId()));
        assertThat(artifact.isTapped()).isFalse();
        assertThat(artifact.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.isStolenUntilEndOfTurn(artifact.getId())).isTrue();
    }

    @Test
    @DisplayName("Control and haste expire at cleanup step")
    void controlAndHasteExpireAtCleanup() {
        Permanent artifact = addArtifact(player2);
        harness.setHand(player1, List.of(new MetallicMastery()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, artifact.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(artifact.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(artifact.getId()));
        assertThat(artifact.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.isStolenUntilEndOfTurn(artifact.getId())).isFalse();
    }

    @Test
    @DisplayName("Can target own artifact (control change is a no-op, still untaps and grants haste)")
    void canTargetOwnArtifact() {
        Permanent artifact = addArtifact(player1);
        artifact.tap();
        harness.setHand(player1, List.of(new MetallicMastery()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, artifact.getId());

        assertThat(artifact.isTapped()).isFalse();
        assertThat(artifact.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.isStolenUntilEndOfTurn(artifact.getId())).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-artifact permanent")
    void cannotTargetNonArtifact() {
        addArtifact(player1); // valid target so spell is playable
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BlisterstickShaman());
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
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Can steal an artifact creature and it can attack due to haste")
    void canStealArtifactCreature() {
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player2, new MyrSire());
        artifactCreature.setSummoningSick(false);

        harness.setHand(player1, List.of(new MetallicMastery()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, artifactCreature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(artifactCreature.getId()));
        assertThat(artifactCreature.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(artifactCreature.isTapped()).isFalse();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(artifactCreature.isAttacking()).isTrue();
    }

    @Test
    @CardUsed({MesmericOrb.class})
    @DisplayName("Control changes before untapping, so Mesmeric Orb mills the new controller")
    void gainsControlBeforeUntapTriggers() {
        harness.addToBattlefield(player2, new MesmericOrb());
        Permanent artifact = addArtifact(player2);
        artifact.tap();
        IchorWellspring player1TopCard = new IchorWellspring();
        IchorWellspring player2TopCard = new IchorWellspring();
        harness.setLibrary(player1, List.of(player1TopCard));
        harness.setLibrary(player2, List.of(player2TopCard));
        harness.setHand(player1, List.of(new MetallicMastery()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(player1TopCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(player2TopCard);
    }

    private Permanent addArtifact(com.github.laxika.magicalvibes.model.Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new IchorWellspring());
        perm.setSummoningSick(false);
        return perm;
    }
}
