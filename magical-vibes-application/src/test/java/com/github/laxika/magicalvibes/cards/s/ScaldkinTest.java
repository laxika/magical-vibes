package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScaldkinTest extends BaseCardTest {

    @Test
    @DisplayName("Scaldkin deals 2 damage to target player")
    void dealsDamageToPlayer() {
        addReadyScaldkin(player1);
        harness.setLife(player2, 20);
        addActivationMana(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Scaldkin is sacrificed as part of the activation cost")
    void sacrificedAsCost() {
        addReadyScaldkin(player1);
        addActivationMana(player1);

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Scaldkin");
        harness.assertInGraveyard(player1, "Scaldkin");
    }

    @Test
    @DisplayName("Scaldkin deals 2 damage to target creature")
    void dealsDamageToCreature() {
        addReadyScaldkin(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent target = findPermanent(player2, "Grizzly Bears");
        addActivationMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot activate Scaldkin without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyScaldkin(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyScaldkin(Player player) {
        Permanent permanent = new Permanent(new Scaldkin());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void addActivationMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.addMana(player, ManaColor.RED, 1);
    }
}
