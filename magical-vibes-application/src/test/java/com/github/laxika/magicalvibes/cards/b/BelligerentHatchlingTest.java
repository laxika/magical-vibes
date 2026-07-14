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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BelligerentHatchlingTest extends BaseCardTest {

    // ===== ETB: enters with four -1/-1 counters =====

    @Test
    @DisplayName("Enters the battlefield with four -1/-1 counters (6/6 becomes 2/2)")
    void entersWithFourMinusCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BelligerentHatchling()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB effect

        Permanent hatchling = findHatchling(player1);
        assertThat(hatchling.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(4);
        assertThat(hatchling.getEffectivePower()).isEqualTo(2);
        assertThat(hatchling.getEffectiveToughness()).isEqualTo(2);
    }

    // ===== Spell-cast counter removal =====

    @Test
    @DisplayName("Casting a red spell removes a -1/-1 counter")
    void redSpellRemovesCounter() {
        Permanent hatchling = addReadyHatchling(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
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

        harness.setHand(player1, List.of(new SuntailHawk()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
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

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).noneMatch(e -> e.getCard().getName().equals("Belligerent Hatchling"));
        assertThat(hatchling.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(4);
    }

    // ===== Helpers =====

    private Permanent addReadyHatchling(Player player) {
        BelligerentHatchling card = new BelligerentHatchling();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        perm.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 4);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent findHatchling(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Belligerent Hatchling"))
                .findFirst().orElseThrow();
    }
}
