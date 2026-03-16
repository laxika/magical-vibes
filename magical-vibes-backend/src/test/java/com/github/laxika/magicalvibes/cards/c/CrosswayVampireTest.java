package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CrosswayVampireTest extends BaseCardTest {

    @Test
    @DisplayName("ETB makes target creature unable to block this turn")
    void etbMakesTargetUnableToBlock() {
        Permanent blocker = addReadyCreature(player2);
        harness.setHand(player1, List.of(new CrosswayVampire()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = blocker.getId();
        harness.castCreature(player1, 0, 0, targetId);

        // Resolve creature spell → ETB on stack
        harness.passBothPriorities();

        // ETB triggered ability should be on stack
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getTargetPermanentId()).isEqualTo(targetId);

        // Resolve ETB
        harness.passBothPriorities();

        assertThat(blocker.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Target creature cannot declare as blocker after ETB resolves")
    void targetCannotDeclareAsBlocker() {
        Permanent attacker = addReadyCreature(player1);
        Permanent blocker = addReadyCreature(player2);

        harness.setHand(player1, List.of(new CrosswayVampire()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = blocker.getId();
        harness.castCreature(player1, 0, 0, targetId);

        // Resolve creature spell
        harness.passBothPriorities();
        // Resolve ETB
        harness.passBothPriorities();

        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can cast without target when no creatures on battlefield")
    void canCastWithoutTargetWhenNoCreatures() {
        harness.setHand(player1, List.of(new CrosswayVampire()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Crossway Vampire"));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB fizzles if target creature is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CrosswayVampire()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castCreature(player1, 0, 0, targetId);

        // Resolve creature spell → ETB on stack
        harness.passBothPriorities();

        // Remove target before ETB resolves
        gd.playerBattlefields.get(player2.getId()).clear();

        // Resolve ETB → fizzles
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    private Permanent addReadyCreature(Player player) {
        GrizzlyBears card = new GrizzlyBears();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
