package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InvasionPlans.class, YouthfulKnight.class})
class InvasionPlansTest extends BaseCardTest {

    @Test
    @DisplayName("The attacking player chooses blockers and every able creature must block")
    void attackingPlayerChoosesBlockersAndAllAbleCreaturesBlock() {
        harness.addToBattlefield(player1, new InvasionPlans());
        Permanent attacker = addCreatureReady(player1, new YouthfulKnight());
        Permanent blocker = addCreatureReady(player2, new YouthfulKnight());
        PendingInteraction.BlockerDeclaration pending = beginCombat(attacker);

        assertThat(pending.decidingPlayerId()).isEqualTo(player1.getId());
        assertThat(pending.defenderId()).isEqualTo(player2.getId());
        assertThat(pending.choosingForOpponent()).isTrue();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(blocker.getBlockingTargetIds()).contains(attacker.getId());
    }

    @Test
    @DisplayName("Tapped creatures are not required to block")
    void tappedCreaturesAreNotRequiredToBlock() {
        harness.addToBattlefield(player1, new InvasionPlans());
        Permanent attacker = addCreatureReady(player1, new YouthfulKnight());
        Permanent tappedBlocker = addCreatureReady(player2, new YouthfulKnight());
        tappedBlocker.tap();
        Permanent blocker = addCreatureReady(player2, new YouthfulKnight());
        PendingInteraction.BlockerDeclaration pending = beginCombat(attacker);

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(pending.decidingPlayerId()).isEqualTo(player1.getId());
        assertThat(tappedBlocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("The attacking player chooses how multiple blockers are assigned")
    void attackingPlayerChoosesEachBlockerAssignment() {
        harness.addToBattlefield(player1, new InvasionPlans());
        Permanent firstAttacker = addCreatureReady(player1, new YouthfulKnight());
        Permanent secondAttacker = addCreatureReady(player1, new YouthfulKnight());
        firstAttacker.setAttacking(true);
        firstAttacker.setAttackTarget(player2.getId());
        secondAttacker.setAttacking(true);
        secondAttacker.setAttackTarget(player2.getId());
        Permanent firstBlocker = addCreatureReady(player2, new YouthfulKnight());
        Permanent secondBlocker = addCreatureReady(player2, new YouthfulKnight());
        beginCombat(firstAttacker);

        int firstAttackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(firstAttacker);
        int secondAttackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(secondAttacker);
        int firstBlockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(firstBlocker);
        int secondBlockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(secondBlocker);
        gs.declareBlockers(gd, player1, List.of(
                new BlockerAssignment(firstBlockerIdx, secondAttackerIdx),
                new BlockerAssignment(secondBlockerIdx, firstAttackerIdx)));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(firstBlocker.getBlockingTargetIds()).containsExactly(secondAttacker.getId());
        assertThat(secondBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.getBlockingTargetIds()).containsExactly(firstAttacker.getId());
    }

    @Test
    @DisplayName("The attacking player chooses blockers regardless of Invasion Plans' controller")
    void attackingPlayerChoosesBlockersWhenDefenderControlsInvasionPlans() {
        harness.addToBattlefield(player2, new InvasionPlans());
        Permanent attacker = addCreatureReady(player1, new YouthfulKnight());
        Permanent blocker = addCreatureReady(player2, new YouthfulKnight());
        PendingInteraction.BlockerDeclaration pending = beginCombat(attacker);

        assertThat(pending.decidingPlayerId()).isEqualTo(player1.getId());
        assertThat(pending.defenderId()).isEqualTo(player2.getId());
        assertThat(pending.choosingForOpponent()).isTrue();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Losing Invasion Plans' abilities removes both static effects")
    void losingAbilitiesRemovesStaticEffects() {
        Permanent plans = harness.addToBattlefieldAndReturn(player1, new InvasionPlans());
        plans.setLosesAllAbilitiesUntilEndOfTurn(true);
        Permanent attacker = addCreatureReady(player1, new YouthfulKnight());
        addCreatureReady(player2, new YouthfulKnight());
        PendingInteraction.BlockerDeclaration pending = beginCombat(attacker);

        assertThat(pending.decidingPlayerId()).isEqualTo(player2.getId());
        assertThat(pending.choosingForOpponent()).isFalse();
        gs.declareBlockers(gd, player2, List.of());
    }

    private PendingInteraction.BlockerDeclaration beginCombat(Permanent attacker) {
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.inMutationScope(() -> harness.getCombatBlockService().handleDeclareBlockersStep(gd));
        return gd.interaction.activeInteraction(PendingInteraction.BlockerDeclaration.class);
    }
}
