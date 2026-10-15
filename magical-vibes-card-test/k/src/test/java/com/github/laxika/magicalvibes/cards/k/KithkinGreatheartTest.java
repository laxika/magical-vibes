package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.g.GoldmeadowStalwart;
import com.github.laxika.magicalvibes.cards.h.HillcomberGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KithkinGreatheart.class, HillcomberGiant.class, GoldmeadowStalwart.class, AvianChangeling.class})
class KithkinGreatheartTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 and first strike when controller controls a Giant")
    void boostedWithGiant() {
        Permanent greatheart = harness.addToBattlefieldAndReturn(player1, new KithkinGreatheart());
        harness.addToBattlefield(player1, new HillcomberGiant());

        assertThat(gqs.getEffectivePower(gd, greatheart)).isEqualTo(3); // 2 base + 1
        assertThat(gqs.getEffectiveToughness(gd, greatheart)).isEqualTo(2); // 1 base + 1
        assertThat(gqs.hasKeyword(gd, greatheart, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("No bonus without a Giant")
    void noBonusWithoutGiant() {
        Permanent greatheart = harness.addToBattlefieldAndReturn(player1, new KithkinGreatheart());

        assertThat(gqs.getEffectivePower(gd, greatheart)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, greatheart)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, greatheart, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Non-Giant creature does not grant bonus")
    void nonGiantDoesNotGrantBonus() {
        Permanent greatheart = harness.addToBattlefieldAndReturn(player1, new KithkinGreatheart());
        harness.addToBattlefield(player1, new GoldmeadowStalwart());

        assertThat(gqs.getEffectivePower(gd, greatheart)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, greatheart, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Loses bonus when the Giant leaves the battlefield")
    void losesBonusWhenGiantLeaves() {
        Permanent greatheart = harness.addToBattlefieldAndReturn(player1, new KithkinGreatheart());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillcomberGiant());

        assertThat(gqs.getEffectivePower(gd, greatheart)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, greatheart, Keyword.FIRST_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(giant);

        assertThat(gqs.getEffectivePower(gd, greatheart)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, greatheart, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Opponent's Giant does not grant bonus")
    void opponentGiantDoesNotCount() {
        Permanent greatheart = harness.addToBattlefieldAndReturn(player1, new KithkinGreatheart());
        harness.addToBattlefield(player2, new HillcomberGiant());

        assertThat(gqs.getEffectivePower(gd, greatheart)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, greatheart, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Changeling grants the Giant bonus")
    void changelingGrantsBonus() {
        Permanent greatheart = harness.addToBattlefieldAndReturn(player1, new KithkinGreatheart());
        harness.addToBattlefield(player1, new AvianChangeling());

        assertThat(gqs.getEffectivePower(gd, greatheart)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, greatheart)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, greatheart, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Multiple Giants grant only one bonus, which remains while one Giant is present")
    void multipleGiantsDoNotStackBonus() {
        Permanent greatheart = harness.addToBattlefieldAndReturn(player1, new KithkinGreatheart());
        Permanent firstGiant = harness.addToBattlefieldAndReturn(player1, new HillcomberGiant());
        harness.addToBattlefield(player1, new HillcomberGiant());

        assertThat(gqs.getEffectivePower(gd, greatheart)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, greatheart)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, greatheart, Keyword.FIRST_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(firstGiant);

        assertThat(gqs.getEffectivePower(gd, greatheart)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, greatheart)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, greatheart, Keyword.FIRST_STRIKE)).isTrue();
    }
}
