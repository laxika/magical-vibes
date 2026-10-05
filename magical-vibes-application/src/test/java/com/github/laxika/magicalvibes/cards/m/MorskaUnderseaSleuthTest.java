package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MorskaUnderseaSleuth.class})
class MorskaUnderseaSleuthTest extends BaseCardTest {

    @Test
    @DisplayName("Investigates at the beginning of your upkeep")
    void upkeepCreatesClue() {
        harness.addToBattlefield(player1, new MorskaUnderseaSleuth());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        List<Permanent> clues = findPermanents(player1, "Clue");
        assertThat(clues).hasSize(1);
        assertThat(clues.getFirst().getCard().getSubtypes()).contains(CardSubtype.CLUE);
    }

    @Test
    @DisplayName("Puts two +1/+1 counters on itself on your second draw of the turn")
    void secondDrawAddsTwoCountersOnlyOncePerTurn() {
        Permanent morska = harness.addToBattlefieldAndReturn(player1, new MorskaUnderseaSleuth());
        harness.setLibrary(player1, List.of(new MorskaUnderseaSleuth(), new MorskaUnderseaSleuth(), new MorskaUnderseaSleuth()));

        draw();
        assertThat(morska.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        draw();
        resolveAllTriggers();
        assertThat(morska.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        draw();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Has no maximum hand size")
    void noMaximumHandSize() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.addToBattlefield(player1, new MorskaUnderseaSleuth());
        harness.setHand(player1, new ArrayList<>(List.of(
                new MorskaUnderseaSleuth(), new MorskaUnderseaSleuth(), new MorskaUnderseaSleuth(),
                new MorskaUnderseaSleuth(), new MorskaUnderseaSleuth(), new MorskaUnderseaSleuth(),
                new MorskaUnderseaSleuth(), new MorskaUnderseaSleuth(), new MorskaUnderseaSleuth()
        )));

        harness.getGameService().advanceStep(gd);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(9);
    }

    private void draw() {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
    }

    @Test
    void doesNotInvestigateOnOpponentsUpkeep() {
        harness.addToBattlefield(player1, new MorskaUnderseaSleuth());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(findPermanents(player2, "Clue")).isEmpty();
    }

    @Test
    void secondDrawOnOpponentsTurnAddsCounters() {
        harness.forceActivePlayer(player2);
        Permanent morska = harness.addToBattlefieldAndReturn(player1, new MorskaUnderseaSleuth());
        harness.setLibrary(player1, List.of(new MorskaUnderseaSleuth(), new MorskaUnderseaSleuth()));

        draw();
        draw();
        resolveAllTriggers();

        assertThat(morska.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void opponentsSecondDrawDoesNotAddCounters() {
        Permanent morska = harness.addToBattlefieldAndReturn(player1, new MorskaUnderseaSleuth());
        harness.setLibrary(player2, List.of(new MorskaUnderseaSleuth(), new MorskaUnderseaSleuth()));

        harness.inMutationScope(() -> {
            harness.getDrawService().resolveDrawCard(gd, player2.getId());
            harness.getDrawService().resolveDrawCard(gd, player2.getId());
        });

        assertThat(gd.stack).isEmpty();
        assertThat(morska.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void countsFirstDrawBeforeMorskaEntered() {
        harness.setLibrary(player1, List.of(new MorskaUnderseaSleuth(), new MorskaUnderseaSleuth()));
        draw();
        Permanent morska = harness.addToBattlefieldAndReturn(player1, new MorskaUnderseaSleuth());

        draw();
        resolveAllTriggers();

        assertThat(morska.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void clueCanBeSacrificedForTwoManaToDrawSecondCard() {
        Permanent morska = harness.addToBattlefieldAndReturn(player1, new MorskaUnderseaSleuth());
        harness.setLibrary(player1, List.of(new MorskaUnderseaSleuth(), new MorskaUnderseaSleuth()));
        advanceToUpkeep(player1);
        resolveAllTriggers();
        draw();
        int handSize = gd.playerHands.get(player1.getId()).size();
        Permanent clue = findPermanent(player1, "Clue");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(clue), null, null);

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
        assertThat(morska.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

}
