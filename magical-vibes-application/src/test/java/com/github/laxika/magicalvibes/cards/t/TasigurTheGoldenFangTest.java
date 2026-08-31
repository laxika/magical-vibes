package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TasigurTheGoldenFangTest extends BaseCardTest {

    @Test
    @DisplayName("Delve exiles graveyard cards to pay for the creature")
    void delveExilesCardsFromGraveyard() {
        List<Card> graveyard = List.of(new Shock(), new Shock(), new Shock(), new Shock(), new Shock());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new TasigurTheGoldenFang()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1, 2, 3, 4));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(graveyard);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof TasigurTheGoldenFang);
    }

    @Test
    @DisplayName("Mills two cards, then the opponent chooses a nonland card to return")
    void millsThenOpponentChoosesCardToReturn() {
        Card existingCard = new Shock();
        harness.addToBattlefield(player1, new TasigurTheGoldenFang());
        harness.setGraveyard(player1, List.of(existingCard));
        harness.setLibrary(player1, List.of(new Shock(), new Forest()));
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        int existingCardIndex = choice.cardPool().indexOf(existingCard);
        assertThat(existingCardIndex).isGreaterThanOrEqualTo(0);

        harness.handleGraveyardCardChosen(player2, existingCardIndex);

        harness.assertInHand(player1, "Shock");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Does not return a land card")
    void doesNotReturnLandCard() {
        harness.addToBattlefield(player1, new TasigurTheGoldenFang());
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        harness.assertNotInHand(player1, "Forest");
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
    }
}
