package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DreamSeizer.class})
class DreamSeizerTest extends BaseCardTest {

    @Test
    void acceptingEnterTriggerBlightsCreatureAndEachOpponentDiscards() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DreamSeizer());
        harness.setHand(player2, List.of(new DreamSeizer()));
        harness.setHand(player1, List.of(new DreamSeizer()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, creature.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Dream Seizer");
    }

    @Test
    void decliningEnterTriggerDoesNotBlightOrDiscard() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DreamSeizer());
        Card opponentCard = new DreamSeizer();
        harness.setHand(player2, List.of(opponentCard));
        harness.setHand(player1, List.of(new DreamSeizer()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canBlightItselfAndOpponentChoosesExactlyOneCardWithoutAnotherPriorityRound() {
        Card firstCard = new DreamSeizer();
        Card secondCard = new DreamSeizer();
        Card controllerCard = new DreamSeizer();
        harness.setHand(player2, List.of(firstCard, secondCard));
        harness.setHand(player1, List.of(new DreamSeizer(), controllerCard));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        Permanent seizer = findPermanent(player1, "Dream Seizer");
        harness.handleMayAbilityChosen(player1, true);

        assertThat(seizer.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(firstCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(secondCard);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(controllerCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void acceptingWithEmptyOpponentHandStillBlightsWithoutCreatingAnotherAbility() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new DreamSeizer()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        Permanent seizer = findPermanent(player1, "Dream Seizer");
        harness.handleMayAbilityChosen(player1, true);

        assertThat(seizer.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
