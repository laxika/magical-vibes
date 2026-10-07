package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.z.ZulaportEnforcer;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheDarkBarony.class, GrizzlyBears.class, HillGiant.class, ZulaportEnforcer.class})
class TheDarkBaronyTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new TheDarkBarony(), gd.nextTimestamp()));
    }

    @Test
    void nonblackCardsPutIntoEitherGraveyardCauseTheirOwnerToLoseLife() {
        Permanent ownCard = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCard = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, ownCard);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, opposingCard);
        });
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }

    @Test
    void blackCardsDoNotCauseLifeLoss() {
        Permanent blackCard = harness.addToBattlefieldAndReturn(player2, new ZulaportEnforcer());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, blackCard));
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    void chaosMakesEachOpponentDiscardOneCard() {
        harness.setHand(player2, List.of(new GrizzlyBears()));

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void millingMultipleCardsLosesLifeOnlyForNonblackCards() {
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new ZulaportEnforcer(), new HillGiant()));
        var graveyard = GameTestEngineContext.get().getBean(
                com.github.laxika.magicalvibes.service.graveyard.GraveyardService.class);

        harness.inMutationScope(() -> graveyard.resolveMillPlayer(gd, player2.getId(), 3));
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    void nonblackTokensDyingDoNotCauseLifeLoss() {
        GrizzlyBears tokenCard = new GrizzlyBears();
        tokenCard.setToken(true);
        Permanent token = harness.addToBattlefieldAndReturn(player2, tokenCard);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, token));
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void chaosDiscardingNonblackCardAlsoCausesLifeLossAndKeepsControllersHand() {
        harness.setHand(player1, List.of(new HillGiant()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new ZulaportEnforcer()));

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void chaosDiscardingBlackCardDoesNotCauseLifeLoss() {
        harness.setHand(player2, List.of(new ZulaportEnforcer()));

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Zulaport Enforcer");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void chaosWithEmptyOpponentHandDoesNotDiscardControllersCards() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of());

        harness.inMutationScope(() -> planar.chaos(gd));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
