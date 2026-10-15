package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.v.Vorstclaw;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PathbreakerWurm.class, Vorstclaw.class})
class PathbreakerWurmTest extends BaseCardTest {

    private Permanent castAndPairWithPartner() {
        Permanent partner = harness.addToBattlefieldAndReturn(player1, new Vorstclaw());
        harness.setHand(player1, List.of(new PathbreakerWurm()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, partner.getId());
        return partner;
    }

    private Permanent findWurm() {
        return findPermanent(player1, "Pathbreaker Wurm");
    }

    @Test
    @DisplayName("Soulbond ETB pairs Pathbreaker Wurm with another unpaired creature")
    void soulbondPairsOnEnter() {
        Permanent partner = castAndPairWithPartner();
        Permanent wurm = findWurm();

        assertThat(wurm.getPairedWithId()).isEqualTo(partner.getId());
        assertThat(partner.getPairedWithId()).isEqualTo(wurm.getId());
    }

    @Test
    @DisplayName("While paired, both creatures have trample")
    void pairedBothHaveTrample() {
        Permanent partner = castAndPairWithPartner();
        Permanent wurm = findWurm();

        assertThat(gqs.hasKeyword(gd, wurm, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, partner, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Unpaired Pathbreaker Wurm does not have trample")
    void unpairedHasNoTrample() {
        harness.addToBattlefield(player1, new PathbreakerWurm());
        Permanent wurm = findWurm();

        assertThat(wurm.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, wurm, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Declining soulbond leaves both unpaired and without trample")
    void decliningLeavesUnpairedWithoutTrample() {
        Permanent partner = harness.addToBattlefieldAndReturn(player1, new Vorstclaw());
        harness.setHand(player1, List.of(new PathbreakerWurm()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        Permanent wurm = findWurm();
        assertThat(wurm.getPairedWithId()).isNull();
        assertThat(partner.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, wurm, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, partner, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void enteringWithoutAnotherUnpairedCreatureDoesNotTriggerSoulbond() {
        harness.enterBattlefieldAndReturn(player1, new PathbreakerWurm());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void pairsWithLaterEnteringCreatureAndOnlyPairGainsTrample() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new PathbreakerWurm());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new Vorstclaw());
        Permanent partner = harness.enterBattlefieldAndReturn(player1, new Vorstclaw());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(wurm.getPairedWithId()).isEqualTo(partner.getId());
        assertThat(partner.getPairedWithId()).isEqualTo(wurm.getId());
        assertThat(gqs.hasKeyword(gd, wurm, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, partner, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void mayDeclinePairingWithLaterEnteringCreature() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new PathbreakerWurm());
        Permanent partner = harness.enterBattlefieldAndReturn(player1, new Vorstclaw());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(wurm.getPairedWithId()).isNull();
        assertThat(partner.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, wurm, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, partner, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void opponentsEnteringCreatureDoesNotTriggerSoulbond() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new PathbreakerWurm());
        Permanent opponent = harness.enterBattlefieldAndReturn(player2, new Vorstclaw());

        assertThat(gd.stack).isEmpty();
        assertThat(wurm.getPairedWithId()).isNull();
        assertThat(opponent.getPairedWithId()).isNull();
    }

    @Test
    void partnerLeavingBreaksPairAndRemovesTrample() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new PathbreakerWurm());
        Permanent partner = harness.enterBattlefieldAndReturn(player1, new Vorstclaw());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gqs.hasKeyword(gd, wurm, Keyword.TRAMPLE)).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, partner));

        assertThat(wurm.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, wurm, Keyword.TRAMPLE)).isFalse();
    }
}
