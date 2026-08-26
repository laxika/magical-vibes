package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
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

@CardUsed({RatsFeast.class, GrizzlyBears.class, LlanowarElves.class})
class RatsFeastTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles exactly X target cards from one graveyard")
    void exilesExactlyXCardsFromOneGraveyard() {
        Card first = new GrizzlyBears();
        Card second = new LlanowarElves();
        Card untouched = new GrizzlyBears();
        RatsFeast spell = new RatsFeast();
        harness.setGraveyard(player1, List.of(first, second, untouched));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 2);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.minCount()).isEqualTo(2);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(untouched, spell);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(first, second);
    }

    @Test
    @DisplayName("Rejects fewer than X target cards")
    void rejectsFewerThanXTargets() {
        Card first = new GrizzlyBears();
        Card second = new LlanowarElves();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new RatsFeast()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 2);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(first.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("exactly 2");
    }

    @Test
    @DisplayName("Requires all X targets to come from one graveyard")
    void requiresOneGraveyard() {
        Card ownCard = new GrizzlyBears();
        Card opponentCard = new LlanowarElves();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.setHand(player1, List.of(new RatsFeast()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("single graveyard");
    }

    @Test
    @DisplayName("With X=0, exiles no cards")
    void zeroExilesNothing() {
        Card graveyardCard = new GrizzlyBears();
        RatsFeast spell = new RatsFeast();
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardCard, spell);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }
}
