package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.q.QasaliAmbusher;
import com.github.laxika.magicalvibes.cards.w.WoollyThoctar;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JundHackbladeTest extends BaseCardTest {

    // ===== With another multicolored permanent =====

    @Test
    @DisplayName("Gets +1/+1 (becomes 3/2) and haste while controlling another multicolored permanent")
    void boostWithAnotherMulticolored() {
        harness.addToBattlefield(player1, new JundHackblade());
        harness.addToBattlefield(player1, new QasaliAmbusher()); // {1}{G}{W}, GW multicolored

        Permanent hackblade = findPermanent(player1, "Jund Hackblade");
        assertThat(gqs.getEffectivePower(gd, hackblade)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hackblade)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, hackblade, Keyword.HASTE)).isTrue();
    }

    // ===== Without another multicolored permanent =====

    @Test
    @DisplayName("Base 2/1 with no haste when alone (its own multicoloredness does not count)")
    void noBoostAlone() {
        harness.addToBattlefield(player1, new JundHackblade());

        Permanent hackblade = findPermanent(player1, "Jund Hackblade");
        assertThat(gqs.getEffectivePower(gd, hackblade)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hackblade)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, hackblade, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("No boost with only a monocolored other permanent")
    void noBoostWithMonocolored() {
        harness.addToBattlefield(player1, new JundHackblade());
        harness.addToBattlefield(player1, new GrizzlyBears()); // {1}{G}, monocolored

        Permanent hackblade = findPermanent(player1, "Jund Hackblade");
        assertThat(gqs.getEffectivePower(gd, hackblade)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hackblade)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, hackblade, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("An opponent's multicolored permanent does not grant the boost")
    void opponentMulticoloredDoesNotCount() {
        harness.addToBattlefield(player1, new JundHackblade());
        harness.addToBattlefield(player2, new WoollyThoctar()); // {R}{G}{W}, multicolored, opponent

        Permanent hackblade = findPermanent(player1, "Jund Hackblade");
        assertThat(gqs.getEffectivePower(gd, hackblade)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hackblade)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, hackblade, Keyword.HASTE)).isFalse();
    }

    // ===== Boost is dynamic =====

    @Test
    @DisplayName("Loses +1/+1 and haste when the other multicolored permanent leaves")
    void losesBoostWhenMulticoloredLeaves() {
        harness.addToBattlefield(player1, new JundHackblade());
        harness.addToBattlefield(player1, new WoollyThoctar());

        Permanent hackblade = findPermanent(player1, "Jund Hackblade");
        assertThat(gqs.getEffectivePower(gd, hackblade)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, hackblade, Keyword.HASTE)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Woolly Thoctar"));

        assertThat(gqs.getEffectivePower(gd, hackblade)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hackblade)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, hackblade, Keyword.HASTE)).isFalse();
    }
}
