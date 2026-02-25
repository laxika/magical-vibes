package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.GiveEachPlayerPoisonCountersEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IchorRatsTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has ETB effect that gives each player a poison counter")
    void hasCorrectEffects() {
        IchorRats card = new IchorRats();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(GiveEachPlayerPoisonCountersEffect.class);
        GiveEachPlayerPoisonCountersEffect effect =
                (GiveEachPlayerPoisonCountersEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.amount()).isEqualTo(1);
    }

    // ===== ETB: each player gets a poison counter =====

    @Test
    @DisplayName("Casting Ichor Rats gives each player a poison counter")
    void etbGivesEachPlayerPoisonCounter() {
        harness.setHand(player1, List.of(new IchorRats()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        int p1PoisonBefore = gd.playerPoisonCounters.getOrDefault(player1.getId(), 0);
        int p2PoisonBefore = gd.playerPoisonCounters.getOrDefault(player2.getId(), 0);

        harness.castCreature(player1, 0);
        // Resolve creature spell (ETB triggers go on stack)
        harness.passBothPriorities();
        // Resolve ETB trigger
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0))
                .isEqualTo(p1PoisonBefore + 1);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0))
                .isEqualTo(p2PoisonBefore + 1);
    }

    @Test
    @DisplayName("Ichor Rats enters the battlefield when cast")
    void castingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new IchorRats()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        // Resolve ETB trigger
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ichor Rats");
    }

    @Test
    @DisplayName("Multiple Ichor Rats ETBs accumulate poison counters")
    void multipleEtbsAccumulatePoison() {
        harness.setHand(player1, List.of(new IchorRats()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);

        // Cast a second Ichor Rats
        harness.setHand(player1, List.of(new IchorRats()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(2);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }
}
