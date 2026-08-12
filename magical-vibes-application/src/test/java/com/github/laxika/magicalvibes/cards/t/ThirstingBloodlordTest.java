package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VampireOfTheDireMoon;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ThirstingBloodlordTest extends BaseCardTest {

    @Test
    @DisplayName("Other Vampires you control get +1/+1")
    void buffsOtherVampiresYouControl() {
        harness.addToBattlefield(player1, new ThirstingBloodlord());
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new VampireOfTheDireMoon());

        assertThat(gqs.getEffectivePower(gd, vampire)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, vampire)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff itself, non-Vampires, or opposing Vampires")
    void onlyBuffsOtherOwnVampires() {
        Permanent bloodlord = harness.addToBattlefieldAndReturn(player1, new ThirstingBloodlord());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentVampire = harness.addToBattlefieldAndReturn(player2, new VampireOfTheDireMoon());

        assertThat(gqs.getEffectivePower(gd, bloodlord)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bloodlord)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentVampire)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentVampire)).isEqualTo(1);
    }
}
