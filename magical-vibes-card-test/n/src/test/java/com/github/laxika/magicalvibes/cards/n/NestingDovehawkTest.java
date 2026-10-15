package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.CallTheCavalry;
import com.github.laxika.magicalvibes.cards.m.MigratoryRoute;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NestingDovehawk.class, CallTheCavalry.class, NoviceInspector.class, MigratoryRoute.class})
class NestingDovehawkTest extends BaseCardTest {

    @Test
    @DisplayName("Puts one +1/+1 counter on itself for each creature token entering")
    void putsCountersForCreatureTokens() {
        Permanent dovehawk = harness.addToBattlefieldAndReturn(player1, new NestingDovehawk());

        harness.castFromHand(player1, new CallTheCavalry(), "{3}{W}");
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(dovehawk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger for a noncreature token")
    void ignoresNoncreatureToken() {
        Permanent dovehawk = harness.addToBattlefieldAndReturn(player1, new NestingDovehawk());

        harness.castFromHand(player1, new NoviceInspector(), "{W}");
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(dovehawk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Populates at the beginning of combat on its controller's turn")
    void populatesAtBeginningOfCombat() {
        Permanent dovehawk = harness.addToBattlefieldAndReturn(player1, new NestingDovehawk());
        harness.addToBattlefield(player1, token("Bird", CardType.CREATURE));

        advanceToBeginningOfCombat(player1);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Bird")).hasSize(2);
        assertThat(dovehawk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not populate at the beginning of an opponent's combat")
    void doesNotPopulateDuringOpponentsCombat() {
        harness.addToBattlefield(player1, new NestingDovehawk());
        harness.addToBattlefield(player1, token("Bird", CardType.CREATURE));

        advanceToBeginningOfCombat(player2);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Bird")).hasSize(1);
    }

    @Test
    @DisplayName("Each simultaneously entering creature token creates a separate counter trigger")
    void simultaneousCreatureTokensTriggerSeparately() {
        Permanent dovehawk = harness.addToBattlefieldAndReturn(player1, new NestingDovehawk());

        harness.castFromHand(player1, new MigratoryRoute(), "{3}{W}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(4);
        assertThat(dovehawk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(dovehawk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).hasSize(3);
        resolveAllTriggers();
        assertThat(dovehawk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Opposing creature tokens do not put counters on Nesting Dovehawk")
    void ignoresOpponentsCreatureTokens() {
        Permanent dovehawk = harness.addToBattlefieldAndReturn(player1, new NestingDovehawk());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player2, new MigratoryRoute(), "{3}{W}{U}");
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Bird")).hasSize(4);
        assertThat(dovehawk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Populate ignores nontoken creatures, noncreature tokens, and opposing tokens")
    void populateDoesNothingWithoutAnEligibleToken() {
        Permanent dovehawk = harness.addToBattlefieldAndReturn(player1, new NestingDovehawk());
        harness.addToBattlefield(player1, token("Clue", CardType.ARTIFACT));
        harness.addToBattlefield(player2, token("Bird", CardType.CREATURE));

        advanceToBeginningOfCombat(player1);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(findPermanents(player2, "Bird")).hasSize(1);
        assertThat(dovehawk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Populate copies the chosen creature token without copying counters or tapped state")
    void populateCopiesChosenTokenWithoutCountersOrTappedState() {
        harness.castFromHand(player1, new MigratoryRoute(), "{3}{W}{U}");
        harness.passBothPriorities();
        Permanent original = findPermanents(player1, "Bird").getFirst();
        original.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        original.tap();
        Permanent dovehawk = harness.addToBattlefieldAndReturn(player1, new NestingDovehawk());

        advanceToBeginningOfCombat(player1);
        resolveAllTriggers();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, original.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Bird")).hasSize(5);
        Permanent copy = findPermanents(player1, "Bird").getLast();
        assertThat(copy.getId()).isNotEqualTo(original.getId());
        assertThat(copy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(copy.isTapped()).isFalse();
        assertThat(original.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(original.isTapped()).isTrue();
        assertThat(dovehawk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }

    private static Card token(String name, CardType type) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setManaCost("");
        card.setToken(true);
        if (type == CardType.CREATURE) {
            card.setPower(1);
            card.setToughness(1);
        }
        return card;
    }
}
