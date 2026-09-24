package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ShalaisAcolyte.class)
class ShalaisAcolyteTest extends BaseCardTest {

    @Test
    @DisplayName("Without kicker, Shalai's Acolyte enters without +1/+1 counters")
    void castWithoutKicker() {
        harness.setHand(player1, List.of(new ShalaisAcolyte()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent acolyte = findAcolyte();
        assertThat(acolyte.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("With kicker, Shalai's Acolyte enters with two +1/+1 counters")
    void castWithKicker() {
        harness.setHand(player1, List.of(new ShalaisAcolyte()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        Permanent acolyte = findAcolyte();
        assertThat(acolyte.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private Permanent findAcolyte() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof ShalaisAcolyte)
                .findFirst()
                .orElseThrow();
    }
}
