package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhyrexianRager.class, HillGiant.class})
class PhyrexianRagerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Phyrexian Rager puts it on stack as creature spell")
    void castingPutsOnStack() {
        harness.castFromHand(player1, new PhyrexianRager(), "{2}{B}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Resolving creature spell puts ETB trigger on stack")
    void resolvingCreaturePutsEtbOnStack() {
        harness.castFromHand(player1, new PhyrexianRager(), "{2}{B}");
        harness.passBothPriorities(); // resolve creature spell

        harness.assertOnBattlefield(player1, "Phyrexian Rager");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("ETB draws a card and loses 1 life")
    void etbDrawsAndLosesLife() {
        harness.setLibrary(player1, List.of(new HillGiant()));

        harness.castFromHand(player1, new PhyrexianRager(), "{2}{B}");
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);
        harness.assertInHand(player1, "Hill Giant");
        harness.assertLife(player1, lifeBefore - 1);
    }

    @Test
    @DisplayName("ETB affects only its controller")
    void etbAffectsOnlyController() {
        harness.setLibrary(player1, List.of(new HillGiant()));
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castFromHand(player1, new PhyrexianRager(), "{2}{B}");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore);
        harness.assertLife(player2, opponentLifeBefore);
    }

    @Test
    @DisplayName("An empty library does not prevent the trigger's life loss")
    void emptyLibraryStillLosesLife() {
        harness.setLibrary(player1, List.of());
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new PhyrexianRager(), "{2}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("At one life the controller draws before losing the game")
    void drawsAtOneLife() {
        harness.setLibrary(player1, List.of(new HillGiant()));
        harness.setLife(player1, 1);
        harness.castFromHand(player1, new PhyrexianRager(), "{2}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Hill Giant");
        harness.assertLife(player1, 0);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }
}
