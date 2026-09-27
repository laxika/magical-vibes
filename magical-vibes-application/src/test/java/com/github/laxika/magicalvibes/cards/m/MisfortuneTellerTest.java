package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MisfortuneTeller.class, Forest.class, GrizzlyBears.class, Shock.class})
class MisfortuneTellerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiling a creature card creates a 2/2 Rogue token")
    void etbExilingCreatureCreatesRogue() {
        Card creature = new GrizzlyBears();

        castAndResolveEnterTrigger(creature);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(creature);
        assertThat(findPermanents(player1, "Rogue")).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("ETB exiling a land card creates a Treasure token")
    void etbExilingLandCreatesTreasure() {
        Card land = new Forest();

        castAndResolveEnterTrigger(land);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(land);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player1, "Rogue")).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("ETB exiling another card gains 3 life")
    void etbExilingOtherCardGainsLife() {
        Card other = new Shock();

        castAndResolveEnterTrigger(other);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(other);
        assertThat(findPermanents(player1, "Rogue")).isEmpty();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Combat damage to a player also exiles a graveyard card and resolves its branch")
    void combatDamageTriggerCreatesTreasureForLand() {
        Card land = new Forest();
        harness.setGraveyard(player2, List.of(land));
        addCreatureReady(player1, new MisfortuneTeller());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        chooseGraveyardCard(land);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(land);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    private void castAndResolveEnterTrigger(Card graveyardCard) {
        harness.setGraveyard(player2, List.of(graveyardCard));
        harness.setHand(player1, List.of(new MisfortuneTeller()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        chooseGraveyardCard(graveyardCard);
        harness.passBothPriorities();
    }

    private void chooseGraveyardCard(Card card) {
        if (gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class) != null) {
            harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        } else {
            harness.handleGraveyardCardChosen(player1, 0);
        }
    }
}
