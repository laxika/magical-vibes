package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.NyxbornCourser;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheBirthOfMeletis.class, Forest.class, NyxbornCourser.class, Plains.class})
class TheBirthOfMeletisTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I searches for a basic Plains and puts it into hand")
    void chapterISearchesForBasicPlains() {
        Permanent saga = addSagaWithLore(0);
        harness.setLibrary(player1, List.of(new Forest(), new NyxbornCourser(), new Plains()));

        advanceToNextChapter();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).singleElement()
                .satisfies(card -> assertThat(card.getSubtypes()).contains(CardSubtype.PLAINS));

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getSubtypes().contains(CardSubtype.PLAINS));
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
    }

    @Test
    @DisplayName("Chapter II creates a 0/4 colorless Wall artifact creature token with defender")
    void chapterIICreatesWallToken() {
        Permanent saga = addSagaWithLore(1);

        advanceToNextChapter();

        Permanent wall = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent != saga && permanent.getCard().getName().equals("Wall"))
                .findFirst()
                .orElseThrow();
        assertThat(wall.getCard().getPower()).isZero();
        assertThat(wall.getCard().getToughness()).isEqualTo(4);
        assertThat(wall.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(wall.getCard().getSubtypes()).contains(CardSubtype.WALL);
        assertThat(wall.getCard().getKeywords()).contains(Keyword.DEFENDER);
    }

    @Test
    @DisplayName("Chapter III gains 2 life")
    void chapterIIIGainsLife() {
        Permanent saga = addSagaWithLore(2);
        int lifeBefore = gd.getLife(player1.getId());

        advanceToNextChapter();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
    }

    @Test
    @DisplayName("Casting the Saga triggers chapter I as it enters")
    void castingTriggersChapterI() {
        Plains plains = new Plains();
        harness.setLibrary(player1, List.of(new Forest(), plains));
        harness.castFromHand(player1, new TheBirthOfMeletis(), "{1}{W}");

        harness.passBothPriorities();

        Permanent saga = findPermanent(player1, "The Birth of Meletis");
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(plains);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(plains);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(saga);
    }

    @Test
    @DisplayName("Chapter I may fail to find even when a Plains is available")
    void chapterICanFailToFind() {
        addSagaWithLore(0);
        Plains plains = new Plains();
        Forest forest = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(plains, forest));

        advanceToNextChapter();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(plains, forest);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Chapter I completes without a matching Plains")
    void chapterIWithoutMatchingPlains() {
        Permanent saga = addSagaWithLore(0);
        Forest forest = new Forest();
        NyxbornCourser courser = new NyxbornCourser();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(forest, courser));

        advanceToNextChapter();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, courser);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
    }

    @Test
    @DisplayName("Chapter II creates exactly one colorless creature for its controller")
    void wallIsColorlessCreatureControlledBySagaController() {
        Permanent saga = addSagaWithLore(1);

        advanceToNextChapter();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        Permanent wall = findPermanent(player1, "Wall");
        assertThat(wall.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(wall.getCard().getColors()).isEmpty();
        assertThat(wall.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
    }

    @Test
    @DisplayName("The Saga is sacrificed only after chapter III leaves the stack")
    void finalChapterRemainsOnBattlefieldUntilResolution() {
        Permanent saga = addSagaWithLore(2);
        harness.setLife(player1, 10);
        harness.setLife(player2, 15);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);

        harness.passUntil(TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
        harness.assertLife(player1, 10);
        harness.passBothPriorities();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 15);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(saga.getCard());
    }

    @Test
    @DisplayName("Chapter I completes with an empty library")
    void chapterIWithEmptyLibrary() {
        Permanent saga = addSagaWithLore(0);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());

        advanceToNextChapter();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The opponent's precombat main phase does not advance the Saga")
    void opponentsTurnDoesNotAdvanceSaga() {
        Permanent saga = addSagaWithLore(1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DRAW);

        harness.passUntil(TurnStep.PRECOMBAT_MAIN);

        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(saga);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheBirthOfMeletis());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
        harness.passBothPriorities();
    }
}
