package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KulrathKnightTest extends BaseCardTest {

    // ===== Attack side =====

    @Test
    @DisplayName("An opponent's creature with a counter can't attack while Kulrath Knight is out")
    void opponentCounteredCreatureCannotAttack() {
        addReadyKulrath(player1);
        Permanent attacker = addCreature(player2);
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        beginAttackers(player2);

        int index = gd.playerBattlefields.get(player2.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of(index)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent's creature without counters can still attack")
    void opponentCreatureWithoutCountersCanAttack() {
        addReadyKulrath(player1);
        Permanent attacker = addCreature(player2);

        beginAttackers(player2);

        int index = gd.playerBattlefields.get(player2.getId()).indexOf(attacker);
        gs.declareAttackers(gd, player2, List.of(index));

        assertThat(attacker.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Kulrath's controller's own countered creatures can still attack")
    void ownCounteredCreatureCanAttack() {
        addReadyKulrath(player1);
        Permanent attacker = addCreature(player1);
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLife(player2, 20);

        beginAttackers(player1);

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareAttackers(gd, player1, List.of(index));

        // 2/2 test creature plus a +1/+1 counter = 3 damage.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    // ===== Block side =====

    @Test
    @DisplayName("An opponent's creature with a counter can't block while Kulrath Knight is out")
    void opponentCounteredCreatureCannotBlock() {
        addReadyKulrath(player1);
        Permanent attacker = addCreature(player1);
        attacker.setAttacking(true);

        Permanent blocker = addCreature(player2);
        blocker.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        prepareDeclareBlockers();

        // Kulrath is at attacker-battlefield index 0; the attacking test creature is at index 1.
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent's creature without counters can still block")
    void opponentCreatureWithoutCountersCanBlock() {
        addReadyKulrath(player1);
        Permanent attacker = addCreature(player1);
        attacker.setAttacking(true);

        addCreature(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(gd.gameLog).anyMatch(log -> log.contains("declares 1 blocker"));
    }

    // ===== Helpers =====

    private Permanent addReadyKulrath(Player player) {
        Permanent perm = new Permanent(new KulrathKnight());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addCreature(Player controller) {
        Card creature = new Card();
        creature.setName("Test Creature");
        creature.setType(CardType.CREATURE);
        creature.setSubtypes(new ArrayList<>());
        creature.setPower(2);
        creature.setToughness(2);
        Permanent perm = new Permanent(creature);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(controller.getId()).add(perm);
        return perm;
    }

    private void beginAttackers(Player attackingPlayer) {
        harness.forceActivePlayer(attackingPlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    private void prepareDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
