package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.MoorlandInquisitor;
import com.github.laxika.magicalvibes.cards.a.AngelsTomb;
import com.github.laxika.magicalvibes.cards.t.ThunderousWrath;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.TurnStep;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpectralGateguards.class, MoorlandInquisitor.class, ThunderousWrath.class, AngelsTomb.class})
class SpectralGateguardsTest extends BaseCardTest {

    private Permanent castAndPairWithInquisitor() {
        Permanent inquisitor = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());
        harness.setHand(player1, List.of(new SpectralGateguards()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, inquisitor.getId());
        return inquisitor;
    }

    private Permanent findGateguards() {
        return findPermanent(player1, "Spectral Gateguards");
    }

    @Test
    @DisplayName("Soulbond ETB pairs Spectral Gateguards with another unpaired creature")
    void soulbondPairsOnEnter() {
        Permanent inquisitor = castAndPairWithInquisitor();
        Permanent gateguards = findGateguards();

        assertThat(gateguards.getPairedWithId()).isEqualTo(inquisitor.getId());
        assertThat(inquisitor.getPairedWithId()).isEqualTo(gateguards.getId());
    }

    @Test
    @DisplayName("While paired, both creatures have vigilance")
    void pairedBothHaveVigilance() {
        Permanent inquisitor = castAndPairWithInquisitor();
        Permanent gateguards = findGateguards();

        assertThat(gqs.hasKeyword(gd, gateguards, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, inquisitor, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Unpaired Spectral Gateguards does not have vigilance")
    void unpairedHasNoVigilance() {
        harness.addToBattlefield(player1, new SpectralGateguards());
        Permanent gateguards = findGateguards();

        assertThat(gateguards.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, gateguards, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Declining soulbond leaves both unpaired and without vigilance")
    void decliningLeavesUnpairedWithoutVigilance() {
        Permanent inquisitor = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());
        harness.setHand(player1, List.of(new SpectralGateguards()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        Permanent gateguards = findGateguards();
        assertThat(gateguards.getPairedWithId()).isNull();
        assertThat(inquisitor.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, gateguards, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, inquisitor, Keyword.VIGILANCE)).isFalse();
        assertThat(gd.interaction.permanentChoiceContext()).isNull();
    }
    @Test
    void noSoulbondTriggerWithoutAnotherUnpairedCreature() {
        harness.setHand(player1, List.of(new SpectralGateguards()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void pairsWhenAnotherCreatureEnters() {
        Permanent gateguards = harness.addToBattlefieldAndReturn(player1, new SpectralGateguards());
        Permanent partner = harness.enterBattlefieldAndReturn(player1, new MoorlandInquisitor());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gateguards.getPairedWithId()).isEqualTo(partner.getId());
        assertThat(partner.getPairedWithId()).isEqualTo(gateguards.getId());
        assertThat(gqs.hasKeyword(gd, gateguards, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, partner, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void decliningAnotherCreatureEntryLeavesBothUnpaired() {
        Permanent gateguards = harness.addToBattlefieldAndReturn(player1, new SpectralGateguards());
        Permanent partner = harness.enterBattlefieldAndReturn(player1, new MoorlandInquisitor());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gateguards.getPairedWithId()).isNull();
        assertThat(partner.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, gateguards, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, partner, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void opponentCreatureEntryDoesNotTriggerSoulbond() {
        Permanent gateguards = harness.addToBattlefieldAndReturn(player1, new SpectralGateguards());
        harness.enterBattlefieldAndReturn(player2, new MoorlandInquisitor());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gateguards.getPairedWithId()).isNull();
    }

    @Test
    void alreadyPairedGateguardsDoesNotPairWithANewCreature() {
        Permanent partner = castAndPairWithInquisitor();
        Permanent gateguards = findGateguards();
        Permanent newcomer = harness.enterBattlefieldAndReturn(player1, new MoorlandInquisitor());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gateguards.getPairedWithId()).isEqualTo(partner.getId());
        assertThat(newcomer.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, newcomer, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void partnerDyingRemovesVigilanceAndAllowsPairingAgain() {
        Permanent partner = castAndPairWithInquisitor();
        Permanent gateguards = findGateguards();
        harness.setHand(player1, List.of(new ThunderousWrath()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.castAndResolveInstant(player1, 0, partner.getId());

        harness.assertInGraveyard(player1, "Moorland Inquisitor");
        assertThat(gateguards.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, gateguards, Keyword.VIGILANCE)).isFalse();
        Permanent replacement = harness.enterBattlefieldAndReturn(player1, new MoorlandInquisitor());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gateguards.getPairedWithId()).isEqualTo(replacement.getId());
        assertThat(gqs.hasKeyword(gd, replacement, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void gateguardsDyingRemovesPartnersVigilance() {
        Permanent partner = castAndPairWithInquisitor();
        Permanent gateguards = findGateguards();
        harness.setHand(player1, List.of(new ThunderousWrath()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.castAndResolveInstant(player1, 0, gateguards.getId());

        harness.assertInGraveyard(player1, "Spectral Gateguards");
        assertThat(partner.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, partner, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void pairingEndsWhenAnimatedPartnerStopsBeingACreature() {
        Permanent tomb = harness.addToBattlefieldAndReturn(player1, new AngelsTomb());
        harness.enterBattlefieldAndReturn(player1, new MoorlandInquisitor());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        Permanent gateguards = harness.enterBattlefieldAndReturn(player1, new SpectralGateguards());
        resolveAllTriggers();
        while (gd.interaction.isAwaitingInput() && gd.interaction.permanentChoiceContext() == null) {
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();
        }
        harness.handlePermanentChosen(player1, tomb.getId());
        resolveAllTriggers();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();
        }
        assertThat(gateguards.getPairedWithId()).isEqualTo(tomb.getId());
        assertThat(gqs.hasKeyword(gd, tomb, Keyword.VIGILANCE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, tomb)).isFalse();
        assertThat(gateguards.getPairedWithId()).isNull();
        assertThat(tomb.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, gateguards, Keyword.VIGILANCE)).isFalse();
    }
}
