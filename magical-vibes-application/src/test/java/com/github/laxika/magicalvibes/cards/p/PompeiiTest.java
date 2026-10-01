package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarDieResult;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Pompeii.class, Panopticon.class, AirElemental.class, Forest.class, GrizzlyBears.class})
class PompeiiTest extends BaseCardTest {

    private PlanechaseService planar;
    private PlanarObject pompeii;

    @BeforeEach
    void setupPlanarState() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        pompeii = new PlanarObject(new Pompeii(), gd.nextTimestamp());
        gd.planechase.faceUp.add(pompeii);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void arrivalAndUpkeepAddEruptionCounters() {
        harness.inMutationScope(() -> planar.trigger(gd, pompeii,
                EffectSlot.PLANESWALK_TO_TRIGGERED, player1.getId()));
        harness.passBothPriorities();

        assertThat(pompeii.getCounters().get(CounterType.ERUPTION)).isEqualTo(1);

        harness.forceStep(TurnStep.UPKEEP);
        GameTestEngineContext.get().getBean(StepTriggerService.class).handleUpkeepTriggers(gd);
        harness.passBothPriorities();

        assertThat(pompeii.getCounters().get(CounterType.ERUPTION)).isEqualTo(2);
    }

    @Test
    void blankRollScriesAndAddsEruptionCounter() {
        harness.inMutationScope(() -> planar.completeRoll(gd, player1.getId(), PlanarDieResult.BLANK));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNotNull();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(pompeii.getCounters().get(CounterType.ERUPTION)).isEqualTo(1);
    }

    @Test
    void nonblankRollDoesNotScryOrAddCounter() {
        harness.inMutationScope(() -> planarTriggers()
                .checkControllerRollsPlanarDieTriggers(gd, player1.getId(), 1));

        assertThat(gd.stack).isEmpty();
        assertThat(pompeii.getCounters()).doesNotContainKey(CounterType.ERUPTION);
    }

    @Test
    void chaosDamagesCreaturesAndPlayersSacrificeLandsThenPlaneswalks() {
        pompeii.getCounters().put(CounterType.ERUPTION, 2);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new AirElemental());
        gd.planechase.deck.add(new Panopticon());

        int player1Life = gd.getLife(player1.getId());
        int player2Life = gd.getLife(player2.getId());
        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(player1Life - 2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2Life - 2);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Forest"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Forest"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Air Elemental"));
        assertThat(gd.planechase.faceUp).extracting(object -> object.getCard().getName())
                .containsExactly("Panopticon");
    }

    private com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService planarTriggers() {
        return GameTestEngineContext.get().getBean(
                com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService.class);
    }
}
