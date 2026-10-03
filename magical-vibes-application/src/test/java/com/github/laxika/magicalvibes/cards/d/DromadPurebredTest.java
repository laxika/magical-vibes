package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.Char;
import com.github.laxika.magicalvibes.cards.v.VotaryOfTheConclave;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DromadPurebred.class, Char.class, VotaryOfTheConclave.class})
class DromadPurebredTest extends BaseCardTest {

    @Test
    void gainsLifeWhenDealtNoncombatDamage() {
        harness.setLife(player1, 20);
        Permanent dromad = harness.addToBattlefieldAndReturn(player1, new DromadPurebred());
        harness.setHand(player2, List.of(new Char()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player2, 0, dromad.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(dromad.getMarkedDamage()).isEqualTo(4);
        harness.assertOnBattlefield(player1, "Dromad Purebred");
    }

    @Test
    void gainsLifeWhenLethallyDamaged() {
        harness.setLife(player1, 20);
        Permanent dromad = harness.addToBattlefieldAndReturn(player1, new DromadPurebred());
        harness.setHand(player2, List.of(
                new Char(),
                new Char()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        for (int i = 0; i < 2; i++) {
            harness.castInstant(player2, 0, dromad.getId());
            resolveAllTriggers();
        }

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        harness.assertInGraveyard(player1, "Dromad Purebred");
    }

    @Test
    void gainsLifeWhenDealtCombatDamage() {
        harness.setLife(player1, 20);
        Permanent dromad = addCreatureReady(player1, new DromadPurebred());
        Permanent attacker = addCreatureReady(player2, new VotaryOfTheConclave());

        attacker.setAttacking(true);
        dromad.setBlocking(true);
        dromad.addBlockingTarget(0);

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        harness.assertOnBattlefield(player1, "Dromad Purebred");
        harness.assertInGraveyard(player2, "Votary of the Conclave");
    }

    @Test
    void gainsLifeOnlyOnceWhenTwoBlockersDealDamageSimultaneously() {
        harness.setLife(player1, 20);
        Permanent dromad = addCreatureReady(player1, new DromadPurebred());
        Permanent firstBlocker = addCreatureReady(player2, new VotaryOfTheConclave());
        Permanent secondBlocker = addCreatureReady(player2, new VotaryOfTheConclave());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                firstBlocker.getId(), 1,
                secondBlocker.getId(), 0));
        resolveAllTriggers();

        harness.assertLife(player1, 21);
        assertThat(dromad.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Dromad Purebred");
    }
}
