package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Venomcrawler.class, GrizzlyBears.class, Shock.class})
class VenomcrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when an opponent's creature dies")
    void putsCounterWhenOpponentCreatureDies() {
        Permanent venomcrawler = harness.addToBattlefieldAndReturn(player1, new Venomcrawler());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        killWithShock(player1, bears);

        assertThat(venomcrawler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when an ally creature dies")
    void putsCounterWhenAllyCreatureDies() {
        Permanent venomcrawler = harness.addToBattlefieldAndReturn(player1, new Venomcrawler());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        killWithShock(player1, bears);

        assertThat(venomcrawler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void killWithShock(com.github.laxika.magicalvibes.model.Player caster, Permanent target) {
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castInstant(caster, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
