package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DraugrThoughtThiefTest extends BaseCardTest {

    private void castDraugrThoughtThief() {
        harness.setHand(player1, List.of(new DraugrThoughtThief()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
    }

    @Test
    @DisplayName("ETB target selection only offers opponents")
    void targetFilterExcludesController() {
        castDraugrThoughtThief();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .doesNotContain(player1.getId())
                .containsExactly(player2.getId());
    }

    @Test
    @DisplayName("Controller may put the target opponent's top card into their graveyard")
    void putsTopCardIntoTargetGraveyardWhenAccepted() {
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player2.getId()).add(0, topCard);

        castDraugrThoughtThief();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("Declining leaves the target opponent's top card on their library")
    void leavesTopCardWhenDeclined() {
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player2.getId()).add(0, topCard);
        int deckBefore = gd.playerDecks.get(player2.getId()).size();

        castDraugrThoughtThief();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(topCard);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckBefore);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(topCard);
    }
}
