package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EchoOfDusk.class, GrizzlyBears.class, Opt.class})
class EchoOfDuskTest extends BaseCardTest {

    @Test
    @DisplayName("Does not get the bonus without four permanent cards in its controller's graveyard")
    void noBonusBelowThreshold() {
        Permanent echo = addEcho();
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new Opt()));

        assertBaseStatsAndNoLifelink(echo);
    }

    @Test
    @DisplayName("Gets +1/+1 and lifelink with four permanent cards in its controller's graveyard")
    void getsBonusAtThreshold() {
        Permanent echo = addEcho();
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        assertThat(gqs.getEffectivePower(gd, echo)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, echo)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, echo, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Loses the bonus when its controller's graveyard drops below four permanent cards")
    void losesBonusBelowThreshold() {
        Permanent echo = addEcho();
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        assertThat(gqs.hasKeyword(gd, echo, Keyword.LIFELINK)).isTrue();

        gd.playerGraveyards.get(player1.getId()).removeFirst();

        assertBaseStatsAndNoLifelink(echo);
    }

    private Permanent addEcho() {
        return harness.addToBattlefieldAndReturn(player1, new EchoOfDusk());
    }

    private void assertBaseStatsAndNoLifelink(Permanent echo) {
        assertThat(gqs.getEffectivePower(gd, echo)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, echo)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, echo, Keyword.LIFELINK)).isFalse();
    }
}
