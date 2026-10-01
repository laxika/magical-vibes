package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KerblamWarehouse.class, DarksteelRelic.class, GrizzlyBears.class})
class KerblamWarehouseTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new KerblamWarehouse(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void combatDamageCreatesOneTreasureForTheDamageEvent() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(java.util.List.of(0, 1));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isOne();
    }

    @Test
    void chaosGrantsSacrificeCoinFlipAbilityUntilYourNextTurn() {
        Permanent firstRelic = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        Permanent secondRelic = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(firstRelic),
                0, null, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(firstRelic);
        assertThat(gd.getLife(player2.getId())).isIn(20, 17);

        secondRelic.clearUntilNextTurnEffects();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(secondRelic), 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
