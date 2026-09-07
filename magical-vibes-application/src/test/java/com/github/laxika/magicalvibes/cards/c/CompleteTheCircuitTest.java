package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CompleteTheCircuit.class, Divination.class, GrizzlyBears.class, LightningBolt.class})
class CompleteTheCircuitTest extends BaseCardTest {

    @Test
    @DisplayName("allows sorcery spells to be cast as though they had flash this turn")
    void grantsFlashToSorcerySpells() {
        resolveCompleteTheCircuit();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Divination"));
    }

    @Test
    @DisplayName("does not grant flash to creature spells")
    void doesNotGrantFlashToCreatureSpells() {
        resolveCompleteTheCircuit();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("copies the next instant or sorcery spell twice")
    void copiesNextInstantOrSorceryTwice() {
        resolveCompleteTheCircuit();

        LightningBolt bolt = new LightningBolt();
        harness.setHand(player1, List.of(bolt));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount).doesNotContainKey(player1.getId());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(2);

        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(11);
    }

    @Test
    @DisplayName("does not copy a creature spell")
    void doesNotCopyCreatureSpell() {
        resolveCompleteTheCircuit();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack).noneMatch(StackEntry::isCopy);
        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount.get(player1.getId())).isEqualTo(2);
    }

    private void resolveCompleteTheCircuit() {
        harness.setHand(player1, List.of(new CompleteTheCircuit()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
