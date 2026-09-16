package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MurasaBehemoth.class, Forest.class, GrizzlyBears.class})
class MurasaBehemothTest extends BaseCardTest {

    @Test
    @DisplayName("Does not get the bonus without a land card in its controller's graveyard")
    void noBonusWithoutLandCard() {
        Permanent behemoth = addBehemoth();
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        assertStats(behemoth, 5, 5);
    }

    @Test
    @DisplayName("Gets +3/+3 with a land card in its controller's graveyard")
    void getsBonusWithLandCard() {
        Permanent behemoth = addBehemoth();
        harness.setGraveyard(player1, List.of(new Forest()));

        assertStats(behemoth, 8, 8);
    }

    @Test
    @DisplayName("Only checks its controller's graveyard")
    void ignoresOpponentsGraveyard() {
        Permanent behemoth = addBehemoth();
        harness.setGraveyard(player2, List.of(new Forest()));

        assertStats(behemoth, 5, 5);
    }

    private Permanent addBehemoth() {
        return harness.addToBattlefieldAndReturn(player1, new MurasaBehemoth());
    }

    private void assertStats(Permanent behemoth, int power, int toughness) {
        assertThat(gqs.getEffectivePower(gd, behemoth)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, behemoth)).isEqualTo(toughness);
    }
}
