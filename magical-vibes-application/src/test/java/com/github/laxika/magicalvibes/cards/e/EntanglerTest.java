package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EntanglerTest extends BaseCardTest {

    @Test
    @DisplayName("Entangler lets the enchanted creature block any number of attackers")
    void enchantedCreatureCanBlockAnyNumberOfAttackers() {
        Permanent blocker = addReadyCreature(player2);
        addReadyAttacker(player1);
        addReadyAttacker(player1);
        addReadyAttacker(player1);

        harness.setHand(player1, List.of(new Entangler()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castEnchantment(player1, 0, blocker.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1),
                new BlockerAssignment(0, 2)));

        assertThat(blocker.getBlockingTargets()).containsExactlyInAnyOrder(0, 1, 2);
    }

    @Test
    @DisplayName("Entangler cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        addReadyCreature(player2);
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        harness.setHand(player1, List.of(new Entangler()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addReadyCreature(Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private void addReadyAttacker(Player player) {
        Permanent attacker = addReadyCreature(player);
        attacker.setAttacking(true);
    }
}
