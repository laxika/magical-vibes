package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
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
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheCaveOfTwoLovers.class, Mountain.class})
class TheCaveOfTwoLoversTest extends BaseCardTest {

    @Test
    void chapterICreatesTwoAllyTokens() {
        addSaga(0);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Ally")).hasSize(2);
    }

    @Test
    void chapterIISearchesForMountainOrCave() {
        addSaga(1);
        Mountain mountain = new Mountain();
        Card cave = caveCard();
        harness.setLibrary(player1, List.of(new Card(), mountain, cave));

        advanceToNextChapter();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(mountain, cave);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(search.params().cards().indexOf(cave)));

        assertThat(gd.playerHands.get(player1.getId())).contains(cave);
    }

    @Test
    void chapterIIIEarthbendsALandYouControl() {
        addSaga(2);
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, mountain.getId());
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, mountain)).isTrue();
        assertThat(gqs.isCreature(gd, mountain)).isTrue();
        assertThat(gqs.getEffectivePower(gd, mountain)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mountain)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, mountain, Keyword.HASTE)).isTrue();
        assertThat(mountain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void chapterIIIEarthbendRejectsALandControlledByOpponent() {
        addSaga(2);
        Permanent ownMountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());

        advanceToNextChapter();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");

        harness.handlePermanentChosen(player1, ownMountain.getId());
        harness.passBothPriorities();
    }

    private Permanent addSaga(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheCaveOfTwoLovers());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Card caveCard() {
        Card cave = new Card();
        cave.setName("Cave");
        cave.setType(CardType.LAND);
        cave.setSubtypes(List.of(CardSubtype.CAVE));
        return cave;
    }
}
