package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({BalduvianBears.class, GrizzlyBears.class, HillGiant.class, SabretoothTiger.class})
class SabretoothTigerTest extends BaseCardTest {

    @Test
    @DisplayName("First strike does not prevent regular combat damage")
    void firstStrikeDoesNotPreventRegularCombatDamage() {
        addCreatureReady(player1, new SabretoothTiger());
        addCreatureReady(player2, new HillGiant());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Sabretooth Tiger");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("First strike kills a 2/2 blocker before regular combat damage")
    void firstStrikeKillsBlockerBeforeRegularDamage() {
        addCreatureReady(player1, new SabretoothTiger());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Sabretooth Tiger");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("First strike kills a 2/2 blocker before regular combat damage")
    void firstStrikeKillsBlockerBeforeRegularDamageUpstreamReview() {
        addCreatureReady(player1, new SabretoothTiger());
        addCreatureReady(player2, new BalduvianBears());

        declareAttackers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Sabretooth Tiger");
        harness.assertInGraveyard(player2, "Balduvian Bears");
    }
}
