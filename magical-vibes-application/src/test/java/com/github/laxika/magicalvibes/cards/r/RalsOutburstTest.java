package com.github.laxika.magicalvibes.cards.r;

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

@CardUsed({RalsOutburst.class, Forest.class, GrizzlyBears.class})
class RalsOutburstTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to a player, then puts one of the top two cards into hand and the other into the graveyard")
    void dealsDamageAndSeparatesTopCards() {
        Card chosen = new Forest();
        Card other = new GrizzlyBears();
        Card remaining = new Forest();
        harness.setLibrary(player1, List.of(chosen, other, remaining));
        harness.setHand(player1, List.of(new RalsOutburst()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 3);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.LibraryRevealChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(other);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
    }
}
