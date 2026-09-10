package com.github.laxika.magicalvibes.cards.g;

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

@CardUsed({GollumTheAbandoned.class, GrizzlyBears.class})
class GollumTheAbandonedTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles up to one card from an opponent's graveyard and each opponent loses 2 life")
    void etbExilesCardAndEachOpponentLosesLife() {
        Card graveyardCard = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(graveyardCard)));
        harness.setHand(player1, List.of(new GollumTheAbandoned()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(graveyardCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).noneMatch(card -> card.getId().equals(graveyardCard.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId())).anyMatch(card -> card.getId().equals(graveyardCard.getId()));
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("ETB may exile no card and still makes each opponent lose 2 life")
    void etbMayExileNoCardAndStillMakesEachOpponentLoseLife() {
        Card graveyardCard = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(graveyardCard)));
        harness.setHand(player1, List.of(new GollumTheAbandoned()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(graveyardCard);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The sorcery ability may sacrifice Gollum itself and return it to its owner's hand")
    void abilityMaySacrificeGollumItselfAndReturnItToHand() {
        Permanent gollum = harness.addToBattlefieldAndReturn(player1, new GollumTheAbandoned());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(gollum);
        assertThat(gd.playerHands.get(player1.getId())).contains(gollum.getCard());
    }
}
