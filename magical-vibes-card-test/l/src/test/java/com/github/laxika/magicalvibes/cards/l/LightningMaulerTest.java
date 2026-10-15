package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.p.PillarOfFlame;
import com.github.laxika.magicalvibes.cards.s.SomberwaldVigilante;
import com.github.laxika.magicalvibes.cards.s.SoulSculptor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LightningMauler.class, SomberwaldVigilante.class, PillarOfFlame.class, SoulSculptor.class})
class LightningMaulerTest extends BaseCardTest {

    private Permanent castAndPairWithPartner() {
        Permanent partner = harness.addToBattlefieldAndReturn(player1, new SomberwaldVigilante());
        harness.castFromHand(player1, new LightningMauler(), "{1}{R}");
        harness.passBothPriorities(); // resolve spell -> soulbond may on stack
        harness.passBothPriorities(); // resolve may -> prompt
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, partner.getId());
        return partner;
    }

    private Permanent findMauler() {
        return findPermanent(player1, "Lightning Mauler");
    }

    @Test
    @DisplayName("Soulbond ETB pairs Lightning Mauler with another unpaired creature")
    void soulbondPairsOnEnter() {
        Permanent partner = castAndPairWithPartner();
        Permanent mauler = findMauler();

        assertThat(mauler.getPairedWithId()).isEqualTo(partner.getId());
        assertThat(partner.getPairedWithId()).isEqualTo(mauler.getId());
    }

    @Test
    @DisplayName("While paired, both creatures have haste")
    void pairedBothHaveHaste() {
        Permanent partner = castAndPairWithPartner();
        Permanent mauler = findMauler();

        assertThat(gqs.hasKeyword(gd, mauler, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, partner, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Unpaired Lightning Mauler does not have haste")
    void unpairedHasNoHaste() {
        harness.addToBattlefield(player1, new LightningMauler());
        Permanent mauler = findMauler();

        assertThat(mauler.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, mauler, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Declining soulbond leaves both unpaired and without haste")
    void decliningLeavesUnpairedWithoutHaste() {
        Permanent partner = harness.addToBattlefieldAndReturn(player1, new SomberwaldVigilante());
        harness.castFromHand(player1, new LightningMauler(), "{1}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent mauler = findMauler();
        assertThat(mauler.getPairedWithId()).isNull();
        assertThat(partner.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, mauler, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, partner, Keyword.HASTE)).isFalse();
        assertThat(gd.interaction.permanentChoiceContext()).isNull();
    }

    @Test
    @DisplayName("Soulbond does not trigger when Lightning Mauler enters without an eligible partner")
    void noTriggerWithoutPartner() {
        harness.addToBattlefield(player2, new SomberwaldVigilante());
        harness.castFromHand(player1, new LightningMauler(), "{1}{R}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(findMauler().getPairedWithId()).isNull();
    }

    @Test
    @DisplayName("An entering creature can pair with an unpaired Lightning Mauler")
    void pairsWithEnteringCreature() {
        Permanent mauler = harness.addToBattlefieldAndReturn(player1, new LightningMauler());
        harness.castFromHand(player1, new SomberwaldVigilante(), "{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent partner = findPermanent(player1, "Somberwald Vigilante");
        assertThat(mauler.getPairedWithId()).isEqualTo(partner.getId());
        assertThat(partner.getPairedWithId()).isEqualTo(mauler.getId());
        assertThat(gqs.hasKeyword(gd, mauler, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, partner, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Pairing with an entering creature is optional")
    void declinesPairingWithEnteringCreature() {
        Permanent mauler = harness.addToBattlefieldAndReturn(player1, new LightningMauler());
        harness.castFromHand(player1, new SomberwaldVigilante(), "{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent partner = findPermanent(player1, "Somberwald Vigilante");
        assertThat(mauler.getPairedWithId()).isNull();
        assertThat(partner.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, mauler, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, partner, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("A paired Mauler does not trigger soulbond for another entering creature")
    void cannotReplaceExistingPartner() {
        Permanent partner = castAndPairWithPartner();
        Permanent mauler = findMauler();
        harness.castFromHand(player1, new SomberwaldVigilante(), "{R}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(mauler.getPairedWithId()).isEqualTo(partner.getId());
        assertThat(partner.getPairedWithId()).isEqualTo(mauler.getId());
        Permanent newcomer = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> !p.getId().equals(partner.getId()) && !p.getId().equals(mauler.getId()))
                .findFirst().orElseThrow();
        assertThat(newcomer.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, newcomer, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("An opponent's entering creature does not trigger soulbond")
    void ignoresOpponentsEnteringCreature() {
        Permanent mauler = harness.addToBattlefieldAndReturn(player1, new LightningMauler());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new SomberwaldVigilante(), "{R}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(mauler.getPairedWithId()).isNull();
        assertThat(findPermanent(player2, "Somberwald Vigilante").getPairedWithId()).isNull();
    }

    @Test
    @DisplayName("Mauler loses haste when its partner leaves and can pair again")
    void partnerLeavesAndMaulerCanPairAgain() {
        Permanent partner = castAndPairWithPartner();
        Permanent mauler = findMauler();
        harness.setHand(player1, List.of(new PillarOfFlame()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, partner.getId());

        assertThat(mauler.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, mauler, Keyword.HASTE)).isFalse();
        harness.castFromHand(player1, new SomberwaldVigilante(), "{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent newPartner = findPermanent(player1, "Somberwald Vigilante");
        assertThat(mauler.getPairedWithId()).isEqualTo(newPartner.getId());
        assertThat(newPartner.getPairedWithId()).isEqualTo(mauler.getId());
        assertThat(gqs.hasKeyword(gd, mauler, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, newPartner, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Pairing ends when the partner stops being a creature")
    void partnerStopsBeingCreature() {
        Permanent partner = castAndPairWithPartner();
        Permanent mauler = findMauler();
        addCreatureReady(player1, new SoulSculptor());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 2, null, partner.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, partner)).isFalse();
        assertThat(mauler.getPairedWithId()).isNull();
        assertThat(partner.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, mauler, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("The surviving partner loses haste when Lightning Mauler leaves")
    void maulerLeaves() {
        Permanent partner = castAndPairWithPartner();
        Permanent mauler = findMauler();
        harness.setHand(player1, List.of(new PillarOfFlame()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, mauler.getId());

        harness.assertNotOnBattlefield(player1, "Lightning Mauler");
        assertThat(partner.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, partner, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Pairing ends when Lightning Mauler stops being a creature")
    void maulerStopsBeingCreature() {
        Permanent partner = castAndPairWithPartner();
        Permanent mauler = findMauler();
        addCreatureReady(player1, new SoulSculptor());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 2, null, mauler.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, mauler)).isFalse();
        assertThat(mauler.getPairedWithId()).isNull();
        assertThat(partner.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, partner, Keyword.HASTE)).isFalse();
    }
}
