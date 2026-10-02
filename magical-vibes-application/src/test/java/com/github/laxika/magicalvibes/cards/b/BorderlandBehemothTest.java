package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.i.IndomitableAncients;
import com.github.laxika.magicalvibes.cards.s.StonehewerGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BorderlandBehemoth.class, StonehewerGiant.class, IndomitableAncients.class})
class BorderlandBehemothTest extends BaseCardTest {

    @Test
    @DisplayName("Borderland Behemoth is 4/4 with no other Giants")
    void baseStatsWithNoOtherGiants() {
        Permanent behemoth = addCreatureReady(player1, new BorderlandBehemoth());

        assertThat(gqs.getEffectivePower(gd, behemoth)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, behemoth)).isEqualTo(4);
    }

    @Test
    @DisplayName("Borderland Behemoth gets +4/+4 for each other Giant you control")
    void countsOtherGiants() {
        Permanent behemoth = addCreatureReady(player1, new BorderlandBehemoth());
        addCreatureReady(player1, new StonehewerGiant());
        addCreatureReady(player1, new BorderlandBehemoth());

        assertThat(gqs.getEffectivePower(gd, behemoth)).isEqualTo(12);
        assertThat(gqs.getEffectiveToughness(gd, behemoth)).isEqualTo(12);
    }

    @Test
    @DisplayName("Borderland Behemoth does not count opponent's Giants")
    void doesNotCountOpponentGiants() {
        Permanent behemoth = addCreatureReady(player1, new BorderlandBehemoth());
        addCreatureReady(player2, new StonehewerGiant());

        assertThat(gqs.getEffectivePower(gd, behemoth)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, behemoth)).isEqualTo(4);
    }

    @Test
    @DisplayName("Borderland Behemoth does not count non-Giant creatures")
    void doesNotCountNonGiants() {
        Permanent behemoth = addCreatureReady(player1, new BorderlandBehemoth());
        addCreatureReady(player1, new IndomitableAncients());

        assertThat(gqs.getEffectivePower(gd, behemoth)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, behemoth)).isEqualTo(4);
    }

    @Test
    @DisplayName("Bonus updates when other Giants leave the battlefield")
    void bonusUpdatesWhenGiantsLeave() {
        Permanent behemoth = addCreatureReady(player1, new BorderlandBehemoth());
        Permanent giant = addCreatureReady(player1, new StonehewerGiant());

        assertThat(gqs.getEffectivePower(gd, behemoth)).isEqualTo(8);

        gd.playerBattlefields.get(player1.getId()).remove(giant);

        assertThat(gqs.getEffectivePower(gd, behemoth)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, behemoth)).isEqualTo(4);
    }
}
