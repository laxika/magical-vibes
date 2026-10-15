package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LivingBreakthrough;
import com.github.laxika.magicalvibes.cards.n.NuisanceEngine;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TezzeretBetrayerOfFlesh;
import com.github.laxika.magicalvibes.model.Card;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InventiveIteration.class, LivingBreakthrough.class, GrizzlyBears.class,
        NuisanceEngine.class, Shock.class, TezzeretBetrayerOfFlesh.class})
class InventiveIterationTest extends BaseCardTest {

    @Test
    void chapterIReturnsUpToOneCreatureOrPlaneswalker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addSaga(0);

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Inventive Iteration");
    }

    @Test
    void chapterIIReturnsAnArtifactFromTheGraveyard() {
        Card artifact = new NuisanceEngine();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setLibrary(player1, List.of());
        addSaga(1);

        advanceToNextChapter();
        harness.passBothPriorities();

        PendingInteraction.GraveyardChoice choice = gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice)
                .as("hand=%s graveyard=%s stack=%s pending=%s",
                        gd.playerHands.get(player1.getId()), gd.playerGraveyards.get(player1.getId()),
                        gd.stack, gd.pendingInteractions)
                .isNotNull();
        assertThat(choice.validIndices()).containsExactly(0);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertInHand(player1, "Nuisance Engine");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void chapterIIDrawsIfThereIsNoArtifactInTheGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Shock(), new Shock()));
        addSaga(1);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .as("stack=%s pending=%s graveyard=%s", gd.stack, gd.pendingInteractions,
                        gd.playerGraveyards.get(player1.getId()))
                .hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()))
                .allMatch(card -> card.getName().equals("Shock"));
    }

    @Test
    void chapterIIITransformsIntoLivingBreakthrough() {
        addSaga(2);

        advanceToNextChapter();
        harness.passBothPriorities();

        Permanent transformed = findPermanent(player1, "Living Breakthrough");
        assertThat(transformed).isNotNull();
        assertThat(transformed.isTransformed()).isTrue();
    }

    @Test
    void livingBreakthroughPreventsOpponentsFromCastingTheTriggeringManaValue() {
        addTransformedSaga();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void chapterICanChooseNoTargetEvenWhenACreatureExists() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        addSaga(0);

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInHand(player2, "Grizzly Bears");
    }

    @Test
    void chapterIReturnsAPlaneswalker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TezzeretBetrayerOfFlesh());
        target.setCounterCount(CounterType.LOYALTY, 4);
        addSaga(0);

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Tezzeret, Betrayer of Flesh");
        harness.assertNotOnBattlefield(player2, "Tezzeret, Betrayer of Flesh");
    }

    @Test
    void chapterIIDoesNotDrawAfterReturningTheLastArtifact() {
        Card artifact = new NuisanceEngine();
        Card libraryCard = new Shock();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(artifact));
        harness.setLibrary(player1, List.of(libraryCard));
        addSaga(1);

        advanceToNextChapter();
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    void chapterIICannotDeclineReturningAnAvailableArtifact() {
        harness.setGraveyard(player1, List.of(new NuisanceEngine()));
        addSaga(1);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot decline forced graveyard choice");
    }

    @Test
    void chapterIIReturnsOnlyOneArtifactWhenSeveralAreAvailable() {
        Card first = new NuisanceEngine();
        Card second = new NuisanceEngine();
        Card libraryCard = new Shock();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(first, second));
        harness.setLibrary(player1, List.of(libraryCard));
        addSaga(1);

        advanceToNextChapter();
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    void chapterIIDrawsWhenOnlyTheOpponentHasAnArtifactInTheGraveyard() {
        Card libraryCard = new Shock();
        Card opponentArtifact = new NuisanceEngine();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(opponentArtifact));
        harness.setLibrary(player1, List.of(libraryCard));
        addSaga(1);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentArtifact);
    }

    @Test
    void chapterIIIReturnsAnOpponentsSagaUnderTheAbilityControllersControl() {
        InventiveIteration card = new InventiveIteration();
        card.setOwnerId(player2.getId());
        Permanent saga = harness.addToBattlefieldAndReturn(player1, card);
        saga.setCounterCount(CounterType.LORE, 2);

        advanceToNextChapter();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Living Breakthrough");
        harness.assertNotOnBattlefield(player2, "Living Breakthrough");
    }

    @Test
    void livingBreakthroughAllowsAResponseBeforeItsTriggerResolves() {
        addTransformedSaga();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player1, 0, player2.getId());
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    void livingBreakthroughAllowsOpponentsToCastADifferentManaValue() {
        addTransformedSaga();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void livingBreakthroughDoesNotRestrictItsControllersSpells() {
        addTransformedSaga();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
    }

    @Test
    void livingBreakthroughRestrictionExpiresAtTheControllersNextTurn() {
        addTransformedSaga();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.UPKEEP);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 18);
    }

    @Test
    void castingTheSagaTriggersChapterIWhenItEnters() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new InventiveIteration(), "{3}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(findPermanent(player1, "Inventive Iteration").getCounterCount(CounterType.LORE))
                .isEqualTo(1);
    }

    @Test
    void chapterIIIReturnsANewPermanentWithoutLoreCounters() {
        Permanent original = addSaga(2);
        original.tap();

        advanceToNextChapter();
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Living Breakthrough");
        assertThat(returned).isNotNull();
        assertThat(returned.getId()).isNotEqualTo(original.getId());
        assertThat(returned.getCounterCount(CounterType.LORE)).isZero();
        assertThat(returned.isTapped()).isFalse();
        assertThat(returned.isSummoningSick()).isTrue();
        harness.assertNotInGraveyard(player1, "Inventive Iteration");
    }

    private Permanent addSaga(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new InventiveIteration());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private Permanent addTransformedSaga() {
        InventiveIteration front = new InventiveIteration();
        Permanent saga = harness.addToBattlefieldAndReturn(player1, front);
        saga.setCard(front.getBackFaceCard());
        saga.setTransformed(true);
        saga.setSummoningSick(false);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
