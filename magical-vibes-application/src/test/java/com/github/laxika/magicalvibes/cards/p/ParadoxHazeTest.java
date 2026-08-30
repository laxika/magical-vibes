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
    @DisplayName("Paradox Haze does not trigger when the upkeep is skipped")
    void doesNotTriggerWhenUpkeepIsSkipped() {
        harness.addToBattlefieldAndReturn(player1, new EonHub()).tap();
        Permanent clock = placeClock(player2);
        placeHazeOnPlayer(player1, player2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.PRECOMBAT_MAIN);
        assertThat(clock.getCounterCount(CounterType.DOOM)).isZero();
    }

    private Permanent placeClock(Player player) {
        return harness.addToBattlefieldAndReturn(player, new ArmageddonClock());
    }

    private Permanent placeHazeOnPlayer(Player controller, Player enchantedPlayer) {
        Permanent haze = new Permanent(new ParadoxHaze());
        haze.setAttachedTo(enchantedPlayer.getId());
        gd.playerBattlefields.get(controller.getId()).add(haze);
        return haze;
    }

    private void advanceUntilDoomCounters(Player activePlayer, Permanent clock, int expectedCount) {
        harness.setLibrary(activePlayer, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();

        int attempts = 0;
        while (clock.getCounterCount(CounterType.DOOM) < expectedCount && attempts++ < 20) {
            harness.passBothPriorities();
        }

        assertThat(clock.getCounterCount(CounterType.DOOM)).isEqualTo(expectedCount);
    }
}
