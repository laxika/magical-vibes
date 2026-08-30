package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AyarasOathsworn.class, GrizzlyBears.class})
class AyarasOathswornTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a counter on combat damage but does not search before reaching four")
    void getsCounterWithoutSearchingBeforeFour() {
        Permanent oathsworn = addReadyOathsworn();
        oathsworn.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Card libraryCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(libraryCard));

        dealCombatDamageAndResolveTrigger(oathsworn);

        assertThat(oathsworn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Searches for a card when combat damage adds the fourth counter")
    void searchesWhenFourthCounterIsAdded() {
        Permanent oathsworn = addReadyOathsworn();
        oathsworn.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Card libraryCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(libraryCard));

        dealCombatDamageAndResolveTrigger(oathsworn);

        assertThat(oathsworn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).contains(libraryCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Does not trigger when it already has four counters")
    void doesNotTriggerAtFourCounters() {
        Permanent oathsworn = addReadyOathsworn();
        oathsworn.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        Card libraryCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(libraryCard));

        oathsworn.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(oathsworn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addReadyOathsworn() {
        Permanent oathsworn = harness.addToBattlefieldAndReturn(player1, new AyarasOathsworn());
        oathsworn.setSummoningSick(false);
        return oathsworn;
    }

    private void dealCombatDamageAndResolveTrigger(Permanent oathsworn) {
        oathsworn.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
