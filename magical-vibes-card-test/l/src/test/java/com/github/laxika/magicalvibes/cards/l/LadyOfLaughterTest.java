package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LadyOfLaughter.class, GrizzlyBears.class, Forest.class})
class LadyOfLaughterTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card after two nonland permanents enter under your control")
    void drawsAfterTwoNonlandPermanentsEnter() {
        Card drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        castLadyOfLaughter();
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToEndStep();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Does not draw without two nonland permanents entering")
    void doesNotDrawWithoutTwoNonlandPermanents() {
        Card drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        castLadyOfLaughter();

        advanceToEndStep();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Does not count lands toward celebration")
    void doesNotCountLands() {
        Card drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        castLadyOfLaughter();
        harness.enterBattlefieldAndReturn(player1, new Forest());

        advanceToEndStep();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Does not count permanents entering under an opponent's control")
    void doesNotCountOpponentsPermanents() {
        Card drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        castLadyOfLaughter();
        harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToEndStep();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
    }

    private void castLadyOfLaughter() {
        harness.setHand(player1, List.of(new LadyOfLaughter()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
