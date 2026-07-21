package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.q.QasaliAmbusher;
import com.github.laxika.magicalvibes.cards.w.WoollyThoctar;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NayaHushbladeTest extends BaseCardTest {

    // ===== With another multicolored permanent =====

    @Test
    @DisplayName("Gets +1/+1 (becomes 3/2) and shroud while controlling another multicolored permanent")
    void boostWithAnotherMulticolored() {
        harness.addToBattlefield(player1, new NayaHushblade());
        harness.addToBattlefield(player1, new QasaliAmbusher()); // {1}{G}{W}, GW multicolored

        Permanent hushblade = findPermanent(player1, "Naya Hushblade");
        assertThat(gqs.getEffectivePower(gd, hushblade)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hushblade)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, hushblade, Keyword.SHROUD)).isTrue();
    }

    // ===== Without another multicolored permanent =====

    @Test
    @DisplayName("Base 2/1 with no shroud when alone (its own multicoloredness does not count)")
    void noBoostAlone() {
        harness.addToBattlefield(player1, new NayaHushblade());

        Permanent hushblade = findPermanent(player1, "Naya Hushblade");
        assertThat(gqs.getEffectivePower(gd, hushblade)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hushblade)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, hushblade, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("No boost with only a monocolored other permanent")
    void noBoostWithMonocolored() {
        harness.addToBattlefield(player1, new NayaHushblade());
        harness.addToBattlefield(player1, new GrizzlyBears()); // {1}{G}, monocolored

        Permanent hushblade = findPermanent(player1, "Naya Hushblade");
        assertThat(gqs.getEffectivePower(gd, hushblade)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hushblade)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, hushblade, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("An opponent's multicolored permanent does not grant the boost")
    void opponentMulticoloredDoesNotCount() {
        harness.addToBattlefield(player1, new NayaHushblade());
        harness.addToBattlefield(player2, new WoollyThoctar()); // {R}{G}{W}, multicolored, opponent

        Permanent hushblade = findPermanent(player1, "Naya Hushblade");
        assertThat(gqs.getEffectivePower(gd, hushblade)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hushblade)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, hushblade, Keyword.SHROUD)).isFalse();
    }

    // ===== Boost is dynamic =====

    @Test
    @DisplayName("Loses +1/+1 and shroud when the other multicolored permanent leaves")
    void losesBoostWhenMulticoloredLeaves() {
        harness.addToBattlefield(player1, new NayaHushblade());
        harness.addToBattlefield(player1, new WoollyThoctar());

        Permanent hushblade = findPermanent(player1, "Naya Hushblade");
        assertThat(gqs.getEffectivePower(gd, hushblade)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, hushblade, Keyword.SHROUD)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Woolly Thoctar"));

        assertThat(gqs.getEffectivePower(gd, hushblade)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hushblade)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, hushblade, Keyword.SHROUD)).isFalse();
    }
}
