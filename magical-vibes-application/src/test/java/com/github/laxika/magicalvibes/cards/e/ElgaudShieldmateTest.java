package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DreadStatuary;
import com.github.laxika.magicalvibes.cards.m.MistRaven;
import com.github.laxika.magicalvibes.cards.v.Vorstclaw;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElgaudShieldmate.class, Vorstclaw.class, MistRaven.class, DreadStatuary.class})
class ElgaudShieldmateTest extends BaseCardTest {

    private Permanent castAndPairWithPartner() {
        Permanent partner = harness.addToBattlefieldAndReturn(player1, new Vorstclaw());
        harness.castFromHand(player1, new ElgaudShieldmate(), "{3}{U}");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, partner.getId());
        return partner;
    }

    private Permanent findShieldmate() {
        return findPermanent(player1, "Elgaud Shieldmate");
    }

    @Test
    @DisplayName("Soulbond ETB pairs Elgaud Shieldmate with another unpaired creature")
    void soulbondPairsOnEnter() {
        Permanent bears = castAndPairWithPartner();
        Permanent shieldmate = findShieldmate();

        assertThat(shieldmate.getPairedWithId()).isEqualTo(bears.getId());
        assertThat(bears.getPairedWithId()).isEqualTo(shieldmate.getId());
    }

    @Test
    @DisplayName("While paired, both creatures have hexproof")
    void pairedBothHaveHexproof() {
        Permanent bears = castAndPairWithPartner();
        Permanent shieldmate = findShieldmate();

        assertThat(gqs.hasKeyword(gd, shieldmate, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Unpaired Elgaud Shieldmate does not have hexproof")
    void unpairedHasNoHexproof() {
        harness.addToBattlefield(player1, new ElgaudShieldmate());
        Permanent shieldmate = findShieldmate();

        assertThat(shieldmate.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, shieldmate, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Declining soulbond leaves both unpaired and without hexproof")
    void decliningLeavesUnpairedWithoutHexproof() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new Vorstclaw());
        harness.castFromHand(player1, new ElgaudShieldmate(), "{3}{U}");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        Permanent shieldmate = findShieldmate();
        assertThat(shieldmate.getPairedWithId()).isNull();
        assertThat(bears.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, shieldmate, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isFalse();
        assertThat(gd.interaction.permanentChoiceContext()).isNull();
    }

    @Test
    void enteringWithoutAnotherOwnCreatureDoesNotTriggerSoulbond() {
        harness.addToBattlefield(player2, new Vorstclaw());
        harness.castFromHand(player1, new ElgaudShieldmate(), "{3}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findShieldmate().getPairedWithId()).isNull();
    }

    @Test
    void anotherOwnCreatureEnteringCanPairWithShieldmate() {
        Permanent shieldmate = harness.addToBattlefieldAndReturn(player1, new ElgaudShieldmate());
        harness.castFromHand(player1, new Vorstclaw(), "{4}{G}{G}");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        Permanent partner = findPermanent(player1, "Vorstclaw");
        assertThat(shieldmate.getPairedWithId()).isEqualTo(partner.getId());
        assertThat(partner.getPairedWithId()).isEqualTo(shieldmate.getId());
        assertThat(gqs.hasKeyword(gd, shieldmate, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, partner, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    void decliningAnotherCreatureEnteringLeavesBothUnpaired() {
        Permanent shieldmate = harness.addToBattlefieldAndReturn(player1, new ElgaudShieldmate());
        harness.castFromHand(player1, new Vorstclaw(), "{4}{G}{G}");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        Permanent partner = findPermanent(player1, "Vorstclaw");
        assertThat(shieldmate.getPairedWithId()).isNull();
        assertThat(partner.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, shieldmate, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, partner, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    void pairedShieldmateDoesNotPairWithNewCreature() {
        Permanent partner = castAndPairWithPartner();
        Permanent shieldmate = findShieldmate();
        harness.castFromHand(player1, new Vorstclaw(), "{4}{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(shieldmate.getPairedWithId()).isEqualTo(partner.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()).getLast().getPairedWithId()).isNull();
    }

    @Test
    void opponentCreatureEnteringDoesNotTriggerSoulbond() {
        Permanent shieldmate = harness.addToBattlefieldAndReturn(player1, new ElgaudShieldmate());
        harness.enterBattlefieldAndReturn(player2, new Vorstclaw());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(shieldmate.getPairedWithId()).isNull();
    }

    @Test
    void controllerCanBouncePairedShieldmateAndPartnerLosesHexproof() {
        Permanent partner = castAndPairWithPartner();
        Permanent shieldmate = findShieldmate();
        harness.setHand(player1, List.of(new MistRaven()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0, shieldmate.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Elgaud Shieldmate");
        harness.assertInHand(player1, "Elgaud Shieldmate");
        assertThat(partner.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, partner, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    void pairingEndsWhenAnimatedLandStopsBeingACreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new DreadStatuary());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.castFromHand(player1, new ElgaudShieldmate(), "{3}{U}");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, land.getId());
        Permanent shieldmate = findShieldmate();
        assertThat(shieldmate.getPairedWithId()).isEqualTo(land.getId());
        assertThat(gqs.hasKeyword(gd, shieldmate, Keyword.HEXPROOF)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, land)).isFalse();
        assertThat(shieldmate.getPairedWithId()).isNull();
        assertThat(land.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, shieldmate, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, land, Keyword.HEXPROOF)).isFalse();
    }
}
