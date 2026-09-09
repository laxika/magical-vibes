package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UlamogTheCeaselessHunger.class, GrizzlyBears.class})
class UlamogTheCeaselessHungerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Ulamog exiles two target permanents")
    void castingExilesTwoTargetPermanents() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new UlamogTheCeaselessHunger()));
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, firstTarget.getId());
        harness.handlePermanentChosen(player1, secondTarget.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(firstTarget.getOriginalCard().getId())).isNotNull();
        assertThat(gd.findExiledCard(secondTarget.getOriginalCard().getId())).isNotNull();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Ulamog, the Ceaseless Hunger");
    }

    @Test
    @DisplayName("Attacking Ulamog exiles the top twenty cards of the defending player's library")
    void attackingExilesTopTwentyCardsOfDefendingLibrary() {
        Permanent ulamog = addCreatureReady(player1, new UlamogTheCeaselessHunger());
        List<Card> library = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            library.add(new GrizzlyBears());
        }
        harness.setLibrary(player2, library);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(ulamog)));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId()))
                .containsExactlyElementsOf(library.subList(20, 25));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactlyElementsOf(library.subList(0, 20).stream().map(Card::getId).toList());
    }
}
