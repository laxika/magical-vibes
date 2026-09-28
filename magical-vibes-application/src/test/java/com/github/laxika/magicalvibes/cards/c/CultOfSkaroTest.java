package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CultOfSkaro.class, GrizzlyBears.class})
class CultOfSkaroTest extends BaseCardTest {

    @Test
    void attackingResolvesExactlyOneRandomMode() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        Permanent cult = addCreatureReady(player1, new CultOfSkaro());
        declareAttackers(List.of(0));
        resolveAllTriggers();

        int resolvedModes = 0;
        if (cult.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) == 1) {
            resolvedModes++;
        }
        if (gd.playerHands.get(player1.getId()).size() == 2) {
            resolvedModes++;
        }
        if (countPermanents(player1, "Dalek") == 1) {
            resolvedModes++;
        }
        if (gd.getLife(player2.getId()) == 16) {
            resolvedModes++;
        }

        assertThat(resolvedModes)
                .withFailMessage("counter=%s, hand=%s, daleks=%s, opponentLife=%s",
                        cult.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE),
                        gd.playerHands.get(player1.getId()).size(),
                        countPermanents(player1, "Dalek"),
                        gd.getLife(player2.getId()))
                .isEqualTo(1);
    }
}
