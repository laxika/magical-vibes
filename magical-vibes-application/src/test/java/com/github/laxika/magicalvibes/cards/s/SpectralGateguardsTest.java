package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SpectralGateguardsTest extends BaseCardTest {

    private Permanent castAndPairWithBears() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SpectralGateguards()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve spell -> soulbond may on stack
        harness.passBothPriorities(); // resolve may -> prompt
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        return bears;
    }

    private Permanent findGateguards() {
        return findPermanent(player1, "Spectral Gateguards");
    }

    @Test
    @DisplayName("Soulbond ETB pairs Spectral Gateguards with another unpaired creature")
    void soulbondPairsOnEnter() {
        Permanent bears = castAndPairWithBears();
        Permanent gateguards = findGateguards();

        assertThat(gateguards.getPairedWithId()).isEqualTo(bears.getId());
        assertThat(bears.getPairedWithId()).isEqualTo(gateguards.getId());
    }

    @Test
    @DisplayName("While paired, both creatures have vigilance")
    void pairedBothHaveVigilance() {
        Permanent bears = castAndPairWithBears();
        Permanent gateguards = findGateguards();

        assertThat(gqs.hasKeyword(gd, gateguards, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();
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
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SpectralGateguards()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent gateguards = findGateguards();
        assertThat(gateguards.getPairedWithId()).isNull();
        assertThat(bears.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, gateguards, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
        assertThat(gd.interaction.permanentChoiceContext()).isNull();
    }
}
