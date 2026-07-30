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

class NearheathPilgrimTest extends BaseCardTest {

    private Permanent castAndPairWithBears() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new NearheathPilgrim()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve spell -> soulbond may on stack
        harness.passBothPriorities(); // resolve may -> prompt
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        return bears;
    }

    private Permanent findPilgrim() {
        return findPermanent(player1, "Nearheath Pilgrim");
    }

    @Test
    @DisplayName("Soulbond ETB pairs Nearheath Pilgrim with another unpaired creature")
    void soulbondPairsOnEnter() {
        Permanent bears = castAndPairWithBears();
        Permanent pilgrim = findPilgrim();

        assertThat(pilgrim.getPairedWithId()).isEqualTo(bears.getId());
        assertThat(bears.getPairedWithId()).isEqualTo(pilgrim.getId());
    }

    @Test
    @DisplayName("While paired, both creatures have lifelink")
    void pairedBothHaveLifelink() {
        Permanent bears = castAndPairWithBears();
        Permanent pilgrim = findPilgrim();

        assertThat(gqs.hasKeyword(gd, pilgrim, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Unpaired Nearheath Pilgrim does not have lifelink")
    void unpairedHasNoLifelink() {
        harness.addToBattlefield(player1, new NearheathPilgrim());
        Permanent pilgrim = findPilgrim();

        assertThat(pilgrim.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, pilgrim, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Declining soulbond leaves both unpaired and without lifelink")
    void decliningLeavesUnpairedWithoutLifelink() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new NearheathPilgrim()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent pilgrim = findPilgrim();
        assertThat(pilgrim.getPairedWithId()).isNull();
        assertThat(bears.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, pilgrim, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isFalse();
    }
}
