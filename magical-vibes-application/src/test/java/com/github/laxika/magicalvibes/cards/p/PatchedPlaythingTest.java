package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(PatchedPlaything.class)
class PatchedPlaythingTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two -1/-1 counters when cast from hand")
    void entersWithCountersWhenCastFromHand() {
        harness.setHand(player1, List.of(new PatchedPlaything()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent plaything = findPermanent(player1, "Patched Plaything");
        assertThat(plaything.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Enters without counters when put onto the battlefield")
    void entersWithoutCountersWhenNotCast() {
        Permanent plaything = harness.addToBattlefieldAndReturn(player1, new PatchedPlaything());

        assertThat(plaything.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }
}
