package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GoblinBrigand;
import com.github.laxika.magicalvibes.cards.l.LongTermPlans;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReapingTheGraves.class, GoblinBrigand.class, LongTermPlans.class})
class ReapingTheGravesTest extends BaseCardTest {

    @Test
    void returnsTargetCreatureCardFromGraveyardToHand() {
        Card creature = new GoblinBrigand();
        harness.setGraveyard(player1, List.of(creature));
        castReapingTheGraves(creature.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Goblin Brigand");
        harness.assertInGraveyard(player1, "Reaping the Graves");
    }

    @Test
    void cannotTargetNonCreatureCardInGraveyard() {
        Card instant = new LongTermPlans();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new ReapingTheGraves()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, instant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetCreatureCardInOpponentsGraveyard() {
        Card creature = new GoblinBrigand();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new ReapingTheGraves()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void stormCopiesForEachSpellCastBeforeItThisTurn() {
        gd.recordSpellCast(player1.getId(), new GoblinBrigand());
        gd.recordSpellCast(player2.getId(), new GoblinBrigand());

        Card creature = new GoblinBrigand();
        harness.setGraveyard(player1, List.of(creature));
        castReapingTheGraves(creature.getId());

        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
    }

    @Test
    void stormCopyCanBeRetargetedToAnotherCreatureCardInGraveyard() {
        Card originalTarget = new GoblinBrigand();
        Card newTarget = new GoblinBrigand();
        harness.setGraveyard(player1, List.of(originalTarget, newTarget));
        gd.recordSpellCast(player1.getId(), new GoblinBrigand());

        castReapingTheGraves(originalTarget.getId());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice retargetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(retargetChoice).isNotNull();
        assertThat(retargetChoice.validIds()).contains(newTarget.getId());

        harness.handlePermanentChosen(player1, newTarget.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(originalTarget.getId(), newTarget.getId());
    }

    private void castReapingTheGraves(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new ReapingTheGraves()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, targetId);
    }
}
