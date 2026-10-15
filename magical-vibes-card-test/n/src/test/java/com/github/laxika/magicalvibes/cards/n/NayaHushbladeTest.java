package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.FirewildBorderpost;
import com.github.laxika.magicalvibes.cards.t.Terminate;
import com.github.laxika.magicalvibes.cards.q.QasaliAmbusher;
import com.github.laxika.magicalvibes.cards.w.WoollyThoctar;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

@CardUsed({NayaHushblade.class, QasaliAmbusher.class, WoollyThoctar.class, GrizzlyBears.class,
        FirewildBorderpost.class, Terminate.class})
class NayaHushbladeTest extends BaseCardTest {

    // ===== With another multicolored permanent =====

    @Test
    @DisplayName("Gets +1/+1 (becomes 3/2) and shroud while controlling another multicolored permanent")
    void boostWithAnotherMulticolored() {
        Permanent hushblade = harness.addToBattlefieldAndReturn(player1, new NayaHushblade());
        harness.addToBattlefield(player1, new QasaliAmbusher()); // {1}{G}{W}, GW multicolored

        assertThat(gqs.getEffectivePower(gd, hushblade)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hushblade)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, hushblade, Keyword.SHROUD)).isTrue();
    }

    // ===== Without another multicolored permanent =====

    @Test
    @DisplayName("Base 2/1 with no shroud when alone (its own multicoloredness does not count)")
    void noBoostAlone() {
        Permanent hushblade = harness.addToBattlefieldAndReturn(player1, new NayaHushblade());

        assertThat(gqs.getEffectivePower(gd, hushblade)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hushblade)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, hushblade, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("No boost with only a monocolored other permanent")
    void noBoostWithMonocolored() {
        Permanent hushblade = harness.addToBattlefieldAndReturn(player1, new NayaHushblade());
        harness.addToBattlefield(player1, new GrizzlyBears()); // {1}{G}, monocolored

        assertThat(gqs.getEffectivePower(gd, hushblade)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hushblade)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, hushblade, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("An opponent's multicolored permanent does not grant the boost")
    void opponentMulticoloredDoesNotCount() {
        Permanent hushblade = harness.addToBattlefieldAndReturn(player1, new NayaHushblade());
        harness.addToBattlefield(player2, new WoollyThoctar()); // {R}{G}{W}, multicolored, opponent

        assertThat(gqs.getEffectivePower(gd, hushblade)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hushblade)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, hushblade, Keyword.SHROUD)).isFalse();
    }

    // ===== Boost is dynamic =====

    @Test
    @DisplayName("Loses +1/+1 and shroud when the other multicolored permanent leaves")
    void losesBoostWhenMulticoloredLeaves() {
        Permanent hushblade = harness.addToBattlefieldAndReturn(player1, new NayaHushblade());
        harness.addToBattlefield(player1, new WoollyThoctar());

        assertThat(gqs.getEffectivePower(gd, hushblade)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, hushblade, Keyword.SHROUD)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Woolly Thoctar"));

        assertThat(gqs.getEffectivePower(gd, hushblade)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hushblade)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, hushblade, Keyword.SHROUD)).isFalse();
    }

    @Test
    void anotherHushbladeEnablesBothWithoutStackingTheBonus() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new NayaHushblade());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new NayaHushblade());

        for (Permanent hushblade : List.of(first, second)) {
            assertThat(gqs.getEffectivePower(gd, hushblade)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, hushblade)).isEqualTo(2);
            assertThat(gqs.hasKeyword(gd, hushblade, Keyword.SHROUD)).isTrue();
        }

        harness.addToBattlefield(player1, new FirewildBorderpost());
        for (Permanent hushblade : List.of(first, second)) {
            assertThat(gqs.getEffectivePower(gd, hushblade)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, hushblade)).isEqualTo(2);
        }
    }

    @Test
    void gainsBonusWhenMulticoloredArtifactEnters() {
        Permanent hushblade = harness.addToBattlefieldAndReturn(player1, new NayaHushblade());
        assertThat(gqs.hasKeyword(gd, hushblade, Keyword.SHROUD)).isFalse();

        harness.addToBattlefield(player1, new FirewildBorderpost());

        assertThat(gqs.getEffectivePower(gd, hushblade)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hushblade)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, hushblade, Keyword.SHROUD)).isTrue();
    }

    @Test
    void shroudPreventsControllerFromTargetingIt() {
        Permanent hushblade = harness.addToBattlefieldAndReturn(player1, new NayaHushblade());
        harness.addToBattlefield(player1, new FirewildBorderpost());
        harness.addToBattlefield(player2, new NayaHushblade());
        harness.setHand(player1, List.of(new Terminate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, hushblade.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    void shroudPreventsOpponentFromTargetingIt() {
        Permanent hushblade = harness.addToBattlefieldAndReturn(player2, new NayaHushblade());
        harness.addToBattlefield(player2, new FirewildBorderpost());
        harness.addToBattlefield(player1, new NayaHushblade());
        harness.setHand(player1, List.of(new Terminate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, hushblade.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }
}
