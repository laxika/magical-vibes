package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HoodedKavu;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Skizzik.class, HoodedKavu.class})
class SkizzikTest extends BaseCardTest {

    @Test
    @DisplayName("Cast Skizzik can attack immediately because it has haste")
    void canAttackImmediatelyWithHaste() {
        harness.setHand(player1, List.of(new Skizzik()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        declareAttackers(List.of(0));

        assertThat(findPermanent(player1, "Skizzik").isTapped()).isTrue();
        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Skizzik's trample deals excess combat damage through a blocker")
    void trampleDealsExcessCombatDamage() {
        harness.setLife(player2, 20);
        Permanent blocker = addCreatureReady(player2, new HoodedKavu());
        harness.setHand(player1, List.of(new Skizzik()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 3
        ));

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(blocker.getId()));
    }

    // ===== Cast without kicker =====

    @Test
    @DisplayName("Cast without kicker — sacrificed at end step")
    void castWithoutKickerSacrificedAtEndStep() {
        harness.setHand(player1, List.of(new Skizzik()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        harness.assertOnBattlefield(player1, "Skizzik");

        // Advance to end step
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // End step trigger should be on the stack
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getCard().getName()).isEqualTo("Skizzik");

        // Resolve the sacrifice trigger
        harness.passBothPriorities();

        // Should be sacrificed
        harness.assertNotOnBattlefield(player1, "Skizzik");
        harness.assertInGraveyard(player1, "Skizzik");
    }

    // ===== Cast with kicker =====

    @Test
    @DisplayName("Cast with kicker — stays on battlefield at end step")
    void castWithKickerStaysOnBattlefield() {
        harness.setHand(player1, List.of(new Skizzik()));
        harness.addMana(player1, ManaColor.RED, 2); // 1 for base cost + 1 for kicker
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        assertThat(findPermanent(player1, "Skizzik").isKicked()).isTrue();

        // Advance through end step — no trigger should fire since it was kicked
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances past end step (no triggers)

        assertThat(gd.stack).isEmpty();

        // Should still be on battlefield (not sacrificed)
        harness.assertOnBattlefield(player1, "Skizzik");
        // Should not be in graveyard
        harness.assertNotInGraveyard(player1, "Skizzik");
    }
}
