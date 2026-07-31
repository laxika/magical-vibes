package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WoodbornBehemothTest extends BaseCardTest {

    @Test
    @DisplayName("Base 4/4 without trample with no lands")
    void noBoostWithoutLands() {
        harness.addToBattlefield(player1, new WoodbornBehemoth());

        Permanent behemoth = findBehemoth();
        assertThat(gqs.getEffectivePower(gd, behemoth)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, behemoth)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, behemoth, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Still 4/4 without trample at 7 lands")
    void noBoostAt7Lands() {
        addLands(player1, 7);
        harness.addToBattlefield(player1, new WoodbornBehemoth());

        Permanent behemoth = findBehemoth();
        assertThat(gqs.getEffectivePower(gd, behemoth)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, behemoth)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, behemoth, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Gets +4/+4 and trample at exactly 8 lands")
    void boostAtExactly8Lands() {
        addLands(player1, 8);
        harness.addToBattlefield(player1, new WoodbornBehemoth());

        Permanent behemoth = findBehemoth();
        assertThat(gqs.getEffectivePower(gd, behemoth)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, behemoth)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, behemoth, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Gets +4/+4 and trample above 8 lands")
    void boostAbove8Lands() {
        addLands(player1, 10);
        harness.addToBattlefield(player1, new WoodbornBehemoth());

        Permanent behemoth = findBehemoth();
        assertThat(gqs.getEffectivePower(gd, behemoth)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, behemoth)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, behemoth, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Loses boost and trample when lands drop below 8")
    void losesBoostWhenLandsDrop() {
        addLands(player1, 8);
        harness.addToBattlefield(player1, new WoodbornBehemoth());

        Permanent behemoth = findBehemoth();
        assertThat(gqs.getEffectivePower(gd, behemoth)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, behemoth, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Forest"));

        assertThat(gqs.getEffectivePower(gd, behemoth)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, behemoth)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, behemoth, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Opponent's lands do not count")
    void opponentLandsDoNotCount() {
        addLands(player2, 8);
        harness.addToBattlefield(player1, new WoodbornBehemoth());

        Permanent behemoth = findBehemoth();
        assertThat(gqs.getEffectivePower(gd, behemoth)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, behemoth)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, behemoth, Keyword.TRAMPLE)).isFalse();
    }

    private void addLands(Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new Forest());
        }
    }

    private Permanent findBehemoth() {
        return findPermanent(player1, "Woodborn Behemoth");
    }
}
