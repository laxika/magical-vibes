package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
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

class MatsuTribeDecoyTest extends BaseCardTest {

    private void resolveStack() {
        for (int guard = 0; guard < 40 && !gd.stack.isEmpty() && !gd.interaction.isAwaitingInput(); guard++) {
            harness.passBothPriorities();
        }
    }

    @Test
    @DisplayName("Activated ability forces the target to block the Decoy")
    void abilityForcesTargetToBlock() {
        Permanent decoy = addCreatureReady(player1, new MatsuTribeDecoy());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.getMustBlockIds()).contains(decoy.getId());

        decoy.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
    }

    @Test
    @DisplayName("Ability cannot be activated without enough mana")
    void abilityNeedsMana() {
        addCreatureReady(player1, new MatsuTribeDecoy());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, blocker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Combat damage to a creature taps it and locks its next untap step")
    void combatDamageTapsAndLocksBlocker() {
        Permanent decoy = addCreatureReady(player1, new MatsuTribeDecoy());
        decoy.setAttacking(true);
        // 2/4 survives the Decoy's 1 damage, so the tap/untap lock is observable.
        addCreatureReady(player2, new GiantSpider());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveStack();

        Permanent spider = findPermanent(player2, "Giant Spider");
        assertThat(spider.isTapped()).isTrue();
        assertThat(spider.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Combat damage to a player does not tap or lock any creature")
    void unblockedDamageDoesNotTapCreatures() {
        Permanent decoy = addCreatureReady(player1, new MatsuTribeDecoy());
        decoy.setAttacking(true);
        addCreatureReady(player2, new GiantSpider());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveStack();

        Permanent spider = findPermanent(player2, "Giant Spider");
        assertThat(spider.isTapped()).isFalse();
        assertThat(spider.getSkipUntapCount()).isZero();
    }
}
