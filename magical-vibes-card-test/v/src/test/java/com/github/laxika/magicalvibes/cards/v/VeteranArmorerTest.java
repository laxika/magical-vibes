package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.s.SellSwordBrute;
import com.github.laxika.magicalvibes.cards.l.LastGasp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VeteranArmorer.class, SellSwordBrute.class, LastGasp.class})
class VeteranArmorerTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures entering after Armorer receive the boost, which ends when Armorer dies")
    void boostEndsWhenArmorerDies() {
        Permanent armorer = harness.addToBattlefieldAndReturn(player1, new VeteranArmorer());
        Permanent brute = harness.enterBattlefieldAndReturn(player1, new SellSwordBrute());
        assertThat(gqs.getEffectiveToughness(gd, brute)).isEqualTo(3);

        harness.setHand(player1, List.of(new LastGasp()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, armorer.getId());

        harness.assertInGraveyard(player1, "Veteran Armorer");
        harness.assertNotOnBattlefield(player1, "Veteran Armorer");
        assertThat(gqs.getEffectivePower(gd, brute)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, brute)).isEqualTo(2);
    }

    @Test
    @DisplayName("Other creatures you control get +0/+1")
    void buffsOtherOwnCreatures() {
        harness.addToBattlefield(player1, new VeteranArmorer());
        Permanent brute = harness.addToBattlefieldAndReturn(player1, new SellSwordBrute());

        assertThat(gqs.getEffectivePower(gd, brute)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, brute)).isEqualTo(3);
    }

    @Test
    @DisplayName("Multiple Armorers boost one another and other creatures")
    void multipleArmorersBoostEachOther() {
        Permanent firstArmorer = harness.addToBattlefieldAndReturn(player1, new VeteranArmorer());
        Permanent secondArmorer = harness.addToBattlefieldAndReturn(player1, new VeteranArmorer());
        Permanent brute = harness.addToBattlefieldAndReturn(player1, new SellSwordBrute());

        assertThat(gqs.getEffectivePower(gd, firstArmorer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, firstArmorer)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, secondArmorer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, secondArmorer)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, brute)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, brute)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not buff itself or opponent's creatures")
    void doesNotBuffItselfOrOpponentsCreatures() {
        Permanent armorer = harness.addToBattlefieldAndReturn(player1, new VeteranArmorer());
        Permanent opponentBrute = harness.addToBattlefieldAndReturn(player2, new SellSwordBrute());

        assertThat(gqs.getEffectivePower(gd, armorer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, armorer)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentBrute)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBrute)).isEqualTo(2);
    }
}
