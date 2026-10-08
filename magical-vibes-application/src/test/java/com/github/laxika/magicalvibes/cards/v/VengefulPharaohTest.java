package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GideonJura;
import com.github.laxika.magicalvibes.cards.m.MerfolkLooter;
import com.github.laxika.magicalvibes.cards.r.Reclaim;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VengefulPharaoh.class, RuneclawBear.class, GideonJura.class, Reclaim.class,
        MerfolkLooter.class, Shock.class, Unsummon.class, VastwoodGorger.class})
class VengefulPharaohTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to you destroys the attacker and puts Vengeful Pharaoh on top of your library")
    void destroysAttackerAndTucksItself() {
        Permanent attacker = addCreatureReady(player1, new RuneclawBear());
        harness.setGraveyard(player2, List.of(new VengefulPharaoh()));

        declareAttackers(List.of(0));
        resolveCombat();
        harness.handlePermanentChosen(player2, attacker.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(attacker.getId()));
        harness.assertNotInGraveyard(player2, "Vengeful Pharaoh");
        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getName()).isEqualTo("Vengeful Pharaoh");
    }

    @Test
    @DisplayName("No trigger when the attack deals no combat damage to you")
    void doesNotTriggerWithoutCombatDamage() {
        addCreatureReady(player1, new RuneclawBear());
        addCreatureReady(player2, new RuneclawBear());
        harness.setGraveyard(player2, List.of(new VengefulPharaoh()));

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Vengeful Pharaoh");
        assertThat(gd.playerDecks.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Vengeful Pharaoh"));
    }

    @Test
    @DisplayName("Does not trigger from the attacking player's graveyard")
    void doesNotTriggerFromAttackerGraveyard() {
        Permanent attacker = addCreatureReady(player1, new RuneclawBear());
        harness.setGraveyard(player1, List.of(new VengefulPharaoh()));

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(attacker.getId()));
        harness.assertInGraveyard(player1, "Vengeful Pharaoh");
    }

    @Test
    void triggersFromCombatDamageToPlaneswalker() {
        Permanent attacker = addCreatureReady(player1, new RuneclawBear());
        Permanent gideon = harness.addToBattlefieldAndReturn(player2, new GideonJura());
        gideon.setCounterCount(CounterType.LOYALTY, 6);
        harness.setGraveyard(player2, List.of(new VengefulPharaoh()));

        attackPlayerAndPlaneswalker(List.of(0), Map.of(0, gideon.getId()));
        harness.handlePermanentChosen(player2, attacker.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getName()).isEqualTo("Vengeful Pharaoh");
    }

    @Test
    void simultaneousDamageToPlayerAndPlaneswalkerTriggersTwice() {
        Permanent first = addCreatureReady(player1, new RuneclawBear());
        Permanent second = addCreatureReady(player1, new RuneclawBear());
        Permanent gideon = harness.addToBattlefieldAndReturn(player2, new GideonJura());
        gideon.setCounterCount(CounterType.LOYALTY, 6);
        harness.setGraveyard(player2, List.of(new VengefulPharaoh()));

        attackPlayerAndPlaneswalker(List.of(0, 1), Map.of(0, player2.getId(), 1, gideon.getId()));
        harness.handlePermanentChosen(player2, first.getId());
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player2, second.getId());
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.assertNotInGraveyard(player2, "Vengeful Pharaoh");
        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getName()).isEqualTo("Vengeful Pharaoh");
    }

    @Test
    void simultaneousDamageFromMultipleCreaturesToPlayerTriggersOnce() {
        Permanent first = addCreatureReady(player1, new RuneclawBear());
        addCreatureReady(player1, new RuneclawBear());
        harness.setGraveyard(player2, List.of(new VengefulPharaoh()));

        declareAttackers(List.of(0, 1));
        resolveCombat();
        harness.handlePermanentChosen(player2, first.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        harness.assertLife(player2, 16);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void doesNothingIfSourceLeavesGraveyardBeforeResolution() {
        Permanent attacker = addCreatureReady(player1, new RuneclawBear());
        VengefulPharaoh pharaoh = new VengefulPharaoh();
        harness.setGraveyard(player2, List.of(pharaoh));
        harness.setHand(player2, List.of(new Reclaim()));

        declareAttackers(List.of(0));
        resolveCombat();
        harness.handlePermanentChosen(player2, attacker.getId());
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player2, 0, pharaoh.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Runeclaw Bear");
        harness.assertNotInGraveyard(player2, "Vengeful Pharaoh");
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(pharaoh);
    }

    @Test
    void doesNothingIfSourceLeavesAndReentersGraveyardBeforeResolution() {
        Permanent attacker = addCreatureReady(player1, new RuneclawBear());
        addCreatureReady(player2, new MerfolkLooter());
        VengefulPharaoh pharaoh = new VengefulPharaoh();
        harness.setGraveyard(player2, List.of(pharaoh));
        harness.setHand(player2, List.of(new Reclaim()));

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();
        harness.handlePermanentChosen(player2, attacker.getId());
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player2, 0, pharaoh.getId());
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Runeclaw Bear");
        harness.assertInGraveyard(player2, "Vengeful Pharaoh");
        assertThat(gd.playerDecks.get(player2.getId())).noneMatch(c -> c.getId().equals(pharaoh.getId()));
    }

    @Test
    void illegalTargetPreventsReturningSourceToLibrary() {
        Permanent attacker = addCreatureReady(player1, new RuneclawBear());
        harness.setGraveyard(player2, List.of(new VengefulPharaoh()));
        harness.setHand(player2, List.of(new Unsummon()));

        declareAttackers(List.of(0));
        resolveCombat();
        harness.handlePermanentChosen(player2, attacker.getId());
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, attacker.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Runeclaw Bear");
        harness.assertInGraveyard(player2, "Vengeful Pharaoh");
        assertThat(gd.playerDecks.get(player2.getId())).noneMatch(c -> c.getName().equals("Vengeful Pharaoh"));
    }

    @Test
    void noncombatDamageDoesNotTrigger() {
        harness.setGraveyard(player2, List.of(new VengefulPharaoh()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player2, "Vengeful Pharaoh");
    }

    @Test
    void twoPharaohsTargetingSameAttackerLeaveOneInGraveyard() {
        Permanent attacker = addCreatureReady(player1, new RuneclawBear());
        harness.setGraveyard(player2, List.of(new VengefulPharaoh(), new VengefulPharaoh()));

        declareAttackers(List.of(0));
        resolveCombat();
        harness.handlePermanentChosen(player2, attacker.getId());
        harness.handlePermanentChosen(player2, attacker.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(c -> c.getName().equals("Vengeful Pharaoh")).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId()))
                .filteredOn(c -> c.getName().equals("Vengeful Pharaoh")).hasSize(1);
    }

    @Test
    void doesNotTriggerWhileOnBattlefield() {
        addCreatureReady(player1, new RuneclawBear());
        addCreatureReady(player2, new VengefulPharaoh());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player2, 18);
        harness.assertOnBattlefield(player1, "Runeclaw Bear");
        harness.assertOnBattlefield(player2, "Vengeful Pharaoh");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void deathtouchKillsLargerCreatureButDyingInSameDamageStepDoesNotTrigger() {
        addCreatureReady(player1, new VastwoodGorger());
        addCreatureReady(player1, new RuneclawBear());
        addCreatureReady(player2, new VengefulPharaoh());

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Vastwood Gorger");
        harness.assertInGraveyard(player2, "Vengeful Pharaoh");
        harness.assertOnBattlefield(player1, "Runeclaw Bear");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void attackPlayerAndPlaneswalker(List<Integer> attackers, Map<Integer, UUID> targets) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, attackers, targets);
        resolveCombat();
    }
}
