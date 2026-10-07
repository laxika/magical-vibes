package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.MistralCharger;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.SacrificeAtEndOfCombat;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StoicEphemera.class, MistralCharger.class})
class StoicEphemeraTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking pushes Stoic Ephemera's sacrifice trigger onto the stack")
    void blockingPushesSacrificeTrigger() {
        Permanent attacker = addCreatureReady(player1, new MistralCharger());
        attacker.setAttacking(true);
        Permanent stoicEphemera = addCreatureReady(player2, new StoicEphemera());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).anyMatch(entry ->
                entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && entry.getCard().getName().equals("Stoic Ephemera"));

        harness.passBothPriorities();

        assertThat(gd.getDelayedActions(SacrificeAtEndOfCombat.class))
                .anyMatch(action -> action.permanentId().equals(stoicEphemera.getId()));
    }

    @Test
    @DisplayName("Stoic Ephemera is sacrificed at end of combat after blocking")
    void sacrificedAtEndOfCombatAfterBlocking() {
        Permanent attacker = addCreatureReady(player1, new MistralCharger());
        attacker.setAttacking(true);
        addCreatureReady(player2, new StoicEphemera());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertNotOnBattlefield(player2, "Stoic Ephemera");
        harness.assertInGraveyard(player2, "Stoic Ephemera");
    }

    @Test
    @DisplayName("Stoic Ephemera is not sacrificed when it does not block")
    void doesNotTriggerWhenItDoesNotBlock() {
        Permanent attacker = addCreatureReady(player1, new MistralCharger());
        attacker.setAttacking(true);
        addCreatureReady(player2, new StoicEphemera());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        harness.passBothPriorities();

        assertThat(gd.hasDelayedAction(SacrificeAtEndOfCombat.class)).isFalse();
        harness.assertOnBattlefield(player2, "Stoic Ephemera");
    }

    @Test
    @DisplayName("End-of-combat sacrifice uses the stack and allows responses")
    void endOfCombatSacrificeAllowsResponses() {
        Permanent attacker = addCreatureReady(player1, new MistralCharger());
        attacker.setAttacking(true);
        Permanent ephemera = addCreatureReady(player2, new StoicEphemera());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passUntil(TurnStep.END_OF_COMBAT);

        harness.assertOnBattlefield(player2, "Stoic Ephemera");
        assertThat(gd.stack).anyMatch(entry ->
                entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && ephemera.getId().equals(entry.getSourcePermanentId())
                        && player2.getId().equals(entry.getControllerId()));

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Stoic Ephemera");
        harness.assertInGraveyard(player2, "Stoic Ephemera");
    }

    @Test
    @DisplayName("Only the Stoic Ephemera that blocked is sacrificed")
    void nonblockingCopySurvives() {
        Permanent attacker = addCreatureReady(player1, new MistralCharger());
        attacker.setAttacking(true);
        Permanent blockingEphemera = addCreatureReady(player2, new StoicEphemera());
        Permanent nonblockingEphemera = addCreatureReady(player2, new StoicEphemera());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(nonblockingEphemera).doesNotContain(blockingEphemera);
        harness.assertInGraveyard(player2, "Stoic Ephemera");
    }

}
