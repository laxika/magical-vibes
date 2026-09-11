package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GhastlyGloomhunter.class})
class GhastlyGloomhunterTest extends BaseCardTest {

    @Test
    void entersWithoutCountersWhenNotKicked() {
        harness.setHand(player1, List.of(new GhastlyGloomhunter()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent gloomhunter = findPermanent(player1, "Ghastly Gloomhunter");
        assertThat(gloomhunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void entersWithTwoCountersWhenKicked() {
        harness.setHand(player1, List.of(new GhastlyGloomhunter()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        Permanent gloomhunter = findPermanent(player1, "Ghastly Gloomhunter");
        assertThat(gloomhunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
