package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MonssGoblinRaiders;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChokingVinesTest extends BaseCardTest {

    private void giveSpell(int mana) {
        harness.setHand(player2, List.of(new ChokingVines()));
        harness.addMana(player2, ManaColor.GREEN, mana);
    }

    @Test
    @DisplayName("X=2 makes both targeted attackers blocked and deals 1 damage to each")
    void blocksAndDamagesEachTarget() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears()); // a possible blocker keeps combat from auto-resolving
        declareAttackers(List.of(0, 1));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        giveSpell(3); // X=2: {2}{G}

        harness.castInstantForX(player2, 0, 2, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.isBlockedWithoutBlockers()).isTrue();
        assertThat(second.isBlockedWithoutBlockers()).isTrue();
        assertThat(first.getMarkedDamage()).isEqualTo(1);
        assertThat(second.getMarkedDamage()).isEqualTo(1);

        resolveCombat();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The 1 damage kills a targeted 1/1 attacker")
    void damageKillsSmallAttacker() {
        Permanent goblin = addCreatureReady(player1, new MonssGoblinRaiders());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(List.of(0));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        giveSpell(2); // X=1: {1}{G}

        harness.castInstantForX(player2, 0, 1, List.of(goblin.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mons's Goblin Raiders");
    }

    @Test
    @DisplayName("An already blocked attacker is a legal target and just takes the damage")
    void alreadyBlockedAttackerTakesDamage() {
        Permanent blockedAttacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(List.of(0));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.clearPriorityPassed();
        giveSpell(2); // X=1

        harness.castInstantForX(player2, 0, 1, List.of(blockedAttacker.getId()));
        harness.passBothPriorities();

        assertThat(blockedAttacker.isBlockedWithoutBlockers()).isFalse();
        assertThat(blockedAttacker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot be cast outside the declare blockers step")
    void cannotCastOutsideDeclareBlockers() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        declareAttackers(List.of(0));

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        giveSpell(2);

        assertThatThrownBy(() -> harness.castInstantForX(player2, 0, 1, List.of(attacker.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot target a creature that isn't attacking")
    void cannotTargetNonAttacker() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent bystander = addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(List.of(0));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        giveSpell(2);

        assertThatThrownBy(() -> harness.castInstantForX(player2, 0, 1, List.of(bystander.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an attacking creature");
    }
}
