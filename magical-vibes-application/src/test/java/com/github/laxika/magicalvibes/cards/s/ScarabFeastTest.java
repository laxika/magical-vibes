package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DuneBeetle;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScarabFeast.class, DuneBeetle.class, GiantSpider.class})
class ScarabFeastTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles up to three chosen cards from a single graveyard")
    void exilesThreeFromOneGraveyard() {
        Card a = new DuneBeetle();
        Card b = new DuneBeetle();
        Card c = new GiantSpider();
        harness.setGraveyard(player1, List.of(a, b, c));
        harness.setHand(player1, List.of(new ScarabFeast()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0);
        List<UUID> targets = List.of(a.getId(), b.getId(), c.getId());
        harness.handleMultipleCardsChosen(player1, targets);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> targets.contains(card.getId()));
        assertThat(gd.exiledCards.stream().map(e -> e.card().getId()))
                .contains(a.getId(), b.getId(), c.getId());
    }

    @Test
    @DisplayName("Can exile cards from an opponent's graveyard")
    void exilesFromOpponentGraveyard() {
        Card opponentCard = new DuneBeetle();
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.setHand(player1, List.of(new ScarabFeast()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(opponentCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(opponentCard.getId()));
        assertThat(gd.exiledCards.stream().map(e -> e.card().getId())).contains(opponentCard.getId());
    }

    @Test
    @DisplayName("Choosing fewer than three targets leaves the rest in the graveyard")
    void choosingFewerLeavesRest() {
        Card chosen = new DuneBeetle();
        Card left = new GiantSpider();
        harness.setGraveyard(player1, List.of(chosen, left));
        harness.setHand(player1, List.of(new ScarabFeast()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(left.getId()))
                .noneMatch(card -> card.getId().equals(chosen.getId()));
    }

    @Test
    @DisplayName("Targets must all come from a single graveyard")
    void rejectsTargetsAcrossTwoGraveyards() {
        Card mine = new DuneBeetle();
        Card theirs = new GiantSpider();
        harness.setGraveyard(player1, List.of(mine));
        harness.setGraveyard(player2, List.of(theirs));
        harness.setHand(player1, List.of(new ScarabFeast()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0);
        // Both graveyards' cards are legal targets, but picking one from each is illegal.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .contains(mine.getId(), theirs.getId());

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(mine.getId(), theirs.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new ScarabFeast()));
        harness.setLibrary(player1, List.of(new DuneBeetle()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Scarab Feast");
        harness.assertInHand(player1, "Dune Beetle");
    }

    @Test
    void cyclingDiscardsAsCostBeforeDrawing() {
        harness.setHand(player1, List.of(new ScarabFeast()));
        harness.setLibrary(player1, List.of(new DuneBeetle()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Scarab Feast");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Dune Beetle");
    }

    @Test
    void canChooseZeroTargetsWhenCardsAreAvailable() {
        Card untouched = new DuneBeetle();
        harness.setGraveyard(player2, List.of(untouched));
        harness.setHand(player1, List.of(new ScarabFeast()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(untouched);
        assertThat(gd.exiledCards).isEmpty();
        harness.assertInGraveyard(player1, "Scarab Feast");
    }

    @Test
    void canCastWithEmptyGraveyards() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        harness.setHand(player1, List.of(new ScarabFeast()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.exiledCards).isEmpty();
        harness.assertInGraveyard(player1, "Scarab Feast");
    }

    @Test
    void rejectsMoreThanThreeTargets() {
        Card a = new DuneBeetle();
        Card b = new DuneBeetle();
        Card c = new GiantSpider();
        Card d = new GiantSpider();
        harness.setGraveyard(player2, List.of(a, b, c, d));
        harness.setHand(player1, List.of(new ScarabFeast()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(a.getId(), b.getId(), c.getId(), d.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canExileNoncreatureCards() {
        Card instant = new ScarabFeast();
        harness.setGraveyard(player2, List.of(instant));
        harness.setHand(player1, List.of(new ScarabFeast()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(e -> e.card().getId())).containsExactly(instant.getId());
    }

    @Test
    void exilesRemainingLegalTargetAfterAnotherTargetLeaves() {
        Card removed = new DuneBeetle();
        Card remaining = new GiantSpider();
        harness.setGraveyard(player2, List.of(removed, remaining));
        harness.setHand(player1, List.of(new ScarabFeast(), new ScarabFeast()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(removed.getId(), remaining.getId()));
        harness.castInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(removed.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(remaining);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(e -> e.card().getId()))
                .containsExactlyInAnyOrder(removed.getId(), remaining.getId());
    }
}
