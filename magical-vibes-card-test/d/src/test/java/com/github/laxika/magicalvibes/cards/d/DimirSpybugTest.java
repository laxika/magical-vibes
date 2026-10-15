package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.w.WishcoinCrab;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DimirSpybug.class, DazzlingLights.class, WishcoinCrab.class})
class DimirSpybugTest extends BaseCardTest {

    @Test
    @DisplayName("Whenever its controller surveils, it gets a +1/+1 counter")
    void surveilingAddsCounter() {
        Permanent spybug = addCreatureReady(player1, new DimirSpybug());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WishcoinCrab());
        Card topCard = new WishcoinCrab();
        Card secondCard = new WishcoinCrab();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.setHand(player1, List.of(new DazzlingLights()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of(1)));
        harness.passBothPriorities();

        assertThat(spybug.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Keeping all surveilled cards gives one counter after surveil completes")
    void keepingAllCardsAddsOneCounter() {
        Permanent spybug = harness.addToBattlefieldAndReturn(player1, new DimirSpybug());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WishcoinCrab());
        harness.setLibrary(player1, List.of(new WishcoinCrab(), new WishcoinCrab()));
        harness.setHand(player1, List.of(new DazzlingLights()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(spybug.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
        harness.passBothPriorities();

        assertThat(spybug.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Putting all surveilled cards in the graveyard gives only one counter")
    void puttingAllCardsInGraveyardAddsOneCounter() {
        Permanent spybug = harness.addToBattlefieldAndReturn(player1, new DimirSpybug());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WishcoinCrab());
        Card first = new WishcoinCrab();
        Card second = new WishcoinCrab();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new DazzlingLights()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));
        harness.passBothPriorities();

        assertThat(spybug.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second);
    }

    @Test
    @DisplayName("An opponent surveilling does not give a counter")
    void opponentsSurveilDoesNotAddCounter() {
        Permanent spybug = harness.addToBattlefieldAndReturn(player1, new DimirSpybug());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WishcoinCrab());
        harness.setLibrary(player2, List.of(new WishcoinCrab(), new WishcoinCrab()));
        harness.setHand(player2, List.of(new DazzlingLights()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player2, 0, target.getId());
        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(0), List.of(1)));

        assertThat(spybug.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Surveilling an empty library still gives a counter")
    void surveilingEmptyLibraryAddsCounter() {
        Permanent spybug = harness.addToBattlefieldAndReturn(player1, new DimirSpybug());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WishcoinCrab());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new DazzlingLights()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(spybug.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each separate surveil in the same turn gives another counter")
    void repeatedSurveilsAddCounters() {
        Permanent spybug = harness.addToBattlefieldAndReturn(player1, new DimirSpybug());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WishcoinCrab());
        harness.setLibrary(player1, List.of(new WishcoinCrab(), new WishcoinCrab()));
        harness.setHand(player1, List.of(new DazzlingLights(), new DazzlingLights()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        for (int expectedCounters = 1; expectedCounters <= 2; expectedCounters++) {
            harness.castAndResolveInstant(player1, 0, target.getId());
            gs.handleInteractionAnswer(gd, player1,
                    new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
            harness.passBothPriorities();

            assertThat(spybug.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                    .isEqualTo(expectedCounters);
        }
    }
}
