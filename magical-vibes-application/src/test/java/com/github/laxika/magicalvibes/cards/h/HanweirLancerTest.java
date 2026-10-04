package com.github.laxika.magicalvibes.cards.h;

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

@CardUsed({HanweirLancer.class, SomberwaldVigilante.class, PillarOfFlame.class, SoulSculptor.class})
class HanweirLancerTest extends BaseCardTest {

    private Permanent castAndPairWithPartner() {
        Permanent partner = harness.addToBattlefieldAndReturn(player1, new SomberwaldVigilante());
        harness.castFromHand(player1, new HanweirLancer(), "{2}{R}");
        harness.passBothPriorities(); // resolve spell -> soulbond may on stack
        harness.passBothPriorities(); // resolve may -> prompt
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, partner.getId());
        return partner;
    }

    private Permanent findLancer() {
        return findPermanent(player1, "Hanweir Lancer");
    }

    @Test
    @DisplayName("Soulbond ETB pairs Hanweir Lancer with another unpaired creature")
    void soulbondPairsOnEnter() {
        Permanent partner = castAndPairWithPartner();
        Permanent lancer = findLancer();

        assertThat(lancer.getPairedWithId()).isEqualTo(partner.getId());
        assertThat(partner.getPairedWithId()).isEqualTo(lancer.getId());
    }

    @Test
    @DisplayName("While paired, both creatures have first strike")
    void pairedBothHaveFirstStrike() {
        Permanent partner = castAndPairWithPartner();
        Permanent lancer = findLancer();

        assertThat(gqs.hasKeyword(gd, lancer, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, partner, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Unpaired Hanweir Lancer does not have first strike")
    void unpairedHasNoFirstStrike() {
        harness.addToBattlefield(player1, new HanweirLancer());
        Permanent lancer = findLancer();

        assertThat(lancer.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, lancer, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Declining soulbond leaves both unpaired and without first strike")
    void decliningLeavesUnpairedWithoutFirstStrike() {
        Permanent partner = harness.addToBattlefieldAndReturn(player1, new SomberwaldVigilante());
        harness.castFromHand(player1, new HanweirLancer(), "{2}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent lancer = findLancer();
        assertThat(lancer.getPairedWithId()).isNull();
        assertThat(partner.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, lancer, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, partner, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gd.interaction.permanentChoiceContext()).isNull();
    }

    @Test
    @DisplayName("Soulbond does not trigger when Hanweir Lancer enters without an eligible partner")
    void noTriggerWithoutPartner() {
        harness.addToBattlefield(player2, new SomberwaldVigilante());
        harness.castFromHand(player1, new HanweirLancer(), "{2}{R}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(findLancer().getPairedWithId()).isNull();
    }

    @Test
    @DisplayName("An entering creature can pair with an unpaired Hanweir Lancer")
    void pairsWithEnteringCreature() {
        Permanent lancer = harness.addToBattlefieldAndReturn(player1, new HanweirLancer());
        harness.castFromHand(player1, new SomberwaldVigilante(), "{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent partner = findPermanent(player1, "Somberwald Vigilante");
        assertThat(lancer.getPairedWithId()).isEqualTo(partner.getId());
        assertThat(partner.getPairedWithId()).isEqualTo(lancer.getId());
        assertThat(gqs.hasKeyword(gd, lancer, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, partner, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Pairing with an entering creature is optional")
    void declinesPairingWithEnteringCreature() {
        Permanent lancer = harness.addToBattlefieldAndReturn(player1, new HanweirLancer());
        harness.castFromHand(player1, new SomberwaldVigilante(), "{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent partner = findPermanent(player1, "Somberwald Vigilante");
        assertThat(lancer.getPairedWithId()).isNull();
        assertThat(partner.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, lancer, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, partner, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("A paired Lancer does not trigger soulbond for another entering creature")
    void cannotReplaceExistingPartner() {
        Permanent partner = castAndPairWithPartner();
        Permanent lancer = findLancer();
        harness.castFromHand(player1, new SomberwaldVigilante(), "{R}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(lancer.getPairedWithId()).isEqualTo(partner.getId());
        assertThat(partner.getPairedWithId()).isEqualTo(lancer.getId());
        Permanent newcomer = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> !p.getId().equals(partner.getId()) && !p.getId().equals(lancer.getId()))
                .findFirst().orElseThrow();
        assertThat(newcomer.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, newcomer, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("An opponent's entering creature does not trigger soulbond")
    void ignoresOpponentsEnteringCreature() {
        Permanent lancer = harness.addToBattlefieldAndReturn(player1, new HanweirLancer());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new SomberwaldVigilante(), "{R}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(lancer.getPairedWithId()).isNull();
        assertThat(findPermanent(player2, "Somberwald Vigilante").getPairedWithId()).isNull();
    }

    @Test
    @DisplayName("Lancer loses first strike when its partner leaves and can pair again")
    void partnerLeavesAndLancerCanPairAgain() {
        Permanent partner = castAndPairWithPartner();
        Permanent lancer = findLancer();
        harness.setHand(player1, List.of(new PillarOfFlame()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, partner.getId());

        assertThat(lancer.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, lancer, Keyword.FIRST_STRIKE)).isFalse();
        harness.castFromHand(player1, new SomberwaldVigilante(), "{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent newPartner = findPermanent(player1, "Somberwald Vigilante");
        assertThat(lancer.getPairedWithId()).isEqualTo(newPartner.getId());
        assertThat(newPartner.getPairedWithId()).isEqualTo(lancer.getId());
        assertThat(gqs.hasKeyword(gd, lancer, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, newPartner, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Pairing ends when the partner stops being a creature")
    void partnerStopsBeingCreature() {
        Permanent partner = castAndPairWithPartner();
        Permanent lancer = findLancer();
        addCreatureReady(player1, new SoulSculptor());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 2, null, partner.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, partner)).isFalse();
        assertThat(lancer.getPairedWithId()).isNull();
        assertThat(partner.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, lancer, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The surviving partner loses first strike when Hanweir Lancer leaves")
    void lancerLeaves() {
        Permanent partner = castAndPairWithPartner();
        Permanent lancer = findLancer();
        harness.setHand(player1, List.of(new PillarOfFlame()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, lancer.getId());

        harness.assertNotOnBattlefield(player1, "Hanweir Lancer");
        assertThat(partner.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, partner, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Pairing ends when Hanweir Lancer stops being a creature")
    void lancerStopsBeingCreature() {
        Permanent partner = castAndPairWithPartner();
        Permanent lancer = findLancer();
        addCreatureReady(player1, new SoulSculptor());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 2, null, lancer.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, lancer)).isFalse();
        assertThat(lancer.getPairedWithId()).isNull();
        assertThat(partner.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, partner, Keyword.FIRST_STRIKE)).isFalse();
    }
}
