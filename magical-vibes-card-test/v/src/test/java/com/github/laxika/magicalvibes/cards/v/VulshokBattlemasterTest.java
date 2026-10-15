package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.b.BoshIronGolem;
import com.github.laxika.magicalvibes.cards.k.KondasBanner;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.l.LightningGreaves;
import com.github.laxika.magicalvibes.cards.t.Terror;
import com.github.laxika.magicalvibes.cards.y.YotianSoldier;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VulshokBattlemaster.class, YotianSoldier.class, Bonesplitter.class,
        LeoninScimitar.class, BoshIronGolem.class, KondasBanner.class,
        LightningGreaves.class, Terror.class})
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

    @Test
    @DisplayName("Shroud granted by an attached Equipment does not stop other attachments")
    void attachesEquipmentDespiteShroud() {
        Permanent greaves = harness.addToBattlefieldAndReturn(player1, new LightningGreaves());
        Permanent bonesplitter = harness.addToBattlefieldAndReturn(player1, new Bonesplitter());

        Permanent battlemaster = castBattlemaster();

        assertThat(greaves.getAttachedTo()).isEqualTo(battlemaster.getId());
        assertThat(bonesplitter.getAttachedTo()).isEqualTo(battlemaster.getId());
    }

    @Test
    @DisplayName("Equipment stays on its old host if Battlemaster leaves before its trigger resolves")
    void leavesEquipmentInPlaceWhenSourceIsGone() {
        Permanent oldHost = harness.addToBattlefieldAndReturn(player2, new YotianSoldier());
        Permanent bonesplitter = harness.addToBattlefieldAndReturn(player2, new Bonesplitter());
        bonesplitter.setAttachedTo(oldHost.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new VulshokBattlemaster(), "{4}{R}");
        harness.passBothPriorities();
        Permanent battlemaster = findPermanent(player1, "Vulshok Battlemaster");

        harness.setHand(player2, List.of(new Terror()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, battlemaster.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(battlemaster);
        assertThat(bonesplitter.getAttachedTo()).isEqualTo(oldHost.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The trigger attaches Equipment present at resolution rather than only on entry")
    void includesEquipmentAddedBeforeTriggerResolves() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new VulshokBattlemaster(), "{4}{R}");
        harness.passBothPriorities();
        Permanent battlemaster = findPermanent(player1, "Vulshok Battlemaster");
        Permanent scimitar = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());

        harness.passBothPriorities();

        assertThat(scimitar.getAttachedTo()).isEqualTo(battlemaster.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(scimitar);
    }

    @Test
    @DisplayName("Haste allows Battlemaster to attack on the turn it enters")
    void attacksOnTheTurnItEnters() {
        Permanent battlemaster = castBattlemaster();

        declareAttackers(List.of(0));

        assertThat(battlemaster.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The trigger resolves harmlessly when there is no Equipment")
    void resolvesWithNoEquipment() {
        Permanent battlemaster = castBattlemaster();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(battlemaster);
        assertThat(gd.stack).isEmpty();
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
