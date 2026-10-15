package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.v.VotaryOfTheConclave;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BorosSwiftblade.class, VotaryOfTheConclave.class})
class BorosSwiftbladeTest extends BaseCardTest {

    @Test
    @DisplayName("Double strike deals combat damage twice")
    void doubleStrikeDealsDamageTwice() {
        harness.setLife(player2, 20);

        Permanent attacker = addCreatureReady(player1, new BorosSwiftblade());
        attacker.setAttacking(true);

        resolveCombat();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("A blocker killed in the first damage step deals no damage and does not let damage through")
    void killedBlockerDoesNotDealDamageOrLetDamageThrough() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new BorosSwiftblade());
        addCreatureReady(player2, new VotaryOfTheConclave());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player2, "Votary of the Conclave");
        harness.assertOnBattlefield(player1, "Boros Swiftblade");
        assertThat(attacker.getMarkedDamage()).isZero();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Double strike works while blocking and kills an attacker before it deals damage")
    void blockingSwiftbladeKillsAttackerBeforeNormalDamage() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new VotaryOfTheConclave());
        Permanent blocker = addCreatureReady(player2, new BorosSwiftblade());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Votary of the Conclave");
        harness.assertOnBattlefield(player2, "Boros Swiftblade");
        assertThat(blocker.getMarkedDamage()).isZero();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Opposing Swiftblades survive first strike damage and die to the second damage step")
    void opposingSwiftbladesDealDamageInBothSteps() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new BorosSwiftblade());
        addCreatureReady(player2, new BorosSwiftblade());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Boros Swiftblade");
        harness.assertInGraveyard(player2, "Boros Swiftblade");
        harness.assertNotOnBattlefield(player1, "Boros Swiftblade");
        harness.assertNotOnBattlefield(player2, "Boros Swiftblade");
        harness.assertLife(player2, 20);
    }
}
