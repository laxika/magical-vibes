package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpidersilkArmorTest extends BaseCardTest {

    @Test
    @DisplayName("Your creatures get +0/+1 and reach")
    void buffsOwnCreaturesAndGrantsReach() {
        harness.addToBattlefield(player1, new SpidersilkArmor());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Spidersilk Armor does not affect an opponent's creatures")
    void doesNotAffectOpponentsCreatures() {
        harness.addToBattlefield(player1, new SpidersilkArmor());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent opponentBears = findPermanent(player2, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.REACH)).isFalse();
    }

    @Test
    @DisplayName("The bonus is removed when Spidersilk Armor leaves the battlefield")
    void bonusIsRemovedWhenArmorLeaves() {
        harness.addToBattlefield(player1, new SpidersilkArmor());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Spidersilk Armor"));

        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.REACH)).isFalse();
    }
}
