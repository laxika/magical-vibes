package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({ElvishArchers.class, GrizzlyBears.class})
class ElvishArchersTest extends BaseCardTest {

    @Test
    @DisplayName("First strike kills a 2/2 blocker before it deals combat damage")
    void firstStrikeKillsBlockerBeforeItDealsCombatDamage() {
        addCreatureReady(player1, new ElvishArchers()).setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Elvish Archers");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
