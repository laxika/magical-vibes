package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MoonholdTest extends BaseCardTest {

    /** Player1 casts Moonhold at player2 on player1's turn, paying the given mana. */
    private void castMoonholdAtPlayer2(int red, int white) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Moonhold()));
        if (red > 0) harness.addMana(player1, ManaColor.RED, red);
        if (white > 0) harness.addMana(player1, ManaColor.WHITE, white);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    /**
     * Hand indices player2 could play right now on their own main phase, with a land at index 0
     * (needs no mana) and an affordable creature at index 1.
     */
    private List<Integer> player2Playable() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player2, List.of(new Forest(), new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.clearPriorityPassed();
        harness.ensurePriority(player2);
        return harness.getGameBroadcastService()
                .getPlayableCardIndices(harness.getGameData(), player2.getId());
    }

    @Test
    @DisplayName("{R} spent: target can't play lands but can still cast creatures")
    void redSpentBlocksLands() {
        castMoonholdAtPlayer2(3, 0); // {2}{R/W} all red → only {R} spent

        List<Integer> playable = player2Playable();
        assertThat(playable).doesNotContain(0); // land blocked
        assertThat(playable).contains(1);        // creature still castable
    }

    @Test
    @DisplayName("{W} spent: target can't cast creatures but can still play lands")
    void whiteSpentBlocksCreatures() {
        castMoonholdAtPlayer2(0, 3); // {2}{R/W} all white → only {W} spent

        List<Integer> playable = player2Playable();
        assertThat(playable).contains(0);        // land still playable
        assertThat(playable).doesNotContain(1);  // creature blocked
    }

    @Test
    @DisplayName("{R} and {W} both spent: target can neither play lands nor cast creatures")
    void bothColorsBlockBoth() {
        // {2} generic split across colors and the {R/W} hybrid the other → both {R} and {W} spent.
        castMoonholdAtPlayer2(2, 2);

        List<Integer> playable = player2Playable();
        assertThat(playable).doesNotContain(0);
        assertThat(playable).doesNotContain(1);
    }

    @Test
    @DisplayName("Restriction wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        castMoonholdAtPlayer2(3, 0);
        assertThat(player2Playable()).doesNotContain(0);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(player2Playable()).contains(0);
    }
}
