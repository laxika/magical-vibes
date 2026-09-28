package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.ChaosBalor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KarlachRagingTiefling;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TieflingOutcasts.class, KarlachRagingTiefling.class, ChaosBalor.class, GrizzlyBears.class})
class TieflingOutcastsTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts other Demons and Tieflings you control only")
    void boostsMatchingCreatures() {
        Permanent outcasts = addCreatureReady(player1, new TieflingOutcasts());
        Permanent tiefling = addCreatureReady(player1, new KarlachRagingTiefling());
        Permanent demon = addCreatureReady(player1, new ChaosBalor());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentDemon = addCreatureReady(player2, new ChaosBalor());

        assertThat(gqs.getEffectivePower(gd, outcasts)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, tiefling)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, demon)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentDemon)).isEqualTo(4);
    }

    @Test
    @DisplayName("Double team conjures a copy without double team")
    void doubleTeamConjuresCopy() {
        Permanent outcasts = addCreatureReady(player1, new TieflingOutcasts());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, outcasts, Keyword.DOUBLE_TEAM)).isFalse();
        Card copy = gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Tiefling Outcasts"))
                .findFirst()
                .orElseThrow();
        assertThat(copy.getKeywords()).doesNotContain(Keyword.DOUBLE_TEAM);
    }
}
