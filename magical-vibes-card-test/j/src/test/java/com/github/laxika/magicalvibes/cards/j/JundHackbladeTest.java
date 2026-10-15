package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.FirewildBorderpost;
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

@CardUsed({JundHackblade.class, QasaliAmbusher.class, WoollyThoctar.class,
        GrizzlyBears.class, FirewildBorderpost.class})
class JundHackbladeTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 (becomes 3/2) and haste while controlling another multicolored permanent")
    void boostWithAnotherMulticolored() {
        Permanent hackblade = harness.addToBattlefieldAndReturn(player1, new JundHackblade());
        harness.addToBattlefield(player1, new QasaliAmbusher()); // {1}{G}{W}, GW multicolored

        assertThat(gqs.getEffectivePower(gd, hackblade)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hackblade)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, hackblade, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Base 2/1 with no haste when alone (its own multicoloredness does not count)")
    void noBoostAlone() {
        Permanent hackblade = harness.addToBattlefieldAndReturn(player1, new JundHackblade());

        assertThat(gqs.getEffectivePower(gd, hackblade)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hackblade)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, hackblade, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("No boost with only a monocolored other permanent")
    void noBoostWithMonocolored() {
        Permanent hackblade = harness.addToBattlefieldAndReturn(player1, new JundHackblade());
        harness.addToBattlefield(player1, new GrizzlyBears()); // {1}{G}, monocolored

        assertThat(gqs.getEffectivePower(gd, hackblade)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hackblade)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, hackblade, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("An opponent's multicolored permanent does not grant the boost")
    void opponentMulticoloredDoesNotCount() {
        Permanent hackblade = harness.addToBattlefieldAndReturn(player1, new JundHackblade());
        harness.addToBattlefield(player2, new WoollyThoctar()); // {R}{G}{W}, multicolored, opponent

        assertThat(gqs.getEffectivePower(gd, hackblade)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hackblade)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, hackblade, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Loses +1/+1 and haste when the other multicolored permanent leaves")
    void losesBoostWhenMulticoloredLeaves() {
        Permanent hackblade = harness.addToBattlefieldAndReturn(player1, new JundHackblade());
        harness.addToBattlefield(player1, new WoollyThoctar());

        assertThat(gqs.getEffectivePower(gd, hackblade)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, hackblade, Keyword.HASTE)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Woolly Thoctar"));

        assertThat(gqs.getEffectivePower(gd, hackblade)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hackblade)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, hackblade, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Hackblades enable each other without stacking their bonuses")
    void hackbladesEnableEachOther() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new JundHackblade());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new JundHackblade());
        harness.addToBattlefield(player1, new JundHackblade());

        for (Permanent hackblade : findPermanents(player1, "Jund Hackblade")) {
            assertThat(gqs.getEffectivePower(gd, hackblade)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, hackblade)).isEqualTo(2);
            assertThat(gqs.hasKeyword(gd, hackblade, Keyword.HASTE)).isTrue();
        }
        gd.playerBattlefields.get(player1.getId()).remove(second);
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
    }

    @Test
    @DisplayName("A multicolored artifact entering enables the bonus and immediate attack eligibility")
    void multicoloredArtifactEnablesHasteDynamically() {
        Permanent hackblade = harness.addToBattlefieldAndReturn(player1, new JundHackblade());
        hackblade.setSummoningSick(true);

        assertThat(als.canAttack(gd, hackblade, player1.getId())).isFalse();
        Permanent borderpost = harness.enterBattlefieldAndReturn(player1, new FirewildBorderpost());

        assertThat(gqs.getEffectivePower(gd, hackblade)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hackblade)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, hackblade, Keyword.HASTE)).isTrue();
        assertThat(als.canAttack(gd, hackblade, player1.getId())).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(borderpost);

        assertThat(gqs.getEffectivePower(gd, hackblade)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hackblade)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, hackblade, Keyword.HASTE)).isFalse();
        assertThat(als.canAttack(gd, hackblade, player1.getId())).isFalse();
    }
}
