package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.CabalCoffers;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrganGrinder.class, CabalCoffers.class})
class OrganGrinderTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles three graveyard cards and makes a target player lose 3 life")
    void exilesThreeCardsAndMakesTargetPlayerLoseLife() {
        Permanent organGrinder = addReadyOrganGrinder(player1);
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, List.of(new OrganGrinder(), new OrganGrinder(), new OrganGrinder()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(organGrinder.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Can target yourself")
    void canTargetYourself() {
        addReadyOrganGrinder(player1);
        harness.setLife(player1, 20);
        harness.setGraveyard(player1, List.of(new OrganGrinder(), new OrganGrinder(), new OrganGrinder()));

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Cannot activate without three cards in the graveyard")
    void cannotActivateWithoutThreeGraveyardCards() {
        addReadyOrganGrinder(player1);
        harness.setGraveyard(player1, List.of(new OrganGrinder(), new OrganGrinder()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Exiles exactly three chosen cards when more are available")
    void exilesExactlyThreeChosenCardsWhenMoreAreAvailable() {
        addReadyOrganGrinder(player1);
        harness.setLife(player2, 20);
        List<Card> graveyard = List.of(
                new OrganGrinder(), new CabalCoffers(), new OrganGrinder(), new CabalCoffers());
        harness.setGraveyard(player1, graveyard);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ActivatedAbilityGraveyardExileCostChoice.class);
        List<Card> chosenCards = graveyard.subList(1, 4);
        harness.handleMultipleCardsChosen(player1, chosenCards.stream().map(Card::getId).toList());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyard.get(0));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrderElementsOf(chosenCards);
    }

    @Test
    @DisplayName("Cannot use cards from an opponent's graveyard to pay the cost")
    void cannotUseOpponentsGraveyardToPayCost() {
        Permanent organGrinder = addReadyOrganGrinder(player1);
        List<Card> ownGraveyard = List.of(new OrganGrinder(), new OrganGrinder());
        harness.setGraveyard(player1, ownGraveyard);
        harness.setGraveyard(player2, List.of(new OrganGrinder()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(organGrinder.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(ownGraveyard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate while the creature has summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new OrganGrinder());
        prepareForAbilityActivation(player1);
        harness.setGraveyard(player1, List.of(new OrganGrinder(), new OrganGrinder(), new OrganGrinder()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyOrganGrinder(Player player) {
        Permanent permanent = addCreatureReady(player, new OrganGrinder());
        prepareForAbilityActivation(player);
        return permanent;
    }

    private void prepareForAbilityActivation(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
