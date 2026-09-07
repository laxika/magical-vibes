package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BattleBrawlerTest extends BaseCardTest {

    @Test
    void getsBonusAndFirstStrikeWithRedPermanent() {
        harness.addToBattlefield(player1, new BattleBrawler());
        harness.addToBattlefield(player1, new GoblinPiker());

        assertBattleBrawlerHasBonus();
    }

    @Test
    void getsBonusAndFirstStrikeWithWhitePermanent() {
        harness.addToBattlefield(player1, new BattleBrawler());
        harness.addToBattlefield(player1, new BenalishKnight());

        assertBattleBrawlerHasBonus();
    }

    @Test
    void doesNotGetBonusFromOpponentsPermanent() {
        harness.addToBattlefield(player1, new BattleBrawler());
        harness.addToBattlefield(player2, new BenalishKnight());

        GameData gd = harness.getGameData();
        Permanent brawler = findPermanent(player1, "Battle Brawler");

        assertThat(gqs.getEffectivePower(gd, brawler)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, brawler, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void doesNotGetBonusWithoutRedOrWhitePermanent() {
        harness.addToBattlefield(player1, new BattleBrawler());
        harness.addToBattlefield(player1, new GrizzlyBears());

        GameData gd = harness.getGameData();
        Permanent brawler = findPermanent(player1, "Battle Brawler");

        assertThat(gqs.getEffectivePower(gd, brawler)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, brawler, Keyword.FIRST_STRIKE)).isFalse();
    }

    private void assertBattleBrawlerHasBonus() {
        GameData gd = harness.getGameData();
        Permanent brawler = findPermanent(player1, "Battle Brawler");

        assertThat(gqs.getEffectivePower(gd, brawler)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, brawler)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, brawler, Keyword.FIRST_STRIKE)).isTrue();
    }
}
