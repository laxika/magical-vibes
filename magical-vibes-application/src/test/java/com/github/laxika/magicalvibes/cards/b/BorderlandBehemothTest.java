package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AxegrinderGiant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BorderlandBehemothTest extends BaseCardTest {

    @Test
    @DisplayName("Borderland Behemoth is 4/4 with no other Giants")
    void baseStatsWithNoOtherGiants() {
        Permanent behemoth = addBehemoth(player1);

        assertThat(gqs.getEffectivePower(gd, behemoth)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, behemoth)).isEqualTo(4);
    }

    @Test
    @DisplayName("Borderland Behemoth gets +4/+4 for each other Giant you control")
    void countsOtherGiants() {
        Permanent behemoth = addBehemoth(player1);
        harness.addToBattlefield(player1, new AxegrinderGiant());
        harness.addToBattlefield(player1, new BorderlandBehemoth());

        assertThat(gqs.getEffectivePower(gd, behemoth)).isEqualTo(12);
        assertThat(gqs.getEffectiveToughness(gd, behemoth)).isEqualTo(12);
    }

    @Test
    @DisplayName("Borderland Behemoth does not count opponent's Giants")
    void doesNotCountOpponentGiants() {
        Permanent behemoth = addBehemoth(player1);
        harness.addToBattlefield(player2, new AxegrinderGiant());

        assertThat(gqs.getEffectivePower(gd, behemoth)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, behemoth)).isEqualTo(4);
    }

    @Test
    @DisplayName("Borderland Behemoth does not count non-Giant creatures")
    void doesNotCountNonGiants() {
        Permanent behemoth = addBehemoth(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, behemoth)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, behemoth)).isEqualTo(4);
    }

    @Test
    @DisplayName("Bonus updates when other Giants leave the battlefield")
    void bonusUpdatesWhenGiantsLeave() {
        Permanent behemoth = addBehemoth(player1);
        harness.addToBattlefield(player1, new AxegrinderGiant());

        assertThat(gqs.getEffectivePower(gd, behemoth)).isEqualTo(8);

        gd.playerBattlefields.get(player1.getId()).removeIf(
                p -> p.getCard().getName().equals("Axegrinder Giant"));

        assertThat(gqs.getEffectivePower(gd, behemoth)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, behemoth)).isEqualTo(4);
    }

    private Permanent addBehemoth(Player player) {
        Permanent permanent = new Permanent(new BorderlandBehemoth());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
