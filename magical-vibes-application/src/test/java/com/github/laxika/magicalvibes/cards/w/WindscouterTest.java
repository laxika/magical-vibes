package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WindscouterTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking returns Windscouter to its owner's hand at end of combat")
    void attackingReturnsItToHand() {
        Permanent windscouter = addReady(player1, new Windscouter());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Windscouter");
        harness.assertInHand(player1, "Windscouter");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(windscouter.getId()));
    }

    @Test
    @DisplayName("Blocking returns Windscouter to its owner's hand at end of combat")
    void blockingReturnsItToHand() {
        Permanent attacker = addReady(player1, new FugitiveWizard());
        attacker.setAttacking(true);
        addReady(player2, new Windscouter());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Windscouter");
        harness.assertInHand(player2, "Windscouter");
    }

    @Test
    @DisplayName("Windscouter is not returned if it leaves before end of combat")
    void notReturnedIfItLeavesBeforeEndOfCombat() {
        Permanent attacker = addReady(player1, new FugitiveWizard());
        attacker.setAttacking(true);
        Permanent windscouter = addReady(player2, new Windscouter());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        gd.playerBattlefields.get(player2.getId()).remove(windscouter);
        harness.passBothPriorities();

        harness.assertNotInHand(player2, "Windscouter");
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
