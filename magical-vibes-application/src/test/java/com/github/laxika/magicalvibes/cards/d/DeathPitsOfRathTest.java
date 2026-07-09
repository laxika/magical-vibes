package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DeathPitsOfRathTest extends BaseCardTest {

    private boolean onBattlefield(com.github.laxika.magicalvibes.model.Player owner, String name) {
        return gd.playerBattlefields.get(owner.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals(name));
    }

    @Test
    @DisplayName("A creature dealt non-lethal noncombat damage is destroyed")
    void noncombatDamageDestroysDamagedCreature() {
        harness.addToBattlefield(player1, new DeathPitsOfRath());
        harness.addToBattlefield(player2, new HillGiant()); // 3/3 survives 2 damage normally
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.castInstant(player1, 0, giantId);
        harness.passBothPriorities(); // Resolve Shock — 2 damage to the 3/3

        // Death Pits trigger should now be on the stack
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities(); // Resolve the trigger

        assertThat(onBattlefield(player2, "Hill Giant")).isFalse();
    }

    @Test
    @DisplayName("Death Pits destroys any creature — including its controller's own")
    void destroysControllersOwnCreature() {
        harness.addToBattlefield(player1, new DeathPitsOfRath());
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID giantId = harness.getPermanentId(player1, "Hill Giant");
        harness.castInstant(player1, 0, giantId);
        harness.passBothPriorities(); // Resolve Shock

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(onBattlefield(player1, "Hill Giant")).isFalse();
    }

    @Test
    @DisplayName("A creature dealt non-lethal combat damage is destroyed by Death Pits")
    void combatDamageDestroysSurvivingBlocker() {
        harness.addToBattlefield(player1, new DeathPitsOfRath());
        harness.addToBattlefield(player1, new GrizzlyBears()); // 2/2 attacker
        harness.addToBattlefield(player2, new HillGiant());    // 3/3 blocker, survives combat

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Hill Giant"))
                .findFirst().orElseThrow();
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(1); // Grizzly Bears is index 1 on player1's battlefield

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        // Resolve combat damage and the Death Pits trigger it queues.
        harness.passBothPriorities();
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        // Hill Giant survived combat (3 toughness, took only 2) but Death Pits destroys it anyway.
        assertThat(onBattlefield(player2, "Hill Giant")).isFalse();
    }
}
