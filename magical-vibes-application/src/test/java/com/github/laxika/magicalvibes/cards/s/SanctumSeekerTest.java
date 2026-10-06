package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.n.NestRobber;
import com.github.laxika.magicalvibes.cards.q.QueensBaySoldier;
import com.github.laxika.magicalvibes.cards.v.VanquishTheWeak;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SanctumSeeker.class, QueensBaySoldier.class, NestRobber.class, VanquishTheWeak.class})
class SanctumSeekerTest extends BaseCardTest {

    @Test
    @DisplayName("Puts triggered ability on stack when a Vampire attacks")
    void triggersWhenVampireAttacks() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addCreatureReady(player1, new SanctumSeeker());
        addCreatureReady(player1, new QueensBaySoldier());

        declareAttackers(List.of(1)); // Vampire creature attacks (2/2)

        // Trigger should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Sanctum Seeker");

        harness.passBothPriorities(); // resolve trigger + combat auto-advances

        // Opponent: 20 - 1 (trigger) - 2 (combat damage from 2/2) = 17
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        // Controller: 20 + 1 (trigger) = 21
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Triggers for each attacking Vampire separately")
    void triggersPerVampire() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addCreatureReady(player1, new SanctumSeeker());
        addCreatureReady(player1, new QueensBaySoldier());
        addCreatureReady(player1, new QueensBaySoldier());

        declareAttackers(List.of(1, 2)); // both Vampires attack (each 2/2)

        // Two separate triggers on the stack (one per attacking Vampire)
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities(); // resolve first trigger
        harness.passBothPriorities(); // resolve second trigger + combat auto-advances

        // Opponent: 20 - 2 (2 triggers) - 4 (combat damage from two 2/2s) = 14
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        // Controller: 20 + 2 (2 triggers) = 22
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Sanctum Seeker itself is a Vampire — triggers when it attacks")
    void triggersWhenSanctumSeekerAttacks() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addCreatureReady(player1, new SanctumSeeker());

        declareAttackers(List.of(0)); // Sanctum Seeker attacks (3/4)

        // Trigger should be on stack
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities(); // resolve trigger + combat

        // Opponent: 20 - 1 (trigger) - 3 (combat damage from 3/4) = 16
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        // Controller: 20 + 1 (trigger) = 21
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Does not trigger when a non-Vampire creature attacks")
    void doesNotTriggerForNonVampire() {
        addCreatureReady(player1, new SanctumSeeker());
        addCreatureReady(player1, new NestRobber());

        declareAttackers(List.of(1)); // non-Vampire attacks

        // No trigger on the stack
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Triggers only for attacking Vampires in a mixed group")
    void triggersOnlyForVampiresInMixedGroup() {
        addCreatureReady(player1, new SanctumSeeker());
        addCreatureReady(player1, new QueensBaySoldier());
        addCreatureReady(player1, new NestRobber());

        declareAttackers(List.of(1, 2)); // Vampire + non-Vampire attack

        // Only 1 trigger (from the Vampire), not 2
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Sanctum Seeker");
    }

    @Test
    @DisplayName("Does not trigger for opponent's attacking Vampires")
    void doesNotTriggerForOpponentVampires() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addCreatureReady(player1, new SanctumSeeker());
        addCreatureReady(player2, new QueensBaySoldier());

        // Opponent declares attackers
        declareAttackers(player2, List.of(0));

        // Sanctum Seeker belongs to player1 — opponent's Vampires shouldn't trigger it
        // Stack should have no Sanctum Seeker triggers
        assertThat(gd.stack.stream()
                .filter(se -> se.getCard().getName().equals("Sanctum Seeker"))
                .count()).isZero();
    }

    @Test
    @DisplayName("Each Sanctum Seeker triggers for every attacking Vampire")
    void multipleSeekersTriggerIndependently() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new SanctumSeeker());
        addCreatureReady(player1, new SanctumSeeker());

        declareAttackers(List.of(0, 1));

        assertThat(gd.stack).hasSize(4);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("Removing Sanctum Seeker does not stop its pending trigger")
    void triggerResolvesAfterSourceDies() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent seeker = addCreatureReady(player1, new SanctumSeeker());
        addCreatureReady(player1, new QueensBaySoldier());
        harness.setHand(player2, List.of(new VanquishTheWeak()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        declareAttackers(List.of(1));
        harness.castInstant(player2, 0, seeker.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(seeker.getCard());
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Removing the attacking Vampire does not stop the pending trigger")
    void triggerResolvesAfterAttackerDies() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new SanctumSeeker());
        Permanent attacker = addCreatureReady(player1, new QueensBaySoldier());
        harness.setHand(player2, List.of(new VanquishTheWeak()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        declareAttackers(List.of(1));
        harness.castInstant(player2, 0, attacker.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(attacker.getCard());
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }
}
