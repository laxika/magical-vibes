package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MaestrosInitiate.class, Island.class})
class MaestrosInitiateTest extends BaseCardTest {

    @Test
    void activationExilesSourceAndDrawsThenDiscards() {
        setDeck(player1, List.of(new Island(), new Island()));
        MaestrosInitiate initiate = new MaestrosInitiate();
        harness.setGraveyard(player1, List.of(initiate));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateGraveyardAbility(player1, 0);

        harness.assertNotInGraveyard(player1, "Maestros Initiate");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(initiate);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    void cannotActivateWithoutEnoughMana() {
        MaestrosInitiate initiate = new MaestrosInitiate();
        harness.setGraveyard(player1, List.of(initiate));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Maestros Initiate");
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
