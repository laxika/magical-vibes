package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnuridSwarmsnapper.class, GrizzlyBears.class})
class AnuridSwarmsnapperTest extends BaseCardTest {

    @Test
    @DisplayName("Anurid Swarmsnapper cannot block two creatures without activating its ability")
    void cannotBlockTwoCreaturesWithoutActivation() {
        Permanent swarmsnapper = addSwarmsnapper();
        addAttackers(2);

        beginBlockers();
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(swarmsnapper);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIdx, 0),
                new BlockerAssignment(blockerIdx, 1)
        ))).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activating Anurid Swarmsnapper lets it block two creatures")
    void blocksTwoCreaturesAfterActivating() {
        Permanent swarmsnapper = addSwarmsnapper();
        addAttackers(2);

        activate(swarmsnapper);
        beginBlockers();
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(swarmsnapper);

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIdx, 0),
                new BlockerAssignment(blockerIdx, 1)
        ));

        assertThat(swarmsnapper.getBlockingTargets()).containsExactlyInAnyOrder(0, 1);
    }

    @Test
    @DisplayName("The additional block grant expires at end of turn")
    void grantExpiresAtEndOfTurn() {
        Permanent swarmsnapper = addSwarmsnapper();
        activate(swarmsnapper);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        addAttackers(2);
        beginBlockers();
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(swarmsnapper);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIdx, 0),
                new BlockerAssignment(blockerIdx, 1)
        ))).isInstanceOf(IllegalStateException.class);
    }

    private Permanent addSwarmsnapper() {
        Permanent perm = new Permanent(new AnuridSwarmsnapper());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(perm);
        return perm;
    }

    private void activate(Permanent swarmsnapper) {
        int idx = gd.playerBattlefields.get(player2.getId()).indexOf(swarmsnapper);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
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
