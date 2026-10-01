package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BesiegedVikingVillage.class, GrizzlyBears.class})
class BesiegedVikingVillageTest extends BaseCardTest {

    private PlanechaseService planar;
    private TriggerCollectionService triggers;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        triggers = GameTestEngineContext.get().getBean(TriggerCollectionService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new BesiegedVikingVillage(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void allCreaturesCanUseTheGrantedBoastAbility() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        ownCreature.setAttackedThisTurn(true);
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());
        opposingCreature.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(ownCreature), 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(opposingCreature), 0, null, null);
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(opposingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
    }

    @Test
    void boastRequiresTheCreatureToHaveAttackedThisTurn() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                        player1, gd.playerBattlefields.get(player1.getId()).indexOf(creature), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacked this turn");
    }

    @Test
    void boastCanOnlyBeActivatedOnceEachTurn() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int battlefieldIndex = gd.playerBattlefields.get(player1.getId()).indexOf(creature);

        harness.activateAbility(player1, battlefieldIndex, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    void chaosPutsAnIndestructibleCounterOnlyOnAControlledCreatureThatAttackedThisTurn() {
        Permanent attacked = addCreatureReady(player1, new GrizzlyBears());
        attacked.setAttackedThisTurn(true);
        Permanent notAttacked = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentAttacked = addCreatureReady(player2, new GrizzlyBears());
        opponentAttacked.setAttackedThisTurn(true);

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> triggers.processNextSpellTargetTrigger(gd));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(attacked.getId());

        harness.handlePermanentChosen(player1, attacked.getId());
        harness.passBothPriorities();

        assertThat(attacked.getCounterCount(CounterType.INDESTRUCTIBLE)).isOne();
        assertThat(notAttacked.getCounterCount(CounterType.INDESTRUCTIBLE)).isZero();
        assertThat(opponentAttacked.getCounterCount(CounterType.INDESTRUCTIBLE)).isZero();
    }
}
