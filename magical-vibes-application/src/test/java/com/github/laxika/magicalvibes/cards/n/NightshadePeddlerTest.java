package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NightshadePeddlerTest extends BaseCardTest {

    private Permanent castAndPairWithBears() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new NightshadePeddler()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve spell -> soulbond may on stack
        harness.passBothPriorities(); // resolve may -> prompt
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        return bears;
    }

    private Permanent findPeddler() {
        return findPermanent(player1, "Nightshade Peddler");
    }

    @Test
    @DisplayName("Soulbond ETB pairs Nightshade Peddler with another unpaired creature")
    void soulbondPairsOnEnter() {
        Permanent bears = castAndPairWithBears();
        Permanent peddler = findPeddler();

        assertThat(peddler.getPairedWithId()).isEqualTo(bears.getId());
        assertThat(bears.getPairedWithId()).isEqualTo(peddler.getId());
    }

    @Test
    @DisplayName("While paired, both creatures have deathtouch")
    void pairedBothHaveDeathtouch() {
        Permanent bears = castAndPairWithBears();
        Permanent peddler = findPeddler();

        assertThat(gqs.hasKeyword(gd, peddler, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Unpaired Nightshade Peddler does not have deathtouch")
    void unpairedHasNoDeathtouch() {
        harness.addToBattlefield(player1, new NightshadePeddler());
        Permanent peddler = findPeddler();

        assertThat(peddler.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, peddler, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Declining soulbond leaves both unpaired and without deathtouch")
    void decliningLeavesUnpairedWithoutDeathtouch() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new NightshadePeddler()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent peddler = findPeddler();
        assertThat(peddler.getPairedWithId()).isNull();
        assertThat(bears.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, peddler, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.DEATHTOUCH)).isFalse();
    }
}
