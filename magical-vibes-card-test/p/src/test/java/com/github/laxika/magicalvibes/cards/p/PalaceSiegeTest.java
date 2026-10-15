package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PalaceSiege.class, GrizzlyBears.class, Shock.class, Naturalize.class})
class PalaceSiegeTest extends BaseCardTest {

    @Test
    @DisplayName("Khans returns a target creature card from the graveyard to hand")
    void khansReturnsTargetCreatureCard() {
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(bears, shock));
        castSiege("Khans");

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(bears).contains(shock);
    }

    @Test
    @DisplayName("Khans does not trigger without a creature card in the graveyard")
    void khansDoesNotTriggerWithoutCreatureCard() {
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        castSiege("Khans");

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(shock);
    }

    @Test
    @DisplayName("Dragons makes each opponent lose 2 life and the controller gain 2 life")
    void dragonsDrainsOpponents() {
        castSiege("Dragons");

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    private void castSiege(String mode) {
        harness.castFromHand(player1, new PalaceSiege(), "{3}{B}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, mode);
    }

    @Test
    @DisplayName("Dragons does not return creatures and does not trigger during an opponent's upkeep")
    void dragonsOnlyDrainsOnControllersUpkeep() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        castSiege("Dragons");

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(bears);
    }

    @Test
    @DisplayName("Khans cannot return a creature from an opponent's graveyard")
    void khansOnlyUsesControllersGraveyard() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));
        castSiege("Khans");

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(bears);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Khans still returns its target after Palace Siege is destroyed in response")
    void khansResolvesAfterSourceIsDestroyed() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        castSiege("Khans");
        var siegeId = findPermanent(player1, "Palace Siege").getId();

        advanceToUpkeep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, siegeId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Palace Siege");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(bears);
    }

    @Test
    @DisplayName("Khans returns only the selected creature and does not drain life")
    void khansReturnsOnlySelectedCreatureWithoutDraining() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second));
        castSiege("Khans");

        advanceToUpkeep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(second).doesNotContain(first);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
