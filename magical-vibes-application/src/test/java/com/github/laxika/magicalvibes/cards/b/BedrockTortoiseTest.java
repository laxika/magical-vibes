package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BedrockTortoise.class, GiantSpider.class, GoblinPiker.class, GrizzlyBears.class})
class BedrockTortoiseTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control with greater toughness assign combat damage equal to toughness")
    void higherToughnessCreaturesUseToughnessForCombatDamage() {
        addReadyCreature(player1, new BedrockTortoise());
        Permanent spider = addReadyCreature(player1, new GiantSpider());
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        Permanent piker = addReadyCreature(player1, new GoblinPiker());

        assertThat(gqs.getEffectiveCombatDamage(gd, spider)).isEqualTo(4);
        assertThat(gqs.getEffectiveCombatDamage(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveCombatDamage(gd, piker)).isEqualTo(2);
    }

    @Test
    @DisplayName("The Tortoise itself and only your creatures use the restricted damage effect")
    void sourceAndControllerScopeAreCorrect() {
        Permanent tortoise = addReadyCreature(player1, new BedrockTortoise());
        Permanent opponentSpider = addReadyCreature(player2, new GiantSpider());

        assertThat(gqs.getEffectiveCombatDamage(gd, tortoise)).isEqualTo(6);
        assertThat(gqs.getEffectiveCombatDamage(gd, opponentSpider)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures you control have hexproof only during your turn")
    void grantsHexproofDuringControllerTurn() {
        Permanent tortoise = addReadyCreature(player1, new BedrockTortoise());
        Permanent spider = addReadyCreature(player1, new GiantSpider());

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, tortoise, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, spider, Keyword.HEXPROOF)).isTrue();

        harness.forceActivePlayer(player2);
        assertThat(gqs.hasKeyword(gd, tortoise, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, spider, Keyword.HEXPROOF)).isFalse();
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player,
                                       com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
