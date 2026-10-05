package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzledLeotau;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarisisTwinclaws.class, GrizzledLeotau.class})
class MarisisTwinclawsTest extends BaseCardTest {

    @Test
    @DisplayName("Double strike attacking a player deals combat damage twice (2 + 2 = 4)")
    void doubleStrikeDealsDamageTwice() {
        harness.setLife(player2, 20);

        Permanent attacker = addCreatureReady(player1, new MarisisTwinclaws());
        attacker.setAttacking(true);
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("A surviving blocker takes damage in both combat damage steps")
    void survivingBlockerTakesBothHits() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new MarisisTwinclaws());
        harness.addToBattlefield(player2, new GrizzledLeotau());
        Permanent blocker = findPermanent(player2, "Grizzled Leotau");

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(blocker.getMarkedDamage()).isEqualTo(4);
        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Marisi's Twinclaws");
        harness.assertOnBattlefield(player2, "Grizzled Leotau");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A blocking Twinclaws deals damage in both combat damage steps")
    void blockingTwinclawsDealsBothHits() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new GrizzledLeotau());
        harness.addToBattlefield(player2, new MarisisTwinclaws());
        Permanent blocker = findPermanent(player2, "Marisi's Twinclaws");

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(attacker.getMarkedDamage()).isEqualTo(4);
        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Grizzled Leotau");
        harness.assertOnBattlefield(player2, "Marisi's Twinclaws");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Opposing Twinclaws deal lethal damage simultaneously in the second damage step")
    void opposingTwinclawsDieSimultaneously() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new MarisisTwinclaws());
        harness.addToBattlefield(player2, new MarisisTwinclaws());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertNotOnBattlefield(player1, "Marisi's Twinclaws");
        harness.assertNotOnBattlefield(player2, "Marisi's Twinclaws");
        harness.assertInGraveyard(player1, "Marisi's Twinclaws");
        harness.assertInGraveyard(player2, "Marisi's Twinclaws");
        harness.assertLife(player2, 20);
    }
}
