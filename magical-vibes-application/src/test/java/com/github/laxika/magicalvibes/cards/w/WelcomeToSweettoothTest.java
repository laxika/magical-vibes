package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.r.RedtoothVanguard;
import com.github.laxika.magicalvibes.cards.t.ToughCookie;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({WelcomeToSweettooth.class, RedtoothVanguard.class, ToughCookie.class})
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
        Permanent firstCookie = harness.enterBattlefieldAndReturn(player1, new ToughCookie());
        resolveAllTriggers();
        Permanent secondCookie = harness.enterBattlefieldAndReturn(player1, new ToughCookie());
        resolveAllTriggers();
        Permanent creature = addCreatureReady(player1, new RedtoothVanguard());

        advanceToNextChapter();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validPermanentIds()).containsExactlyInAnyOrder(
                        firstCookie.getId(), secondCookie.getId(), creature.getId());
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    void allChaptersGrowTheCreatedHumanThenSacrificeTheSaga() {
        castAndResolveSaga();
        Permanent saga = findPermanent(player1, "Welcome to Sweettooth");
        Permanent human = findPermanent(player1, "Human");
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);

        advanceToNextChapter();
        resolveAllTriggers();
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(2);
        assertThat(countPermanents(player1, "Food")).isOne();

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, human.getId());
        resolveAllTriggers();

        assertThat(human.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(countPermanents(player1, "Human")).isOne();
        assertThat(countPermanents(player1, "Food")).isOne();
        assertThat(countPermanents(player1, "Welcome to Sweettooth")).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(saga.getCard());
    }

    @Test
    void chapterIIProducesFoodThatCanBeSacrificedForLife() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new WelcomeToSweettooth());
        saga.setCounterCount(CounterType.LORE, 1);
        harness.setLife(player1, 20);

        advanceToNextChapter();
        resolveAllTriggers();

        Permanent food = findPermanent(player1, "Food");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(food), null, null);

        assertThat(findPermanents(player1, "Food")).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        resolveAllTriggers();
        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
    }

    @Test
    void chapterIIIWithNoFoodsAddsOneCounterAndExcludesOpposingCreatures() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new WelcomeToSweettooth());
        saga.setCounterCount(CounterType.LORE, 2);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RedtoothVanguard());
        Permanent opposingFood = harness.addToBattlefieldAndReturn(player2, new ToughCookie());

        advanceToNextChapter();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validPermanentIds()).containsExactly(creature.getId());
        harness.handlePermanentChosen(player1, creature.getId());
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingFood.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanents(player1, "Welcome to Sweettooth")).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(saga.getCard());
    }

    @Test
    void chapterIIICountsFoodsWhenItResolvesAfterFoodIsSacrificed() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new WelcomeToSweettooth());
        saga.setCounterCount(CounterType.LORE, 1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RedtoothVanguard());
        advanceToNextChapter();
        resolveAllTriggers();
        Permanent food = findPermanent(player1, "Food");

        advanceToNextChapter();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handlePermanentChosen(player1, creature.getId());

        assertThat(findPermanents(player1, "Welcome to Sweettooth")).hasSize(1);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(food), null, null);
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Welcome to Sweettooth")).isEmpty();
    }

    @Test
    void chapterIIIMustChooseATargetWhenALegalCreatureExists() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new WelcomeToSweettooth());
        saga.setCounterCount(CounterType.LORE, 2);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RedtoothVanguard());

        advanceToNextChapter();

        PendingInteraction.PermanentChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPlayerIds()).isEmpty();
        harness.handlePermanentChosen(player1, creature.getId());
        resolveAllTriggers();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void castAndResolveSaga() {
        harness.setHand(player1, List.of(new WelcomeToSweettooth()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castEnchantment(player1, 0);
        resolveAllTriggers();
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
