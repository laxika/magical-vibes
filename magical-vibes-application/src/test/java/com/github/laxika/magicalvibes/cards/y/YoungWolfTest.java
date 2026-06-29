package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class YoungWolfTest extends BaseCardTest {

    @Test
    @DisplayName("Undying returns Young Wolf with a +1/+1 counter when it dies with no counters")
    void undyingReturnsWithCounter() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new YoungWolf());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, wolf.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent returnedWolf = findPermanent(player1, "Young Wolf");
        assertThat(returnedWolf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Young Wolf"));
    }

    @Test
    @DisplayName("Undying does not return Young Wolf when it died with a +1/+1 counter")
    void undyingDoesNotReturnWithCounter() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new YoungWolf());
        wolf.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, wolf.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Young Wolf"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Young Wolf"));
        assertThat(gd.stack).isEmpty();
    }
}
