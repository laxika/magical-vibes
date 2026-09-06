package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.ImposingVantasaur;
import com.github.laxika.magicalvibes.cards.i.IrrigatedFarmland;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnpredictableCyclone.class, ImposingVantasaur.class, IrrigatedFarmland.class,
        Forest.class, GrizzlyBears.class})
class UnpredictableCycloneTest extends BaseCardTest {

    @Test
    @CardUsed(com.github.laxika.magicalvibes.cards.q.QuantumRiddler.class)
    void additionalCyclingDrawKeepsItsSourceAfterTheFirstCastChoice() {
        harness.addToBattlefield(player1, new UnpredictableCyclone());
        harness.addToBattlefield(player1, new com.github.laxika.magicalvibes.cards.q.QuantumRiddler());
        harness.setHand(player1, List.of(new ImposingVantasaur()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.pendingCardDraws).isEmpty();
    }

    @Test
    @DisplayName("Cycling digs to a shared card type and offers that card for free")
    void cyclingDigsToSharedTypeAndOffersFreeCast() {
        Permanent cyclone = harness.addToBattlefieldAndReturn(player1, new UnpredictableCyclone());
        Card land = new Forest();
        Card creature = new GrizzlyBears();
        harness.setHand(player1, List.of(new ImposingVantasaur()));
        harness.setLibrary(player1, List.of(land, creature));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Imposing Vantasaur");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getCardsExiledByPermanent(cyclone.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
    }

    @Test
    @DisplayName("Declining the free cast puts every exiled card on the bottom")
    void decliningFreeCastBottomsAllExiledCards() {
        Permanent cyclone = harness.addToBattlefieldAndReturn(player1, new UnpredictableCyclone());
        Card land = new Forest();
        Card creature = new GrizzlyBears();
        harness.setHand(player1, List.of(new ImposingVantasaur()));
        harness.setLibrary(player1, List.of(land, creature));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Imposing Vantasaur");
        assertThat(gd.getCardsExiledByPermanent(cyclone.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, creature);
    }

    @Test
    @DisplayName("If no shared type is found, the exiled cards return to the bottom")
    void noSharedTypeBottomsExiledCards() {
        Permanent cyclone = harness.addToBattlefieldAndReturn(player1, new UnpredictableCyclone());
        Card land = new Forest();
        harness.setHand(player1, List.of(new ImposingVantasaur()));
        harness.setLibrary(player1, List.of(land));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Imposing Vantasaur");
        assertThat(gd.getCardsExiledByPermanent(cyclone.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
    }

    @Test
    @DisplayName("Cycling a land is not replaced")
    void cyclingLandDrawsNormally() {
        Permanent cyclone = harness.addToBattlefieldAndReturn(player1, new UnpredictableCyclone());
        Card land = new IrrigatedFarmland();
        Card creature = new GrizzlyBears();
        harness.setHand(player1, List.of(land));
        harness.setLibrary(player1, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Irrigated Farmland");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.getCardsExiledByPermanent(cyclone.getId())).isEmpty();
    }
}
