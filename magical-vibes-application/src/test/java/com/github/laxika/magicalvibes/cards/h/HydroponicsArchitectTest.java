package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HydroponicsArchitect.class, Forest.class})
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

    @Test
    @DisplayName("Repeated attack triggers give the same land multiple entry draws")
    void repeatedAttacksAccumulateEntryDraws() {
        Card firstDraw = new HydroponicsArchitect();
        Card secondDraw = new HydroponicsArchitect();
        Card remaining = new HydroponicsArchitect();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), firstDraw, secondDraw, remaining));
        addCreatureReady(player1, new HydroponicsArchitect());
        addCreatureReady(player1, new HydroponicsArchitect());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCards(gd, player1.getId(), 1));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> harness.playLand(player1, 0));
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        Permanent land = findPermanent(player1, "Forest");
        assertThat(gqs.effectiveBasicLandTypes(gd, land))
                .containsExactlyInAnyOrder(CardSubtype.FOREST, CardSubtype.ISLAND);
        assertThat(gqs.intrinsicBasicLandManaColors(gd, land))
                .containsExactlyInAnyOrder(ManaColor.GREEN, ManaColor.BLUE);
    }

    @Test
    @DisplayName("An opponent's attack changes only that opponent's library")
    void opposingArchitectModifiesItsControllersLibrary() {
        Card unaffected = new Forest();
        Card drawn = new HydroponicsArchitect();
        Card remaining = new HydroponicsArchitect();
        harness.setLibrary(player1, List.of(unaffected));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Forest(), drawn, remaining));
        addCreatureReady(player2, new HydroponicsArchitect());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCards(gd, player2.getId(), 1));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.playLand(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remaining);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unaffected);
    }

    private Card card(String name, CardType type) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        return card;
    }
}
