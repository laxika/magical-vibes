package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LagonnaBandTrailblazerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell targeting Lagonna-Band Trailblazer puts a +1/+1 counter on it")
    void targetingTrailblazerAddsCounter() {
        harness.addToBattlefield(player1, new LagonnaBandTrailblazer());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID trailblazerId = harness.getPermanentId(player1, "Lagonna-Band Trailblazer");
        harness.castInstant(player1, 0, trailblazerId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent trailblazer = findPermanent(player1, "Lagonna-Band Trailblazer");
        assertThat(trailblazer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A spell targeting a player does not trigger Lagonna-Band Trailblazer")
    void targetingPlayerDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new LagonnaBandTrailblazer());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        Permanent trailblazer = findPermanent(player1, "Lagonna-Band Trailblazer");
        assertThat(trailblazer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
