package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MaskedVandal;
import com.github.laxika.magicalvibes.cards.m.Mistwalker;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheRavensWarning.class, Mistwalker.class, MaskedVandal.class, Forest.class})
class TheRavensWarningTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I creates a flying Bird and gains 2 life")
    void chapterICreatesBirdAndGainsLife() {
        Permanent saga = addSagaWithLore(0);
        int lifeBefore = gd.getLife(player1.getId());

        advanceSagaToNextChapter();
        harness.passBothPriorities();

        Permanent bird = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Bird"))
                .findFirst()
                .orElse(null);
        assertThat(bird).isNotNull();
        assertThat(bird.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(bird.getCard().getSubtypes()).contains(CardSubtype.BIRD);
        assertThat(bird.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Chapter II looks at the damaged player's hand and draws once for multiple flying creatures")
    void chapterIIBatchesFlyingCombatDamage() {
        addSagaWithLore(1);
        addCreatureReady(player1, new Mistwalker());
        addCreatureReady(player1, new Mistwalker());
        harness.setHand(player2, new ArrayList<>(List.of(new MaskedVandal())));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        advanceSagaToNextChapter();
        harness.passBothPriorities();

        int handBeforeCombat = gd.playerHands.get(player1.getId()).size();
        declareAttackers(List.of(1, 2));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBeforeCombat + 1);
        assertThat(harness.getConn1().getMessagesContaining("REVEAL_HAND"))
                .anyMatch(message -> message.contains("Masked Vandal"));
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_HAND")).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("looks at") && log.contains("hand"));
    }

    @Test
    @DisplayName("Chapter II does not trigger for nonflying combat damage")
    void chapterIIDoesNotTriggerForNonflyingCombatDamage() {
        addSagaWithLore(1);
        addCreatureReady(player1, new MaskedVandal());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        advanceSagaToNextChapter();
        harness.passBothPriorities();

        int handBeforeCombat = gd.playerHands.get(player1.getId()).size();
        declareAttackers(List.of(1));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBeforeCombat);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .noneMatch(log -> log.contains("looks at") && log.contains("hand"));
    }

    @Test
    @DisplayName("Chapter III may put a sideboard card on top of the library without revealing it")
    void chapterIIIPutsSideboardCardOnTop() {
        addSagaWithLore(2);
        Card chosen = new Forest();
        Card remaining = new MaskedVandal();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(chosen, remaining)));
        Card existingTop = new Forest();
        harness.setLibrary(player1, List.of(existingTop));

        advanceSagaToNextChapter();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().sourceSideboard()).isTrue();
        assertThat(search.params().reveals()).isFalse();
        assertThat(search.params().shuffleAfterSelection()).isFalse();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(chosen, existingTop);
    }

    @Test
    @DisplayName("Chapter II still draws for an empty opposing hand after the Saga leaves")
    void chapterIIContinuesAfterSagaLeavesWithEmptyOpposingHand() {
        Permanent saga = addSagaWithLore(1);
        addCreatureReady(player1, new Mistwalker());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        advanceSagaToNextChapter();
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(saga);
        gd.playerGraveyards.get(player1.getId()).add(saga.getCard());

        int handBeforeCombat = gd.playerHands.get(player1.getId()).size();
        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBeforeCombat + 1);
    }

    @Test
    @DisplayName("Chapter III can decline the outside-game card and still sacrifices the Saga")
    void chapterIIICanDecline() {
        addSagaWithLore(2);
        Card outsideCard = new Forest();
        Card existingTop = new Forest();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(outsideCard)));
        harness.setLibrary(player1, List.of(existingTop));

        advanceSagaToNextChapter();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(outsideCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(existingTop);
        harness.assertNotOnBattlefield(player1, "The Raven's Warning");
        harness.assertInGraveyard(player1, "The Raven's Warning");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Chapter III with no outside-game cards leaves the library alone and sacrifices the Saga")
    void chapterIIIWithEmptySideboard() {
        addSagaWithLore(2);
        gd.playerSideboards.put(player1.getId(), new ArrayList<>());
        Card existingTop = new Forest();
        harness.setLibrary(player1, List.of(existingTop));

        advanceSagaToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(existingTop);
        harness.assertNotOnBattlefield(player1, "The Raven's Warning");
        harness.assertInGraveyard(player1, "The Raven's Warning");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent addSagaWithLore(int loreCounters) {
        harness.addToBattlefield(player1, new TheRavensWarning());
        Permanent saga = findPermanent(player1, "The Raven's Warning");
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void advanceSagaToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
