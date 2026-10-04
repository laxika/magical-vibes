package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.FieldmistBorderpost;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.q.QasaliAmbusher;
import com.github.laxika.magicalvibes.cards.w.WoollyThoctar;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EsperStormblade.class, QasaliAmbusher.class, WoollyThoctar.class, GrizzlyBears.class, FieldmistBorderpost.class})
class EsperStormbladeTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 (becomes 3/2) and flying while controlling another multicolored permanent")
    void boostWithAnotherMulticolored() {
        Permanent stormblade = harness.addToBattlefieldAndReturn(player1, new EsperStormblade());
        harness.addToBattlefield(player1, new QasaliAmbusher()); // {1}{G}{W}, GW multicolored

        assertThat(gqs.getEffectivePower(gd, stormblade)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, stormblade)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, stormblade, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Base 2/1 with no flying when alone (its own multicoloredness does not count)")
    void noBoostAlone() {
        Permanent stormblade = harness.addToBattlefieldAndReturn(player1, new EsperStormblade());

        assertThat(gqs.getEffectivePower(gd, stormblade)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, stormblade)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, stormblade, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("No boost with only a monocolored other permanent")
    void noBoostWithMonocolored() {
        Permanent stormblade = harness.addToBattlefieldAndReturn(player1, new EsperStormblade());
        harness.addToBattlefield(player1, new GrizzlyBears()); // {1}{G}, monocolored

        assertThat(gqs.getEffectivePower(gd, stormblade)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, stormblade)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, stormblade, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("An opponent's multicolored permanent does not grant the boost")
    void opponentMulticoloredDoesNotCount() {
        Permanent stormblade = harness.addToBattlefieldAndReturn(player1, new EsperStormblade());
        harness.addToBattlefield(player2, new WoollyThoctar()); // {R}{G}{W}, multicolored, opponent

        assertThat(gqs.getEffectivePower(gd, stormblade)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, stormblade)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, stormblade, Keyword.FLYING)).isFalse();
    }

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

    @Test
    @DisplayName("Loses +1/+1 and flying when the other multicolored permanent leaves")
    void losesBoostWhenMulticoloredLeaves() {
        Permanent stormblade = harness.addToBattlefieldAndReturn(player1, new EsperStormblade());
        harness.addToBattlefield(player1, new WoollyThoctar());

        assertThat(gqs.getEffectivePower(gd, stormblade)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, stormblade, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Woolly Thoctar"));

        assertThat(gqs.getEffectivePower(gd, stormblade)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, stormblade)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, stormblade, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("A multicolored noncreature permanent enables the boost immediately")
    void gainsBoostWhenMulticoloredArtifactEnters() {
        Permanent stormblade = harness.addToBattlefieldAndReturn(player1, new EsperStormblade());
        assertThat(gqs.getEffectivePower(gd, stormblade)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, stormblade, Keyword.FLYING)).isFalse();

        harness.addToBattlefield(player1, new FieldmistBorderpost());

        assertThat(gqs.getEffectivePower(gd, stormblade)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, stormblade)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, stormblade, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Multiple qualifying permanents grant only one boost, which remains when one leaves")
    void multipleQualifyingPermanentsDoNotStack() {
        Permanent stormblade = harness.addToBattlefieldAndReturn(player1, new EsperStormblade());
        Permanent borderpost = harness.addToBattlefieldAndReturn(player1, new FieldmistBorderpost());
        harness.addToBattlefield(player1, new EsperStormblade());

        assertThat(gqs.getEffectivePower(gd, stormblade)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, stormblade)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, stormblade, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(borderpost);

        assertThat(gqs.getEffectivePower(gd, stormblade)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, stormblade)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, stormblade, Keyword.FLYING)).isTrue();
    }
}
