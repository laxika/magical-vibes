package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.Geistflame;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.cards.w.WorldheartPhoenix;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpiderVerse.class, SpiderPunk.class, ThinkTwice.class, WorldheartPhoenix.class, Geistflame.class})
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
        resolveAllTriggers();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.castFromGraveyard(player1, 0);
        resolveAllTriggers();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.castFromGraveyard(player1, 0);
        resolveAllTriggers();
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
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        List<Permanent> phoenixes = findPermanents(player1, "Worldheart Phoenix");
        assertThat(phoenixes).hasSize(2);
        assertThat(phoenixes).filteredOn(p -> gqs.hasKeyword(gd, p,
                com.github.laxika.magicalvibes.model.Keyword.HASTE)).hasSize(1);
    }

    @Test
    @DisplayName("Players can respond before the optional copy decision is made")
    void copyDecisionWaitsForTriggerResolution() {
        harness.addToBattlefield(player1, new SpiderVerse());
        harness.setGraveyard(player1, List.of(new ThinkTwice()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFromGraveyard(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(2);

        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("A hand cast does not offer a copy or consume the later non-hand copy")
    void handCastDoesNotConsumeCopy() {
        harness.addToBattlefield(player1, new SpiderVerse());
        harness.castFromHand(player1, new ThinkTwice(), "{1}{U}");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromGraveyard(player1, 0);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("An opponent's non-hand cast is not copied")
    void opponentCastDoesNotTrigger() {
        harness.addToBattlefield(player1, new SpiderVerse());
        harness.setGraveyard(player2, List.of(new ThinkTwice()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castFromGraveyard(player2, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Opponent-controlled Spiders still obey the legend rule")
    void opponentSpidersAreNotExempt() {
        harness.addToBattlefield(player1, new SpiderVerse());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new SpiderPunk());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new SpiderPunk());

        harness.runStateBasedActions();

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactly(first.getId(), second.getId());
    }

    @Test
    @DisplayName("One non-Spider and one Spider with the same legendary name both survive")
    void singleNonSpiderWithSpiderSurvives() {
        harness.addToBattlefield(player1, new SpiderVerse());
        harness.addToBattlefield(player1, new SpiderPunk());
        Permanent nonSpider = harness.addToBattlefieldAndReturn(player1, new SpiderPunk());
        TestCards.mutableCard(nonSpider).setSubtypes(List.of());

        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.permanentChoiceContext()).isNull();
    }

    @Test
    @DisplayName("The copy can choose a new target without changing the original spell")
    void copyCanChooseNewTarget() {
        harness.addToBattlefield(player1, new SpiderVerse());
        harness.setGraveyard(player1, List.of(new Geistflame()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFromGraveyardTargeting(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("A copied permanent resolves as a token while the original remains a card")
    void permanentCopyBecomesToken() {
        harness.addToBattlefield(player1, new SpiderVerse());
        harness.setGraveyard(player1, List.of(new WorldheartPhoenix()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castFromGraveyard(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Worldheart Phoenix")).hasSize(2);
        assertThat(findPermanents(player1, "Worldheart Phoenix"))
                .filteredOn(p -> p.getCard().isToken()).hasSize(1);
    }

    @Test
    @DisplayName("Keeping the original targets deals damage twice and copying resets on the next turn")
    void keepTargetsAndCopyAgainOnOpponentsTurn() {
        harness.addToBattlefield(player1, new SpiderVerse());
        harness.setGraveyard(player1, List.of(new Geistflame(), new Geistflame()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFromGraveyardTargeting(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
        harness.assertLife(player2, 18);

        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castFromGraveyardTargeting(player1, 0, player2.getId());
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertLife(player2, 16);
    }
}
