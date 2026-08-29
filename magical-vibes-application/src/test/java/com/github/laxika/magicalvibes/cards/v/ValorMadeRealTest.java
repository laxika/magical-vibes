package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ValorMadeReal.class, GrizzlyBears.class, Pacifism.class})
class ValorMadeRealTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature can block any number of attackers")
    void targetCreatureCanBlockAnyNumberOfAttackers() {
        Permanent blocker = addCreature(player2);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        addAttacker();
        addAttacker();
        addAttacker();

        castValorMadeReal(blocker);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIndex, 0),
                new BlockerAssignment(blockerIndex, 1),
                new BlockerAssignment(blockerIndex, 2)
        ));

        assertThat(blocker.getBlockingTargets()).containsExactlyInAnyOrder(0, 1, 2);
    }

    @Test
    @DisplayName("Valor Made Real's effect expires at end of turn")
    void effectExpiresAtEndOfTurn() {
        Permanent blocker = addCreature(player2);
        castValorMadeReal(blocker);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getAdditionalBlocksUntilEndOfTurn()).isZero();
    }

    @Test
    @DisplayName("Valor Made Real cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent noncreature = new Permanent(new Pacifism());
        gd.playerBattlefields.get(player2.getId()).add(noncreature);
        harness.setHand(player1, List.of(new ValorMadeReal()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castValorMadeReal(Permanent target) {
        harness.setHand(player1, List.of(new ValorMadeReal()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addCreature(Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private void addAttacker() {
        Permanent attacker = addCreature(player1);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
    }
}
