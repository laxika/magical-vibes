package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.m.MoorlandInquisitor;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HomicidalSeclusion.class, MoorlandInquisitor.class, Opalescence.class})
class HomicidalSeclusionTest extends BaseCardTest {

    @Test
    @DisplayName("Sole creature gets +3/+1 and lifelink")
    void boostsSoleCreature() {
        harness.addToBattlefield(player1, new HomicidalSeclusion());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("No boost with two creatures")
    void noBoostWithTwoCreatures() {
        harness.addToBattlefield(player1, new HomicidalSeclusion());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());
        harness.addToBattlefield(player1, new MoorlandInquisitor());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Opponent's sole creature is unaffected")
    void doesNotBoostOpponentCreature() {
        harness.addToBattlefield(player1, new HomicidalSeclusion());
        harness.addToBattlefield(player1, new MoorlandInquisitor());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new MoorlandInquisitor());

        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Boost returns when the second creature leaves the battlefield")
    void boostReturnsAfterSecondCreatureLeaves() {
        harness.addToBattlefield(player1, new HomicidalSeclusion());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(other);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();
    }
    @Test
    @DisplayName("A second creature immediately removes both bonuses and lifelink")
    void losesBonusWhenSecondCreatureEnters() {
        harness.addToBattlefield(player1, new HomicidalSeclusion());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isTrue();

        harness.addToBattlefield(player1, new MoorlandInquisitor());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("The creature loses the bonuses when Homicidal Seclusion leaves")
    void losesBonusWhenEnchantmentLeaves() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new HomicidalSeclusion());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);

        gd.playerBattlefields.get(player1.getId()).remove(enchantment);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Two copies stack their stat bonuses but lifelink gains life only once")
    void multipleCopiesStackWithoutDuplicatingLifeGain() {
        harness.addToBattlefield(player1, new HomicidalSeclusion());
        harness.addToBattlefield(player1, new HomicidalSeclusion());
        Permanent creature = addCreatureReady(player1, new MoorlandInquisitor());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        creature.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.resolveCombatDamage();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 12);
    }

    @Test
    @DisplayName("An animated Homicidal Seclusion boosts itself when it is the sole creature")
    void animatedSeclusionBoostsItself() {
        harness.addToBattlefield(player1, new Opalescence());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new HomicidalSeclusion());

        assertThat(gqs.isCreature(gd, enchantment)).isTrue();
        assertThat(gqs.getEffectivePower(gd, enchantment)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, enchantment)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, enchantment, Keyword.LIFELINK)).isTrue();
    }
}
