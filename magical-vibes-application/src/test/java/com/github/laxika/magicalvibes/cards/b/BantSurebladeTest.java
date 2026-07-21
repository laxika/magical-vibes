package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.q.QasaliAmbusher;
import com.github.laxika.magicalvibes.cards.w.WoollyThoctar;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BantSurebladeTest extends BaseCardTest {

    // ===== With another multicolored permanent =====

    @Test
    @DisplayName("Gets +1/+1 (becomes 3/2) and first strike while controlling another multicolored permanent")
    void boostWithAnotherMulticolored() {
        harness.addToBattlefield(player1, new BantSureblade());
        harness.addToBattlefield(player1, new QasaliAmbusher()); // {1}{G}{W}, GW multicolored

        Permanent sureblade = findPermanent(player1, "Bant Sureblade");
        assertThat(gqs.getEffectivePower(gd, sureblade)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sureblade)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, sureblade, Keyword.FIRST_STRIKE)).isTrue();
    }

    // ===== Without another multicolored permanent =====

    @Test
    @DisplayName("Base 2/1 with no first strike when alone (its own multicoloredness does not count)")
    void noBoostAlone() {
        harness.addToBattlefield(player1, new BantSureblade());

        Permanent sureblade = findPermanent(player1, "Bant Sureblade");
        assertThat(gqs.getEffectivePower(gd, sureblade)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sureblade)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, sureblade, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("No boost with only a monocolored other permanent")
    void noBoostWithMonocolored() {
        harness.addToBattlefield(player1, new BantSureblade());
        harness.addToBattlefield(player1, new GrizzlyBears()); // {1}{G}, monocolored

        Permanent sureblade = findPermanent(player1, "Bant Sureblade");
        assertThat(gqs.getEffectivePower(gd, sureblade)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sureblade)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, sureblade, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("An opponent's multicolored permanent does not grant the boost")
    void opponentMulticoloredDoesNotCount() {
        harness.addToBattlefield(player1, new BantSureblade());
        harness.addToBattlefield(player2, new WoollyThoctar()); // {R}{G}{W}, multicolored, opponent

        Permanent sureblade = findPermanent(player1, "Bant Sureblade");
        assertThat(gqs.getEffectivePower(gd, sureblade)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sureblade)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, sureblade, Keyword.FIRST_STRIKE)).isFalse();
    }

    // ===== Boost is dynamic =====

    @Test
    @DisplayName("Loses +1/+1 and first strike when the other multicolored permanent leaves")
    void losesBoostWhenMulticoloredLeaves() {
        harness.addToBattlefield(player1, new BantSureblade());
        harness.addToBattlefield(player1, new WoollyThoctar());

        Permanent sureblade = findPermanent(player1, "Bant Sureblade");
        assertThat(gqs.getEffectivePower(gd, sureblade)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, sureblade, Keyword.FIRST_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Woolly Thoctar"));

        assertThat(gqs.getEffectivePower(gd, sureblade)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sureblade)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, sureblade, Keyword.FIRST_STRIKE)).isFalse();
    }
}
