package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SandskitterOutrider.class)
class SandskitterOutriderTest extends BaseCardTest {

    @Test
    @DisplayName("When Sandskitter Outrider enters, enduring with counters puts two +1/+1 counters on it")
    void enduresWithCounters() {
        castOutrider();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Put 2 +1/+1 counters on this permanent");

        Permanent outrider = findPermanent(player1, "Sandskitter Outrider");
        assertThat(outrider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    @Test
    @DisplayName("When Sandskitter Outrider enters, enduring with a token creates a 2/2 Spirit")
    void enduresWithSpiritToken() {
        castOutrider();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Create a 2/2 white Spirit creature token");

        Permanent outrider = findPermanent(player1, "Sandskitter Outrider");
        assertThat(outrider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        Permanent spirit = findPermanents(player1, "Spirit").getFirst();
        assertThat(spirit.getCard().getPower()).isEqualTo(2);
        assertThat(spirit.getCard().getToughness()).isEqualTo(2);
    }

    private void castOutrider() {
        harness.setHand(player1, List.of(new SandskitterOutrider()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }
}
