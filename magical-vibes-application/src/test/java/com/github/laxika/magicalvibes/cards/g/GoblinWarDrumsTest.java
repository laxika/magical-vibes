package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoblinWarDrumsTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Goblin War Drums puts it on the stack as an enchantment spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new GoblinWarDrums()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
    }

    @Test
    @DisplayName("Creatures you control gain menace")
    void ownCreaturesGainMenace() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GoblinWarDrums());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Opponent creatures do not gain menace")
    void opponentCreaturesDoNotGainMenace() {
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new GoblinWarDrums());

        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Menace bonus is removed when Goblin War Drums leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GoblinWarDrums());
        assertThat(gqs.hasKeyword(gd, bears, Keyword.MENACE)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Goblin War Drums"));

        assertThat(gqs.hasKeyword(gd, bears, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Granted menace stops a single blocker")
    void grantedMenaceStopsSingleBlocker() {
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GoblinWarDrums());
        addCreatureReady(player2, new GrizzlyBears());

        attackWithFirstCreature();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked except by two or more creatures");
    }

    @Test
    @DisplayName("Granted menace allows two blockers")
    void grantedMenaceAllowsTwoBlockers() {
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GoblinWarDrums());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        attackWithFirstCreature();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId()).get(1).isBlocking()).isTrue();
    }

    private void attackWithFirstCreature() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));
        harness.passBothPriorities();
    }
}
