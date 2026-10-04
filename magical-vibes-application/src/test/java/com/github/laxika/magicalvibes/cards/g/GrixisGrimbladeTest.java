package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.MistveinBorderpost;
import com.github.laxika.magicalvibes.cards.q.QasaliAmbusher;
import com.github.laxika.magicalvibes.cards.w.WoollyThoctar;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrixisGrimblade.class, QasaliAmbusher.class, WoollyThoctar.class, MistveinBorderpost.class, Island.class})
class GrixisGrimbladeTest extends BaseCardTest {

    // ===== With another multicolored permanent =====

    @Test
    @DisplayName("Gets +1/+1 (becomes 3/2) and deathtouch while controlling another multicolored permanent")
    void boostWithAnotherMulticolored() {
        Permanent grimblade = harness.addToBattlefieldAndReturn(player1, new GrixisGrimblade());
        harness.addToBattlefield(player1, new QasaliAmbusher()); // {1}{G}{W}, GW multicolored

        assertThat(gqs.getEffectivePower(gd, grimblade)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, grimblade)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, grimblade, Keyword.DEATHTOUCH)).isTrue();
    }

    // ===== Without another multicolored permanent =====

    @Test
    @DisplayName("Base 2/1 with no deathtouch when alone (its own multicoloredness does not count)")
    void noBoostAlone() {
        Permanent grimblade = harness.addToBattlefieldAndReturn(player1, new GrixisGrimblade());
        assertThat(gqs.getEffectivePower(gd, grimblade)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, grimblade)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, grimblade, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("An opponent's multicolored permanent does not grant the boost")
    void opponentMulticoloredDoesNotCount() {
        Permanent grimblade = harness.addToBattlefieldAndReturn(player1, new GrixisGrimblade());
        harness.addToBattlefield(player2, new WoollyThoctar()); // {R}{G}{W}, multicolored, opponent

        assertThat(gqs.getEffectivePower(gd, grimblade)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, grimblade)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, grimblade, Keyword.DEATHTOUCH)).isFalse();
    }

    // ===== Boost is dynamic =====

    @Test
    @DisplayName("Loses +1/+1 and deathtouch when the other multicolored permanent leaves")
    void losesBoostWhenMulticoloredLeaves() {
        Permanent grimblade = harness.addToBattlefieldAndReturn(player1, new GrixisGrimblade());
        harness.addToBattlefield(player1, new WoollyThoctar());

        assertThat(gqs.getEffectivePower(gd, grimblade)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, grimblade, Keyword.DEATHTOUCH)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Woolly Thoctar"));

        assertThat(gqs.getEffectivePower(gd, grimblade)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, grimblade)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, grimblade, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("A multicolored noncreature permanent enables the bonus immediately")
    void gainsBonusWhenMulticoloredArtifactEnters() {
        Permanent grimblade = harness.addToBattlefieldAndReturn(player1, new GrixisGrimblade());
        assertThat(gqs.getEffectivePower(gd, grimblade)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, grimblade)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, grimblade, Keyword.DEATHTOUCH)).isFalse();

        harness.addToBattlefield(player1, new MistveinBorderpost());

        assertThat(gqs.getEffectivePower(gd, grimblade)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, grimblade)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, grimblade, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Two Grimblades each count the other, without stacking the bonus")
    void copiesEnableEachOtherAndBonusDoesNotStack() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrixisGrimblade());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrixisGrimblade());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new MistveinBorderpost());

        for (Permanent grimblade : List.of(first, second)) {
            assertThat(gqs.getEffectivePower(gd, grimblade)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, grimblade)).isEqualTo(2);
            assertThat(gqs.hasKeyword(gd, grimblade, Keyword.DEATHTOUCH)).isTrue();
        }

        gd.playerBattlefields.get(player1.getId()).remove(artifact);

        for (Permanent grimblade : List.of(first, second)) {
            assertThat(gqs.getEffectivePower(gd, grimblade)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, grimblade)).isEqualTo(2);
            assertThat(gqs.hasKeyword(gd, grimblade, Keyword.DEATHTOUCH)).isTrue();
        }

        gd.playerBattlefields.get(player1.getId()).remove(second);

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, first, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Multicolored cards in hand and graveyard do not enable the bonus")
    void multicoloredCardsOutsideBattlefieldDoNotCount() {
        Permanent grimblade = harness.addToBattlefieldAndReturn(player1, new GrixisGrimblade());
        harness.setHand(player1, List.of(new MistveinBorderpost()));
        harness.setGraveyard(player1, List.of(new GrixisGrimblade()));

        assertThat(gqs.getEffectivePower(gd, grimblade)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, grimblade)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, grimblade, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("A land's colored mana ability does not make it multicolored")
    void colorlessLandDoesNotEnableBonus() {
        Permanent grimblade = harness.addToBattlefieldAndReturn(player1, new GrixisGrimblade());
        harness.addToBattlefield(player1, new Island());

        assertThat(gqs.getEffectivePower(gd, grimblade)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, grimblade)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, grimblade, Keyword.DEATHTOUCH)).isFalse();
    }
}
