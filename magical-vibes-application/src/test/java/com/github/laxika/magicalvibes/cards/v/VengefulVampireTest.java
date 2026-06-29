package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VengefulVampireTest extends BaseCardTest {

    @Test
    @DisplayName("Undying returns Vengeful Vampire with a +1/+1 counter when it dies with no counters")
    void undyingReturnsWithCounter() {
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new VengefulVampire());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, vampire.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent returnedVampire = findPermanent(player1, "Vengeful Vampire");
        assertThat(returnedVampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Vengeful Vampire"));
    }

    @Test
    @DisplayName("Undying does not return Vengeful Vampire when it died with a +1/+1 counter")
    void undyingDoesNotReturnWithCounter() {
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new VengefulVampire());
        vampire.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, vampire.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Vengeful Vampire"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Vengeful Vampire"));
        assertThat(gd.stack).isEmpty();
    }
}
