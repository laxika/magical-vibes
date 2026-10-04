package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HixusPrisonWarden.class, GrizzlyBears.class})
class HixusPrisonWardenTest extends BaseCardTest {

    /** Puts Hixus onto player2's battlefield, optionally recorded as having entered this turn. */
    private Permanent putHixus(boolean enteredThisTurn) {
        Permanent hixus = enteredThisTurn
                ? harness.enterBattlefieldAndReturn(player2, new HixusPrisonWarden())
                : harness.addToBattlefieldAndReturn(player2, new HixusPrisonWarden());
        hixus.setSummoningSick(false);
        return hixus;
    }

    /** Attacks player2 with a Grizzly Bears and lets combat damage resolve. */
    private void attackWithBears() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        resolveCombat();
        harness.passBothPriorities(); // exile trigger resolves
    }

    @Test
    @DisplayName("Creature dealing combat damage is exiled when Hixus entered this turn")
    void exilesCombatDamageSourceWhenEnteredThisTurn() {
        putHixus(true);
        attackWithBears();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Nothing is exiled when Hixus did not enter this turn")
    void doesNotExileWhenHixusEnteredEarlier() {
        putHixus(false);
        attackWithBears();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Exiled creature returns when Hixus leaves the battlefield")
    void exiledCreatureReturnsWhenHixusLeaves() {
        Permanent hixus = putHixus(true);
        attackWithBears();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        hixus.setMarkedDamage(4);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Hixus, Prison Warden");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void doesNotExileIfHixusLeavesBeforeTriggerResolves() {
        Permanent hixus = putHixus(true);
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        resolveCombat();
        assertThat(gd.stack).hasSize(1);

        hixus.setMarkedDamage(4);
        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void stolenCreatureReturnsImmediatelyToItsOwner() {
        Permanent hixus = putHixus(true);
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.stolenCreatures.put(attacker.getId(), player2.getId());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        resolveCombat();
        resolveAllTriggers();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        hixus.setMarkedDamage(4);
        harness.runStateBasedActions();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void exilesEachCreatureThatDealsCombatDamage() {
        Permanent hixus = putHixus(true);
        for (int i = 0; i < 2; i++) {
            Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
            attacker.setSummoningSick(false);
            attacker.setAttacking(true);
        }
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");

        hixus.setMarkedDamage(4);
        harness.runStateBasedActions();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Grizzly Bears"))
                .hasSize(2);
    }

    @Test
    void canBeFlashedInBeforeCombatDamage() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.setHand(player2, List.of(new HixusPrisonWarden()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Hixus, Prison Warden");

        resolveCombat();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }
}
