package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KorvoldAndTheNobleThief.class, Forest.class, GrizzlyBears.class})
class KorvoldAndTheNobleThiefTest extends BaseCardTest {

    @Test
    void firstTwoChaptersCreateTreasureTokens() {
        Permanent saga = addSaga(0);

        triggerChapter();
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);

        saga.setCounterCount(CounterType.LORE, 1);
        triggerChapter();
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    @Test
    void thirdChapterExilesThreeCardsAndLetsControllerPlayThemThisTurn() {
        Card first = new Forest();
        Card second = new GrizzlyBears();
        Card third = new Forest();
        harness.setLibrary(player2, List.of(first, second, third));
        addSaga(2);

        triggerChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPlayerIds()).containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions.values())
                .containsOnly(player1.getId());
        assertThat(gd.exilePlayPermissions).hasSize(3);
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).hasSize(3);
    }

    private Permanent addSaga(int loreCounters) {
        Permanent saga = new Permanent(new KorvoldAndTheNobleThief());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        gd.playerBattlefields.get(player1.getId()).add(saga);
        return saga;
    }

    private void triggerChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
