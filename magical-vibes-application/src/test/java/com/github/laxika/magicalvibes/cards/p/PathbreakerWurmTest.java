package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PathbreakerWurmTest extends BaseCardTest {

    private Permanent castAndPairWithBears() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new PathbreakerWurm()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve spell -> soulbond may on stack
        harness.passBothPriorities(); // resolve may -> prompt
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        return bears;
    }

    private Permanent findWurm() {
        return findPermanent(player1, "Pathbreaker Wurm");
    }

    @Test
    @DisplayName("Soulbond ETB pairs Pathbreaker Wurm with another unpaired creature")
    void soulbondPairsOnEnter() {
        Permanent bears = castAndPairWithBears();
        Permanent wurm = findWurm();

        assertThat(wurm.getPairedWithId()).isEqualTo(bears.getId());
        assertThat(bears.getPairedWithId()).isEqualTo(wurm.getId());
    }

    @Test
    @DisplayName("While paired, both creatures have trample")
    void pairedBothHaveTrample() {
        Permanent bears = castAndPairWithBears();
        Permanent wurm = findWurm();

        assertThat(gqs.hasKeyword(gd, wurm, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
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
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new PathbreakerWurm()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent wurm = findWurm();
        assertThat(wurm.getPairedWithId()).isNull();
        assertThat(bears.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, wurm, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }
}
