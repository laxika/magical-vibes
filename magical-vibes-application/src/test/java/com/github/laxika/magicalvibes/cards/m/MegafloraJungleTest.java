package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MegafloraJungle.class, GrizzlyBears.class, ColossalDreadmaw.class})
class MegafloraJungleTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new MegafloraJungle(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void creaturesWithManaValueTwoOrLessGetPlusTwoPlusTwo() {
        Permanent smallCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent largeCreature = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());

        assertThat(gqs.getEffectivePower(gd, smallCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, smallCreature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, largeCreature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, largeCreature)).isEqualTo(6);
    }

    @Test
    void chaosCreatesFlyingInsectButterflyTokenForPlanarController() {
        harness.inMutationScope(() -> planar.chaos(gd));
        resolveAllTriggers();

        Permanent butterfly = findPermanents(player1, "Butterfly").getFirst();
        assertThat(butterfly.getEffectivePower()).isEqualTo(1);
        assertThat(butterfly.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.effectiveCreatureSubtypes(gd, butterfly)).contains(CardSubtype.INSECT);
        assertThat(gqs.hasKeyword(gd, butterfly, Keyword.FLYING)).isTrue();
        assertThat(findPermanents(player2, "Butterfly")).isEmpty();
    }
}
