package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WightOfPrecinctSix.class, GrizzlyBears.class, Forest.class})
class WightOfPrecinctSixTest extends BaseCardTest {

    @Test
    @DisplayName("Is 1/1 when opponents' graveyards are empty")
    void baseSizeWithEmptyGraveyards() {
        Permanent wight = addWight();

        assertThat(gqs.getEffectivePower(gd, wight)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, wight)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets +1/+1 for each creature card in an opponent's graveyard")
    void boostsPerOpponentCreatureCard() {
        Permanent wight = addWight();
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        assertThat(gqs.getEffectivePower(gd, wight)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wight)).isEqualTo(3);
    }

    @Test
    @DisplayName("Ignores non-creature cards in opponents' graveyards")
    void ignoresNonCreatureCards() {
        Permanent wight = addWight();
        harness.setGraveyard(player2, List.of(new Forest(), new GrizzlyBears(), new Forest()));

        assertThat(gqs.getEffectivePower(gd, wight)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wight)).isEqualTo(2);
    }

    @Test
    @DisplayName("Ignores creature cards in its controller's own graveyard")
    void ignoresOwnGraveyard() {
        Permanent wight = addWight();
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        assertThat(gqs.getEffectivePower(gd, wight)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, wight)).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost updates dynamically as opponent's graveyard changes")
    void boostUpdatesDynamically() {
        Permanent wight = addWight();
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        assertThat(gqs.getEffectivePower(gd, wight)).isEqualTo(2);

        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        assertThat(gqs.getEffectivePower(gd, wight)).isEqualTo(4);
    }

    @Test
    @DisplayName("Bonus shrinks when creature cards leave the opponent's graveyard")
    void boostShrinksAsGraveyardEmpties() {
        Permanent wight = addWight();
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        assertThat(gqs.getEffectivePower(gd, wight)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wight)).isEqualTo(3);

        harness.setGraveyard(player2, List.of(new Forest()));

        assertThat(gqs.getEffectivePower(gd, wight)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, wight)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's Wight counts our graveyard rather than theirs")
    void opponentControlledWightCountsOurGraveyard() {
        Permanent wight = harness.addToBattlefieldAndReturn(player2, new WightOfPrecinctSix());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        assertThat(gqs.getEffectivePower(gd, wight)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wight)).isEqualTo(3);
    }

    private Permanent addWight() {
        return addCreatureReady(player1, new WightOfPrecinctSix());
    }
}
