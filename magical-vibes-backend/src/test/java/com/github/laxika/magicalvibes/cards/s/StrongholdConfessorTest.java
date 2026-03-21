package com.github.laxika.magicalvibes.cards.s;

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

class StrongholdConfessorTest extends BaseCardTest {

    // ===== Card setup =====

    @Test
    @DisplayName("Has KickerEffect with cost {3}")
    void hasKickerEffect() {
        StrongholdConfessor card = new StrongholdConfessor();

        assertThat(card.getEffects(EffectSlot.STATIC))
                .anyMatch(e -> e instanceof KickerEffect ke && ke.cost().equals("{3}"));
    }

    @Test
    @DisplayName("Has EnterWithPlusOnePlusOneCountersIfKickedEffect with count 2")
    void hasKickedETBEffect() {
        StrongholdConfessor card = new StrongholdConfessor();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(EnterWithPlusOnePlusOneCountersIfKickedEffect.class);
        var effect = (EnterWithPlusOnePlusOneCountersIfKickedEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.count()).isEqualTo(2);
    }

    // ===== Casting without kicker =====

    @Test
    @DisplayName("Cast without kicker — enters as 1/1 with no counters")
    void castWithoutKicker() {
        harness.setHand(player1, List.of(new StrongholdConfessor()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent confessor = findConfessor(player1);
        assertThat(confessor).isNotNull();
        assertThat(confessor.getPlusOnePlusOneCounters()).isEqualTo(0);
    }

    // ===== Casting with kicker =====

    @Test
    @DisplayName("Cast with kicker — enters as 3/3 with two +1/+1 counters")
    void castWithKicker() {
        harness.setHand(player1, List.of(new StrongholdConfessor()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 3); // 3 generic kicker

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        Permanent confessor = findConfessor(player1);
        assertThat(confessor).isNotNull();
        assertThat(confessor.getPlusOnePlusOneCounters()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cast with kicker but not enough mana — throws exception")
    void castWithKickerNotEnoughMana() {
        harness.setHand(player1, List.of(new StrongholdConfessor()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 2); // only 2 generic (need 3 for kicker)

        assertThatThrownBy(() -> harness.castKickedCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private Permanent findConfessor(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Stronghold Confessor"))
                .findFirst().orElse(null);
    }
}
