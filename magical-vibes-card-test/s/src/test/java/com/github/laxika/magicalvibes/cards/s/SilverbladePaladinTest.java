package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.MoorlandInquisitor;
import com.github.laxika.magicalvibes.cards.a.AngelsTomb;
import com.github.laxika.magicalvibes.cards.t.ThunderousWrath;
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

@CardUsed({SilverbladePaladin.class, MoorlandInquisitor.class, ThunderousWrath.class, AngelsTomb.class})
class SilverbladePaladinTest extends BaseCardTest {

    private Permanent castAndPairWithInquisitor() {
        Permanent inquisitor = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());
        harness.setHand(player1, List.of(new SilverbladePaladin()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, inquisitor.getId());
        return inquisitor;
    }

    private Permanent findPaladin() {
        return findPermanent(player1, "Silverblade Paladin");
    }

    @Test
    @DisplayName("Soulbond ETB pairs Silverblade Paladin with another unpaired creature")
    void soulbondPairsOnEnter() {
        Permanent inquisitor = castAndPairWithInquisitor();
        Permanent paladin = findPaladin();

        assertThat(paladin.getPairedWithId()).isEqualTo(inquisitor.getId());
        assertThat(inquisitor.getPairedWithId()).isEqualTo(paladin.getId());
    }

    @Test
    @DisplayName("While paired, both creatures have double strike")
    void pairedBothHaveDoubleStrike() {
        Permanent inquisitor = castAndPairWithInquisitor();
        Permanent paladin = findPaladin();

        assertThat(gqs.hasKeyword(gd, paladin, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, inquisitor, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Unpaired Silverblade Paladin does not have double strike")
    void unpairedHasNoDoubleStrike() {
        harness.addToBattlefield(player1, new SilverbladePaladin());
        Permanent paladin = findPaladin();

        assertThat(paladin.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, paladin, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Declining soulbond leaves both unpaired and without double strike")
    void decliningLeavesUnpairedWithoutDoubleStrike() {
        Permanent inquisitor = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());
        harness.setHand(player1, List.of(new SilverbladePaladin()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        Permanent paladin = findPaladin();
        assertThat(paladin.getPairedWithId()).isNull();
        assertThat(inquisitor.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, paladin, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, inquisitor, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gd.interaction.permanentChoiceContext()).isNull();
    }

    @Test
    void noSoulbondTriggerWithoutAnotherUnpairedCreature() {
        harness.setHand(player1, List.of(new SilverbladePaladin()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void pairsWhenAnotherCreatureEnters() {
        Permanent paladin = harness.addToBattlefieldAndReturn(player1, new SilverbladePaladin());
        Permanent partner = harness.enterBattlefieldAndReturn(player1, new MoorlandInquisitor());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(paladin.getPairedWithId()).isEqualTo(partner.getId());
        assertThat(partner.getPairedWithId()).isEqualTo(paladin.getId());
        assertThat(gqs.hasKeyword(gd, paladin, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, partner, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void decliningAnotherCreatureEntryLeavesBothUnpaired() {
        Permanent paladin = harness.addToBattlefieldAndReturn(player1, new SilverbladePaladin());
        Permanent partner = harness.enterBattlefieldAndReturn(player1, new MoorlandInquisitor());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(paladin.getPairedWithId()).isNull();
        assertThat(partner.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, partner, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void opponentCreatureEntryDoesNotTriggerSoulbond() {
        Permanent paladin = harness.addToBattlefieldAndReturn(player1, new SilverbladePaladin());
        harness.enterBattlefieldAndReturn(player2, new MoorlandInquisitor());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(paladin.getPairedWithId()).isNull();
    }

    @Test
    void partnerDyingRemovesDoubleStrikeAndAllowsPairingAgain() {
        Permanent partner = castAndPairWithInquisitor();
        Permanent paladin = findPaladin();
        harness.setHand(player1, List.of(new ThunderousWrath()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.castAndResolveInstant(player1, 0, partner.getId());

        assertThat(paladin.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, paladin, Keyword.DOUBLE_STRIKE)).isFalse();
        Permanent replacement = harness.enterBattlefieldAndReturn(player1, new MoorlandInquisitor());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(paladin.getPairedWithId()).isEqualTo(replacement.getId());
        assertThat(gqs.hasKeyword(gd, replacement, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void pairingEndsWhenAnimatedPartnerStopsBeingACreature() {
        Permanent tomb = harness.addToBattlefieldAndReturn(player1, new AngelsTomb());
        harness.enterBattlefieldAndReturn(player1, new MoorlandInquisitor());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        Permanent paladin = harness.enterBattlefieldAndReturn(player1, new SilverbladePaladin());
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
        assertThat(paladin.getPairedWithId()).isEqualTo(tomb.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, tomb)).isFalse();
        assertThat(paladin.getPairedWithId()).isNull();
        assertThat(tomb.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, paladin, Keyword.DOUBLE_STRIKE)).isFalse();
    }
}
