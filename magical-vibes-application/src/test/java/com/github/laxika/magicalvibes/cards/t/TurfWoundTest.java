package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.NomadicElf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TurfWound.class, Forest.class, NomadicElf.class})
class TurfWoundTest extends BaseCardTest {

    private void castAt(Player target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new TurfWound()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private List<Integer> playableCards(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player, List.of(new Forest(), new NomadicElf()));
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
    }

    @Test
    @DisplayName("Only the targeted player is prevented from playing lands")
    void restrictsOnlyTargetedPlayer() {
        castAt(player2);

        assertThat(playableCards(player2)).doesNotContain(0);
        assertThat(playableCards(player1)).contains(0);
    }

    @Test
    @DisplayName("Land restriction wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        castAt(player2);
        assertThat(playableCards(player2)).doesNotContain(0);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(playableCards(player2)).contains(0);
    }

    @Test
    @DisplayName("Controller draws a card immediately")
    void controllerDrawsCard() {
        harness.setLibrary(player1, List.of(new NomadicElf()));
        castAt(player2);

        assertThat(harness.getGameData().playerHands.get(player1.getId()))
                .anyMatch(card -> card instanceof NomadicElf);
        assertThat(harness.getGameData().playerDecks.get(player1.getId())).isEmpty();
    }
}
