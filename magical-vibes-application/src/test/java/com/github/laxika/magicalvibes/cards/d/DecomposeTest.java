package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AegisOfHonor;
import com.github.laxika.magicalvibes.cards.a.AngelicWall;
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

@CardUsed({Decompose.class, AegisOfHonor.class, AngelicWall.class})
class DecomposeTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles up to three chosen cards from a single graveyard")
    void exilesThreeFromOneGraveyard() {
        Card a = new AegisOfHonor();
        Card b = new AegisOfHonor();
        Card c = new AngelicWall();
        harness.setGraveyard(player1, List.of(a, b, c));
        harness.setHand(player1, List.of(new Decompose()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0);
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
        Card opponentCard = new AegisOfHonor();
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.setHand(player1, List.of(new Decompose()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(opponentCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(opponentCard.getId()));
        assertThat(gd.exiledCards.stream().map(e -> e.card().getId())).contains(opponentCard.getId());
    }

    @Test
    @DisplayName("Choosing fewer than three targets leaves the rest in the graveyard")
    void choosingFewerLeavesRest() {
        Card chosen = new AegisOfHonor();
        Card left = new AngelicWall();
        harness.setGraveyard(player1, List.of(chosen, left));
        harness.setHand(player1, List.of(new Decompose()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(left.getId()))
                .noneMatch(card -> card.getId().equals(chosen.getId()));
    }

    @Test
    @DisplayName("May choose no cards")
    void mayChooseNoCards() {
        Card left = new AngelicWall();
        harness.setGraveyard(player1, List.of(left));
        harness.setHand(player1, List.of(new Decompose()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(left);
        assertThat(gd.exiledCards.stream().map(e -> e.card().getId())).doesNotContain(left.getId());
    }

    @Test
    @DisplayName("Targets must all come from a single graveyard")
    void rejectsTargetsAcrossTwoGraveyards() {
        Card mine = new AegisOfHonor();
        Card theirs = new AngelicWall();
        harness.setGraveyard(player1, List.of(mine));
        harness.setGraveyard(player2, List.of(theirs));
        harness.setHand(player1, List.of(new Decompose()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .contains(mine.getId(), theirs.getId());

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(mine.getId(), theirs.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
