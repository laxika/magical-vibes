package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VoraciousWurmTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with no counters when controller has gained no life this turn")
    void entersWithNoCountersWhenNoLifeGained() {
        harness.setHand(player1, List.of(new VoraciousWurm()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent wurm = findWurm(player1);
        assertThat(wurm).isNotNull();
        assertThat(wurm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Enters with counters equal to life gained this turn by controller")
    void entersWithCountersEqualToLifeGained() {
        gd.lifeGainedThisTurn.put(player1.getId(), 5);

        harness.setHand(player1, List.of(new VoraciousWurm()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent wurm = findWurm(player1);
        assertThat(wurm).isNotNull();
        assertThat(wurm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not count opponent's life gained this turn")
    void ignoresOpponentLifeGained() {
        gd.lifeGainedThisTurn.put(player2.getId(), 7);

        harness.setHand(player1, List.of(new VoraciousWurm()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent wurm = findWurm(player1);
        assertThat(wurm).isNotNull();
        assertThat(wurm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    private Permanent findWurm(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Voracious Wurm"))
                .findFirst().orElse(null);
    }
}
