package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.SigilOfTheEmptyThrone;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodCursedKnight.class, SigilOfTheEmptyThrone.class})
class BloodCursedKnightTest extends BaseCardTest {

    @Test
    @DisplayName("Base 3/2 without lifelink when controlling no enchantment")
    void noBoostWithoutEnchantment() {
        harness.addToBattlefield(player1, new BloodCursedKnight());

        Permanent knight = findPermanent(player1, "Blood-Cursed Knight");
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, knight, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Gets +1/+1 and lifelink while controlling an enchantment")
    void boostWithEnchantment() {
        harness.addToBattlefield(player1, new BloodCursedKnight());
        harness.addToBattlefield(player1, new SigilOfTheEmptyThrone());

        Permanent knight = findPermanent(player1, "Blood-Cursed Knight");
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, knight, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("An opponent's enchantment does not grant the boost")
    void opponentEnchantmentDoesNotCount() {
        harness.addToBattlefield(player1, new BloodCursedKnight());
        harness.addToBattlefield(player2, new SigilOfTheEmptyThrone());

        Permanent knight = findPermanent(player1, "Blood-Cursed Knight");
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, knight, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Loses the boost and lifelink when the enchantment leaves the battlefield")
    void losesBoostWhenEnchantmentLeaves() {
        harness.addToBattlefield(player1, new BloodCursedKnight());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new SigilOfTheEmptyThrone());

        Permanent knight = findPermanent(player1, "Blood-Cursed Knight");
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(enchantment);

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, knight, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Multiple enchantments grant only one bonus, retained until the last leaves")
    void multipleEnchantmentsDoNotStack() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new BloodCursedKnight());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SigilOfTheEmptyThrone());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SigilOfTheEmptyThrone());

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, knight, Keyword.LIFELINK)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(first);

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, knight, Keyword.LIFELINK)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(second);

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, knight, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Combat damage gains life while an enchantment is controlled")
    void combatDamageWithEnchantmentGainsLife() {
        addCreatureReady(player1, new BloodCursedKnight());
        harness.addToBattlefield(player1, new SigilOfTheEmptyThrone());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Combat damage without an enchantment does not gain life")
    void combatDamageWithoutEnchantmentDoesNotGainLife() {
        addCreatureReady(player1, new BloodCursedKnight());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }
}
