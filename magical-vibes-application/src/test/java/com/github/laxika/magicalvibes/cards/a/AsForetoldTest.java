package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AsForetoldTest extends BaseCardTest {

    // ===== Free cast: mana value within the time-counter cap =====

    @Test
    @DisplayName("A spell with mana value at most the time-counter count can be cast for {0}")
    void castsQualifyingSpellForFree() {
        Permanent asForetold = harness.addToBattlefieldAndReturn(player1, new AsForetold());
        asForetold.setCounterCount(CounterType.TIME, 2);
        // Grizzly Bears costs {1}{G} (mana value 2). With 2 time counters it may be cast for {0}.
        harness.setHand(player1, List.of(new GrizzlyBears()));
        // No mana added — it should still be castable.

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Casting for {0} spends no mana")
    void freeCastSpendsNoMana() {
        Permanent asForetold = harness.addToBattlefieldAndReturn(player1, new AsForetold());
        asForetold.setCounterCount(CounterType.TIME, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(5);
    }

    // ===== Mana-value cap enforced =====

    @Test
    @DisplayName("A spell with mana value above the time-counter count cannot be cast for {0}")
    void spellAboveCounterCapIsNotFree() {
        Permanent asForetold = harness.addToBattlefieldAndReturn(player1, new AsForetold());
        asForetold.setCounterCount(CounterType.TIME, 1);
        // Grizzly Bears (mana value 2) exceeds the single time counter, so it is not free.
        harness.setHand(player1, List.of(new GrizzlyBears()));
        // No mana — casting must fail.

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Once each turn =====

    @Test
    @DisplayName("Only one spell per turn may be cast for {0}")
    void freeCastLimitedToOncePerTurn() {
        Permanent asForetold = harness.addToBattlefieldAndReturn(player1, new AsForetold());
        asForetold.setCounterCount(CounterType.TIME, 1);
        // Opt costs {U} (mana value 1); both qualify for the {0} cost.
        harness.setHand(player1, List.of(new Opt(), new Opt()));
        // No mana — only the first cast can be free.

        harness.castInstant(player1, 0);
        assertThat(gd.stack).hasSize(1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Upkeep trigger =====

    @Test
    @DisplayName("At the beginning of the controller's upkeep, a time counter is added")
    void upkeepTriggerAddsTimeCounter() {
        Permanent asForetold = harness.addToBattlefieldAndReturn(player1, new AsForetold());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // UNTAP -> UPKEEP fires the upkeep trigger
        harness.passBothPriorities(); // resolve PutCountersOnSelfEffect

        assertThat(asForetold.getCounterCount(CounterType.TIME)).isEqualTo(1);
    }
}
