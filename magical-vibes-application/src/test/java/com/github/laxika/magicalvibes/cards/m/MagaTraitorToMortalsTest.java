package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MagaTraitorToMortalsTest extends BaseCardTest {

    @Test
    @DisplayName("Maga enters with X +1/+1 counters and makes the target player lose that much life")
    void entersWithCountersAndCausesLifeLoss() {
        harness.setHand(player1, List.of(new MagaTraitorToMortals()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castCreature(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        Permanent maga = findMaga();
        assertThat(maga.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);

        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("Maga's ETB life loss uses the counters on Maga when the ability resolves")
    void etbUsesCountersAtResolution() {
        harness.setHand(player1, List.of(new MagaTraitorToMortals()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castCreature(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        Permanent maga = findMaga();
        maga.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    private Permanent findMaga() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Maga, Traitor to Mortals"))
                .findFirst()
                .orElseThrow();
    }
}
