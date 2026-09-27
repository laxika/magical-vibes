package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
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

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Unyaro.class, GrizzlyBears.class})
class UnyaroTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new Unyaro(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void chaosCreatesTwoVigilantWhiteAndBlueKnightTokens() {
        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(2);
            assertThat(token.getCard().getToughness()).isEqualTo(2);
            assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLUE);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.KNIGHT);
            assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isTrue();
        });
    }

    @Test
    void planeswalkedToUnyaroEndStepUntapsAndPhasesOutAllCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        ownCreature.tap();
        opposingCreature.tap();
        gd.planechase.planeswalkedToTurn = gd.turnNumber;
        gd.planechase.planeswalkedToNamesThisTurn.add("Unyaro");

        harness.inMutationScope(() -> planar.step(gd, EffectSlot.CONTROLLER_END_STEP_TRIGGERED));
        harness.passBothPriorities();

        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(opposingCreature.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingCreature);
        List<Permanent> phasedOut = gd.phasedOutPermanents.values().stream().flatMap(List::stream).toList();
        assertThat(phasedOut)
                .containsExactlyInAnyOrder(ownCreature, opposingCreature);
    }

    @Test
    void creaturesHeldByUnyaroPhaseInWhenAPlayerPlaneswalks() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.planechase.planeswalkedToTurn = gd.turnNumber;
        gd.planechase.planeswalkedToNamesThisTurn.add("Unyaro");

        harness.inMutationScope(() -> planar.step(gd, EffectSlot.CONTROLLER_END_STEP_TRIGGERED));
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);

        harness.inMutationScope(() -> planar.finishPlaneswalk(gd, List.of()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.phasedOutUntilPlaneswalk).isEmpty();
        List<Permanent> phasedOut = gd.phasedOutPermanents.values().stream().flatMap(List::stream).toList();
        assertThat(phasedOut).doesNotContain(creature);
    }

    @Test
    void endStepAbilityDoesNotTriggerIfUnyaroWasNotEnteredThisTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.tap();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(creature.isTapped()).isTrue();
    }
}
