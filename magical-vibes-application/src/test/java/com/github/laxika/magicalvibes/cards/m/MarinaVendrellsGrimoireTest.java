package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarinaVendrellsGrimoire.class, Forest.class})
class MarinaVendrellsGrimoireTest extends BaseCardTest {

    @Test
    @DisplayName("Draws five cards when cast onto the battlefield")
    void castEtbDrawsFiveCards() {
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castFromHand(player1, new MarinaVendrellsGrimoire(), "{5}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 5);
    }

    @Test
    @DisplayName("Does not draw when it enters the battlefield without being cast")
    void nonCastEtbDoesNotDraw() {
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.enterBattlefieldAndReturn(player1, new MarinaVendrellsGrimoire());

        assertThat(gd.stack).isEmpty();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
    }

    @Test
    @DisplayName("Gaining life draws that many cards")
    void lifeGainDrawsCards() {
        harness.addToBattlefield(player1, new MarinaVendrellsGrimoire());
        harness.setLife(player1, 20);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 3);
    }

    @Test
    @DisplayName("Losing life discards that many cards")
    void lifeLossDiscardsCards() {
        harness.addToBattlefield(player1, new MarinaVendrellsGrimoire());
        harness.setLife(player1, 20);
        harness.setHand(player1, new ArrayList<>(List.of(
                new Forest(), new Forest(), new Forest(), new Forest())));

        harness.inMutationScope(() -> harness.getLifeSupport().applyLifeLoss(gd, player1.getId(), 2, "test"));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNotNull();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Losing life with no cards remaining loses the game")
    void lifeLossWithEmptyHandLosesGame() {
        harness.addToBattlefield(player1, new MarinaVendrellsGrimoire());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Forest()));

        harness.inMutationScope(() -> harness.getLifeSupport().applyLifeLoss(gd, player1.getId(), 2, "test"));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("The grimoire prevents loss from having 0 or less life")
    void doesNotLoseFromZeroLife() {
        harness.addToBattlefield(player1, new MarinaVendrellsGrimoire());
        harness.setLife(player1, 0);

        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Controller has no maximum hand size")
    void controllerHasNoMaximumHandSize() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.addToBattlefield(player1, new MarinaVendrellsGrimoire());
        harness.setHand(player1, new ArrayList<>(List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest())));

        harness.getGameService().advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(9);
    }

    @Test
    void lifeGainWaitsForTriggeredAbilityToResolve() {
        harness.addToBattlefield(player1, new MarinaVendrellsGrimoire());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void lifeLossWithAlreadyEmptyHandLosesOnlyOnResolution() {
        harness.addToBattlefield(player1, new MarinaVendrellsGrimoire());
        harness.setHand(player1, List.of());

        harness.inMutationScope(() -> harness.getLifeSupport().applyLifeLoss(gd, player1.getId(), 1, "test"));

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);

        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    @Test
    void negativeLifeAndEmptyHandAloneDoNotLoseTheGame() {
        harness.addToBattlefield(player1, new MarinaVendrellsGrimoire());
        harness.setHand(player1, List.of());
        harness.setLife(player1, -5);

        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void opponentLifeChangesDoNotDrawOrDiscardForController() {
        harness.addToBattlefield(player1, new MarinaVendrellsGrimoire());
        harness.setHand(player1, List.of(new Forest()));
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.inMutationScope(() -> {
            harness.getLifeSupport().applyGainLife(gd, player2.getId(), 2);
            harness.getLifeSupport().applyLifeLoss(gd, player2.getId(), 1, "test");
        });

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void payingLifeTriggersDiscard() {
        harness.addToBattlefield(player1, new MarinaVendrellsGrimoire());
        harness.setHand(player1, List.of(new Forest(), new Forest()));

        harness.inMutationScope(() -> harness.getLifeSupport().applyLifePayment(gd, player1.getId(), 1, "test"));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void drawingFromEmptyLibraryStillLosesTheGame() {
        harness.addToBattlefield(player1, new MarinaVendrellsGrimoire());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new Forest()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }
}
