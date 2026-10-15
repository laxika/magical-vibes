package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.n.NezumiInformant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KithkinBillyrider.class, NezumiInformant.class})
class KithkinBillyriderTest extends BaseCardTest {

    @Test
    @DisplayName("Double strike deals damage in both combat damage steps")
    void doubleStrikeDealsDamageInBothCombatDamageSteps() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new KithkinBillyrider());

        declareAttackers(player1, List.of(0));
        resolveCombat(player1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Killing a blocker in the first damage step does not damage the defending player")
    void killedBlockerDoesNotLetDamageThrough() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new KithkinBillyrider());
        harness.addToBattlefield(player2, new NezumiInformant());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Kithkin Billyrider");
        harness.assertInGraveyard(player2, "Nezumi Informant");
        assertThat(attacker.getMarkedDamage()).isZero();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A blocking Billyrider kills a small attacker before it can deal damage")
    void blockingBillyriderKillsBeforeRegularDamage() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new NezumiInformant());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new KithkinBillyrider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Nezumi Informant");
        harness.assertOnBattlefield(player2, "Kithkin Billyrider");
        assertThat(blocker.getMarkedDamage()).isZero();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Attacking and blocking Billyriders each deal damage in both steps")
    void survivingCombatantsDealDamageInBothSteps() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new KithkinBillyrider());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new KithkinBillyrider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Kithkin Billyrider");
        harness.assertOnBattlefield(player2, "Kithkin Billyrider");
        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
        harness.assertLife(player2, 20);
    }
}
