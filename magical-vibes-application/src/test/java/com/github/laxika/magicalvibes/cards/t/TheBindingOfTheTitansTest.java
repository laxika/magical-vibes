package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GroundSeal;
import com.github.laxika.magicalvibes.cards.i.Infuriate;
import com.github.laxika.magicalvibes.cards.n.NyleasForerunner;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheBindingOfTheTitans.class, Forest.class, NyleasForerunner.class, Infuriate.class, GroundSeal.class})
class TheBindingOfTheTitansTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I mills three cards from each player")
    void chapterIMillsEachPlayer() {
        List<Card> controllerCards = List.of(new Forest(), new Forest(), new Forest());
        List<Card> opponentCards = List.of(new NyleasForerunner(), new NyleasForerunner(), new NyleasForerunner());
        harness.setLibrary(player1, controllerCards);
        harness.setLibrary(player2, opponentCards);
        addSagaWithLore(0);

        advanceToNextChapter();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(controllerCards);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyElementsOf(opponentCards);
    }

    @Test
    @DisplayName("Chapter II exiles up to two cards from any graveyards and gains life for creatures")
    void chapterIIExilesCardsAndGainsLifeForCreatureCards() {
        NyleasForerunner creature = new NyleasForerunner();
        Forest land = new Forest();
        harness.setGraveyard(player1, List.of(creature));
        harness.setGraveyard(player2, List.of(land));
        harness.setLife(player1, 20);
        addSagaWithLore(1);

        advanceToNextChapter();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(creature.getId(), land.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId(), land.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(creature);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(land);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Chapter III returns a creature or land from the graveyard to hand")
    void chapterIIIReturnsCreatureOrLandToHand() {
        NyleasForerunner creature = new NyleasForerunner();
        Forest land = new Forest();
        Infuriate instant = new Infuriate();
        harness.setGraveyard(player1, List.of(creature, land, instant));
        Permanent saga = addSagaWithLore(2);

        advanceToNextChapter();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(creature.getId(), land.getId());

        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Nylea's Forerunner");
        harness.assertInGraveyard(player1, "Infuriate");
        harness.assertNotOnBattlefield(player1, "The Binding of the Titans");
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Chapter I triggers when the Saga enters and mills only available cards")
    void chapterITriggersOnEntryWithShortLibraries() {
        Forest controllerCard = new Forest();
        Infuriate opponentCard = new Infuriate();
        harness.setLibrary(player1, List.of(controllerCard));
        harness.setLibrary(player2, List.of(opponentCard));
        harness.castFromHand(player1, new TheBindingOfTheTitans(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(controllerCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "The Binding of the Titans");
    }

    @Test
    @DisplayName("Chapter II allows choosing no targets even with cards available")
    void chapterIIAllowsZeroTargets() {
        NyleasForerunner creature = new NyleasForerunner();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLife(player1, 20);
        addSagaWithLore(1);

        advanceToNextChapter();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Chapter II gains two life for two creatures from the same graveyard")
    void chapterIIExilesTwoCreaturesFromOneGraveyard() {
        NyleasForerunner first = new NyleasForerunner();
        NyleasForerunner second = new NyleasForerunner();
        harness.setGraveyard(player2, List.of(first, second));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addSagaWithLore(1);

        advanceToNextChapter();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Chapter II can exile just one noncreature without gaining life")
    void chapterIIExilesOneNoncreature() {
        Infuriate instant = new Infuriate();
        Forest land = new Forest();
        harness.setGraveyard(player2, List.of(instant, land));
        harness.setLife(player1, 20);
        addSagaWithLore(1);

        advanceToNextChapter();
        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(instant);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(land);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Chapter II gains life only for targets it actually exiles")
    void chapterIIResolvesWithOneTargetRemaining() {
        NyleasForerunner creature = new NyleasForerunner();
        Forest land = new Forest();
        harness.setGraveyard(player1, List.of(creature, land));
        harness.setLife(player1, 20);
        addSagaWithLore(1);

        advanceToNextChapter();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId(), land.getId()));
        harness.setGraveyard(player1, List.of(land));
        harness.setExile(player1, List.of(creature));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(creature, land);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Chapter III returns a creature and excludes the opponent's graveyard")
    void chapterIIIReturnsOnlyOwnCreature() {
        NyleasForerunner ownCreature = new NyleasForerunner();
        NyleasForerunner opposingCreature = new NyleasForerunner();
        Forest opposingLand = new Forest();
        harness.setGraveyard(player1, List.of(ownCreature));
        harness.setGraveyard(player2, List.of(opposingCreature, opposingLand));
        addSagaWithLore(2);

        advanceToNextChapter();
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(ownCreature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(ownCreature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(ownCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(ownCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingCreature, opposingLand);
        harness.assertInGraveyard(player1, "The Binding of the Titans");
        harness.assertNotOnBattlefield(player1, "The Binding of the Titans");
    }

    @Test
    @DisplayName("Chapter III does not return a target that leaves the graveyard")
    void chapterIIIDoesNotReturnMissingTarget() {
        Forest land = new Forest();
        harness.setGraveyard(player1, List.of(land));
        addSagaWithLore(2);

        advanceToNextChapter();
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(land));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(land);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(land);
        harness.assertInGraveyard(player1, "The Binding of the Titans");
        harness.assertNotOnBattlefield(player1, "The Binding of the Titans");
    }

    @Test
    @DisplayName("The Saga is sacrificed after chapter III when there are no legal targets")
    void chapterIIIWithoutLegalTargetsStillSacrificesSaga() {
        Infuriate instant = new Infuriate();
        harness.setGraveyard(player1, List.of(instant));
        harness.setGraveyard(player2, List.of(new Forest()));
        addSagaWithLore(2);

        advanceToNextChapter();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Infuriate");
        harness.assertInGraveyard(player1, "The Binding of the Titans");
        harness.assertNotOnBattlefield(player1, "The Binding of the Titans");
    }

    @Test
    @CardUsed({TheBindingOfTheTitans.class, NyleasForerunner.class, GroundSeal.class})
    @DisplayName("Chapter II cannot target graveyard cards while Ground Seal is on the battlefield")
    void chapterIICannotTargetCardsProtectedByGroundSeal() {
        NyleasForerunner creature = new NyleasForerunner();
        harness.setGraveyard(player2, List.of(creature));
        harness.setLife(player1, 20);
        harness.addToBattlefield(player2, new GroundSeal());
        addSagaWithLore(1);

        advanceToNextChapter();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creature);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player1, "The Binding of the Titans");
    }

    @Test
    @CardUsed({TheBindingOfTheTitans.class, Forest.class, GroundSeal.class})
    @DisplayName("Chapter III has no legal targets under Ground Seal and the Saga is sacrificed")
    void chapterIIICannotTargetCardsProtectedByGroundSeal() {
        Forest land = new Forest();
        harness.setGraveyard(player1, List.of(land));
        harness.addToBattlefield(player2, new GroundSeal());
        addSagaWithLore(2);

        advanceToNextChapter();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(land);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land);
        harness.assertInGraveyard(player1, "The Binding of the Titans");
        harness.assertNotOnBattlefield(player1, "The Binding of the Titans");
    }

    @Test
    @DisplayName("Chapter II resolves without choosing cards when both graveyards are empty")
    void chapterIIWithEmptyGraveyards() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        harness.setLife(player1, 20);
        addSagaWithLore(1);

        advanceToNextChapter();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player1, "The Binding of the Titans");
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheBindingOfTheTitans());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
