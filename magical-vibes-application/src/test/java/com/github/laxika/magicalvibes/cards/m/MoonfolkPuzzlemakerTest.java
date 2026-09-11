package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoonfolkPuzzlemaker.class, GrizzlyBears.class})
class MoonfolkPuzzlemakerTest extends BaseCardTest {

    @Test
    @DisplayName("Scry 1 when Moonfolk Puzzlemaker becomes tapped")
    void scriesWhenBecomesTapped() {
        Permanent puzzlemaker = addCreatureReady(player1, new MoonfolkPuzzlemaker());
        Card originalTop = new GrizzlyBears();
        harness.setLibrary(player1, List.of(originalTop));

        tap(puzzlemaker);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(originalTop);
    }

    @Test
    @DisplayName("Tapping another creature you control does not trigger Moonfolk Puzzlemaker")
    void tappingAnotherCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new MoonfolkPuzzlemaker());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());

        tap(other);

        assertThat(gd.stack).isEmpty();
    }

    private void tap(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }
}
