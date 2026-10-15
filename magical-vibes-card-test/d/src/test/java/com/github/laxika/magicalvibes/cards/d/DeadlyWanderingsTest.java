package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeadlyWanderings.class, GrizzlyBears.class, Opalescence.class})
class DeadlyWanderingsTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent creatures do not affect the condition or receive the bonus")
    void onlyCountsAndBoostsControllersCreatures() {
        harness.addToBattlefield(player1, new DeadlyWanderings());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, own)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, own, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, own, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opposing)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opposing, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposing, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Bonus disappears immediately when a second creature enters")
    void losesBonusWhenSecondCreatureEnters() {
        harness.addToBattlefield(player1, new DeadlyWanderings());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, first, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, first, Keyword.LIFELINK)).isTrue();

        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        for (Permanent creature : new Permanent[]{first, second}) {
            assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
            assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isFalse();
            assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isFalse();
        }
    }

    @Test
    @DisplayName("Animated Deadly Wanderings boosts itself when it is the sole creature")
    void boostsItselfWhenAnimatedAsSoleCreature() {
        Permanent wanderings = harness.addToBattlefieldAndReturn(player1, new DeadlyWanderings());
        harness.addToBattlefield(player2, new Opalescence());

        assertThat(gqs.isCreature(gd, wanderings)).isTrue();
        assertThat(gqs.getEffectivePower(gd, wanderings)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, wanderings)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, wanderings, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, wanderings, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Sole creature gets +2/+0, deathtouch, and lifelink")
    void boostsSoleCreature() {
        harness.addToBattlefield(player1, new DeadlyWanderings());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Does not boost when you control two creatures")
    void noBoostWithTwoCreatures() {
        harness.addToBattlefield(player1, new DeadlyWanderings());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Boost returns when the second creature leaves the battlefield")
    void boostReturnsAfterSecondCreatureLeaves() {
        harness.addToBattlefield(player1, new DeadlyWanderings());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(other);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();
    }
}
