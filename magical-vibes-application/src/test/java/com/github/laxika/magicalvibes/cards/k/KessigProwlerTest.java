package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    private Permanent addReadyProwler() {
        Permanent prowler = new Permanent(new KessigProwler());
        prowler.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(prowler);
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
        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);
    }

    private void beginBlocks() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
