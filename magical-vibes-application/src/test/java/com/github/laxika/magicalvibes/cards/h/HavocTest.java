package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AdvanceScout;
import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.m.MoggConscripts;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Havoc.class, AdvanceScout.class, MoggConscripts.class, Counterspell.class})
class HavocTest extends BaseCardTest {

    /** Player1 controls Havoc; it is player2's (the opponent's) turn. */
    private void setUpOpponentTurn() {
        harness.addToBattlefield(player1, new Havoc());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Opponent's white spell: that player loses 2 life, controller gains none")
    void opponentWhiteSpellCostsTwoLife() {
        setUpOpponentTurn();
        harness.castFromHand(player2, new AdvanceScout(), "{1}{W}");

        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());
        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities(); // resolve the life-loss trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLifeBefore);
    }

    @Test
    @DisplayName("Opponent's non-white spell does not trigger")
    void opponentNonWhiteSpellDoesNotTrigger() {
        setUpOpponentTurn();
        harness.castFromHand(player2, new MoggConscripts(), "{R}");

        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    @Test
    @DisplayName("Controller's own white spell does not trigger")
    void ownWhiteSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new Havoc());
        harness.castFromHand(player1, new AdvanceScout(), "{1}{W}");

        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLifeBefore);
    }

    @Test
    @DisplayName("White spell still causes life loss if that spell is countered")
    void whiteSpellStillCausesLifeLossWhenCountered() {
        setUpOpponentTurn();
        AdvanceScout whiteSpell = new AdvanceScout();
        harness.castFromHand(player2, whiteSpell, "{1}{W}");
        harness.passPriority(player2);

        Counterspell counterspell = new Counterspell();
        harness.setHand(player1, List.of(counterspell));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, whiteSpell.getId());

        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 2);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard() == whiteSpell);
    }
}
