package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JacesIngenuity;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StormkeldProwler.class, JacesIngenuity.class, GrizzlyBears.class})
class StormkeldProwlerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell with mana value 5 or greater puts two +1/+1 counters on Stormkeld Prowler")
    void castingHighManaValueSpellPutsTwoCountersOnProwler() {
        Permanent prowler = addCreatureReady(player1, new StormkeldProwler());
        harness.setHand(player1, List.of(new JacesIngenuity()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(prowler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting a spell with mana value less than 5 does not trigger Stormkeld Prowler")
    void castingLowManaValueSpellDoesNotPutCountersOnProwler() {
        Permanent prowler = addCreatureReady(player1, new StormkeldProwler());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(prowler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
