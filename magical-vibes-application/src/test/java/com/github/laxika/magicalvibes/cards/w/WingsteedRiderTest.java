package com.github.laxika.magicalvibes.cards.w;

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

class WingsteedRiderTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell that targets Wingsteed Rider puts a +1/+1 counter on it")
    void castingSpellThatTargetsRiderPutsCounterOnIt() {
        harness.addToBattlefield(player1, new WingsteedRider());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID riderId = harness.getPermanentId(player1, "Wingsteed Rider");
        harness.castInstant(player1, 0, riderId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent rider = findPermanent(player1, "Wingsteed Rider");
        assertThat(rider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A spell that targets a player does not trigger Wingsteed Rider")
    void targetingPlayerDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new WingsteedRider());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        Permanent rider = findPermanent(player1, "Wingsteed Rider");
        assertThat(rider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent's spell that targets Wingsteed Rider does not trigger it")
    void opponentsSpellDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new WingsteedRider());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        UUID riderId = harness.getPermanentId(player1, "Wingsteed Rider");
        harness.castInstant(player2, 0, riderId);
        harness.passBothPriorities();

        Permanent rider = findPermanent(player1, "Wingsteed Rider");
        assertThat(rider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
