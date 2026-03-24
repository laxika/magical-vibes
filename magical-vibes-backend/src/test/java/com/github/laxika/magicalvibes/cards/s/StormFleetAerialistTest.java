package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.EnterWithPlusOnePlusOneCountersIfRaidEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StormFleetAerialistTest extends BaseCardTest {

    // ===== Card setup =====

    @Test
    @DisplayName("Has EnterWithPlusOnePlusOneCountersIfRaidEffect with count 1")
    void hasRaidETBCounterEffect() {
        StormFleetAerialist card = new StormFleetAerialist();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(EnterWithPlusOnePlusOneCountersIfRaidEffect.class);
        var effect = (EnterWithPlusOnePlusOneCountersIfRaidEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.count()).isEqualTo(1);
    }

    // ===== Casting without raid =====

    @Test
    @DisplayName("Cast without raid — enters as 1/2 with no counters")
    void castWithoutRaid() {
        harness.setHand(player1, List.of(new StormFleetAerialist()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent aerialist = findAerialist(player1);
        assertThat(aerialist).isNotNull();
        assertThat(aerialist.getPlusOnePlusOneCounters()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cast without raid — creature enters battlefield")
    void creatureEntersWithoutRaid() {
        harness.setHand(player1, List.of(new StormFleetAerialist()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Storm Fleet Aerialist"));
    }

    // ===== Casting with raid =====

    @Test
    @DisplayName("Cast with raid — enters as 2/3 with one +1/+1 counter")
    void castWithRaid() {
        markAttackedThisTurn();
        harness.setHand(player1, List.of(new StormFleetAerialist()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent aerialist = findAerialist(player1);
        assertThat(aerialist).isNotNull();
        assertThat(aerialist.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cast with raid — log message includes counter info")
    void castWithRaidLogMessage() {
        markAttackedThisTurn();
        harness.setHand(player1, List.of(new StormFleetAerialist()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log ->
                log.contains("Storm Fleet Aerialist") && log.contains("+1/+1 counter"));
    }

    // ===== No ETB trigger on the stack =====

    @Test
    @DisplayName("Raid counter is a replacement effect — no ETB trigger on stack")
    void raidCounterIsReplacementEffect() {
        markAttackedThisTurn();
        harness.setHand(player1, List.of(new StormFleetAerialist()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Opponent attacking does not count for your raid")
    void opponentAttackDoesNotCountForRaid() {
        gd.playersDeclaredAttackersThisTurn.add(player2.getId());
        harness.setHand(player1, List.of(new StormFleetAerialist()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent aerialist = findAerialist(player1);
        assertThat(aerialist).isNotNull();
        assertThat(aerialist.getPlusOnePlusOneCounters()).isEqualTo(0);
    }

    // ===== Helpers =====

    private void markAttackedThisTurn() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
    }

    private Permanent findAerialist(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Storm Fleet Aerialist"))
                .findFirst().orElse(null);
    }
}
