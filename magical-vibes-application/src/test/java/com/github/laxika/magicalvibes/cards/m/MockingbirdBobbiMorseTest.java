package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MockingbirdBobbiMorse.class, Shock.class})
class MockingbirdBobbiMorseTest extends BaseCardTest {

    @Test
    @DisplayName("When Mockingbird is dealt nonlethal damage, it gets a +1/+1 counter")
    void nonlethalDamageAddsCounter() {
        Permanent mockingbird = harness.addToBattlefieldAndReturn(player2, new MockingbirdBobbiMorse());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID mockingbirdId = mockingbird.getId();
        harness.castInstant(player1, 0, mockingbirdId);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(mockingbird);
        assertThat(mockingbird.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Lethal damage destroys Mockingbird before its trigger resolves")
    void lethalDamagePreventsCounter() {
        Permanent mockingbird = harness.addToBattlefieldAndReturn(player2, new MockingbirdBobbiMorse());
        mockingbird.setMarkedDamage(1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID mockingbirdId = harness.getPermanentId(player2, "Mockingbird, Bobbi Morse");
        harness.castInstant(player1, 0, mockingbirdId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Mockingbird, Bobbi Morse");
        resolveAllTriggers();
        harness.assertInGraveyard(player2, "Mockingbird, Bobbi Morse");
    }
}
