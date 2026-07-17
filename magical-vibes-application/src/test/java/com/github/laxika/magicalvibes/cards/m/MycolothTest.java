package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MycolothTest extends BaseCardTest {

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP
    }

    private List<Permanent> saprolings(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();
    }

    @Test
    @DisplayName("Devouring two creatures enters with four counters, then upkeep makes four Saprolings")
    void devourThenUpkeepTokens() {
        Permanent fodderA = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent fodderB = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, new ArrayList<>(List.of(new Mycoloth())));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0, 0, null);
        harness.passBothPriorities(); // resolve creature spell -> devour choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(fodderA.getId(), fodderB.getId()));

        Permanent mycoloth = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Mycoloth"))
                .findFirst().orElseThrow();
        assertThat(mycoloth.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve upkeep trigger

        assertThat(saprolings(player1)).hasSize(4);
    }

    @Test
    @DisplayName("With no +1/+1 counters, no Saprolings are created on upkeep")
    void noCountersNoTokens() {
        harness.addToBattlefield(player1, new Mycoloth());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(saprolings(player1)).isEmpty();
    }

    @Test
    @DisplayName("Creates one Saproling per +1/+1 counter on upkeep")
    void oneSaprolingPerCounter() {
        Permanent mycoloth = harness.addToBattlefieldAndReturn(player1, new Mycoloth());
        mycoloth.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(saprolings(player1)).hasSize(3);
    }

    @Test
    @DisplayName("Does not trigger during opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        Permanent mycoloth = harness.addToBattlefieldAndReturn(player1, new Mycoloth());
        mycoloth.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        advanceToUpkeep(player2); // opponent's upkeep

        assertThat(saprolings(player1)).isEmpty();
    }
}
