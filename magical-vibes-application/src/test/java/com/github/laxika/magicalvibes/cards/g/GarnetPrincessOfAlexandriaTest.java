package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HistoryOfBenalia;
import com.github.laxika.magicalvibes.cards.t.TheFlameOfKeld;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GarnetPrincessOfAlexandria.class, HistoryOfBenalia.class, TheFlameOfKeld.class})
class GarnetPrincessOfAlexandriaTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking lets you remove lore counters from any number of your Sagas")
    void removesLoreCountersAndAddsMatchingPlusOneCounters() {
        Permanent garnet = addCreatureReady(player1, new GarnetPrincessOfAlexandria());
        Permanent history = addSaga(player1, new HistoryOfBenalia(), 1);
        Permanent flame = addSaga(player1, new TheFlameOfKeld(), 2);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(history.getId(), flame.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(history.getId(), flame.getId()));

        assertThat(history.getCounterCount(CounterType.LORE)).isZero();
        assertThat(flame.getCounterCount(CounterType.LORE)).isEqualTo(1);
        assertThat(garnet.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The attack trigger can choose only some of the Sagas")
    void choosesSubsetOfSagas() {
        Permanent garnet = addCreatureReady(player1, new GarnetPrincessOfAlexandria());
        Permanent history = addSaga(player1, new HistoryOfBenalia(), 1);
        Permanent flame = addSaga(player1, new TheFlameOfKeld(), 1);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultiplePermanentsChosen(player1, List.of(history.getId()));

        assertThat(history.getCounterCount(CounterType.LORE)).isZero();
        assertThat(flame.getCounterCount(CounterType.LORE)).isEqualTo(1);
        assertThat(garnet.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the attack trigger leaves Sagas and Garnet unchanged")
    void decliningDoesNothing() {
        Permanent garnet = addCreatureReady(player1, new GarnetPrincessOfAlexandria());
        Permanent history = addSaga(player1, new HistoryOfBenalia(), 1);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(history.getCounterCount(CounterType.LORE)).isEqualTo(1);
        assertThat(garnet.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addSaga(Player player, Card card, int loreCounters) {
        Permanent saga = new Permanent(card);
        saga.setCounterCount(CounterType.LORE, loreCounters);
        gd.playerBattlefields.get(player.getId()).add(saga);
        return saga;
    }
}
