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

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();

        harness.assertOnBattlefield(player1, "Stone-Throwing Devils");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("First strike lets Stone-Throwing Devils destroy a 1/1 attacker before it deals combat damage")
    void firstStrikeWorksWhenBlocking() {
        addCreatureReady(player1, new LlanowarElves());
        addCreatureReady(player2, new StoneThrowingDevils());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertOnBattlefield(player2, "Stone-Throwing Devils");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Opposing Stone-Throwing Devils deal lethal first-strike damage simultaneously")
    void opposingFirstStrikersTrade() {
        addCreatureReady(player1, new StoneThrowingDevils());
        addCreatureReady(player2, new StoneThrowingDevils());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Stone-Throwing Devils");
        harness.assertInGraveyard(player2, "Stone-Throwing Devils");
        harness.assertNotOnBattlefield(player1, "Stone-Throwing Devils");
        harness.assertNotOnBattlefield(player2, "Stone-Throwing Devils");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Unblocked Stone-Throwing Devils deals combat damage only once")
    void firstStrikeDoesNotDealRegularCombatDamageToo() {
        addCreatureReady(player1, new StoneThrowingDevils());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player2, 19);
        harness.assertOnBattlefield(player1, "Stone-Throwing Devils");
    }
}
