package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RiddlesInTheDark.class, Island.class, Forest.class, Swamp.class, Plains.class})
class RiddlesInTheDarkTest extends BaseCardTest {

    @Test
    @DisplayName("The opponent can choose the face-down pile")
    void opponentChoosesFaceDownPile() {
        Card island = new Island();
        Card forest = new Forest();
        Card swamp = new Swamp();
        Card plains = new Plains();
        castRiddlesInTheDark(island, forest, swamp, plains);

        harness.handleMultipleCardsChosen(player1, List.of(island.getId(), forest.getId()));

        PendingInteraction.MayAbilityChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.description()).contains("Island", "Forest", "2 cards")
                .doesNotContain("Swamp", "Plains");

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(swamp, plains);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(island, forest);
    }

    @Test
    @DisplayName("Choosing the face-up pile puts it into hand and the face-down pile into the graveyard")
    void opponentChoosesFaceUpPile() {
        Card island = new Island();
        Card forest = new Forest();
        Card swamp = new Swamp();
        Card plains = new Plains();
        castRiddlesInTheDark(island, forest, swamp, plains);

        harness.handleMultipleCardsChosen(player1, List.of(island.getId(), forest.getId()));
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(island, forest)
                .doesNotContain(swamp, plains);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(swamp, plains);
    }

    private void castRiddlesInTheDark(Card... library) {
        harness.setLibrary(player1, List.of(library));
        harness.setHand(player1, List.of(new RiddlesInTheDark()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
