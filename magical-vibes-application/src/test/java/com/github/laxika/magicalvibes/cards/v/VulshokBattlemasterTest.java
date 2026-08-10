package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DarksteelAxe;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KondasBanner;
import com.github.laxika.magicalvibes.cards.k.KondaLordOfEiganjo;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VulshokBattlemasterTest extends BaseCardTest {

    @Test
    @DisplayName("Attaches all legally attachable Equipment on the battlefield")
    void attachesAllEquipment() {
        Permanent oldHost = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentHost = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent axe = harness.addToBattlefieldAndReturn(player1, new DarksteelAxe());
        Permanent scimitar = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        axe.setAttachedTo(oldHost.getId());
        scimitar.setAttachedTo(opponentHost.getId());

        Permanent battlemaster = castBattlemaster();

        assertThat(axe.getAttachedTo()).isEqualTo(battlemaster.getId());
        assertThat(scimitar.getAttachedTo()).isEqualTo(battlemaster.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(scimitar);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(scimitar);
    }

    @Test
    @DisplayName("Leaves an Equipment attached when it cannot legally attach")
    void leavesIllegalEquipmentWhereItWas() {
        Permanent oldHost = harness.addToBattlefieldAndReturn(player1, new KondaLordOfEiganjo());
        Permanent banner = harness.addToBattlefieldAndReturn(player1, new KondasBanner());
        banner.setAttachedTo(oldHost.getId());

        castBattlemaster();

        assertThat(banner.getAttachedTo()).isEqualTo(oldHost.getId());
    }

    private Permanent castBattlemaster() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new VulshokBattlemaster()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        return findPermanent(player1, "Vulshok Battlemaster");
    }
}
