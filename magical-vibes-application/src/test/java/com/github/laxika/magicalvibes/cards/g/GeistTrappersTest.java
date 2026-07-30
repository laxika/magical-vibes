package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GeistTrappersTest extends BaseCardTest {

    private Permanent castAndPairWithBears() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GeistTrappers()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve spell -> soulbond may on stack
        harness.passBothPriorities(); // resolve may -> prompt
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        return bears;
    }

    private Permanent findTrappers() {
        return findPermanent(player1, "Geist Trappers");
    }

    @Test
    @DisplayName("Soulbond ETB pairs Geist Trappers with another unpaired creature")
    void soulbondPairsOnEnter() {
        Permanent bears = castAndPairWithBears();
        Permanent trappers = findTrappers();

        assertThat(trappers.getPairedWithId()).isEqualTo(bears.getId());
        assertThat(bears.getPairedWithId()).isEqualTo(trappers.getId());
    }

    @Test
    @DisplayName("While paired, both creatures have reach")
    void pairedBothHaveReach() {
        Permanent bears = castAndPairWithBears();
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
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GeistTrappers()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent trappers = findTrappers();
        assertThat(trappers.getPairedWithId()).isNull();
        assertThat(bears.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, trappers, Keyword.REACH)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.REACH)).isFalse();
    }
}
