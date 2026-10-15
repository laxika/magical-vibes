package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.t.TravelersAmulet;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScavengingHarpy.class, TravelersAmulet.class})
class ScavengingHarpyTest extends BaseCardTest {

    private void castScavengingHarpy() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new ScavengingHarpy(), "{2}{B}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB exiles a targeted card from an opponent's graveyard")
    void etbExilesOpponentGraveyardCard() {
        Card harpy = new ScavengingHarpy();
        harness.setGraveyard(player2, List.of(harpy));

        castScavengingHarpy();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(harpy.getId()));
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Scavenging Harpy");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(harpy.getId()));
    }

    @Test
    @DisplayName("A card in the controller's own graveyard is not a legal target")
    void ownGraveyardCardNotTargetable() {
        Card harpy = new ScavengingHarpy();
        harness.setGraveyard(player1, List.of(harpy));

        castScavengingHarpy();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Scavenging Harpy");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("ETB must choose a target when an opponent has a graveyard card")
    void cannotDeclineMandatoryTarget() {
        Card harpy = new ScavengingHarpy();
        harness.setGraveyard(player2, List.of(harpy));

        castScavengingHarpy();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(harpy.getId()));
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Scavenging Harpy");
    }

    @Test
    @DisplayName("ETB can exile a noncreature card and leaves unchosen cards alone")
    void exilesOnlyChosenNoncreatureCard() {
        Card amulet = new TravelersAmulet();
        Card harpy = new ScavengingHarpy();
        harness.setGraveyard(player2, List.of(amulet, harpy));

        castScavengingHarpy();
        harness.handleMultipleCardsChosen(player1, List.of(amulet.getId()));

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(amulet, harpy);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(harpy);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(amulet);
    }

    @Test
    @DisplayName("ETB does not choose a replacement when its target leaves the graveyard")
    void targetLeavingGraveyardDoesNotExileAnotherCard() {
        Card amulet = new TravelersAmulet();
        Card harpy = new ScavengingHarpy();
        harness.setGraveyard(player2, List.of(amulet, harpy));

        castScavengingHarpy();
        harness.handleMultipleCardsChosen(player1, List.of(amulet.getId()));
        harness.setGraveyard(player2, List.of(harpy));
        harness.setHand(player2, List.of(amulet));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(harpy);
        harness.assertInHand(player2, "Traveler's Amulet");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }
}
