package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.p.Panopticon;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarDieResult;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanarDieRoller;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@CardUsed({ChaoticAether.class, Panopticon.class})
class ChaoticAetherTest extends BaseCardTest {
    private PlanechaseService planar;
    private PlanarDieRoller die;
    private PlanarDieRoller originalDie;

    @BeforeEach
    void preparePlanechase() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        originalDie = (PlanarDieRoller) ReflectionTestUtils.getField(planar, "die");
        die = mock(PlanarDieRoller.class);
        ReflectionTestUtils.setField(planar, "die", die);

        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.deck.add(new Panopticon());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @AfterEach
    void restorePlanarDie() {
        ReflectionTestUtils.setField(planar, "die", originalDie);
    }

    @Test
    void encounterResolvesThenPlaneswalksAway() {
        gd.planechase.deck.addFirst(new ChaoticAether());

        harness.inMutationScope(() -> planar.reveal(gd, true));
        harness.passBothPriorities();

        assertThat(gd.planechase.faceUp).singleElement().extracting(PlanarObject::getCard)
                .isInstanceOf(Panopticon.class);
    }

    @Test
    void blankRollCausesChaosOnThePlaneAfterTheEncounter() {
        gd.planechase.deck.addFirst(new ChaoticAether());
        harness.inMutationScope(() -> planar.reveal(gd, true));
        harness.passBothPriorities();
        harness.passBothPriorities();

        when(die.roll()).thenReturn(PlanarDieResult.BLANK);
        int before = gd.playerHands.get(player1.getId()).size();
        harness.inMutationScope(() -> planar.rollSpecialAction(gd, player1.getId()));
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(before + 1);
        assertThat(gd.planechase.faceUp).singleElement().extracting(PlanarObject::getCard)
                .isInstanceOf(Panopticon.class);
    }

    @Test
    void effectEndsWhenAPlayerPlaneswalksAwayFromTheNextPlane() {
        gd.planechase.deck.addFirst(new ChaoticAether());
        gd.planechase.deck.addLast(new Panopticon());
        harness.inMutationScope(() -> planar.reveal(gd, true));
        harness.passBothPriorities();
        harness.passBothPriorities();

        var departing = gd.planechase.faceUp.getFirst().getId();
        when(die.roll()).thenReturn(PlanarDieResult.PLANESWALKER);
        harness.inMutationScope(() -> planar.rollSpecialAction(gd, player1.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.planechase.faceUp.getFirst().getId()).isNotEqualTo(departing);

        when(die.roll()).thenReturn(PlanarDieResult.BLANK);
        int before = gd.playerHands.get(player1.getId()).size();
        harness.inMutationScope(() -> planar.roll(gd, player1.getId()));
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(before);
    }
}
