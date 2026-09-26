package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DeathmaskNezumi;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GodosIrregulars.class, DeathmaskNezumi.class})
class GodosIrregularsTest extends BaseCardTest {

    @Test
    @DisplayName("{R}: deals 1 damage to a creature blocking Godo's Irregulars")
    void damagesBlocker() {
        addCreatureReady(player1, new GodosIrregulars());
        Permanent blocker = addCreatureReady(player2, new DeathmaskNezumi());

        blockIrregulars();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability can be activated repeatedly for more damage")
    void damageStacksAcrossActivations() {
        addCreatureReady(player1, new GodosIrregulars());
        Permanent blocker = addCreatureReady(player2, new DeathmaskNezumi());

        blockIrregulars();
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 0, 0, null, blocker.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Only the chosen creature is damaged when multiple creatures block")
    void damagesOnlyChosenBlocker() {
        addCreatureReady(player1, new GodosIrregulars());
        Permanent firstBlocker = addCreatureReady(player2, new DeathmaskNezumi());
        Permanent chosenBlocker = addCreatureReady(player2, new DeathmaskNezumi());

        blockIrregulars(List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, 0, null, chosenBlocker.getId());
        harness.passBothPriorities();

        assertThat(firstBlocker.getMarkedDamage()).isZero();
        assertThat(chosenBlocker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a creature that isn't blocking Godo's Irregulars")
    void cannotTargetNonBlocker() {
        addCreatureReady(player1, new GodosIrregulars());
        addCreatureReady(player2, new DeathmaskNezumi());
        Permanent bystander = addCreatureReady(player2, new DeathmaskNezumi());

        blockIrregulars();
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bystander.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature blocking another attacker")
    void cannotTargetCreatureBlockingAnotherAttacker() {
        addCreatureReady(player1, new GodosIrregulars());
        addCreatureReady(player1, new DeathmaskNezumi());
        addCreatureReady(player2, new DeathmaskNezumi());
        Permanent otherBlocker = addCreatureReady(player2, new DeathmaskNezumi());

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 1)
        ));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, otherBlocker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void blockIrregulars() {
        blockIrregulars(List.of(new BlockerAssignment(0, 0)));
    }

    private void blockIrregulars(List<BlockerAssignment> blockerAssignments) {
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, blockerAssignments);
    }
}
