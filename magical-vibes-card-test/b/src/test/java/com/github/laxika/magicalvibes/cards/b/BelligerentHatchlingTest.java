package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
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

@CardUsed({BelligerentHatchling.class, HillGiant.class, SuntailHawk.class, GrizzlyBears.class})
class BelligerentHatchlingTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with four -1/-1 counters")
    void entersWithFourMinusCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BelligerentHatchling()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        Permanent hatchling = findHatchling(player1);
        assertThat(hatchling.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(4);
        assertThat(hatchling.getEffectivePower()).isEqualTo(2);
        assertThat(hatchling.getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Casting a red spell removes a -1/-1 counter")
    void redSpellRemovesCounter() {
        Permanent hatchling = addReadyHatchling(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new HillGiant(), "{3}{R}");
        harness.passBothPriorities(); // resolve the removal trigger

        assertThat(hatchling.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Casting a white spell removes a -1/-1 counter")
    void whiteSpellRemovesCounter() {
        Permanent hatchling = addReadyHatchling(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new SuntailHawk(), "{W}");
        harness.passBothPriorities(); // resolve the removal trigger

        assertThat(hatchling.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Casting a non-red non-white spell does not remove a counter")
    void greenSpellDoesNotRemoveCounter() {
        Permanent hatchling = addReadyHatchling(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        assertThat(gd.stack).noneMatch(e -> e.getCard().getName().equals("Belligerent Hatchling"));
        assertThat(hatchling.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(4);
    }

    @Test
    void redWhiteSpellCreatesTwoIndependentRemovalTriggers() {
        Permanent hatchling = addReadyHatchling(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BelligerentHatchling()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(3);
        assertThat(hatchling.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(4);
        harness.passBothPriorities();
        assertThat(hatchling.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        harness.passBothPriorities();
        assertThat(hatchling.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void opponentsRedWhiteSpellDoesNotTrigger() {
        Permanent hatchling = addReadyHatchling(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new BelligerentHatchling()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(hatchling.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void removalTriggerDoesNothingWhenNoMinusCountersRemain() {
        Permanent hatchling = addReadyHatchling(player1);
        hatchling.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 0);
        hatchling.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new SuntailHawk(), "{W}");
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(hatchling.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(hatchling.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent addReadyHatchling(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new BelligerentHatchling());
        perm.setSummoningSick(false);
        perm.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 4);
        return perm;
    }

    private Permanent findHatchling(Player player) {
        return findPermanent(player, "Belligerent Hatchling");
    }
}
