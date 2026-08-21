package com.github.laxika.magicalvibes.cards.i;

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

@CardUsed(IcatianStore.class)
class IcatianStoreTest extends BaseCardTest {

    @Test
    @DisplayName("Icatian Store enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new IcatianStore()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Icatian Store").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Upkeep adds a storage counter while the land is tapped")
    void upkeepAddsStorageCounterWhileTapped() {
        Permanent store = harness.addToBattlefieldAndReturn(player1, new IcatianStore());
        store.tap();

        // player2 ends their turn; on player1's untap step decline to untap (keep it tapped),
        // then the upkeep trigger sees a tapped land and puts a storage counter on it.
        beginPlayer1UntapChoice();
        harness.handleMayAbilityChosen(player1, false);
        harness.passUntil(player1, TurnStep.UPKEEP);
        resolveAllTriggers();

        assertThat(store.getCounterCount(CounterType.STORAGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Controller may choose to untap Icatian Store")
    void controllerMayChooseToUntap() {
        Permanent store = harness.addToBattlefieldAndReturn(player1, new IcatianStore());
        store.tap();

        beginPlayer1UntapChoice();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(store.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Upkeep adds no storage counter while the land is untapped")
    void upkeepAddsNoCounterWhileUntapped() {
        Permanent store = harness.addToBattlefieldAndReturn(player1, new IcatianStore());

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(store.getCounterCount(CounterType.STORAGE)).isZero();
    }

    @Test
    @DisplayName("Untapped Icatian Store does not put an upkeep ability on the stack")
    void untappedStoreDoesNotTrigger() {
        harness.addToBattlefieldAndReturn(player1, new IcatianStore());

        advanceToUpkeep(player1);

        assertThat(gameLogContains("Icatian Store's upkeep ability triggers.")).isFalse();
    }

    @Test
    @DisplayName("Upkeep does not add a storage counter during an opponent's upkeep")
    void upkeepDoesNotAddCounterDuringOpponentsUpkeep() {
        Permanent store = harness.addToBattlefieldAndReturn(player1, new IcatianStore());
        store.tap();

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(store.getCounterCount(CounterType.STORAGE)).isZero();
    }

    @Test
    @DisplayName("Upkeep trigger does nothing if Icatian Store becomes untapped before resolution")
    void upkeepTriggerDoesNothingIfStoreBecomesUntappedBeforeResolution() {
        Permanent store = harness.addToBattlefieldAndReturn(player1, new IcatianStore());
        store.tap();

        beginPlayer1UntapChoice();
        harness.handleMayAbilityChosen(player1, false);
        harness.passUntil(player1, TurnStep.UPKEEP);

        assertThat(gd.stack).hasSize(1);
        store.untap();
        harness.passBothPriorities();

        assertThat(store.getCounterCount(CounterType.STORAGE)).isZero();
    }

    @Test
    @DisplayName("Removing all storage counters adds that much white mana")
    void removingAllCountersAddsThatMuchWhite() {
        Permanent store = addStoreWithCounters(3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "3");

        assertThat(whiteMana()).isEqualTo(3);
        assertThat(store.getCounterCount(CounterType.STORAGE)).isZero();
        assertThat(store.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Removing fewer counters than present keeps the rest")
    void removingSomeCountersKeepsTheRest() {
        Permanent store = addStoreWithCounters(3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "1");

        assertThat(whiteMana()).isEqualTo(1);
        assertThat(store.getCounterCount(CounterType.STORAGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removing zero counters produces no mana but still taps the land")
    void removingZeroCountersProducesNoMana() {
        Permanent store = addStoreWithCounters(3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "0");

        assertThat(whiteMana()).isZero();
        assertThat(store.getCounterCount(CounterType.STORAGE)).isEqualTo(3);
        assertThat(store.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating with no storage counters produces no mana and no choice")
    void activatingWithNoCountersProducesNoMana() {
        Permanent store = addStoreWithCounters(0);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(whiteMana()).isZero();
        assertThat(store.isTapped()).isTrue();
    }

    private void beginPlayer1UntapChoice() {
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.UNTAP);
    }

    private Permanent addStoreWithCounters(int counters) {
        Permanent store = harness.addToBattlefieldAndReturn(player1, new IcatianStore());
        if (counters > 0) {
            store.setCounterCount(CounterType.STORAGE, counters);
        }
        return store;
    }

    private int whiteMana() {
        return gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE);
    }
}
