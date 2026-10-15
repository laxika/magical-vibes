package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SpectroxMines.class)
class SpectroxMinesTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void planeswalkingToSpectroxMinesCostsThreeLifeAndCreatesTreasure() {
        gd.planechase.deck.add(new SpectroxMines());

        harness.inMutationScope(() -> planar.reveal(gd, true));
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void upkeepCostsThreeLifeAndCreatesTreasure() {
        gd.planechase.deck.add(new SpectroxMines());
        harness.inMutationScope(() -> planar.reveal(gd, true));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.UPKEEP);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(StepTriggerService.class)
                .handleUpkeepTriggers(gd));
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(14);
        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    @Test
    void chaosCreatesFoodAndHumanRogue() {
        gd.planechase.deck.add(new SpectroxMines());
        harness.inMutationScope(() -> planar.reveal(gd, true));
        harness.passBothPriorities();

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Food")).hasSize(1);
        assertThat(findPermanents(player1, "Human Rogue")).hasSize(1);
        Permanent rogue = findPermanents(player1, "Human Rogue").getFirst();
        assertThat(rogue.getCard().getColors()).containsExactly(CardColor.BLACK);
        assertThat(rogue.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.ROGUE);
        assertThat(gqs.getEffectivePower(gd, rogue)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rogue)).isEqualTo(2);
    }
}
