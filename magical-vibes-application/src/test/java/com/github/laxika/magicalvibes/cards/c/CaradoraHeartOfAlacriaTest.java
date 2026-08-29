package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BrightfieldGlider;
import com.github.laxika.magicalvibes.cards.d.DaringMechanic;
import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CaradoraHeartOfAlacriaTest extends BaseCardTest {

    @Test
    @DisplayName("searches for a Mount or Vehicle")
    void searchesForMountOrVehicle() {
        castCaradora();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new BrightfieldGlider(), new DuskLegionDreadnought(), new GrizzlyBears()));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).hasSize(2);
        assertThat(search.params().cards()).allMatch(card -> card.getSubtypes().contains(CardSubtype.MOUNT)
                || card.getSubtypes().contains(CardSubtype.VEHICLE));

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("adds a counter to a Vehicle you control")
    void addsCounterToControlledVehicle() {
        harness.addToBattlefield(player1, new CaradoraHeartOfAlacria());
        harness.addToBattlefield(player1, new DaringMechanic());
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new DuskLegionDreadnought());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 1, null, vehicle.getId());
        harness.passBothPriorities();

        assertThat(vehicle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("does not add a counter to an opponent's Vehicle")
    void doesNotAddCounterToOpponentsVehicle() {
        harness.addToBattlefield(player1, new CaradoraHeartOfAlacria());
        harness.addToBattlefield(player1, new DaringMechanic());
        Permanent vehicle = harness.addToBattlefieldAndReturn(player2, new DuskLegionDreadnought());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 1, null, vehicle.getId());
        harness.passBothPriorities();

        assertThat(vehicle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void castCaradora() {
        harness.setHand(player1, List.of(new CaradoraHeartOfAlacria()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
    }
}
