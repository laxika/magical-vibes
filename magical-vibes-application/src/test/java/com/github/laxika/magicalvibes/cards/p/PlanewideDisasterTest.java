package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PlanewideDisaster.class, GrizzlyBears.class, HowlingMine.class, Panopticon.class})
class PlanewideDisasterTest extends BaseCardTest {

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
    void encounterDestroysAllCreaturesThenPlaneswalksAway() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new HowlingMine());
        gd.planechase.deck.addFirst(new PlanewideDisaster());

        harness.inMutationScope(() -> planar.reveal(gd, true));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Howling Mine");
        assertThat(gd.planechase.faceUp).singleElement().extracting(object -> object.getCard())
                .isInstanceOf(Panopticon.class);
    }

    @Test
    void indestructibleCreatureSurvivesEncounter() {
        var creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        gd.planechase.deck.addFirst(new PlanewideDisaster());

        harness.inMutationScope(() -> planar.reveal(gd, true));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.planechase.faceUp).singleElement().extracting(object -> object.getCard())
                .isInstanceOf(Panopticon.class);
    }
}
