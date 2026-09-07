package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZuranEnchanter;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AbyssalNocturnus.class, GrizzlyBears.class, ZuranEnchanter.class})
class AbyssalNocturnusTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent discarding a card gives it +2/+2 and fear")
    void opponentDiscardBoostsAndGrantsFear() {
        Permanent nocturnus = addCreatureReady(player1, new AbyssalNocturnus());
        addCreatureReady(player1, new ZuranEnchanter());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        readyEnchanterMana();

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        assertThat(nocturnus.getPowerModifier()).isEqualTo(2);
        assertThat(nocturnus.getToughnessModifier()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, nocturnus, Keyword.FEAR)).isTrue();
    }

    @Test
    @DisplayName("Each opponent discard stacks another boost")
    void opponentDiscardsStack() {
        Permanent nocturnus = addCreatureReady(player1, new AbyssalNocturnus());
        addCreatureReady(player1, new ZuranEnchanter());
        addCreatureReady(player1, new ZuranEnchanter());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears())));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        harness.activateAbility(player1, 2, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        assertThat(nocturnus.getPowerModifier()).isEqualTo(4);
        assertThat(nocturnus.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("The boost and fear wear off at end of turn")
    void boostAndFearWearOffAtEndOfTurn() {
        Permanent nocturnus = addCreatureReady(player1, new AbyssalNocturnus());
        addCreatureReady(player1, new ZuranEnchanter());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        readyEnchanterMana();

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(nocturnus.getPowerModifier()).isZero();
        assertThat(nocturnus.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, nocturnus, Keyword.FEAR)).isFalse();
    }

    @Test
    @DisplayName("A controller discard does not trigger it")
    void controllerDiscardDoesNotTrigger() {
        Permanent nocturnus = addCreatureReady(player1, new AbyssalNocturnus());
        addCreatureReady(player1, new ZuranEnchanter());
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        readyEnchanterMana();

        harness.activateAbility(player1, 1, null, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(nocturnus.getPowerModifier()).isZero();
        assertThat(nocturnus.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, nocturnus, Keyword.FEAR)).isFalse();
    }

    private void readyEnchanterMana() {
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
