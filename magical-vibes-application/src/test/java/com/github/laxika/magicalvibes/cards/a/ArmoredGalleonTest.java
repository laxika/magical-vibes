package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArmoredGalleon.class, Island.class})
class ArmoredGalleonTest extends BaseCardTest {

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Armored Galleon puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new ArmoredGalleon()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Resolving puts Armored Galleon onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new ArmoredGalleon()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Armored Galleon");
    }

    // ===== Attack restriction =====

    @Test
    @DisplayName("Armored Galleon can attack when defending player controls an Island")
    void canAttackWhenDefenderControlsIsland() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new Island());

        addCreatureReady(player1, new ArmoredGalleon());

        declareAttackers(List.of(0));

        // Combat auto-advances; 5/4 deals 5 damage when unblocked
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Armored Galleon cannot attack when defending player does not control an Island")
    void cannotAttackWhenDefenderDoesNotControlIsland() {
        addCreatureReady(player1, new ArmoredGalleon());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Armored Galleon cannot attack when only its controller controls an Island")
    void cannotAttackWhenOnlyControllerControlsIsland() {
        harness.addToBattlefield(player1, new Island());
        addCreatureReady(player1, new ArmoredGalleon());

        assertThatThrownBy(() -> declareAttackers(List.of(1)))
                .isInstanceOf(IllegalStateException.class);
    }
}
