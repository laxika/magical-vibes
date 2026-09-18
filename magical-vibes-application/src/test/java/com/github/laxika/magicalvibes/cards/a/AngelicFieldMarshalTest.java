package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.EdgarMarkov;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AngelicFieldMarshal.class, EdgarMarkov.class, GrizzlyBears.class})
class AngelicFieldMarshalTest extends BaseCardTest {

    @Test
    void commanderGrantsBoostAndVigilanceToAllControlledCreatures() {
        addToCommandZone(player1, new EdgarMarkov());
        addCreatureReady(player1, new EdgarMarkov());
        Permanent marshal = addCreatureReady(player1, new AngelicFieldMarshal());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, marshal)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, marshal)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, marshal, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void noControlledCommanderMeansNoLieutenantBonus() {
        Permanent marshal = addCreatureReady(player1, new AngelicFieldMarshal());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, marshal)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, marshal)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, marshal, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
    }

    private void addToCommandZone(Player player, Card card) {
        gd.playerCommandZones.get(player.getId()).add(card);
    }
}
