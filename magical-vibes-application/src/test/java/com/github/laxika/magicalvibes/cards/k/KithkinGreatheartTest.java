package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GoldmeadowStalwart;
import com.github.laxika.magicalvibes.cards.h.HillcomberGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KithkinGreatheart.class, HillcomberGiant.class, GoldmeadowStalwart.class})
class KithkinGreatheartTest extends BaseCardTest {

    // ===== Conditional +1/+1 and first strike with a Giant =====

    @Test
    @DisplayName("Gets +1/+1 and first strike when controller controls a Giant")
    void boostedWithGiant() {
        harness.addToBattlefield(player1, new KithkinGreatheart());
        harness.addToBattlefield(player1, new HillcomberGiant());

        Permanent greatheart = findPermanent(player1, "Kithkin Greatheart");
        assertThat(gqs.getEffectivePower(gd, greatheart)).isEqualTo(3); // 2 base + 1
        assertThat(gqs.getEffectiveToughness(gd, greatheart)).isEqualTo(2); // 1 base + 1
        assertThat(gqs.hasKeyword(gd, greatheart, Keyword.FIRST_STRIKE)).isTrue();
    }

    // ===== No bonus without a Giant =====

    @Test
    @DisplayName("No bonus without a Giant")
    void noBonusWithoutGiant() {
        harness.addToBattlefield(player1, new KithkinGreatheart());

        Permanent greatheart = findPermanent(player1, "Kithkin Greatheart");
        assertThat(gqs.getEffectivePower(gd, greatheart)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, greatheart)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, greatheart, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Non-Giant creature does not grant bonus")
    void nonGiantDoesNotGrantBonus() {
        harness.addToBattlefield(player1, new KithkinGreatheart());
        harness.addToBattlefield(player1, new GoldmeadowStalwart());

        Permanent greatheart = findPermanent(player1, "Kithkin Greatheart");
        assertThat(gqs.getEffectivePower(gd, greatheart)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, greatheart, Keyword.FIRST_STRIKE)).isFalse();
    }

    // ===== Loses bonus when the Giant leaves =====

    @Test
    @DisplayName("Loses bonus when the Giant leaves the battlefield")
    void losesBonusWhenGiantLeaves() {
        harness.addToBattlefield(player1, new KithkinGreatheart());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillcomberGiant());

        Permanent greatheart = findPermanent(player1, "Kithkin Greatheart");
        assertThat(gqs.getEffectivePower(gd, greatheart)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, greatheart, Keyword.FIRST_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(giant);

        assertThat(gqs.getEffectivePower(gd, greatheart)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, greatheart, Keyword.FIRST_STRIKE)).isFalse();
    }

    // ===== Opponent's Giant doesn't count =====

    @Test
    @DisplayName("Opponent's Giant does not grant bonus")
    void opponentGiantDoesNotCount() {
        harness.addToBattlefield(player1, new KithkinGreatheart());
        harness.addToBattlefield(player2, new HillcomberGiant());

        Permanent greatheart = findPermanent(player1, "Kithkin Greatheart");
        assertThat(gqs.getEffectivePower(gd, greatheart)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, greatheart, Keyword.FIRST_STRIKE)).isFalse();
    }

}
