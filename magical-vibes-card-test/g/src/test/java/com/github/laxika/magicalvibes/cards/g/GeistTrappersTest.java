package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.Cloudshift;
import com.github.laxika.magicalvibes.cards.v.Vorstclaw;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GeistTrappers.class, Vorstclaw.class, Cloudshift.class})
class GeistTrappersTest extends BaseCardTest {

    private Permanent castAndPairWithVorstclaw() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new Vorstclaw());
        harness.castFromHand(player1, new GeistTrappers(), "{4}{G}");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        return bears;
    }

    private Permanent findTrappers() {
        return findPermanent(player1, "Geist Trappers");
    }

    @Test
    void enteringAloneDoesNotTriggerSoulbond() {
        harness.castFromHand(player1, new GeistTrappers(), "{4}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Geist Trappers");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void opponentsCreatureDoesNotEnableSelfEnterSoulbond() {
        harness.addToBattlefield(player2, new Vorstclaw());
        harness.castFromHand(player1, new GeistTrappers(), "{4}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(findTrappers().getPairedWithId()).isNull();
    }

    @Test
    void anotherCreatureEnteringCanPairAndGainReach() {
        Permanent trappers = harness.addToBattlefieldAndReturn(player1, new GeistTrappers());
        harness.castFromHand(player1, new Vorstclaw(), "{4}{G}{G}");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        Permanent partner = findPermanent(player1, "Vorstclaw");
        assertThat(trappers.getPairedWithId()).isEqualTo(partner.getId());
        assertThat(partner.getPairedWithId()).isEqualTo(trappers.getId());
        assertThat(gqs.hasKeyword(gd, trappers, Keyword.REACH)).isTrue();
        assertThat(gqs.hasKeyword(gd, partner, Keyword.REACH)).isTrue();
    }

    @Test
    void anotherCreatureEnteringPairingCanBeDeclined() {
        Permanent trappers = harness.addToBattlefieldAndReturn(player1, new GeistTrappers());
        harness.castFromHand(player1, new Vorstclaw(), "{4}{G}{G}");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        Permanent partner = findPermanent(player1, "Vorstclaw");
        assertThat(trappers.getPairedWithId()).isNull();
        assertThat(partner.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, trappers, Keyword.REACH)).isFalse();
        assertThat(gqs.hasKeyword(gd, partner, Keyword.REACH)).isFalse();
    }

    @Test
    void pairedTrappersDoesNotPairWithThirdCreature() {
        Permanent partner = castAndPairWithVorstclaw();
        Permanent trappers = findTrappers();
        Permanent newcomer = harness.enterBattlefieldAndReturn(player1, new Vorstclaw());

        assertThat(trappers.getPairedWithId()).isEqualTo(partner.getId());
        assertThat(partner.getPairedWithId()).isEqualTo(trappers.getId());
        assertThat(newcomer.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, newcomer, Keyword.REACH)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void blinkingPartnerBreaksPairAndDecliningRePairingRemovesReach() {
        Permanent partner = castAndPairWithVorstclaw();
        Permanent trappers = findTrappers();
        harness.setHand(player1, java.util.List.of(new Cloudshift()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, partner.getId());

        Permanent returnedPartner = findPermanent(player1, "Vorstclaw");
        assertThat(returnedPartner.getId()).isNotEqualTo(partner.getId());
        assertThat(trappers.getPairedWithId()).isNull();
        assertThat(returnedPartner.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, trappers, Keyword.REACH)).isFalse();
        assertThat(gqs.hasKeyword(gd, returnedPartner, Keyword.REACH)).isFalse();

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(trappers.getPairedWithId()).isNull();
        assertThat(returnedPartner.getPairedWithId()).isNull();
    }

    @Test
    @DisplayName("Soulbond ETB pairs Geist Trappers with another unpaired creature")
    void soulbondPairsOnEnter() {
        Permanent bears = castAndPairWithVorstclaw();
        Permanent trappers = findTrappers();

        assertThat(trappers.getPairedWithId()).isEqualTo(bears.getId());
        assertThat(bears.getPairedWithId()).isEqualTo(trappers.getId());
    }

    @Test
    @DisplayName("While paired, both creatures have reach")
    void pairedBothHaveReach() {
        Permanent bears = castAndPairWithVorstclaw();
        Permanent trappers = findTrappers();

        assertThat(gqs.hasKeyword(gd, trappers, Keyword.REACH)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Unpaired Geist Trappers does not have reach")
    void unpairedHasNoReach() {
        harness.addToBattlefield(player1, new GeistTrappers());
        Permanent trappers = findTrappers();

        assertThat(trappers.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, trappers, Keyword.REACH)).isFalse();
    }

    @Test
    @DisplayName("Declining soulbond leaves both unpaired and without reach")
    void decliningLeavesUnpairedWithoutReach() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new Vorstclaw());
        harness.castFromHand(player1, new GeistTrappers(), "{4}{G}");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        Permanent trappers = findTrappers();
        assertThat(trappers.getPairedWithId()).isNull();
        assertThat(bears.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, trappers, Keyword.REACH)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.REACH)).isFalse();
    }
}
