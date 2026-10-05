package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KnightOfTheMists;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InspiringVeteran.class, KnightOfTheMists.class, GrizzlyBears.class})
class InspiringVeteranTest extends BaseCardTest {

    @Test
    @DisplayName("Multiple Veterans boost each other and their bonuses stack")
    void multipleVeteransBoostEachOther() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new InspiringVeteran());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new InspiringVeteran());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new InspiringVeteran());

        for (Permanent veteran : new Permanent[]{first, second, third}) {
            assertThat(gqs.getEffectivePower(gd, veteran)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, veteran)).isEqualTo(4);
        }
    }

    @Test
    @DisplayName("A Knight entering later immediately receives the bonus")
    void boostsKnightsEnteringLater() {
        Permanent first = harness.enterBattlefieldAndReturn(player1, new InspiringVeteran());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);

        Permanent second = harness.enterBattlefieldAndReturn(player1, new InspiringVeteran());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
    }

    @Test
    @DisplayName("Other Knights you control get +1/+1")
    void boostsOtherKnightsYouControl() {
        harness.addToBattlefield(player1, new InspiringVeteran());
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new KnightOfTheMists());

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not boost itself, opposing Knights, or non-Knights")
    void excludesSourceOpposingKnightsAndNonKnights() {
        Permanent veteran = harness.addToBattlefieldAndReturn(player1, new InspiringVeteran());
        Permanent opposingKnight = harness.addToBattlefieldAndReturn(player2, new KnightOfTheMists());
        Permanent nonKnight = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, veteran)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, veteran)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingKnight)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingKnight)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, nonKnight)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nonKnight)).isEqualTo(2);
    }

    @Test
    @DisplayName("The bonus disappears when Inspiring Veteran leaves the battlefield")
    void bonusDisappearsWhenSourceLeaves() {
        Permanent veteran = harness.addToBattlefieldAndReturn(player1, new InspiringVeteran());
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new KnightOfTheMists());

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(veteran);

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(2);
    }
}
