package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BatrocTheLeaper.class, GrizzlyBears.class})
class BatrocTheLeaperTest extends BaseCardTest {

    @Test
    @DisplayName("Without multikicker, Batroc enters without a counter and deals no damage")
    void withoutMultikicker() {
        castBatroc(List.of());

        Permanent batroc = findPermanent(player1, "Batroc the Leaper");
        assertThat(batroc.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("One multikicker payment adds a counter and deals Batroc's power to one target")
    void oneMultikickerPayment() {
        castBatroc(List.of("{2}"));

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        Permanent batroc = findPermanent(player1, "Batroc the Leaper");
        assertThat(batroc.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Two multikicker payments allow two targets and use Batroc's increased power")
    void twoMultikickerPayments() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castBatroc(List.of("{2}", "{2}"));

        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        Permanent batroc = findPermanent(player1, "Batroc the Leaper");
        assertThat(batroc.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bear);
    }

    private void castBatroc(List<String> payments) {
        harness.setHand(player1, List.of(new BatrocTheLeaper()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1 + payments.size() * 2);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, null, null, null, false, null, null, null, null,
                payments, false);
        harness.passBothPriorities();
    }
}
