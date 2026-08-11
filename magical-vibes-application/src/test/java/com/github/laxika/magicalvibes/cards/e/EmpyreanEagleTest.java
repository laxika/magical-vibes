package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmpyreanEagleTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts other flying creatures you control")
    void boostsOwnFlyers() {
        harness.addToBattlefield(player1, new EmpyreanEagle());
        Permanent hawk = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());

        assertThat(gqs.getEffectivePower(gd, hawk)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hawk)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost itself or nonflying creatures")
    void excludesSourceAndNonflyers() {
        Permanent eagle = harness.addToBattlefieldAndReturn(player1, new EmpyreanEagle());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, eagle)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, eagle)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost flying creatures controlled by an opponent")
    void excludesOpponentsFlyers() {
        harness.addToBattlefield(player1, new EmpyreanEagle());
        Permanent hawk = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());

        assertThat(gqs.getEffectivePower(gd, hawk)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, hawk)).isEqualTo(1);
    }
}
