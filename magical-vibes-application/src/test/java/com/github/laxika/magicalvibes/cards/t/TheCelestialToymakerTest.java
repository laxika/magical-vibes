package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheCelestialToymaker.class, Forest.class, Island.class, Swamp.class})
class TheCelestialToymakerTest extends BaseCardTest {

    @Test
    void defendingPlayerChoosesBetweenFaceUpAndFaceDownPiles() {
        addReadyToymaker();
        Card faceUpOne = new Forest();
        Card faceUpTwo = new Island();
        Card faceDown = new Swamp();
        harness.setLibrary(player1, List.of(faceUpOne, faceUpTwo, faceDown));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice separation =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(separation.playerId()).isEqualTo(player1.getId());
        harness.handleMultipleCardsChosen(player1, List.of(faceUpOne.getId(), faceUpTwo.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(faceUpOne, faceUpTwo);
        assertThat(gd.exiledCards).anySatisfy(exiled -> {
            assertThat(exiled.card()).isSameAs(faceDown);
            assertThat(exiled.faceDown()).isTrue();
        });
        assertThat(gd.pileGroupingOrGuessCountThisTurn).isEqualTo(1);
    }

    @Test
    void endStepLifeLossUsesDistinctPileGroupingCount() {
        addReadyToymaker();
        Card first = new Forest();
        Card second = new Island();
        Card third = new Swamp();
        harness.setLibrary(player1, List.of(first, second, third));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.handleMayAbilityChosen(player2, true);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
    }

    private Permanent addReadyToymaker() {
        return addCreatureReady(player1, new TheCelestialToymaker());
    }
}
