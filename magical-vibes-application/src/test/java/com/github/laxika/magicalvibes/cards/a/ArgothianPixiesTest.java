package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.t.Triskelion;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArgothianPixiesTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot be blocked by an artifact creature")
    void cannotBeBlockedByArtifactCreature() {
        Permanent pixies = new Permanent(new ArgothianPixies());
        pixies.setSummoningSick(false);
        pixies.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(pixies);

        Permanent blocker = new Permanent(new Ornithopter());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block");
    }

    @Test
    @DisplayName("Can be blocked by a non-artifact creature")
    void canBeBlockedByNonArtifactCreature() {
        Permanent pixies = new Permanent(new ArgothianPixies());
        pixies.setSummoningSick(false);
        pixies.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(pixies);

        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Prevents combat damage from artifact creatures")
    void preventsCombatDamageFromArtifactCreatures() {
        Permanent pixies = new Permanent(new ArgothianPixies());
        pixies.setSummoningSick(false);
        pixies.setBlocking(true);
        pixies.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(pixies);

        Ornithopter attackerCard = new Ornithopter();
        attackerCard.setPower(2);
        Permanent attacker = new Permanent(attackerCard);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Argothian Pixies");
        assertThat(pixies.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not prevent combat damage from non-artifact creatures")
    void doesNotPreventCombatDamageFromNonArtifactCreatures() {
        Permanent pixies = new Permanent(new ArgothianPixies());
        pixies.setSummoningSick(false);
        pixies.setBlocking(true);
        pixies.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(pixies);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Argothian Pixies");
        harness.assertInGraveyard(player2, "Argothian Pixies");
    }

    @Test
    @DisplayName("Prevents noncombat damage from an artifact creature")
    void preventsNoncombatDamageFromArtifactCreature() {
        Permanent pixies = addCreatureReady(player2, new ArgothianPixies());
        Permanent triskelion = addCreatureReady(player1, new Triskelion());
        triskelion.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, 0, null, pixies.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Argothian Pixies");
        assertThat(pixies.getMarkedDamage()).isZero();
    }
}
