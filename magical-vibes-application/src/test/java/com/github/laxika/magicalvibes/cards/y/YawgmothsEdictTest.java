package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.w.Watchwolf;
import com.github.laxika.magicalvibes.cards.w.Worship;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YawgmothsEdict.class, Worship.class, LlanowarElves.class, Watchwolf.class})
class YawgmothsEdictTest extends BaseCardTest {

    /** Player1 controls Yawgmoth's Edict; it is player2's (the opponent's) turn. */
    private void setUpOpponentTurn() {
        harness.addToBattlefield(player1, new YawgmothsEdict());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Opponent's white spell: that player loses 1 life and you gain 1 life")
    void opponentWhiteSpellDrains() {
        setUpOpponentTurn();
        harness.setHand(player2, List.of(new Worship()));
        harness.addMana(player2, ManaColor.WHITE, 4);

        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());
        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castEnchantment(player2, 0);

        // Drain trigger sits on top of the enchantment spell.
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities(); // resolve the drain trigger

        harness.assertLife(player2, opponentLifeBefore - 1);
        harness.assertLife(player1, controllerLifeBefore + 1);
    }

    @Test
    @DisplayName("Opponent's multicolored white spell triggers")
    void opponentMulticoloredWhiteSpellDrains() {
        setUpOpponentTurn();
        harness.setHand(player2, List.of(new Watchwolf()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);

        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());
        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        harness.assertLife(player2, opponentLifeBefore - 1);
        harness.assertLife(player1, controllerLifeBefore + 1);
    }

    @Test
    @DisplayName("Opponent's non-white spell does not trigger")
    void opponentNonWhiteSpellDoesNotTrigger() {
        setUpOpponentTurn();
        harness.setHand(player2, List.of(new LlanowarElves()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());
        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castCreature(player2, 0);

        // Only the creature spell is on the stack — no triggered ability.
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);

        harness.passBothPriorities();

        harness.assertLife(player2, opponentLifeBefore);
        harness.assertLife(player1, controllerLifeBefore);
    }

    @Test
    @DisplayName("Controller's own white spell does not trigger (only opponents' casts count)")
    void ownWhiteSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new YawgmothsEdict());
        harness.setHand(player1, List.of(new Worship()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castEnchantment(player1, 0);

        // Only the enchantment spell — no triggered ability.
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);

        harness.passBothPriorities();

        harness.assertLife(player1, controllerLifeBefore);
        harness.assertLife(player2, opponentLifeBefore);
    }
}
