package com.github.laxika.magicalvibes.cards.w;

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

@CardUsed(WalkingArchive.class)
class WalkingArchiveTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a +1/+1 counter")
    void entersWithCounter() {
        Permanent archive = harness.enterBattlefieldAndReturn(player1, new WalkingArchive());

        assertThat(archive.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Activated ability puts a +1/+1 counter on Walking Archive")
    void activatedAbilityAddsCounter() {
        Permanent archive = harness.addToBattlefieldAndReturn(player1, new WalkingArchive());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(archive.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each player's upkeep makes that player draw for each counter")
    void activePlayerDrawsForEachCounter() {
        Permanent archive = harness.addToBattlefieldAndReturn(player1, new WalkingArchive());
        archive.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new WalkingArchive(), new WalkingArchive()));
        harness.setLibrary(player2, List.of(new WalkingArchive(), new WalkingArchive()));

        advanceToUpkeep(player2);
        harness.withAutoStop(TurnStep.UPKEEP, harness::passBothPriorities);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Upkeep trigger draws nothing when Walking Archive has no +1/+1 counters")
    void upkeepDrawsNothingWithoutCounters() {
        Permanent archive = harness.addToBattlefieldAndReturn(player1, new WalkingArchive());
        archive.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new WalkingArchive()));

        advanceToUpkeep(player2);
        harness.withAutoStop(TurnStep.UPKEEP, harness::passBothPriorities);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Upkeep trigger still draws while Walking Archive is tapped")
    void upkeepDrawsWhileTapped() {
        Permanent archive = harness.addToBattlefieldAndReturn(player1, new WalkingArchive());
        archive.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        archive.tap();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new WalkingArchive()));

        advanceToUpkeep(player2);
        harness.withAutoStop(TurnStep.UPKEEP, harness::passBothPriorities);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }
}
