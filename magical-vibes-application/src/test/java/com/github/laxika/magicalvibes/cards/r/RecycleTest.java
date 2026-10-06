package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LotusPetal;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Recycle.class, Forest.class, Mountain.class, LotusPetal.class, Spellbook.class})
class RecycleTest extends BaseCardTest {

    @Test
    @DisplayName("Controller skips their draw step")
    void controllerSkipsDrawStep() {
        harness.addToBattlefield(player1, new Recycle());

        harness.forceActivePlayer(player1);
        gd.turnNumber = 2; // avoid the first-turn skip
        harness.forceStep(TurnStep.UPKEEP);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance UPKEEP → DRAW, runs handleDrawStep

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
    }

    @Test
    @DisplayName("Casting a spell draws a card")
    void castingSpellDraws() {
        harness.addToBattlefield(player1, new Recycle());
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castFromHand(player1, new LotusPetal(), "{0}");
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
    }

    @Test
    @DisplayName("Playing a land draws a card")
    void playingLandDraws() {
        harness.addToBattlefield(player1, new Recycle());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new Forest()));
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Opponent casting a spell does not trigger the draw")
    void opponentSpellDoesNotDraw() {
        harness.addToBattlefield(player1, new Recycle());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castFromHand(player2, new LotusPetal(), "{0}");
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
    }

    @Test
    @DisplayName("Controller must discard down to two during cleanup")
    void controllerDiscardsDownToTwo() {
        harness.addToBattlefield(player1, new Recycle());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);

        harness.setHand(player1, List.of(
                new Forest(), new Forest(), new Mountain(), new Mountain()
        ));

        gs.advanceStep(gd);

        assertThat(gd.currentStep).isEqualTo(TurnStep.CLEANUP);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's maximum hand size is unaffected (still seven)")
    void opponentHandSizeUnaffected() {
        harness.addToBattlefield(player1, new Recycle());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        harness.setHand(player2, List.of(
                new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Mountain()
        ));

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(6);
    }

    @Test
    @DisplayName("The skipped draw step never begins")
    void skipsEntireDrawStep() {
        harness.addToBattlefield(player1, new Recycle());
        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);

        gs.advanceStep(gd);

        assertThat(gd.currentStep).isEqualTo(TurnStep.PRECOMBAT_MAIN);
    }

    @Test
    @DisplayName("Opponent still takes their normal draw")
    void opponentDrawStepIsUnaffected() {
        harness.addToBattlefield(player1, new Recycle());
        harness.forceActivePlayer(player2);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        int handBefore = gd.playerHands.get(player2.getId()).size();
        int deckBefore = gd.playerDecks.get(player2.getId()).size();

        gs.advanceStep(gd);

        assertThat(gd.currentStep).isEqualTo(TurnStep.DRAW);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckBefore - 1);
    }

    @Test
    @DisplayName("Casting Recycle itself does not draw a card")
    void doesNotTriggerForItsOwnCast() {
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castFromHand(player1, new Recycle(), "{4}{G}{G}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Recycle");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Opponent playing a land does not trigger Recycle")
    void opponentLandDoesNotDraw() {
        harness.addToBattlefield(player1, new Recycle());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Forest()));
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.playLand(player2, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
    }

    @Test
    @DisplayName("Recycle entering after Spellbook restores a maximum hand size of two")
    void newerRecycleOverridesSpellbook() {
        harness.addToBattlefieldAndReturn(player1, new Spellbook()).setTimestamp(1);
        harness.addToBattlefieldAndReturn(player1, new Recycle()).setTimestamp(2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new Forest(), new Forest(), new Mountain()));

        gs.advanceStep(gd);

        assertThat(gd.currentStep).isEqualTo(TurnStep.CLEANUP);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Spellbook entering after Recycle removes the maximum hand size")
    void newerSpellbookOverridesRecycle() {
        harness.addToBattlefieldAndReturn(player1, new Recycle()).setTimestamp(1);
        harness.addToBattlefieldAndReturn(player1, new Spellbook()).setTimestamp(2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new Forest(), new Forest(), new Mountain()));

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }
}
