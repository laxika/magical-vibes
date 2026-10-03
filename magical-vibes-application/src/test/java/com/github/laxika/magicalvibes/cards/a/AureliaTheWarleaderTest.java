package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BorosGuildgate;
import com.github.laxika.magicalvibes.cards.d.DutifulThrull;
import com.github.laxika.magicalvibes.cards.h.Humility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AureliaTheWarleader.class, DutifulThrull.class, BorosGuildgate.class, Humility.class})
class AureliaTheWarleaderTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking untaps every creature you control and grants an additional combat phase")
    void attackUntapsAllCreaturesAndGrantsExtraCombat() {
        Permanent aurelia = addCreatureReady(player1, new AureliaTheWarleader());
        Permanent bear = addCreatureReady(player1, new DutifulThrull());
        Permanent tappedBear = addCreatureReady(player1, new DutifulThrull());
        tappedBear.tap(); // a creature that stayed home, not an attacker

        declareAttackers(player1, List.of(0, 1), 1);
        assertThat(bear.isTapped()).isTrue();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);

        // Untap is "all creatures you control", so the non-attacker untaps too. Vigilance keeps
        // Aurelia untapped throughout.
        assertThat(bear.isTapped()).isFalse();
        assertThat(tappedBear.isTapped()).isFalse();
        assertThat(aurelia.isTapped()).isFalse();

        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.passUntil(TurnStep.DECLARE_ATTACKERS);

        // The additional combat phase followed directly, with no postcombat main phase between.
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);
        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);
    }

    @Test
    @DisplayName("Attacking a second time in the same turn does not trigger again")
    void secondAttackSameTurnDoesNotTrigger() {
        addCreatureReady(player1, new AureliaTheWarleader());

        declareAttackers(player1, List.of(0), 1);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.passUntil(TurnStep.DECLARE_ATTACKERS);

        // Aurelia is now attacking again in the extra combat phase she created. "For the first time
        // each turn" gates the ability, so nothing new goes on the stack and no third combat phase
        // is queued.
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.stack).noneMatch(e -> e.getCard().getName().equals("Aurelia, the Warleader"));
        assertThat(gd.additionalCombatPhasesOnly).isEqualTo(0);
    }

    @Test
    @DisplayName("Attacking for the first time in a later combat phase still triggers")
    void firstAttackInLaterCombatPhaseStillTriggers() {
        Permanent aurelia = addCreatureReady(player1, new AureliaTheWarleader());

        // Aurelia sat out the turn's first combat and attacks for the first time in the second one:
        // the gate is per-creature "first time each turn", not "first combat phase".
        declareAttackers(player1, List.of(0), 2);

        assertThat(gd.stack).anyMatch(e -> e.getCard().getName().equals("Aurelia, the Warleader"));
        assertThat(aurelia.isTapped()).isFalse(); // vigilance
    }

    @Test
    @DisplayName("The once-each-turn gate resets, so Aurelia triggers again on a later turn")
    void triggersAgainOnALaterTurn() {
        addCreatureReady(player1, new AureliaTheWarleader());

        declareAttackers(player1, List.of(0), 1);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.passUntil(player1, TurnStep.UPKEEP);

        declareAttackers(player1, List.of(0), 1);

        assertThat(gd.stack).anyMatch(e -> e.getCard().getName().equals("Aurelia, the Warleader"));
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices, int combatPhaseNumber) {
        gd.combatPhasesThisTurn = combatPhaseNumber;
        declareAttackers(player, attackerIndices);
    }

    @Test
    @DisplayName("The trigger untaps only controlled creatures, leaving lands and opposing creatures tapped")
    void untapExcludesLandsAndOpposingCreatures() {
        addCreatureReady(player1, new AureliaTheWarleader());
        Permanent friendly = addCreatureReady(player1, new DutifulThrull());
        Permanent opposing = addCreatureReady(player2, new DutifulThrull());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new BorosGuildgate());
        friendly.tap();
        opposing.tap();
        land.tap();

        declareAttackers(player1, List.of(0), 1);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);

        assertThat(friendly.isTapped()).isFalse();
        assertThat(opposing.isTapped()).isTrue();
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Removing Aurelia in response does not stop untapping or the additional combat")
    void triggerResolvesWithoutItsSource() {
        Permanent aurelia = addCreatureReady(player1, new AureliaTheWarleader());
        Permanent friendly = addCreatureReady(player1, new DutifulThrull());
        friendly.tap();

        declareAttackers(player1, List.of(0), 1);
        gd.playerBattlefields.get(player1.getId()).remove(aurelia);
        gd.playerGraveyards.get(player1.getId()).add(aurelia.getCard());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);

        assertThat(friendly.isTapped()).isFalse();
        assertThat(gd.additionalCombatPhasesOnly).isEqualTo(1);
    }

    @Test
    @DisplayName("Aurelia does not trigger while Humility removes her abilities")
    void noAttackTriggerWhenAbilitiesAreRemoved() {
        addCreatureReady(player1, new AureliaTheWarleader());
        Permanent friendly = addCreatureReady(player1, new DutifulThrull());
        friendly.tap();
        harness.addToBattlefield(player1, new Humility());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0), 1));

        assertThat(gd.stack).isEmpty();
        assertThat(friendly.isTapped()).isTrue();
        assertThat(gd.additionalCombatPhasesOnly).isZero();
    }
}
