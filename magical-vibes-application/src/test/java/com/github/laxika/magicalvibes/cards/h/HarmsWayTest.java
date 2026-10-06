package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.s.StampedingRhino;
import com.github.laxika.magicalvibes.cards.c.CanyonMinotaur;
import com.github.laxika.magicalvibes.cards.c.ChildOfNight;
import com.github.laxika.magicalvibes.cards.d.DeadlyRecluse;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.l.LeylineOfPunishment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HarmsWay.class, RuneclawBear.class, StampedingRhino.class, CanyonMinotaur.class,
        ChildOfNight.class, DeadlyRecluse.class, LightningBolt.class, ProdigalPyromancer.class,
        LeylineOfPunishment.class})
class HarmsWayTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Harm's Way targeting a player puts it on the stack")
    void castTargetingPlayerPutsOnStack() {
        harness.setHand(player1, List.of(new HarmsWay()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Casting Harm's Way targeting a creature puts it on the stack")
    void castTargetingCreaturePutsOnStack() {
        Permanent bear = addCreatureReady(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new HarmsWay()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, bear.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(bear.getId());
    }

    @Test
    @DisplayName("Resolving Harm's Way prompts for source choice")
    void resolvingPromptsForSourceChoice() {
        addCreatureReady(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new HarmsWay()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class) != null).isTrue();
    }

    @Test
    @DisplayName("Choosing a source creates a source damage redirect shield")
    void choosingSourceCreatesShield() {
        Permanent opponentCreature = addCreatureReady(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new HarmsWay()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, opponentCreature.getId());

        assertThat(gd.sourceDamageRedirectShields).hasSize(1);
        assertThat(gd.sourceDamageRedirectShields.getFirst().protectedPlayerId()).isEqualTo(player1.getId());
        assertThat(gd.sourceDamageRedirectShields.getFirst().damageSourceId()).isEqualTo(opponentCreature.getId());
        assertThat(gd.sourceDamageRedirectShields.getFirst().remainingAmount()).isEqualTo(2);
        assertThat(gd.sourceDamageRedirectShields.getFirst().redirectTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Redirect shield prevents 2 combat damage and redirects to target player")
    void redirectsCombatDamageToPlayer() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent opponentCreature = addCreatureReady(player2, new RuneclawBear());

        // Cast Harm's Way targeting player2, choose opponent's creature as source
        harness.setHand(player1, List.of(new HarmsWay()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, opponentCreature.getId());

        // Set up combat: opponent's Runeclaw Bear (2/2) attacks player1
        harness.forceActivePlayer(player2);
        opponentCreature.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Player1 takes 0 damage (2 prevented by redirect shield)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        // Player2 takes 2 damage (redirected)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Redirect shield partially consumed when source deals more than 2 damage")
    void partialRedirectWhenSourceDealsMoreThan2() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // Use a creature with higher power — add a 4/4
        Permanent bigCreature = addCreatureReady(player2, new StampedingRhino());

        // Cast Harm's Way targeting player2, choose big creature as source
        harness.setHand(player1, List.of(new HarmsWay()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, bigCreature.getId());

        // Combat: big creature attacks player1
        harness.forceActivePlayer(player2);
        bigCreature.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Player1 takes 2 damage (4 - 2 prevented = 2)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        // Player2 takes 2 damage (redirected)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Redirect shield can redirect damage to a creature target")
    void redirectsCombatDamageToCreature() {
        harness.setLife(player1, 20);
        Permanent opponentCreature = addCreatureReady(player2, new RuneclawBear());
        Permanent targetCreature = addCreatureReady(player2, new CanyonMinotaur());

        // Cast Harm's Way targeting opponent's 3/3 creature, choose the 2/2 as source
        harness.setHand(player1, List.of(new HarmsWay()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, targetCreature.getId());
        harness.handlePermanentChosen(player1, opponentCreature.getId());

        // Combat: 2/2 creature attacks player1
        harness.forceActivePlayer(player2);
        opponentCreature.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Player1 takes 0 damage (2/2 creature, all 2 prevented)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        // The 3/3 creature should have received 2 damage (marked but not lethal)
        assertThat(targetCreature.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Redirect shield does not affect damage from non-matching source")
    void doesNotAffectNonMatchingSource() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent creature1 = addCreatureReady(player2, new RuneclawBear());
        Permanent creature2 = addCreatureReady(player2, new RuneclawBear());

        // Cast Harm's Way targeting player2, choose creature1 as source
        harness.setHand(player1, List.of(new HarmsWay()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, creature1.getId());

        // Combat: creature2 (not the chosen source) attacks player1
        harness.forceActivePlayer(player2);
        creature2.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Player1 takes full damage from creature2 (not the chosen source)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        // Player2 takes no redirected damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        // Shield should still be active (not consumed)
        assertThat(gd.sourceDamageRedirectShields).hasSize(1);
    }

    @Test
    @DisplayName("Redirect shield still works when damage can't be prevented (Leyline of Punishment)")
    void redirectWorksWhenDamageCantBePrevented() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent opponentCreature = addCreatureReady(player2, new RuneclawBear());

        // Leyline of Punishment on battlefield — damage can't be prevented
        harness.addToBattlefield(player2, new LeylineOfPunishment());

        // Cast Harm's Way targeting player2, choose opponent's creature as source
        harness.setHand(player1, List.of(new HarmsWay()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, opponentCreature.getId());

        // Combat: opponent's Runeclaw Bear (2/2) attacks player1
        harness.forceActivePlayer(player2);
        opponentCreature.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Harm's Way is redirection (replacement), not prevention — it still works
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Source redirect shield is cleared at end of turn")
    void shieldClearedAtEndOfTurn() {
        Permanent opponentCreature = addCreatureReady(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new HarmsWay()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, opponentCreature.getId());

        assertThat(gd.sourceDamageRedirectShields).hasSize(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.sourceDamageRedirectShields).isEmpty();
    }

    @Test
    @DisplayName("Answering the source choice resumes the parked resolution entry")
    void answeringSourceChoiceClearsParkedResolution() {
        Permanent opponentCreature = addCreatureReady(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new HarmsWay()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(gd.pendingEffectResolutionEntry).isNotNull();

        harness.handlePermanentChosen(player1, opponentCreature.getId());

        assertThat(gd.pendingEffectResolutionEntry).isNull();
        assertThat(gd.deferPlayerLossCheck).isFalse();
    }

    @Test
    @DisplayName("A spell on the stack can be chosen as the damage source")
    void redirectsDamageFromSpellOnStack() {
        LightningBolt bolt = new LightningBolt();
        harness.setHand(player2, List.of(bolt));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.setHand(player1, List.of(new HarmsWay()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.handlePermanentChosen(player1, bolt.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Spell damage to a controlled creature can be redirected")
    void redirectsSpellDamageToControlledCreature() {
        Permanent protectedCreature = addCreatureReady(player1, new RuneclawBear());
        LightningBolt bolt = new LightningBolt();
        harness.setHand(player2, List.of(bolt));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, protectedCreature.getId());
        harness.setHand(player1, List.of(new HarmsWay()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.handlePermanentChosen(player1, bolt.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(protectedCreature);
        assertThat(protectedCreature.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("A departed source referenced by an ability on the stack remains choosable")
    void canChooseDepartedSourceOfStackAbility() {
        Permanent source = addCreatureReady(player2, new ProdigalPyromancer());
        harness.setHand(player1, List.of(new LightningBolt(), new HarmsWay()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.castAndResolveInstant(player1, 0, source.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(source);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.handlePermanentChosen(player1, source.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Damage is not redirected when the destination has left the battlefield")
    void destinationLeavingBattlefieldDoesNotPreventDamage() {
        Permanent source = addCreatureReady(player2, new RuneclawBear());
        Permanent destination = addCreatureReady(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new HarmsWay(), new LightningBolt()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, destination.getId());
        harness.handlePermanentChosen(player1, source.getId());
        harness.castAndResolveInstant(player1, 0, destination.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(destination);

        harness.forceActivePlayer(player2);
        source.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Redirected damage retains its source's deathtouch")
    void redirectedDamageRetainsDeathtouch() {
        Permanent source = addCreatureReady(player2, new DeadlyRecluse());
        Permanent destination = addCreatureReady(player2, new CanyonMinotaur());
        harness.setHand(player1, List.of(new HarmsWay()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, destination.getId());
        harness.handlePermanentChosen(player1, source.getId());

        harness.forceActivePlayer(player2);
        source.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(destination);
        harness.assertInGraveyard(player2, "Canyon Minotaur");
    }

    @Test
    @DisplayName("Redirected combat damage still causes lifelink life gain")
    void redirectedCombatDamageRetainsLifelink() {
        Permanent source = addCreatureReady(player2, new ChildOfNight());
        Permanent destination = addCreatureReady(player1, new CanyonMinotaur());
        harness.setLife(player2, 10);
        harness.setHand(player1, List.of(new HarmsWay()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, destination.getId());
        harness.handlePermanentChosen(player1, source.getId());

        harness.forceActivePlayer(player2);
        source.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(destination.getMarkedDamage()).isEqualTo(2);
        harness.assertLife(player2, 12);
    }

    @Test
    @DisplayName("The two-damage shield protects controlled permanents across separate events")
    void protectsPermanentAcrossSeparateDamageEvents() {
        Permanent source = addCreatureReady(player2, new ProdigalPyromancer());
        Permanent protectedCreature = addCreatureReady(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new HarmsWay()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, source.getId());

        harness.activateAbility(player2, 0, null, protectedCreature.getId());
        harness.passBothPriorities();
        assertThat(protectedCreature.getMarkedDamage()).isZero();
        harness.assertLife(player2, 19);

        source.untap();
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);

        source.untap();
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 18);
    }
}
