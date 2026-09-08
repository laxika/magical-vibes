package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BreachTheMultiverse.class, GrizzlyBears.class, Island.class})
class BreachTheMultiverseTest extends BaseCardTest {

    @Test
    void millsEachPlayerAndReturnsOneCardFromEachGraveyardUnderItsController() {
        Card ownCreature = new GrizzlyBears();
        Card opposingCreature = new GrizzlyBears();
        harness.setLibrary(player1, tenIslands());
        harness.setLibrary(player2, tenIslands());
        harness.setGraveyard(player1, List.of(ownCreature));
        harness.setGraveyard(player2, List.of(opposingCreature));
        harness.setHand(player1, List.of(new BreachTheMultiverse()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        chooseOnlyCardFromCurrentGraveyard();
        chooseOnlyCardFromCurrentGraveyard();

        Permanent ownPermanent = findPermanentByCardId(ownCreature.getId());
        Permanent opposingPermanent = findPermanentByCardId(opposingCreature.getId());
        assertThat(GameQueryService.permanentHasSubtype(ownPermanent, CardSubtype.PHYREXIAN)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(opposingPermanent, CardSubtype.PHYREXIAN)).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(opposingCreature.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void doesNotReturnNonCreatureOrPlaneswalkerCards() {
        harness.setLibrary(player1, tenIslands());
        harness.setLibrary(player2, tenIslands());
        harness.setGraveyard(player1, List.of(new Island()));
        harness.setGraveyard(player2, List.of(new Island()));
        harness.setHand(player1, List.of(new BreachTheMultiverse()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getClass().equals(Island.class));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getClass().equals(Island.class));
    }

    private void chooseOnlyCardFromCurrentGraveyard() {
        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.cardPool()).hasSize(1);
        harness.handleGraveyardCardChosen(player1, 0);
    }

    private Permanent findPermanentByCardId(UUID cardId) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(cardId))
                .findFirst()
                .orElseThrow();
    }

    private List<Card> tenIslands() {
        return java.util.stream.IntStream.range(0, 10)
                .mapToObj(index -> (Card) new Island())
                .toList();
    }
}
