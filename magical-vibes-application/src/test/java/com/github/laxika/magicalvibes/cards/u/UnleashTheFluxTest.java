package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Panopticon;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnleashTheFlux.class, Forest.class, GrizzlyBears.class, Panopticon.class})
class UnleashTheFluxTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlanechase() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.deck.add(new Panopticon());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void sacrificesNonlandPermanentsAndPlaneswalksAway() {
        Permanent playerOneCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent playerTwoCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent playerOneLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent playerTwoLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        gd.planechase.deck.addFirst(new UnleashTheFlux());

        harness.inMutationScope(() -> planar.reveal(gd, true));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(playerOneCreature)
                .contains(playerOneLand);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(playerTwoCreature)
                .contains(playerTwoLand);
        assertThat(gd.planechase.faceUp).singleElement()
                .extracting(PlanarObject::getCard)
                .isInstanceOf(Panopticon.class);
    }
}
