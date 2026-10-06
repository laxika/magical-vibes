package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.IntrepidHero;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({SerraZealot.class, IntrepidHero.class})
class SerraZealotTest extends BaseCardTest {

    @Test
    @DisplayName("First strike kills a 1/1 attacker before regular combat damage")
    void firstStrikeKillsAttackerBeforeRegularDamage() {
        addCreatureReady(player1, new IntrepidHero());
        addCreatureReady(player2, new SerraZealot());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Intrepid Hero");
        harness.assertOnBattlefield(player2, "Serra Zealot");
    }

    @Test
    @DisplayName("First strike kills a 1/1 blocker before regular combat damage")
    void firstStrikeKillsBlockerBeforeRegularDamage() {
        addCreatureReady(player1, new SerraZealot());

        addCreatureReady(player2, new IntrepidHero());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Serra Zealot");
        harness.assertInGraveyard(player2, "Intrepid Hero");
    }

    @Test
    @DisplayName("Opposing first strikers deal lethal damage simultaneously")
    void opposingFirstStrikersTrade() {
        addCreatureReady(player1, new SerraZealot());
        addCreatureReady(player2, new SerraZealot());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Serra Zealot");
        harness.assertInGraveyard(player2, "Serra Zealot");
    }
}
