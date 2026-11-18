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
    void artifactSpellsCostOneLessForEveryPlayer() {
        assertThat(harness.getCastingCostService()
                .getCastCostModifier(gd, player1.getId(), new PalladiumMyr())).isEqualTo(-1);
        assertThat(harness.getCastingCostService()
                .getCastCostModifier(gd, player2.getId(), new PalladiumMyr())).isEqualTo(-1);
        assertThat(harness.getCastingCostService()
                .getCastCostModifier(gd, player1.getId(), new GrizzlyBears())).isZero();
    }

    @Test
    void chaosDoesNotAffectCreaturesEnteringAfterItResolves() {
        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();

        Permanent white = harness.addToBattlefieldAndReturn(player1, new SavannahLions());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Memnite());

        assertThat(gqs.isArtifact(gd, white)).isFalse();
        for (Permanent permanent : List.of(white, artifact)) {
            assertThat(gqs.hasKeyword(gd, permanent, Keyword.VIGILANCE)).isFalse();
            assertThat(gqs.hasKeyword(gd, permanent, Keyword.MENACE)).isFalse();
            assertThat(gqs.hasKeyword(gd, permanent, Keyword.LIFELINK)).isFalse();
        }
    }

    @Test
    void chaosUsesCreaturesPresentAtResolutionRatherThanAtTriggerTime() {
        harness.inMutationScope(() -> planar.chaos(gd));
        Permanent white = harness.addToBattlefieldAndReturn(player1, new SavannahLions());

        harness.passBothPriorities();

        assertThat(gqs.isArtifact(gd, white)).isTrue();
        assertThat(gqs.hasKeyword(gd, white, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, white, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, white, Keyword.LIFELINK)).isTrue();
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
            assertThat(gqs.hasKeyword(gd, permanent, Keyword.VIGILANCE)).isTrue();
            assertThat(gqs.hasKeyword(gd, permanent, Keyword.MENACE)).isTrue();
            assertThat(gqs.hasKeyword(gd, permanent, Keyword.LIFELINK)).isTrue();
        }
        assertThat(gqs.hasKeyword(gd, green, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentWhite, Keyword.MENACE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isArtifact(gd, white)).isFalse();
        assertThat(gqs.hasKeyword(gd, white, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, artifact, Keyword.LIFELINK)).isFalse();
    }
}
