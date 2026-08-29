package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BindingTheOldGodsTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I destroys a nonland permanent controlled by an opponent")
    void chapterIDestroysOpponentsNonlandPermanent() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addSaga(player1, 0);

        triggerChapter();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Chapter II puts a Forest from the library onto the battlefield tapped")
    void chapterIISearchesForTappedForest() {
        addSaga(player1, 1);
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), forest));

        triggerChapter();
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        gs.handleInteractionAnswer(gameData, player1, new InteractionAnswer.LibraryCardChosen(0));

        Permanent searchedForest = gameData.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() == forest)
                .findFirst()
                .orElse(null);
        assertThat(searchedForest).isNotNull();
        assertThat(searchedForest.isTapped()).isTrue();
        assertThat(gameData.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gameData.playerDecks.get(player1.getId()).getFirst()).isInstanceOf(GrizzlyBears.class);
    }

    @Test
    @DisplayName("Chapter III grants deathtouch to your creatures until end of turn")
    void chapterIIIGrantsTemporaryDeathtouch() {
        addSaga(player1, 2);
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentsCreature = addCreatureReady(player2, new GrizzlyBears());

        triggerChapter();
        harness.passBothPriorities();

        assertThat(ownCreature.getGrantedKeywords()).contains(Keyword.DEATHTOUCH);
        assertThat(opponentsCreature.getGrantedKeywords()).doesNotContain(Keyword.DEATHTOUCH);

        ownCreature.resetModifiers();
        assertThat(ownCreature.getGrantedKeywords()).doesNotContain(Keyword.DEATHTOUCH);
    }

    @Test
    @DisplayName("Chapter I cannot target a land or a permanent controlled by its controller")
    void chapterIOnlyOffersLegalOpponentNonlandTargets() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentLand = new Permanent(new Forest());
        gd.playerBattlefields.get(player2.getId()).add(opponentLand);
        Permanent legalTarget = addCreatureReady(player2, new GrizzlyBears());
        addSaga(player1, 0);

        triggerChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(legalTarget.getId());
        assertThat(choice.validPermanentIds()).doesNotContain(ownCreature.getId(), opponentLand.getId());
    }

    private Permanent addSaga(com.github.laxika.magicalvibes.model.Player player, int loreCounters) {
        Permanent saga = new Permanent(new BindingTheOldGods());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        gd.playerBattlefields.get(player.getId()).add(saga);
        return saga;
    }

    private void triggerChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
