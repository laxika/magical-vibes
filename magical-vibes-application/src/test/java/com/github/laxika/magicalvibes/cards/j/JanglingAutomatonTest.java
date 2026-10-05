package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.a.AwakenedSkyclave;
import com.github.laxika.magicalvibes.cards.d.DuskriderFalcon;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.cards.x.XanthicStatue;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JanglingAutomaton.class, DuskriderFalcon.class, XanthicStatue.class,
        InvasionOfZendikar.class, AwakenedSkyclave.class})
class JanglingAutomatonTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking untaps all creatures the defending player controls")
    void attackUntapsDefendingCreatures() {
        Permanent automaton = addCreatureReady(player1, new JanglingAutomaton());
        Permanent ownCreature = addCreatureReady(player1, new DuskriderFalcon());
        Permanent defendingCreature = addCreatureReady(player2, new DuskriderFalcon());
        Permanent otherDefendingCreature = addCreatureReady(player2, new DuskriderFalcon());
        Permanent defendingArtifact = harness.addToBattlefieldAndReturn(player2, new XanthicStatue());

        ownCreature.tap();
        defendingCreature.tap();
        otherDefendingCreature.tap();
        defendingArtifact.tap();

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(defendingCreature.isTapped()).isFalse();
        assertThat(otherDefendingCreature.isTapped()).isFalse();
        assertThat(automaton.isTapped()).isTrue();
        assertThat(ownCreature.isTapped()).isTrue();
        assertThat(defendingArtifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not trigger when another creature attacks")
    void doesNotTriggerForAnotherAttacker() {
        Permanent automaton = addCreatureReady(player1, new JanglingAutomaton());
        Permanent otherAttacker = addCreatureReady(player1, new DuskriderFalcon());
        Permanent defendingCreature = addCreatureReady(player2, new DuskriderFalcon());
        defendingCreature.tap();

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(automaton.isTapped()).isFalse();
        assertThat(otherAttacker.isTapped()).isTrue();
        assertThat(defendingCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Still uses the original defending player if the attacker leaves combat")
    void usesOriginalDefendingPlayerAfterAttackerLeavesCombat() {
        Permanent automaton = addCreatureReady(player1, new JanglingAutomaton());
        Permanent defendingCreature = addCreatureReady(player2, new DuskriderFalcon());
        defendingCreature.tap();

        declareAttackers(player1, List.of(0));
        automaton.setAttacking(false);
        automaton.setAttackTarget(null);
        resolveAllTriggers();

        assertThat(defendingCreature.isTapped()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @DisplayName("Attacking a battle untaps its protector's creatures even if the battle leaves")
    void attackUntapsBattleProtectorsCreatures(boolean battleLeaves) {
        Permanent automaton = addCreatureReady(player1, new JanglingAutomaton());
        Permanent ownCreature = addCreatureReady(player1, new DuskriderFalcon());
        Permanent defendingCreature = addCreatureReady(player2, new DuskriderFalcon());
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfZendikar());
        battle.setProtectorPlayerId(player2.getId());
        ownCreature.tap();
        defendingCreature.tap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0), Map.of(0, battle.getId()));
        assertThat(gd.stack).hasSize(1);

        if (battleLeaves) {
            gd.playerBattlefields.get(player1.getId()).remove(battle);
            automaton.setAttacking(false);
            automaton.setAttackTarget(null);
        }
        resolveAllTriggers();

        assertThat(defendingCreature.isTapped()).isFalse();
        assertThat(ownCreature.isTapped()).isTrue();
        assertThat(automaton.isTapped()).isTrue();
    }
}
