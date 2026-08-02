package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CinderElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X damage to target player")
    void dealsXDamageToPlayer() {
        addCreatureReady(player1, new CinderElemental());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Deals X damage to a target creature, killing it")
    void dealsXDamageToCreature() {
        addCreatureReady(player1, new CinderElemental());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID victim = harness.getPermanentId(player2, "Grizzly Bears");
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, 2, victim);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Sacrifices itself as part of the activation cost")
    void sacrificesItselfAsCost() {
        Permanent elemental = addCreatureReady(player1, new CinderElemental());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 1, player2.getId());

        assertThat(elemental.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Cinder Elemental");

        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }
}
