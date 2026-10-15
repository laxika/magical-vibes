package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.Banefire;
import com.github.laxika.magicalvibes.cards.c.CanyonMinotaur;
import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SafePassage.class, CanyonMinotaur.class, CrawWurm.class, RuneclawBear.class,
        LightningBolt.class, GarrukWildspeaker.class, SignInBlood.class, Banefire.class})
class SafePassageTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Safe Passage puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new SafePassage()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Safe Passage");
    }

    @Test
    @DisplayName("Cannot cast Safe Passage without enough mana")
    void cannotCastWithoutMana() {
        harness.setHand(player1, List.of(new SafePassage()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Resolving Safe Passage adds controller to playersWithAllDamagePrevented")
    void resolvingAddsControllerToPrevented() {
        harness.setHand(player1, List.of(new SafePassage()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.playersWithAllDamagePrevented).contains(player1.getId());
    }

    @Test
    @DisplayName("Safe Passage goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new SafePassage()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        harness.assertInGraveyard(player1, "Safe Passage");
    }

    @Test
    @DisplayName("Prevents combat damage to controller from attacking creature")
    void preventsCombatDamageToPlayer() {
        harness.setLife(player2, 20);
        harness.getGameData().playersWithAllDamagePrevented.add(player2.getId());

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new CanyonMinotaur());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Prevents combat damage to controller's blocking creature")
    void preventsCombatDamageToBlockingCreature() {
        harness.getGameData().playersWithAllDamagePrevented.add(player2.getId());

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new CrawWurm());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // 6 damage to blocker is prevented, so Runeclaw Bear (2/2) survives
        harness.assertOnBattlefield(player2, "Runeclaw Bear");
    }

    @Test
    @DisplayName("Does not prevent damage to opponent player")
    void doesNotPreventDamageToOpponent() {
        harness.setLife(player1, 20);
        // Only player2 has Safe Passage protection
        harness.getGameData().playersWithAllDamagePrevented.add(player2.getId());

        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new CanyonMinotaur());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Does not prevent damage to opponent's creatures")
    void doesNotPreventDamageToOpponentsCreatures() {
        // Only player1 has Safe Passage protection
        harness.getGameData().playersWithAllDamagePrevented.add(player1.getId());

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new CrawWurm());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Runeclaw Bear (2/2) takes 6 damage — not protected, should die
        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
    }

    @Test
    @DisplayName("Prevents spell damage to controller")
    void preventsSpellDamageToPlayer() {
        harness.setLife(player2, 20);
        harness.getGameData().playersWithAllDamagePrevented.add(player2.getId());

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Prevents spell damage to controller's creature")
    void preventsSpellDamageToCreature() {
        harness.getGameData().playersWithAllDamagePrevented.add(player2.getId());

        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        creature.setSummoningSick(false);

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        // Runeclaw Bear (2/2) takes 3 damage from Lightning Bolt, but it's prevented
        harness.assertOnBattlefield(player2, "Runeclaw Bear");
    }

    @Test
    @DisplayName("Prevention is cleared at end of turn")
    void preventionClearedAtEndOfTurn() {
        harness.getGameData().playersWithAllDamagePrevented.add(player1.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playersWithAllDamagePrevented).isEmpty();
    }

    @Test
    void protectsCreaturesEnteringAfterResolutionAndRepeatedDamage() {
        resolveSafePassage();
        Permanent creature = harness.enterBattlefieldAndReturn(player1, new RuneclawBear());
        harness.setHand(player2, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertOnBattlefield(player1, "Runeclaw Bear");
        assertThat(creature.getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);
    }

    @Test
    void doesNotPreventSpellDamageToOpponentsCreatures() {
        resolveSafePassage();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertInGraveyard(player2, "Runeclaw Bear");
    }

    @Test
    void doesNotProtectPlaneswalkers() {
        Permanent garruk = harness.enterBattlefieldAndReturn(player1, new GarrukWildspeaker());
        resolveSafePassage();
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, garruk.getId());

        harness.assertInGraveyard(player1, "Garruk Wildspeaker");
    }

    @Test
    void doesNotPreventLifeLoss() {
        resolveSafePassage();
        harness.setLibrary(player1, List.of(new RuneclawBear(), new RuneclawBear()));
        harness.setHand(player1, List.of(new SignInBlood()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        harness.assertLife(player1, 18);
    }

    @Test
    void doesNotPreventUnpreventableDamageToPlayer() {
        resolveSafePassage();
        harness.setHand(player1, List.of(new Banefire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAndResolveSorcery(player1, 0, 5, player1.getId());

        harness.assertLife(player1, 15);
    }

    @Test
    void doesNotPreventUnpreventableDamageToCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        resolveSafePassage();
        harness.setHand(player1, List.of(new Banefire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAndResolveSorcery(player1, 0, 5, creature.getId());

        harness.assertInGraveyard(player1, "Runeclaw Bear");
    }

    @Test
    void protectionExpiresBeforeNextTurnDamage() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        resolveSafePassage();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.castAndResolveInstant(player2, 0, creature.getId());

        harness.assertLife(player1, 17);
        harness.assertInGraveyard(player1, "Runeclaw Bear");
    }

    @Test
    void preventsCombatDamageWithoutPreventingProtectedCreaturesDamage() {
        Permanent attacker = addCreatureReady(player2, new CrawWurm());
        Permanent blocker = addCreatureReady(player1, new RuneclawBear());
        resolveSafePassage();
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();
        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Runeclaw Bear");
        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void canRespondToDamageSpellWithSafePassage() {
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.setHand(player1, List.of(new SafePassage()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Safe Passage");
        harness.assertInGraveyard(player2, "Lightning Bolt");
    }

    private void resolveSafePassage() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new SafePassage()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0);
    }
}
