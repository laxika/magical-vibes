package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.b.BoshIronGolem;
import com.github.laxika.magicalvibes.cards.k.KondasBanner;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.y.YotianSoldier;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VulshokBattlemaster.class, YotianSoldier.class, Bonesplitter.class,
        LeoninScimitar.class, BoshIronGolem.class, KondasBanner.class})
class VulshokBattlemasterTest extends BaseCardTest {

    @Test
    @DisplayName("Attaches all legally attachable Equipment on the battlefield")
    void attachesAllEquipment() {
        Permanent oldHost = harness.addToBattlefieldAndReturn(player1, new YotianSoldier());
        Permanent opponentHost = harness.addToBattlefieldAndReturn(player2, new YotianSoldier());
        Permanent bonesplitter = harness.addToBattlefieldAndReturn(player1, new Bonesplitter());
        Permanent scimitar = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        Permanent unattachedScimitar = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        bonesplitter.setAttachedTo(oldHost.getId());
        scimitar.setAttachedTo(opponentHost.getId());

        Permanent battlemaster = castBattlemaster();

        assertThat(bonesplitter.getAttachedTo()).isEqualTo(battlemaster.getId());
        assertThat(scimitar.getAttachedTo()).isEqualTo(battlemaster.getId());
        assertThat(unattachedScimitar.getAttachedTo()).isEqualTo(battlemaster.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(scimitar);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(scimitar);
    }

    @Test
    @DisplayName("Leaves an Equipment attached when it cannot legally attach")
    void leavesIllegalEquipmentWhereItWas() {
        Permanent oldHost = harness.addToBattlefieldAndReturn(player1, new BoshIronGolem());
        Permanent banner = harness.addToBattlefieldAndReturn(player1, new KondasBanner());
        banner.setAttachedTo(oldHost.getId());

        castBattlemaster();

        assertThat(banner.getAttachedTo()).isEqualTo(oldHost.getId());
    }

    private Permanent castBattlemaster() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new VulshokBattlemaster(), "{4}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        return findPermanent(player1, "Vulshok Battlemaster");
    }
}
