package com.github.laxika.magicalvibes.cards.u;

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

class UntamedKavuTest extends BaseCardTest {

    // ===== Card setup =====

    @Test
    @DisplayName("Has KickerEffect with cost {3}")
    void hasKickerEffect() {
        UntamedKavu card = new UntamedKavu();

        assertThat(card.getEffects(EffectSlot.STATIC))
                .anyMatch(e -> e instanceof KickerEffect ke && ke.cost().equals("{3}"));
    }

    @Test
    @DisplayName("Has EnterWithPlusOnePlusOneCountersIfKickedEffect with count 3")
    void hasKickedETBEffect() {
        UntamedKavu card = new UntamedKavu();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(EnterWithPlusOnePlusOneCountersIfKickedEffect.class);
        var effect = (EnterWithPlusOnePlusOneCountersIfKickedEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.count()).isEqualTo(3);
    }

    // ===== Casting without kicker =====

    @Test
    @DisplayName("Cast without kicker — enters as 2/2 with no counters")
    void castWithoutKicker() {
        harness.setHand(player1, List.of(new UntamedKavu()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1); // 1 generic

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent kavu = findKavu(player1);
        assertThat(kavu).isNotNull();
        assertThat(kavu.getPlusOnePlusOneCounters()).isEqualTo(0);
    }

    // ===== Casting with kicker =====

    @Test
    @DisplayName("Cast with kicker — enters as 5/5 with three +1/+1 counters")
    void castWithKicker() {
        harness.setHand(player1, List.of(new UntamedKavu()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 4); // 1 generic + 3 kicker

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        Permanent kavu = findKavu(player1);
        assertThat(kavu).isNotNull();
        assertThat(kavu.getPlusOnePlusOneCounters()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cast with kicker but not enough mana — throws exception")
    void castWithKickerNotEnoughMana() {
        harness.setHand(player1, List.of(new UntamedKavu()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 2); // only 1 generic + 1 kicker (need 3)

        assertThatThrownBy(() -> harness.castKickedCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private Permanent findKavu(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Untamed Kavu"))
                .findFirst().orElse(null);
    }
}
