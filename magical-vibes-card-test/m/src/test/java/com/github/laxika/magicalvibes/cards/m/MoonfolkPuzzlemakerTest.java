package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.ArmguardFamiliar;
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

@CardUsed({MoonfolkPuzzlemaker.class, ArmguardFamiliar.class})
class MoonfolkPuzzlemakerTest extends BaseCardTest {

    @Test
    @DisplayName("Scry 1 when Moonfolk Puzzlemaker becomes tapped")
    void scriesWhenBecomesTapped() {
        Permanent puzzlemaker = addCreatureReady(player1, new MoonfolkPuzzlemaker());
        Card originalTop = new ArmguardFamiliar();
        harness.setLibrary(player1, List.of(originalTop));

        tap(puzzlemaker);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(originalTop);
    }

    @Test
    @DisplayName("Tapping another creature you control does not trigger Moonfolk Puzzlemaker")
    void tappingAnotherCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new MoonfolkPuzzlemaker());
        Permanent other = addCreatureReady(player1, new ArmguardFamiliar());

        tap(other);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void attackingTriggersScryAndAllowsBottomingOnlyTheTopCard() {
        addCreatureReady(player1, new MoonfolkPuzzlemaker());
        Card top = new ArmguardFamiliar();
        Card second = new MoonfolkPuzzlemaker();
        harness.setLibrary(player1, List.of(top, second));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, top);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappingOneCopyTriggersOnlyThatCopy() {
        Permanent first = addCreatureReady(player1, new MoonfolkPuzzlemaker());
        harness.addToBattlefield(player1, new MoonfolkPuzzlemaker());

        tap(first);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void opponentCopyScriesItsControllersLibrary() {
        harness.addToBattlefield(player1, new MoonfolkPuzzlemaker());
        Permanent opposing = addCreatureReady(player2, new MoonfolkPuzzlemaker());
        Card ownTop = new ArmguardFamiliar();
        Card opposingTop = new ArmguardFamiliar();
        Card opposingSecond = new MoonfolkPuzzlemaker();
        harness.setLibrary(player1, List.of(ownTop));
        harness.setLibrary(player2, List.of(opposingTop, opposingSecond));

        tap(opposing);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownTop);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opposingSecond, opposingTop);
    }

    @Test
    void scryWithEmptyLibraryFinishesWithoutAChoice() {
        Permanent puzzlemaker = addCreatureReady(player1, new MoonfolkPuzzlemaker());
        harness.setLibrary(player1, List.of());

        tap(puzzlemaker);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void tap(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }
}
