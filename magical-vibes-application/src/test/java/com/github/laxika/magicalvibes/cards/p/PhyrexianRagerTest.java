package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AngelfireCrusader;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhyrexianRager.class, AngelfireCrusader.class})
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
        harness.setLibrary(player1, List.of(new AngelfireCrusader()));

        harness.castFromHand(player1, new PhyrexianRager(), "{2}{B}");
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);
        harness.assertInHand(player1, "Angelfire Crusader");
        harness.assertLife(player1, lifeBefore - 1);
    }

    @Test
    @DisplayName("ETB affects only its controller")
    void etbAffectsOnlyController() {
        harness.setLibrary(player1, List.of(new AngelfireCrusader()));
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castFromHand(player1, new PhyrexianRager(), "{2}{B}");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB

        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore);
        harness.assertLife(player2, opponentLifeBefore);
    }
}
