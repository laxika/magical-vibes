package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AshenmoorCohort;
import com.github.laxika.magicalvibes.cards.b.BallynockCohort;
import com.github.laxika.magicalvibes.cards.g.GoldenglowMoth;
import com.github.laxika.magicalvibes.cards.g.GreaterAuramancy;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KithkinRabble.class, BallynockCohort.class, GoldenglowMoth.class,
        GreaterAuramancy.class, AshenmoorCohort.class})
class KithkinRabbleTest extends BaseCardTest {

    @Test
    @DisplayName("Counts itself as a white permanent when alone: 1/1")
    void countsItselfWhenAlone() {
        Permanent rabble = addCreatureReady(player1, new KithkinRabble());

        assertThat(gqs.getEffectivePower(gd, rabble)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, rabble)).isEqualTo(1);
    }

    @Test
    @DisplayName("P/T equals the number of white permanents you control")
    void ptEqualsWhitePermanents() {
        Permanent rabble = addCreatureReady(player1, new KithkinRabble());
        addCreatureReady(player1, new GoldenglowMoth());
        addCreatureReady(player1, new BallynockCohort());

        // itself + 2 white creatures = 3
        assertThat(gqs.getEffectivePower(gd, rabble)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, rabble)).isEqualTo(3);
    }

    @Test
    @DisplayName("Counts white noncreature permanents")
    void countsWhiteNonCreaturePermanents() {
        Permanent rabble = addCreatureReady(player1, new KithkinRabble());
        harness.addToBattlefield(player1, new GreaterAuramancy());

        assertThat(gqs.getEffectivePower(gd, rabble)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rabble)).isEqualTo(2);
    }

    @Test
    @DisplayName("Non-white permanents are not counted")
    void nonWhiteNotCounted() {
        Permanent rabble = addCreatureReady(player1, new KithkinRabble());
        addCreatureReady(player1, new AshenmoorCohort());

        assertThat(gqs.getEffectivePower(gd, rabble)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, rabble)).isEqualTo(1);
    }

    @Test
    @DisplayName("Only counts your white permanents, not the opponent's")
    void countsOnlyControllersPermanents() {
        Permanent rabble = addCreatureReady(player1, new KithkinRabble());
        addCreatureReady(player2, new GoldenglowMoth());

        assertThat(gqs.getEffectivePower(gd, rabble)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, rabble)).isEqualTo(1);
    }

    @Test
    @DisplayName("P/T updates when white permanents change")
    void ptUpdatesWhenWhitePermanentsChange() {
        Permanent rabble = addCreatureReady(player1, new KithkinRabble());
        Permanent whiteSupport = addCreatureReady(player1, new GoldenglowMoth());
        assertThat(gqs.getEffectivePower(gd, rabble)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(whiteSupport);
        assertThat(gqs.getEffectivePower(gd, rabble)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, rabble)).isEqualTo(1);
    }
}
