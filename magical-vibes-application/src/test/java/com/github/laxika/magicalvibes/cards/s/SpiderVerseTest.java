package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.cards.w.WorldheartPhoenix;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpiderVerse.class, SpiderPunk.class, ThinkTwice.class, WorldheartPhoenix.class})
class SpiderVerseTest extends BaseCardTest {

    @Test
    @DisplayName("Spider duplicates survive the legend rule")
    void spiderDuplicatesSurvive() {
        harness.addToBattlefield(player1, new SpiderVerse());
        harness.addToBattlefield(player1, new SpiderPunk());
        harness.addToBattlefield(player1, new SpiderPunk());

        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.permanentChoiceContext()).isNull();
    }

    @Test
    @DisplayName("Only non-Spiders are offered when a duplicate group mixes Spiders and non-Spiders")
    void onlyNonSpidersViolateLegendRule() {
        harness.addToBattlefield(player1, new SpiderVerse());
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new SpiderPunk());
        Permanent firstNonSpider = harness.addToBattlefieldAndReturn(player1, new SpiderPunk());
        Permanent secondNonSpider = harness.addToBattlefieldAndReturn(player1, new SpiderPunk());
        TestCards.mutableCard(firstNonSpider).setSubtypes(List.of());
        TestCards.mutableCard(secondNonSpider).setSubtypes(List.of());

        harness.runStateBasedActions();

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactly(firstNonSpider.getId(), secondNonSpider.getId());
        assertThat(choice.validPermanentIds()).doesNotContain(spider.getId());
    }

    @Test
    @DisplayName("A declined non-hand copy choice can be offered again, but acceptance consumes it")
    void oncePerTurnIsConsumedOnAcceptance() {
        harness.addToBattlefield(player1, new SpiderVerse());
        harness.setGraveyard(player1, List.of(new ThinkTwice(), new ThinkTwice(), new ThinkTwice()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castFromGraveyard(player1, 0);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.castFromGraveyard(player1, 0);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.castFromGraveyard(player1, 0);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A copied permanent spell gains haste")
    void copiedPermanentSpellGainsHaste() {
        harness.addToBattlefield(player1, new SpiderVerse());
        harness.setGraveyard(player1, List.of(new WorldheartPhoenix()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castFromGraveyard(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> phoenixes = findPermanents(player1, "Worldheart Phoenix");
        assertThat(phoenixes).hasSize(2);
        assertThat(phoenixes).filteredOn(p -> gqs.hasKeyword(gd, p,
                com.github.laxika.magicalvibes.model.Keyword.HASTE)).hasSize(1);
    }
}
