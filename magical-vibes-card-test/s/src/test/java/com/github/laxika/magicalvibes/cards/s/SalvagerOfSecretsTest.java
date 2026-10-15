package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SalvagerOfSecrets.class, Shock.class, Divination.class, GreenwoodSentinel.class})
class SalvagerOfSecretsTest extends BaseCardTest {

    /** Casts Salvager of Secrets and resolves it so its ETB trigger sets up graveyard targeting. */
    private void castSalvager() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new SalvagerOfSecrets(), "{3}{U}{U}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB returns a targeted instant card from the graveyard to hand")
    void etbReturnsInstantToHand() {
        Shock bolt = new Shock();
        harness.setGraveyard(player1, List.of(bolt));

        castSalvager();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bolt.getId()));
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Shock");
        harness.assertInHand(player1, "Shock");
    }

    @Test
    @DisplayName("ETB can return a sorcery card from the graveyard to hand")
    void etbReturnsSorceryToHand() {
        Divination divination = new Divination();
        harness.setGraveyard(player1, List.of(divination));

        castSalvager();

        harness.handleMultipleCardsChosen(player1, List.of(divination.getId()));
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Divination");
        harness.assertInHand(player1, "Divination");
    }

    @Test
    @DisplayName("A creature card is not a legal target")
    void creatureCardNotTargetable() {
        harness.setGraveyard(player1, List.of(new GreenwoodSentinel()));

        castSalvager();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Greenwood Sentinel");
    }

    @Test
    @DisplayName("Only your instant and sorcery cards are offered, and exactly one target is required")
    void targetsExactlyOneMatchingCardFromYourGraveyard() {
        Shock instant = new Shock();
        Divination sorcery = new Divination();
        GreenwoodSentinel creature = new GreenwoodSentinel();
        Shock opposingInstant = new Shock();
        harness.setGraveyard(player1, List.of(instant, sorcery, creature));
        harness.setGraveyard(player2, List.of(opposingInstant));

        castSalvager();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(instant.getId(), sorcery.getId());
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(instant.getId(), sorcery.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Shock");
        harness.assertInGraveyard(player1, "Divination");
        harness.assertInGraveyard(player1, "Greenwood Sentinel");
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("No target is available when only the opponent has an instant in their graveyard")
    void cannotTargetOpponentsGraveyard() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new Shock()));

        castSalvager();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Salvager of Secrets");
        harness.assertInGraveyard(player2, "Shock");
        harness.assertNotInHand(player1, "Shock");
    }

    @Test
    @DisplayName("A target that leaves the graveyard is not returned and another card is not chosen")
    void missingTargetDoesNotReturnAnotherCard() {
        Shock instant = new Shock();
        Divination sorcery = new Divination();
        harness.setGraveyard(player1, List.of(instant, sorcery));

        castSalvager();
        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));
        harness.setGraveyard(player1, List.of(sorcery));
        gd.getPlayerExiledCards(player1.getId()).add(instant);
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Shock");
        harness.assertNotInHand(player1, "Divination");
        harness.assertInGraveyard(player1, "Divination");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The entry trigger still returns its target after Salvager is destroyed")
    void triggerResolvesAfterSourceDies() {
        Divination sorcery = new Divination();
        harness.setGraveyard(player1, List.of(sorcery));

        castSalvager();
        harness.handleMultipleCardsChosen(player1, List.of(sorcery.getId()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Salvager of Secrets"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Salvager of Secrets");
        harness.assertInGraveyard(player1, "Salvager of Secrets");
        harness.assertInGraveyard(player1, "Divination");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Divination");
        harness.assertNotInGraveyard(player1, "Divination");
    }
}
