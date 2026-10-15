package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ForsakenCity.class})
class ForsakenCityTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for any color adds the chosen mana")
    void tapsForAnyColor() {
        harness.addToBattlefield(player1, new ForsakenCity());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The land stays tapped through the controller's untap step")
    void doesNotUntapDuringUntapStep() {
        Permanent city = addCreatureReady(player1, new ForsakenCity());
        city.tap();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(city.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Exiling a card during upkeep untaps the land")
    void exilingCardUntapsLand() {
        Permanent city = addCreatureReady(player1, new ForsakenCity());
        city.tap();
        harness.setHand(player1, List.of(new ForsakenCity()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ExileFromHandChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(city.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getName().equals("Forsaken City"));
    }

    @Test
    @DisplayName("Accepting the upkeep ability with an empty hand does not untap the land")
    void emptyHandDoesNotUntapLand() {
        Permanent city = addCreatureReady(player1, new ForsakenCity());
        city.tap();
        harness.setHand(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(city.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Declining the upkeep ability preserves the hand and leaves the land tapped")
    void decliningWithCardInHandDoesNotExileOrUntap() {
        Permanent city = addCreatureReady(player1, new ForsakenCity());
        city.tap();
        ForsakenCity handCard = new ForsakenCity();
        harness.setHand(player1, List.of(handCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(city.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handCard);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("An untapped land still allows exiling a card during upkeep")
    void untappedCityStillAllowsExilingCard() {
        Permanent city = addCreatureReady(player1, new ForsakenCity());
        ForsakenCity handCard = new ForsakenCity();
        harness.setHand(player1, List.of(handCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(city.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card() == handCard);
    }

    @Test
    @DisplayName("The upkeep ability does not trigger during the opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        Permanent city = addCreatureReady(player1, new ForsakenCity());
        city.tap();
        ForsakenCity handCard = new ForsakenCity();
        harness.setHand(player1, List.of(handCard));

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(city.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handCard);
        assertThat(gd.exiledCards).isEmpty();
    }

}
