package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
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

@CardUsed({GroundRift.class, GrizzlyBears.class, WindDrake.class})
class GroundRiftTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature without flying can't block this turn")
    void targetCreatureCannotBlockThisTurn() {
        Permanent attacker = addReadyCreature(player1);
        Permanent blocker = addReadyCreature(player2);

        castGroundRift(blocker.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(blocker.isCantBlockThisTurn()).isTrue();
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature with flying")
    void cannotTargetCreatureWithFlying() {
        Permanent flier = harness.addToBattlefieldAndReturn(player2, new WindDrake());
        harness.setHand(player1, List.of(new GroundRift()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, flier.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature without flying");
    }

    @Test
    @DisplayName("Storm creates one copy for each spell cast before Ground Rift")
    void stormCreatesCopiesForEachPriorSpell() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.recordSpellCast(player1.getId(), new GrizzlyBears());
        gd.recordSpellCast(player2.getId(), new GrizzlyBears());

        castGroundRift(target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
    }

    private Permanent addReadyCreature(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void castGroundRift(UUID targetId) {
        harness.setHand(player1, List.of(new GroundRift()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0, targetId);
    }
}
