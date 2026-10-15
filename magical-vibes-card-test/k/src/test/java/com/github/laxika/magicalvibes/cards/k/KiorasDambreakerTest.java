package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.s.Snarespinner;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KiorasDambreaker.class, Snarespinner.class})
class KiorasDambreakerTest extends BaseCardTest {

    @Test
    @DisplayName("When Kiora's Dambreaker enters, it proliferates")
    void proliferatesWhenItEnters() {
        Permanent creature = addCreatureReady(player1, new Snarespinner());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.castFromHand(player1, new KiorasDambreaker(), "{5}{U}");
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId()));

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void addsOneOfEveryExistingKindToSelectedPermanentsAndPlayers() {
        Permanent own = addCreatureReady(player1, new Snarespinner());
        own.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        own.setCounterCount(CounterType.CHARGE, 3);
        Permanent opposing = addCreatureReady(player2, new Snarespinner());
        opposing.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent unchosen = addCreatureReady(player2, new Snarespinner());
        unchosen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.playerPoisonCounters.put(player1.getId(), 1);
        gd.playerPoisonCounters.put(player2.getId(), 2);
        gd.playerEnergyCounters.put(player2.getId(), 3);
        gd.playerExperienceCounters.put(player2.getId(), 4);

        harness.castFromHand(player1, new KiorasDambreaker(), "{5}{U}");
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1,
                List.of(own.getId(), opposing.getId(), player2.getId()));

        assertThat(own.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(own.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
        assertThat(own.getCounterCount(CounterType.FLYING)).isZero();
        assertThat(opposing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(unchosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(3);
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(4);
        assertThat(gd.playerExperienceCounters.get(player2.getId())).isEqualTo(5);
    }

    @Test
    void canChooseNothingEvenWhenCountersExist() {
        Permanent creature = addCreatureReady(player1, new Snarespinner());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.playerPoisonCounters.put(player2.getId(), 2);

        harness.castFromHand(player1, new KiorasDambreaker(), "{5}{U}");
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void resolvesWithoutAChoiceWhenNothingHasCounters() {
        Permanent creature = addCreatureReady(player1, new Snarespinner());

        harness.castFromHand(player1, new KiorasDambreaker(), "{5}{U}");
        resolveAllTriggers();

        assertThat(creature.getCounters()).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Kiora's Dambreaker");
    }
}
