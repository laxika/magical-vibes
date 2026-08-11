package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CoastlineChimeraTest extends BaseCardTest {

    @Test
    @DisplayName("Coastline Chimera cannot block two creatures without activating its ability")
    void cannotBlockTwoCreaturesWithoutActivation() {
        Permanent chimera = addChimera();
        addAttackers(2);

        beginBlockers();
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(chimera);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIdx, 0),
                new BlockerAssignment(blockerIdx, 1)
        ))).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activating the ability lets Coastline Chimera block two creatures")
    void blocksTwoCreaturesAfterActivating() {
        Permanent chimera = addChimera();
        addAttackers(2);

        activate(chimera);
        beginBlockers();
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(chimera);

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIdx, 0),
                new BlockerAssignment(blockerIdx, 1)
        ));

        assertThat(chimera.getBlockingTargets()).containsExactlyInAnyOrder(0, 1);
    }

    @Test
    @DisplayName("Activating the ability twice grants two additional blocks")
    void grantsStackingAdditionalBlocks() {
        Permanent chimera = addChimera();

        activate(chimera);
        activate(chimera);

        addAttackers(3);
        beginBlockers();
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(chimera);

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIdx, 0),
                new BlockerAssignment(blockerIdx, 1),
                new BlockerAssignment(blockerIdx, 2)
        ));

        assertThat(chimera.getBlockingTargets()).containsExactlyInAnyOrder(0, 1, 2);
    }

    @Test
    @DisplayName("The additional block grant expires at end of turn")
    void grantExpiresAtEndOfTurn() {
        Permanent chimera = addChimera();
        activate(chimera);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        addAttackers(2);
        beginBlockers();
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(chimera);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIdx, 0),
                new BlockerAssignment(blockerIdx, 1)
        ))).isInstanceOf(IllegalStateException.class);
    }

    private Permanent addChimera() {
        Permanent perm = new Permanent(new CoastlineChimera());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(perm);
        return perm;
    }

    private void activate(Permanent chimera) {
        int idx = gd.playerBattlefields.get(player2.getId()).indexOf(chimera);
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.activateAbility(player2, idx, null, null);
        harness.passBothPriorities();
    }

    private void addAttackers(int count) {
        for (int i = 0; i < count; i++) {
            Permanent attacker = new Permanent(new GrizzlyBears());
            attacker.setSummoningSick(false);
            attacker.setAttacking(true);
            attacker.setAttackTarget(player2.getId());
            gd.playerBattlefields.get(player1.getId()).add(attacker);
        }
    }

    private void beginBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
