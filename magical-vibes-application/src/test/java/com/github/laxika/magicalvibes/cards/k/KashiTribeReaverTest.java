package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KashiTribeReaverTest extends BaseCardTest {

    private void resolveStack() {
        for (int guard = 0; guard < 40 && !gd.stack.isEmpty() && !gd.interaction.isAwaitingInput(); guard++) {
            harness.passBothPriorities();
        }
    }

    @Test
    @DisplayName("Combat damage to a creature taps it and locks its next untap step")
    void combatDamageTapsAndLocksBlocker() {
        Permanent reaver = addCreatureReady(player1, new KashiTribeReaver());
        reaver.setAttacking(true);
        // 2/4 survives the Reaver's 3 damage, so the tap/untap lock is observable.
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
        Permanent reaver = addCreatureReady(player1, new KashiTribeReaver());
        reaver.setAttacking(true);
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

    @Test
    @DisplayName("Activating {1}{G} grants a regeneration shield")
    void regenerationAbilityGrantsShield() {
        Permanent reaver = addCreatureReady(player1, new KashiTribeReaver());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(reaver.getRegenerationShield()).isEqualTo(1);
    }
}
