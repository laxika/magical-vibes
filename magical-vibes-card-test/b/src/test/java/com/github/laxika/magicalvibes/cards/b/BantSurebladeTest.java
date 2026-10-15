package com.github.laxika.magicalvibes.cards.b;

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

@CardUsed({BantSureblade.class, GrizzlyBears.class, QasaliAmbusher.class, WoollyThoctar.class, FieldmistBorderpost.class})
class BantSurebladeTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 (becomes 3/2) and first strike while controlling another multicolored permanent")
    void boostWithAnotherMulticolored() {
        Permanent sureblade = harness.addToBattlefieldAndReturn(player1, new BantSureblade());
        harness.addToBattlefield(player1, new QasaliAmbusher()); // {1}{G}{W}, GW multicolored

        assertThat(gqs.getEffectivePower(gd, sureblade)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sureblade)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, sureblade, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Base 2/1 with no first strike when alone (its own multicoloredness does not count)")
    void noBoostAlone() {
        Permanent sureblade = harness.addToBattlefieldAndReturn(player1, new BantSureblade());

        assertThat(gqs.getEffectivePower(gd, sureblade)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sureblade)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, sureblade, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("No boost with only a monocolored other permanent")
    void noBoostWithMonocolored() {
        Permanent sureblade = harness.addToBattlefieldAndReturn(player1, new BantSureblade());
        harness.addToBattlefield(player1, new GrizzlyBears()); // {1}{G}, monocolored

        assertThat(gqs.getEffectivePower(gd, sureblade)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sureblade)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, sureblade, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("An opponent's multicolored permanent does not grant the boost")
    void opponentMulticoloredDoesNotCount() {
        Permanent sureblade = harness.addToBattlefieldAndReturn(player1, new BantSureblade());
        harness.addToBattlefield(player2, new WoollyThoctar()); // {R}{G}{W}, multicolored, opponent

        assertThat(gqs.getEffectivePower(gd, sureblade)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sureblade)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, sureblade, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Loses +1/+1 and first strike when the other multicolored permanent leaves")
    void losesBoostWhenMulticoloredLeaves() {
        Permanent sureblade = harness.addToBattlefieldAndReturn(player1, new BantSureblade());
        harness.addToBattlefield(player1, new WoollyThoctar());

        assertThat(gqs.getEffectivePower(gd, sureblade)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, sureblade, Keyword.FIRST_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Woolly Thoctar"));

        assertThat(gqs.getEffectivePower(gd, sureblade)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sureblade)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, sureblade, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("A multicolored artifact grants the bonus immediately")
    void gainsBoostWhenMulticoloredArtifactArrives() {
        Permanent sureblade = harness.addToBattlefieldAndReturn(player1, new BantSureblade());
        assertThat(gqs.getEffectivePower(gd, sureblade)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, sureblade, Keyword.FIRST_STRIKE)).isFalse();

        harness.addToBattlefield(player1, new FieldmistBorderpost());

        assertThat(gqs.getEffectivePower(gd, sureblade)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sureblade)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, sureblade, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Two Sureblades enable each other, and additional qualifying permanents do not stack the bonus")
    void multipleQualifyingPermanentsGrantOnlyOneBonus() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BantSureblade());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BantSureblade());
        Permanent borderpost = harness.addToBattlefieldAndReturn(player1, new FieldmistBorderpost());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, first, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, second, Keyword.FIRST_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(borderpost);

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, first, Keyword.FIRST_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(second);

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, first, Keyword.FIRST_STRIKE)).isFalse();
    }
}
