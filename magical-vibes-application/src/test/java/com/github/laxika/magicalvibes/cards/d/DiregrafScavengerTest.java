package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DiregrafScavenger.class, Forest.class})
class DiregrafScavengerTest extends BaseCardTest {

    @Test
    @DisplayName("Exiling a creature card makes each opponent lose 2 life and gains 2 life")
    void creatureCardExiledAppliesLifeRider() {
        Card creature = new DiregrafScavenger();
        harness.setGraveyard(player2, List.of(creature));
        castAndResolveCreatureToTargetingPrompt();

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(creature);
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Exiling a noncreature card does not apply the life rider")
    void nonCreatureCardExiledDoesNotApplyLifeRider() {
        Card land = new Forest();
        harness.setGraveyard(player2, List.of(land));
        castAndResolveCreatureToTargetingPrompt();

        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(land);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Choosing no card does not apply the life rider")
    void choosingNoCardDoesNotApplyLifeRider() {
        Card creature = new DiregrafScavenger();
        harness.setGraveyard(player2, List.of(creature));
        castAndResolveCreatureToTargetingPrompt();

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A creature in the controller's graveyard can be exiled for the life rider")
    void creatureInOwnGraveyardAppliesLifeRider() {
        Card creature = new DiregrafScavenger();
        harness.setGraveyard(player1, List.of(creature));
        castAndResolveCreatureToTargetingPrompt();

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("A target that leaves its graveyard before resolution does not apply the life rider")
    void missingTargetDoesNotApplyLifeRider() {
        Card creature = new DiregrafScavenger();
        harness.setGraveyard(player2, List.of(creature));
        castAndResolveCreatureToTargetingPrompt();

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.setGraveyard(player2, List.of());
        harness.setHand(player2, List.of(creature));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(creature);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Empty graveyards allow the trigger to resolve without changing life totals")
    void emptyGraveyardsDoNotApplyLifeRider() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        harness.setHand(player1, List.of(new DiregrafScavenger()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Diregraf Scavenger");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private void castAndResolveCreatureToTargetingPrompt() {
        harness.setHand(player1, List.of(new DiregrafScavenger()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
    }
}
