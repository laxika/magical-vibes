package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CompelledDuel.class, GrizzlyBears.class})
class CompelledDuelTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gets +3/+3 and must be blocked if able")
    void boostsAndForcesBlock() {
        castCompelledDuelOnBears();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.getEffectivePower()).isEqualTo(5);
        assertThat(bears.getEffectiveToughness()).isEqualTo(5);
        assertThat(bears.isMustBeBlockedThisTurn()).isTrue();
        assertThat(bears.isMustBeBlockedByAllThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Boost and must-be-blocked requirement wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        castCompelledDuelOnBears();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
        assertThat(bears.isMustBeBlockedThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Compelled Duel fizzles if its target leaves the battlefield")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CompelledDuel()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.castSorcery(player1, 0, targetId);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Compelled Duel");
    }

    @Test
    @DisplayName("An available blocker cannot decline to block the compelled attacker")
    void requiresAnAvailableBlocker() {
        castCompelledDuelOnBears();
        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be blocked if able");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(blocker.getBlockingTargetIds()).containsExactly(attacker.getId());
    }

    @Test
    @DisplayName("One blocker satisfies Compelled Duel without forcing all creatures to block")
    void oneBlockerIsEnough() {
        castCompelledDuelOnBears();
        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent unusedBlocker = addCreatureReady(player2, new GrizzlyBears());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.getBlockingTargetIds()).containsExactly(attacker.getId());
        assertThat(unusedBlocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("A tapped defender does not have to block the compelled attacker")
    void tappedDefenderCannotBeForcedToBlock() {
        castCompelledDuelOnBears();
        gd.playerBattlefields.get(player1.getId()).getFirst().setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.tap();
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of());

        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Assigning the only blocker to another attacker cannot evade Compelled Duel")
    void cannotEvadeRequirementByBlockingAnotherAttacker() {
        castCompelledDuelOnBears();
        gd.playerBattlefields.get(player1.getId()).getFirst().setAttacking(true);
        addCreatureReady(player1, new GrizzlyBears()).setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be blocked if able");
    }

    @Test
    @DisplayName("Compelled Duel can target an opponent's creature")
    void canTargetOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CompelledDuel()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);
        assertThat(target.isMustBeBlockedThisTurn()).isTrue();
    }

    private void castCompelledDuelOnBears() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CompelledDuel()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.castAndResolveSorcery(player1, 0, targetId);
    }
}
