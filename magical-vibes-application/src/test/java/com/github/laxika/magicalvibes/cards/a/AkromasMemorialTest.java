package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AkromasMemorialTest extends BaseCardTest {

    private static final List<Keyword> GRANTED = List.of(
            Keyword.FLYING, Keyword.FIRST_STRIKE, Keyword.VIGILANCE, Keyword.TRAMPLE, Keyword.HASTE);

    @Test
    @DisplayName("Creatures you control gain all five keywords")
    void grantsKeywordsToOwnCreatures() {
        harness.addToBattlefield(player1, new AkromasMemorial());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        for (Keyword keyword : GRANTED) {
            assertThat(gqs.hasKeyword(gd, bears, keyword)).as(keyword.name()).isTrue();
        }
    }

    @Test
    @DisplayName("Creatures you control have protection from black and from red")
    void grantsProtectionFromBlackAndRed() {
        harness.addToBattlefield(player1, new AkromasMemorial());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.hasProtectionFrom(gd, bears, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, bears, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, bears, CardColor.GREEN)).isFalse();
    }

    @Test
    @DisplayName("Opponent's creatures get nothing")
    void doesNotAffectOpponentCreatures() {
        harness.addToBattlefield(player1, new AkromasMemorial());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, bears, CardColor.BLACK)).isFalse();
    }

    @Test
    @DisplayName("Grants wear off when the Memorial leaves the battlefield")
    void grantsEndWhenMemorialLeaves() {
        harness.addToBattlefield(player1, new AkromasMemorial());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Akroma's Memorial"));

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, bears, CardColor.BLACK)).isFalse();
    }

    @Test
    @DisplayName("Does not change power or toughness")
    void doesNotBoostPowerToughness() {
        harness.addToBattlefield(player1, new AkromasMemorial());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }
}
