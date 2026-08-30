package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MonasteryMessenger.class, GrizzlyBears.class, Island.class, Shock.class})
class MonasteryMessengerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers only noncreature, nonland cards from its controller's graveyard")
    void etbOffersOnlyNoncreatureNonlandCards() {
        Shock shock = new Shock();
        GrizzlyBears creature = new GrizzlyBears();
        Island land = new Island();
        harness.setGraveyard(player1, List.of(shock, creature, land));

        castMessenger();

        List<UUID> validIds = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds();
        assertThat(validIds).containsExactly(shock.getId());
    }

    @Test
    @DisplayName("ETB puts the chosen card on top of its controller's library")
    void etbPutsChosenCardOnTopOfLibrary() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.setLibrary(player1, List.of(new Island()));

        castMessenger();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(shock);
        harness.assertNotInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("ETB can choose no card")
    void etbCanChooseNoCard() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.setLibrary(player1, List.of());

        castMessenger();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Shock");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("ETB does not trigger when there is no legal graveyard target")
    void etbDoesNotTriggerWithoutLegalTarget() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Island()));

        castMessenger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNull();
    }

    private void castMessenger() {
        harness.setHand(player1, List.of(new MonasteryMessenger()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
