package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HobgoblinCaptain;
import com.github.laxika.magicalvibes.cards.i.ImprovisedWeaponry;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.t.TwinningStaff;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StormKingsThunder.class, LightningBolt.class, HobgoblinCaptain.class, ImprovisedWeaponry.class,
        TwinningStaff.class})
class StormKingsThunderTest extends BaseCardTest {

    @Test
    @DisplayName("Copies the next instant X times")
    void copiesNextInstantForX() {
        castStormKingsThunder(2);

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        harness.passBothPriorities();
        declineRetargetingCopies(2);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(11);
    }

    @Test
    @DisplayName("Waits through a creature spell for the next instant or sorcery")
    void ignoresCreatureSpell() {
        castStormKingsThunder(2);

        harness.setHand(player1, List.of(new HobgoblinCaptain()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        harness.passBothPriorities();
        declineRetargetingCopies(2);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(11);
    }

    @Test
    @DisplayName("Does not copy the next spell when X is zero")
    void zeroXCreatesNoCopies() {
        castStormKingsThunder(0);

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    private void castStormKingsThunder(int xValue) {
        harness.setHand(player1, List.of(new StormKingsThunder()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
        harness.castInstant(player1, 0, xValue, null);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("One delayed trigger creates all X copies")
    void createsOneDelayedTriggerForMultipleCopies() {
        castStormKingsThunder(3);

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    @DisplayName("Copies a sorcery including all its effects")
    void copiesSorcery() {
        castStormKingsThunder(1);

        harness.setHand(player1, List.of(new ImprovisedWeaponry()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        declineRetargetingCopies(1);
        resolveAllTriggers();

        harness.assertLife(player2, 16);
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(2);
    }

    @Test
    @DisplayName("Can choose a different target for a copy")
    void canRetargetCopy() {
        castStormKingsThunder(1);

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Only the next qualifying spell is copied")
    void doesNotCopySecondInstant() {
        castStormKingsThunder(1);

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        declineRetargetingCopies(1);
        resolveAllTriggers();

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 11);
    }

    private void declineRetargetingCopies(int count) {
        for (int i = 0; i < count; i++) {
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(player1, false);
        }
    }

    @Test
    @CardUsed(TwinningStaff.class)
    @DisplayName("Twinning Staff adds one copy to the entire copying event")
    void additionalCopyReplacementAppliesOnce() {
        castStormKingsThunder(2);
        harness.addToBattlefield(player1, new TwinningStaff());

        harness.setHand(player1, List.of(new ImprovisedWeaponry()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, player2.getId());

        for (int i = 0; i < 20 && (!gd.stack.isEmpty() || gd.interaction.isAwaitingInput()); i++) {
            resolveAllTriggers();
            if (gd.interaction.isAwaitingInput()) {
                assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
                harness.handleMayAbilityChosen(player1, false);
            }
        }

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 12);
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(4);
    }
}
