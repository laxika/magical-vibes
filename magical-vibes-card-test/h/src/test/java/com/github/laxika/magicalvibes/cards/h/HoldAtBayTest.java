package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BoltOfKeranos;
import com.github.laxika.magicalvibes.cards.s.SwordwiseCentaur;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HoldAtBay.class, SwordwiseCentaur.class, BoltOfKeranos.class})
class HoldAtBayTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents the next 7 damage to a player")
    void preventsNextSevenDamageToPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new HoldAtBay()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        for (int i = 0; i < 3; i++) {
            Permanent attacker = harness.addToBattlefieldAndReturn(player1, new SwordwiseCentaur());
            attacker.setSummoningSick(false);
            attacker.setAttacking(true);
        }

        harness.forceActivePlayer(player1);
        harness.resolveCombatDamage();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Prevents the next 7 damage to a creature")
    void preventsNextSevenDamageToCreature() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new SwordwiseCentaur());
        harness.setHand(player1, List.of(new HoldAtBay()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, blocker.getId());
        harness.passBothPriorities();

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new SwordwiseCentaur());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.resolveCombatDamage();

        harness.assertOnBattlefield(player2, "Swordwise Centaur");
        assertThat(blocker.getMarkedDamage()).isZero();
        harness.assertNotOnBattlefield(player1, "Swordwise Centaur");
    }

    @Test
    void consumesShieldAcrossNoncombatDamageEvents() {
        harness.setLife(player2, 20);
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new HoldAtBay()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        castBolt(player2.getId());
        harness.assertLife(player2, 20);
        castBolt(player2.getId());
        harness.assertLife(player2, 20);
        castBolt(player2.getId());
        harness.assertLife(player2, 18);
        castBolt(player2.getId());
        harness.assertLife(player2, 15);
    }

    @Test
    void creatureShieldPreventsNoncombatDamageUntilConsumed() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SwordwiseCentaur());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new HoldAtBay()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        castBolt(creature.getId());
        assertThat(creature.getMarkedDamage()).isZero();
        castBolt(creature.getId());
        assertThat(creature.getMarkedDamage()).isZero();
        castBolt(creature.getId());
        harness.assertNotOnBattlefield(player2, "Swordwise Centaur");
        harness.assertInGraveyard(player2, "Swordwise Centaur");
    }

    @Test
    void doesNotPreventDamageToAnotherTarget() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new HoldAtBay()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        castBolt(player1.getId());
        harness.assertLife(player1, 17);
        castBolt(player2.getId());
        harness.assertLife(player2, 20);
    }

    @Test
    void unusedShieldExpiresAtEndOfTurn() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new HoldAtBay()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player2, List.of());
        harness.setHand(player2, List.of(new BoltOfKeranos()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
    }

    private void castBolt(UUID targetId) {
        harness.setHand(player1, List.of(new BoltOfKeranos()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
