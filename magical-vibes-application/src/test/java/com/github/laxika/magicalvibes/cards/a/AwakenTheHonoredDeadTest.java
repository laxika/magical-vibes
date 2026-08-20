package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AwakenTheHonoredDeadTest extends BaseCardTest {

    @Test
    void chapterITargetsAndDestroysNonlandPermanent() {
        harness.addToBattlefield(player1, new AwakenTheHonoredDead());
        FountainOfYouth fountain = new FountainOfYouth();
        Forest forest = new Forest();
        Permanent fountainPermanent = harness.addToBattlefieldAndReturn(player2, fountain);
        Permanent forestPermanent = harness.addToBattlefieldAndReturn(player2, forest);

        advanceSagaToNextChapter(0);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(fountainPermanent.getId());
        assertThat(choice.validIds()).doesNotContain(forestPermanent.getId());

        harness.handlePermanentChosen(player1, fountainPermanent.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(p -> p.getCard().getName())
                .doesNotContain("Fountain of Youth")
                .contains("Forest");
    }

    @Test
    void chapterIIMillsThreeCards() {
        harness.addToBattlefield(player1, new AwakenTheHonoredDead());
        Permanent saga = findSaga();
        saga.setCounterCount(CounterType.LORE, 1);
        GrizzlyBears cardOne = new GrizzlyBears();
        Forest cardTwo = new Forest();
        FountainOfYouth cardThree = new FountainOfYouth();
        Shock cardFour = new Shock();
        harness.setLibrary(player1, List.of(cardOne, cardTwo, cardThree, cardFour));

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    void chapterIIIOptionallyDiscardsThenReturnsCreatureOrLand() {
        harness.addToBattlefield(player1, new AwakenTheHonoredDead());
        Permanent saga = findSaga();
        saga.setCounterCount(CounterType.LORE, 2);
        Shock discardedCard = new Shock();
        GrizzlyBears returnedCard = new GrizzlyBears();
        Forest remainingCard = new Forest();
        harness.setHand(player1, List.of(discardedCard));
        harness.setGraveyard(player1, List.of(returnedCard, remainingCard));

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(returnedCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getId())
                .contains(returnedCard.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(card -> card.getId())
                .contains(discardedCard.getId(), remainingCard.getId())
                .doesNotContain(returnedCard.getId());
    }

    private void advanceSagaToNextChapter(int loreCount) {
        Permanent saga = findSaga();
        saga.setCounterCount(CounterType.LORE, loreCount);
        advanceToNextChapter();
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent findSaga() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof AwakenTheHonoredDead)
                .findFirst()
                .orElseThrow();
    }
}
