package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
        addPermanent(player1, createCard("Test Enchantment", CardType.ENCHANTMENT));

        Permanent knight = findPermanent(player1, "Blood-Cursed Knight");
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, knight, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("An opponent's enchantment does not grant the boost")
    void opponentEnchantmentDoesNotCount() {
        harness.addToBattlefield(player1, new BloodCursedKnight());
        addPermanent(player2, createCard("Test Enchantment", CardType.ENCHANTMENT));

        Permanent knight = findPermanent(player1, "Blood-Cursed Knight");
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, knight, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Loses the boost and lifelink when the enchantment leaves the battlefield")
    void losesBoostWhenEnchantmentLeaves() {
        harness.addToBattlefield(player1, new BloodCursedKnight());
        Permanent enchantment = addPermanent(player1, createCard("Test Enchantment", CardType.ENCHANTMENT));

        Permanent knight = findPermanent(player1, "Blood-Cursed Knight");
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(enchantment);

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, knight, Keyword.LIFELINK)).isFalse();
    }

    private Permanent addPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Card createCard(String name, CardType type) {
        Card card = new Card() {};
        card.setName(name);
        card.setType(type);
        return card;
    }
}
