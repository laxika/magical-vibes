package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.h.HuatliRadiantChampion;
import com.github.laxika.magicalvibes.cards.g.GraspingScoundrel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RagingRegisaur.class, GraspingScoundrel.class, HuatliRadiantChampion.class})
class RagingRegisaurTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking deals 1 damage to a target player")
    void attackingDealsDamageToPlayer() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new RagingRegisaur());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Attacking deals 1 damage to a target creature")
    void attackingDealsDamageToCreature() {
        addCreatureReady(player1, new RagingRegisaur());
        harness.addToBattlefield(player2, new GraspingScoundrel());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Grasping Scoundrel"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grasping Scoundrel");
        harness.assertInGraveyard(player2, "Grasping Scoundrel");
    }

    @Test
    @DisplayName("The attack trigger can target its controller before combat damage")
    void attackTriggerCanTargetController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new RagingRegisaur());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0));
            harness.handlePermanentChosen(player1, player1.getId());
            harness.passBothPriorities();

            harness.assertLife(player1, 19);
            harness.assertLife(player2, 20);
        });
    }

    @Test
    @DisplayName("The attack trigger can target Raging Regisaur itself")
    void attackTriggerCanTargetItself() {
        Permanent regisaur = addCreatureReady(player1, new RagingRegisaur());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0));
            harness.handlePermanentChosen(player1, regisaur.getId());
            harness.passBothPriorities();

            assertThat(regisaur.getMarkedDamage()).isEqualTo(1);
            harness.assertOnBattlefield(player1, "Raging Regisaur");
        });
    }

    @Test
    @DisplayName("The attack trigger can damage a planeswalker without attacking it")
    void attackTriggerCanTargetPlaneswalker() {
        addCreatureReady(player1, new RagingRegisaur());
        Permanent huatli = harness.addToBattlefieldAndReturn(player2, new HuatliRadiantChampion());
        huatli.setCounterCount(CounterType.LOYALTY, 3);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0));
            harness.handlePermanentChosen(player1, huatli.getId());
            harness.passBothPriorities();

            assertThat(huatli.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        });
    }

    @Test
    @DisplayName("The attack trigger still deals damage after its source leaves the battlefield")
    void attackTriggerResolvesAfterSourceLeaves() {
        harness.setLife(player2, 20);
        Permanent regisaur = addCreatureReady(player1, new RagingRegisaur());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0));
            harness.handlePermanentChosen(player1, player2.getId());
            harness.inMutationScope(() -> harness.getPermanentRemovalService()
                    .removePermanentToGraveyard(gd, regisaur));
            harness.passBothPriorities();

            harness.assertLife(player2, 19);
            harness.assertInGraveyard(player1, "Raging Regisaur");
        });
    }

    @Test
    @DisplayName("The attack trigger does not damage a creature that left the battlefield")
    void attackTriggerDoesNotFollowTargetToGraveyard() {
        addCreatureReady(player1, new RagingRegisaur());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RagingRegisaur());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0));
            harness.handlePermanentChosen(player1, target.getId());
            harness.inMutationScope(() -> harness.getPermanentRemovalService()
                    .removePermanentToGraveyard(gd, target));
            harness.passBothPriorities();

            assertThat(target.getMarkedDamage()).isZero();
            harness.assertInGraveyard(player2, "Raging Regisaur");
        });
    }
}
