package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BogImp;
import com.github.laxika.magicalvibes.cards.c.ChaosBalor;
import com.github.laxika.magicalvibes.cards.c.ChainDevil;
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

@CardUsed({TieflingOutcasts.class, KarlachRagingTiefling.class, ChaosBalor.class, GrizzlyBears.class, ChainDevil.class, BogImp.class})
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

    @Test
    void boostsDevilsWithoutChangingToughness() {
        addCreatureReady(player1, new TieflingOutcasts());
        Permanent devil = addCreatureReady(player1, new ChainDevil());
        Permanent opponentDevil = addCreatureReady(player2, new ChainDevil());

        assertThat(gqs.getEffectivePower(gd, devil)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, devil)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentDevil)).isEqualTo(4);
    }

    @Test
    void boostsImpsYouControlOnly() {
        addCreatureReady(player1, new TieflingOutcasts());
        Permanent imp = addCreatureReady(player1, new BogImp());
        Permanent opponentImp = addCreatureReady(player2, new BogImp());

        assertThat(gqs.getEffectivePower(gd, imp)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, imp)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opponentImp)).isEqualTo(1);
    }

    @Test
    void multipleOutcastsBoostEachOtherAndStopWhenSourceLeaves() {
        Permanent first = addCreatureReady(player1, new TieflingOutcasts());
        Permanent second = addCreatureReady(player1, new TieflingOutcasts());
        Permanent tiefling = addCreatureReady(player1, new KarlachRagingTiefling());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, tiefling)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, tiefling)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(first);

        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, tiefling)).isEqualTo(3);
    }

    @Test
    void originalDoesNotConjureAgainOnAnotherAttack() {
        Permanent outcasts = addCreatureReady(player1, new TieflingOutcasts());
        harness.setHand(player1, List.of());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        outcasts.untap();
        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
