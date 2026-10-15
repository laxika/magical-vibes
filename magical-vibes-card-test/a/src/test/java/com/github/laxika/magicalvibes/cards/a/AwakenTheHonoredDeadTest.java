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
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AwakenTheHonoredDead.class, Forest.class, FountainOfYouth.class, GrizzlyBears.class, Shock.class})
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

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    void chapterIIMillsThreeCards() {
        harness.addToBattlefield(player1, new AwakenTheHonoredDead());
        Permanent saga = findPermanent(player1, "Awaken the Honored Dead");
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
        Permanent saga = findPermanent(player1, "Awaken the Honored Dead");
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

    @Test
    void chapterICanDestroyTheSagaItself() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new AwakenTheHonoredDead());

        advanceSagaToNextChapter(0);
        harness.handlePermanentChosen(player1, saga.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Awaken the Honored Dead");
        harness.assertInGraveyard(player1, "Awaken the Honored Dead");
    }

    @Test
    void chapterIIMillsAllRemainingCardsWhenLibraryHasFewerThanThree() {
        harness.addToBattlefield(player1, new AwakenTheHonoredDead());
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.setGraveyard(player1, List.of());

        advanceSagaToNextChapter(1);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void chapterIIICanDeclineDiscardWithoutReturningAnything() {
        harness.addToBattlefield(player1, new AwakenTheHonoredDead());
        Forest handCard = new Forest();
        Forest graveyardCard = new Forest();
        harness.setHand(player1, List.of(handCard));
        harness.setGraveyard(player1, List.of(graveyardCard));

        advanceSagaToNextChapter(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotOnBattlefield(player1, "Awaken the Honored Dead");
        harness.assertInGraveyard(player1, "Awaken the Honored Dead");
    }

    @Test
    void chapterIIICanTargetAndReturnTheLandJustDiscarded() {
        harness.addToBattlefield(player1, new AwakenTheHonoredDead());
        Forest discardedCard = new Forest();
        harness.setHand(player1, List.of(discardedCard));
        harness.setGraveyard(player1, List.of());

        advanceSagaToNextChapter(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(discardedCard.getId()));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discardedCard);
        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Awaken the Honored Dead");

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discardedCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(discardedCard);
    }

    @Test
    void chapterIIIOnlyTargetsCreatureOrLandCardsInItsControllersGraveyard() {
        harness.addToBattlefield(player1, new AwakenTheHonoredDead());
        GrizzlyBears discardedCard = new GrizzlyBears();
        Forest ownLand = new Forest();
        Shock ownInstant = new Shock();
        FountainOfYouth ownArtifact = new FountainOfYouth();
        Forest opposingLand = new Forest();
        harness.setHand(player1, List.of(discardedCard));
        harness.setGraveyard(player1, List.of(ownLand, ownInstant, ownArtifact));
        harness.setGraveyard(player2, List.of(opposingLand));

        advanceSagaToNextChapter(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.cards()).containsExactlyInAnyOrder(ownLand, discardedCard);
        harness.handleMultipleCardsChosen(player1, List.of(discardedCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discardedCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ownLand, ownInstant, ownArtifact);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingLand);
    }

    @Test
    void chapterIIIDoesNotReturnAnythingWhenHandIsEmpty() {
        harness.addToBattlefield(player1, new AwakenTheHonoredDead());
        Forest graveyardCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(graveyardCard));

        advanceSagaToNextChapter(2);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Awaken the Honored Dead");
    }

    private void advanceSagaToNextChapter(int loreCount) {
        Permanent saga = findPermanent(player1, "Awaken the Honored Dead");
        saga.setCounterCount(CounterType.LORE, loreCount);
        advanceToNextChapter();
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

}
