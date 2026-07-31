package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MartyrdomTest extends BaseCardTest {

    private void addBearsAndPyromancer() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new ProdigalPyromancer());
        for (Permanent perm : gd.playerBattlefields.get(player1.getId())) {
            perm.setSummoningSick(false);
        }
    }

    /** Casts Martyrdom on Grizzly Bears (battlefield index 0) and resolves it. */
    private void castMartyrdomOnBears() {
        harness.setHand(player1, List.of(new Martyrdom()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Granted ability redirects the next 1 damage dealt to you onto the granted creature")
    void redirectsDamageToPlayer() {
        addBearsAndPyromancer();
        harness.setLife(player1, 20);
        castMartyrdomOnBears();

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        // The Pyromancer pings player1 for 1 — that damage lands on Grizzly Bears instead.
        harness.activateAbility(player1, 1, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(findPermanent(player1, "Grizzly Bears").getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Granted ability redirects the next 1 damage dealt to a targeted creature")
    void redirectsDamageToCreature() {
        addBearsAndPyromancer();
        harness.addToBattlefield(player2, new SerraAngel());
        castMartyrdomOnBears();

        UUID angelId = harness.getPermanentId(player2, "Serra Angel");
        harness.activateAbility(player1, 0, null, angelId);
        harness.passBothPriorities();

        harness.activateAbility(player1, 1, null, angelId);
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Serra Angel").getMarkedDamage()).isEqualTo(0);
        assertThat(findPermanent(player1, "Grizzly Bears").getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Only the next 1 damage is redirected; the rest still hits the protected player")
    void redirectsOnlyOneDamage() {
        addBearsAndPyromancer();
        harness.addToBattlefield(player1, new ProdigalPyromancer());
        for (Permanent perm : gd.playerBattlefields.get(player1.getId())) {
            perm.setSummoningSick(false);
        }
        harness.setLife(player1, 20);
        castMartyrdomOnBears();

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 1, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(findPermanent(player1, "Grizzly Bears").getMarkedDamage()).isEqualTo(1);

        // Shield spent: the second Pyromancer's ping goes to player1's life total.
        harness.activateAbility(player1, 2, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(findPermanent(player1, "Grizzly Bears").getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentsCreature() {
        addBearsAndPyromancer();
        harness.addToBattlefield(player2, new SerraAngel());
        harness.setHand(player1, List.of(new Martyrdom()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID angelId = harness.getPermanentId(player2, "Serra Angel");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, angelId))
                .isInstanceOf(IllegalStateException.class);
    }
}
