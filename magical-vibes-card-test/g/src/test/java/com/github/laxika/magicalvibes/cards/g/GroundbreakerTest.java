package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.m.MireBoa;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Groundbreaker.class, MireBoa.class})
class GroundbreakerTest extends BaseCardTest {

    @Test
    @DisplayName("Can attack immediately due to haste")
    void canAttackImmediatelyDueToHaste() {
        harness.setLife(player2, 20);

        Permanent groundbreaker = addCreatureReady(player1, new Groundbreaker());
        groundbreaker.setSummoningSick(true);

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Trample assigns excess combat damage to defending player")
    void trampleAssignsExcessCombatDamageToDefendingPlayer() {
        harness.setLife(player2, 20);

        Permanent groundbreaker = addCreatureReady(player1, new Groundbreaker());
        groundbreaker.setAttacking(true);

        Permanent mireBoa = addCreatureReady(player2, new MireBoa());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                mireBoa.getId(), 2,
                player2.getId(), 4
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        harness.assertInGraveyard(player1, "Groundbreaker");
        harness.assertInGraveyard(player2, "Mire Boa");
    }

    @Test
    @DisplayName("Sacrifices itself at end step")
    void sacrificesItselfAtEndStep() {
        Permanent groundbreaker = addCreatureReady(player1, new Groundbreaker());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getCard().getName()).isEqualTo("Groundbreaker");
        assertThat(trigger.getSourcePermanentId()).isEqualTo(groundbreaker.getId());

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Groundbreaker");
        harness.assertInGraveyard(player1, "Groundbreaker");
    }

    @Test
    @DisplayName("Sacrifices itself at the beginning of an opponent's end step")
    void sacrificesItselfAtBeginningOfOpponentsEndStep() {
        Permanent groundbreaker = addCreatureReady(player1, new Groundbreaker());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getCard().getName()).isEqualTo("Groundbreaker");
        assertThat(trigger.getSourcePermanentId()).isEqualTo(groundbreaker.getId());

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Groundbreaker");
        harness.assertInGraveyard(player1, "Groundbreaker");
    }

    @Test
    @DisplayName("Each Groundbreaker sacrifices only itself at the end step")
    void eachGroundbreakerSacrificesOnlyItself() {
        addCreatureReady(player1, new Groundbreaker());
        addCreatureReady(player1, new Groundbreaker());
        addCreatureReady(player1, new MireBoa());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Groundbreaker")).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Mire Boa");

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Groundbreaker");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Groundbreaker"))
                .hasSize(2);
        harness.assertOnBattlefield(player1, "Mire Boa");
    }

    @Test
    @DisplayName("Groundbreaker entering after the end step begins waits until the next end step")
    void enteringAfterEndStepBeginsWaitsUntilNextEndStep() {
        Permanent original = addCreatureReady(player1, new Groundbreaker());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).hasSize(1);
        Permanent newcomer = harness.enterBattlefieldAndReturn(player1, new Groundbreaker());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(newcomer).doesNotContain(original);
        harness.assertInGraveyard(player1, "Groundbreaker");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Groundbreaker");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Groundbreaker"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Casting requires three green mana")
    void castingRequiresThreeGreenMana() {
        harness.setHand(player1, List.of(new Groundbreaker()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Groundbreaker");
    }
}
