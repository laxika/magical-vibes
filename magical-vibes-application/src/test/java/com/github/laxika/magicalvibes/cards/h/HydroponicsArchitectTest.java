package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(HydroponicsArchitect.class)
class HydroponicsArchitectTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking makes a random library land an Island that draws on entry")
    void attackPerpetuallyChangesLandAndAddsEntryDraw() {
        Card nonland = card("Nonland", CardType.CREATURE);
        Card land = card("Utility Land", CardType.LAND);
        Card draw = card("Drawn Card", CardType.CREATURE);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(nonland, land, draw));
        addCreatureReady(player1, new HydroponicsArchitect());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCards(gd, player1.getId(), 2));
        assertThat(gd.playerHands.get(player1.getId())).contains(nonland).hasSize(2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.playLand(player1, 1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(nonland, draw);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Attacking does nothing when the library has no land cards")
    void attackWithNoLandDoesNothing() {
        Card nonland = card("Nonland", CardType.CREATURE);
        harness.setLibrary(player1, List.of(nonland));
        addCreatureReady(player1, new HydroponicsArchitect());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);
    }

    private Card card(String name, CardType type) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        return card;
    }
}
