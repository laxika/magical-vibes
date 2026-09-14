package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.p.Panopticon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BlankPlanarDieRollsCauseChaosEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RollPlanarDieEffect;
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

import java.util.List;

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
    void blankRollCausesChaosUntilThePhenomenonLeaves() {
        ChaoticAether aether = new ChaoticAether();
        aether.addEffect(EffectSlot.CHAOS_TRIGGERED, new DrawCardEffect(1));
        PlanarObject source = new PlanarObject(aether, gd.nextTimestamp());
        gd.planechase.faceUp.add(source);

        StackEntry encounter = new StackEntry(StackEntryType.TRIGGERED_ABILITY, aether, player1.getId(),
                "Chaotic Aether's ability", List.of(
                        new BlankPlanarDieRollsCauseChaosEffect(), new RollPlanarDieEffect()));
        encounter.setSourcePlanarObject(source.copy());
        gd.enqueueTrigger(encounter);
        when(die.roll()).thenReturn(PlanarDieResult.BLANK);

        int before = gd.playerHands.get(player1.getId()).size();
        harness.passBothPriorities();
        assertThat(gd.planechase.faceUp).singleElement().extracting(PlanarObject::getCard)
                .isSameAs(aether);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(before + 1);
        assertThat(gd.planechase.faceUp).singleElement().extracting(PlanarObject::getCard)
                .isInstanceOf(Panopticon.class);
    }
}
