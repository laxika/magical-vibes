package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PhantomWhelpTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking returns Phantom Whelp to its owner's hand at end of combat")
    void attackingReturnsItToHand() {
        harness.setLife(player2, 20);
        Permanent whelp = addReady(player1, new PhantomWhelp());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertNotOnBattlefield(player1, "Phantom Whelp");
        harness.assertInHand(player1, "Phantom Whelp");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(whelp.getId()));
    }

    @Test
    @DisplayName("Blocking returns Phantom Whelp to its owner's hand at end of combat")
    void blockingReturnsItToHand() {
        Permanent attacker = addReady(player1, new FugitiveWizard());
        attacker.setAttacking(true);
        addReady(player2, new PhantomWhelp());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Phantom Whelp");
        harness.assertInHand(player2, "Phantom Whelp");
    }

    @Test
    @DisplayName("Phantom Whelp is not returned if it leaves before end of combat")
    void notReturnedIfItLeavesBeforeEndOfCombat() {
        Permanent attacker = addReady(player1, new FugitiveWizard());
        attacker.setAttacking(true);
        Permanent whelp = addReady(player2, new PhantomWhelp());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        gd.playerBattlefields.get(player2.getId()).remove(whelp);
        harness.passBothPriorities();

        harness.assertNotInHand(player2, "Phantom Whelp");
    }

    private Permanent addReady(com.github.laxika.magicalvibes.model.Player player,
                               com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
