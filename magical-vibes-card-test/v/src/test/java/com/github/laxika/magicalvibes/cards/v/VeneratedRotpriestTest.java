package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.e.ElaborateFirecannon;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.cards.t.TyvarsStand;
import com.github.laxika.magicalvibes.cards.u.UnctussRetrofitter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VeneratedRotpriest.class, GiantGrowth.class, GrizzlyBears.class, Shock.class, ElaborateFirecannon.class})
class VeneratedRotpriestTest extends BaseCardTest {

    @Test
    @DisplayName("Poisons an opponent when your creature is targeted by a spell")
    void poisonsOpponentWhenOwnCreatureIsTargetedBySpell() {
        harness.addToBattlefield(player1, new VeneratedRotpriest());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, creature.getId());
        harness.handlePermanentChosen(player1, player2.getId());

        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Triggers when an opponent's spell targets your creature")
    void triggersWhenOpponentsSpellTargetsOwnCreature() {
        harness.addToBattlefield(player1, new VeneratedRotpriest());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castInstant(player2, 0, creature.getId());
        harness.handlePermanentChosen(player1, player2.getId());

        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when an opponent's creature is targeted")
    void doesNotTriggerForOpponentsCreature() {
        harness.addToBattlefield(player1, new VeneratedRotpriest());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerPoisonCounters).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger for an activated ability targeting your creature")
    void doesNotTriggerForActivatedAbility() {
        harness.addToBattlefield(player1, new VeneratedRotpriest());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        addCreatureReady(player2, new ElaborateFirecannon());

        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.activateAbility(player2, 0, null, creature.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerPoisonCounters).isEmpty();
    }

    @Test
    @DisplayName("Toxic 1 poisons the opponent during combat damage without using the stack")
    void toxicPoisonsOpponentDuringCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new VeneratedRotpriest());
        attacker.setAttacking(true);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        harness.assertLife(player2, 19);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({TyvarsStand.class})
    @DisplayName("Targeting Rotpriest itself triggers once, and boosted combat damage still gives only one toxic counter")
    void selfTargetingAndBoostedCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new VeneratedRotpriest());
        harness.setHand(player1, List.of(new TyvarsStand()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castInstant(player1, 0, 3, attacker.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        attacker.setAttacking(true);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        harness.assertLife(player2, 16);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each Rotpriest triggers independently when a creature is targeted")
    void multipleRotpriestsEachPoisonOpponent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new VeneratedRotpriest());
        harness.addToBattlefield(player1, new VeneratedRotpriest());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, creature.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @CardUsed({TurnToFrog.class})
    @DisplayName("Rotpriest does not trigger after losing its abilities")
    void doesNotTriggerAfterLosingAbilities() {
        Permanent rotpriest = harness.addToBattlefieldAndReturn(player1, new VeneratedRotpriest());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, rotpriest.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, creature.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @CardUsed({UnctussRetrofitter.class, TyvarsStand.class})
    @DisplayName("Targeting an animated artifact creature triggers Rotpriest")
    void triggersForAnimatedArtifactCreature() {
        harness.addToBattlefield(player1, new VeneratedRotpriest());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ElaborateFirecannon());
        harness.setHand(player1, List.of(new UnctussRetrofitter()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0, List.of(artifact.getId()));
        resolveAllTriggers();
        assertThat(gqs.isCreature(gd, artifact)).isTrue();
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();

        harness.setHand(player1, List.of(new TyvarsStand()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, 0, artifact.getId());

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }
}
