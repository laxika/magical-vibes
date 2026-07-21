package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.q.QasaliAmbusher;
import com.github.laxika.magicalvibes.cards.w.WoollyThoctar;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EsperStormbladeTest extends BaseCardTest {

    // ===== With another multicolored permanent =====

    @Test
    @DisplayName("Gets +1/+1 (becomes 3/2) and flying while controlling another multicolored permanent")
    void boostWithAnotherMulticolored() {
        harness.addToBattlefield(player1, new EsperStormblade());
        harness.addToBattlefield(player1, new QasaliAmbusher()); // {1}{G}{W}, GW multicolored

        Permanent stormblade = findPermanent(player1, "Esper Stormblade");
        assertThat(gqs.getEffectivePower(gd, stormblade)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, stormblade)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, stormblade, Keyword.FLYING)).isTrue();
    }

    // ===== Without another multicolored permanent =====

    @Test
    @DisplayName("Base 2/1 with no flying when alone (its own multicoloredness does not count)")
    void noBoostAlone() {
        harness.addToBattlefield(player1, new EsperStormblade());

        Permanent stormblade = findPermanent(player1, "Esper Stormblade");
        assertThat(gqs.getEffectivePower(gd, stormblade)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, stormblade)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, stormblade, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("No boost with only a monocolored other permanent")
    void noBoostWithMonocolored() {
        harness.addToBattlefield(player1, new EsperStormblade());
        harness.addToBattlefield(player1, new GrizzlyBears()); // {1}{G}, monocolored

        Permanent stormblade = findPermanent(player1, "Esper Stormblade");
        assertThat(gqs.getEffectivePower(gd, stormblade)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, stormblade)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, stormblade, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("An opponent's multicolored permanent does not grant the boost")
    void opponentMulticoloredDoesNotCount() {
        harness.addToBattlefield(player1, new EsperStormblade());
        harness.addToBattlefield(player2, new WoollyThoctar()); // {R}{G}{W}, multicolored, opponent

        Permanent stormblade = findPermanent(player1, "Esper Stormblade");
        assertThat(gqs.getEffectivePower(gd, stormblade)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, stormblade)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, stormblade, Keyword.FLYING)).isFalse();
    }

    // ===== Two Stormblades each satisfy the other's "another" =====

    @Test
    @DisplayName("Two Esper Stormblades each count as the other's multicolored permanent")
    void twoStormbladesBoostEachOther() {
        harness.addToBattlefield(player1, new EsperStormblade());
        harness.addToBattlefield(player1, new EsperStormblade());

        for (Permanent stormblade : gd.playerBattlefields.get(player1.getId())) {
            assertThat(gqs.getEffectivePower(gd, stormblade)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, stormblade)).isEqualTo(2);
            assertThat(gqs.hasKeyword(gd, stormblade, Keyword.FLYING)).isTrue();
        }
    }

    // ===== Boost is dynamic =====

    @Test
    @DisplayName("Loses +1/+1 and flying when the other multicolored permanent leaves")
    void losesBoostWhenMulticoloredLeaves() {
        harness.addToBattlefield(player1, new EsperStormblade());
        harness.addToBattlefield(player1, new WoollyThoctar());

        Permanent stormblade = findPermanent(player1, "Esper Stormblade");
        assertThat(gqs.getEffectivePower(gd, stormblade)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, stormblade, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Woolly Thoctar"));

        assertThat(gqs.getEffectivePower(gd, stormblade)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, stormblade)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, stormblade, Keyword.FLYING)).isFalse();
    }
}
