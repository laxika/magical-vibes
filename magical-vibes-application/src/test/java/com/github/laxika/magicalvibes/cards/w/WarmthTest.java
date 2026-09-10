package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GoblinBombardment;
import com.github.laxika.magicalvibes.cards.m.MoggConscripts;
import com.github.laxika.magicalvibes.cards.s.SoltariFootSoldier;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Warmth.class, MoggConscripts.class, SoltariFootSoldier.class, GoblinBombardment.class})
class WarmthTest extends BaseCardTest {

    /** Player1 controls Warmth; it is player2's (the opponent's) turn. */
    private void setUpOpponentTurn() {
        harness.addToBattlefield(player1, new Warmth());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Opponent's red spell: you gain 2 life")
    void opponentRedSpellGainsLife() {
        setUpOpponentTurn();
        harness.castFromHand(player2, new MoggConscripts(), "{R}");

        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());

        // Gain-life trigger sits on top of the creature spell.
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Warmth");

        harness.passBothPriorities(); // resolve the gain-life trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLifeBefore + 2);
    }

    @Test
    @DisplayName("Opponent's non-red spell does not trigger")
    void opponentNonRedSpellDoesNotTrigger() {
        setUpOpponentTurn();
        harness.castFromHand(player2, new SoltariFootSoldier(), "{W}");

        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());

        // Only the creature spell is on the stack — no triggered ability.
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLifeBefore);
    }

    @Test
    @DisplayName("Opponent's red noncreature spell: you gain 2 life")
    void opponentRedNoncreatureSpellGainsLife() {
        setUpOpponentTurn();
        harness.castFromHand(player2, new GoblinBombardment(), "{1}{R}");

        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());

        // Gain-life trigger sits on top of the enchantment spell.
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Warmth");

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLifeBefore + 2);
    }

    @Test
    @DisplayName("Controller's own red spell does not trigger (only opponents' casts count)")
    void ownRedSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new Warmth());
        harness.castFromHand(player1, new MoggConscripts(), "{R}");

        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());

        // Only the creature spell — no triggered ability.
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLifeBefore);
    }
}
