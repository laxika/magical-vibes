package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.Skinrender;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TatterkiteTest extends BaseCardTest {

    @Test
    @DisplayName("Tatterkite can't have -1/-1 counters put on it by Skinrender ETB")
    void cantHaveMinusOneMinusOneCountersFromSkinrender() {
        harness.addToBattlefield(player1, new Tatterkite());
        UUID tatterkiteId = harness.getPermanentId(player1, "Tatterkite");

        harness.setHand(player2, List.of(new Skinrender()));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player2);

        harness.getGameService().playCard(gd, player2, 0, 0, tatterkiteId, null);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        Permanent tatterkite = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Tatterkite"))
                .findFirst().orElseThrow();
        assertThat(tatterkite.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(tatterkite.getEffectivePower()).isEqualTo(2);
        assertThat(tatterkite.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Query service reports Tatterkite can't have counters")
    void cantHaveCountersReported() {
        harness.addToBattlefield(player1, new Tatterkite());

        Permanent tatterkite = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Tatterkite"))
                .findFirst().orElseThrow();
        assertThat(gqs.cantHaveCounters(gd, tatterkite)).isTrue();
    }
}
