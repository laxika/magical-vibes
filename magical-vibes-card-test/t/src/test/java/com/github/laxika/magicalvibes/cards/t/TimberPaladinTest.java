package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TimberPaladin.class, Pacifism.class})
class TimberPaladinTest extends BaseCardTest {

    @Test
    void scalesWithExactlyOneTwoOrThreeAuras() {
        Permanent paladin = addCreatureReady(player1, new TimberPaladin());

        assertStats(paladin, 1, 1);
        assertThat(gqs.hasKeyword(gd, paladin, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, paladin, Keyword.TRAMPLE)).isFalse();

        Permanent firstAura = attachAura(paladin);
        assertStats(paladin, 3, 3);
        assertThat(gqs.hasKeyword(gd, paladin, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, paladin, Keyword.TRAMPLE)).isFalse();

        Permanent secondAura = attachAura(paladin);
        assertStats(paladin, 5, 5);
        assertThat(gqs.hasKeyword(gd, paladin, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, paladin, Keyword.TRAMPLE)).isFalse();

        Permanent thirdAura = attachAura(paladin);
        assertStats(paladin, 10, 10);
        assertThat(gqs.hasKeyword(gd, paladin, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, paladin, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(thirdAura);
        assertStats(paladin, 5, 5);
        assertThat(gqs.hasKeyword(gd, paladin, Keyword.TRAMPLE)).isFalse();

        gd.playerBattlefields.get(player1.getId()).remove(secondAura);
        assertStats(paladin, 3, 3);
        assertThat(gqs.hasKeyword(gd, paladin, Keyword.VIGILANCE)).isFalse();

        gd.playerBattlefields.get(player1.getId()).remove(firstAura);
        assertStats(paladin, 1, 1);
    }

    private Permanent attachAura(Permanent paladin) {
        Permanent aura = new Permanent(new Pacifism());
        aura.setAttachedTo(paladin.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }

    private void assertStats(Permanent permanent, int power, int toughness) {
        assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(toughness);
    }
}
