package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AxavarFateThief.class, Forest.class, GrizzlyBears.class})
class AxavarFateThiefTest extends BaseCardTest {

    @Test
    void voidDiscardsThenHeistsThreeRandomNonlandCards() {
        harness.addToBattlefield(player1, new AxavarFateThief());
        Permanent leavingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, leavingCreature));

        Card discarded = new Forest();
        Card land = new Forest();
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player2, List.of(land, first, second, third));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(
                PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).hasSize(3)
                .allMatch(card -> !card.hasType(com.github.laxika.magicalvibes.model.CardType.LAND));

        Card chosen = search.params().cards().getFirst();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(chosen);
        assertThat(gd.exilePlayPermissions).containsEntry(chosen.getId(), player1.getId());
        assertThat(gd.exilePlayAnyManaTypeWhileExiled).contains(chosen.getId());
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(chosen);
    }
}
