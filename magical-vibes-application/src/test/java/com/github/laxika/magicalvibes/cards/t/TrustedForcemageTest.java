package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.m.MoorlandInquisitor;
import com.github.laxika.magicalvibes.cards.w.WanderingWolf;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TrustedForcemage.class, MoorlandInquisitor.class, WanderingWolf.class})
class TrustedForcemageTest extends BaseCardTest {

    private Permanent castAndPairWithPartner() {
        Permanent partner = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());
        harness.castFromHand(player1, new TrustedForcemage(), "{2}{G}");
        harness.passBothPriorities(); // resolve spell -> soulbond may on stack
        harness.passBothPriorities(); // resolve may -> prompt
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, partner.getId());
        return partner;
    }

    private Permanent forcemage() {
        return findPermanent(player1, "Trusted Forcemage");
    }

    @Test
    @DisplayName("Soulbond ETB pairs Trusted Forcemage with another unpaired creature")
    void soulbondPairsOnEnter() {
        Permanent partner = castAndPairWithPartner();
        Permanent forcemage = forcemage();

        assertThat(forcemage.getPairedWithId()).isEqualTo(partner.getId());
        assertThat(partner.getPairedWithId()).isEqualTo(forcemage.getId());
    }

    @Test
    @DisplayName("While paired, both creatures get +1/+1")
    void pairedBothGetBoost() {
        Permanent partner = castAndPairWithPartner();
        Permanent forcemage = forcemage();

        assertThat(gqs.getEffectivePower(gd, forcemage)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, forcemage)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, partner)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, partner)).isEqualTo(3);
    }

    @Test
    @DisplayName("Unpaired Trusted Forcemage gets no boost")
    void unpairedHasNoBoost() {
        harness.addToBattlefield(player1, new TrustedForcemage());
        Permanent forcemage = forcemage();

        assertThat(forcemage.getPairedWithId()).isNull();
        assertThat(gqs.getEffectivePower(gd, forcemage)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, forcemage)).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining soulbond leaves both unpaired and unboosted")
    void decliningLeavesUnpairedWithoutBoost() {
        Permanent partner = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());
        harness.castFromHand(player1, new TrustedForcemage(), "{2}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent forcemage = forcemage();
        assertThat(forcemage.getPairedWithId()).isNull();
        assertThat(partner.getPairedWithId()).isNull();
        assertThat(gqs.getEffectivePower(gd, forcemage)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, partner)).isEqualTo(2);
    }

    @Test
    @DisplayName("An unpaired third creature is not boosted")
    void unpairedBystanderIsNotBoosted() {
        castAndPairWithPartner();
        Permanent other = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());

        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
    }

    @Test
    void enteringAloneDoesNotTriggerSoulbond() {
        harness.castFromHand(player1, new TrustedForcemage(), "{2}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(forcemage().getPairedWithId()).isNull();
    }

    @Test
    void opponentsCreatureDoesNotEnableOwnEntryTrigger() {
        harness.addToBattlefield(player2, new WanderingWolf());
        harness.castFromHand(player1, new TrustedForcemage(), "{2}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(forcemage().getPairedWithId()).isNull();
    }

    @Test
    void pairsWithAnotherCreatureEnteringAndBoostsBoth() {
        Permanent mage = harness.addToBattlefieldAndReturn(player1, new TrustedForcemage());
        harness.castFromHand(player1, new WanderingWolf(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        Permanent wolf = findPermanent(player1, "Wandering Wolf");

        assertThat(mage.getPairedWithId()).isEqualTo(wolf.getId());
        assertThat(wolf.getPairedWithId()).isEqualTo(mage.getId());
        assertThat(gqs.getEffectivePower(gd, mage)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mage)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(2);
    }

    @Test
    void canDeclinePairingWithAnotherEnteringCreature() {
        Permanent mage = harness.addToBattlefieldAndReturn(player1, new TrustedForcemage());
        harness.castFromHand(player1, new WanderingWolf(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        Permanent wolf = findPermanent(player1, "Wandering Wolf");

        assertThat(mage.getPairedWithId()).isNull();
        assertThat(wolf.getPairedWithId()).isNull();
        assertThat(gqs.getEffectivePower(gd, mage)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);
    }

    @Test
    void alreadyPairedForcemageDoesNotTriggerForAnotherCreature() {
        Permanent partner = castAndPairWithPartner();
        harness.castFromHand(player1, new WanderingWolf(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(forcemage().getPairedWithId()).isEqualTo(partner.getId());
        assertThat(findPermanent(player1, "Wandering Wolf").getPairedWithId()).isNull();
    }

    @Test
    void partnerDyingEndsPairingAndRemovesBoost() {
        Permanent partner = castAndPairWithPartner();
        partner.setMarkedDamage(3);
        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Moorland Inquisitor");
        assertThat(forcemage().getPairedWithId()).isNull();
        assertThat(gqs.getEffectivePower(gd, forcemage())).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, forcemage())).isEqualTo(2);
    }
}
