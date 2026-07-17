package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HollowTreesTest extends BaseCardTest {

    // ===== Enters tapped =====

    @Test
    @DisplayName("Hollow Trees enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new HollowTrees()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        assertThat(findPermanent(player1, "Hollow Trees").isTapped()).isTrue();
    }

    // ===== Upkeep storage-counter accrual =====

    @Test
    @DisplayName("Upkeep adds a storage counter while the land is tapped")
    void upkeepAddsStorageCounterWhileTapped() {
        Permanent trees = harness.addToBattlefieldAndReturn(player1, new HollowTrees());
        trees.tap();

        // player2 ends their turn; on player1's untap step decline to untap (keep it tapped),
        // then the upkeep trigger sees a tapped land and puts a storage counter on it.
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // cascade into player1's untap → "Untap Hollow Trees?" prompt
        harness.handleMayAbilityChosen(player1, false); // choose NOT to untap
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // untap → upkeep, trigger onto the stack
        harness.passBothPriorities(); // resolve the trigger

        assertThat(trees.getCounterCount(CounterType.STORAGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Upkeep adds no storage counter while the land is untapped")
    void upkeepAddsNoCounterWhileUntapped() {
        Permanent trees = harness.addToBattlefieldAndReturn(player1, new HollowTrees());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep
        harness.passBothPriorities(); // resolve any trigger (intervening-if is false)

        assertThat(trees.getCounterCount(CounterType.STORAGE)).isZero();
    }

    // ===== Mana ability =====

    @Test
    @DisplayName("Removing all storage counters adds that much green mana")
    void removingAllCountersAddsThatMuchGreen() {
        Permanent trees = addTreesWithCounters(3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "3");

        assertThat(greenMana()).isEqualTo(3);
        assertThat(trees.getCounterCount(CounterType.STORAGE)).isZero();
        assertThat(trees.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Removing fewer counters than present keeps the rest")
    void removingSomeCountersKeepsTheRest() {
        Permanent trees = addTreesWithCounters(3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "1");

        assertThat(greenMana()).isEqualTo(1);
        assertThat(trees.getCounterCount(CounterType.STORAGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removing zero counters produces no mana but still taps the land")
    void removingZeroCountersProducesNoMana() {
        Permanent trees = addTreesWithCounters(3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "0");

        assertThat(greenMana()).isZero();
        assertThat(trees.getCounterCount(CounterType.STORAGE)).isEqualTo(3);
        assertThat(trees.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating with no storage counters produces no mana and no choice")
    void activatingWithNoCountersProducesNoMana() {
        Permanent trees = addTreesWithCounters(0);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(greenMana()).isZero();
        assertThat(trees.isTapped()).isTrue();
    }

    // ===== Helpers =====

    private Permanent addTreesWithCounters(int counters) {
        Permanent trees = harness.addToBattlefieldAndReturn(player1, new HollowTrees());
        trees.setSummoningSick(false);
        if (counters > 0) {
            trees.setCounterCount(CounterType.STORAGE, counters);
        }
        return trees;
    }

    private int greenMana() {
        return gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN);
    }
}
