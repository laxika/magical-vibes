package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VedalkenGhoulTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming blocked makes the defending player lose 4 life")
    void blockedDrainsDefendingPlayer() {
        addAttackingGhoul(player1, player2);
        addReadyBears(player2);
        int startingLife = gd.getLife(player2.getId());

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife - 4);
    }

    @Test
    @DisplayName("Unblocked Vedalken Ghoul does not fire the becomes-blocked drain (only combat damage)")
    void unblockedDoesNotDrain() {
        addAttackingGhoul(player1, player2);
        int startingLife = gd.getLife(player2.getId());

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        // Only the 1 combat damage lands; the 4-life becomes-blocked drain never triggers.
        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife - 1);
    }

    private Permanent addAttackingGhoul(Player attacker, Player defender) {
        Permanent perm = new Permanent(new VedalkenGhoul());
        perm.setSummoningSick(false);
        perm.setAttacking(true);
        perm.setAttackTarget(defender.getId());
        gd.playerBattlefields.get(attacker.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyBears(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void setupDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
