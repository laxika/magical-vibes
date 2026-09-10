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
}
