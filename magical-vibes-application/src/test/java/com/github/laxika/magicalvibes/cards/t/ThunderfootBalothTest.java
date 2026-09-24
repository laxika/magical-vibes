package com.github.laxika.magicalvibes.cards.t;

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

@CardUsed({ThunderfootBaloth.class, EdgarMarkov.class, GrizzlyBears.class})
class ThunderfootBalothTest extends BaseCardTest {

    @Test
    void lieutenantBoostsSourceAndOtherControlledCreatures() {
        addCommanderToBattlefield(player1);
        Permanent baloth = addCreatureReady(player1, new ThunderfootBaloth());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingBears = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, baloth)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, baloth)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, baloth, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opposingBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingBears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opposingBears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void withoutCommanderLieutenantDoesNotApply() {
        Permanent baloth = addCreatureReady(player1, new ThunderfootBaloth());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, baloth)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, baloth)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    private void addCommanderToBattlefield(Player player) {
        Card commander = new EdgarMarkov();
        gd.playerCommandZones.get(player.getId()).add(commander);
        addCreatureReady(player, new EdgarMarkov());
    }
}
