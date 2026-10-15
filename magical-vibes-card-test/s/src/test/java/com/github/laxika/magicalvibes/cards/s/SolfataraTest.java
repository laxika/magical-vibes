package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.q.Quicksand;
import com.github.laxika.magicalvibes.cards.r.RiverBoa;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Solfatara.class, Quicksand.class, RiverBoa.class})
class SolfataraTest extends BaseCardTest {

    private void castAt(Player target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Solfatara()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private List<Integer> playableCards(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player, List.of(new Quicksand(), new RiverBoa()));
        harness.addMana(player, ManaColor.GREEN, 2);
        harness.clearPriorityPassed();
        harness.ensurePriority(player);
        return harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(harness.getGameData(), player.getId());
    }

    @Test
    @DisplayName("Target can't play lands this turn but can still cast creatures")
    void blocksLandsButNotCreatures() {
        castAt(player2);

        List<Integer> playable = playableCards(player2);
        assertThat(playable).doesNotContain(0);
        assertThat(playable).contains(1);

        harness.castCreature(player2, 1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "River Boa");
    }

    @Test
    @DisplayName("Only the chosen player is prevented from playing lands")
    void restrictsOnlyChosenPlayer() {
        castAt(player1);

        List<Integer> casterPlayable = playableCards(player1);
        assertThat(casterPlayable).doesNotContain(0);
        assertThat(casterPlayable).contains(1);

        assertThat(playableCards(player2)).contains(0);
    }

    @Test
    @DisplayName("Land restriction wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        castAt(player2);
        assertThat(playableCards(player2)).doesNotContain(0);

        harness.passUntil(player1, TurnStep.UPKEEP);
        harness.passBothPriorities();

        assertThat(playableCards(player2)).contains(0);
    }

    @Test
    @DisplayName("Schedules a draw for the controller at the next turn's upkeep")
    void schedulesDelayedDraw() {
        castAt(player2);

        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());
        assertThat(scheduled.getFirst().count()).isEqualTo(1);
    }

    @Test
    @DisplayName("The scheduled draw resolves at the next upkeep")
    void drawResolvesAtNextUpkeep() {
        castAt(player2);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }

    @Test
    @DisplayName("Casting during upkeep does not draw until the following turn's upkeep")
    void upkeepCastWaitsForNextTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new Solfatara()));
        harness.setLibrary(player1, List.of(new RiverBoa(), new Quicksand(), new RiverBoa()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passUntil(player1, TurnStep.DRAW);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }
}
