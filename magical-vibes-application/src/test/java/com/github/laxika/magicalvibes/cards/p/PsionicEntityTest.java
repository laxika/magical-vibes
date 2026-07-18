package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PsionicEntityTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to target player and 3 damage to itself — the 2/2 dies")
    void deals2ToPlayerAnd3ToSelf() {
        addReadyEntity(player1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertNotOnBattlefield(player1, "Psionic Entity");
        harness.assertInGraveyard(player1, "Psionic Entity");
    }

    @Test
    @DisplayName("Deals 2 damage to target creature, killing a 2/2, and 3 to itself")
    void deals2ToCreatureAnd3ToSelf() {
        addReadyEntity(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, bearsId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Psionic Entity");
    }

    @Test
    @DisplayName("2 damage does not kill a 4/4, but the entity still kills itself")
    void twoDamageDoesNotKillFourToughness() {
        addReadyEntity(player1);
        harness.addToBattlefield(player2, new AirElemental());
        UUID elementalId = harness.getPermanentId(player2, "Air Elemental");

        harness.activateAbility(player1, 0, null, elementalId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Air Elemental");
        harness.assertNotOnBattlefield(player1, "Psionic Entity");
    }

    @Test
    @DisplayName("Activating the ability taps Psionic Entity")
    void activatingTaps() {
        Permanent entity = addReadyEntity(player1);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(entity.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ability fizzles if target creature is removed — entity takes no self-damage")
    void fizzlesIfTargetRemoved() {
        Permanent entity = addReadyEntity(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, bearsId);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(entity.getMarkedDamage()).isEqualTo(0);
        harness.assertOnBattlefield(player1, "Psionic Entity");
    }

    @Test
    @DisplayName("Cannot activate the ability when already tapped")
    void cannotActivateWhenTapped() {
        Permanent entity = addReadyEntity(player1);
        entity.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    private Permanent addReadyEntity(Player player) {
        PsionicEntity card = new PsionicEntity();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
