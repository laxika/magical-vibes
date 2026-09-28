package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.CallTheCavalry;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NestingDovehawk.class, CallTheCavalry.class, NoviceInspector.class})
class NestingDovehawkTest extends BaseCardTest {

    @Test
    @DisplayName("Puts one +1/+1 counter on itself for each creature token entering")
    void putsCountersForCreatureTokens() {
        Permanent dovehawk = harness.addToBattlefieldAndReturn(player1, new NestingDovehawk());

        harness.setHand(player1, List.of(new CallTheCavalry()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(dovehawk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger for a noncreature token")
    void ignoresNoncreatureToken() {
        Permanent dovehawk = harness.addToBattlefieldAndReturn(player1, new NestingDovehawk());

        harness.setHand(player1, List.of(new NoviceInspector()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
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

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
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
