package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.cards.s.SavorTheMoment;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HomaridWarrior.class, ProdigalSorcerer.class, SavorTheMoment.class})
class HomaridWarriorTest extends BaseCardTest {

    @Test
    @DisplayName("Requires one blue mana to activate")
    void requiresBlueMana() {
        addCreatureReady(player1, new HomaridWarrior());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Can activate while already tapped")
    void canActivateWhileAlreadyTapped() {
        Permanent warrior = addCreatureReady(player1, new HomaridWarrior());
        warrior.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(warrior.isTapped()).isTrue();

        advanceToUpkeep(player1);

        assertThat(warrior.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating grants shroud, taps itself, and skips next untap")
    void activatingGrantsShroudTapsAndSkipsUntap() {
        Permanent warrior = addCreatureReady(player1, new HomaridWarrior());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(warrior.getGrantedKeywords()).contains(Keyword.SHROUD);
        assertThat(warrior.isTapped()).isTrue();

        advanceToUpkeep(player1);

        assertThat(warrior.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Shroud wears off at end of turn")
    void shroudWearsOffAtEndOfTurn() {
        Permanent warrior = addCreatureReady(player1, new HomaridWarrior());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(warrior.getGrantedKeywords()).contains(Keyword.SHROUD);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(warrior.getGrantedKeywords()).doesNotContain(Keyword.SHROUD);
    }

    @Test
    @DisplayName("Shroud prevents an opposing ability from targeting it")
    void shroudPreventsTargeting() {
        Permanent warrior = addCreatureReady(player1, new HomaridWarrior());
        addCreatureReady(player2, new ProdigalSorcerer());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, warrior.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The next-untap restriction waits through a wholly skipped untap step")
    void nextUntapRestrictionWaitsThroughSkippedUntapStep() {
        Permanent warrior = addCreatureReady(player1, new HomaridWarrior());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.castFromHand(player1, new SavorTheMoment(), "{1}{U}{U}");
        harness.passBothPriorities();

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        assertThat(warrior.isTapped()).isTrue();

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        assertThat(warrior.isTapped()).isTrue();
    }
}
