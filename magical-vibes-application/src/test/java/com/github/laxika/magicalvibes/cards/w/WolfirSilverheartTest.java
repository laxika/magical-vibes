package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WolfirSilverheartTest extends BaseCardTest {

    private Permanent castAndPairWithBears() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new WolfirSilverheart()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve spell -> soulbond may on stack
        harness.passBothPriorities(); // resolve may -> prompt
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        return bears;
    }

    private Permanent findWolfir() {
        return findPermanent(player1, "Wolfir Silverheart");
    }

    @Test
    @DisplayName("While paired, both creatures get +4/+4")
    void pairedBothGetBoost() {
        Permanent bears = castAndPairWithBears();
        Permanent wolfir = findWolfir();

        assertThat(wolfir.getPairedWithId()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, wolfir)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, wolfir)).isEqualTo(8);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(6);
    }

    @Test
    @DisplayName("Unpaired Wolfir Silverheart is a plain 4/4")
    void unpairedGetsNoBoost() {
        harness.addToBattlefield(player1, new WolfirSilverheart());
        Permanent wolfir = findWolfir();

        assertThat(wolfir.getPairedWithId()).isNull();
        assertThat(gqs.getEffectivePower(gd, wolfir)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, wolfir)).isEqualTo(4);
    }

    @Test
    @DisplayName("Declining soulbond leaves both creatures unboosted")
    void decliningLeavesBothUnboosted() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new WolfirSilverheart()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent wolfir = findWolfir();
        assertThat(wolfir.getPairedWithId()).isNull();
        assertThat(bears.getPairedWithId()).isNull();
        assertThat(gqs.getEffectivePower(gd, wolfir)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }
}
