package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DiregrafScavenger.class, Forest.class, GrizzlyBears.class})
class DiregrafScavengerTest extends BaseCardTest {

    @Test
    @DisplayName("Exiling a creature card makes each opponent lose 2 life and gains 2 life")
    void creatureCardExiledAppliesLifeRider() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        castAndResolveCreatureToTargetingPrompt();

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(creature);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
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
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Choosing no card does not apply the life rider")
    void choosingNoCardDoesNotApplyLifeRider() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        castAndResolveCreatureToTargetingPrompt();

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private void castAndResolveCreatureToTargetingPrompt() {
        harness.setHand(player1, List.of(new DiregrafScavenger()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
    }
}
