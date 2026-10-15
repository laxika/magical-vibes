package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeaOfSand.class, Forest.class, GrizzlyBears.class})
class SeaOfSandTest extends BaseCardTest {
    private PlanechaseService planar;
    private TriggerCollectionService triggers;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        triggers = GameTestEngineContext.get().getBean(TriggerCollectionService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new SeaOfSand(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void landDrawRevealsAndGainsLife() {
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(new Forest()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gameLogContains("reveals Forest")).isTrue();
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
    }

    @Test
    void nonlandDrawRevealsAndLosesLife() {
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gameLogContains("reveals Grizzly Bears")).isTrue();
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
    }

    @Test
    void opponentDrawIsAlsoClassified() {
        harness.setLife(player2, 20);
        harness.setLibrary(player2, List.of(new Forest(), new GrizzlyBears()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));
        harness.passBothPriorities();
        harness.assertLife(player2, 23);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gameLogContains("reveals Forest")).isTrue();
        assertThat(gameLogContains("reveals Grizzly Bears")).isTrue();
    }

    @Test
    void multipleDrawsEachCreateALifeTriggerThatWaitsForResolution() {
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCards(gd, player1.getId(), 2));

        harness.assertLife(player1, 20);
        harness.assertInHand(player1, "Forest");
        harness.passBothPriorities();
        harness.assertLife(player1, 23);
        harness.passBothPriorities();
        harness.assertLife(player1, 26);
        harness.assertLife(player2, 20);
    }

    @Test
    void nonlandLifeLossWaitsForResolutionAndAffectsOnlyTheDrawingPlayer() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        harness.assertLife(player2, 20);
        harness.assertInHand(player2, "Grizzly Bears");
        harness.passBothPriorities();
        harness.assertLife(player2, 17);
        harness.assertLife(player1, 20);
    }

    @Test
    void chaosCanPutAnOpponentsLandOnTopOfItsOwnersLibrary() {
        Forest land = new Forest();
        land.setOwnerId(player1.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, land);
        GrizzlyBears previousTop = new GrizzlyBears();
        harness.setLibrary(player1, List.of(previousTop));
        harness.setLibrary(player2, List.of());

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> triggers.processNextSpellTargetTrigger(gd));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land, previousTop);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void chaosPutsTargetPermanentOnTopOfItsOwnersLibrary() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> triggers.processNextSpellTargetTrigger(gd));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst())
                .isSameAs(target.getCard());
    }
}
