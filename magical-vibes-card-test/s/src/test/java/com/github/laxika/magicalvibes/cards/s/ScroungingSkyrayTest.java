package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Censor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScroungingSkyray.class, Censor.class, GrizzlyBears.class})
class ScroungingSkyrayTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling a card puts a +1/+1 counter on Scrounging Skyray")
    void cyclingPutsCounterOnSelf() {
        Permanent skyray = harness.addToBattlefieldAndReturn(player1, new ScroungingSkyray());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(skyray.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each discarded card puts another +1/+1 counter on Scrounging Skyray")
    void multipleDiscardsStackCounters() {
        Permanent skyray = harness.addToBattlefieldAndReturn(player1, new ScroungingSkyray());
        harness.setHand(player1, List.of(new Censor(), new Censor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(skyray.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Scrounging Skyray can be cycled for two generic mana")
    void cyclesFromHandAndDrawsWithoutTriggeringItself() {
        ScroungingSkyray cycled = new ScroungingSkyray();
        ScroungingSkyray drawn = new ScroungingSkyray();
        harness.setHand(player1, List.of(cycled));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(cycled);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Discarding two cards simultaneously creates one trigger that puts two counters")
    void simultaneousCleanupDiscardsCreateOneTrigger() {
        Permanent skyray = harness.addToBattlefieldAndReturn(player1, new ScroungingSkyray());
        harness.setHand(player1, IntStream.range(0, 9)
                .<Card>mapToObj(i -> new ScroungingSkyray()).toList());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);

        gs.advanceStep(gd);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).filteredOn(entry -> skyray.getId().equals(entry.getSourcePermanentId()))
                .hasSize(1);
        assertThat(skyray.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        resolveAllTriggers();
        assertThat(skyray.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's discard does not put counters on Scrounging Skyray")
    void opponentDiscardDoesNotTrigger() {
        Permanent skyray = harness.addToBattlefieldAndReturn(player1, new ScroungingSkyray());
        harness.setHand(player2, IntStream.range(0, 8)
                .<Card>mapToObj(i -> new ScroungingSkyray()).toList());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        gs.advanceStep(gd);
        harness.handleCardChosen(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(skyray.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
