package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LivingBreakthrough;
import com.github.laxika.magicalvibes.cards.n.NuisanceEngine;
import com.github.laxika.magicalvibes.cards.s.Shock;
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
        NuisanceEngine.class, Shock.class})
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

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    private Permanent addSaga(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new InventiveIteration());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private Permanent addTransformedSaga() {
        InventiveIteration front = new InventiveIteration();
        Permanent saga = new Permanent(front);
        saga.setCard(front.getBackFaceCard());
        saga.setTransformed(true);
        saga.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(saga);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
