package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.e.ExpeditionLookout;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CompleteTheCircuit.class, Divination.class, ExpeditionLookout.class, GrizzlyBears.class, LightningBolt.class})
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

        while (!gd.stack.isEmpty()) {
            if (gd.interaction.isAwaitingInput()) {
                harness.handleMayAbilityChosen(player1, false);
            } else {
                harness.passBothPriorities();
            }
        }

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
        resolveAllTriggers();
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        while (!gd.stack.isEmpty()) {
            if (gd.interaction.isAwaitingInput()) {
                harness.handleMayAbilityChosen(player1, false);
            } else {
                harness.passBothPriorities();
            }
        }
        harness.assertLife(player2, 11);
    }

    private void resolveCompleteTheCircuit() {
        harness.setHand(player1, List.of(new CompleteTheCircuit()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castAndResolveInstant(player1, 0);
    }

    @Test
    @DisplayName("copying twice is a single delayed triggered ability")
    void createsOneDelayedTriggerForBothCopies() {
        resolveCompleteTheCircuit();
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).filteredOn(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .hasSize(1);
    }

    @Test
    @DisplayName("a summoning sick blue creature can convoke the blue mana requirement")
    void convokePaysColoredManaWithSummoningSickCreature() {
        var lookout = harness.addToBattlefieldAndReturn(player1, new ExpeditionLookout());
        lookout.setSummoningSick(true);
        harness.setHand(player1, List.of(new CompleteTheCircuit()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castInstantWithConvoke(player1, 0, List.of(), List.of(lookout.getId()));
        harness.passBothPriorities();

        assertThat(lookout.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Complete the Circuit");
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.castSorcery(player1, 0, 0);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Divination"));
    }

    @Test
    @DisplayName("copies a nontargeted sorcery and does not copy the following spell")
    void copiesOnlyNextSorcery() {
        resolveCompleteTheCircuit();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, 0);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("each copy may choose a different target without changing the original")
    void copiesCanChooseDifferentTargets() {
        resolveCompleteTheCircuit();
        var originalTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        var firstCopyTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        var secondCopyTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, originalTarget.getId());

        var newTargets = List.of(firstCopyTarget.getId(), secondCopyTarget.getId());
        int choices = 0;
        while (!gd.stack.isEmpty()) {
            if (gd.interaction.isAwaitingInput()) {
                harness.handleMayAbilityChosen(player1, true);
                harness.handlePermanentChosen(player1, newTargets.get(choices++));
            } else {
                harness.passBothPriorities();
            }
        }

        assertThat(choices).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }
}
