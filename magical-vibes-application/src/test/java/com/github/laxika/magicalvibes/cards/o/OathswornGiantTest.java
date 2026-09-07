package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OathswornGiant.class, GrizzlyBears.class})
class OathswornGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Other creatures you control get +0/+2 and vigilance")
    void buffsOtherCreaturesYouControl() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");

        int basePower = gqs.getEffectivePower(gd, bears);
        int baseToughness = gqs.getEffectiveToughness(gd, bears);
        harness.addToBattlefield(player1, new OathswornGiant());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(baseToughness + 2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Oathsworn Giant does not buff itself")
    void doesNotBuffItself() {
        OathswornGiant giant = new OathswornGiant();
        giant.setPower(10);
        giant.setToughness(10);
        harness.addToBattlefield(player1, giant);

        Permanent permanent = findPermanent(player1, "Oathsworn Giant");

        assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(10);
    }

    @Test
    @DisplayName("Does not affect an opponent's creatures")
    void doesNotAffectOpponentsCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");

        int basePower = gqs.getEffectivePower(gd, bears);
        int baseToughness = gqs.getEffectiveToughness(gd, bears);
        harness.addToBattlefield(player1, new OathswornGiant());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(baseToughness);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
    }
}
