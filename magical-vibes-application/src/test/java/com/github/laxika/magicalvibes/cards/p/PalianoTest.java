package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Paliano.class, GrizzlyBears.class})
class PalianoTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new Paliano(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void combatDamageMakesControllerTheMonarchWhenThereIsNoMonarch() {
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    void combatDamageTakesTheMonarchFromTheDamagedPlayer() {
        gd.monarchPlayerId = player2.getId();
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    void chaosCreatesAnAssassinWithDeathtouchAndHaste() {
        harness.inMutationScope(() -> planar.chaos(gd));
        resolveAllTriggers();

        Permanent assassin = findPermanent(player1, "Assassin");
        assertThat(gqs.hasKeyword(gd, assassin, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, assassin, Keyword.HASTE)).isTrue();
    }

    @Test
    void existingMonarchPreventsPalianoFromTriggering() {
        gd.monarchPlayerId = player1.getId();
        addAttackingBear();
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    void simultaneousAttackersCreateOnlyOneMonarchTrigger() {
        addAttackingBear();
        addAttackingBear();
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    void monarchConditionIsCheckedAgainOnResolution() {
        addAttackingBear();
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        assertThat(gd.stack).hasSize(1);

        gd.monarchPlayerId = player2.getId();
        resolveAllTriggers();

        assertThat(gd.monarchPlayerId).isEqualTo(player2.getId());
    }

    @Test
    void chaosCreatesExactlyOneBlackOneOneAssassinForThePlanarController() {
        gd.planechase.controllerId = player2.getId();
        harness.forceActivePlayer(player2);

        harness.inMutationScope(() -> planar.chaos(gd));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        Permanent assassin = findPermanent(player2, "Assassin");
        assertThat(assassin.getCard().isToken()).isTrue();
        assertThat(assassin.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(assassin.getCard().getPower()).isEqualTo(1);
        assertThat(assassin.getCard().getToughness()).isEqualTo(1);
        assertThat(assassin.getCard().getSubtypes()).containsExactly(CardSubtype.ASSASSIN);
        assertThat(gqs.hasKeyword(gd, assassin, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, assassin, Keyword.HASTE)).isTrue();
    }

    private void addAttackingBear() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        bear.setAttacking(true);
        bear.setAttackTarget(player2.getId());
    }
}
