package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({Sokenzan.class, GrizzlyBears.class})
class SokenzanTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new Sokenzan(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void givesAllCreaturesPlusOnePlusOneAndHaste() {
        Permanent ownCreature = addReadyCreature(player1, new GrizzlyBears());
        Permanent opposingCreature = addReadyCreature(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.HASTE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.HASTE)).isTrue();

        gd.planechase.faceUp.clear();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.HASTE)).isFalse();
    }

    @Test
    void chaosUntapsCreaturesThatAttackedAndAddsCombatAndMainPhaseDuringMainPhase() {
        Permanent attackedOwnCreature = addReadyCreature(player1, new GrizzlyBears());
        Permanent attackedOpposingCreature = addReadyCreature(player2, new GrizzlyBears());
        Permanent nonAttackedCreature = addReadyCreature(player1, new GrizzlyBears());
        attackedOwnCreature.setAttackedThisTurn(true);
        attackedOpposingCreature.setAttackedThisTurn(true);
        attackedOwnCreature.tap();
        attackedOpposingCreature.tap();
        nonAttackedCreature.tap();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();

        assertThat(attackedOwnCreature.isTapped()).isFalse();
        assertThat(attackedOpposingCreature.isTapped()).isFalse();
        assertThat(nonAttackedCreature.isTapped()).isTrue();
        assertThat(gd.additionalCombatMainPhasePairs).isEqualTo(1);
    }

    @Test
    void chaosOutsideMainPhaseStillUntapsButDoesNotAddAnotherCombatAndMainPhase() {
        Permanent attackedCreature = addReadyCreature(player1, new GrizzlyBears());
        attackedCreature.setAttackedThisTurn(true);
        attackedCreature.tap();

        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();

        assertThat(attackedCreature.isTapped()).isFalse();
        assertThat(gd.additionalCombatMainPhasePairs).isZero();
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
