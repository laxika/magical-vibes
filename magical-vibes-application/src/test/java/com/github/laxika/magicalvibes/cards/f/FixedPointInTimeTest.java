package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.planar.PlanarDieResult;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(FixedPointInTime.class)
class FixedPointInTimeTest extends BaseCardTest {
    private PlanechaseService planar;

    @BeforeEach
    void preparePlanechase() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void encounterReplacesPlanarDiePlaneswalkWithChaosAndThenLeaves() {
        FixedPointInTime fixedPoint = new FixedPointInTime();
        Card chaosPlane = chaosPlane();
        PlanarObject source = new PlanarObject(fixedPoint, gd.nextTimestamp());
        gd.planechase.faceUp.add(source);
        gd.planechase.deck.add(chaosPlane);

        planar.trigger(gd, source, EffectSlot.ENCOUNTER_TRIGGERED, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.planechase.faceUp).singleElement().extracting(PlanarObject::getCard)
                .isSameAs(chaosPlane);
        int cardsInHand = gd.playerHands.get(player1.getId()).size();

        planar.completeRoll(gd, player1.getId(), PlanarDieResult.PLANESWALKER);
        assertThat(gd.planechase.faceUp).singleElement().extracting(PlanarObject::getCard)
                .isSameAs(chaosPlane);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(cardsInHand + 1);
    }

    @Test
    void replacementExpiresAtItsControllersNextTurn() {
        FixedPointInTime fixedPoint = new FixedPointInTime();
        PlanarObject source = new PlanarObject(fixedPoint, gd.nextTimestamp());
        gd.planechase.faceUp.add(source);
        gd.planechase.deck.add(chaosPlane());

        planar.trigger(gd, source, EffectSlot.ENCOUNTER_TRIGGERED, player1.getId());
        harness.passBothPriorities();
        gd.expireFloatingEffectsAtTurnStart(player1.getId());

        Card nextPlane = new Card();
        nextPlane.setName("Next plane");
        nextPlane.setType(CardType.PLANE);
        gd.planechase.deck.add(nextPlane);
        planar.completeRoll(gd, player1.getId(), PlanarDieResult.PLANESWALKER);
        harness.passBothPriorities();

        assertThat(gd.planechase.faceUp).singleElement().extracting(PlanarObject::getCard)
                .isSameAs(nextPlane);
    }

    private Card chaosPlane() {
        Card plane = new Card();
        plane.setName("Chaos plane");
        plane.setType(CardType.PLANE);
        plane.addEffect(EffectSlot.CHAOS_TRIGGERED, new DrawCardEffect(1));
        return plane;
    }
}
