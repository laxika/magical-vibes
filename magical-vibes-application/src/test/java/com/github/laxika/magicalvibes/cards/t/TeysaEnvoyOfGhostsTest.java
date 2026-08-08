package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TeysaEnvoyOfGhostsTest extends BaseCardTest {

    @Test
    @DisplayName("Creature that deals combat damage to Teysa's controller is destroyed and a Spirit token is created")
    void combatDamageDestroysCreatureAndCreatesToken() {
        harness.addToBattlefield(player2, new TeysaEnvoyOfGhosts());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat(player1);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(countPermanents(player2, "Spirit")).isEqualTo(1);
    }

    @Test
    @DisplayName("Noncombat damage to Teysa's controller does not trigger her ability")
    void noncombatDamageDoesNotTrigger() {
        harness.addToBattlefield(player2, new TeysaEnvoyOfGhosts());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(countPermanents(player2, "Spirit")).isZero();
    }

    @Test
    @DisplayName("Teysa has protection from creatures, so an attacking creature cannot damage her")
    void protectionFromCreaturesPreventsCombatDamage() {
        Permanent teysa = harness.addToBattlefieldAndReturn(player2, new TeysaEnvoyOfGhosts());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(teysa.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Teysa, Envoy of Ghosts");
    }
}
