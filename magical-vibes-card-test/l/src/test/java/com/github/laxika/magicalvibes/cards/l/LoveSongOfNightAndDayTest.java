package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.CoalitionWarbrute;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LoveSongOfNightAndDay.class, CoalitionWarbrute.class})
class LoveSongOfNightAndDayTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I makes you and a target opponent draw two cards")
    void chapterIDrawsTwoCardsForBothPlayers() {
        Card ownFirst = new CoalitionWarbrute();
        Card ownSecond = new CoalitionWarbrute();
        Card opponentFirst = new CoalitionWarbrute();
        Card opponentSecond = new CoalitionWarbrute();
        harness.setLibrary(player1, List.of(ownFirst, ownSecond));
        harness.setLibrary(player2, List.of(opponentFirst, opponentSecond));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        addSagaWithLore(0);

        triggerChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(ownFirst, ownSecond);
        assertThat(gd.playerHands.get(player2.getId())).containsExactlyInAnyOrder(opponentFirst, opponentSecond);
    }

    @Test
    @DisplayName("Chapter II creates a white Bird token with flying")
    void chapterIICreatesBirdToken() {
        addSagaWithLore(1);

        triggerChapter();
        harness.passBothPriorities();

        Permanent bird = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Bird"))
                .findFirst()
                .orElse(null);
        assertThat(bird).isNotNull();
        assertThat(bird.getEffectivePower()).isEqualTo(1);
        assertThat(bird.getEffectiveToughness()).isEqualTo(1);
        assertThat(bird.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(bird.getCard().getSubtypes()).contains(CardSubtype.BIRD);
        assertThat(bird.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Chapter III puts counters on up to two creatures")
    void chapterIIICountersTwoTargetCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new CoalitionWarbrute());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new CoalitionWarbrute());
        addSagaWithLore(2);

        triggerChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(ownCreature.getId(), opponentCreature.getId());
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Read ahead can start at chapter II without either player drawing")
    void readAheadStartsAtChapterTwo() {
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new CoalitionWarbrute(), new CoalitionWarbrute()));
        harness.setLibrary(player2, List.of(new CoalitionWarbrute(), new CoalitionWarbrute()));
        harness.castFromHand(player1, new LoveSongOfNightAndDay(), "{2}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "2");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bird");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof LoveSongOfNightAndDay)
                .findFirst().orElseThrow();
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Chapter III may choose no creatures and the Saga is sacrificed")
    void chapterIIICanChooseZeroTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CoalitionWarbrute());
        addSagaWithLore(2);

        triggerChapter();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Love Song of Night and Day");
        harness.assertNotOnBattlefield(player1, "Love Song of Night and Day");
    }

    @Test
    @DisplayName("Chapter III may choose just one creature even when two are available")
    void chapterIIICanChooseOneTarget() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new CoalitionWarbrute());
        Permanent unchosen = harness.addToBattlefieldAndReturn(player2, new CoalitionWarbrute());
        Permanent saga = addSagaWithLore(2);

        triggerChapter();
        PendingInteraction.PermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(firstChoice.validIds()).doesNotContain(saga.getId());
        harness.handlePermanentChosen(player1, chosen.getId());
        PendingInteraction.PermanentChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(secondChoice.validIds()).doesNotContain(chosen.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(chosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(unchosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Love Song of Night and Day");
    }

    @Test
    @DisplayName("Chapter III still counters the remaining target if another leaves the battlefield")
    void chapterIIIPartiallyResolves() {
        Permanent removed = harness.addToBattlefieldAndReturn(player1, new CoalitionWarbrute());
        Permanent remaining = harness.addToBattlefieldAndReturn(player2, new CoalitionWarbrute());
        addSagaWithLore(2);

        triggerChapter();
        harness.handlePermanentChosen(player1, removed.getId());
        harness.handlePermanentChosen(player1, remaining.getId());
        gd.playerBattlefields.get(player1.getId()).remove(removed);
        harness.passBothPriorities();

        assertThat(remaining.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(removed.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new LoveSongOfNightAndDay());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void triggerChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

}
