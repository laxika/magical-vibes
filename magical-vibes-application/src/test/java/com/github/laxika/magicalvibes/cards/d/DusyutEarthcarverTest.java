package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(DusyutEarthcarver.class)
class DusyutEarthcarverTest extends BaseCardTest {

    private static final String COUNTERS = "Put 3 +1/+1 counters on this permanent";
    private static final String SPIRIT = "Create a 3/3 white Spirit creature token";

    @Test
    void enteringCanPutCountersOnDusyutEarthcarver() {
        harness.setHand(player1, List.of(new DusyutEarthcarver()));
        addManaToCast();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent dusyutEarthcarver = findPermanent(player1, "Dusyut Earthcarver");

        harness.passBothPriorities();
        harness.handleListChoice(player1, COUNTERS);

        assertThat(dusyutEarthcarver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    @Test
    void enteringCanCreateASpirit() {
        harness.setHand(player1, List.of(new DusyutEarthcarver()));
        addManaToCast();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, SPIRIT);

        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(spirit.getCard().isToken()).isTrue();
        assertThat(spirit.getCard().getPower()).isEqualTo(3);
        assertThat(spirit.getCard().getToughness()).isEqualTo(3);
    }

    private void addManaToCast() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
