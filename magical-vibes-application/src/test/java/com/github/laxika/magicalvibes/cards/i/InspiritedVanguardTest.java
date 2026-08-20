package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InspiritedVanguardTest extends BaseCardTest {

    private static final String COUNTERS = "Put 2 +1/+1 counters on this permanent";
    private static final String SPIRIT = "Create a 2/2 white Spirit creature token";

    @Test
    void enteringCanPutCountersOnInspiritedVanguard() {
        Permanent vanguard = castVanguard();

        harness.passBothPriorities();
        harness.handleListChoice(player1, COUNTERS);

        assertThat(vanguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    @Test
    void enteringCanCreateASpirit() {
        castVanguard();

        harness.passBothPriorities();
        harness.handleListChoice(player1, SPIRIT);

        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(spirit.getCard().getPower()).isEqualTo(2);
        assertThat(spirit.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    void attackingCanEndure() {
        Permanent vanguard = addCreatureReady(player1, new InspiritedVanguard());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleListChoice(player1, COUNTERS);

        assertThat(vanguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private Permanent castVanguard() {
        harness.setHand(player1, List.of(new InspiritedVanguard()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        return findPermanent(player1, "Inspirited Vanguard");
    }
}
