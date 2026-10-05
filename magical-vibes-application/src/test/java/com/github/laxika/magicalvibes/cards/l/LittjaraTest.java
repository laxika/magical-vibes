package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Littjara.class, GrizzlyBears.class, GoblinPiker.class})
class LittjaraTest extends BaseCardTest {

    @Test
    void planeswalkToAndUpkeepCreateChangelingShapeshifters() {
        PlanechaseService planar = setupPlanarDeckWithLittjara();

        harness.inMutationScope(() -> planar.reveal(gd, true));
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Shapeshifter")).hasSize(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        GameTestEngineContext.get().getBean(StepTriggerService.class).handleUpkeepTriggers(gd);
        harness.passBothPriorities();

        var tokens = findPermanents(player1, "Shapeshifter");
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLUE);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SHAPESHIFTER);
            assertThat(token.getCard().getKeywords()).contains(Keyword.CHANGELING);
            assertThat(token.getEffectivePower()).isEqualTo(2);
            assertThat(token.getEffectiveToughness()).isEqualTo(2);
        });
    }

    @Test
    void chaosChoosesTypeAndCountersOnlyMatchingCreaturesYouControl() {
        PlanechaseService planar = setupPlanarWithLittjara();
        var ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        var ownGoblin = harness.addToBattlefieldAndReturn(player1, new GoblinPiker());
        var opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.BEAR.name());

        assertThat(ownBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownGoblin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opposingBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void upkeepCreatesTokenForTheNewActivePlayer() {
        setupPlanarWithLittjara();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Shapeshifter")).isEmpty();
        assertThat(findPermanents(player2, "Shapeshifter")).hasSize(1);
    }

    @Test
    void chaosCountersChangelingsForDifferentCreatureTypeChoices() {
        PlanechaseService planar = setupPlanarDeckWithLittjara();
        harness.inMutationScope(() -> planar.reveal(gd, true));
        harness.passBothPriorities();
        var token = findPermanents(player1, "Shapeshifter").getFirst();

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.BEAR.name());
        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());
        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void chaosAllowsChoosingTypeWithNoMatchingCreatures() {
        PlanechaseService planar = setupPlanarWithLittjara();

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.BEAR.name());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanents(player1, "Shapeshifter")).isEmpty();
    }

    private PlanechaseService setupPlanarDeckWithLittjara() {
        PlanechaseService planar = setupPlanarState();
        gd.planechase.deck.add(new Littjara());
        return planar;
    }

    private PlanechaseService setupPlanarWithLittjara() {
        PlanechaseService planar = setupPlanarState();
        gd.planechase.faceUp.add(new PlanarObject(new Littjara(), gd.nextTimestamp()));
        return planar;
    }

    private PlanechaseService setupPlanarState() {
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return GameTestEngineContext.get().getBean(PlanechaseService.class);
    }
}
