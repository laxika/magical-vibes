package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingPileSeparation;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SplitTheSpoils.class, GrizzlyBears.class, LlanowarElves.class, Shock.class, SidisiBroodTyrant.class, Forest.class})
class SplitTheSpoilsTest extends BaseCardTest {

    @Test
    @DisplayName("targets only permanent cards from its controller's graveyard")
    void targetsOwnPermanentCardsOnly() {
        Card bears = new GrizzlyBears();
        Card elves = new LlanowarElves();
        Card instant = new Shock();
        Card opponentPermanent = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears, elves, instant));
        harness.setGraveyard(player2, List.of(opponentPermanent));
        cast();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(bears.getId(), elves.getId());
        assertThat(choice.validCardIds()).doesNotContain(instant.getId(), opponentPermanent.getId());
        assertThat(choice.maxCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("opponent chooses the hand pile and the other pile returns to the graveyard")
    void opponentChoosesPileForHand() {
        Card bears = new GrizzlyBears();
        Card elves = new LlanowarElves();
        harness.setGraveyard(player1, List.of(bears, elves));
        cast();

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(elves);
        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isFalse();
    }

    @Test
    @DisplayName("choosing the other pile puts that pile into hand")
    void opponentChoosesOtherPileForHand() {
        Card bears = new GrizzlyBears();
        Card elves = new LlanowarElves();
        harness.setGraveyard(player1, List.of(bears, elves));
        cast();

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), elves.getId()));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).contains(elves);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears);
    }

    @Test
    void canChooseZeroTargetsWithEligibleCards() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        cast();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isFalse();
    }

    @Test
    void canCastWithNoPermanentCardsInGraveyard() {
        Card instant = new Shock();
        harness.setGraveyard(player1, List.of(instant));
        cast();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(instant);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isFalse();
    }

    @Test
    void opponentCanChooseEmptyPile() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        cast();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears);
        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isFalse();
    }

    @Test
    void opponentCanChooseAllCardsWithOtherPileEmpty() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        cast();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(bears);
    }

    @Test
    void resolvesForRemainingTargetWhenAnotherLeavesGraveyard() {
        Card bears = new GrizzlyBears();
        Card elves = new LlanowarElves();
        harness.setGraveyard(player1, List.of(bears, elves));
        cast();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), elves.getId()));
        harness.setGraveyard(player1, List.of(elves));
        harness.setExile(player1, List.of(bears));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactly(elves.getId());
        harness.handleMultipleCardsChosen(player1, List.of(elves.getId()));
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(elves);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(bears, elves);
    }

    @Test
    void doesNotSeparatePilesWhenAllTargetsLeaveGraveyard() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        cast();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(bears));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isFalse();
    }

    @Test
    void returningUnchosenCreatureFromExileDoesNotTriggerSidisi() {
        harness.addToBattlefield(player1, new SidisiBroodTyrant());
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        cast();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(countPermanents(player1, "Zombie")).isZero();
    }
    @Test
    void targetsAtMostFivePermanentCardsIncludingLands() {
        Card land = new Forest();
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        Card fourth = new GrizzlyBears();
        Card unselected = new GrizzlyBears();
        List<Card> selected = List.of(land, first, second, third, fourth);
        harness.setGraveyard(player1, List.of(land, first, second, third, fourth, unselected));
        cast();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).contains(land.getId(), unselected.getId()).hasSize(6);
        assertThat(choice.maxCount()).isEqualTo(5);
        List<java.util.UUID> selectedIds = selected.stream().map(Card::getId).toList();
        harness.handleMultipleCardsChosen(player1, selectedIds);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, selectedIds);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrderElementsOf(selected);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(unselected);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(land, first, second, third, fourth);
    }
    private void cast() {
        harness.setHand(player1, List.of(new SplitTheSpoils()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
    }
}
