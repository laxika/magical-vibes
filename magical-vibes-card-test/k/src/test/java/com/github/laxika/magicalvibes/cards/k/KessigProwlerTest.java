package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.w.WoodlandPatrol;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KessigProwler.class, WoodlandPatrol.class})
class KessigProwlerTest extends BaseCardTest {

    @Test
    @DisplayName("{4}{G} transforms Kessig Prowler")
    void transformAbilityFlipsToSinuousPredator() {
        Permanent prowler = addReadyProwler();
        addTransformMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(prowler.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Sinuous Predator can be blocked by one creature")
    void canBeBlockedByOneCreature() {
        Permanent prowler = addReadyProwler();
        transform(prowler);
        prowler.setAttacking(true);
        addBlocker();

        beginBlocks();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Sinuous Predator can't be blocked by two creatures")
    void cannotBeBlockedByTwoCreatures() {
        Permanent prowler = addReadyProwler();
        transform(prowler);
        prowler.setAttacking(true);
        addBlocker();
        addBlocker();

        beginBlocks();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked by more than 1 creature");
    }

    @Test
    @DisplayName("The transform ability works while tapped and summoning sick")
    void transformsWhileTappedAndSummoningSick() {
        Permanent prowler = harness.addToBattlefieldAndReturn(player1, new KessigProwler());
        prowler.setSummoningSick(true);
        prowler.tap();
        addTransformMana();

        harness.activateAbility(player1, 0, null, null);
        assertThat(prowler.isTransformed()).isFalse();
        harness.passBothPriorities();

        assertThat(prowler.isTransformed()).isTrue();
        assertThat(prowler.isTapped()).isTrue();
        assertThat(prowler.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Two queued transform activations leave Sinuous Predator face up")
    void queuedActivationsDoNotTransformBack() {
        Permanent prowler = addReadyProwler();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(prowler.isTransformed()).isTrue();
        harness.passBothPriorities();

        assertThat(prowler.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Kessig Prowler can be blocked by two creatures before transforming")
    void frontFaceCanBeBlockedByTwoCreatures() {
        Permanent prowler = addReadyProwler();
        prowler.setAttacking(true);
        addBlocker();
        addBlocker();
        beginBlocks();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .allSatisfy(blocker -> assertThat(blocker.isBlocking()).isTrue());
    }

    @Test
    @DisplayName("Sinuous Predator can be left unblocked")
    void canBeUnblocked() {
        Permanent prowler = addReadyProwler();
        transform(prowler);
        prowler.setAttacking(true);
        addBlocker();
        beginBlocks();

        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isFalse();
    }

    private Permanent addReadyProwler() {
        Permanent prowler = harness.addToBattlefieldAndReturn(player1, new KessigProwler());
        prowler.setSummoningSick(false);
        return prowler;
    }

    private void addTransformMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private void transform(Permanent prowler) {
        addTransformMana();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(prowler), null, null);
        harness.passBothPriorities();
        assertThat(prowler.isTransformed()).isTrue();
    }

    private void addBlocker() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new WoodlandPatrol());
        blocker.setSummoningSick(false);
    }

    private void beginBlocks() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
