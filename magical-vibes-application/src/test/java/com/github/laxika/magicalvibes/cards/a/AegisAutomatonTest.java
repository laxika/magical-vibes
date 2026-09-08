package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AegisAutomatonTest extends BaseCardTest {

    @Test
    @DisplayName("Activated ability returns another creature you control to its owner's hand")
    void returnsAnotherCreatureYouControl() {
        Permanent automaton = harness.addToBattlefieldAndReturn(player1, new AegisAutomaton());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addAbilityMana();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(automaton), 0,
                null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Aegis Automaton");
    }

    @Test
    @DisplayName("Cannot target Aegis Automaton itself")
    void cannotTargetItself() {
        Permanent automaton = harness.addToBattlefieldAndReturn(player1, new AegisAutomaton());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(automaton),
                0,
                null,
                automaton.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature you control");
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        Permanent automaton = harness.addToBattlefieldAndReturn(player1, new AegisAutomaton());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(automaton),
                0,
                null,
                target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature you control");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent automaton = harness.addToBattlefieldAndReturn(player1, new AegisAutomaton());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(automaton),
                0,
                null,
                island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature you control");
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
