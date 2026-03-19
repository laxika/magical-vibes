package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.EnterWithPlusOnePlusOneCountersIfKickedEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BalothGorgerTest extends BaseCardTest {

    // ===== Card setup =====

    @Test
    @DisplayName("Has KickerEffect with cost {4}")
    void hasKickerEffect() {
        BalothGorger card = new BalothGorger();

        assertThat(card.getEffects(EffectSlot.STATIC))
                .anyMatch(e -> e instanceof KickerEffect ke && ke.cost().equals("{4}"));
    }

    @Test
    @DisplayName("Has EnterWithPlusOnePlusOneCountersIfKickedEffect with count 3")
    void hasKickedETBEffect() {
        BalothGorger card = new BalothGorger();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(EnterWithPlusOnePlusOneCountersIfKickedEffect.class);
        var effect = (EnterWithPlusOnePlusOneCountersIfKickedEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.count()).isEqualTo(3);
    }

    // ===== Casting without kicker =====

    @Test
    @DisplayName("Cast without kicker — enters as 4/4 with no counters")
    void castWithoutKicker() {
        harness.setHand(player1, List.of(new BalothGorger()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 2); // 2 generic

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent gorger = findGorger(player1);
        assertThat(gorger).isNotNull();
        assertThat(gorger.getPlusOnePlusOneCounters()).isEqualTo(0);
    }

    // ===== Casting with kicker =====

    @Test
    @DisplayName("Cast with kicker — enters as 7/7 with three +1/+1 counters")
    void castWithKicker() {
        harness.setHand(player1, List.of(new BalothGorger()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 6); // 2 generic + 4 kicker

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        Permanent gorger = findGorger(player1);
        assertThat(gorger).isNotNull();
        assertThat(gorger.getPlusOnePlusOneCounters()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cast with kicker but not enough mana — throws exception")
    void castWithKickerNotEnoughMana() {
        harness.setHand(player1, List.of(new BalothGorger()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 3); // only 2 generic + 1 kicker (need 4)

        assertThatThrownBy(() -> harness.castKickedCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private Permanent findGorger(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Baloth Gorger"))
                .findFirst().orElse(null);
    }
}
