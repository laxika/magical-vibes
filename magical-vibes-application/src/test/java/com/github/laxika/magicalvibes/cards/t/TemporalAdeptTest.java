package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BraidwoodCup;
import com.github.laxika.magicalvibes.cards.g.GoliathBeetle;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TemporalAdept.class, GoliathBeetle.class, BraidwoodCup.class})
class TemporalAdeptTest extends BaseCardTest {

    // ===== Activating ability =====

    @Test
    @DisplayName("Activating ability puts it on the stack targeting a permanent")
    void activatingPutsOnStack() {
        addCreatureReady(player1, new TemporalAdept());
        Permanent target = addCreatureReady(player2, new GoliathBeetle());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, target.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Activating ability taps Temporal Adept and consumes {U}{U}{U}")
    void activatingTapsAndConsumesMana() {
        Permanent adept = addCreatureReady(player1, new TemporalAdept());
        Permanent target = addCreatureReady(player2, new GoliathBeetle());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(adept.isTapped()).isTrue();
        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Resolving returns opponent's creature to owner's hand")
    void resolvingReturnsOpponentCreature() {
        addCreatureReady(player1, new TemporalAdept());
        Permanent target = addCreatureReady(player2, new GoliathBeetle());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Goliath Beetle");
        harness.assertInHand(player2, "Goliath Beetle");
        harness.assertNotInGraveyard(player2, "Goliath Beetle");
    }

    @Test
    @DisplayName("Resolving returns an opponent's noncreature permanent to its owner's hand")
    void resolvingReturnsOpponentNoncreaturePermanent() {
        addCreatureReady(player1, new TemporalAdept());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BraidwoodCup());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Braidwood Cup");
        harness.assertInHand(player2, "Braidwood Cup");
    }

    @Test
    @DisplayName("Can bounce itself")
    void canBounceItself() {
        Permanent adept = addCreatureReady(player1, new TemporalAdept());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, adept.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Temporal Adept");
        harness.assertInHand(player1, "Temporal Adept");
    }

    // ===== Cannot activate =====

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new TemporalAdept());
        Permanent target = addCreatureReady(player2, new GoliathBeetle());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate ability when already tapped")
    void cannotActivateWhenTapped() {
        Permanent adept = addCreatureReady(player1, new TemporalAdept());
        adept.tap();
        Permanent target = addCreatureReady(player2, new GoliathBeetle());
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Ability fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        addCreatureReady(player1, new TemporalAdept());
        Permanent target = addCreatureReady(player2, new GoliathBeetle());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, target.getId());

        harness.getGameData().playerBattlefields.get(player2.getId())
                .removeIf(p -> p.getCard().getName().equals("Goliath Beetle"));

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

}
