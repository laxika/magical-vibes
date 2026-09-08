package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        harness.setHand(player1, List.of(new PalaceSiege()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, mode);
    }
}
