package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FinaleOfRevelation.class, Forest.class, GrizzlyBears.class})
class FinaleOfRevelationTest extends BaseCardTest {

    @Test
    void belowTenOnlyDraws() {
        FinaleOfRevelation finale = new FinaleOfRevelation();
        Card graveyardCard = new GrizzlyBears();
        harness.setHand(player1, List.of(finale));
        harness.setLibrary(player1, cards(20));
        gd.playerGraveyards.get(player1.getId()).add(graveyardCard);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        land.tap();

        cast(9);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(9);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardCard);
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playersWithNoMaximumHandSize).doesNotContain(player1.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(finale);
    }

    @Test
    void tenShufflesGraveyardDrawsUntapsAndGrantsNoMaximumHandSize() {
        FinaleOfRevelation finale = new FinaleOfRevelation();
        Card graveyardCard = new GrizzlyBears();
        harness.setHand(player1, List.of(finale));
        List<Card> library = cards(20);
        harness.setLibrary(player1, library);
        gd.playerGraveyards.get(player1.getId()).add(graveyardCard);

        List<Permanent> lands = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
            land.tap();
            lands.add(land);
        }
        for (int i = 0; i < 3; i++) {
            Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
            land.tap();
            lands.add(land);
        }
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.tap();

        cast(10);

        harness.handleMultiplePermanentsChosen(player1,
                lands.subList(0, 5).stream().map(Permanent::getId).toList());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(10);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        List<Card> libraryAndHand = new ArrayList<>(gd.playerDecks.get(player1.getId()));
        libraryAndHand.addAll(gd.playerHands.get(player1.getId()));
        assertThat(libraryAndHand).contains(graveyardCard).hasSize(21);
        assertThat(gd.playersWithNoMaximumHandSize).contains(player1.getId());
        assertThat(lands.subList(0, 5)).allMatch(land -> !land.isTapped());
        assertThat(lands.subList(5, 7)).allMatch(Permanent::isTapped);
        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(finale);
    }

    private void cast(int xValue) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
        harness.castSorcery(player1, 0, xValue);
        harness.passBothPriorities();
    }

    private List<Card> cards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new GrizzlyBears());
        }
        return cards;
    }
}
