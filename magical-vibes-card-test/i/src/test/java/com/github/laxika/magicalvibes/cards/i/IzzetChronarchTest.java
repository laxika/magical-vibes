package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.CausticRain;
import com.github.laxika.magicalvibes.cards.c.Cremate;
import com.github.laxika.magicalvibes.cards.g.GhostWarden;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IzzetChronarch.class, CausticRain.class, Cremate.class, GhostWarden.class})
class IzzetChronarchTest extends BaseCardTest {

    private void castIzzetChronarch() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new IzzetChronarch(), "{3}{U}{R}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB returns a targeted instant card from graveyard to hand")
    void etbReturnsInstantToHand() {
        Cremate cremate = new Cremate();
        harness.setGraveyard(player1, List.of(cremate));

        castIzzetChronarch();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(cremate.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Cremate");
        harness.assertNotInGraveyard(player1, "Cremate");
    }

    @Test
    @DisplayName("ETB returns a targeted sorcery card from graveyard to hand")
    void etbReturnsSorceryToHand() {
        CausticRain causticRain = new CausticRain();
        harness.setGraveyard(player1, List.of(causticRain));

        castIzzetChronarch();

        harness.handleMultipleCardsChosen(player1, List.of(causticRain.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Caustic Rain");
        harness.assertNotInGraveyard(player1, "Caustic Rain");
    }

    @Test
    @DisplayName("ETB returns only the chosen card when multiple legal targets exist")
    void etbReturnsOnlyChosenCard() {
        Cremate cremate = new Cremate();
        CausticRain causticRain = new CausticRain();
        harness.setGraveyard(player1, List.of(cremate, causticRain));

        castIzzetChronarch();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(cremate.getId(), causticRain.getId());

        harness.handleMultipleCardsChosen(player1, List.of(cremate.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Cremate");
        harness.assertNotInGraveyard(player1, "Cremate");
        harness.assertInGraveyard(player1, "Caustic Rain");
        harness.assertNotInHand(player1, "Caustic Rain");
    }

    @Test
    @DisplayName("ETB excludes creatures and opposing cards when legal targets exist")
    void mixedGraveyardsOfferOnlyOwnInstantAndSorcery() {
        Cremate cremate = new Cremate();
        CausticRain causticRain = new CausticRain();
        harness.setGraveyard(player1, List.of(cremate, causticRain, new GhostWarden()));
        harness.setGraveyard(player2, List.of(new Cremate()));

        castIzzetChronarch();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(cremate.getId(), causticRain.getId());

        harness.handleMultipleCardsChosen(player1, List.of(causticRain.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Caustic Rain");
        harness.assertInGraveyard(player1, "Ghost Warden");
        harness.assertInGraveyard(player2, "Cremate");
    }

    @Test
    @DisplayName("ETB does not choose a replacement when its target is exiled in response")
    void exiledTargetIsNotReturnedOrReplaced() {
        CausticRain target = new CausticRain();
        Cremate other = new Cremate();
        harness.setGraveyard(player1, List.of(target, other));

        castIzzetChronarch();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));

        harness.setHand(player1, List.of(new Cremate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Caustic Rain");
        harness.assertNotInGraveyard(player1, "Caustic Rain");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target);
        harness.assertInGraveyard(player1, "Cremate");
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(other);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A creature card in the graveyard is not a legal target")
    void creatureNotTargetable() {
        harness.setGraveyard(player1, List.of(new GhostWarden()));

        castIzzetChronarch();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Ghost Warden");
    }

    @Test
    @DisplayName("An instant in an opponent's graveyard is not a legal target")
    void opponentGraveyardNotTargetable() {
        harness.setGraveyard(player2, List.of(new Cremate()));

        castIzzetChronarch();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player2, "Cremate");
    }

    @Test
    @DisplayName("Empty graveyard produces no trigger")
    void emptyGraveyardNoTrigger() {
        castIzzetChronarch();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }
}
