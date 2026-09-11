package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RootwaterMystic.class, RagingGoblin.class})
class RootwaterMysticTest extends BaseCardTest {

    @Test
    @DisplayName("Looks at the top card of the target player's library and leaves it on top")
    void looksAtTargetPlayersTopCard() {
        addCreatureReady(player1, new RootwaterMystic());
        Card topCard = new RagingGoblin();
        Card secondCard = new RagingGoblin();
        harness.setLibrary(player2, List.of(topCard, secondCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(topCard);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard, secondCard);
    }

    @Test
    @DisplayName("Can target its controller's library")
    void canTargetController() {
        addCreatureReady(player1, new RootwaterMystic());
        Card topCard = new RagingGoblin();
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(topCard);
    }

    @Test
    @DisplayName("Does nothing when the target player's library is empty")
    void doesNothingForEmptyLibrary() {
        addCreatureReady(player1, new RootwaterMystic());
        harness.setLibrary(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Rejects a permanent as the target")
    void rejectsPermanentTarget() {
        addCreatureReady(player1, new RootwaterMystic());
        harness.addToBattlefield(player2, new RagingGoblin());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, null, harness.getPermanentId(player2, "Raging Goblin")))
                .isInstanceOf(IllegalStateException.class);
    }
}
