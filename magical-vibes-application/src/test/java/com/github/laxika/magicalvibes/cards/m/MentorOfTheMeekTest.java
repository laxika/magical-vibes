package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MentorOfTheMeekTest extends BaseCardTest {

    // ===== Triggers and pays to draw =====

    @Test
    @DisplayName("Paying {1} draws a card when creature with power 2 or less enters")
    void payingOneManaDrawsCard() {
        harness.addToBattlefield(player1, new MentorOfTheMeek());

        // Cast Grizzly Bears (2/2) — power 2 should trigger
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1); // for Mentor's {1} cost
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // Resolve Grizzly Bears
        harness.passBothPriorities(); // Resolve MayPayManaEffect from stack -> may prompt

        // Accept and pay {1} — draw a card
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    // ===== Triggers but player declines =====

    @Test
    @DisplayName("Declining does not draw a card")
    void decliningDoesNotDraw() {
        harness.addToBattlefield(player1, new MentorOfTheMeek());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // Resolve Grizzly Bears
        harness.passBothPriorities(); // Resolve MayPayManaEffect from stack -> may prompt

        // Decline
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    // ===== Does NOT trigger for power > 2 =====

    @Test
    @DisplayName("Does not trigger when creature with power greater than 2 enters")
    void doesNotTriggerForHighPowerCreature() {
        harness.addToBattlefield(player1, new MentorOfTheMeek());

        // Cast Hill Giant (3/3) — power 3 should NOT trigger
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // Resolve Hill Giant

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    // ===== Does not trigger for itself =====

    @Test
    @DisplayName("Does not trigger for itself entering the battlefield")
    void doesNotTriggerForItself() {
        // Cast Mentor of the Meek (2/2) — should NOT trigger itself
        harness.setHand(player1, List.of(new MentorOfTheMeek()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // Resolve Mentor

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    // ===== Does not trigger for opponent's creatures =====

    @Test
    @DisplayName("Does not trigger when opponent's creature with power 2 or less enters")
    void doesNotTriggerForOpponentCreature() {
        harness.addToBattlefield(player1, new MentorOfTheMeek());
        harness.setHand(player1, List.of());

        // Opponent's Grizzly Bears (2/2) enters — should NOT trigger
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    // ===== Triggers for power 1 creature =====

    @Test
    @DisplayName("Triggers for creature with power less than 2")
    void triggersForPowerOnCreature() {
        harness.addToBattlefield(player1, new MentorOfTheMeek());

        // Cast Elite Vanguard (2/1) — power 2 should trigger
        harness.setHand(player1, List.of(new EliteVanguard()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1); // for Mentor's {1} cost
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // Resolve Elite Vanguard
        harness.passBothPriorities(); // Resolve MayPayManaEffect from stack -> may prompt

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
