package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TangleclawWerewolfTest extends BaseCardTest {

    @Test
    @DisplayName("Tangleclaw Werewolf can block two creatures")
    void frontFaceCanBlockTwoCreatures() {
        Permanent werewolf = addReadyWerewolf(player2);
        addAttacker(player1);
        addAttacker(player1);

        beginBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(werewolf);
        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIndex, 0),
                new BlockerAssignment(blockerIndex, 1)
        ))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("{6}{G} transforms Tangleclaw Werewolf")
    void transformsWithActivatedAbility() {
        Permanent werewolf = addReadyWerewolf(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(werewolf);
        harness.activateAbility(player1, index, null, null);
        harness.passBothPriorities();

        assertThat(werewolf.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Fibrous Entangler must be blocked if able")
    void backFaceMustBeBlockedIfAble() {
        Permanent werewolf = addReadyWerewolf(player1);
        transform(werewolf);
        werewolf.setAttacking(true);
        addReadyWerewolf(player2);

        beginBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be blocked if able");
    }

    private Permanent addReadyWerewolf(com.github.laxika.magicalvibes.model.Player player) {
        Permanent werewolf = new Permanent(new TangleclawWerewolf());
        werewolf.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(werewolf);
        return werewolf;
    }

    private void addAttacker(com.github.laxika.magicalvibes.model.Player player) {
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player.getId()).add(attacker);
    }

    private void beginBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }

    private void transform(Permanent werewolf) {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(werewolf);
        harness.activateAbility(player1, index, null, null);
        harness.passBothPriorities();
    }
}
