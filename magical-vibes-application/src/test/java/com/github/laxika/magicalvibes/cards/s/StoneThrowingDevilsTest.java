package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({StoneThrowingDevils.class, LlanowarElves.class})
class StoneThrowingDevilsTest extends BaseCardTest {

    @Test
    @DisplayName("First strike lets Stone-Throwing Devils destroy a 1/1 blocker before it deals combat damage")
    void firstStrikeDealsDamageBeforeRegularCombatDamage() {
        addCreatureReady(player1, new StoneThrowingDevils());
        addCreatureReady(player2, new LlanowarElves());

        declareAttackers(List.of(0));
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();

        harness.assertOnBattlefield(player1, "Stone-Throwing Devils");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }
}
