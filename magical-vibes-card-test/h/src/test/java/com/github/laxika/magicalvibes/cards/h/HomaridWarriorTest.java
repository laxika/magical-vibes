package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BindingGrasp;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HomaridWarrior.class, ProdigalSorcerer.class, SavorTheMoment.class, BindingGrasp.class})
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

    @Test
    @DisplayName("Tapping and shroud happen on resolution rather than as activation costs")
    void effectsWaitForResolution() {
        Permanent warrior = addCreatureReady(player1, new HomaridWarrior());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(warrior.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, warrior, Keyword.SHROUD)).isFalse();

        harness.passBothPriorities();

        assertThat(warrior.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, warrior, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Multiple activations all prevent only the same next untap")
    void multipleActivationsDoNotSkipMultipleUntaps() {
        Permanent warrior = addCreatureReady(player1, new HomaridWarrior());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.performUntapStep(player2);
        assertThat(warrior.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(warrior.isTapped()).isTrue();
        harness.performUntapStep(player2);
        harness.performUntapStep(player1);
        assertThat(warrior.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Shroud also prevents its controller from targeting it")
    void shroudPreventsItsControllersTargets() {
        Permanent warrior = addCreatureReady(player1, new HomaridWarrior());
        addCreatureReady(player1, new ProdigalSorcerer());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, warrior.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Changing controllers does not move the restriction to the new controller's untap")
    void untapRestrictionRemainsWithActivatingPlayer() {
        Permanent warrior = addCreatureReady(player2, new HomaridWarrior());
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        harness.setHand(player1, List.of(new BindingGrasp()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castEnchantment(player1, 0, warrior.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(warrior);
        assertThat(warrior.isTapped()).isTrue();

        harness.performUntapStep(player2);
        harness.performUntapStep(player1);

        assertThat(warrior.isTapped()).isFalse();
    }
}
