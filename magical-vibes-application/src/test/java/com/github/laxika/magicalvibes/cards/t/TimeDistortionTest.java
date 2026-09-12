package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.p.Panopticon;
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

@CardUsed({TimeDistortion.class, Panopticon.class})
class TimeDistortionTest extends BaseCardTest {

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
    void reversesTurnOrderAndPlaneswalksAway() {
        List<UUID> originalOrder = List.copyOf(gd.orderedPlayerIds);
        gd.planechase.deck.addFirst(new TimeDistortion());

        harness.inMutationScope(() -> planar.reveal(gd, true));
        harness.passBothPriorities();

        assertThat(gd.orderedPlayerIds).containsExactly(originalOrder.get(1), originalOrder.get(0));
        assertThat(gd.planechase.faceUp).singleElement().extracting(PlanarObject::getCard)
                .isInstanceOf(Panopticon.class);
    }
}
