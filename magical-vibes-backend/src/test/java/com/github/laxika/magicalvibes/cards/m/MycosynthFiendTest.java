package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.BoostSelfPerOpponentPoisonCounterEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MycosynthFiendTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has STATIC BoostSelfPerOpponentPoisonCounterEffect(1, 1)")
    void hasCorrectEffect() {
        MycosynthFiend card = new MycosynthFiend();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(BoostSelfPerOpponentPoisonCounterEffect.class);

        BoostSelfPerOpponentPoisonCounterEffect boost =
                (BoostSelfPerOpponentPoisonCounterEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(boost.powerPerCounter()).isEqualTo(1);
        assertThat(boost.toughnessPerCounter()).isEqualTo(1);
    }

    // ===== No poison counters =====

    @Test
    @DisplayName("Base 2/2 when no opponent has poison counters")
    void basePowerToughnessWithNoPoisonCounters() {
        harness.addToBattlefield(player1, new MycosynthFiend());

        Permanent fiend = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, fiend)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, fiend)).isEqualTo(2);
    }

    // ===== Opponent has poison counters =====

    @Test
    @DisplayName("Gets +1/+1 per poison counter on opponent")
    void boostsPerOpponentPoisonCounter() {
        harness.addToBattlefield(player1, new MycosynthFiend());
        gd.playerPoisonCounters.put(player2.getId(), 3);

        Permanent fiend = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, fiend)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, fiend)).isEqualTo(5);
    }

    // ===== Controller's own poison counters don't count =====

    @Test
    @DisplayName("Controller's own poison counters do not boost")
    void controllerPoisonCountersDoNotCount() {
        harness.addToBattlefield(player1, new MycosynthFiend());
        gd.playerPoisonCounters.put(player1.getId(), 5);

        Permanent fiend = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, fiend)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, fiend)).isEqualTo(2);
    }

    // ===== Boost updates dynamically =====

    @Test
    @DisplayName("Boost updates when opponent gains more poison counters")
    void boostUpdatesDynamically() {
        harness.addToBattlefield(player1, new MycosynthFiend());

        Permanent fiend = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, fiend)).isEqualTo(2);

        gd.playerPoisonCounters.put(player2.getId(), 2);
        assertThat(gqs.getEffectivePower(gd, fiend)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, fiend)).isEqualTo(4);

        gd.playerPoisonCounters.put(player2.getId(), 7);
        assertThat(gqs.getEffectivePower(gd, fiend)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, fiend)).isEqualTo(9);
    }
}
