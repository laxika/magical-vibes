package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DwarvenHold.class})
class DwarvenHoldTest extends BaseCardTest {
    @Test
    @DisplayName("Dwarven Hold enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new DwarvenHold()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Dwarven Hold").isTapped()).isTrue();
    }
    @Test
    @DisplayName("Upkeep adds a storage counter while the land is tapped")
    void upkeepAddsStorageCounterWhileTapped() {
        Permanent hold = harness.addToBattlefieldAndReturn(player1, new DwarvenHold());
        hold.tap();

        // player2 ends their turn; on player1's untap step decline to untap (keep it tapped),
        // then the upkeep trigger sees a tapped land and puts a storage counter on it.
        beginPlayer1UntapChoice();
        harness.handleMayAbilityChosen(player1, false); // choose NOT to untap
        harness.passUntil(player1, TurnStep.UPKEEP); // untap → upkeep, trigger onto the stack
        resolveAllTriggers();

        assertThat(hold.getCounterCount(CounterType.STORAGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Controller may choose to untap Dwarven Hold")
    void controllerMayChooseToUntap() {
        Permanent hold = harness.addToBattlefieldAndReturn(player1, new DwarvenHold());
        hold.tap();

        beginPlayer1UntapChoice();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(hold.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Upkeep adds no storage counter while the land is untapped")
    void upkeepAddsNoCounterWhileUntapped() {
        Permanent hold = harness.addToBattlefieldAndReturn(player1, new DwarvenHold());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(hold.getCounterCount(CounterType.STORAGE)).isZero();
    }

    @Test
    @DisplayName("Untapped Dwarven Hold does not trigger its upkeep ability")
    void untappedLandDoesNotTriggerAtUpkeep() {
        harness.addToBattlefieldAndReturn(player1, new DwarvenHold());

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Upkeep trigger occurs only during Dwarven Hold's controller's upkeep")
    void upkeepDoesNotTriggerDuringOpponentsUpkeep() {
        Permanent hold = harness.addToBattlefieldAndReturn(player1, new DwarvenHold());
        hold.tap();

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(hold.getCounterCount(CounterType.STORAGE)).isZero();
    }

    @Test
    @DisplayName("Upkeep trigger does nothing if Dwarven Hold becomes untapped before resolution")
    void upkeepTriggerDoesNothingIfHoldBecomesUntappedBeforeResolution() {
        Permanent hold = harness.addToBattlefieldAndReturn(player1, new DwarvenHold());
        hold.tap();

        beginPlayer1UntapChoice();
        harness.handleMayAbilityChosen(player1, false);
        harness.passUntil(player1, TurnStep.UPKEEP);

        assertThat(gd.stack).hasSize(1);
        hold.untap();
        resolveAllTriggers();

        assertThat(hold.getCounterCount(CounterType.STORAGE)).isZero();
    }
    @Test
    @DisplayName("Removing all storage counters adds that much red mana")
    void removingAllCountersAddsThatMuchRed() {
        Permanent hold = addHoldWithCounters(3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "3");

        assertThat(redMana()).isEqualTo(3);
        assertThat(hold.getCounterCount(CounterType.STORAGE)).isZero();
        assertThat(hold.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Removing fewer counters than present keeps the rest")
    void removingSomeCountersKeepsTheRest() {
        Permanent hold = addHoldWithCounters(3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "1");

        assertThat(redMana()).isEqualTo(1);
        assertThat(hold.getCounterCount(CounterType.STORAGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removing zero counters produces no mana but still taps the land")
    void removingZeroCountersProducesNoMana() {
        Permanent hold = addHoldWithCounters(3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "0");

        assertThat(redMana()).isZero();
        assertThat(hold.getCounterCount(CounterType.STORAGE)).isEqualTo(3);
        assertThat(hold.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating with no storage counters produces no mana and no choice")
    void activatingWithNoCountersProducesNoMana() {
        Permanent hold = addHoldWithCounters(0);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(redMana()).isZero();
        assertThat(hold.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The mana ability cannot be activated while Dwarven Hold is tapped")
    void cannotActivateManaAbilityWhileTapped() {
        Permanent hold = addHoldWithCounters(3);
        hold.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }
    private void beginPlayer1UntapChoice() {
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.UNTAP);
    }

    private Permanent addHoldWithCounters(int counters) {
        Permanent hold = harness.addToBattlefieldAndReturn(player1, new DwarvenHold());
        hold.setSummoningSick(false);
        if (counters > 0) {
            hold.setCounterCount(CounterType.STORAGE, counters);
        }
        return hold;
    }

    private int redMana() {
        return gd.playerManaPools.get(player1.getId()).get(ManaColor.RED);
    }
}
