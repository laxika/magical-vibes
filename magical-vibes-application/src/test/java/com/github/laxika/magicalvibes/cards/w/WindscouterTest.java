package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.m.MarshBoa;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Windscouter.class, MarshBoa.class})
class WindscouterTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking returns Windscouter to its owner's hand at end of combat")
    void attackingReturnsItToHand() {
        Permanent windscouter = addCreatureReady(player1, new Windscouter());

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
        Permanent attacker = addCreatureReady(player1, new MarshBoa());
        attacker.setAttacking(true);
        addCreatureReady(player2, new Windscouter());

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
        Permanent attacker = addCreatureReady(player1, new MarshBoa());
        attacker.setAttacking(true);
        Permanent windscouter = addCreatureReady(player2, new Windscouter());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        gd.playerBattlefields.get(player2.getId()).remove(windscouter);
        harness.passBothPriorities();

        harness.assertNotInHand(player2, "Windscouter");
    }

    @Test
    @DisplayName("Windscouter is not returned if it dies in combat")
    void notReturnedIfItDiesInCombat() {
        Permanent attacker = addCreatureReady(player1, new Windscouter());
        attacker.setAttacking(true);
        addCreatureReady(player2, new Windscouter());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Windscouter");
        harness.assertNotInHand(player2, "Windscouter");
    }

}
