package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FungusaurTest extends BaseCardTest {

    @Test
    @DisplayName("When Fungusaur takes non-lethal combat damage, it gets a +1/+1 counter and survives")
    void nonLethalCombatDamageAddsCounter() {
        harness.addToBattlefield(player2, new Fungusaur()); // 2/2 blocker
        harness.addToBattlefield(player1, new FugitiveWizard()); // 1/1 attacker

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent fungusaur = gd.playerBattlefields.get(player2.getId()).getFirst();
        fungusaur.setSummoningSick(false);
        fungusaur.setBlocking(true);
        fungusaur.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        // Resolve combat damage and the resulting trigger
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Fungusaur survives (2 toughness, 1 damage) and has a +1/+1 counter
        harness.assertOnBattlefield(player2, "Fungusaur");
        Permanent survivor = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fungusaur"))
                .findFirst().orElseThrow();
        assertThat(survivor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        // Fugitive Wizard dies to the 2/2 Fungusaur
        harness.assertInGraveyard(player1, "Fugitive Wizard");
    }

    @Test
    @DisplayName("Lethal damage destroys Fungusaur before the +1/+1 counter trigger can resolve")
    void lethalDamageKillsBeforeCounterResolves() {
        harness.addToBattlefield(player2, new Fungusaur()); // 2/2
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID fungusaurId = harness.getPermanentId(player2, "Fungusaur");
        harness.castInstant(player1, 0, fungusaurId);
        harness.passBothPriorities(); // Resolve Shock — 2 damage, lethal for a 2/2

        // The trigger is on the stack, but state-based actions already destroyed Fungusaur
        harness.assertInGraveyard(player2, "Fungusaur");

        // Resolving the trigger does nothing (source is gone)
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
        harness.assertInGraveyard(player2, "Fungusaur");
    }
}
