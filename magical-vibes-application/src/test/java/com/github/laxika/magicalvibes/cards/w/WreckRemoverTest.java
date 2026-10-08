package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WreckRemover.class})
class WreckRemoverTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles up to one graveyard card and gains 1 life")
    void etbExilesCardAndGainsLife() {
        Card graveyardCard = new WreckRemover();
        harness.setGraveyard(player2, new ArrayList<>(List.of(graveyardCard)));
        WreckRemover wreckRemover = new WreckRemover();
        harness.setHand(player1, List.of(wreckRemover));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(graveyardCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).noneMatch(card -> card.getId().equals(graveyardCard.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId())).anyMatch(card -> card.getId().equals(graveyardCard.getId()));
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("ETB may exile no card and still gains 1 life")
    void etbMayChooseNoCardAndStillGainsLife() {
        Card graveyardCard = new WreckRemover();
        harness.setGraveyard(player2, new ArrayList<>(List.of(graveyardCard)));
        harness.setHand(player1, List.of(new WreckRemover()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).anyMatch(card -> card.getId().equals(graveyardCard.getId()));
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Attack trigger exiles up to one graveyard card and gains 1 life")
    void attackTriggerExilesCardAndGainsLife() {
        WreckRemover wreckRemover = new WreckRemover();
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, wreckRemover);
        permanent.setSummoningSick(false);
        Card graveyardCard = new WreckRemover();
        harness.setGraveyard(player2, new ArrayList<>(List.of(graveyardCard)));

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(graveyardCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).anyMatch(card -> card.getId().equals(graveyardCard.getId()));
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Cycling discards Wreck Remover and draws a card")
    void cyclingDrawsACard() {
        WreckRemover wreckRemover = new WreckRemover();
        Card graveyardCard = new WreckRemover();
        harness.setHand(player1, List.of(wreckRemover));
        harness.setLibrary(player1, List.of(graveyardCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(wreckRemover.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(graveyardCard.getId()));
    }

    @Test
    @DisplayName("ETB with empty graveyards still gains 1 life")
    void etbWithEmptyGraveyardsGainsLife() {
        harness.setHand(player1, List.of(new WreckRemover()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("ETB can exile a card from its controller's graveyard")
    void etbExilesOwnGraveyardCard() {
        Card graveyardCard = new WreckRemover();
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setHand(player1, List.of(new WreckRemover()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(graveyardCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(graveyardCard);
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("ETB gains no life when its only target leaves the graveyard")
    void etbWithRemovedTargetDoesNotGainLife() {
        Card graveyardCard = new WreckRemover();
        harness.setGraveyard(player2, List.of(graveyardCard));
        harness.setHand(player1, List.of(new WreckRemover()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(graveyardCard.getId()));
        harness.setGraveyard(player2, List.of());
        harness.setHand(player2, List.of(graveyardCard));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(graveyardCard);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Attack trigger may choose no card and still gains 1 life")
    void attackMayChooseNoCardAndStillGainsLife() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new WreckRemover());
        permanent.setSummoningSick(false);
        Card graveyardCard = new WreckRemover();
        harness.setGraveyard(player2, List.of(graveyardCard));

        declareAttackers(player1, List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(graveyardCard);
        harness.assertLife(player1, 21);
    }
}
