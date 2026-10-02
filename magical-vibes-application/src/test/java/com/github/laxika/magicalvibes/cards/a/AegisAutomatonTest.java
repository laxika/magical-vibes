package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.r.RenegadeMap;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AegisAutomaton.class, Ornithopter.class, RenegadeMap.class})
class AegisAutomatonTest extends BaseCardTest {

    @Test
    @DisplayName("Activated ability returns another creature you control to its owner's hand")
    void returnsAnotherCreatureYouControl() {
        Permanent automaton = harness.addToBattlefieldAndReturn(player1, new AegisAutomaton());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        addAbilityMana();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(automaton), 0,
                null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.assertInHand(player1, "Ornithopter");
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
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
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
        Permanent map = harness.addToBattlefieldAndReturn(player1, new RenegadeMap());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(automaton),
                0,
                null,
                map.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature you control");
    }

    @Test
    @DisplayName("A tapped, summoning-sick Automaton can activate repeatedly")
    void canActivateRepeatedlyWhileTapped() {
        Permanent automaton = harness.addToBattlefieldAndReturn(player1, new AegisAutomaton());
        automaton.tap();
        automaton.setSummoningSick(true);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent otherAutomaton = harness.addToBattlefieldAndReturn(player1, new AegisAutomaton());
        addAbilityMana();
        addAbilityMana();

        harness.activateAbility(player1, 0, null, first.getId());
        harness.activateAbility(player1, 0, null, otherAutomaton.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Ornithopter");
        harness.assertInHand(player1, "Aegis Automaton");
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(automaton);
    }

    @Test
    @DisplayName("A borrowed creature returns to its owner's hand")
    void returnsBorrowedCreatureToOwner() {
        harness.addToBattlefield(player1, new AegisAutomaton());
        Ornithopter card = new Ornithopter();
        card.setOwnerId(player2.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player1, card);
        gd.stolenCreatures.put(target.getId(), player2.getId());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.assertInHand(player2, "Ornithopter");
        harness.assertNotInHand(player1, "Ornithopter");
    }

    @Test
    @DisplayName("A target that changes controllers is illegal at resolution")
    void doesNotReturnTargetNowControlledByOpponent() {
        harness.addToBattlefield(player1, new AegisAutomaton());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        addAbilityMana();
        harness.activateAbility(player1, 0, null, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        gd.stolenCreatures.put(target.getId(), player1.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Ornithopter");
        harness.assertNotInHand(player1, "Ornithopter");
        harness.assertNotInHand(player2, "Ornithopter");
    }

    @Test
    @DisplayName("Five colorless mana cannot pay the white component")
    void requiresWhiteMana() {
        harness.addToBattlefield(player1, new AegisAutomaton());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Ornithopter");
    }

    @Test
    @DisplayName("The generic component requires four mana in addition to white")
    void requiresFullGenericCost() {
        harness.addToBattlefield(player1, new AegisAutomaton());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Ornithopter");
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
