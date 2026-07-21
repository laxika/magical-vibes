package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.q.QasaliAmbusher;
import com.github.laxika.magicalvibes.cards.w.WoollyThoctar;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GrixisGrimbladeTest extends BaseCardTest {

    // ===== With another multicolored permanent =====

    @Test
    @DisplayName("Gets +1/+1 (becomes 3/2) and deathtouch while controlling another multicolored permanent")
    void boostWithAnotherMulticolored() {
        harness.addToBattlefield(player1, new GrixisGrimblade());
        harness.addToBattlefield(player1, new QasaliAmbusher()); // {1}{G}{W}, GW multicolored

        Permanent grimblade = findPermanent(player1, "Grixis Grimblade");
        assertThat(gqs.getEffectivePower(gd, grimblade)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, grimblade)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, grimblade, Keyword.DEATHTOUCH)).isTrue();
    }

    // ===== Without another multicolored permanent =====

    @Test
    @DisplayName("Base 2/1 with no deathtouch when alone (its own multicoloredness does not count)")
    void noBoostAlone() {
        harness.addToBattlefield(player1, new GrixisGrimblade());

        Permanent grimblade = findPermanent(player1, "Grixis Grimblade");
        assertThat(gqs.getEffectivePower(gd, grimblade)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, grimblade)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, grimblade, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("An opponent's multicolored permanent does not grant the boost")
    void opponentMulticoloredDoesNotCount() {
        harness.addToBattlefield(player1, new GrixisGrimblade());
        harness.addToBattlefield(player2, new WoollyThoctar()); // {R}{G}{W}, multicolored, opponent

        Permanent grimblade = findPermanent(player1, "Grixis Grimblade");
        assertThat(gqs.getEffectivePower(gd, grimblade)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, grimblade)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, grimblade, Keyword.DEATHTOUCH)).isFalse();
    }

    // ===== Boost is dynamic =====

    @Test
    @DisplayName("Loses +1/+1 and deathtouch when the other multicolored permanent leaves")
    void losesBoostWhenMulticoloredLeaves() {
        harness.addToBattlefield(player1, new GrixisGrimblade());
        harness.addToBattlefield(player1, new WoollyThoctar());

        Permanent grimblade = findPermanent(player1, "Grixis Grimblade");
        assertThat(gqs.getEffectivePower(gd, grimblade)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, grimblade, Keyword.DEATHTOUCH)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Woolly Thoctar"));

        assertThat(gqs.getEffectivePower(gd, grimblade)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, grimblade)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, grimblade, Keyword.DEATHTOUCH)).isFalse();
    }
}
