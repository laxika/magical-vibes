package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.o.Ostracize;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GixianRecycler.class, Millstone.class, Ostracize.class})
class GixianRecyclerTest extends BaseCardTest {

    @Test
    void conjuresDuplicateWhenItDies() {
        Permanent recycler = harness.addToBattlefieldAndReturn(player1, new GixianRecycler());
        recycler.setMarkedDamage(1);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(cardsNamedInGraveyard("Gixian Recycler")).hasSize(2);
    }

    @Test
    void conjuresDuplicateWhenDiscardedFromHand() {
        harness.setHand(player1, List.of(new GixianRecycler()));
        harness.setHand(player2, List.of(new Ostracize()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);

        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        assertThat(cardsNamedInGraveyard("Gixian Recycler")).hasSize(2);
    }

    @Test
    void conjuresDuplicateWhenMilled() {
        GixianRecycler recycler = new GixianRecycler();
        harness.setLibrary(player1, List.of(recycler));
        harness.addToBattlefieldAndReturn(player2, new Millstone());
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(cardsNamedInGraveyard("Gixian Recycler")).hasSize(2);
    }

    @Test
    void conjuredDuplicateDoesNotRetrigger() {
        Permanent recycler = harness.addToBattlefieldAndReturn(player1, new GixianRecycler());
        recycler.setMarkedDamage(1);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(cardsNamedInGraveyard("Gixian Recycler")).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void unearthReturnsItWithHasteAndExilesItAtEndOfTurn() {
        harness.setGraveyard(player1, List.of(new GixianRecycler()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Gixian Recycler");
        assertThat(returned.getGrantedKeywords()).contains(Keyword.HASTE);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Gixian Recycler");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Gixian Recycler"));
    }

    private List<Card> cardsNamedInGraveyard(String name) {
        return gd.playerGraveyards.get(player1.getId()).stream()
                .filter(card -> card.getName().equals(name))
                .toList();
    }
}
