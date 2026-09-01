package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WelcomeToSweettooth.class, GrizzlyBears.class})
class WelcomeToSweettoothTest extends BaseCardTest {

    @Test
    void chapterICreatesAHumanToken() {
        castAndResolveSaga();

        Permanent human = findPermanent(player1, "Human");
        assertThat(human.getCard().isToken()).isTrue();
        assertThat(human.getCard().getPower()).isEqualTo(1);
        assertThat(human.getCard().getToughness()).isEqualTo(1);
        assertThat(human.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(human.getCard().getSubtypes()).contains(CardSubtype.HUMAN);
    }

    @Test
    void chapterIICreatesAFoodToken() {
        harness.addToBattlefield(player1, new WelcomeToSweettooth());
        Permanent saga = findPermanent(player1, "Welcome to Sweettooth");
        saga.setCounterCount(CounterType.LORE, 1);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Food")).hasSize(1);
        assertThat(findPermanent(player1, "Food").getCard().getSubtypes()).contains(CardSubtype.FOOD);
    }

    @Test
    void chapterIIIUsesOnePlusFoodsYouControlForCounters() {
        harness.addToBattlefield(player1, new WelcomeToSweettooth());
        Permanent saga = findPermanent(player1, "Welcome to Sweettooth");
        saga.setCounterCount(CounterType.LORE, 2);
        harness.addToBattlefield(player1, createFoodToken());
        harness.addToBattlefield(player1, createFoodToken());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        advanceToNextChapter();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validPermanentIds()).containsExactly(creature.getId());
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    private void castAndResolveSaga() {
        harness.setHand(player1, List.of(new WelcomeToSweettooth()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Card createFoodToken() {
        Card food = new Card();
        food.setToken(true);
        food.setName("Food");
        food.setType(CardType.ARTIFACT);
        food.setSubtypes(List.of(CardSubtype.FOOD));
        return food;
    }
}
