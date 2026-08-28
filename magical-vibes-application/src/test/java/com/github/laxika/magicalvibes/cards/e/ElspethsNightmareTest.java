package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElspethsNightmare.class, Forest.class, GrizzlyBears.class, HillGiant.class, Opt.class})
class ElspethsNightmareTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I destroys only an opponent's creature with power 2 or less")
    void chapterIDestroysEligibleOpponentCreature() {
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent opponentGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        addSagaWithLore(0);

        triggerNextChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(opponentBears.getId());
        assertThat(choice.validIds()).doesNotContain(ownBears.getId(), opponentGiant.getId());

        harness.handlePermanentChosen(player1, opponentBears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownBears);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentGiant).doesNotContain(opponentBears);
    }

    @Test
    @DisplayName("Chapter II lets you choose a noncreature, nonland card for an opponent to discard")
    void chapterIITargetsOpponentHand() {
        Card land = new Forest();
        Card creature = new GrizzlyBears();
        Card chosenCard = new Opt();
        harness.setHand(player2, List.of(land, creature, chosenCard));
        addSagaWithLore(1);

        triggerNextChapter();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice.validIndices()).containsExactly(2);
        harness.handleCardChosen(player1, 2);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land, creature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(chosenCard);
    }

    @Test
    @DisplayName("Chapter III exiles the targeted opponent's graveyard")
    void chapterIIIExilesOpponentGraveyard() {
        Card graveyardCard = new GrizzlyBears();
        Permanent saga = addSagaWithLore(2);
        harness.setGraveyard(player2, List.of(graveyardCard));

        triggerNextChapter();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(graveyardCard);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new ElspethsNightmare());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void triggerNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
