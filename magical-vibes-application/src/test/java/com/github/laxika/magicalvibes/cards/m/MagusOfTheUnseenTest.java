package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AladdinsRing;
import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.f.FreyalisesWinds;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.StealArtifact;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AladdinsRing.class, Bonesplitter.class, Disenchant.class, FreyalisesWinds.class, GrizzlyBears.class, MagusOfTheUnseen.class,
        MesmericOrb.class, StealArtifact.class})
class MagusOfTheUnseenTest extends BaseCardTest {

    @Test
    @DisplayName("Activating untaps the opponent's artifact and gains control of it")
    void activatingStealsAndUntapsArtifact() {
        Permanent magus = addCreatureReady(player1, new MagusOfTheUnseen());
        Permanent artifact = addArtifact(player2);
        artifact.tap();
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isFalse();
        assertThat(magus.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, artifact, Keyword.HASTE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(artifact.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(artifact.getId()));
    }

    @Test
    @DisplayName("Can target an already untapped artifact")
    void canTargetUntappedArtifact() {
        addCreatureReady(player1, new MagusOfTheUnseen());
        Permanent artifact = addArtifact(player2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(artifact.getId()));
    }

    @Test
    @DisplayName("At end of turn the artifact returns to its owner tapped")
    void artifactReturnsTappedAtEndOfTurn() {
        // Run this on the artifact owner's (player2's) turn so the cleanup control-revert is
        // observable before player2's next untap step would clear the tap.
        harness.forceActivePlayer(player2);
        addCreatureReady(player1, new MagusOfTheUnseen());
        Permanent artifact = addArtifact(player2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();
        harness.addToBattlefieldAndReturn(player2, new FreyalisesWinds());

        assertThat(artifact.isTapped()).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(artifact.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(artifact.getId()));
        assertThat(artifact.isTapped()).isTrue();
        assertThat(artifact.getCounterCount(CounterType.WIND)).isEqualTo(1);
    }

    @Test
    @DisplayName("Taps the artifact when another effect takes it before end of turn")
    void artifactIsTappedWhenAnotherEffectTakesControl() {
        harness.forceActivePlayer(player2);
        addCreatureReady(player1, new MagusOfTheUnseen());
        Permanent artifact = addArtifact(player2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        harness.addToBattlefieldAndReturn(player2, new FreyalisesWinds());
        harness.setHand(player2, List.of(new StealArtifact()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.castEnchantment(player2, 0, artifact.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(artifact.getId()));
        assertThat(artifact.isTapped()).isTrue();
        assertThat(artifact.getCounterCount(CounterType.WIND)).isEqualTo(1);
    }

    @Test
    @DisplayName("Untaps the artifact before changing its controller")
    void untapsBeforeGainingControl() {
        harness.addToBattlefieldAndReturn(player2, new MesmericOrb());
        addCreatureReady(player1, new MagusOfTheUnseen());
        Permanent artifact = addArtifact(player2);
        artifact.tap();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, artifact.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target an artifact you control")
    void cannotTargetOwnArtifact() {
        addCreatureReady(player1, new MagusOfTheUnseen());
        Permanent ownArtifact = addArtifact(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, ownArtifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact an opponent controls");
    }

    @Test
    @DisplayName("Cannot target a non-artifact permanent")
    void cannotTargetNonArtifact() {
        addCreatureReady(player1, new MagusOfTheUnseen());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact an opponent controls");
    }

    @Test
    @DisplayName("The delayed tap trigger survives cleanup while another effect maintains control")
    void delayedTapSurvivesCleanupWhenAuraMaintainsControl() {
        addCreatureReady(player1, new MagusOfTheUnseen());
        Permanent artifact = addArtifact(player2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, 0, null, artifact.getId());
        resolveAllTriggers();

        harness.setHand(player1, List.of(new StealArtifact()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castEnchantment(player1, 0, artifact.getId());
        resolveAllTriggers();
        Permanent aura = findPermanent(player1, "Steal Artifact");

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
        assertThat(artifact.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, artifact, Keyword.HASTE)).isFalse();

        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castInstant(player2, 0, aura.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact);
        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Stolen Equipment remains attached when temporary control expires")
    void equipmentRemainsAttachedWhenControlExpires() {
        harness.forceActivePlayer(player2);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new Bonesplitter());
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, 1, null, creature.getId());
        resolveAllTriggers();
        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());

        harness.forceActivePlayer(player1);
        addCreatureReady(player1, new MagusOfTheUnseen());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, 0, null, equipment.getId());
        resolveAllTriggers();
        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(equipment);
        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addArtifact(Player player) {
        return harness.addToBattlefieldAndReturn(player, new AladdinsRing());
    }
}
