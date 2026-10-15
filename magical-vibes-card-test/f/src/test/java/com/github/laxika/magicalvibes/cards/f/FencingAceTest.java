package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AzoriusArrester;
import com.github.laxika.magicalvibes.cards.c.ConcordiaPegasus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FencingAce.class, AzoriusArrester.class, ConcordiaPegasus.class})
class FencingAceTest extends BaseCardTest {

    @Test
    @DisplayName("Double strike — attacking a player deals combat damage twice (1 + 1 = 2)")
    void doubleStrikeDealsDamageTwice() {
        harness.setLife(player2, 20);

        Permanent attacker = addCreatureReady(player1, new FencingAce());
        attacker.setAttacking(true);
        resolveCombat();

        // Fencing Ace (1/1) with intrinsic double strike deals 1 + 1 = 2 damage.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Killing the blocker in first-strike damage does not deal regular damage to the player")
    void killedBlockerDoesNotLetDamageThrough() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new FencingAce());
        harness.addToBattlefield(player2, new AzoriusArrester());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Fencing Ace");
        harness.assertInGraveyard(player2, "Azorius Arrester");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A surviving blocker takes damage in both combat damage steps")
    void survivingBlockerTakesBothHits() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new FencingAce());
        harness.addToBattlefield(player2, new ConcordiaPegasus());
        Permanent blocker = findPermanent(player2, "Concordia Pegasus");

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Concordia Pegasus");
        harness.assertInGraveyard(player1, "Fencing Ace");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A blocking Fencing Ace kills an attacker before it deals regular combat damage")
    void blockingAceKillsBeforeRegularDamage() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new AzoriusArrester());
        harness.addToBattlefield(player2, new FencingAce());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Azorius Arrester");
        harness.assertOnBattlefield(player2, "Fencing Ace");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Two Fencing Aces deal lethal first-strike damage simultaneously")
    void doubleStrikersDieSimultaneously() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new FencingAce());
        harness.addToBattlefield(player2, new FencingAce());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Fencing Ace");
        harness.assertInGraveyard(player2, "Fencing Ace");
        harness.assertLife(player2, 20);
    }
}
