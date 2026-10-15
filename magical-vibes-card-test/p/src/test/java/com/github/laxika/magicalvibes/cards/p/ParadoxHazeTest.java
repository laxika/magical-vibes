package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.ArmageddonClock;
import com.github.laxika.magicalvibes.cards.e.EonHub;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({ParadoxHaze.class, ArmageddonClock.class, EonHub.class, GrizzlyBears.class})
class ParadoxHazeTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Paradox Haze attaches it to the target player")
    void resolvingAttachesToTargetPlayer() {
        harness.setHand(player1, List.of(new ParadoxHaze()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Paradox Haze")
                        && p.isAttached()
                        && p.getAttachedTo().equals(player2.getId()));
    }

    @Test
    @DisplayName("Other upkeep triggers fire during the additional upkeep")
    void otherUpkeepTriggersFireDuringAdditionalUpkeep() {
        Permanent clock = placeClock(player2);
        placeHazeOnPlayer(player1, player2);

        advanceUntilDoomCounters(player2, clock, 2);

        assertThat(clock.getCounterCount(CounterType.DOOM)).isEqualTo(2);
    }

    @Test
    @DisplayName("Two Paradox Hazes create two additional upkeeps")
    void twoHazesCreateTwoAdditionalUpkeeps() {
        Permanent clock = placeClock(player2);
        placeHazeOnPlayer(player1, player2);
        placeHazeOnPlayer(player1, player2);

        advanceUntilDoomCounters(player2, clock, 3);

        assertThat(clock.getCounterCount(CounterType.DOOM)).isEqualTo(3);
    }

    @Test
    @DisplayName("The additional upkeep occurs before the draw step")
    void additionalUpkeepOccursBeforeDrawStep() {
        Permanent clock = placeClock(player2);
        placeHazeOnPlayer(player1, player2);
        gd.turnNumber = 2;

        advanceUntilDoomCounters(player2, clock, 2);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Only the enchanted player's upkeep gets an additional upkeep")
    void onlyEnchantedPlayerGetsAdditionalUpkeep() {
        Permanent clock = placeClock(player1);
        placeHazeOnPlayer(player1, player2);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        assertThat(clock.getCounterCount(CounterType.DOOM)).isEqualTo(1);
    }

    @Test
    @DisplayName("Paradox Haze does not trigger when the upkeep is skipped")
    void doesNotTriggerWhenUpkeepIsSkipped() {
        harness.addToBattlefield(player1, new EonHub());
        Permanent clock = placeClock(player2);
        placeHazeOnPlayer(player1, player2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.currentStep).isEqualTo(TurnStep.PRECOMBAT_MAIN);
        assertThat(clock.getCounterCount(CounterType.DOOM)).isZero();
    }

    @Test
    @DisplayName("Paradox Haze does not trigger at the beginning of the additional upkeep")
    void doesNotTriggerDuringAdditionalUpkeep() {
        placeHazeOnPlayer(player1, player2);
        advanceToUpkeep(player2);

        assertThat(gd.stack).hasSize(1);
        harness.withAutoStop(TurnStep.UPKEEP, () -> {
            harness.passBothPriorities();
            assertThat(gd.stack).isEmpty();
            harness.passBothPriorities();
        });

        assertThat(gd.currentStep).isEqualTo(TurnStep.UPKEEP);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Removing Paradox Haze after it triggers does not prevent the additional upkeep")
    void removingHazeDoesNotPreventAdditionalUpkeep() {
        Permanent clock = placeClock(player1);
        Permanent haze = placeHazeOnPlayer(player1, player1);
        advanceToUpkeep(player1);
        gd.playerBattlefields.get(player1.getId()).remove(haze);

        harness.withAutoStop(TurnStep.UPKEEP, () -> {
            for (int attempts = 0; !gd.stack.isEmpty() && attempts < 10; attempts++) {
                harness.passBothPriorities();
            }
            assertThat(gd.stack).isEmpty();
            harness.passBothPriorities();
            for (int attempts = 0; !gd.stack.isEmpty() && attempts < 10; attempts++) {
                harness.passBothPriorities();
            }
        });

        assertThat(gd.currentStep).isEqualTo(TurnStep.UPKEEP);
        assertThat(clock.getCounterCount(CounterType.DOOM)).isEqualTo(2);
    }

    private Permanent placeClock(Player player) {
        return harness.addToBattlefieldAndReturn(player, new ArmageddonClock());
    }

    private Permanent placeHazeOnPlayer(Player controller, Player enchantedPlayer) {
        Permanent haze = harness.addToBattlefieldAndReturn(controller, new ParadoxHaze());
        haze.setAttachedTo(enchantedPlayer.getId());
        return haze;
    }

    private void advanceUntilDoomCounters(Player activePlayer, Permanent clock, int expectedCount) {
        harness.setLibrary(activePlayer, List.of(new GrizzlyBears(), new GrizzlyBears()));
        advanceToUpkeep(activePlayer);

        int attempts = 0;
        while (clock.getCounterCount(CounterType.DOOM) < expectedCount && attempts++ < 20) {
            harness.passBothPriorities();
        }

        assertThat(clock.getCounterCount(CounterType.DOOM)).isEqualTo(expectedCount);
    }
}
