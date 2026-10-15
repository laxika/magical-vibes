package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.v.ValMaroonedSurveyor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.networking.message.GameStateMessage;
import com.github.laxika.magicalvibes.service.JacksonConfig;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TezzeretsReckoning.class, GrizzlyBears.class, Island.class, ValMaroonedSurveyor.class})
class TezzeretsReckoningTest extends BaseCardTest {

    @Test
    void exilesThreeCardsFaceDownFromLibrary() {
        List<Card> library = List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        resolveReckoning(library);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(library);
        assertThat(gd.exiledCards)
                .filteredOn(entry -> library.contains(entry.card()))
                .allMatch(ExiledCardEntry::faceDown);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void permissionSurvivesResolutionAndAllowsExactlyOneNormalCostCast() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        resolveReckoning(List.of(first, second, third));

        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, first.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castFromExile(player1, second.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No permission to play this exiled card");
    }

    @Test
    void exilesAllAvailableCardsWhenLibraryHasFewerThanThree() {
        List<Card> library = List.of(new GrizzlyBears(), new GrizzlyBears());
        resolveReckoning(library);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(library);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void emptyLibraryDoesNotPreventResolution() {
        resolveReckoning(List.of());

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Tezzeret's Reckoning");
    }

    @Test
    void playingLandConsumesTheOneCardPermission() {
        Card land = new Island();
        Card creature = new GrizzlyBears();
        resolveReckoning(List.of(land, creature));

        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player1, land.getId());

        harness.assertOnBattlefield(player1, "Island");
        harness.addMana(player1, ManaColor.GREEN, 2);
        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No permission to play this exiled card");
    }

    @Test
    void controllerCanLookAtAllExiledCardsWithoutMana() throws Exception {
        List<Card> library = List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        resolveReckoning(library);
        harness.publishState();

        GameStateMessage controllerState = new JacksonConfig().objectMapper().readValue(harness.getConn1()
                .getMessagesContaining("\"type\":\"GAME_STATE\"").getLast(), GameStateMessage.class);
        GameStateMessage opponentState = new JacksonConfig().objectMapper().readValue(harness.getConn2()
                .getMessagesContaining("\"type\":\"GAME_STATE\"").getLast(), GameStateMessage.class);

        assertThat(controllerState.lookedAtExileCards()).extracting(card -> card.id())
                .containsExactlyInAnyOrderElementsOf(library.stream().map(Card::getId).toList());
        assertThat(opponentState.lookedAtExileCards()).isEmpty();
    }

    @Test
    void randomExileDoesNotTriggerSeekAbilities() {
        harness.addToBattlefield(player1, new ValMaroonedSurveyor());
        resolveReckoning(List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private void resolveReckoning(List<? extends Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new TezzeretsReckoning()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
