package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SacredCat;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KingOfThePride.class, SacredCat.class, GrizzlyBears.class})
class KingOfThePrideTest extends BaseCardTest {

    @Test
    @DisplayName("Other Cats you control get +2/+1")
    void boostsOtherCatsYouControl() {
        harness.addToBattlefield(player1, new KingOfThePride());
        Permanent cat = harness.addToBattlefieldAndReturn(player1, new SacredCat());

        assertThat(gqs.getEffectivePower(gd, cat)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, cat)).isEqualTo(2);
    }

    @Test
    @DisplayName("King of the Pride does not boost itself")
    void doesNotBoostItself() {
        Permanent king = harness.addToBattlefieldAndReturn(player1, new KingOfThePride());

        assertThat(gqs.getEffectivePower(gd, king)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, king)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not boost non-Cat creatures you control")
    void doesNotBoostNonCats() {
        harness.addToBattlefield(player1, new KingOfThePride());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost an opponent's Cats")
    void doesNotBoostOpponentCats() {
        harness.addToBattlefield(player1, new KingOfThePride());
        Permanent opponentCat = harness.addToBattlefieldAndReturn(player2, new SacredCat());

        assertThat(gqs.getEffectivePower(gd, opponentCat)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentCat)).isEqualTo(1);
    }

    @Test
    @DisplayName("Two Kings of the Pride stack their bonuses")
    void bonusesStack() {
        harness.addToBattlefield(player1, new KingOfThePride());
        harness.addToBattlefield(player1, new KingOfThePride());
        Permanent cat = harness.addToBattlefieldAndReturn(player1, new SacredCat());

        assertThat(gqs.getEffectivePower(gd, cat)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, cat)).isEqualTo(3);
    }

    @Test
    @DisplayName("Bonus is removed when King of the Pride leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        Permanent king = harness.addToBattlefieldAndReturn(player1, new KingOfThePride());
        Permanent cat = harness.addToBattlefieldAndReturn(player1, new SacredCat());

        gd.playerBattlefields.get(player1.getId()).remove(king);

        assertThat(gqs.getEffectivePower(gd, cat)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, cat)).isEqualTo(1);
    }
}
