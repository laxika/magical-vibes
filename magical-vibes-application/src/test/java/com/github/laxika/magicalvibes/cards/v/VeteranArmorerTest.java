package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VeteranArmorer.class, GrizzlyBears.class})
class VeteranArmorerTest extends BaseCardTest {

    @Test
    @DisplayName("Other creatures you control get +0/+1")
    void buffsOtherOwnCreatures() {
        harness.addToBattlefield(player1, new VeteranArmorer());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Multiple Armorers boost one another and other creatures")
    void multipleArmorersBoostEachOther() {
        Permanent firstArmorer = harness.addToBattlefieldAndReturn(player1, new VeteranArmorer());
        Permanent secondArmorer = harness.addToBattlefieldAndReturn(player1, new VeteranArmorer());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, firstArmorer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, firstArmorer)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, secondArmorer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, secondArmorer)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not buff itself or opponent's creatures")
    void doesNotBuffItselfOrOpponentsCreatures() {
        Permanent armorer = harness.addToBattlefieldAndReturn(player1, new VeteranArmorer());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, armorer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, armorer)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
    }
}
