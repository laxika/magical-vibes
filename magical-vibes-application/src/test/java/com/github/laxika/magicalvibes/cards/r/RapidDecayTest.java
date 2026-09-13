package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GoliathBeetle;
import com.github.laxika.magicalvibes.cards.p.PlatedSpider;
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

@CardUsed({RapidDecay.class, GoliathBeetle.class, PlatedSpider.class})
class RapidDecayTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles up to three chosen cards from a single graveyard")
    void exilesThreeFromOneGraveyard() {
        Card first = new GoliathBeetle();
        Card second = new GoliathBeetle();
        Card third = new PlatedSpider();
        harness.setGraveyard(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new RapidDecay()));
        addSpellMana();

        harness.castInstant(player1, 0);
        List<UUID> targets = List.of(first.getId(), second.getId(), third.getId());
        harness.handleMultipleCardsChosen(player1, targets);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> targets.contains(card.getId()));
        assertThat(gd.exiledCards.stream().map(e -> e.card().getId()))
                .contains(first.getId(), second.getId(), third.getId());
    }

    @Test
    @DisplayName("Can exile cards from an opponent's graveyard")
    void exilesFromOpponentGraveyard() {
        Card opponentCard = new GoliathBeetle();
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.setHand(player1, List.of(new RapidDecay()));
        addSpellMana();

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
        Card chosen = new GoliathBeetle();
        Card left = new PlatedSpider();
        harness.setGraveyard(player1, List.of(chosen, left));
        harness.setHand(player1, List.of(new RapidDecay()));
        addSpellMana();

        harness.castInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(left.getId()))
                .noneMatch(card -> card.getId().equals(chosen.getId()));
    }

    @Test
    @DisplayName("May choose no cards")
    void mayChooseNoCards() {
        Card left = new GoliathBeetle();
        harness.setGraveyard(player1, List.of(left));
        harness.setHand(player1, List.of(new RapidDecay()));
        addSpellMana();

        harness.castInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(left);
        assertThat(gd.exiledCards.stream().map(e -> e.card().getId())).doesNotContain(left.getId());
    }

    @Test
    @DisplayName("Targets must all come from a single graveyard")
    void rejectsTargetsAcrossTwoGraveyards() {
        Card mine = new GoliathBeetle();
        Card theirs = new PlatedSpider();
        harness.setGraveyard(player1, List.of(mine));
        harness.setGraveyard(player2, List.of(theirs));
        harness.setHand(player1, List.of(new RapidDecay()));
        addSpellMana();

        harness.castInstant(player1, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .contains(mine.getId(), theirs.getId());

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(mine.getId(), theirs.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new RapidDecay()));
        harness.setLibrary(player1, List.of(new GoliathBeetle()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Rapid Decay");
        harness.assertInHand(player1, "Goliath Beetle");
    }

    private void addSpellMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
