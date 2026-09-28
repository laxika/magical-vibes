package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AlabasterLeech;
import com.github.laxika.magicalvibes.cards.c.ChromaticSphere;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.NoblePanther;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RewardsOfDiversity.class, AlabasterLeech.class, ChromaticSphere.class, Forest.class,
        NoblePanther.class})
class RewardsOfDiversityTest extends BaseCardTest {

    private void setUpOpponentTurn() {
        harness.addToBattlefield(player1, new RewardsOfDiversity());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Opponent's multicolored spell makes you gain 4 life")
    void opponentMulticoloredSpellGainsLife() {
        setUpOpponentTurn();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player2, new NoblePanther(), "{1}{G}{W}");

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore + 4);
    }

    @Test
    @DisplayName("Opponent's monocolored spell does not trigger")
    void opponentMonocoloredSpellDoesNotTrigger() {
        setUpOpponentTurn();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player2, new AlabasterLeech(), "{W}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);

        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore);
    }

    @Test
    @DisplayName("Opponent's colorless spell does not trigger")
    void opponentColorlessSpellDoesNotTrigger() {
        setUpOpponentTurn();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player2, new ChromaticSphere(), "{1}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);

        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore);
    }

    @Test
    @DisplayName("Your own multicolored spell does not trigger")
    void ownMulticoloredSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new RewardsOfDiversity());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new NoblePanther(), "{1}{G}{W}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);

        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore);
    }

    @Test
    @DisplayName("Playing a land does not trigger")
    void playingALandDoesNotTrigger() {
        setUpOpponentTurn();
        harness.setHand(player2, List.of(new Forest()));

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, lifeBefore);
    }
}
