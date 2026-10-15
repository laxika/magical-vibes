package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Panopticon;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
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

@CardUsed({CaughtInAParallelUniverse.class, GrizzlyBears.class, LlanowarElves.class, Panopticon.class})
class CaughtInAParallelUniverseTest extends BaseCardTest {

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
    void eachPlayerCopiesACreatureControlledByThePlayerToTheirLeftWithMenace() {
        Permanent playerOneCreature = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent firstPlayerTwoCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        gd.planechase.deck.addFirst(new CaughtInAParallelUniverse());

        harness.inMutationScope(() -> planar.reveal(gd, true));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, firstPlayerTwoCreature.getId());
        harness.passBothPriorities();

        Permanent playerOneToken = findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        Permanent playerTwoToken = findPermanents(player2, "Llanowar Elves").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();

        assertThat(gqs.hasKeyword(gd, playerOneToken, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, playerTwoToken, Keyword.MENACE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(playerOneCreature);
        assertThat(gd.planechase.faceUp).singleElement()
                .extracting(PlanarObject::getCard)
                .isInstanceOf(Panopticon.class);
    }
}
