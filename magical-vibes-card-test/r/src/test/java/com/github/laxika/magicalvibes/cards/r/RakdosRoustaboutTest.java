package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.SenateCourier;
import com.github.laxika.magicalvibes.cards.d.DovinGrandArbiter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RakdosRoustabout.class, SenateCourier.class, DovinGrandArbiter.class})
class RakdosRoustaboutTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to the player it is attacking when it becomes blocked")
    void dealsDamageToAttackedPlayerWhenBlocked() {
        harness.setLife(player2, 20);
        Permanent roustabout = addCreatureReady(player1, new RakdosRoustabout());
        addCreatureReady(player2, new SenateCourier());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(roustabout.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Deals 1 damage to the planeswalker it is attacking when it becomes blocked")
    void dealsDamageToAttackedPlaneswalkerWhenBlocked() {
        Permanent roustabout = addCreatureReady(player1, new RakdosRoustabout());
        addCreatureReady(player2, new SenateCourier());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new DovinGrandArbiter());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);

        declareAttackers(player1, List.of(0), Map.of(0, planeswalker.getId()));
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(roustabout.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Does not trigger when it is not blocked")
    void doesNotTriggerWhenUnblocked() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new RakdosRoustabout());
        addCreatureReady(player2, new SenateCourier());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Multiple blockers cause only one damage trigger")
    void triggersOnceForMultipleBlockers() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new RakdosRoustabout());
        addCreatureReady(player2, new SenateCourier());
        addCreatureReady(player2, new SenateCourier());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(gd.stack).hasSize(1);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("The trigger still deals damage after Roustabout leaves the battlefield")
    void dealsDamageAfterSourceLeavesBattlefield() {
        harness.setLife(player2, 20);
        Permanent roustabout = addCreatureReady(player1, new RakdosRoustabout());
        addCreatureReady(player2, new SenateCourier());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, roustabout));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Rakdos Roustabout");
    }

    @Test
    @DisplayName("Damage is not redirected to the player when the attacked planeswalker leaves")
    void doesNotDamagePlayerAfterAttackedPlaneswalkerLeaves() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new RakdosRoustabout());
        addCreatureReady(player2, new SenateCourier());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new DovinGrandArbiter());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);

        declareAttackers(player1, List.of(0), Map.of(0, planeswalker.getId()));
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, planeswalker));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        harness.assertLife(player2, 20);
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices, Map<Integer, java.util.UUID> attackTargets) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices, attackTargets);
    }

}
