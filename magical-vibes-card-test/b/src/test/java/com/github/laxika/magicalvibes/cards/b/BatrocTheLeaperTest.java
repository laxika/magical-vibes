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

        harness.handleMultiplePermanentsChosen(player1, List.of(player2.getId()));
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

        harness.handleMultiplePermanentsChosen(player1, List.of(player2.getId(), bear.getId()));
        harness.passBothPriorities();

        Permanent batroc = findPermanent(player1, "Batroc the Leaper");
        assertThat(batroc.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bear);
    }

    @Test
    @DisplayName("A kicked Batroc can choose no targets and still enters with counters")
    void kickedWithNoTargets() {
        castBatroc(List.of("{2}"));

        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Batroc the Leaper")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Two kicks allow choosing only one target")
    void fewerTargetsThanKicks() {
        castBatroc(List.of("{2}", "{2}"));

        harness.handleMultiplePermanentsChosen(player1, List.of(player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Batroc can deal damage to his own controller")
    void canTargetController() {
        castBatroc(List.of("{2}"));

        harness.handleMultiplePermanentsChosen(player1, List.of(player1.getId()));
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Damage uses Batroc's power when the triggered ability resolves")
    void usesPowerAtResolution() {
        castBatroc(List.of("{2}"));
        harness.handleMultiplePermanentsChosen(player1, List.of(player2.getId()));

        Permanent batroc = findPermanent(player1, "Batroc the Leaper");
        batroc.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
    }

    private void castBatroc(List<String> payments) {
        harness.setHand(player1, List.of(new BatrocTheLeaper()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1 + payments.size() * 2);

        harness.castCreatureWithRepeatedCosts(player1, 0, payments);
        harness.passBothPriorities();
    }
}
