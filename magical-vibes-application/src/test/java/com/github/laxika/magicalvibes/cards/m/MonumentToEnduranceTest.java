package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.Censor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MonumentToEnduranceTest extends BaseCardTest {

    private static final String DRAW = "Draw a card";
    private static final String TREASURE = "Create a Treasure token";
    private static final String LIFE_LOSS = "Each opponent loses 3 life";

    @Test
    @DisplayName("Each mode can be chosen once per turn")
    void eachModeCanBeChosenOncePerTurn() {
        harness.addToBattlefield(player1, new MonumentToEndurance());
        harness.setHand(player1, List.of(new Censor(), new Censor(), new Censor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.setLife(player2, 20);

        cycleAndChoose(DRAW);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);

        cycleAndChoose(TREASURE);
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);

        cycleAndChoose(LIFE_LOSS);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("A fourth discard in the same turn has no effect after all modes were chosen")
    void fourthDiscardHasNoEffect() {
        harness.addToBattlefield(player1, new MonumentToEndurance());
        harness.setHand(player1, List.of(new Censor(), new Censor(), new Censor(), new Censor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.setLife(player2, 20);

        cycleAndChoose(DRAW);
        cycleAndChoose(TREASURE);
        cycleAndChoose(LIFE_LOSS);

        cycleCard();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    @DisplayName("The mode choices reset when a new turn begins")
    void choicesResetAtTurnStart() {
        harness.addToBattlefield(player1, new MonumentToEndurance());
        harness.setHand(player1, List.of(new Censor(), new Censor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        cycleAndChoose(DRAW);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 1);
        cycleCard();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).contains(DRAW);
    }

    private void cycleAndChoose(String mode) {
        cycleCard();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, mode);
        settleStack();
    }

    private void cycleCard() {
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
    }

    private void settleStack() {
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
