package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThijarianWitness.class, GrizzlyBears.class})
class ThijarianWitnessTest extends BaseCardTest {

    @Test
    void exilesAndInvestigatesWhenAnotherCreatureAttacksAlone() {
        addCreatureReady(player1, new ThijarianWitness());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        markAsAttacking(attacker);

        kill(attacker);

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void doesNotTriggerWhenAnotherCreatureAttacksWithCompany() {
        addCreatureReady(player1, new ThijarianWitness());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        Permanent companion = addCreatureReady(player2, new GrizzlyBears());
        markAsAttacking(attacker);
        markAsAttacking(companion);

        kill(attacker);

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void exilesAndInvestigatesWhenAnotherCreatureBlocksAlone() {
        addCreatureReady(player1, new ThijarianWitness());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player1, new GrizzlyBears());
        markAsAttacking(attacker);
        markAsBlocking(blocker, attacker);

        kill(blocker);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void doesNotTriggerWhenAnotherCreatureBlocksWithCompany() {
        addCreatureReady(player1, new ThijarianWitness());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player1, new GrizzlyBears());
        Permanent companion = addCreatureReady(player1, new GrizzlyBears());
        markAsAttacking(attacker);
        markAsBlocking(blocker, attacker);
        markAsBlocking(companion, attacker);

        kill(blocker);

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void doesNotTriggerForItsOwnDeathWhileAttackingAlone() {
        Permanent witness = addCreatureReady(player1, new ThijarianWitness());
        markAsAttacking(witness);

        kill(witness);

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void doesNotTriggerForCreatureOutsideCombat() {
        addCreatureReady(player1, new ThijarianWitness());
        Permanent creature = addCreatureReady(player2, new ThijarianWitness());

        kill(creature);

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void triggersWhenLastRemainingAttackerDiesAfterItsCompanion() {
        addCreatureReady(player1, new ThijarianWitness());
        Permanent attacker = addCreatureReady(player2, new ThijarianWitness());
        Permanent companion = addCreatureReady(player2, new ThijarianWitness());
        markAsAttacking(attacker);
        markAsAttacking(companion);

        kill(companion);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        kill(attacker);

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(attacker.getCard());
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void doesNotTriggerWhenAnAdditionalCreatureWasPutOntoBattlefieldAttacking() {
        addCreatureReady(player1, new ThijarianWitness());
        Permanent attacker = addCreatureReady(player2, new ThijarianWitness());
        markAsAttacking(attacker);
        Permanent companion = addCreatureReady(player2, new ThijarianWitness());
        companion.setAttacking(true);
        companion.setAttackTarget(player1.getId());

        kill(attacker);

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void triggersForSoleAttackerPutOntoBattlefieldAttacking() {
        addCreatureReady(player1, new ThijarianWitness());
        Permanent attacker = addCreatureReady(player2, new ThijarianWitness());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());

        kill(attacker);

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(attacker.getCard());
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void investigatesEvenWhenDyingCardHasLeftGraveyardBeforeResolution() {
        addCreatureReady(player1, new ThijarianWitness());
        Permanent attacker = addCreatureReady(player2, new ThijarianWitness());
        markAsAttacking(attacker);
        attacker.setMarkedDamage(gqs.getEffectiveToughness(gd, attacker));
        harness.runStateBasedActions();
        harness.setGraveyard(player2, java.util.List.of());
        harness.setExile(player2, java.util.List.of(attacker.getCard()));

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(attacker.getCard());
    }

    @Test
    void doesNotTriggerWhenTwoBlockersDieSimultaneously() {
        addCreatureReady(player1, new ThijarianWitness());
        Permanent attacker = addCreatureReady(player2, new ThijarianWitness());
        Permanent blocker = addCreatureReady(player1, new ThijarianWitness());
        Permanent companion = addCreatureReady(player1, new ThijarianWitness());
        markAsAttacking(attacker);
        markAsBlocking(blocker, attacker);
        markAsBlocking(companion, attacker);
        blocker.setMarkedDamage(gqs.getEffectiveToughness(gd, blocker));
        companion.setMarkedDamage(gqs.getEffectiveToughness(gd, companion));

        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }
    private void markAsAttacking(Permanent creature) {
        creature.setAttacking(true);
        creature.setAttackTarget(player1.getId());
        gd.declaredAttackerIdsThisCombat.add(creature.getId());
    }

    private void markAsBlocking(Permanent blocker, Permanent attacker) {
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(attacker.getId());
    }

    private void kill(Permanent creature) {
        creature.setMarkedDamage(gqs.getEffectiveToughness(gd, creature));
        harness.runStateBasedActions();
        resolveAllTriggers();
    }
}
