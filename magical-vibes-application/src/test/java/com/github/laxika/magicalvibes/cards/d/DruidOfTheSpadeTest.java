package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(DruidOfTheSpade.class)
class DruidOfTheSpadeTest extends BaseCardTest {

    @Test
    void hasNoBonusWithoutToken() {
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new DruidOfTheSpade());

        assertThat(gqs.getEffectivePower(gd, druid)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, druid)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, druid, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void getsBonusAndTrampleWhileControllingToken() {
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new DruidOfTheSpade());
        addToken(player1);

        assertThat(gqs.getEffectivePower(gd, druid)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, druid)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, druid, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void opponentTokenDoesNotCount() {
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new DruidOfTheSpade());
        addToken(player2);

        assertThat(gqs.getEffectivePower(gd, druid)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, druid, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void losesBonusAndTrampleWhenTokenLeaves() {
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new DruidOfTheSpade());
        Permanent token = addToken(player1);
        assertThat(gqs.hasKeyword(gd, druid, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(token);

        assertThat(gqs.getEffectivePower(gd, druid)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, druid)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, druid, Keyword.TRAMPLE)).isFalse();
    }

    private Permanent addToken(Player player) {
        Card tokenCard = new Card() {};
        tokenCard.setName("Soldier Token");
        tokenCard.setType(CardType.CREATURE);
        tokenCard.setToken(true);

        Permanent token = new Permanent(tokenCard);
        token.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(token);
        return token;
    }
}
