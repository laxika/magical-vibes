package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConvulsingLicidTest extends BaseCardTest {

    @Test
    @DisplayName("Attached Licid prevents the enchanted creature from blocking")
    void attachedLicidPreventsBlocking() {
        Permanent licid = addReadyLicid(player1);
        Permanent host = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThat(bls.canBlock(gd, host)).isTrue();

        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        assertThat(licid.getAttachedTo()).isEqualTo(host.getId());
        assertThat(licid.getCard().isAura()).isTrue();
        assertThat(bls.canBlock(gd, host)).isFalse();
    }

    @Test
    @DisplayName("Paying the end cost restores the Licid and allows blocking again")
    void payingEndCostRestoresBlocking() {
        Permanent licid = addReadyLicid(player1);
        Permanent host = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(licid.getAttachedTo()).isNull();
        assertThat(licid.getCard().isAura()).isFalse();
        assertThat(bls.canBlock(gd, host)).isTrue();
    }

    private Permanent addReadyCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyLicid(Player player) {
        Permanent perm = new Permanent(new ConvulsingLicid());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
