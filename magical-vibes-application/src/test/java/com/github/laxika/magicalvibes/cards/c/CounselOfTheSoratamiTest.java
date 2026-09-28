package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(CounselOfTheSoratami.class)
class CounselOfTheSoratamiTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Counsel of the Soratami puts it on the stack")
    void castingPutsOnStack() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.castFromHand(player1, counsel, "{2}{U}");

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard()).isSameAs(counsel);
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Resolving draws two cards")
    void resolvingDrawsTwoCards() {
        CounselOfTheSoratami firstDraw = new CounselOfTheSoratami();
        CounselOfTheSoratami secondDraw = new CounselOfTheSoratami();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.castFromHand(player1, new CounselOfTheSoratami(), "{2}{U}");

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Counsel of the Soratami goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.castFromHand(player1, counsel, "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(counsel);
    }

    @Test
    @DisplayName("Drawing with only one card in the library draws it, then loses the game")
    void drawsAvailableCardThenLosesOnEmptyLibrary() {
        CounselOfTheSoratami lastCard = new CounselOfTheSoratami();
        harness.setLibrary(player1, List.of(lastCard));
        harness.castFromHand(player1, new CounselOfTheSoratami(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(lastCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }
}

