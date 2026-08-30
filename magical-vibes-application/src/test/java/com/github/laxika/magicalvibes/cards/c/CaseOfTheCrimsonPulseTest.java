package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CaseOfTheCrimsonPulse.class, GrizzlyBears.class, Island.class, Mountain.class})
class CaseOfTheCrimsonPulseTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, discards a card and draws two cards")
    void entersAndDiscardsThenDraws() {
        GrizzlyBears discarded = new GrizzlyBears();
        Island firstDraw = new Island();
        Mountain secondDraw = new Mountain();
        harness.setHand(player1, List.of(new CaseOfTheCrimsonPulse(), discarded));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        addCastMana();

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The enter trigger still draws two cards with an empty hand")
    void entersAndDrawsWithEmptyHand() {
        Island firstDraw = new Island();
        Mountain secondDraw = new Mountain();
        harness.setHand(player1, List.of(new CaseOfTheCrimsonPulse()));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        addCastMana();

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
    }

    @Test
    @DisplayName("Solves at the end step when the controller has no cards in hand")
    void solvesWithEmptyHand() {
        Permanent pulse = harness.addToBattlefieldAndReturn(player1, new CaseOfTheCrimsonPulse());
        harness.setHand(player1, List.of());

        resolveEndStepTriggers();

        assertThat(pulse.isSolved()).isTrue();
    }

    @Test
    @DisplayName("The solved upkeep trigger discards the hand and draws two cards")
    void solvedUpkeepDiscardsAndDrawsTwo() {
        Permanent pulse = harness.addToBattlefieldAndReturn(player1, new CaseOfTheCrimsonPulse());
        harness.setHand(player1, List.of());
        resolveEndStepTriggers();
        assertThat(pulse.isSolved()).isTrue();

        GrizzlyBears discarded = new GrizzlyBears();
        Island firstDraw = new Island();
        Mountain secondDraw = new Mountain();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));

        gd.turnNumber = 2;
        advanceToUpkeep();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private void addCastMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void resolveEndStepTriggers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void advanceToUpkeep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.UPKEEP);
    }
}
