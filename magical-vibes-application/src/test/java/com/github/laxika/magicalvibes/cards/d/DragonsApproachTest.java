package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.l.LoreholdExcavation;
import com.github.laxika.magicalvibes.cards.v.VelomachusLorehold;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DragonsApproach.class, VelomachusLorehold.class, LoreholdExcavation.class})
class DragonsApproachTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage to each opponent and can exile four same-name cards to search for a Dragon")
    void dealsDamageAndExilesCardsToSearch() {
        Card approach = new DragonsApproach();
        List<Card> graveyardApproaches = List.of(
                new DragonsApproach(), new DragonsApproach(), new DragonsApproach(), new DragonsApproach());
        Card dragon = new VelomachusLorehold();
        Card nonDragon = new LoreholdExcavation();
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, graveyardApproaches);
        harness.setLibrary(player1, List.of(dragon, nonDragon));
        harness.setHand(player1, List.of(approach));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0);
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

        harness.handleCardChosen(player1, 0);

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

        harness.castAndResolveSorcery(player1, 0, 0);
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
        harness.setLibrary(player1, List.of(new VelomachusLorehold()));
        harness.setHand(player1, List.of(approach));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(approach.getId(),
                        graveyardApproaches.get(0).getId(), graveyardApproaches.get(1).getId(),
                        graveyardApproaches.get(2).getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void exilesTheResolvingSpellBeforeSearching() {
        Card approach = new DragonsApproach();
        List<Card> graveyardApproaches = List.of(
                new DragonsApproach(), new DragonsApproach(), new DragonsApproach(), new DragonsApproach());
        harness.setGraveyard(player1, graveyardApproaches);
        harness.setLibrary(player1, List.of(new VelomachusLorehold()));
        harness.setHand(player1, List.of(approach));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, graveyardApproaches.stream().map(Card::getId).toList());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId).contains(approach.getId());
    }

    @Test
    void exilesOnlyFourChosenCopiesAndCanFailToFind() {
        Card approach = new DragonsApproach();
        Card spare = new DragonsApproach();
        Card unrelated = new LoreholdExcavation();
        List<Card> chosen = List.of(
                new DragonsApproach(), new DragonsApproach(), new DragonsApproach(), new DragonsApproach());
        harness.setGraveyard(player1, List.of(chosen.get(0), chosen.get(1), chosen.get(2), chosen.get(3), spare, unrelated));
        Card opposingCopy = new DragonsApproach();
        harness.setGraveyard(player2, List.of(opposingCopy));
        Card dragon = new VelomachusLorehold();
        harness.setLibrary(player1, List.of(dragon));
        harness.setHand(player1, List.of(approach));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 17);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .contains(chosen.get(0).getId(), chosen.get(1).getId(), chosen.get(2).getId(), chosen.get(3).getId(), spare.getId())
                .doesNotContain(unrelated.getId(), opposingCopy.getId());
        harness.handleMultipleCardsChosen(player1, chosen.stream().map(Card::getId).toList());
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(spare.getId(), unrelated.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).extracting(Card::getId)
                .containsExactly(opposingCopy.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(approach.getId(), chosen.get(0).getId(), chosen.get(1).getId(), chosen.get(2).getId(), chosen.get(3).getId());
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId).containsExactly(dragon.getId());
        harness.assertNotOnBattlefield(player1, "Velomachus Lorehold");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void stillExilesFiveCardsWhenLibraryHasNoDragon() {
        Card approach = new DragonsApproach();
        List<Card> chosen = List.of(
                new DragonsApproach(), new DragonsApproach(), new DragonsApproach(), new DragonsApproach());
        Card nonDragon = new LoreholdExcavation();
        harness.setGraveyard(player1, chosen);
        harness.setLibrary(player1, List.of(nonDragon));
        harness.setHand(player1, List.of(approach));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, chosen.stream().map(Card::getId).toList());

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(approach.getId(), chosen.get(0).getId(), chosen.get(1).getId(), chosen.get(2).getId(), chosen.get(3).getId());
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId).containsExactly(nonDragon.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
