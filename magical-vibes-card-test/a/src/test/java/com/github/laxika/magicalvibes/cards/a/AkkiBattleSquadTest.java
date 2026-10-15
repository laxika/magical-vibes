package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IsshinTwoHeavensAsOne;
import com.github.laxika.magicalvibes.cards.s.ShimmerMyr;
import com.github.laxika.magicalvibes.cards.s.SnakeUmbra;
import com.github.laxika.magicalvibes.cards.s.SwiftfootBoots;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AkkiBattleSquad.class, GrizzlyBears.class, ShimmerMyr.class, SnakeUmbra.class,
        SwiftfootBoots.class, IsshinTwoHeavensAsOne.class})
class AkkiBattleSquadTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with a modified creature untaps modified creatures and creates an additional combat")
    void modifiedAttackUntapsModifiedCreaturesAndCreatesAdditionalCombat() {
        addCreatureReady(player1, new AkkiBattleSquad());
        Permanent modifiedAttacker = addCreatureReady(player1, new GrizzlyBears());
        modifiedAttacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent modifiedHome = addCreatureReady(player1, new GrizzlyBears());
        modifiedHome.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent unmodifiedHome = addCreatureReady(player1, new GrizzlyBears());
        modifiedHome.tap();
        unmodifiedHome.tap();

        declareAkkiAttackers(List.of(1));
        harness.passBothPriorities();

        assertThat(modifiedAttacker.isTapped()).isFalse();
        assertThat(modifiedHome.isTapped()).isFalse();
        assertThat(unmodifiedHome.isTapped()).isTrue();
        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);
        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);
    }

    @Test
    @DisplayName("Attacking only with unmodified creatures does not trigger")
    void unmodifiedAttackDoesNotTrigger() {
        addCreatureReady(player1, new AkkiBattleSquad());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAkkiAttackers(List.of(1)));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.combatPhasesThisTurn).isEqualTo(1);
        assertThat(attacker.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The trigger does not happen again in the same turn")
    void triggersOnlyOnceEachTurn() {
        addCreatureReady(player1, new AkkiBattleSquad());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAkkiAttackers(List.of(1));
        harness.passBothPriorities();

        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(1));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.additionalCombatPhasesOnly).isEqualTo(0);
    }

    private void declareAkkiAttackers(List<Integer> attackerIndices) {
        gd.combatPhasesThisTurn = 1;
        declareAttackers(attackerIndices);
    }

    @Test
    @DisplayName("Losing the attacker's last modification does not undo the attack trigger")
    void losingLastModificationStillCreatesAdditionalCombat() {
        addCreatureReady(player1, new AkkiBattleSquad());
        Permanent attacker = addCreatureReady(player1, new ShimmerMyr());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent home = addCreatureReady(player1, new ShimmerMyr());
        home.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        home.tap();

        declareAkkiAttackers(List.of(1));
        assertThat(gd.stack).hasSize(1);
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, harness::passBothPriorities);

        assertThat(attacker.isTapped()).isTrue();
        assertThat(home.isTapped()).isFalse();
        assertThat(gd.additionalCombatPhasesOnly).isEqualTo(1);
    }

    @Test
    @DisplayName("An attacker leaving before resolution does not prevent the additional combat")
    void removedAttackerStillCreatesAdditionalCombat() {
        addCreatureReady(player1, new AkkiBattleSquad());
        Permanent attacker = addCreatureReady(player1, new ShimmerMyr());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAkkiAttackers(List.of(1));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(attacker);
        gd.playerGraveyards.get(player1.getId()).add(attacker.getCard());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, harness::passBothPriorities);

        assertThat(gd.additionalCombatPhasesOnly).isEqualTo(1);
    }

    @Test
    @DisplayName("Untapping uses modifications at resolution and affects only your creatures")
    void untapsCreaturesModifiedAtResolutionOnly() {
        addCreatureReady(player1, new AkkiBattleSquad());
        Permanent attacker = addCreatureReady(player1, new ShimmerMyr());
        attacker.setCounterCount(CounterType.CHARGE, 1);
        Permanent home = addCreatureReady(player1, new ShimmerMyr());
        home.tap();
        Permanent opponent = addCreatureReady(player2, new ShimmerMyr());
        opponent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        opponent.tap();
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new SwiftfootBoots());
        equipment.setCounterCount(CounterType.CHARGE, 1);
        equipment.tap();

        declareAkkiAttackers(List.of(1));
        home.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, harness::passBothPriorities);

        assertThat(attacker.isTapped()).isFalse();
        assertThat(home.isTapped()).isFalse();
        assertThat(opponent.isTapped()).isTrue();
        assertThat(equipment.isTapped()).isTrue();
        assertThat(gd.additionalCombatPhasesOnly).isEqualTo(1);
    }

    @Test
    @DisplayName("Several modified attackers cause one trigger, including a modified Battle Squad")
    void multipleModifiedAttackersTriggerOnce() {
        Permanent squad = addCreatureReady(player1, new AkkiBattleSquad());
        squad.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent attacker = addCreatureReady(player1, new ShimmerMyr());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAkkiAttackers(List.of(0, 1));
        assertThat(gd.stack).hasSize(1);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, harness::passBothPriorities);

        assertThat(squad.isTapped()).isFalse();
        assertThat(attacker.isTapped()).isFalse();
        assertThat(gd.additionalCombatPhasesOnly).isEqualTo(1);
    }

    @Test
    @DisplayName("An Aura you control is a modification")
    void controlledAuraModifiesAttacker() {
        addCreatureReady(player1, new AkkiBattleSquad());
        Permanent attacker = addCreatureReady(player1, new ShimmerMyr());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SnakeUmbra());
        aura.setAttachedTo(attacker.getId());

        declareAkkiAttackers(List.of(1));
        assertThat(gd.stack).hasSize(1);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, harness::passBothPriorities);

        assertThat(attacker.isTapped()).isFalse();
        assertThat(gd.additionalCombatPhasesOnly).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's Aura alone is not a modification")
    void opponentAuraDoesNotModifyAttacker() {
        addCreatureReady(player1, new AkkiBattleSquad());
        Permanent attacker = addCreatureReady(player1, new ShimmerMyr());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new SnakeUmbra());
        aura.setAttachedTo(attacker.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAkkiAttackers(List.of(1)));

        assertThat(gd.stack).isEmpty();
        assertThat(attacker.isTapped()).isTrue();
        assertThat(gd.additionalCombatPhasesOnly).isZero();
    }

    @Test
    @DisplayName("Equipment modifies an attacker regardless of its controller")
    void opponentEquipmentModifiesAttacker() {
        addCreatureReady(player1, new AkkiBattleSquad());
        Permanent attacker = addCreatureReady(player1, new ShimmerMyr());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new SwiftfootBoots());
        equipment.setAttachedTo(attacker.getId());

        declareAkkiAttackers(List.of(1));
        assertThat(gd.stack).hasSize(1);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, harness::passBothPriorities);

        assertThat(attacker.isTapped()).isFalse();
        assertThat(gd.additionalCombatPhasesOnly).isEqualTo(1);
    }

    @Test
    @DisplayName("An unmodified attack does not consume the once-per-turn trigger")
    void unmodifiedAttackDoesNotUseUpTrigger() {
        addCreatureReady(player1, new AkkiBattleSquad());
        Permanent attacker = addCreatureReady(player1, new ShimmerMyr());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAkkiAttackers(List.of(1)));
        assertThat(gd.stack).isEmpty();
        attacker.untap();
        attacker.setAttacking(false);
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(List.of(1));
        assertThat(gd.stack).hasSize(1);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, harness::passBothPriorities);

        assertThat(attacker.isTapped()).isFalse();
        assertThat(gd.additionalCombatPhasesOnly).isEqualTo(1);
    }

    @Test
    @DisplayName("Isshin cannot make an ability limited to once each turn trigger twice")
    void isshinDoesNotBypassOncePerTurnLimit() {
        Permanent squad = addCreatureReady(player1, new AkkiBattleSquad());
        squad.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addCreatureReady(player1, new IsshinTwoHeavensAsOne());

        declareAkkiAttackers(List.of(0));

        assertThat(gd.stack).hasSize(1);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, harness::passBothPriorities);
        assertThat(gd.additionalCombatPhasesOnly).isEqualTo(1);
    }
}
