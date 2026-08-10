package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DominatingLicidTest extends BaseCardTest {

    @Test
    @DisplayName("Ability attaches the Licid to a creature and takes control of it")
    void abilityTurnsLicidIntoControlAura() {
        Permanent licid = addReadyLicid(player1);
        Permanent host = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        assertThat(licid.getAttachedTo()).isEqualTo(host.getId());
        assertThat(licid.getCard().isAura()).isTrue();
        assertThat(gd.findControllerOf(host)).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Paying the end cost reverts the Licid and returns control of the creature")
    void endCostRevertsLicidAndControl() {
        Permanent licid = addReadyLicid(player1);
        Permanent host = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(licid.getAttachedTo()).isNull();
        assertThat(licid.getCard().isAura()).isFalse();
        assertThat(gd.findControllerOf(host)).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetLand() {
        addReadyLicid(player1);
        Permanent land = addReadyLand(player2);
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addReadyLand(Player player) {
        Permanent perm = new Permanent(new Forest());
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyLicid(Player player) {
        Permanent perm = new Permanent(new DominatingLicid());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
