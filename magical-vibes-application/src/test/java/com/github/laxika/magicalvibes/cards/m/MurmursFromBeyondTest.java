package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MurmursFromBeyondTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent chooses one revealed card for the graveyard and the rest go to hand")
    void opponentChoosesGraveyardCard() {
        Card first = new GrizzlyBears();
        Card second = new LlanowarElves();
        Card third = new RagingGoblin();
        Card untouched = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third, untouched));
        harness.setHand(player1, List.of(new MurmursFromBeyond()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validCardIds()).containsExactly(first.getId(), second.getId(), third.getId());
        assertThat(choice.minCount()).isEqualTo(1);
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(second.getId())))
                .hasMessageContaining("Not your turn");

        harness.handleMultipleCardsChosen(player2, List.of(second.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(first, third);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(second, untouched);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A single revealed card is put into the graveyard without a choice")
    void singleRevealedCardGoesToGraveyard() {
        Card only = new RagingGoblin();
        harness.setLibrary(player1, List.of(only));
        harness.setHand(player1, List.of(new MurmursFromBeyond()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(only);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
