package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.ArdentMilitia;
import com.github.laxika.magicalvibes.cards.e.EmpyrialArmor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.PhaseOutAtEndOfCombat;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TeferisVeil.class, ArdentMilitia.class, EmpyrialArmor.class})
class TeferisVeilTest extends BaseCardTest {

    @Test
    @DisplayName("An attacking creature you control phases out at end of combat, not immediately")
    void attackerPhasesOutAtEndOfCombat() {
        harness.addToBattlefield(player1, new TeferisVeil());
        Permanent attacker = addCreatureReady(player1, new ArdentMilitia());
        int startingLife = gd.getLife(player2.getId());

        declareAttackers(player1, List.of(1));
        assertThat(gd.stack).isNotEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.phasedOutPermanents.getOrDefault(player1.getId(), List.of()))
                .doesNotContain(attacker);
        resolveAllTriggers();

        // The phasing is delayed to end of combat, so the attacker still dealt its combat damage.
        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife - 2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(attacker);
    }

    @Test
    @DisplayName("The phased-out attacker phases in during its controller's next untap step")
    void phasedOutAttackerPhasesBackIn() {
        harness.addToBattlefield(player1, new TeferisVeil());
        Permanent attacker = addCreatureReady(player1, new ArdentMilitia());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(attacker);

        advanceToUpkeep(player2); // not its controller's untap step, so it stays phased out
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);

        advanceToUpkeep(player1); // its controller's untap step — it phases in
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
    }

    @Test
    @DisplayName("A creature that stays back does not phase out")
    void nonAttackerDoesNotPhaseOut() {
        harness.addToBattlefield(player1, new TeferisVeil());
        Permanent bystander = addCreatureReady(player1, new ArdentMilitia());

        declareAttackers(player1, List.of());
        resolveCombat();

        assertThat(gd.hasDelayedAction(PhaseOutAtEndOfCombat.class)).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bystander);
    }

    @Test
    @DisplayName("An opponent's attacking creature is unaffected")
    void opponentAttackerDoesNotPhaseOut() {
        harness.addToBattlefield(player1, new TeferisVeil());
        Permanent attacker = addCreatureReady(player2, new ArdentMilitia());

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(attacker);
        assertThat(gd.phasedOutPermanents.getOrDefault(player2.getId(), List.of()))
                .doesNotContain(attacker);
    }

    @Test
    @DisplayName("Each attacking creature you control gets its own phase-out")
    void eachAttackerPhasesOut() {
        harness.addToBattlefield(player1, new TeferisVeil());
        Permanent firstAttacker = addCreatureReady(player1, new ArdentMilitia());
        Permanent secondAttacker = addCreatureReady(player1, new ArdentMilitia());
        int startingLife = gd.getLife(player2.getId());

        declareAttackers(player1, List.of(1, 2));
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife - 4);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .doesNotContain(firstAttacker, secondAttacker);
        assertThat(gd.phasedOutPermanents.get(player1.getId()))
                .contains(firstAttacker, secondAttacker);
    }

    @Test
    @DisplayName("A blocked attacking creature still phases out at end of combat")
    void blockedAttackerPhasesOut() {
        harness.addToBattlefield(player1, new TeferisVeil());
        Permanent attacker = addCreatureReady(player1, new ArdentMilitia());
        Permanent blocker = addCreatureReady(player2, new ArdentMilitia());

        declareAttackersAndPrepareBlockers(player1, List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        resolveAllTriggers();
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
    }

    @Test
    @DisplayName("The delayed phase-out uses the stack as the end of combat step begins")
    void phaseOutTriggersAtBeginningOfEndOfCombat() {
        harness.addToBattlefield(player1, new TeferisVeil());
        Permanent attacker = addCreatureReady(player1, new ArdentMilitia());
        int startingLife = gd.getLife(player2.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(1)));
        harness.passUntil(player1, TurnStep.END_OF_COMBAT);

        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife - 2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.stack).hasSize(1);

        harness.withAutoStop(TurnStep.END_OF_COMBAT, this::resolveAllTriggers);

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_OF_COMBAT);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(attacker);
    }

    @Test
    @DisplayName("Removing Teferi's Veil after its attack ability resolves does not cancel phasing")
    void phaseOutSurvivesRemovalOfVeil() {
        harness.addToBattlefield(player1, new TeferisVeil());
        Permanent veil = findPermanent(player1, "Teferi's Veil");
        Permanent attacker = addCreatureReady(player1, new ArdentMilitia());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(1));
            resolveAllTriggers();
        });
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, veil);
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(attacker);
        harness.assertInGraveyard(player1, "Teferi's Veil");
    }

    @Test
    @DisplayName("An opposing Aura returns with the phased-out attacker and its counters remain intact")
    void auraAndCountersSurvivePhasing() {
        harness.addToBattlefield(player1, new TeferisVeil());
        Permanent attacker = addCreatureReady(player1, new ArdentMilitia());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addToBattlefield(player2, new EmpyrialArmor());
        Permanent aura = findPermanent(player2, "Empyrial Armor");
        aura.setAttachedTo(attacker.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(1)));
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(attacker);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(aura);
        harness.assertNotInGraveyard(player2, "Empyrial Armor");

        advanceToUpkeep(player2);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(aura);

        advanceToUpkeep(player1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(aura);
        assertThat(aura.getAttachedTo()).isEqualTo(attacker.getId());
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A departed attacker does not cause a new creature to phase out")
    void departedAttackerDoesNotPhaseOutReplacement() {
        harness.addToBattlefield(player1, new TeferisVeil());
        Permanent attacker = addCreatureReady(player1, new ArdentMilitia());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(1));
            resolveAllTriggers();
        });
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, attacker);
        Permanent replacement = addCreatureReady(player1, new ArdentMilitia());
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(replacement);
        assertThat(gd.phasedOutPermanents.getOrDefault(player1.getId(), List.of()))
                .doesNotContain(attacker, replacement);
        harness.assertInGraveyard(player1, "Ardent Militia");
    }
}
