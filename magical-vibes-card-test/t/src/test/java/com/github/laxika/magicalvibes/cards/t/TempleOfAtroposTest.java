package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Panopticon;
import com.github.laxika.magicalvibes.model.Card;
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

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TempleOfAtropos.class, GrizzlyBears.class, Panopticon.class})
class TempleOfAtroposTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new TempleOfAtropos(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void createsAnAdditionalBeginningPhaseAfterPostcombatMain() {
        Permanent tappedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        tappedCreature.tap();
        Card draw = new GrizzlyBears();
        harness.setLibrary(player1, List.of(draw));

        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(tappedCreature.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).contains(draw);
    }

    @Test
    void chaosReversesTurnOrderThenPlaneswalks() {
        List<UUID> originalOrder = List.copyOf(gd.orderedPlayerIds);
        gd.planechase.deck.add(new Panopticon());

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();

        assertThat(gd.orderedPlayerIds).containsExactly(originalOrder.get(1), originalOrder.get(0));
        assertThat(gd.planechase.faceUp).singleElement().extracting(PlanarObject::getCard)
                .isInstanceOf(Panopticon.class);
    }
}
