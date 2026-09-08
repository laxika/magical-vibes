package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DragonsApproachTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage to each opponent and can exile four same-name cards to search for a Dragon")
    void dealsDamageAndExilesCardsToSearch() {
        Card approach = new DragonsApproach();
        List<Card> graveyardApproaches = List.of(
                new DragonsApproach(), new DragonsApproach(), new DragonsApproach(), new DragonsApproach());
        Card dragon = new ShivanDragon();
        Card nonDragon = new GrizzlyBears();
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, graveyardApproaches);
        harness.setLibrary(player1, List.of(dragon, nonDragon));
        harness.setHand(player1, List.of(approach));
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);

        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyElementsOf(
                graveyardApproaches.stream().map(Card::getId).toList());

        harness.handleMultipleCardsChosen(player1,
                graveyardApproaches.stream().map(Card::getId).toList());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(Card::getId)
                .containsExactly(dragon.getId());

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(dragon.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(approach.getId())
                        || graveyardApproaches.stream().anyMatch(other -> other.getId().equals(card.getId())));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(
                        approach.getId(),
                        graveyardApproaches.get(0).getId(),
                        graveyardApproaches.get(1).getId(),
                        graveyardApproaches.get(2).getId(),
                        graveyardApproaches.get(3).getId());
    }

    @Test
    @DisplayName("Declining the optional exile leaves the spell and graveyard cards in place")
    void mayBeDeclined() {
        Card approach = new DragonsApproach();
        List<Card> graveyardApproaches = List.of(
                new DragonsApproach(), new DragonsApproach(), new DragonsApproach(), new DragonsApproach());
        harness.setGraveyard(player1, graveyardApproaches);
        harness.setHand(player1, List.of(approach));
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(approach.getId(),
                        graveyardApproaches.get(0).getId(), graveyardApproaches.get(1).getId(),
                        graveyardApproaches.get(2).getId(), graveyardApproaches.get(3).getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Accepting without four same-name graveyard cards does not search")
    void acceptingWithoutFourCardsDoesNothing() {
        Card approach = new DragonsApproach();
        List<Card> graveyardApproaches = List.of(new DragonsApproach(), new DragonsApproach(), new DragonsApproach());
        harness.setGraveyard(player1, graveyardApproaches);
        harness.setLibrary(player1, List.of(new ShivanDragon()));
        harness.setHand(player1, List.of(approach));
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(approach.getId(),
                        graveyardApproaches.get(0).getId(), graveyardApproaches.get(1).getId(),
                        graveyardApproaches.get(2).getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
