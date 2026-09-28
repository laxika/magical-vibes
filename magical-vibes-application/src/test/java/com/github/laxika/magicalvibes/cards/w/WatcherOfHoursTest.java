package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WatcherOfHours.class, GrizzlyBears.class})
class WatcherOfHoursTest extends BaseCardTest {

    @Test
    @DisplayName("Suspend exiles Watcher of Hours with six time counters")
    void suspendExilesWithSixTimeCounters() {
        WatcherOfHours card = suspendCard();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 6);
    }

    @Test
    @DisplayName("Surveils 1 whenever a time counter is removed while suspended")
    void surveilsWhenTimeCounterIsRemoved() {
        WatcherOfHours card = suspendCard();
        GrizzlyBears topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 5);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(topCard);
    }

    private WatcherOfHours suspendCard() {
        WatcherOfHours card = new WatcherOfHours();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, null);
        return card;
    }
}
