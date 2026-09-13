package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.Dodecapod;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PowerstoneMinefield.class, Dodecapod.class})
class PowerstoneMinefieldTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to each attacking creature")
    void damagesAttackingCreature() {
        harness.addToBattlefield(player1, new PowerstoneMinefield());
        Permanent attacker = addCreatureReady(player2, new Dodecapod());

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deals 2 damage separately to each attacking creature")
    void damagesEachAttackingCreature() {
        harness.addToBattlefield(player1, new PowerstoneMinefield());
        Permanent firstAttacker = addCreatureReady(player2, new Dodecapod());
        Permanent secondAttacker = addCreatureReady(player2, new Dodecapod());

        declareAttackers(player2, List.of(0, 1));
        resolveAllTriggers();

        assertThat(firstAttacker.getMarkedDamage()).isEqualTo(2);
        assertThat(secondAttacker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deals 2 damage to each blocking creature")
    void damagesBlockingCreature() {
        harness.addToBattlefield(player1, new PowerstoneMinefield());
        Permanent attacker = addCreatureReady(player1, new Dodecapod());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new Dodecapod());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
    }
}
