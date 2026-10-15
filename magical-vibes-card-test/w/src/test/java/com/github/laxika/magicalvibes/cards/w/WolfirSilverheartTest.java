package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.ImprisonedInTheMoon;
import com.github.laxika.magicalvibes.cards.m.MoorlandInquisitor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WolfirSilverheart.class, GrizzlyBears.class, MoorlandInquisitor.class, ImprisonedInTheMoon.class})
class WolfirSilverheartTest extends BaseCardTest {

    private Permanent castAndPairWithBears() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new WolfirSilverheart(), "{3}{G}{G}");
        harness.passBothPriorities(); // resolve spell -> soulbond may on stack
        harness.passBothPriorities(); // resolve may -> prompt
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        return bears;
    }

    private Permanent findWolfir() {
        return findPermanent(player1, "Wolfir Silverheart");
    }

    @Test
    @DisplayName("While paired, both creatures get +4/+4")
    void pairedBothGetBoost() {
        Permanent bears = castAndPairWithBears();
        Permanent wolfir = findWolfir();

        assertThat(wolfir.getPairedWithId()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, wolfir)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, wolfir)).isEqualTo(8);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(6);
    }

    @Test
    @DisplayName("Unpaired Wolfir Silverheart is a plain 4/4")
    void unpairedGetsNoBoost() {
        harness.addToBattlefield(player1, new WolfirSilverheart());
        Permanent wolfir = findWolfir();

        assertThat(wolfir.getPairedWithId()).isNull();
        assertThat(gqs.getEffectivePower(gd, wolfir)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, wolfir)).isEqualTo(4);
    }

    @Test
    @DisplayName("Declining soulbond leaves both creatures unboosted")
    void decliningLeavesBothUnboosted() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new WolfirSilverheart(), "{3}{G}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent wolfir = findWolfir();
        assertThat(wolfir.getPairedWithId()).isNull();
        assertThat(bears.getPairedWithId()).isNull();
        assertThat(gqs.getEffectivePower(gd, wolfir)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }

    @Test
    void enteringAloneDoesNotTriggerSoulbond() {
        harness.castFromHand(player1, new WolfirSilverheart(), "{3}{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findWolfir().getPairedWithId()).isNull();
    }

    @Test
    void opponentsCreatureDoesNotEnableOwnEntryTrigger() {
        harness.addToBattlefield(player2, new MoorlandInquisitor());
        harness.castFromHand(player1, new WolfirSilverheart(), "{3}{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findWolfir().getPairedWithId()).isNull();
    }

    @Test
    void pairsWithAnotherEnteringCreatureAndBoostsBoth() {
        Permanent wolfir = harness.addToBattlefieldAndReturn(player1, new WolfirSilverheart());
        harness.castFromHand(player1, new MoorlandInquisitor(), "{1}{W}");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        Permanent partner = findPermanent(player1, "Moorland Inquisitor");

        assertThat(wolfir.getPairedWithId()).isEqualTo(partner.getId());
        assertThat(partner.getPairedWithId()).isEqualTo(wolfir.getId());
        assertThat(gqs.getEffectivePower(gd, wolfir)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, wolfir)).isEqualTo(8);
        assertThat(gqs.getEffectivePower(gd, partner)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, partner)).isEqualTo(6);
    }

    @Test
    void canDeclinePairingWithAnotherEnteringCreature() {
        Permanent wolfir = harness.addToBattlefieldAndReturn(player1, new WolfirSilverheart());
        harness.castFromHand(player1, new MoorlandInquisitor(), "{1}{W}");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        Permanent partner = findPermanent(player1, "Moorland Inquisitor");

        assertThat(wolfir.getPairedWithId()).isNull();
        assertThat(partner.getPairedWithId()).isNull();
        assertThat(gqs.getEffectivePower(gd, wolfir)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, partner)).isEqualTo(2);
    }

    @Test
    void alreadyPairedWolfirDoesNotTriggerForAnotherCreature() {
        Permanent partner = castAndPairWithBears();
        harness.castFromHand(player1, new MoorlandInquisitor(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findWolfir().getPairedWithId()).isEqualTo(partner.getId());
        Permanent other = findPermanent(player1, "Moorland Inquisitor");
        assertThat(other.getPairedWithId()).isNull();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
    }

    @Test
    void partnerDyingEndsPairingAndRemovesBoost() {
        Permanent partner = castAndPairWithBears();
        partner.setMarkedDamage(6);
        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(findWolfir().getPairedWithId()).isNull();
        assertThat(gqs.getEffectivePower(gd, findWolfir())).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, findWolfir())).isEqualTo(4);
    }

    @Test
    void wolfirDyingRemovesPartnersBoost() {
        Permanent partner = castAndPairWithBears();
        findWolfir().setMarkedDamage(8);
        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Wolfir Silverheart");
        assertThat(partner.getPairedWithId()).isNull();
        assertThat(gqs.getEffectivePower(gd, partner)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, partner)).isEqualTo(2);
    }

    @Test
    void partnerBecomingLandEndsPairingAndRemovesBoost() {
        Permanent partner = castAndPairWithBears();
        harness.setHand(player1, List.of(new ImprisonedInTheMoon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, partner.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, partner)).isFalse();
        assertThat(partner.getPairedWithId()).isNull();
        assertThat(findWolfir().getPairedWithId()).isNull();
        assertThat(gqs.getEffectivePower(gd, findWolfir())).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, findWolfir())).isEqualTo(4);
    }
}
