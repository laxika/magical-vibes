package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.r.RabidBite;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

@CardUsed({DinosaurHunter.class, AirElemental.class, ColossalDreadmaw.class, RabidBite.class})
class DinosaurHunterTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a Dinosaur it deals damage to")
    void destroysDamagedDinosaur() {
        harness.addToBattlefield(player1, new DinosaurHunter());
        harness.addToBattlefield(player2, new ColossalDreadmaw());
        harness.setHand(player1, List.of(new RabidBite()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, List.of(
                harness.getPermanentId(player1, "Dinosaur Hunter"),
                harness.getPermanentId(player2, "Colossal Dreadmaw")));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Dinosaur Hunter");
        harness.assertInGraveyard(player2, "Colossal Dreadmaw");
    }

    @Test
    @DisplayName("Does not destroy a non-Dinosaur it deals damage to")
    void doesNotDestroyNonDinosaur() {
        harness.addToBattlefield(player1, new DinosaurHunter());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new RabidBite()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, List.of(
                harness.getPermanentId(player1, "Dinosaur Hunter"),
                harness.getPermanentId(player2, "Air Elemental")));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Dinosaur Hunter");
        harness.assertOnBattlefield(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Destroys a Dinosaur blocker even when the Hunter dies in combat")
    void destroysDinosaurBlockerAfterDying() {
        Permanent hunter = addCreatureReady(player1, new DinosaurHunter());
        Permanent dinosaur = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        hunter.setAttacking(true);
        dinosaur.setBlocking(true);
        dinosaur.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Dinosaur Hunter");
        harness.assertInGraveyard(player2, "Colossal Dreadmaw");
    }

    @Test
    @DisplayName("Destroys a Dinosaur attacker when blocking, even though the Hunter dies")
    void destroysDinosaurAttackerAfterDying() {
        Permanent dinosaur = addCreatureReady(player1, new ColossalDreadmaw());
        Permanent hunter = harness.addToBattlefieldAndReturn(player2, new DinosaurHunter());
        dinosaur.setAttacking(true);
        hunter.setBlocking(true);
        hunter.addBlockingTarget(0);

        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(hunter.getId(), 2, player2.getId(), 4));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Colossal Dreadmaw");
        harness.assertInGraveyard(player2, "Dinosaur Hunter");
    }

    @Test
    @DisplayName("Does not destroy a Dinosaur damaged by another creature its controller controls")
    void doesNotTriggerForAnotherCreature() {
        harness.addToBattlefield(player1, new DinosaurHunter());
        harness.addToBattlefield(player1, new AirElemental());
        harness.addToBattlefield(player2, new ColossalDreadmaw());
        harness.setHand(player1, List.of(new RabidBite()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, List.of(
                harness.getPermanentId(player1, "Air Elemental"),
                harness.getPermanentId(player2, "Colossal Dreadmaw")));
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Colossal Dreadmaw");
    }
}
