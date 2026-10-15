package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.m.MoorlandInquisitor;
import com.github.laxika.magicalvibes.cards.p.PeelFromReality;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Wingcrafter.class, MoorlandInquisitor.class, PeelFromReality.class})
class WingcrafterTest extends BaseCardTest {

    private Permanent castAndPairWithInquisitor() {
        Permanent inquisitor = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());
        harness.setHand(player1, List.of(new Wingcrafter()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve spell -> soulbond may on stack
        harness.passBothPriorities(); // resolve may -> prompt
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, inquisitor.getId());
        return inquisitor;
    }

    private Permanent findWingcrafter() {
        return findPermanent(player1, "Wingcrafter");
    }

    @Test
    @DisplayName("Soulbond ETB pairs Wingcrafter with another unpaired creature")
    void soulbondPairsOnEnter() {
        Permanent inquisitor = castAndPairWithInquisitor();
        Permanent wingcrafter = findWingcrafter();

        assertThat(wingcrafter.getPairedWithId()).isEqualTo(inquisitor.getId());
        assertThat(inquisitor.getPairedWithId()).isEqualTo(wingcrafter.getId());
    }

    @Test
    @DisplayName("While paired, both creatures have flying")
    void pairedBothHaveFlying() {
        Permanent inquisitor = castAndPairWithInquisitor();
        Permanent wingcrafter = findWingcrafter();

        assertThat(gqs.hasKeyword(gd, wingcrafter, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, inquisitor, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Unpaired Wingcrafter does not have flying")
    void unpairedHasNoFlying() {
        harness.addToBattlefield(player1, new Wingcrafter());
        Permanent wingcrafter = findWingcrafter();

        assertThat(wingcrafter.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, wingcrafter, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Declining soulbond leaves both unpaired and without flying")
    void decliningLeavesUnpairedWithoutFlying() {
        Permanent inquisitor = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());
        harness.setHand(player1, List.of(new Wingcrafter()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent wingcrafter = findWingcrafter();
        assertThat(wingcrafter.getPairedWithId()).isNull();
        assertThat(inquisitor.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, wingcrafter, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, inquisitor, Keyword.FLYING)).isFalse();
        assertThat(gd.interaction.permanentChoiceContext()).isNull();
    }

    @Test
    @DisplayName("Soulbond does not trigger on entry without another unpaired creature")
    void enteringAloneDoesNotTriggerSoulbond() {
        harness.setHand(player1, List.of(new Wingcrafter()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findWingcrafter().getPairedWithId()).isNull();
    }

    @Test
    @DisplayName("Soulbond can pair with another creature entering under your control")
    void pairsWithEnteringCreature() {
        harness.addToBattlefield(player1, new Wingcrafter());
        harness.setHand(player1, List.of(new MoorlandInquisitor()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent wingcrafter = findWingcrafter();
        Permanent inquisitor = findPermanent(player1, "Moorland Inquisitor");
        assertThat(wingcrafter.getPairedWithId()).isEqualTo(inquisitor.getId());
        assertThat(inquisitor.getPairedWithId()).isEqualTo(wingcrafter.getId());
        assertThat(gqs.hasKeyword(gd, wingcrafter, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, inquisitor, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Pairing with an entering creature is optional")
    void declinesPairingWithEnteringCreature() {
        harness.addToBattlefield(player1, new Wingcrafter());
        harness.setHand(player1, List.of(new MoorlandInquisitor()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent inquisitor = findPermanent(player1, "Moorland Inquisitor");
        assertThat(findWingcrafter().getPairedWithId()).isNull();
        assertThat(inquisitor.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, findWingcrafter(), Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, inquisitor, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Returning the partner to hand breaks the pair and removes flying")
    void partnerLeavingRemovesFlying() {
        Permanent inquisitor = castAndPairWithInquisitor();
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new MoorlandInquisitor());
        harness.setHand(player1, List.of(new PeelFromReality()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, List.of(inquisitor.getId(), opponentCreature.getId()));

        harness.assertInHand(player1, "Moorland Inquisitor");
        assertThat(findWingcrafter().getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, findWingcrafter(), Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("An already paired Wingcrafter does not pair with a new entering creature")
    void enteringCreatureDoesNotReplaceExistingPartner() {
        Permanent partner = castAndPairWithInquisitor();
        harness.setHand(player1, List.of(new MoorlandInquisitor()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findWingcrafter().getPairedWithId()).isEqualTo(partner.getId());
        Permanent entering = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Moorland Inquisitor"))
                .filter(p -> !p.getId().equals(partner.getId()))
                .findFirst().orElseThrow();
        assertThat(entering.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, entering, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Wingcrafter leaving removes flying from its surviving partner")
    void wingcrafterLeavingRemovesPartnerFlying() {
        Permanent partner = castAndPairWithInquisitor();
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new MoorlandInquisitor());
        harness.setHand(player1, List.of(new PeelFromReality()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0,
                List.of(findWingcrafter().getId(), opponentCreature.getId()));

        harness.assertInHand(player1, "Wingcrafter");
        assertThat(partner.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, partner, Keyword.FLYING)).isFalse();
    }
}
