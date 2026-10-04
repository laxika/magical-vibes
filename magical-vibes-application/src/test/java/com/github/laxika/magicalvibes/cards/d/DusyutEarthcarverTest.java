package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DusyutEarthcarver.class, Murder.class})
class DusyutEarthcarverTest extends BaseCardTest {

    private static final String COUNTERS = "Put 3 +1/+1 counters on this permanent";
    private static final String SPIRIT = "Create a 3/3 white Spirit creature token";

    @Test
    void enteringCanPutCountersOnDusyutEarthcarver() {
        harness.castFromHand(player1, new DusyutEarthcarver(), "{5}{G}");
        harness.passBothPriorities();
        Permanent dusyutEarthcarver = findPermanent(player1, "Dusyut Earthcarver");

        harness.passBothPriorities();
        harness.handleListChoice(player1, COUNTERS);

        assertThat(dusyutEarthcarver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    @Test
    void enteringCanCreateASpirit() {
        harness.castFromHand(player1, new DusyutEarthcarver(), "{5}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, SPIRIT);

        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(spirit.getCard().isToken()).isTrue();
        assertThat(spirit.getCard().getPower()).isEqualTo(3);
        assertThat(spirit.getCard().getToughness()).isEqualTo(3);
    }

    @Test
    void createsSpiritWhenEarthcarverDiesBeforeEndureResolves() {
        harness.castFromHand(player1, new DusyutEarthcarver(), "{5}{G}");
        harness.passBothPriorities();
        Permanent earthcarver = findPermanent(player1, "Dusyut Earthcarver");

        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, earthcarver.getId());
        assertThat(findPermanents(player1, "Dusyut Earthcarver")).isEmpty();

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(spirit.getCard().isToken()).isTrue();
        assertThat(spirit.getCard().getPower()).isEqualTo(3);
        assertThat(spirit.getCard().getToughness()).isEqualTo(3);
        assertThat(findPermanents(player2, "Spirit")).isEmpty();
    }
}

