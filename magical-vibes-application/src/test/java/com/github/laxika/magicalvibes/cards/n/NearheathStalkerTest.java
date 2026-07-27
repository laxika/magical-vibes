package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NearheathStalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Undying returns Nearheath Stalker with a +1/+1 counter when it dies with no counters")
    void undyingReturnsWithCounter() {
        Permanent stalker = harness.addToBattlefieldAndReturn(player1, new NearheathStalker());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, stalker.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent returnedStalker = findPermanent(player1, "Nearheath Stalker");
        assertThat(returnedStalker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Nearheath Stalker");
    }

    @Test
    @DisplayName("Undying does not return Nearheath Stalker when it died with a +1/+1 counter")
    void undyingDoesNotReturnWithCounter() {
        Permanent stalker = harness.addToBattlefieldAndReturn(player1, new NearheathStalker());
        stalker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, stalker.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Nearheath Stalker");
        harness.assertInGraveyard(player1, "Nearheath Stalker");
        assertThat(gd.stack).isEmpty();
    }
}
