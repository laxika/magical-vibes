package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BaronyVampire;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.p.PalladiumMyr;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
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

@CardUsed({Esper.class, BaronyVampire.class, GrizzlyBears.class, Memnite.class,
        PalladiumMyr.class, SavannahLions.class, WindDrake.class})
class EsperTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new Esper(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void artifactSpellsCostOneLessForThePlanarController() {
        assertThat(harness.getCastingCostService()
                .getCastCostModifier(gd, player1.getId(), new PalladiumMyr())).isEqualTo(-1);
        assertThat(harness.getCastingCostService()
                .getCastCostModifier(gd, player2.getId(), new PalladiumMyr())).isZero();
        assertThat(harness.getCastingCostService()
                .getCastCostModifier(gd, player1.getId(), new GrizzlyBears())).isZero();
    }

    @Test
    void chaosMakesMatchingCreaturesArtifactsAndGrantsKeywordsUntilEndOfTurn() {
        Permanent white = harness.addToBattlefieldAndReturn(player1, new SavannahLions());
        Permanent blue = harness.addToBattlefieldAndReturn(player1, new WindDrake());
        Permanent black = harness.addToBattlefieldAndReturn(player1, new BaronyVampire());
        Permanent green = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Memnite());
        Permanent opponentWhite = harness.addToBattlefieldAndReturn(player2, new SavannahLions());

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();

        assertThat(gqs.isArtifact(gd, white)).isTrue();
        assertThat(gqs.isArtifact(gd, blue)).isTrue();
        assertThat(gqs.isArtifact(gd, black)).isTrue();
        assertThat(gqs.isArtifact(gd, green)).isFalse();
        assertThat(gqs.isArtifact(gd, artifact)).isTrue();
        assertThat(gqs.isArtifact(gd, opponentWhite)).isFalse();

        for (Permanent permanent : List.of(white, blue, black, artifact)) {
            assertThat(permanent.hasKeyword(Keyword.VIGILANCE)).isTrue();
            assertThat(permanent.hasKeyword(Keyword.MENACE)).isTrue();
            assertThat(permanent.hasKeyword(Keyword.LIFELINK)).isTrue();
        }
        assertThat(green.hasKeyword(Keyword.VIGILANCE)).isFalse();
        assertThat(opponentWhite.hasKeyword(Keyword.MENACE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isArtifact(gd, white)).isFalse();
        assertThat(white.hasKeyword(Keyword.VIGILANCE)).isFalse();
        assertThat(artifact.hasKeyword(Keyword.LIFELINK)).isFalse();
    }
}
